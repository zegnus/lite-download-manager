package com.novoda.litedownloadmanager;

public class DownloadError {

    public enum Error {
        FILE_TOTAL_SIZE_REQUEST_FAILED,
        FILE_CANNOT_BE_CREATED_LOCALLY_INSUFFICIENT_FREE_SPACE,
        FILE_CANNOT_BE_WRITTEN,
        STORAGE_UNAVAILABLE,
        CANNOT_DOWNLOAD_FILE,
        UNKNOWN
    }

    private Error error;

    DownloadError() {
        this.error = Error.UNKNOWN;
    }

    void setError(Error error) {
        this.error = error;
    }

    Error getError() {
        return error;
    }
}
