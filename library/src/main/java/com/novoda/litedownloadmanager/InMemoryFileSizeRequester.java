package com.novoda.litedownloadmanager;

class InMemoryFileSizeRequester implements FileSizeRequester {

    private static final long TOTAL_FILE_SIZE = 5000000;

    @Override
    public FileSize requestFileSize(String url) {
        return FileSize.Total(TOTAL_FILE_SIZE);
    }
}
