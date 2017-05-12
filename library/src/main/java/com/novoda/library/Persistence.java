package com.novoda.library;

import android.content.Context;

import java.io.IOException;

interface Persistence {

    enum Status {
        SUCCESS,
        ERROR_UNKNOWN_TOTAL_FILE_SIZE,
        ERROR_INSUFFICIENT_SPACE;

        public boolean isMarkedAsError() {
            return this == ERROR_INSUFFICIENT_SPACE || this == ERROR_UNKNOWN_TOTAL_FILE_SIZE;
        }

    }

    Status create(FileName fileName, Context context, FileSize fileSize);

    void write(byte[] buffer, int i, int readLast) throws IOException;

    long getCurrentSize();

    void close();
}
