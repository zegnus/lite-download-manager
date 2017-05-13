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

    private final Context context;

    private DownloadServiceCommands downloadService;
    private LiteDownloadManager liteDownloadManager;

    public LiteDownloadManagerCreator(Context context) {
        this.context = context.getApplicationContext();
    }

    public LiteDownloadManager create(final Handler callbackHandler) {
        Intent intent = new Intent(context, DownloadService.class);
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

        context.bindService(intent, serviceConnection, Service.BIND_AUTO_CREATE);

        liteDownloadManager = new LiteDownloadManager(
                callbackHandler,
                new HashMap<DownloadBatchId, DownloadBatch>(),
                new ArrayList<DownloadBatch.Callback>()
        );

        return liteDownloadManager;
    }
}
