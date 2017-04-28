package com.novoda.library;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class DownloadService extends Service implements DownloadServiceCommands {

    private final Executor executor = Executors.newSingleThreadExecutor();
    private final IBinder binder = new DownloadServiceBinder();

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
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void download(final DownloadBatch downloadBatch, final DownloadBatch.Callback callback) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                downloadBatch.download(callback);
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
