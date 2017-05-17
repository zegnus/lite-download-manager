package com.novoda.library;

import android.content.Context;
import android.support.annotation.Nullable;

import com.novoda.notils.logger.simple.Log;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import static com.novoda.library.FilePersistence.Status.*;

class InternalPhysicalFilePersistence implements FilePersistence {

    private final Context context;

    @Nullable
    private FileOutputStream file;
    @Nullable
    private FileName fileName;

    InternalPhysicalFilePersistence(Context applicationContext) {
        this.context = applicationContext.getApplicationContext();
    }

    @Override
    public DownloadError.Error convertError(Status status) {
        switch (status) {

            case SUCCESS:
                Log.e("Cannot convert success status to any DownloadError type");
                break;
            case ERROR_UNKNOWN_TOTAL_FILE_SIZE:
                return DownloadError.Error.FILE_TOTAL_SIZE_REQUEST_FAILED;
            case ERROR_INSUFFICIENT_SPACE:
                return DownloadError.Error.FILE_CANNOT_BE_CREATED_LOCALLY_INSUFFICIENT_FREE_SPACE;
            case ERROR_EXTERNAL_STORAGE_NON_WRITABLE:
                return DownloadError.Error.STORAGE_UNAVAILABLE;
            case ERROR_OPENING_FILE:
                return DownloadError.Error.FILE_CANNOT_BE_WRITTEN;
            default:
                Log.e("Status " + status + " missing to be processed");
                break;
        }

        return DownloadError.Error.UNKNOWN;
    }

    @Override
    public Status create(FileName fileName, FileSize fileSize) {
        if (fileSize.isTotalSizeUnknown()) {
            return ERROR_UNKNOWN_TOTAL_FILE_SIZE;
        }

        long usableSpace = context.getFilesDir().getUsableSpace();
        if (usableSpace < fileSize.getTotalSize()) {
            return ERROR_INSUFFICIENT_SPACE;
        }

        try {
            this.fileName = fileName;
            this.file = context.openFileOutput(fileName.getName(), Context.MODE_APPEND);
        } catch (FileNotFoundException e) {
            Log.e(e, "File could not be opened");
            return ERROR_OPENING_FILE;
        }

        return SUCCESS;
    }

    @Override
    public boolean write(byte[] buffer, int offset, int numberOfBytesToWrite) {
        if (file == null) {
            Log.e("Cannot write, you must create the file first");
            return false;
        }

        try {
            file.write(buffer, offset, numberOfBytesToWrite);
            return true;
        } catch (IOException e) {
            Log.e(e, "Exception while writing to internal physical storage");
            return false;
        }
    }

    @Override
    public void delete() {
        if (fileName == null) {
            Log.e("Cannot delete, you must create the file first");
            return;
        }

        context.deleteFile(fileName.getName());
    }

    @Override
    public long getCurrentSize() {
        if (file == null) {
            Log.e("Cannot get the current file size, you must create the file first");
            return 0;
        }

        try {
            return file.getChannel().size();
        } catch (IOException e) {
            Log.e(e, "Error requesting file size, make sure you create one first");
            return 0;
        }
    }

    @Override
    public void close() {
        if (file == null) {
            return;
        }

        try {
            file.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
