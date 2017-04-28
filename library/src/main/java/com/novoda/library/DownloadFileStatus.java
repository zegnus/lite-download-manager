package com.novoda.library;

class DownloadFileStatus {

    private final DownloadFileId downloadFileId;
    private final long bytesDownloaded;
    private final long totalFileSizeBytes;

    DownloadFileStatus(DownloadFileId downloadFileId, long bytesDownloaded, long totalFileSizeBytes) {
        this.downloadFileId = downloadFileId;
        this.bytesDownloaded = bytesDownloaded;
        this.totalFileSizeBytes = totalFileSizeBytes;
    }

    long bytesDownloaded() {
        return bytesDownloaded;
    }

    public DownloadFileId getDownloadFileId() {
        return downloadFileId;
    }
}
