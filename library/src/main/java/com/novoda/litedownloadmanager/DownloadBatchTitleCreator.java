package com.novoda.litedownloadmanager;

public class DownloadBatchTitleCreator {

    static DownloadBatchTitle createFrom(Batch batch) {
        return new LiteDownloadBatchTitle(batch.getTitle());
    }

    public static DownloadBatchTitle createFrom(String title) {
        return new LiteDownloadBatchTitle(title);
    }
}
