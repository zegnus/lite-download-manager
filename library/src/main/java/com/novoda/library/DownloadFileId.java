package com.novoda.library;

public class DownloadFileId {

    private final int id;

    public DownloadFileId(int id) {

        this.id = id;
    }

    public static DownloadFileId from(String url) {
        return new DownloadFileId(url.hashCode());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DownloadFileId)) {
            return false;
        }

        DownloadFileId that = (DownloadFileId) o;

        return id == that.id;

    }

    @Override
    public int hashCode() {
        return id;
    }
}
