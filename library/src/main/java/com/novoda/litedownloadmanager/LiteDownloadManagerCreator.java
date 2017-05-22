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
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public final class LiteDownloadManagerCreator {

    private final Context context;
    private final FileSizeRequester fileSizeRequester;
    private final PersistenceCreator persistenceCreator;
    private final Downloader downloader;
    private final DownloadsBatchPersistence downloadsBatchPersistence;
    private final DownloadsFilePersistence downloadsFilePersistence;

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
        RoomDownloadsPersistence downloadsPersistence = RoomDownloadsPersistence.newInstance(context);

        Executor executor = Executors.newSingleThreadExecutor();
        DownloadsFilePersistence downloadsFilePersistence = new DownloadsFilePersistence(downloadsPersistence);
        DownloadsBatchPersistence downloadsBatchPersistence = new DownloadsBatchPersistence(executor, downloadsFilePersistence, downloadsPersistence);

        return new LiteDownloadManagerCreator(
                context,
                fileSizeRequester,
                persistenceCreator,
                downloader,
                downloadsBatchPersistence,
                downloadsFilePersistence
        );
    }

    private LiteDownloadManagerCreator(Context context,
                                       FileSizeRequester fileSizeRequester,
                                       PersistenceCreator persistenceCreator,
                                       Downloader downloader,
                                       DownloadsBatchPersistence downloadsBatchPersistence, DownloadsFilePersistence downloadsFilePersistence) {
        this.context = context;
        this.fileSizeRequester = fileSizeRequester;
        this.persistenceCreator = persistenceCreator;
        this.downloader = downloader;
        this.downloadsBatchPersistence = downloadsBatchPersistence;
        this.downloadsFilePersistence = downloadsFilePersistence;
    }

    public LiteDownloadManager create(final Handler callbackHandler) {
        Intent intent = new Intent(context, DownloadService.class);
        ServiceConnection serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                DownloadService.DownloadServiceBinder binder = (DownloadService.DownloadServiceBinder) service;
                downloadService = binder.getService();
                liteDownloadManager.initialise(downloadService);
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
                downloadsBatchPersistence,
                downloadsFilePersistence
        );

        return liteDownloadManager;
    }
}
