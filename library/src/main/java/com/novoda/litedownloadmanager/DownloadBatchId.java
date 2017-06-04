package com.novoda.litedownloadmanager;

public final class DownloadBatchId {

    private final int id;

    public static DownloadBatchId from(Batch batch) {
        String title = batch.getTitle();
        int id = title.hashCode();
        return new DownloadBatchId(id);
    }

    public static DownloadBatchId from(int id) {
        return new DownloadBatchId(id);
    }

    private DownloadBatchId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DownloadBatchId)) {
            return false;
        }

        DownloadBatchId that = (DownloadBatchId) o;

        return id == that.id;

    }

    @Override
    public int hashCode() {
        return id;
    }
}
