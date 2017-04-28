package com.novoda.library;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

public class LiteDownloadManagerCreator {

    private final Context applicationContext;

    private DownloadServiceCommands downloadService;
    private ServiceConnection serviceConnection;
    private boolean serviceIsBound;

    public LiteDownloadManagerCreator(Context applicationContext) {
        this.applicationContext = applicationContext;
    }

    public void create(final Callback callback) {
        Intent intent = new Intent(applicationContext, DownloadService.class);
        serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                DownloadService.DownloadServiceBinder binder = (DownloadService.DownloadServiceBinder) service;
                downloadService = binder.getService();
                serviceIsBound = true;

                LiteDownloadManager liteDownloadManager = new LiteDownloadManager(downloadService);
                callback.onSuccess(liteDownloadManager);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                serviceIsBound = false;
                callback.onError();
            }
        };
        applicationContext.bindService(intent, serviceConnection, Service.BIND_AUTO_CREATE);
    }

    public void destroy() {
        if (serviceIsBound) {
            applicationContext.unbindService(serviceConnection);
        }
    }

    public interface Callback {

        void onSuccess(LiteDownloadManagerCommands liteDownloadManagerCommands);

        void onError();
    }
}
