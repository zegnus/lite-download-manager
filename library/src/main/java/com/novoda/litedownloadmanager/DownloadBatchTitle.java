package com.novoda.litedownloadmanager;

class DownloadBatchTitle {

    private final String title;

    static DownloadBatchTitle from(Batch batch) {
        return new DownloadBatchTitle(batch.getTitle());
    }

    public static DownloadBatchTitle from(String title) {
        return new DownloadBatchTitle(title);
    }

    DownloadBatchTitle(String title) {
        this.title = title;
    }

    public String toString() {
        return title;
    }
}
