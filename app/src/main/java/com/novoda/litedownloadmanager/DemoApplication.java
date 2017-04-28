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
        LiteDownloadManagerCreator liteDownloadManagerCreator = new LiteDownloadManagerCreator(getApplicationContext());
        liteDownloadManagerCreator.create(new LiteDownloadManagerCreator.Callback() {
            @Override
            public void onSuccess(LiteDownloadManagerCommands liteDownloadManagerCommands) {
                DemoApplication.this.liteDownloadManagerCommands = liteDownloadManagerCommands;
            }

            @Override
            public void onError() {
                // no-op
            }
        }, new Handler(Looper.getMainLooper()));
    }

    public LiteDownloadManagerCommands getLiteDownloadManagerCommands() {
        return liteDownloadManagerCommands;
    }
}
