package com.novoda.library;

import android.content.Context;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;

import com.novoda.notils.logger.simple.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import static com.novoda.library.DownloadError.Error.*;
import static com.novoda.library.Persistence.Status.*;

class ExternalPhysicalPersistence implements Persistence {

    private static final String UNDEFINED_DIRECTORY_TYPE = null;
    private static final boolean APPEND = true;
    private final Context context;

    @Nullable
    private FileOutputStream fileOutputStream;
    @Nullable
    private File file;

    ExternalPhysicalPersistence(Context applicationContext) {
        this.context = applicationContext.getApplicationContext();
    }

    @Override
    public DownloadError.Error convertError(Status status) {
        switch (status) {
            case SUCCESS:
                Log.e("Cannot convert success status to any DownloadError type");
                break;
            case ERROR_UNKNOWN_TOTAL_FILE_SIZE:
                return FILE_TOTAL_SIZE_REQUEST_FAILED;
            case ERROR_INSUFFICIENT_SPACE:
                return FILE_CANNOT_BE_CREATED_LOCALLY_INSUFFICIENT_FREE_SPACE;
            case ERROR_EXTERNAL_STORAGE_NON_WRITABLE:
                return STORAGE_UNAVAILABLE;
            case ERROR_OPENING_FILE:
                return FILE_CANNOT_BE_WRITTEN;
            default:
                Log.e("Status " + status + " missing to be processed");
                break;
        }

        return UNKNOWN;
    }

    @Override
    public Status create(FileName fileName, FileSize fileSize) {
        if (fileSize.isTotalSizeUnknown()) {
            return ERROR_UNKNOWN_TOTAL_FILE_SIZE;
        }

        if (!isExternalStorageWritable()) {
            return ERROR_EXTERNAL_STORAGE_NON_WRITABLE;
        }

        File externalFileDir = getExternalFileDirWithBiggerAvailableSpace();

        long usableSpace = externalFileDir.getUsableSpace();
        if (usableSpace < fileSize.getTotalSize()) {
            return ERROR_INSUFFICIENT_SPACE;
        }

        String absolutePath = externalFileDir.getAbsolutePath() + File.separatorChar + fileName.getName();

        try {
            this.file = new File(absolutePath);
            this.fileOutputStream = new FileOutputStream(absolutePath, APPEND);
        } catch (FileNotFoundException e) {
            Log.e(e, "File could not be opened");
            return ERROR_OPENING_FILE;
        }

        return SUCCESS;
    }

    private boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    private File getExternalFileDirWithBiggerAvailableSpace() {
        File externalFileDir = null;
        long usableSpace = 0;

        File[] externalFilesDirs = ContextCompat.getExternalFilesDirs(context, UNDEFINED_DIRECTORY_TYPE);
        for (File dir : externalFilesDirs) {
            if (dir == null) {
                continue;
            }

            long localUsableSpace = dir.getUsableSpace();
            if (usableSpace < localUsableSpace) {
                externalFileDir = dir;
                usableSpace = localUsableSpace;
            }
        }

        return externalFileDir;
    }

    @Override
    public boolean write(byte[] buffer, int offset, int numberOfBytesToWrite) {
        if (fileOutputStream == null) {
            Log.e("Cannot write, you must create the file first");
            return false;
        }

        try {
            fileOutputStream.write(buffer, offset, numberOfBytesToWrite);
            return true;
        } catch (IOException e) {
            Log.e(e, "Exception while writing to internal physical storage");
            return false;
        }
    }

    @Override
    public void delete() {
        if (file == null) {
            Log.e("Cannot delete, you must create the file first");
            return;
        }

        boolean success = file.delete();
        Log.v("File " + file.getAbsolutePath() + " deleted successfully: " + success);
    }

    @Override
    public long getCurrentSize() {
        if (fileOutputStream == null) {
            Log.e("Cannot get the current file size, you must create the file first");
            return 0;
        }

        try {
            return fileOutputStream.getChannel().size();
        } catch (IOException e) {
            Log.e(e, "Error requesting file size, make sure you create one first");
            return 0;
        }
    }

    @Override
    public void close() {
        if (fileOutputStream == null) {
            return;
        }

        try {
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
