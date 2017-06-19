package com.novoda.litedownloadmanager;

public final class DownloadBatchId {

    private final String id;

    public static DownloadBatchId from(String id) {
        return new DownloadBatchId(id);
    }

    private DownloadBatchId(String id) {
        this.id = id;
    }

    public String stringValue() {
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

        return id.equals(that.id);

    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
