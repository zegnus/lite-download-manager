package com.novoda.library;

public class DownloadFileStatus {

    public enum Status {
        PAUSED,
        QUEUED,
        DOWNLOADING
    }

    private final DownloadFileId downloadFileId;

    private Status status;
    private long bytesDownloaded;
    private long totalFileSizeBytes;

    public DownloadFileStatus(DownloadFileId downloadFileId, DownloadFileStatus.Status status) {
        this.downloadFileId = downloadFileId;
        this.status = status;
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

    boolean isDownloading() {
        return status == Status.DOWNLOADING;
    }

    boolean isQueued() {
        return status == Status.QUEUED;
    }

    void setIsDownloading() {
        status = Status.DOWNLOADING;
    }

    void setIsPaused() {
        status = Status.PAUSED;
    }

    void setIsQueued() {
        status = Status.QUEUED;
    }
}
