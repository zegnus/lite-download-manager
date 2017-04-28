package com.novoda.litedownloadmanager;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.novoda.library.DownloadBatch;
import com.novoda.library.DownloadBatchId;
import com.novoda.library.DownloadBatchStatus;
import com.novoda.library.DownloadFile;
import com.novoda.library.DownloadFileId;
import com.novoda.library.LiteDownloadManagerCommands;
import com.novoda.library.LiteDownloadManagerCreator;
import com.novoda.notils.logger.simple.Log;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private LiteDownloadManagerCreator liteDownloadManagerCreator;

    private TextView textViewBatch1;
    private TextView textViewBatch2;
    private View buttonPauseDownload1;
    private View buttonPauseDownload2;
    private View buttonResumeDownload1;
    private View buttonResumeDownload2;

    private LiteDownloadManagerCommands liteDownloadManagerCommands;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.setShowLogs(true);

        textViewBatch1 = (TextView) findViewById(R.id.batch_1);
        textViewBatch2 = (TextView) findViewById(R.id.batch_2);

        findViewById(R.id.button_start_downloading).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setVisibility(View.INVISIBLE);

                DownloadFile[] downloadFiles = new DownloadFile[2];
                downloadFiles[0] = new DownloadFile(DownloadFileId.from("one"), "http://ipv4.download.thinkbroadband.com/100MB.zip");
                downloadFiles[1] = new DownloadFile(DownloadFileId.from("two"), "http://ipv4.download.thinkbroadband.com/100MB.zip");
                DownloadBatch downloadBatch = new DownloadBatch(DownloadBatchId.from("made-in-chelsea"), downloadFiles, new HashMap<DownloadFileId, Long>());
                liteDownloadManagerCommands.download(downloadBatch);

                downloadFiles = new DownloadFile[2];
                downloadFiles[0] = new DownloadFile(DownloadFileId.from("one"), "http://ipv4.download.thinkbroadband.com/100MB.zip");
                downloadFiles[1] = new DownloadFile(DownloadFileId.from("two"), "http://ipv4.download.thinkbroadband.com/100MB.zip");
                downloadBatch = new DownloadBatch(DownloadBatchId.from("hollyoaks"), downloadFiles, new HashMap<DownloadFileId, Long>());
                liteDownloadManagerCommands.download(downloadBatch);
            }
        });

        buttonPauseDownload1 = findViewById(R.id.button_pause_downloading_1);
        buttonPauseDownload1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                liteDownloadManagerCommands.pause(DownloadBatchId.from("made-in-chelsea"));
                buttonPauseDownload1.setVisibility(View.GONE);
                buttonResumeDownload1.setVisibility(View.VISIBLE);
            }
        });

        buttonPauseDownload2 = findViewById(R.id.button_pause_downloading_2);
        buttonPauseDownload2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                liteDownloadManagerCommands.pause(DownloadBatchId.from("hollyoaks"));
                buttonPauseDownload2.setVisibility(View.GONE);
                buttonResumeDownload2.setVisibility(View.VISIBLE);
            }
        });

        buttonResumeDownload1 = findViewById(R.id.button_resume_downloading_1);
        buttonResumeDownload1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                liteDownloadManagerCommands.resume(DownloadBatchId.from("made-in-chelsea"));
                buttonPauseDownload1.setVisibility(View.VISIBLE);
                buttonResumeDownload1.setVisibility(View.GONE);
            }
        });

        buttonResumeDownload2 = findViewById(R.id.button_resume_downloading_2);
        buttonResumeDownload2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                liteDownloadManagerCommands.resume(DownloadBatchId.from("hollyoaks"));
                buttonPauseDownload2.setVisibility(View.VISIBLE);
                buttonResumeDownload2.setVisibility(View.GONE);
            }
        });

        liteDownloadManagerCreator = new LiteDownloadManagerCreator(getApplicationContext());
        liteDownloadManagerCreator.create(new LiteDownloadManagerCreator.Callback() {
            @Override
            public void onSuccess(LiteDownloadManagerCommands liteDownloadManagerCommands) {
                MainActivity.this.liteDownloadManagerCommands = liteDownloadManagerCommands;
                MainActivity.this.liteDownloadManagerCommands.addDownloadBatchCallback(callback);
            }

            @Override
            public void onError() {
                // no-op
            }
        }, new Handler(Looper.getMainLooper()));
    }

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
