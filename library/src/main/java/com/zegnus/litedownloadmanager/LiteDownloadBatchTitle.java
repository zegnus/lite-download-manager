package com.zegnus.litedownloadmanager;

class LiteDownloadBatchTitle implements DownloadBatchTitle {

    private final String title;

    LiteDownloadBatchTitle(String title) {
        this.title = title;
    }

    @Override
    public String asString() {
        return title;
    }

    @Override
    public String toString() {
        return "LiteDownloadBatchTitle{" +
                "title='" + title + '\'' +
                '}';
    }
}
