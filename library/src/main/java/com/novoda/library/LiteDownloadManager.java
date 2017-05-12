package com.novoda.library;

import android.content.Context;
import android.os.Handler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class LiteDownloadManager implements LiteDownloadManagerCommands {

    private final Handler callbackHandler;
    private final Map<DownloadBatchId, DownloadBatch> downloadBatchMap;
    private final Object waitForDownloadService = new Object();
    private final List<DownloadBatch.Callback> callbacks;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private DownloadServiceCommands downloadService;

    LiteDownloadManager(Handler callbackHandler,
                        Map<DownloadBatchId, DownloadBatch> downloadBatchMap,
                        List<DownloadBatch.Callback> callbacks) {
        this.callbackHandler = callbackHandler;
        this.downloadBatchMap = downloadBatchMap;
        this.callbacks = callbacks;
    }

    void setDownloadService(DownloadServiceCommands downloadService) {
        this.downloadService = downloadService;
        synchronized (waitForDownloadService) {
            waitForDownloadService.notifyAll();
        }
    }

    @Override
    public void download(final DownloadBatch downloadBatch, Context context) {
        downloadBatchMap.put(downloadBatch.getId(), downloadBatch);
        if (downloadService == null) {
            ensureDownloadServiceExistsAndProceed(downloadBatch, context);
        } else {
            executeDownload(downloadBatch, context);
        }
    }

    private void ensureDownloadServiceExistsAndProceed(final DownloadBatch downloadBatch, final Context context) {
        executor.submit(new Runnable() {
            @Override
            public void run() {
                waitForDownloadService();
                executeDownload(downloadBatch, context);
            }
        });
    }

    private void executeDownload(final DownloadBatch downloadBatch, Context context) {
        downloadService.download(downloadBatch, new DownloadBatch.Callback() {
            @Override
            public void onUpdate(final DownloadBatchStatus downloadBatchStatus) {
                callbackHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        for (DownloadBatch.Callback callback : callbacks) {
                            callback.onUpdate(downloadBatchStatus);
                        }
                    }
                });
            }
        }, context);
    }

    private void waitForDownloadService() {
        if (downloadService == null) {
            try {
                synchronized (waitForDownloadService) {
                    waitForDownloadService.wait();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void pause(DownloadBatchId downloadBatchId) {
        DownloadBatch downloadBatch = downloadBatchMap.get(downloadBatchId);
        if (downloadBatch == null) {
            return;
        }
        downloadBatch.pause();
    }

    @Override
    public void resume(DownloadBatchId downloadBatchId, Context context) {
        DownloadBatch downloadBatch = downloadBatchMap.get(downloadBatchId);
        if (downloadBatch == null) {
            return;
        }
        downloadBatchMap.remove(downloadBatchId);
        downloadBatch.resume();
        download(downloadBatch, context);
    }

    @Override
    public void delete(DownloadBatchId downloadBatchId) {
        DownloadBatch downloadBatch = downloadBatchMap.get(downloadBatchId);
        if (downloadBatch == null) {
            return;
        }
        downloadBatchMap.remove(downloadBatchId);
        downloadBatch.delete();
    }

    @Override
    public void addDownloadBatchCallback(DownloadBatch.Callback downloadBatchCallback) {
        callbacks.add(downloadBatchCallback);
    }

    @Override
    public void removeDownloadBatchCallback(DownloadBatch.Callback downloadBatchCallback) {
        if (callbacks.contains(downloadBatchCallback)) {
            callbacks.remove(downloadBatchCallback);
        }
    }

    @Override
    public List<DownloadBatchStatus> getAllDownloadBatchStatuses() {
        List<DownloadBatchStatus> downloadBatchStatuses = new ArrayList<>(downloadBatchMap.size());

        for (DownloadBatch downloadBatch : downloadBatchMap.values()) {
            downloadBatchStatuses.add(downloadBatch.getDownloadBatchStatus());
        }

        return downloadBatchStatuses;
    }
}
