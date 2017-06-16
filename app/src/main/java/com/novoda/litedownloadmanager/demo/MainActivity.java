package com.novoda.litedownloadmanager.demo;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.novoda.litedownloadmanager.AllBatchStatusesCallback;
import com.novoda.litedownloadmanager.Batch;
import com.novoda.litedownloadmanager.DownloadBatchCallback;
import com.novoda.litedownloadmanager.DownloadBatchId;
import com.novoda.litedownloadmanager.DownloadBatchStatus;
import com.novoda.litedownloadmanager.LiteDownloadManagerCommands;
import com.novoda.notils.logger.simple.Log;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private DownloadBatchId downloadBatchId1;
    private DownloadBatchId downloadBatchId2;
    private TextView textViewBatch1;
    private TextView textViewBatch2;

    private LiteDownloadManagerCommands liteDownloadManagerCommands;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.setShowLogs(true);

        textViewBatch1 = (TextView) findViewById(R.id.batch_1);
        textViewBatch2 = (TextView) findViewById(R.id.batch_2);

        View buttonDownload = findViewById(R.id.button_start_downloading);
        buttonDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Batch batch = new Batch.Builder("Made in chelsea")
                        .addFile("http://ipv4.download.thinkbroadband.com/10MB.zip")
                        .addFile("http://ipv4.download.thinkbroadband.com/10MB.zip")
                        .build();
                downloadBatchId1 = liteDownloadManagerCommands.download(batch);

                batch = new Batch.Builder("Hollyoaks")
                        .addFile("http://ipv4.download.thinkbroadband.com/10MB.zip")
                        .addFile("http://ipv4.download.thinkbroadband.com/10MB.zip")
                        .build();
                downloadBatchId2 = liteDownloadManagerCommands.download(batch);

                bindViews(downloadBatchId1, downloadBatchId2);
            }
        });

        View buttonDeleteAll = findViewById(R.id.button_delete_all);
        buttonDeleteAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                liteDownloadManagerCommands.delete(downloadBatchId1);
                liteDownloadManagerCommands.delete(downloadBatchId2);
            }
        });

        DemoApplication demoApplication = (DemoApplication) getApplicationContext();
        liteDownloadManagerCommands = demoApplication.getLiteDownloadManagerCommands();
        liteDownloadManagerCommands.addDownloadBatchCallback(callback);
        liteDownloadManagerCommands.getAllDownloadBatchStatuses(new AllBatchStatusesCallback() {
            @Override
            public void onReceived(List<DownloadBatchStatus> downloadBatchStatuses) {
                for (DownloadBatchStatus downloadBatchStatus : downloadBatchStatuses) {
                    String title = downloadBatchStatus.getDownloadBatchTitle().toString();
                    DownloadBatchId downloadBatchId = downloadBatchStatus.getDownloadBatchId();
                    if (title.equals("Made in chelsea")) {
                        downloadBatchId1 = downloadBatchId;
                    } else if (title.equals("Hollyoaks")) {
                        downloadBatchId2 = downloadBatchId;
                    }

                    bindViews(downloadBatchId1, downloadBatchId2);

                    callback.onUpdate(downloadBatchStatus);
                }
            }
        });
    }

    private void bindViews(DownloadBatchId downloadBatchId1, DownloadBatchId downloadBatchId2) {
        View buttonPauseDownload1 = findViewById(R.id.button_pause_downloading_1);
        setPause(buttonPauseDownload1, downloadBatchId1);

        View buttonPauseDownload2 = findViewById(R.id.button_pause_downloading_2);
        setPause(buttonPauseDownload2, downloadBatchId2);

        View buttonResumeDownload1 = findViewById(R.id.button_resume_downloading_1);
        setResume(buttonResumeDownload1, downloadBatchId1);

        View buttonResumeDownload2 = findViewById(R.id.button_resume_downloading_2);
        setResume(buttonResumeDownload2, downloadBatchId2);
    }

    private void setPause(View button, final DownloadBatchId downloadBatchId) {
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                liteDownloadManagerCommands.pause(downloadBatchId);
            }
        });
    }

    private void setResume(View button, final DownloadBatchId downloadBatchId) {
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                liteDownloadManagerCommands.resume(downloadBatchId);
            }
        });
    }

    private final DownloadBatchCallback callback = new DownloadBatchCallback() {
        @Override
        public void onUpdate(DownloadBatchStatus downloadBatchStatus) {
            String status = getStatusMessage(downloadBatchStatus);

            String message = "Batch " + downloadBatchStatus.getDownloadBatchTitle().toString()
                    + "\ndownloaded: " + downloadBatchStatus.percentageDownloaded()
                    + "\nbytes: " + downloadBatchStatus.bytesDownloaded()
                    + status
                    + "\n";

            DownloadBatchId downloadBatchId = downloadBatchStatus.getDownloadBatchId();
            if (downloadBatchId.equals(downloadBatchId1)) {
                textViewBatch1.setText(message);
            } else if (downloadBatchId.equals(downloadBatchId2)) {
                textViewBatch2.setText(message);
            }
        }

        @NonNull
        private String getStatusMessage(DownloadBatchStatus downloadBatchStatus) {
            if (downloadBatchStatus.isMarkedAsError()) {
                return "\nstatus: " + downloadBatchStatus.status()
                        + " - " + downloadBatchStatus.getDownloadErrorType();
            } else {
                return "\nstatus: " + downloadBatchStatus.status();
            }
        }
    };
}
