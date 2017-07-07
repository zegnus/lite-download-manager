package com.novoda.litedownloadmanager;

import android.content.Context;

public interface FilePersistence {

    void initialiseWith(Context context);

    FilePersistenceResult create(FileName fileName, FileSize fileSize);

    FilePersistenceResult create(FilePath filePath);

    boolean write(byte[] buffer, int offset, int numberOfBytesToWrite);

    void delete();

    long getCurrentSize();

    long getCurrentSize(FileName fileName);

    void close();

    FilePersistenceType getType();
}
