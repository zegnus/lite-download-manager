package com.novoda.litedownloadmanager.demo;

import android.content.Context;

import com.novoda.litedownloadmanager.FileName;
import com.novoda.litedownloadmanager.FilePath;
import com.novoda.litedownloadmanager.FilePersistence;
import com.novoda.litedownloadmanager.FilePersistenceResult;
import com.novoda.litedownloadmanager.FilePersistenceType;
import com.novoda.litedownloadmanager.FileSize;
import com.novoda.notils.logger.simple.Log;

public class CustomFilePersistence implements FilePersistence {

    private int currentSize = 0;

    @Override
    public void initialiseWith(Context context) {
        Log.v("initialise");
    }

    @Override
    public FilePersistenceResult create(FileName fileName, FileSize fileSize) {
        Log.v("create " + fileName.toString() + ", " + fileSize.toString());
        return FilePersistenceResult.newInstance(FilePersistenceResult.Status.SUCCESS);
    }

    @Override
    public FilePersistenceResult create(FilePath filePath) {
        Log.v("create " + filePath.toString());
        return FilePersistenceResult.newInstance(FilePersistenceResult.Status.SUCCESS);
    }

    @Override
    public boolean write(byte[] buffer, int offset, int numberOfBytesToWrite) {
        Log.v("write offset: " + offset + ", numberOfBytesToWrite: " + numberOfBytesToWrite);
        currentSize =+ numberOfBytesToWrite;
        return true;
    }

    @Override
    public void delete() {
        Log.v("delete");
    }

    @Override
    public long getCurrentSize() {
        Log.v("getCurrentSize: " + currentSize);
        return currentSize;
    }

    @Override
    public long getCurrentSize(FileName fileName) {
        Log.v("getCurrentSize for " + fileName + ": " + currentSize);
        return 0;
    }

    @Override
    public void close() {
        Log.v("close");
    }

    @Override
    public FilePersistenceType getType() {
        return FilePersistenceType.CUSTOM;
    }
}
