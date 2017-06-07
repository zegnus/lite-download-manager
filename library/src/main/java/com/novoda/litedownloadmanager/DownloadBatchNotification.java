package com.novoda.litedownloadmanager;

import android.app.Notification;
import android.content.Context;
import android.support.annotation.DrawableRes;
import android.support.v4.app.NotificationCompat;

class DownloadBatchNotification {

    private static final boolean NOT_INDETERMINATE = false;
    private final Context context;
    private final int iconDrawable;

    DownloadBatchNotification(Context context, @DrawableRes int iconDrawable) {
        this.context = context.getApplicationContext();
        this.iconDrawable = iconDrawable;
    }

    Notification createNotification(DownloadBatchTitle downloadBatchTitle,
                                    int percentageDownloaded,
                                    int bytesFileSize,
                                    int bytesDownloaded) {
        String title = downloadBatchTitle.toString();
        String content = percentageDownloaded + "% downloaded";

        return new NotificationCompat.Builder(context)
                .setProgress(bytesFileSize, bytesDownloaded, NOT_INDETERMINATE)
                .setSmallIcon(iconDrawable)
                .setContentTitle(title)
                .setContentText(content)
                .build();
    }
}
