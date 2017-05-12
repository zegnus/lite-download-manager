package com.novoda.library;

public class DownloadBatchStatus {

    private static final long ZERO_BYTES = 0;

    enum Status {
        QUEUED,
        DOWNLOADING,
        PAUSED,
        ERROR,
        DELETION,
        DOWNLOADED
    }

    private final DownloadBatchId downloadBatchId;

    private long bytesDownloaded;
    private long totalBatchSizeBytes;
    private int percentageDownloaded;
    private DownloadError downloadError;
    private Status status;

    DownloadBatchStatus(DownloadBatchId downloadBatchId, Status status) {
        this.downloadBatchId = downloadBatchId;
        this.status = status;
    }

    public long bytesDownloaded() {
        return bytesDownloaded;
    }

    void update(long currentBytesDownloaded, long totalBatchSizeBytes) {
        this.bytesDownloaded = currentBytesDownloaded;
        this.totalBatchSizeBytes = totalBatchSizeBytes;
        this.percentageDownloaded = getPercentageFrom(bytesDownloaded, totalBatchSizeBytes);

        if (this.bytesDownloaded == this.totalBatchSizeBytes && this.totalBatchSizeBytes != ZERO_BYTES) {
            this.status = Status.DOWNLOADED;
        }
    }

    private int getPercentageFrom(long bytesDownloaded, long totalFileSizeBytes) {
        if (totalBatchSizeBytes <= ZERO_BYTES) {
            return 0;
        } else {
            return (int) ((((float) bytesDownloaded) / ((float) totalFileSizeBytes)) * 100);
        }
    }

    public int percentageDownloaded() {
        return percentageDownloaded;
    }

    public DownloadBatchId getDownloadBatchId() {
        return downloadBatchId;
    }

    public Status status() {
        return status;
    }

    void markAsDownloading() {
        status = Status.DOWNLOADING;
    }

    void markAsPaused() {
        status = Status.PAUSED;
    }

    void markAsQueued() {
        status = Status.QUEUED;
    }

    void markForDeletion() {
        status = Status.DELETION;
    }

    public boolean isMarkedAsPaused() {
        return status == Status.PAUSED;
    }

    void markAsError(DownloadError downloadError) {
        this.status = Status.ERROR;
        this.downloadError = downloadError;
    }

    boolean isMarkedForDeletion() {
        return status == Status.DELETION;
    }

    public boolean isMarkedAsError() {
        return status == Status.ERROR;
    }

    public DownloadError.Error getDownloadErrorType() {
        return downloadError.getError();
    }

    public int getDownloadErrorCode() {
        return downloadError.getErrorCode();
    }
}
