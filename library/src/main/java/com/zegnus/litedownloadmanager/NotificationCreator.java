package com.zegnus.litedownloadmanager;

public interface NotificationCreator {

    NotificationInformation createNotification(DownloadBatchTitle downloadBatchTitle,
                                               int percentageDownloaded,
                                               int bytesFileSize,
                                               int bytesDownloaded);
}
