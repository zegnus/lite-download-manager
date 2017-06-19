package com.novoda.litedownloadmanager;

import java.util.List;
import java.util.Map;

class DownloadBatch {

    private static final int ZERO_BYTES = 0;

    private final DownloadBatchId downloadBatchId;
    private final DownloadBatchTitle downloadBatchTitle;
    private final Map<DownloadFileId, Long> fileBytesDownloadedMap;
    private final DownloadBatchStatus downloadBatchStatus;
    private final List<DownloadFile> downloadFiles;
    private final DownloadsBatchPersistence downloadsBatchPersistence;

    private DownloadBatchCallback callback;
    private long totalBatchSizeBytes;

    DownloadBatch(DownloadBatchTitle downloadBatchTitle,
                  DownloadBatchId downloadBatchId,
                  List<DownloadFile> downloadFiles,
                  Map<DownloadFileId, Long> fileBytesDownloadedMap,
                  DownloadBatchStatus downloadBatchStatus,
                  DownloadsBatchPersistence downloadsBatchPersistence) {
        this.downloadBatchTitle = downloadBatchTitle;
        this.downloadBatchId = downloadBatchId;
        this.downloadFiles = downloadFiles;
        this.fileBytesDownloadedMap = fileBytesDownloadedMap;
        this.downloadBatchStatus = downloadBatchStatus;
        this.downloadsBatchPersistence = downloadsBatchPersistence;
    }

    void setCallback(DownloadBatchCallback callback) {
        this.callback = callback;
    }

    void download() {
        if (downloadBatchStatus.isMarkedAsPaused()) {
            return;
        }

        if (downloadBatchStatus.isMarkedForDeletion()) {
            return;
        }

        downloadBatchStatus.markAsDownloading();
        notifyCallback(downloadBatchStatus);

        totalBatchSizeBytes = getTotalSize(downloadFiles);

        if (totalBatchSizeBytes <= ZERO_BYTES) {
            DownloadError downloadError = new DownloadError();
            downloadError.setError(DownloadError.Error.CANNOT_DOWNLOAD_FILE);
            downloadBatchStatus.markAsError(downloadError);
            notifyCallback(downloadBatchStatus);
            return;
        }

        DownloadFile.Callback fileDownloadCallback = new DownloadFile.Callback() {

            @Override
            public void onUpdate(DownloadFileStatus downloadFileStatus) {
                fileBytesDownloadedMap.put(downloadFileStatus.getDownloadFileId(), downloadFileStatus.bytesDownloaded());
                long currentBytesDownloaded = getBytesDownloadedFrom(fileBytesDownloadedMap);
                downloadBatchStatus.update(currentBytesDownloaded, totalBatchSizeBytes);
                if (downloadFileStatus.isMarkedAsError()) {
                    downloadBatchStatus.markAsError(downloadFileStatus.getError());
                }

                notifyCallback(downloadBatchStatus);
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
        return downloadBatchStatus.isMarkedAsError() || downloadBatchStatus.isMarkedForDeletion() || downloadBatchStatus.isMarkedAsPaused();
    }

    private long getBytesDownloadedFrom(Map<DownloadFileId, Long> fileBytesDownloadedMap) {
        long bytesDownloaded = 0;
        for (Map.Entry<DownloadFileId, Long> entry : fileBytesDownloadedMap.entrySet()) {
            bytesDownloaded += entry.getValue();
        }
        return bytesDownloaded;
    }

    private void notifyCallback(DownloadBatchStatus downloadBatchStatus) {
        if (callback == null) {
            return;
        }
        callback.onUpdate(downloadBatchStatus);
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
        if (downloadBatchStatus.isMarkedAsPaused()) {
            return;
        }
        downloadBatchStatus.markAsPaused();
        notifyCallback(downloadBatchStatus);
        for (DownloadFile downloadFile : downloadFiles) {
            downloadFile.pause();
        }
    }

    void resume() {
        if (downloadBatchStatus.isMarkedAsResume() || downloadBatchStatus.isMarkedAsDownloading()) {
            return;
        }
        downloadBatchStatus.markAsQueued();
        notifyCallback(downloadBatchStatus);
        for (DownloadFile downloadFile : downloadFiles) {
            downloadFile.resume();
        }
    }

    void delete() {
        downloadsBatchPersistence.deleteAsync(downloadBatchId);
        downloadBatchStatus.markForDeletion();
        notifyCallback(downloadBatchStatus);
        for (DownloadFile downloadFile : downloadFiles) {
            downloadFile.delete();
        }
    }

    DownloadBatchId getId() {
        return downloadBatchId;
    }

    DownloadBatchStatus getDownloadBatchStatus() {
        return downloadBatchStatus;
    }

    void persist() {
        downloadsBatchPersistence.persistAsync(downloadBatchTitle, downloadBatchId, downloadBatchStatus.status(), downloadFiles);
    }
}
