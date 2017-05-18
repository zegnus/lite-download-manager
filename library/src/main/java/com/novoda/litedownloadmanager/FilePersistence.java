package com.novoda.litedownloadmanager;

interface FilePersistence {

    enum Status {
        SUCCESS,
        ERROR_UNKNOWN_TOTAL_FILE_SIZE,
        ERROR_INSUFFICIENT_SPACE,
        ERROR_EXTERNAL_STORAGE_NON_WRITABLE,
        ERROR_OPENING_FILE;

        public boolean isMarkedAsError() {
            return this != SUCCESS;
        }
    }

    DownloadError.Error convertError(Status status);

    Status create(FileName fileName, FileSize fileSize);

    boolean write(byte[] buffer, int i, int readLast);

    void delete();

    long getCurrentSize();

    void close();
}
