package com.novoda.litedownloadmanager;

interface DownloadService {

    void download(DownloadBatch downloadBatch, DownloadBatchCallback callback);

    void updateNotification(NotificationInformation notification);

    void makeNotificationDismissible(NotificationInformation notificationInformation);
}
