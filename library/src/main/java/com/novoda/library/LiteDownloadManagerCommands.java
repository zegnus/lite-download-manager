package com.novoda.library;

public interface LiteDownloadManagerCommands {
        
    void download(DownloadBatch downloadBatch);

    void pause(DownloadBatchId downloadBatchId);

    void resume(DownloadBatchId downloadBatchId);

    void addDownloadBatchCallback(DownloadBatch.Callback downloadBatchCallback);

    void removeDownloadBatchCallback(DownloadBatch.Callback downloadBatchCallback);

    void destroy();
}
