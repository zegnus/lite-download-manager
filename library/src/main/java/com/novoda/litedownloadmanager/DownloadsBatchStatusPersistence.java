package com.novoda.litedownloadmanager;

interface DownloadsBatchStatusPersistence {

    void updateStatusAsync(DownloadBatchId downloadBatchId, DownloadBatchStatus.Status status);
}
