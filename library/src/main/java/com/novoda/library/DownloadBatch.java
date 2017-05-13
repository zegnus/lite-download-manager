package com.novoda.library;

import java.util.HashMap;
import java.util.Map;

public class DownloadBatch {

    private static final int ZERO_BYTES = 0;

    private final DownloadBatchId downloadBatchId;
    private final Map<DownloadFileId, Long> fileBytesDownloadedMap;
    private final DownloadBatchStatus downloadBatchStatus;

    private DownloadFile[] downloadFiles;
    private Callback callback;
    private long totalBatchSizeBytes;

    public static DownloadBatch newInstance(String id, DownloadFile[] downloadFiles) {
        DownloadBatchId downloadBatchId = DownloadBatchId.from(id);
        DownloadBatchStatus downloadBatchStatus = new DownloadBatchStatus(downloadBatchId, DownloadBatchStatus.Status.QUEUED);
        return new DownloadBatch(
                downloadBatchId,
                downloadFiles,
                new HashMap<DownloadFileId, Long>(),
                downloadBatchStatus
        );
    }

    DownloadBatch(DownloadBatchId downloadBatchId,
                  DownloadFile[] downloadFiles,
                  Map<DownloadFileId, Long> fileBytesDownloadedMap,
                  DownloadBatchStatus downloadBatchStatus) {
        this.downloadBatchId = downloadBatchId;
        this.downloadFiles = downloadFiles;
        this.fileBytesDownloadedMap = fileBytesDownloadedMap;
        this.downloadBatchStatus = downloadBatchStatus;
    }

    void setCallback(Callback callback) {
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

    private long getTotalSize(DownloadFile[] downloadFiles) {
        if (totalBatchSizeBytes == 0) {
            for (DownloadFile downloadFile : downloadFiles) {
                totalBatchSizeBytes += downloadFile.getTotalSize();
            }
        }

        return totalBatchSizeBytes;
    }

    void pause() {
        downloadBatchStatus.markAsPaused();
        notifyCallback(downloadBatchStatus);
        for (DownloadFile downloadFile : downloadFiles) {
            downloadFile.pause();
        }
    }

    void resume() {
        downloadBatchStatus.markAsQueued();
        notifyCallback(downloadBatchStatus);
        for (DownloadFile downloadFile : downloadFiles) {
            downloadFile.resume();
        }
    }

    void delete() {
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

    public interface Callback {

        void onUpdate(DownloadBatchStatus downloadBatchStatus);
    }
}
