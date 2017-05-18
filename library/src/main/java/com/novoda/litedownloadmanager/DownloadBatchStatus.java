package com.novoda.litedownloadmanager;

public class DownloadBatchStatus {

    private static final long ZERO_BYTES = 0;

    enum Status {
        QUEUED("queued"),
        DOWNLOADING("downloading"),
        PAUSED("paused"),
        ERROR("error"),
        DELETION("deletion"),
        DOWNLOADED("downloaded");

        private final String rawValue;

        Status(String rawValue) {
            this.rawValue = rawValue;
        }

        public String toRawValue() {
            return rawValue;
        }

        public static Status fromRawValue(String rawValue) {
            switch (rawValue) {
                case "queued":
                    return QUEUED;
                case "downloading":
                    return DOWNLOADING;
                case "paused":
                    return PAUSED;
                case "error":
                    return ERROR;
                case "deletion":
                    return DELETION;
                case "downloaded":
                    return DOWNLOADED;
                default:
                    throw new IllegalStateException("Status value " + rawValue + " is not supported");
            }
        }
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
}