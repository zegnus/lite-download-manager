package com.novoda.library;

public class DownloadBatchId {

    private final String id;

    public DownloadBatchId(String id) {
        this.id = id;
    }

    public static DownloadBatchId from(String id) {
        return new DownloadBatchId(id);
    }

    public String getId() {
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

        return id != null ? id.equals(that.id) : that.id == null;

    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
