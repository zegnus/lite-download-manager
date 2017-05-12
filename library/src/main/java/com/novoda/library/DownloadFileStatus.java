package com.novoda.library;

class DownloadFileStatus {

    enum Status {
        PAUSED,
        QUEUED,
        DOWNLOADING,
        DELETION,
        ERROR
    }

    private final DownloadFileId downloadFileId;

    private DownloadError downloadError;
    private FileSize fileSize;
    private Status status;

    DownloadFileStatus(DownloadFileId downloadFileId, DownloadFileStatus.Status status, FileSize fileSize, DownloadError downloadError) {
        this.downloadFileId = downloadFileId;
        this.status = status;
        this.fileSize = fileSize;
        this.downloadError = downloadError;
    }

    void update(FileSize fileSize) {
        this.fileSize = fileSize;
    }

    long bytesDownloaded() {
        return fileSize.getCurrentSize();
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

    boolean isMarkedAsError() {
        return status == Status.ERROR;
    }

    void markAsQueued() {
        status = Status.QUEUED;
    }

    void markForDeletion() {
        status = Status.DELETION;
    }

    void markAsError(DownloadError.Error error) {
        status = Status.ERROR;
        downloadError.setError(error);
    }

    void setErrorCode(int errorCode) {
        downloadError.setErrorCode(errorCode);
    }

    DownloadError getError() {
        return downloadError;
    }
}
