package com.novoda.library;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;

import java.util.ArrayList;
import java.util.HashMap;

public class LiteDownloadManagerCreator {

    private final Context applicationContext;

    private DownloadServiceCommands downloadService;
    private LiteDownloadManager liteDownloadManager;

    public LiteDownloadManagerCreator(Context applicationContext) {
        this.applicationContext = applicationContext;
    }

    public LiteDownloadManager create(final Handler callbackHandler) {
        Intent intent = new Intent(applicationContext, DownloadService.class);
        ServiceConnection serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                DownloadService.DownloadServiceBinder binder = (DownloadService.DownloadServiceBinder) service;
                downloadService = binder.getService();
                liteDownloadManager.setDownloadService(downloadService);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                // no-op
            }
        };

        applicationContext.bindService(intent, serviceConnection, Service.BIND_AUTO_CREATE);

        liteDownloadManager = new LiteDownloadManager(
                callbackHandler,
                new HashMap<DownloadBatchId, DownloadBatch>(),
                new ArrayList<DownloadBatch.Callback>()
        );

        return liteDownloadManager;
    }
}
