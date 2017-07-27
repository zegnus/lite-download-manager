package com.zegnus.litedownloadmanager;

public interface DownloadsBatchPersisted {

    DownloadBatchId downloadBatchId();

    DownloadBatchStatus.Status downloadBatchStatus();

    DownloadBatchTitle downloadBatchTitle();
}
