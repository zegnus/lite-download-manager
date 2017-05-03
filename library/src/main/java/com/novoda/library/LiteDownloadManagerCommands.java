package com.novoda.library;

import java.util.List;

public interface LiteDownloadManagerCommands {
        
    void download(DownloadBatch downloadBatch);

    void pause(DownloadBatchId downloadBatchId);

    void resume(DownloadBatchId downloadBatchId);

    void delete(DownloadBatchId downloadBatchId);

    void addDownloadBatchCallback(DownloadBatch.Callback downloadBatchCallback);

    void removeDownloadBatchCallback(DownloadBatch.Callback downloadBatchCallback);

    List<DownloadBatchStatus> getAllDownloadBatchStatuses();
}
