package com.novoda.litedownloadmanager;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.novoda.library.DownloadBatch;
import com.novoda.library.DownloadBatchId;
import com.novoda.library.DownloadBatchStatus;
import com.novoda.library.DownloadFile;
import com.novoda.library.LiteDownloadManagerCommands;
import com.novoda.notils.logger.simple.Log;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final DownloadBatchId DOWNLOAD_BATCH_ID_1 = DownloadBatchId.from("made-in-chelsea");
    private static final DownloadBatchId DOWNLOAD_BATCH_ID_2 = DownloadBatchId.from("hollyoaks");
    private TextView textViewBatch1;
    private TextView textViewBatch2;
    private View buttonPauseDownload1;
    private View buttonPauseDownload2;
    private View buttonResumeDownload1;
    private View buttonResumeDownload2;
    private View buttonDownload;
    private View buttonDeleteAll;

    private LiteDownloadManagerCommands liteDownloadManagerCommands;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.setShowLogs(true);

        textViewBatch1 = (TextView) findViewById(R.id.batch_1);
        textViewBatch2 = (TextView) findViewById(R.id.batch_2);

        buttonDownload = findViewById(R.id.button_start_downloading);
        buttonDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonDownload.setVisibility(View.GONE);
                buttonDeleteAll.setVisibility(View.VISIBLE);

                DownloadFile[] downloadFiles = new DownloadFile[2];
                downloadFiles[0] = DownloadFile.newInstance("one", "http://ipv4.download.thinkbroadband.com/100MB.zip");
                downloadFiles[1] = DownloadFile.newInstance("two", "http://ipv4.download.thinkbroadband.com/100MB.zip");
                DownloadBatch downloadBatch = DownloadBatch.newInstance("made-in-chelsea", downloadFiles);
                liteDownloadManagerCommands.download(downloadBatch);

                downloadFiles = new DownloadFile[2];
                downloadFiles[0] = DownloadFile.newInstance("one", "http://ipv4.download.thinkbroadband.com/100MB.zip");
                downloadFiles[1] = DownloadFile.newInstance("two", "http://ipv4.download.thinkbroadband.com/100MB.zip");
                downloadBatch = DownloadBatch.newInstance("hollyoaks", downloadFiles);
                liteDownloadManagerCommands.download(downloadBatch);
            }
        });

        buttonDeleteAll = findViewById(R.id.button_delete_all);
        buttonDeleteAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                liteDownloadManagerCommands.delete(DOWNLOAD_BATCH_ID_1);
                liteDownloadManagerCommands.delete(DOWNLOAD_BATCH_ID_2);
            }
        });

        bindViews();

        DemoApplication demoApplication = (DemoApplication) getApplicationContext();
        liteDownloadManagerCommands = demoApplication.getLiteDownloadManagerCommands();
        liteDownloadManagerCommands.addDownloadBatchCallback(callback);

        List<DownloadBatchStatus> downloadBatchStatuses = liteDownloadManagerCommands.getAllDownloadBatchStatuses();
        for (DownloadBatchStatus downloadBatchStatus : downloadBatchStatuses) {
            callback.onUpdate(downloadBatchStatus);
        }

        updateViews(downloadBatchStatuses);
    }

    private void updateViews(List<DownloadBatchStatus> downloadBatchStatuses) {
        if (downloadBatchStatuses.isEmpty()) {
            return;
        }

        buttonDownload.setVisibility(View.GONE);
        buttonDeleteAll.setVisibility(View.VISIBLE);

        for (DownloadBatchStatus downloadBatchStatus : downloadBatchStatuses) {
            DownloadBatchId downloadBatchId = downloadBatchStatus.getDownloadBatchId();
            if (DOWNLOAD_BATCH_ID_1.equals(downloadBatchId)) {
                if (downloadBatchStatus.isMarkedAsPaused()) {
                    buttonPauseDownload1.setVisibility(View.GONE);
                    buttonResumeDownload1.setVisibility(View.VISIBLE);
                } else {
                    buttonPauseDownload1.setVisibility(View.VISIBLE);
                    buttonResumeDownload1.setVisibility(View.GONE);
                }
            }

            if (DOWNLOAD_BATCH_ID_2.equals(downloadBatchId)) {
                if (downloadBatchStatus.isMarkedAsPaused()) {
                    buttonPauseDownload2.setVisibility(View.GONE);
                    buttonResumeDownload2.setVisibility(View.VISIBLE);
                } else {
                    buttonPauseDownload2.setVisibility(View.VISIBLE);
                    buttonResumeDownload2.setVisibility(View.GONE);
                }
            }
        }
    }

    private void bindViews() {
        buttonPauseDownload1 = findViewById(R.id.button_pause_downloading_1);
        buttonPauseDownload1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                liteDownloadManagerCommands.pause(DOWNLOAD_BATCH_ID_1);
                buttonPauseDownload1.setVisibility(View.GONE);
                buttonResumeDownload1.setVisibility(View.VISIBLE);
            }
        });

        buttonPauseDownload2 = findViewById(R.id.button_pause_downloading_2);
        buttonPauseDownload2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                liteDownloadManagerCommands.pause(DOWNLOAD_BATCH_ID_2);
                buttonPauseDownload2.setVisibility(View.GONE);
                buttonResumeDownload2.setVisibility(View.VISIBLE);
            }
        });

        buttonResumeDownload1 = findViewById(R.id.button_resume_downloading_1);
        buttonResumeDownload1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                liteDownloadManagerCommands.resume(DOWNLOAD_BATCH_ID_1);
                buttonPauseDownload1.setVisibility(View.VISIBLE);
                buttonResumeDownload1.setVisibility(View.GONE);
            }
        });

        buttonResumeDownload2 = findViewById(R.id.button_resume_downloading_2);
        buttonResumeDownload2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                liteDownloadManagerCommands.resume(DOWNLOAD_BATCH_ID_2);
                buttonPauseDownload2.setVisibility(View.VISIBLE);
                buttonResumeDownload2.setVisibility(View.GONE);
            }
        });
    }

    private final DownloadBatch.Callback callback = new DownloadBatch.Callback() {
        @Override
        public void onUpdate(DownloadBatchStatus downloadBatchStatus) {
            String message = "Batch " + downloadBatchStatus.getDownloadBatchId().getId()
                    + "\ndownloaded: " + downloadBatchStatus.percentageDownloaded()
                    + "\nbytes: " + downloadBatchStatus.bytesDownloaded()
                    + "\nstatus: " + downloadBatchStatus.status()
                    + "\n";

            switch (downloadBatchStatus.getDownloadBatchId().getId()) {
                case "made-in-chelsea":
                    textViewBatch1.setText(message);
                    break;
                case "hollyoaks":
                    textViewBatch2.setText(message);
                    break;
            }

            List<DownloadBatchStatus> allDownloadBatchStatuses = liteDownloadManagerCommands.getAllDownloadBatchStatuses();
            if (allDownloadBatchStatuses.isEmpty()) {
                buttonDownload.setVisibility(View.VISIBLE);
                buttonDeleteAll.setVisibility(View.GONE);

                buttonPauseDownload1.setVisibility(View.VISIBLE);
                buttonResumeDownload1.setVisibility(View.GONE);

                buttonPauseDownload2.setVisibility(View.VISIBLE);
                buttonResumeDownload2.setVisibility(View.GONE);
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
