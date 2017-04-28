package com.novoda.library;

import java.util.Map;

public class DownloadBatchStatus {

    private final DownloadBatchId downloadBatchId;
    private final long bytesDownloaded;
    private final long totalFileSizeBytes;

    private int percentageDownloaded;

    DownloadBatchStatus(DownloadBatchId downloadBatchId, Map<DownloadFileId, Long> fileBytesDownloadedMap, long totalFileSizeBytes) {
        this.downloadBatchId = downloadBatchId;
        this.bytesDownloaded = getBytesDownloadedFrom(fileBytesDownloadedMap);
        this.totalFileSizeBytes = totalFileSizeBytes;
        this.percentageDownloaded = getPercentageFrom(bytesDownloaded, totalFileSizeBytes);
    }

    private long getBytesDownloadedFrom(Map<DownloadFileId, Long> fileBytesDownloadedMap) {
        long bytesDownloaded = 0;
        for (Map.Entry<DownloadFileId, Long> entry : fileBytesDownloadedMap.entrySet()) {
            bytesDownloaded += entry.getValue();
        }
        return bytesDownloaded;
    }

    private int getPercentageFrom(long bytesDownloaded, long totalFileSizeBytes) {
        return (int) ((((float) bytesDownloaded) / ((float) totalFileSizeBytes)) * 100);
    }

    public long bytesDownloaded() {
        return bytesDownloaded;
    }

    public int percentageDownloaded() {
        return percentageDownloaded;
    }

    public DownloadBatchId getDownloadBatchId() {
        return downloadBatchId;
    }

    boolean isCompleted() {
        return bytesDownloaded == totalFileSizeBytes;
    }
}
