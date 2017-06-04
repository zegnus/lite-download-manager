package com.novoda.litedownloadmanager.demo;

import com.novoda.litedownloadmanager.FileDownloader;
import com.novoda.litedownloadmanager.FileSize;
import com.novoda.notils.logger.simple.Log;

class CustomFileDownloader implements FileDownloader {

    private static final int BYTES_READ = 50;

    private byte[] buffer = new byte[5000];

    private boolean canDownload;

    @Override
    public void startDownloading(String url, FileSize fileSize, Callback callback) {
        Log.v("Start downloading");

        canDownload = true;

        while (canDownload && fileSize.getCurrentSize() < fileSize.getTotalSize()) {
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            callback.onBytesRead(buffer, BYTES_READ);
            Log.v("New bytes downloaded: " + fileSize);
        }

        Log.v("Download finished");
        callback.onDownloadFinished();
    }

    @Override
    public void stopDownloading() {
        Log.v("Stop downloading");
        canDownload = false;
    }
}
