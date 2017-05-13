package com.novoda.library;

interface Persistence {

    enum Status {
        SUCCESS,
        ERROR_UNKNOWN_TOTAL_FILE_SIZE,
        ERROR_INSUFFICIENT_SPACE;

        public boolean isMarkedAsError() {
            return this == ERROR_INSUFFICIENT_SPACE || this == ERROR_UNKNOWN_TOTAL_FILE_SIZE;
        }

    }

    Status create(FileName fileName, FileSize fileSize);

    boolean write(byte[] buffer, int i, int readLast);

    void delete();

    long getCurrentSize();

    void close();
}
