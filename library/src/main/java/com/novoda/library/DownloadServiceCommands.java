package com.novoda.library;

interface DownloadServiceCommands {

    void download(DownloadBatch downloadBatch, DownloadBatch.Callback callback);
}
