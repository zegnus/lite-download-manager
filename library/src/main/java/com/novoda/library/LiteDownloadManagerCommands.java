package com.novoda.library;

public interface LiteDownloadManagerCommands {
        
    void download(DownloadBatch downloadBatch, DownloadBatch.Callback callback);
}
