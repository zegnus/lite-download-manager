package com.novoda.library;

final class DownloadFileId {

    private final int id;

    public static DownloadFileId from(String url) {
        return new DownloadFileId(url.hashCode());
    }

    private DownloadFileId(int id) {
        this.id = id;
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

    String toRawId() {
        return String.valueOf(id);
    }

    @Override
    public int hashCode() {
        return id;
    }
}
