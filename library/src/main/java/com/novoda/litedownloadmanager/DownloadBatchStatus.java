package com.novoda.litedownloadmanager;

import java.security.InvalidParameterException;

public class DownloadBatchStatus {

    private static final long ZERO_BYTES = 0;

    public enum Status {
        QUEUED,
        DOWNLOADING,
        PAUSED,
        ERROR,
        DELETION,
        DOWNLOADED;

        public String toRawValue() {
            return this.name();
        }

        public static Status from(String rawValue) {
            for (Status status : Status.values()) {
                if (status.name().equals(rawValue)) {
                    return status;
                }
            }

            throw new InvalidParameterException("Batch status " + rawValue + " not supported");
        }
    }

    private final DownloadBatchTitle downloadBatchTitle;
    private final DownloadBatchId downloadBatchId;
    private final NotificationCreator notificationCreator;

    private long bytesDownloaded;
    private long totalBatchSizeBytes;
    private int percentageDownloaded;
    private DownloadError downloadError;
    private Status status;

    DownloadBatchStatus(NotificationCreator notificationCreator,
                        DownloadBatchId downloadBatchId,
                        DownloadBatchTitle downloadBatchTitle,
                        Status status) {
        this.notificationCreator = notificationCreator;
        this.downloadBatchTitle = downloadBatchTitle;
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

    public DownloadBatchTitle getDownloadBatchTitle() {
        return downloadBatchTitle;
    }

    NotificationInformation createNotification() {
        return notificationCreator.createNotification(
                downloadBatchTitle,
                percentageDownloaded,
                (int) totalBatchSizeBytes,
                (int) bytesDownloaded
        );
    }

    public Status status() {
        return status;
    }

    void markAsDownloading(DownloadsBatchStatusPersistence persistence) {
        status = Status.DOWNLOADING;
        updateStatus(status, persistence);
    }

    void markAsPaused(DownloadsBatchStatusPersistence persistence) {
        status = Status.PAUSED;
        updateStatus(status, persistence);
    }

    void markAsQueued(DownloadsBatchStatusPersistence persistence) {
        status = Status.QUEUED;
        updateStatus(status, persistence);
    }

    void markForDeletion() {
        status = Status.DELETION;
    }

    private void updateStatus(Status status, DownloadsBatchStatusPersistence persistence) {
        persistence.updateStatusAsync(downloadBatchId, status);
    }

    void markAsError(DownloadError downloadError) {
        this.status = Status.ERROR;
        this.downloadError = downloadError;
    }

    public boolean isMarkedAsPaused() {
        return status == Status.PAUSED;
    }

    boolean isMarkedForDeletion() {
        return status == Status.DELETION;
    }

    public boolean isMarkedAsError() {
        return status == Status.ERROR;
    }

    public boolean isMarkedAsDownloading() {
        return status == Status.DOWNLOADING;
    }

    public boolean isMarkedAsResume() {
        return status == Status.QUEUED;
    }

    public DownloadError.Error getDownloadErrorType() {
        return downloadError.getError();
    }
}
