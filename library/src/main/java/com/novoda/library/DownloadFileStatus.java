package com.novoda.library;

public class DownloadFileStatus {

    private final DownloadFileId downloadFileId;

    private long bytesDownloaded;
    private long totalFileSizeBytes;

    public DownloadFileStatus(DownloadFileId downloadFileId) {
        this.downloadFileId = downloadFileId;
    }

    void update(long bytesDownloaded, long totalFileSizeBytes) {
        this.bytesDownloaded = bytesDownloaded;
        this.totalFileSizeBytes = totalFileSizeBytes;
    }

    long bytesDownloaded() {
        return bytesDownloaded;
    }

    DownloadFileId getDownloadFileId() {
        return downloadFileId;
    }
}
