package com.novoda.litedownloadmanager;

public class FileSize {

    private static final int ZERO_BYTES = 0;

    private long currentSize;
    private long totalSize;

    static FileSize Unknown() {
        return new FileSize(ZERO_BYTES, ZERO_BYTES);
    }

    public static FileSize Total(long totalFileSize) {
        return new FileSize(ZERO_BYTES, totalFileSize);
    }

    FileSize(long currentSize, long totalSize) {
        this.currentSize = currentSize;
        this.totalSize = totalSize;
    }

    boolean isTotalSizeUnknown() {
        return totalSize <= ZERO_BYTES;
    }

    boolean isTotalSizeKnown() {
        return totalSize > ZERO_BYTES;
    }

    boolean areBytesDownloadedKnown() {
        return currentSize > ZERO_BYTES;
    }

    public long getCurrentSize() {
        return currentSize;
    }

    public long getTotalSize() {
        return totalSize;
    }

    void addToCurrentSize(long newBytes) {
        currentSize += newBytes;
    }

    void setTotalSize(long totalSize) {
        this.totalSize = totalSize;
    }

    void setCurrentSize(long currentSize) {
        this.currentSize = currentSize;
    }

    FileSize copy() {
        return new FileSize(currentSize, totalSize);
    }

    @Override
    public String toString() {
        return "FileSize{" +
                "currentSize=" + currentSize +
                ", totalSize=" + totalSize +
                '}';
    }
}
