package com.novoda.library;

import android.os.Handler;

class LiteDownloadManager implements LiteDownloadManagerCommands {

    private final DownloadServiceCommands downloadService;
    private final Handler callbackHandler;

    LiteDownloadManager(DownloadServiceCommands downloadService, Handler callbackHandler) {
        this.downloadService = downloadService;
        this.callbackHandler = callbackHandler;
    }

    @Override
    public void download(final DownloadBatch downloadBatch, final DownloadBatch.Callback callback) {
        downloadService.download(downloadBatch, new DownloadBatch.Callback() {
            @Override
            public void onUpdate(final DownloadBatchStatus downloadBatchStatus) {
                callbackHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.onUpdate(downloadBatchStatus);
                    }
                });
            }
        });
    }
}
