package com.novoda.library;

import java.util.HashMap;
import java.util.Map;

public class DownloadBatch {

    private final DownloadBatchId downloadBatchId;
    private final Map<DownloadFileId, Long> fileBytesDownloadedMap;
    private final DownloadBatchStatus downloadBatchStatus;

    private DownloadFile[] downloadFiles;
    private long totalBatchSizeBytes;
    private Callback callback;

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
        if (downloadBatchStatus.isPaused()) {
            return;
        }

        downloadBatchStatus.setIsDownloading();
        totalBatchSizeBytes = getTotalSize(downloadFiles);

        DownloadFile.Callback fileDownloadCallback = new DownloadFile.Callback() {

            @Override
            public void onUpdate(DownloadFileStatus downloadFileStatus) {
                fileBytesDownloadedMap.put(downloadFileStatus.getDownloadFileId(), downloadFileStatus.bytesDownloaded());
                long currentBytesDownloaded = getBytesDownloadedFrom(fileBytesDownloadedMap);
                downloadBatchStatus.update(currentBytesDownloaded, totalBatchSizeBytes);
                notifyCallback(downloadBatchStatus);
            }
        };

        for (DownloadFile downloadFile : downloadFiles) {
            downloadFile.download(fileDownloadCallback);
        }
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
        downloadBatchStatus.setIsPaused();
        notifyCallback(downloadBatchStatus);
        for (DownloadFile downloadFile : downloadFiles) {
            downloadFile.pause();
        }
    }

    void resume() {
        downloadBatchStatus.setIsQueued();
        notifyCallback(downloadBatchStatus);
        for (DownloadFile downloadFile : downloadFiles) {
            downloadFile.resume();
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
