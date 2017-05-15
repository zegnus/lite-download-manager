package com.novoda.litedownloadmanager;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import com.novoda.library.LiteDownloadManagerCommands;
import com.novoda.library.LiteDownloadManagerCreator;

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
