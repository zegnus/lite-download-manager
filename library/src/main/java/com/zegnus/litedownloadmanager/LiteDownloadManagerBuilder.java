package com.zegnus.litedownloadmanager;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.DrawableRes;

import com.squareup.okhttp.OkHttpClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public final class LiteDownloadManagerBuilder {

    private static final Object LOCK = new Object();
    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();

    private final Context context;
    private final Handler callbackHandler;

    private DownloadsBatchPersistence downloadsBatchPersistence;
    private DownloadsFilePersistence downloadsFilePersistence;
    private FilePersistenceCreator filePersistenceCreator;
    private FileSizeRequester fileSizeRequester;
    private FileDownloader fileDownloader;
    private DownloadService downloadService;
    private LiteDownloadManager liteDownloadManager;
    private NotificationCreator notificationCreator;

    public static LiteDownloadManagerBuilder newInstance(Context context, Handler callbackHandler, @DrawableRes int notificationIcon) {
        // File persistence
        FilePersistenceCreator filePersistenceCreator = FilePersistenceCreator.newInternalFilePersistenceCreator(context);

        // Downloads information persistence
        DownloadsPersistence downloadsPersistence = RoomDownloadsPersistence.newInstance(context);

        Executor executor = Executors.newSingleThreadExecutor();
        DownloadsFilePersistence downloadsFilePersistence = new DownloadsFilePersistence(downloadsPersistence);
        DownloadsBatchPersistence downloadsBatchPersistence = new DownloadsBatchPersistence(executor, downloadsFilePersistence, downloadsPersistence);

        // Network downloader
        OkHttpClient httpClient = new OkHttpClient();
        httpClient.setConnectTimeout(5, TimeUnit.SECONDS);
        httpClient.setWriteTimeout(5, TimeUnit.SECONDS);
        httpClient.setReadTimeout(5, TimeUnit.SECONDS);

        FileSizeRequester fileSizeRequester = new NetworkFileSizeRequester(httpClient);
        FileDownloader fileDownloader = new NetworkFileDownloader(httpClient);

        DownloadBatchNotification downloadBatchNotification = new DownloadBatchNotification(context, notificationIcon);

        return new LiteDownloadManagerBuilder(
                context,
                callbackHandler,
                filePersistenceCreator,
                downloadsBatchPersistence,
                downloadsFilePersistence,
                fileSizeRequester,
                fileDownloader,
                downloadBatchNotification
        );
    }

    public LiteDownloadManagerBuilder withNetworkDownloader() {
        OkHttpClient httpClient = new OkHttpClient();
        httpClient.setConnectTimeout(5, TimeUnit.SECONDS);
        httpClient.setWriteTimeout(5, TimeUnit.SECONDS);
        httpClient.setReadTimeout(5, TimeUnit.SECONDS);

        fileSizeRequester = new NetworkFileSizeRequester(httpClient);
        fileDownloader = new NetworkFileDownloader(httpClient);
        return this;
    }

    public LiteDownloadManagerBuilder withFilePersistenceInternal() {
        filePersistenceCreator = FilePersistenceCreator.newInternalFilePersistenceCreator(context);
        return this;
    }

    public LiteDownloadManagerBuilder withFilePersistenceExternal() {
        filePersistenceCreator = FilePersistenceCreator.newExternalFilePersistenceCreator(context);
        return this;
    }

    public LiteDownloadManagerBuilder withFileDownloaderCustom(FileSizeRequester fileSizeRequester, FileDownloader fileDownloader) {
        this.fileSizeRequester = fileSizeRequester;
        this.fileDownloader = fileDownloader;
        return this;
    }

    public LiteDownloadManagerBuilder withFilePersistenceCustom(Class<? extends FilePersistence> customFilePersistenceClass) {
        filePersistenceCreator = FilePersistenceCreator.newCustomFilePersistenceCreator(context, customFilePersistenceClass);
        return this;
    }

    public LiteDownloadManagerBuilder withDownloadsPersistenceCustom(DownloadsPersistence downloadsPersistence) {
        Executor executor = Executors.newSingleThreadExecutor();
        this.downloadsFilePersistence = new DownloadsFilePersistence(downloadsPersistence);
        this.downloadsBatchPersistence = new DownloadsBatchPersistence(executor, downloadsFilePersistence, downloadsPersistence);
        return this;
    }

    public LiteDownloadManagerBuilder withNotification(NotificationCreator notificationCreator) {
        this.notificationCreator = notificationCreator;
        return this;
    }

    private LiteDownloadManagerBuilder(Context context,
                                       Handler callbackHandler,
                                       FilePersistenceCreator filePersistenceCreator,
                                       DownloadsBatchPersistence downloadsBatchPersistence,
                                       DownloadsFilePersistence downloadsFilePersistence,
                                       FileSizeRequester fileSizeRequester,
                                       FileDownloader fileDownloader,
                                       NotificationCreator notificationCreator) {
        this.context = context;
        this.callbackHandler = callbackHandler;
        this.filePersistenceCreator = filePersistenceCreator;
        this.downloadsBatchPersistence = downloadsBatchPersistence;
        this.downloadsFilePersistence = downloadsFilePersistence;
        this.fileSizeRequester = fileSizeRequester;
        this.fileDownloader = fileDownloader;
        this.notificationCreator = notificationCreator;
    }

    public LiteDownloadManager build() {
        Intent intent = new Intent(context, LiteDownloadService.class);
        ServiceConnection serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                LiteDownloadService.DownloadServiceBinder binder = (LiteDownloadService.DownloadServiceBinder) service;
                downloadService = binder.getService();
                liteDownloadManager.submitAllStoredDownloads(new AllStoredDownloadsSubmittedCallback() {
                    @Override
                    public void onAllDownloadsSubmitted() {
                        liteDownloadManager.initialise(downloadService);
                    }
                });
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                // no-op
            }
        };

        context.bindService(intent, serviceConnection, Service.BIND_AUTO_CREATE);

        FileOperations fileOperations = new FileOperations(filePersistenceCreator, fileSizeRequester, fileDownloader);
        ArrayList<DownloadBatchCallback> callbacks = new ArrayList<>();

        LiteDownloadManagerDownloader downloader = new LiteDownloadManagerDownloader(
                LOCK,
                EXECUTOR,
                callbackHandler,
                fileOperations,
                downloadsBatchPersistence,
                downloadsFilePersistence,
                notificationCreator,
                callbacks
        );

        liteDownloadManager = new LiteDownloadManager(
                LOCK,
                EXECUTOR,
                new HashMap<DownloadBatchId, DownloadBatch>(),
                callbacks,
                fileOperations,
                downloadsBatchPersistence,
                downloader
        );

        return liteDownloadManager;
    }
}
