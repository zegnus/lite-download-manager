package com.novoda.library;

import android.os.Handler;

import java.util.Map;

class LiteDownloadManager implements LiteDownloadManagerCommands {

    private final DownloadServiceCommands downloadService;
    private final Handler callbackHandler;
    private final Map<DownloadBatchId, DownloadBatch> downloadBatchMap;

    private DownloadBatch.Callback callback = DownloadBatch.Callback.NO_OP;

    LiteDownloadManager(DownloadServiceCommands downloadService,
                        Handler callbackHandler,
                        Map<DownloadBatchId, DownloadBatch> downloadBatchMap) {
        this.downloadService = downloadService;
        this.callbackHandler = callbackHandler;
        this.downloadBatchMap = downloadBatchMap;
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
                        callback.onUpdate(downloadBatchStatus);
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
        callback = downloadBatchCallback;
    }
}
