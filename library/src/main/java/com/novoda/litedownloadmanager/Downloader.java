package com.novoda.litedownloadmanager;

interface Downloader {

    void startDownloading(String url, FileSize fileSize, Callback callback);

    void stopDownloading();

    interface Callback {

        void onBytesRead(byte[] buffer, int bytesRead);

        void onError();

        void onDownloadFinished();
    }
}
