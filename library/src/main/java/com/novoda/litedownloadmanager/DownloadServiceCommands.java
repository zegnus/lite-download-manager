package com.novoda.litedownloadmanager;

import android.app.Notification;

interface DownloadServiceCommands {

    void download(DownloadBatch downloadBatch, DownloadBatchCallback callback);

    void updateNotification(Notification notification);
}
