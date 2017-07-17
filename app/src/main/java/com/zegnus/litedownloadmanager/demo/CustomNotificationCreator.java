package com.zegnus.litedownloadmanager.demo;

import android.app.Notification;
import android.content.Context;
import android.support.v4.app.NotificationCompat;

import com.novoda.notils.logger.simple.Log;
import com.zegnus.litedownloadmanager.DownloadBatchTitle;
import com.zegnus.litedownloadmanager.NotificationCreator;
import com.zegnus.litedownloadmanager.NotificationInformation;

public class CustomNotificationCreator implements NotificationCreator {
    private static final int ID = 1;
    private static final boolean NOT_INDETERMINATE = false;

    private final Context context;
    private final int iconDrawable;

    public CustomNotificationCreator(Context context, int iconDrawable) {
        this.context = context;
        this.iconDrawable = iconDrawable;
    }

    @Override
    public NotificationInformation createNotification(DownloadBatchTitle downloadBatchTitle,
                                                      int percentageDownloaded,
                                                      int bytesFileSize,
                                                      int bytesDownloaded) {
        String title = downloadBatchTitle.toString();
        String content = percentageDownloaded + "% downloaded";

        Log.v("Create notification for " + title + ", " + content);

        Notification notification = new NotificationCompat.Builder(context)
                .setProgress(bytesFileSize, bytesDownloaded, NOT_INDETERMINATE)
                .setSmallIcon(iconDrawable)
                .setContentTitle(title)
                .setContentText(content)
                .build();
        return new CustomNotificationInformation(ID, notification);
    }

    private static class CustomNotificationInformation implements NotificationInformation {

        private final int id;
        private final Notification notification;

        CustomNotificationInformation(int id, Notification notification) {
            this.id = id;
            this.notification = notification;
        }

        @Override
        public int getId() {
            return id;
        }

        @Override
        public Notification getNotification() {
            return notification;
        }
    }
}
