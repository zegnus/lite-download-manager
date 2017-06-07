package com.novoda.litedownloadmanager.demo;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import com.facebook.stetho.Stetho;
import com.novoda.litedownloadmanager.FileDownloader;
import com.novoda.litedownloadmanager.FileSizeRequester;
import com.novoda.litedownloadmanager.LiteDownloadManagerBuilder;
import com.novoda.litedownloadmanager.LiteDownloadManagerCommands;

public class DemoApplication extends Application {

    private volatile LiteDownloadManagerCommands liteDownloadManagerCommands;

    @Override
    public void onCreate() {
        super.onCreate();
        Stetho.initializeWithDefaults(this);
        createLiteDownloadManager();
    }

    private void createLiteDownloadManager() {
        FileSizeRequester fileSizeRequester = new CustomFileSizeRequester();
        FileDownloader fileDownloader = new CustomFileDownloader();
        //DownloadsPersistence downloadsPersistence = new CustomDownloadsPersistence();

        Handler handler = new Handler(Looper.getMainLooper());
        liteDownloadManagerCommands = LiteDownloadManagerBuilder
                .newInstance(this, handler, R.mipmap.ic_launcher_round)
                .withFileDownloaderCustom(fileSizeRequester, fileDownloader)
                //.withFilePersistenceCustom(CustomFilePersistence.class)
                //.withDownloadsPersistenceCustom(downloadsPersistence)
                .build();
    }

    public LiteDownloadManagerCommands getLiteDownloadManagerCommands() {
        return liteDownloadManagerCommands;
    }
}
