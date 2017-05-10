package com.novoda.library;

class DownloadError {

    private static final int UNKNOWN_ERROR_CODE = -1;

    enum Error {
        FILE_TOTAL_SIZE_REQUEST_FAILED,
        FILE_CANNOT_BE_CREATED_LOCALLY,
        CONNECTION_FAILURE,
        CANNOT_READ_FROM_STREAM,
        CANNOT_WRITE,
        CANNOT_DOWNLOAD_FILE_FROM_NETWORK,
        UNKNOWN
    }

    private Error error;
    private int errorCode;

    DownloadError() {
        this.error = Error.UNKNOWN;
        this.errorCode = UNKNOWN_ERROR_CODE;
    }

    void setError(Error error) {
        this.error = error;
    }

    Error getError() {
        return error;
    }

    void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    int getErrorCode() {
        return errorCode;
    }
}
