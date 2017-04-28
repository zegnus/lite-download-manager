package com.novoda.litedownloadmanager;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.novoda.library.DownloadBatch;
import com.novoda.library.DownloadBatchStatus;
import com.novoda.library.DownloadFile;
import com.novoda.library.DownloadFileId;
import com.novoda.library.LiteDownloadManagerCommands;
import com.novoda.library.LiteDownloadManagerCreator;
import com.novoda.notils.logger.simple.Log;

public class MainActivity extends AppCompatActivity {

    private LiteDownloadManagerCreator liteDownloadManagerCreator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.setShowLogs(true);

        liteDownloadManagerCreator = new LiteDownloadManagerCreator(getApplicationContext());
        liteDownloadManagerCreator.create(new LiteDownloadManagerCreator.Callback() {
            @Override
            public void onSuccess(LiteDownloadManagerCommands liteDownloadManagerCommands) {
                DownloadFile[] downloadFiles = new DownloadFile[2];
                downloadFiles[0] = new DownloadFile(DownloadFileId.from("one"), "http://ipv4.download.thinkbroadband.com/100MB.zip");
                downloadFiles[1] = new DownloadFile(DownloadFileId.from("two"), "http://ipv4.download.thinkbroadband.com/100MB.zip");
                DownloadBatch downloadBatch = new DownloadBatch(downloadFiles);
                liteDownloadManagerCommands.download(downloadBatch, callback);
            }

            @Override
            public void onError() {
                // no-op
            }
        });
    }

    private final DownloadBatch.Callback callback = new DownloadBatch.Callback() {
        @Override
        public void onUpdate(DownloadBatchStatus downloadBatchStatus) {
            Log.v("Batch downloaded: " + downloadBatchStatus.percentatgeDownloaded() + ", bytes: " + downloadBatchStatus.bytesDownloaded());
        }
    };

    @Override
    protected void onDestroy() {
        liteDownloadManagerCreator.destroy();
        super.onDestroy();
    }
}
