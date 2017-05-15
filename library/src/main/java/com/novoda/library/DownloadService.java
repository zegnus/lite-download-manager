package com.novoda.library;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DownloadService extends Service implements DownloadServiceCommands {

    private static final String WAKELOCK_TAG = "WakelockTag";

    private ExecutorService executor;
    private IBinder binder;
    private PowerManager.WakeLock wakeLock;

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
    public void download(final DownloadBatch downloadBatch, final DownloadBatchCallback callback) {
        DownloadBatchStatus downloadBatchStatus = downloadBatch.getDownloadBatchStatus();

        startNotification();

        downloadBatchStatus.markAsQueued();
        callback.onUpdate(downloadBatchStatus);

        downloadBatch.setCallback(callback);

        executor.execute(new Runnable() {
            @Override
            public void run() {
                acquireCpuWakeLock();
                downloadBatch.download();
                releaseCpuWakeLock();
            }
        });
    }

    private void acquireCpuWakeLock() {
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WAKELOCK_TAG);
        wakeLock.acquire();
    }

    private void releaseCpuWakeLock() {
        wakeLock.release();
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
        executor.shutdown();
        super.onDestroy();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
    }
}
