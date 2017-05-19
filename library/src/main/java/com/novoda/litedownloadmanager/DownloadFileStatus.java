package com.novoda.litedownloadmanager;

import com.novoda.notils.logger.simple.Log;

import static android.R.attr.name;

class DownloadFileStatus {

    enum Status {
        PAUSED,
        QUEUED,
        DOWNLOADING,
        DELETION,
        ERROR,
        UNKNOWN;

        public String toRawValue() {
            return this.name();
        }

        public static Status from(String rawValue) {
            for (Status status : Status.values()) {
                if (status.name().equals(rawValue)) {
                    return status;
                }
            }

            Log.e("Unsupported status " + rawValue);
            return UNKNOWN;
        }
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

    DownloadError getError() {
        return downloadError;
    }

    Status getStatus() {
        return status;
    }
}
