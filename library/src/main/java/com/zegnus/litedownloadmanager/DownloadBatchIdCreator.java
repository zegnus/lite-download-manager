package com.zegnus.litedownloadmanager;

public final class DownloadBatchIdCreator {

    private DownloadBatchIdCreator() {
        // non-instantiable class
    }

    public static DownloadBatchId createFrom(String id) {
        return new LiteDownloadBatchId(id);
    }
}
