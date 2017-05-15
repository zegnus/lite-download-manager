package com.novoda.library;

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
    private final List<DownloadBatchCallback> callbacks;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final FileSizeRequester fileSizeRequester;
    private final Persistence persistence;
    private final Downloader downloader;

    private DownloadServiceCommands downloadService;

    LiteDownloadManager(Handler callbackHandler,
                        Map<DownloadBatchId, DownloadBatch> downloadBatchMap,
                        List<DownloadBatchCallback> callbacks,
                        FileSizeRequester fileSizeRequester,
                        Persistence persistence,
                        Downloader downloader) {
        this.callbackHandler = callbackHandler;
        this.downloadBatchMap = downloadBatchMap;
        this.callbacks = callbacks;
        this.fileSizeRequester = fileSizeRequester;
        this.persistence = persistence;
        this.downloader = downloader;
    }

    void setDownloadService(DownloadServiceCommands downloadService) {
        this.downloadService = downloadService;
        synchronized (waitForDownloadService) {
            waitForDownloadService.notifyAll();
        }
    }

    @Override
    public DownloadBatchId download(Batch batch) {
        DownloadBatch downloadBatch = DownloadBatch.from(batch, fileSizeRequester, persistence, downloader);
        download(downloadBatch);
        return downloadBatch.getId();
    }

    private void download(DownloadBatch downloadBatch) {
        downloadBatchMap.put(downloadBatch.getId(), downloadBatch);
        if (downloadService == null) {
            ensureDownloadServiceExistsAndProceed(downloadBatch);
        } else {
            executeDownload(downloadBatch);
        }
    }

    private void ensureDownloadServiceExistsAndProceed(final DownloadBatch downloadBatch) {
        executor.submit(new Runnable() {
            @Override
            public void run() {
                waitForDownloadService();
                executeDownload(downloadBatch);
            }
        });
    }

    private void executeDownload(final DownloadBatch downloadBatch) {
        downloadService.download(downloadBatch, new DownloadBatchCallback() {
            @Override
            public void onUpdate(final DownloadBatchStatus downloadBatchStatus) {
                callbackHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        for (DownloadBatchCallback callback : callbacks) {
                            callback.onUpdate(downloadBatchStatus);
                        }
                    }
                });
            }
        });
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
    public void resume(DownloadBatchId downloadBatchId) {
        DownloadBatch downloadBatch = downloadBatchMap.get(downloadBatchId);
        if (downloadBatch == null) {
            return;
        }
        downloadBatchMap.remove(downloadBatchId);
        downloadBatch.resume();

        download(downloadBatch);
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
    public void addDownloadBatchCallback(DownloadBatchCallback downloadBatchCallback) {
        callbacks.add(downloadBatchCallback);
    }

    @Override
    public void removeDownloadBatchCallback(DownloadBatchCallback downloadBatchCallback) {
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
