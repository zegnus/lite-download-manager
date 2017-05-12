package com.novoda.library;

import android.content.Context;

import java.util.List;

public interface LiteDownloadManagerCommands {
        
    void download(DownloadBatch downloadBatch, Context context);

    void pause(DownloadBatchId downloadBatchId);

    void resume(DownloadBatchId downloadBatchId, Context context);

    void delete(DownloadBatchId downloadBatchId);

    void addDownloadBatchCallback(DownloadBatch.Callback downloadBatchCallback);

    void removeDownloadBatchCallback(DownloadBatch.Callback downloadBatchCallback);

    List<DownloadBatchStatus> getAllDownloadBatchStatuses();
}
