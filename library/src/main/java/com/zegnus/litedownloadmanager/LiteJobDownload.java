package com.zegnus.litedownloadmanager;

import android.support.annotation.NonNull;

import com.evernote.android.job.Job;
import com.novoda.notils.logger.simple.Log;

class LiteJobDownload extends Job {

    private final LiteDownloadManager liteDownloadManager;

    LiteJobDownload(LiteDownloadManager liteDownloadManager) {
        this.liteDownloadManager = liteDownloadManager;
    }

    @NonNull
    @Override
    protected Result onRunJob(Params params) {
        liteDownloadManager.submitAllStoredDownloads(new AllStoredDownloadsSubmittedCallback() {
            @Override
            public void onAllDownloadsSubmitted() {
                // done
                Log.v("LiteJobDownload all jobs submitted");
            }
        });
        Log.v("LiteJobDownload run network recovery job");
        return Result.SUCCESS;
    }
}
