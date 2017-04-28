package com.novoda.library;

import java.util.Map;

public class DownloadBatchStatus {

    private final long bytesDownloaded;
    private final long totalFileSizeBytes;

    private int percentatgeDownloaded;

    public DownloadBatchStatus(Map<DownloadFileId, Long> fileBytesDownloadedMap, long totalFileSizeBytes) {
        this.bytesDownloaded = getBytesDownloadedFrom(fileBytesDownloadedMap);
        this.totalFileSizeBytes = totalFileSizeBytes;
        this.percentatgeDownloaded = getPercentatgeFrom(bytesDownloaded, totalFileSizeBytes);
    }

    private long getBytesDownloadedFrom(Map<DownloadFileId, Long> fileBytesDownloadedMap) {
        long bytesDownloaded = 0;
        for (Map.Entry<DownloadFileId, Long> entry : fileBytesDownloadedMap.entrySet()) {
            bytesDownloaded += entry.getValue();
        }
        return bytesDownloaded;
    }

    private int getPercentatgeFrom(long bytesDownloaded, long totalFileSizeBytes) {
        return (int) ((((float) bytesDownloaded) / ((float) totalFileSizeBytes)) * 100);
    }

    public long bytesDownloaded() {
        return bytesDownloaded;
    }

    public int percentatgeDownloaded() {
        return percentatgeDownloaded;
    }
}
