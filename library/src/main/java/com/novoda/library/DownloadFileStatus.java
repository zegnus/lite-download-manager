package com.novoda.library;

class DownloadFileStatus {

    enum Status {
        PAUSED,
        QUEUED,
        DOWNLOADING,
        DELETION
    }

    private final DownloadFileId downloadFileId;

    private Status status;
    private long bytesDownloaded;
    private long totalFileSizeBytes;

    DownloadFileStatus(DownloadFileId downloadFileId, DownloadFileStatus.Status status) {
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

    boolean isMarkedAsDownloading() {
        return status == Status.DOWNLOADING;
    }

    boolean isMarkedAsQueued() {
        return status == Status.QUEUED;
    }

    boolean isMarkedForDeletion() {
        return status == Status.DELETION;
    }

    void markAsDownloading() {
        status = Status.DOWNLOADING;
    }

    void isMarkedAsPaused() {
        status = Status.PAUSED;
    }

    void markAsQueued() {
        status = Status.QUEUED;
    }

    void markForDeletion() {
        status = Status.DELETION;
    }
}
