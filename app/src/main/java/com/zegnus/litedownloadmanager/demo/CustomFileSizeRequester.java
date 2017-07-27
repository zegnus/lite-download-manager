package com.zegnus.litedownloadmanager.demo;

import com.zegnus.litedownloadmanager.FileSize;
import com.zegnus.litedownloadmanager.FileSizeCreator;
import com.zegnus.litedownloadmanager.FileSizeRequester;

class CustomFileSizeRequester implements FileSizeRequester {

    private static final long FILE_TOTAL_SIZE = 1000;

    @Override
    public FileSize requestFileSize(String url) {
        return FileSizeCreator.createFromTotalSize(FILE_TOTAL_SIZE);
    }
}
