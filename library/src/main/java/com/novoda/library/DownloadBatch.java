package com.novoda.library;

import com.novoda.notils.logger.simple.Log;

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
        Log.v("Download batch start");

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

        Log.v("Download batch end");
    }

    private long getTotalSize(DownloadFile[] downloadFiles) {
        if (totalBatchSizeBytes == 0) {
            for (DownloadFile downloadFile : downloadFiles) {
                totalBatchSizeBytes += downloadFile.getTotalSize();
            }
        }

        return totalBatchSizeBytes;
    }

    public void pause() {
        for (DownloadFile downloadFile : downloadFiles) {
            downloadFile.pause();
        }
    }

    public void unpause() {
        for (DownloadFile downloadFile : downloadFiles) {
            downloadFile.unpause();
        }
    }

    public DownloadBatchId getId() {
        return downloadBatchId;
    }

    public interface Callback {

        void onUpdate(DownloadBatchStatus downloadBatchStatus);
    }
}
