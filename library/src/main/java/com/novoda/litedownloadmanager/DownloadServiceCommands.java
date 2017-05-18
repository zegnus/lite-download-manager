package com.novoda.litedownloadmanager;

interface DownloadServiceCommands {

    void download(DownloadBatch downloadBatch, DownloadBatchCallback callback);
}
