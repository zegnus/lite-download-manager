package com.zegnus.litedownloadmanager.demo;

import com.novoda.notils.logger.simple.Log;
import com.zegnus.litedownloadmanager.CallbackThrottle;
import com.zegnus.litedownloadmanager.DownloadBatchCallback;
import com.zegnus.litedownloadmanager.DownloadBatchStatus;

class CustomCallbackThrottle implements CallbackThrottle {

    @Override
    public void setCallback(DownloadBatchCallback callback) {
        Log.v("setCallback");
    }

    @Override
    public void update(DownloadBatchStatus downloadBatchStatus) {
        Log.v("update " + downloadBatchStatus.getDownloadBatchTitle().asString());
    }

    @Override
    public void stopUpdates() {
        Log.v("stopUpdates");
    }
}
