package com.zegnus.litedownloadmanager;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobCreator;
import com.novoda.notils.logger.simple.Log;

class LiteJobCreator implements JobCreator {

    static final String TAG = "test";

    private final LiteDownloadManager liteDownloadManager;

    LiteJobCreator(LiteDownloadManager liteDownloadManager) {
        this.liteDownloadManager = liteDownloadManager;
    }

    @Override
    public Job create(String tag) {
        Log.v("Ferran, create " + tag);
        if (tag.equals(TAG)) {
           return new LiteJobDownload(liteDownloadManager);
        }

        return null;
    }
}
