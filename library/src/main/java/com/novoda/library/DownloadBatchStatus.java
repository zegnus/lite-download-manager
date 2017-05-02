package com.novoda.library;

public class DownloadBatchStatus {

    public enum Status {
        QUEUED,
        DOWNLOADING,
        PAUSED,
        ERROR,
        DOWNLOADED
    }

    private final DownloadBatchId downloadBatchId;

    private long bytesDownloaded;
    private long totalBatchSizeBytes;
    private int percentageDownloaded;
    private Status status;

    public DownloadBatchStatus(DownloadBatchId downloadBatchId, Status status) {
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

        if (this.bytesDownloaded == this.totalBatchSizeBytes) {
            this.status = Status.DOWNLOADED;
        }
    }

    private int getPercentageFrom(long bytesDownloaded, long totalFileSizeBytes) {
        return (int) ((((float) bytesDownloaded) / ((float) totalFileSizeBytes)) * 100);
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

    void setIsDownloading() {
        status = Status.DOWNLOADING;
    }

    void setIsPaused() {
        status = Status.PAUSED;
    }

    void setIsQueued() {
        status = Status.QUEUED;
    }

    boolean isPaused() {
        return status == Status.PAUSED;
    }
}
