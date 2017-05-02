package com.novoda.library;

public class DownloadFile {

    private static final long TOTAL_FILE_SIZE = 5000000;
    private static final int BUFFER_SIZE = 5000;
    private static final int NETWORK_COST = 10;

    private final DownloadFileId downloadFileId;
    private final String url;
    private final DownloadFileStatus downloadFileStatus;

    private long bytesDownloaded;
    private long totalFileSizeBytes;

    public DownloadFile(DownloadFileId downloadFileId, String url, DownloadFileStatus downloadFileStatus) {
        this.url = url;
        this.downloadFileId = downloadFileId;
        this.downloadFileStatus = downloadFileStatus;
    }

    void download(Callback callback) {
        callback.onUpdate(downloadFileStatus);

        totalFileSizeBytes = getTotalSize();

        moveStatusToDownloadingIfQueued();

        while (downloadFileStatus.isDownloading() && bytesDownloaded < totalFileSizeBytes) {
            try {
                Thread.sleep(NETWORK_COST);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if (downloadFileStatus.isDownloading()) {
                bytesDownloaded += BUFFER_SIZE;
                downloadFileStatus.update(bytesDownloaded, totalFileSizeBytes);
                callback.onUpdate(downloadFileStatus);
            }
        }
    }

    private void moveStatusToDownloadingIfQueued() {
        if (downloadFileStatus.isQueued()) {
            downloadFileStatus.setIsDownloading();
        }
    }

    long getTotalSize() {
        if (totalFileSizeBytes == 0) {
            // request network file size
            totalFileSizeBytes = TOTAL_FILE_SIZE;
        }

        return totalFileSizeBytes;
    }

    void pause() {
        downloadFileStatus.setIsPaused();
    }

    void resume() {
        downloadFileStatus.setIsQueued();
    }

    interface Callback {

        void onUpdate(DownloadFileStatus downloadFileStatus);
    }
}
