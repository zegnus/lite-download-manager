package com.novoda.litedownloadmanager;

interface DownloadServiceCommands {

    void download(DownloadBatch downloadBatch, DownloadBatchCallback callback);

    void updateNotification(NotificationInformation notification);

    void makeNotificationDismissible(NotificationInformation notificationInformation);
}
