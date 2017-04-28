package com.novoda.library;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

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
        executor.execute(new Runnable() {
            @Override
            public void run() {
                downloadBatch.download(callback);
            }
        });
    }

    @Override
    public void onDestroy() {
        executor.shutdown();
        super.onDestroy();
    }
}
