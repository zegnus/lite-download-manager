package com.novoda.library;

import com.novoda.notils.logger.simple.Log;

public class DownloadFile {

    private static final long TOTAL_FILE_SIZE = 5000000;
    private static final int BUFFER_SIZE = 5000;
    private static final int NETWORK_COST = 10;

    private final DownloadFileId downloadFileId;
    private final String url;

    private long bytesDownloaded;
    private long totalFileSizeBytes;
    private boolean proceed;

    public DownloadFile(DownloadFileId downloadFileId, String url) {
        this.url = url;
        this.downloadFileId = downloadFileId;
        this.proceed = true;
    }

    void download(Callback callback) {
        totalFileSizeBytes = getTotalSize();

        while (proceed && bytesDownloaded < totalFileSizeBytes) {
            try {
                Thread.sleep(NETWORK_COST);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            bytesDownloaded += BUFFER_SIZE;

            if (proceed) {
                callback.onUpdate(new DownloadFileStatus(downloadFileId, bytesDownloaded, totalFileSizeBytes));
            }
        }

        Log.v("Download file stop url: " + url);
    }

    long getTotalSize() {
        if (totalFileSizeBytes == 0) {
            // request network file size
            totalFileSizeBytes = TOTAL_FILE_SIZE;
        }

        return totalFileSizeBytes;
    }

    void pause() {
        proceed = false;
    }

    void unpause() {
        proceed = true;
    }

    interface Callback {

        void onUpdate(DownloadFileStatus downloadFileStatus);
    }
}
