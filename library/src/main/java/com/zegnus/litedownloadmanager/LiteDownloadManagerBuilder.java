package com.zegnus.litedownloadmanager;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.DrawableRes;

import com.novoda.notils.logger.simple.Log;
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

    private FilePersistenceCreator filePersistenceCreator;
    private FileSizeRequester fileSizeRequester;
    private FileDownloader fileDownloader;
    private DownloadService downloadService;
    private LiteDownloadManager liteDownloadManager;
    private NotificationCreator notificationCreator;
    private ConnectionType connectionTypeAllowed;
    private boolean allowNetworkRecovery;
    private CallbackThrottle callbackThrottle;
    private DownloadsPersistence downloadsPersistence;

    public static LiteDownloadManagerBuilder newInstance(Context context, Handler callbackHandler, @DrawableRes int notificationIcon) {
        Log.setShowLogs(true);

        // Callback throttle
        CallbackThrottle callbackThrottle = new LiteCallbackThrottle();

        // File persistence
        FilePersistenceCreator filePersistenceCreator = FilePersistenceCreator.newInternalFilePersistenceCreator(context);

        // Downloads information persistence
        DownloadsPersistence downloadsPersistence = RoomDownloadsPersistence.newInstance(context);

        Executor executor = Executors.newSingleThreadExecutor();
        DownloadsFilePersistence downloadsFilePersistence = new DownloadsFilePersistence(downloadsPersistence);
        DownloadsBatchPersistence downloadsBatchPersistence = new DownloadsBatchPersistence(
                executor,
                downloadsFilePersistence,
                downloadsPersistence,
                callbackThrottle
        );

        // Network downloader
        OkHttpClient httpClient = new OkHttpClient();
        httpClient.setConnectTimeout(5, TimeUnit.SECONDS);
        httpClient.setWriteTimeout(5, TimeUnit.SECONDS);
        httpClient.setReadTimeout(5, TimeUnit.SECONDS);

        FileSizeRequester fileSizeRequester = new NetworkFileSizeRequester(httpClient);
        FileDownloader fileDownloader = new NetworkFileDownloader(httpClient);

        DownloadBatchNotification downloadBatchNotification = new DownloadBatchNotification(context, notificationIcon);

        ConnectionType connectionTypeAllowed = ConnectionType.ALL;
        boolean allowNetworkRecovery = true;

        return new LiteDownloadManagerBuilder(
                context,
                callbackHandler,
                filePersistenceCreator,
                downloadsPersistence,
                fileSizeRequester,
                fileDownloader,
                downloadBatchNotification,
                connectionTypeAllowed,
                allowNetworkRecovery,
                callbackThrottle
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
        this.downloadsPersistence = downloadsPersistence;
        return this;
    }

    public LiteDownloadManagerBuilder withNotification(NotificationCreator notificationCreator) {
        this.notificationCreator = notificationCreator;
        return this;
    }

    public LiteDownloadManagerBuilder withAllowedConnectionType(ConnectionType connectionTypeNotAllowed) {
        this.connectionTypeAllowed = connectionTypeNotAllowed;
        return this;
    }

    public LiteDownloadManagerBuilder withNetworkRecovery(boolean enabled) {
        allowNetworkRecovery = enabled;
        return this;
    }

    public LiteDownloadManagerBuilder withCallbackThrottle(CallbackThrottle callbackThrottle) {
        this.callbackThrottle = callbackThrottle;
        return this;
    }

    private LiteDownloadManagerBuilder(Context context,
                                       Handler callbackHandler,
                                       FilePersistenceCreator filePersistenceCreator,
                                       DownloadsPersistence downloadsPersistence,
                                       FileSizeRequester fileSizeRequester,
                                       FileDownloader fileDownloader,
                                       NotificationCreator notificationCreator,
                                       ConnectionType connectionTypeAllowed,
                                       boolean allowNetworkRecovery,
                                       CallbackThrottle callbackThrottle) {
        this.context = context;
        this.callbackHandler = callbackHandler;
        this.filePersistenceCreator = filePersistenceCreator;
        this.downloadsPersistence = downloadsPersistence;
        this.fileSizeRequester = fileSizeRequester;
        this.fileDownloader = fileDownloader;
        this.notificationCreator = notificationCreator;
        this.connectionTypeAllowed = connectionTypeAllowed;
        this.allowNetworkRecovery = allowNetworkRecovery;
        this.callbackThrottle = callbackThrottle;
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

                        if (allowNetworkRecovery) {
                            DownloadsNetworkRecoveryCreator.createEnabled(context, liteDownloadManager, connectionTypeAllowed);
                        } else {
                            DownloadsNetworkRecoveryCreator.createDisabled();
                        }
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

        Executor executor = Executors.newSingleThreadExecutor();
        DownloadsFilePersistence downloadsFilePersistence = new DownloadsFilePersistence(downloadsPersistence);
        DownloadsBatchPersistence downloadsBatchPersistence = new DownloadsBatchPersistence(
                executor,
                downloadsFilePersistence,
                downloadsPersistence,
                callbackThrottle
        );

        LiteDownloadManagerDownloader downloader = new LiteDownloadManagerDownloader(
                LOCK,
                EXECUTOR,
                callbackHandler,
                fileOperations,
                downloadsBatchPersistence,
                downloadsFilePersistence,
                notificationCreator,
                callbacks,
                callbackThrottle
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
