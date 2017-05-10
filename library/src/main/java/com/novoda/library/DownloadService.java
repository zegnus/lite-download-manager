package com.novoda.library;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import com.novoda.notils.logger.simple.Log;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DownloadService extends Service implements DownloadServiceCommands {

    private ExecutorService executor;
    private IBinder binder;

    @Override
    public void onCreate() {
        executor = Executors.newSingleThreadExecutor();
        binder = new DownloadServiceBinder();

        super.onCreate();
    }

    class DownloadServiceBinder extends Binder {
        DownloadServiceCommands getService() {
            return DownloadService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void download(final DownloadBatch downloadBatch, final DownloadBatch.Callback callback) {
        DownloadBatchStatus downloadBatchStatus = downloadBatch.getDownloadBatchStatus();

        startNotification();

        downloadBatchStatus.markAsQueued();
        callback.onUpdate(downloadBatchStatus);

        downloadBatch.setCallback(callback);

        executor.execute(new Runnable() {
            @Override
            public void run() {
                downloadBatch.download();
            }
        });
    }

    private void startNotification() {
        Notification notification = new NotificationCompat.Builder(this)
                .setTicker("ticker")  // the status text
                .setWhen(System.currentTimeMillis())  // the time stamp
                .setContentTitle("content title")  // the label of the entry
                .setContentText("content")  // the contents of the entry
                .setContentIntent(null)  // The intent to send when the entry is clicked
                .build();
        startForeground(1, notification);
    }

    @Override
    public void onDestroy() {
        Log.v("Service onDestroy");
        executor.shutdown();
        super.onDestroy();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Log.v("Service onTaskRemoved");
        super.onTaskRemoved(rootIntent);
    }
}
