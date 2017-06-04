package com.novoda.litedownloadmanager.demo;

import com.novoda.litedownloadmanager.FileSize;
import com.novoda.litedownloadmanager.FileSizeRequester;

class CustomFileSizeRequester implements FileSizeRequester {

    private static final long FILE_TOTAL_SIZE = 1000;


    @Override
    public FileSize requestFileSize(String url) {
        return FileSize.Total(FILE_TOTAL_SIZE);
    }
}
