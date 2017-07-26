package com.zegnus.litedownloadmanager;

interface DownloadsBatchStatusPersistence {

    void updateStatusAsync(DownloadBatchId downloadBatchId, LiteDownloadBatchStatus.Status status);
}
