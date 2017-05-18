package com.novoda.litedownloadmanager.demo;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import com.novoda.litedownloadmanager.LiteDownloadManagerCommands;
import com.novoda.litedownloadmanager.LiteDownloadManagerCreator;

public class DemoApplication extends Application {

    private volatile LiteDownloadManagerCommands liteDownloadManagerCommands;

    @Override
    public void onCreate() {
        super.onCreate();

        createLiteDownloadManager();
    }

    private void createLiteDownloadManager() {
        LiteDownloadManagerCreator liteDownloadManagerCreator = LiteDownloadManagerCreator.newInstance(getApplicationContext());
        liteDownloadManagerCommands = liteDownloadManagerCreator.create(new Handler(Looper.getMainLooper()));
    }

    public LiteDownloadManagerCommands getLiteDownloadManagerCommands() {
        return liteDownloadManagerCommands;
    }
}
