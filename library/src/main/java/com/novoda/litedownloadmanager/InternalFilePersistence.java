package com.novoda.litedownloadmanager;

import android.content.Context;
import android.support.annotation.Nullable;

import com.novoda.notils.logger.simple.Log;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import static com.novoda.litedownloadmanager.FilePersistenceResult.Status.*;

class InternalFilePersistence implements FilePersistence {

    private Context context;

    @Nullable
    private FileOutputStream file;
    @Nullable
    private FileName fileName;

    @Override
    public void initialiseWith(Context context) {
        this.context = context.getApplicationContext();
    }

    @Override
    public FilePersistenceResult create(FileName fileName, FileSize fileSize) {
        if (fileSize.isTotalSizeUnknown()) {
            return FilePersistenceResult.newInstance(ERROR_UNKNOWN_TOTAL_FILE_SIZE);
        }

        long usableSpace = context.getFilesDir().getUsableSpace();
        if (usableSpace < fileSize.getTotalSize()) {
            return FilePersistenceResult.newInstance(ERROR_INSUFFICIENT_SPACE);
        }

        FilePath filePath = FilePath.newInstance(fileName.getName());
        return create(filePath);
    }

    @Override
    public FilePersistenceResult create(FilePath filePath) {
        try {
            file = context.openFileOutput(filePath.path(), Context.MODE_APPEND);
        } catch (FileNotFoundException e) {
            Log.e(e, "File could not be opened");
            return FilePersistenceResult.newInstance(ERROR_OPENING_FILE);
        }

        fileName = FileName.from(filePath.path());
        return FilePersistenceResult.newInstance(SUCCESS, filePath);
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
            Log.w("Cannot delete, you must create the file first");
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
    public long getCurrentSize(FileName fileName) {
        FileOutputStream file = null;
        try {
            file = context.openFileOutput(fileName.getName(), Context.MODE_APPEND);
            return file.getChannel().size();
        } catch (IOException e) {
            Log.e(e, "Error requesting file size for " + fileName);
            return 0;
        } finally {
            if (file != null) {
                try {
                    file.close();
                } catch (IOException e) {
                    Log.e(e, "Error requesting file size for " + fileName);
                }
            }
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

    @Override
    public FilePersistenceType getType() {
        return FilePersistenceType.INTERNAL;
    }
}
