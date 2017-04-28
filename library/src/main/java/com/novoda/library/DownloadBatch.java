package com.novoda.library;

import java.util.Map;

public class DownloadBatch {

    private final DownloadBatchId downloadBatchId;
    private final Map<DownloadFileId, Long> fileBytesDownloadedMap;

    private DownloadFile[] downloadFiles;
    private long totalBatchSizeBytes;

    public DownloadBatch(DownloadBatchId downloadBatchId, DownloadFile[] downloadFiles, Map<DownloadFileId, Long> fileBytesDownloadedMap) {
        this.downloadBatchId = downloadBatchId;
        this.downloadFiles = downloadFiles;
        this.fileBytesDownloadedMap = fileBytesDownloadedMap;
    }

    void download(final Callback callback) {
        totalBatchSizeBytes = getTotalSize(downloadFiles);

        DownloadFile.Callback fileDownloadCallback = new DownloadFile.Callback() {

            @Override
            public void onUpdate(DownloadFileStatus downloadFileStatus) {
                fileBytesDownloadedMap.put(downloadFileStatus.getDownloadFileId(), downloadFileStatus.bytesDownloaded());
                callback.onUpdate(new DownloadBatchStatus(downloadBatchId, fileBytesDownloadedMap, totalBatchSizeBytes));
            }
        };

        for (DownloadFile downloadFile : downloadFiles) {
            downloadFile.download(fileDownloadCallback);
        }
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
        for (DownloadFile downloadFile : downloadFiles) {
            downloadFile.pause();
        }
    }

    void unpause() {
        for (DownloadFile downloadFile : downloadFiles) {
            downloadFile.unpause();
        }
    }

    DownloadBatchId getId() {
        return downloadBatchId;
    }

    public interface Callback {

        void onUpdate(DownloadBatchStatus downloadBatchStatus);

        Callback NO_OP = new Callback() {
            @Override
            public void onUpdate(DownloadBatchStatus downloadBatchStatus) {
                // no-op
            }
        };
    }
}
