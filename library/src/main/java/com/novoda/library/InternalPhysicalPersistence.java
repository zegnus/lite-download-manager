package com.novoda.library;

import android.content.Context;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import static com.novoda.library.Persistence.Status.*;

final class InternalPhysicalPersistence implements Persistence {

    private FileOutputStream file;

    @Override
    public Status create(FileName fileName, Context context, FileSize fileSize) {
        if (fileSize.isTotalSizeUnknown()) {
            return ERROR_UNKNOWN_TOTAL_FILE_SIZE;
        }

        long usableSpace = context.getFilesDir().getUsableSpace();
        if (usableSpace < fileSize.getTotalSize()) {
            return ERROR_INSUFFICIENT_SPACE;
        }

        try {
            file = context.getApplicationContext().openFileOutput(fileName.getName(), Context.MODE_APPEND);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return SUCCESS;
    }

    @Override
    public void write(byte[] buffer, int offset, int numberOfBytesToWrite) throws IOException {
        file.write(buffer, offset, numberOfBytesToWrite);
    }

    @Override
    public long getCurrentSize() {
        try {
            return file.getChannel().size();
        } catch (IOException e) {
            e.printStackTrace();
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
