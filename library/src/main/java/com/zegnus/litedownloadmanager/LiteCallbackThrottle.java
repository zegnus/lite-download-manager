package com.zegnus.litedownloadmanager;

class LiteCallbackThrottle implements CallbackThrottle {

    private DownloadBatchCallback callback;

    @Override
    public void setCallback(DownloadBatchCallback callback) {
        this.callback = callback;
    }

    @Override
    public void update(DownloadBatchStatus downloadBatchStatus) {
        if (callback == null) {
            return;
        }
        callback.onUpdate(downloadBatchStatus);
    }
}
