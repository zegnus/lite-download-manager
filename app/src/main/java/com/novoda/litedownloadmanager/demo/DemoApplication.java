package com.novoda.litedownloadmanager.demo;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import com.facebook.stetho.Stetho;
import com.novoda.litedownloadmanager.LiteDownloadManagerCommands;
import com.novoda.litedownloadmanager.LiteDownloadManagerBuilder;

public class DemoApplication extends Application {

    private volatile LiteDownloadManagerCommands liteDownloadManagerCommands;

    @Override
    public void onCreate() {
        super.onCreate();
        Stetho.initializeWithDefaults(this);
        createLiteDownloadManager();
    }

    private void createLiteDownloadManager() {
        Handler handler = new Handler(Looper.getMainLooper());
        liteDownloadManagerCommands = LiteDownloadManagerBuilder
                .newInstance(handler, getApplicationContext())
                .withNetworkDownloader()
                .withCustomFilePersistence(CustomFilePersistence.class)
                .build();
    }

    public LiteDownloadManagerCommands getLiteDownloadManagerCommands() {
        return liteDownloadManagerCommands;
    }
}
