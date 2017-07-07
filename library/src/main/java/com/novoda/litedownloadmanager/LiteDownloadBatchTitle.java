package com.novoda.litedownloadmanager;

class LiteDownloadBatchTitle implements DownloadBatchTitle {

    private final String title;

    LiteDownloadBatchTitle(String title) {
        this.title = title;
    }

    @Override
    public String toString() {
        return title;
    }
}
