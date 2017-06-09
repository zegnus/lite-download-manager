package com.novoda.litedownloadmanager;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DownloadService extends Service implements DownloadServiceCommands {

    private static final String WAKELOCK_TAG = "WakelockTag";
    private static final boolean DO_NOT_REMOVE_NOTIFICATION = false;

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
        updateStatusToQueuedIfNeeded(downloadBatch, callback);
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

    @Override
    public void updateNotification(NotificationInformation notificationInformation) {
        startForeground(notificationInformation.getId(), notificationInformation.getNotification());
    }

    @Override
    public void makeNotificationDismissible(NotificationInformation notificationInformation) {
        stopForeground(DO_NOT_REMOVE_NOTIFICATION);
    }

    private void updateStatusToQueuedIfNeeded(DownloadBatch downloadBatch, DownloadBatchCallback callback) {
        DownloadBatchStatus downloadBatchStatus = downloadBatch.getDownloadBatchStatus();

        if (!downloadBatchStatus.isMarkedAsPaused()) {
            downloadBatchStatus.markAsQueued();
        }

        callback.onUpdate(downloadBatchStatus);
    }

    private void acquireCpuWakeLock() {
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WAKELOCK_TAG);
        wakeLock.acquire();
    }

    private void releaseCpuWakeLock() {
        wakeLock.release();
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
