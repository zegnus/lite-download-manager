package com.novoda.library;

class FileSize {

    private static final int UNKNOWN_VALUE = -1;

    private long currentSize;
    private long totalSize;

    static FileSize Unknown() {
        return new FileSize(UNKNOWN_VALUE, UNKNOWN_VALUE);
    }

    static FileSize Total(long totalFileSize) {
        return new FileSize(UNKNOWN_VALUE, totalFileSize);
    }

    private FileSize(long currentSize, long totalSize) {
        this.currentSize = currentSize;
        this.totalSize = totalSize;
    }

    boolean isTotalSizeUnknown() {
        return totalSize == UNKNOWN_VALUE;
    }

    boolean areBytesDownloadedKnown() {
        return currentSize != UNKNOWN_VALUE;
    }

    long getCurrentSize() {
        return currentSize;
    }

    long getTotalSize() {
        return totalSize;
    }

    void addToCurrentSize(long newBytes) {
        currentSize += newBytes;
    }
}
