package com.novoda.library;

import android.os.Handler;

import java.util.List;
import java.util.Map;

class LiteDownloadManager implements LiteDownloadManagerCommands {

    private final DownloadServiceCommands downloadService;
    private final Handler callbackHandler;
    private final Map<DownloadBatchId, DownloadBatch> downloadBatchMap;
    private final List<DownloadBatch.Callback> callbacks;

    LiteDownloadManager(DownloadServiceCommands downloadService,
                        Handler callbackHandler,
                        Map<DownloadBatchId, DownloadBatch> downloadBatchMap,
                        List<DownloadBatch.Callback> callbacks) {
        this.downloadService = downloadService;
        this.callbackHandler = callbackHandler;
        this.downloadBatchMap = downloadBatchMap;
        this.callbacks = callbacks;
    }

    @Override
    public void download(final DownloadBatch downloadBatch) {
        downloadBatchMap.put(downloadBatch.getId(), downloadBatch);
        downloadService.download(downloadBatch, new DownloadBatch.Callback() {
            @Override
            public void onUpdate(final DownloadBatchStatus downloadBatchStatus) {
                callbackHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        for (DownloadBatch.Callback callback : callbacks) {
                            callback.onUpdate(downloadBatchStatus);
                        }
                        if (downloadBatchStatus.isCompleted()) {
                            downloadBatchMap.remove(downloadBatch.getId());
                        }
                    }
                });
            }
        });
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
        downloadBatch.unpause();
        download(downloadBatch);
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
}
