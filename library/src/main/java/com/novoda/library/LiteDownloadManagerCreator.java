package com.novoda.library;

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

    private DownloadServiceCommands downloadService;
    private LiteDownloadManager liteDownloadManager;

    private final FileSizeRequester fileSizeRequester;
    private final Persistence persistence;
    private final Downloader downloader;

    public static LiteDownloadManagerCreator newInstance(Context context) {
        Persistence persistence = PersistenceCreator.createInternalPhysicalPersistence(context);

        OkHttpClient httpClient = new OkHttpClient();
        httpClient.setConnectTimeout(5, TimeUnit.SECONDS);
        httpClient.setWriteTimeout(5, TimeUnit.SECONDS);
        httpClient.setReadTimeout(5, TimeUnit.SECONDS);

        FileSizeRequester fileSizeRequester = new NetworkFileSizeRequester(httpClient);
        Downloader downloader = new NetworkDownloader(httpClient);

        return new LiteDownloadManagerCreator(context, fileSizeRequester, persistence, downloader);
    }

    private LiteDownloadManagerCreator(Context context, FileSizeRequester fileSizeRequester, Persistence persistence, Downloader downloader) {
        this.context = context;
        this.fileSizeRequester = fileSizeRequester;
        this.persistence = persistence;
        this.downloader = downloader;
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
                new ArrayList<DownloadBatchCallback>(),
                fileSizeRequester,
                persistence,
                downloader
        );

        return liteDownloadManager;
    }
}
