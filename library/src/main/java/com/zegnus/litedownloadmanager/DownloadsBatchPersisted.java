package com.zegnus.litedownloadmanager;

public class DownloadsBatchPersisted {

    private final DownloadBatchTitle downloadBatchTitle;
    private final DownloadBatchId downloadBatchId;
    private final LiteDownloadBatchStatus.Status status;

    DownloadsBatchPersisted(DownloadBatchTitle downloadBatchTitle, DownloadBatchId downloadBatchId, LiteDownloadBatchStatus.Status status) {
        this.downloadBatchTitle = downloadBatchTitle;
        this.downloadBatchId = downloadBatchId;
        this.status = status;
    }

    public DownloadBatchId downloadBatchId() {
        return downloadBatchId;
    }

    public LiteDownloadBatchStatus.Status downloadBatchStatus() {
        return status;
    }

    public DownloadBatchTitle downloadBatchTitle() {
        return downloadBatchTitle;
    }
}
