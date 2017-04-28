package com.novoda.library;

import com.novoda.notils.logger.simple.Log;

import java.util.HashMap;
import java.util.Map;

public class DownloadBatch {

    private final DownloadBatchId downloadBatchId;
    private DownloadFile[] downloadFiles;
    private long totalBatchSizeBytes;

    public DownloadBatch(DownloadBatchId downloadBatchId, DownloadFile[] downloadFiles) {
        this.downloadBatchId = downloadBatchId;
        this.downloadFiles = downloadFiles;
    }

    void download(final Callback callback) {
        Log.v("Download batch start");

        totalBatchSizeBytes = getTotalSize(downloadFiles);

        DownloadFile.Callback fileDownloadCallback = new DownloadFile.Callback() {
            private Map<DownloadFileId, Long> fileBytesDownloadedMap = new HashMap<>();

            @Override
            public void onUpdate(DownloadFileStatus downloadFileStatus) {
                fileBytesDownloadedMap.put(downloadFileStatus.getDownloadFileId(), downloadFileStatus.bytesDownloaded());
                callback.onUpdate(new DownloadBatchStatus(downloadBatchId, fileBytesDownloadedMap, totalBatchSizeBytes));
            }
        };

        DownloadFile[] files = downloadFiles;
        for (DownloadFile file : files) {
            file.download(fileDownloadCallback);
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

    public interface Callback {

        void onUpdate(DownloadBatchStatus downloadBatchStatus);
    }
}
