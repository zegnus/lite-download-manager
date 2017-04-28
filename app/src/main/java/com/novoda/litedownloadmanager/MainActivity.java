package com.novoda.litedownloadmanager;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.novoda.library.DownloadBatch;
import com.novoda.library.DownloadBatchId;
import com.novoda.library.DownloadBatchStatus;
import com.novoda.library.DownloadFile;
import com.novoda.library.DownloadFileId;
import com.novoda.library.LiteDownloadManagerCommands;
import com.novoda.library.LiteDownloadManagerCreator;
import com.novoda.notils.logger.simple.Log;

public class MainActivity extends AppCompatActivity {

    private LiteDownloadManagerCreator liteDownloadManagerCreator;

    private TextView textViewBatch1;
    private TextView textViewBatch2;

    private LiteDownloadManagerCommands liteDownloadManagerCommands;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textViewBatch1 = (TextView) findViewById(R.id.batch_1);
        textViewBatch2 = (TextView) findViewById(R.id.batch_2);

        Button buttonStartDownloading = (Button) findViewById(R.id.button_start_downloading);
        buttonStartDownloading.setOnClickListener(buttonStartDownloadingClick);

        Log.setShowLogs(true);

        liteDownloadManagerCreator = new LiteDownloadManagerCreator(getApplicationContext());
        Handler callbackHandler = new Handler(Looper.getMainLooper());

        liteDownloadManagerCreator.create(new LiteDownloadManagerCreator.Callback() {
            @Override
            public void onSuccess(LiteDownloadManagerCommands liteDownloadManagerCommands) {
                MainActivity.this.liteDownloadManagerCommands = liteDownloadManagerCommands;
            }

            @Override
            public void onError() {
                // no-op
            }
        }, callbackHandler);
    }

    private View.OnClickListener buttonStartDownloadingClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            v.setVisibility(View.INVISIBLE);

            DownloadFile[] downloadFiles = new DownloadFile[2];
            downloadFiles[0] = new DownloadFile(DownloadFileId.from("one"), "http://ipv4.download.thinkbroadband.com/100MB.zip");
            downloadFiles[1] = new DownloadFile(DownloadFileId.from("two"), "http://ipv4.download.thinkbroadband.com/100MB.zip");
            DownloadBatch downloadBatch = new DownloadBatch(DownloadBatchId.from("made-in-chelsea"), downloadFiles);
            liteDownloadManagerCommands.download(downloadBatch, callback);

            downloadFiles = new DownloadFile[2];
            downloadFiles[0] = new DownloadFile(DownloadFileId.from("one"), "http://ipv4.download.thinkbroadband.com/100MB.zip");
            downloadFiles[1] = new DownloadFile(DownloadFileId.from("two"), "http://ipv4.download.thinkbroadband.com/100MB.zip");
            downloadBatch = new DownloadBatch(DownloadBatchId.from("hollyoaks"), downloadFiles);
            liteDownloadManagerCommands.download(downloadBatch, callback);
        }
    };

    private final DownloadBatch.Callback callback = new DownloadBatch.Callback() {
        @Override
        public void onUpdate(DownloadBatchStatus downloadBatchStatus) {
            String message = "Batch " + downloadBatchStatus.getDownloadBatchId().getId()
                    + "\ndownloaded: " + downloadBatchStatus.percentageDownloaded()
                    + "\nbytes: " + downloadBatchStatus.bytesDownloaded()
                    + "\n";

            switch (downloadBatchStatus.getDownloadBatchId().getId()) {
                case "made-in-chelsea":
                    textViewBatch1.setText(message);
                    break;
                case "hollyoaks":
                    textViewBatch2.setText(message);
                    break;
            }
        }
    };

    @Override
    protected void onDestroy() {
        liteDownloadManagerCreator.destroy();
        super.onDestroy();
    }
}
