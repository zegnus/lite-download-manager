package com.zegnus.litedownloadmanager;

import java.util.List;
import java.util.Map;

import static com.zegnus.litedownloadmanager.DownloadBatchStatus.Status.*;

class DownloadBatch {

    private static final int ZERO_BYTES = 0;

    private final DownloadBatchId downloadBatchId;
    private final DownloadBatchTitle downloadBatchTitle;
    private final Map<DownloadFileId, Long> fileBytesDownloadedMap;
    private final LiteDownloadBatchStatus liteDownloadBatchStatus;
    private final List<DownloadFile> downloadFiles;
    private final DownloadsBatchPersistence downloadsBatchPersistence;

    private DownloadBatchCallback callback;
    private long totalBatchSizeBytes;

    DownloadBatch(DownloadBatchTitle downloadBatchTitle,
                  DownloadBatchId downloadBatchId,
                  List<DownloadFile> downloadFiles,
                  Map<DownloadFileId, Long> fileBytesDownloadedMap,
                  LiteDownloadBatchStatus liteDownloadBatchStatus,
                  DownloadsBatchPersistence downloadsBatchPersistence) {
        this.downloadBatchTitle = downloadBatchTitle;
        this.downloadBatchId = downloadBatchId;
        this.downloadFiles = downloadFiles;
        this.fileBytesDownloadedMap = fileBytesDownloadedMap;
        this.liteDownloadBatchStatus = liteDownloadBatchStatus;
        this.downloadsBatchPersistence = downloadsBatchPersistence;
    }

    void setCallback(DownloadBatchCallback callback) {
        this.callback = callback;
    }

    void download() {
        if (liteDownloadBatchStatus.status() == PAUSED) {
            return;
        }

        if (liteDownloadBatchStatus.status() == DELETION) {
            return;
        }

        liteDownloadBatchStatus.markAsDownloading(downloadsBatchPersistence);
        notifyCallback(liteDownloadBatchStatus);

        totalBatchSizeBytes = getTotalSize(downloadFiles);

        if (totalBatchSizeBytes <= ZERO_BYTES) {
            DownloadError downloadError = new DownloadError();
            downloadError.setError(DownloadError.Error.CANNOT_DOWNLOAD_FILE);
            liteDownloadBatchStatus.markAsError(downloadError);
            notifyCallback(liteDownloadBatchStatus);
            return;
        }

        DownloadFile.Callback fileDownloadCallback = new DownloadFile.Callback() {

            @Override
            public void onUpdate(DownloadFileStatus downloadFileStatus) {
                fileBytesDownloadedMap.put(downloadFileStatus.getDownloadFileId(), downloadFileStatus.bytesDownloaded());
                long currentBytesDownloaded = getBytesDownloadedFrom(fileBytesDownloadedMap);
                liteDownloadBatchStatus.update(currentBytesDownloaded, totalBatchSizeBytes);
                if (downloadFileStatus.isMarkedAsError()) {
                    liteDownloadBatchStatus.markAsError(downloadFileStatus.getError());
                }

                notifyCallback(liteDownloadBatchStatus);
            }
        };

        for (DownloadFile downloadFile : downloadFiles) {
            downloadFile.download(fileDownloadCallback);
            if (batchCannotContinue()) {
                return;
            }
        }
    }

    private boolean batchCannotContinue() {
        DownloadBatchStatus.Status status = liteDownloadBatchStatus.status();
        return status == ERROR || status == DELETION || status == PAUSED;
    }

    private long getBytesDownloadedFrom(Map<DownloadFileId, Long> fileBytesDownloadedMap) {
        long bytesDownloaded = 0;
        for (Map.Entry<DownloadFileId, Long> entry : fileBytesDownloadedMap.entrySet()) {
            bytesDownloaded += entry.getValue();
        }
        return bytesDownloaded;
    }

    private void notifyCallback(LiteDownloadBatchStatus liteDownloadBatchStatus) {
        if (callback == null) {
            return;
        }
        callback.onUpdate(liteDownloadBatchStatus);
    }

    private long getTotalSize(List<DownloadFile> downloadFiles) {
        if (totalBatchSizeBytes == 0) {
            for (DownloadFile downloadFile : downloadFiles) {
                totalBatchSizeBytes += downloadFile.getTotalSize();
            }
        }

        return totalBatchSizeBytes;
    }

    void pause() {
        if (liteDownloadBatchStatus.status() == PAUSED) {
            return;
        }
        liteDownloadBatchStatus.markAsPaused(downloadsBatchPersistence);
        notifyCallback(liteDownloadBatchStatus);
        for (DownloadFile downloadFile : downloadFiles) {
            downloadFile.pause();
        }
    }

    void resume() {
        if (liteDownloadBatchStatus.status() == QUEUED || liteDownloadBatchStatus.status() == DOWNLOADING) {
            return;
        }
        liteDownloadBatchStatus.markAsQueued(downloadsBatchPersistence);
        notifyCallback(liteDownloadBatchStatus);
        for (DownloadFile downloadFile : downloadFiles) {
            downloadFile.resume();
        }
    }

    void delete() {
        downloadsBatchPersistence.deleteAsync(downloadBatchId);
        liteDownloadBatchStatus.markForDeletion();
        notifyCallback(liteDownloadBatchStatus);
        for (DownloadFile downloadFile : downloadFiles) {
            downloadFile.delete();
        }
    }

    DownloadBatchId getId() {
        return downloadBatchId;
    }

    LiteDownloadBatchStatus status() {
        return liteDownloadBatchStatus;
    }

    void persist() {
        downloadsBatchPersistence.persistAsync(downloadBatchTitle, downloadBatchId, liteDownloadBatchStatus.status(), downloadFiles);
    }
}
