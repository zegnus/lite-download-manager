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

public final class LiteDownloadManagerBuilder {

    private final Context context;
    private final DownloadsBatchPersistence downloadsBatchPersistence;
    private final DownloadsFilePersistence downloadsFilePersistence;
    private final Handler callbackHandler;

    private FilePersistenceCreator filePersistenceCreator;
    private FileSizeRequester fileSizeRequester;
    private Downloader downloader;
    private DownloadServiceCommands downloadService;
    private LiteDownloadManager liteDownloadManager;

    public static LiteDownloadManagerBuilder newInstance(Handler callbackHandler, Context context) {
        // File persistence
        FilePersistenceCreator filePersistenceCreator = FilePersistenceCreator.newInternalFilePersistenceCreator(context);

        // Downloads information persistence
        RoomDownloadsPersistence downloadsPersistence = RoomDownloadsPersistence.newInstance(context);

        Executor executor = Executors.newSingleThreadExecutor();
        DownloadsFilePersistence downloadsFilePersistence = new DownloadsFilePersistence(downloadsPersistence);
        DownloadsBatchPersistence downloadsBatchPersistence = new DownloadsBatchPersistence(executor, downloadsFilePersistence, downloadsPersistence);

        // Network downloader
        OkHttpClient httpClient = new OkHttpClient();
        httpClient.setConnectTimeout(5, TimeUnit.SECONDS);
        httpClient.setWriteTimeout(5, TimeUnit.SECONDS);
        httpClient.setReadTimeout(5, TimeUnit.SECONDS);

        FileSizeRequester fileSizeRequester = new NetworkFileSizeRequester(httpClient);
        Downloader downloader = new NetworkDownloader(httpClient);

        return new LiteDownloadManagerBuilder(
                context,
                callbackHandler,
                filePersistenceCreator,
                downloadsBatchPersistence,
                downloadsFilePersistence,
                fileSizeRequester,
                downloader
        );
    }

    public LiteDownloadManagerBuilder withNetworkDownloader() {
        OkHttpClient httpClient = new OkHttpClient();
        httpClient.setConnectTimeout(5, TimeUnit.SECONDS);
        httpClient.setWriteTimeout(5, TimeUnit.SECONDS);
        httpClient.setReadTimeout(5, TimeUnit.SECONDS);

        fileSizeRequester = new NetworkFileSizeRequester(httpClient);
        downloader = new NetworkDownloader(httpClient);
        return this;
    }

    public LiteDownloadManagerBuilder withInternalFilePersistence() {
        filePersistenceCreator = FilePersistenceCreator.newInternalFilePersistenceCreator(context);
        return this;
    }

    public LiteDownloadManagerBuilder withExternalFilePersistence() {
        filePersistenceCreator = FilePersistenceCreator.newExternalFilePersistenceCreator(context);
        return this;
    }

    public LiteDownloadManagerBuilder withCustomFilePersistence(Class<? extends FilePersistence> customFilePersistenceClass) {
        filePersistenceCreator = FilePersistenceCreator.newCustomFilePersistenceCreator(context, customFilePersistenceClass);
        return this;
    }

    private LiteDownloadManagerBuilder(Context context,
                                       Handler callbackHandler, FilePersistenceCreator filePersistenceCreator,
                                       DownloadsBatchPersistence downloadsBatchPersistence,
                                       DownloadsFilePersistence downloadsFilePersistence,
                                       FileSizeRequester fileSizeRequester,
                                       Downloader downloader) {
        this.context = context;
        this.callbackHandler = callbackHandler;
        this.filePersistenceCreator = filePersistenceCreator;
        this.downloadsBatchPersistence = downloadsBatchPersistence;
        this.downloadsFilePersistence = downloadsFilePersistence;
        this.fileSizeRequester = fileSizeRequester;
        this.downloader = downloader;
    }

    public LiteDownloadManager build() {
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
                filePersistenceCreator,
                downloader,
                downloadsBatchPersistence,
                downloadsFilePersistence
        );

        return liteDownloadManager;
    }
}
