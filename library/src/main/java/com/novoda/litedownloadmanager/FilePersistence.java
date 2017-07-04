package com.novoda.litedownloadmanager;

import android.content.Context;

public interface FilePersistence {

    void initialiseWith(Context context);

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

    Status create(FileName fileName, FileSize fileSize);

    boolean write(byte[] buffer, int offset, int numberOfBytesToWrite);

    void delete();

    long getCurrentSize();

    long getCurrentSize(FileName fileName);

    void close();

    FilePersistenceType getType();
}
