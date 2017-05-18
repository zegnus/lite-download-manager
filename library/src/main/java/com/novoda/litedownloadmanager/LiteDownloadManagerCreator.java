package com.novoda.litedownloadmanager;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;

import com.squareup.okhttp.OkHttpClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public final class LiteDownloadManagerCreator {

    private final Context context;
    private final FileSizeRequester fileSizeRequester;
    private final PersistenceCreator persistenceCreator;
    private final Downloader downloader;
    private final DownloadsPersistence downloadsPersistence;

    private DownloadServiceCommands downloadService;
    private LiteDownloadManager liteDownloadManager;

    public static LiteDownloadManagerCreator newInstance(Context context) {
        PersistenceCreator persistenceCreator = new PersistenceCreator(context, PersistenceCreator.Type.INTERNAL);

        OkHttpClient httpClient = new OkHttpClient();
        httpClient.setConnectTimeout(5, TimeUnit.SECONDS);
        httpClient.setWriteTimeout(5, TimeUnit.SECONDS);
        httpClient.setReadTimeout(5, TimeUnit.SECONDS);

        FileSizeRequester fileSizeRequester = new NetworkFileSizeRequester(httpClient);
        Downloader downloader = new NetworkDownloader(httpClient);
        DownloadsPersistence downloadsPersistence = new DatabaseDownloadPersistence();

        return new LiteDownloadManagerCreator(context, fileSizeRequester, persistenceCreator, downloader, downloadsPersistence);
    }

    private LiteDownloadManagerCreator(Context context,
                                       FileSizeRequester fileSizeRequester,
                                       PersistenceCreator persistenceCreator,
                                       Downloader downloader,
                                       DownloadsPersistence downloadsPersistence) {
        this.context = context;
        this.fileSizeRequester = fileSizeRequester;
        this.persistenceCreator = persistenceCreator;
        this.downloader = downloader;
        this.downloadsPersistence = downloadsPersistence;
    }

    public LiteDownloadManager create(final Handler callbackHandler) {
        Intent intent = new Intent(context, DownloadService.class);
        ServiceConnection serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                DownloadService.DownloadServiceBinder binder = (DownloadService.DownloadServiceBinder) service;
                downloadService = binder.getService();
                liteDownloadManager.loadFromPersistence();
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
                new ArrayList<DownloadBatchCallback>(),
                fileSizeRequester,
                persistenceCreator,
                downloader,
                downloadsPersistence
        );

        return liteDownloadManager;
    }
}
