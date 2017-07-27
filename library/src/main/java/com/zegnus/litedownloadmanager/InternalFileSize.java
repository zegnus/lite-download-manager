package com.zegnus.litedownloadmanager;

interface InternalFileSize extends FileSize {

    void addToCurrentSize(long newBytes);

    void setTotalSize(long totalSize);

    void setCurrentSize(long currentSize);

    LiteFileSize copy();
}
