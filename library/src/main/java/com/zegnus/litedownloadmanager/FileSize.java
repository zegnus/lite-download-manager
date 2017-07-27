package com.zegnus.litedownloadmanager;

public interface FileSize {

    long currentSize();

    long totalSize();

    boolean isTotalSizeKnown();

    boolean isTotalSizeUnknown();

    boolean areBytesDownloadedKnown();
}
