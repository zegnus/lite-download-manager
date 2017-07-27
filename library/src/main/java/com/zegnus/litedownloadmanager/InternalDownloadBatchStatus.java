package com.zegnus.litedownloadmanager;

interface InternalDownloadBatchStatus extends DownloadBatchStatus {

    void update(long currentBytesDownloaded, long totalBatchSizeBytes);

    void markAsDownloading(DownloadsBatchStatusPersistence persistence);

    void markAsPaused(DownloadsBatchStatusPersistence persistence);

    void markAsQueued(DownloadsBatchStatusPersistence persistence);

    void markForDeletion();

    void markAsError(DownloadError downloadError);
}
