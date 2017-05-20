package com.novoda.litedownloadmanager;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.OperationApplicationException;
import android.net.Uri;
import android.os.RemoteException;

import com.novoda.notils.logger.simple.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import novoda.lib.sqliteprovider.provider.SQLiteContentProviderImpl;

final class DatabaseDownloadPersistence implements DownloadsPersistence {

    private final List<ContentProviderOperation> operations = new ArrayList<>();
    private final ContentResolver contentResolver;
    private final DatabaseDownloadPersistenceBatch downloadPersistenceBatch;
    private final DatabaseDownloadPersistenceFile databaseDownloadPersistenceFile;

    static DownloadsPersistence newInstance(ContentResolver contentResolver) {
        DatabaseDownloadPersistenceBatch databaseDownloadPersistenceBatch = new DatabaseDownloadPersistenceBatch();
        DatabaseDownloadPersistenceFile databaseDownloadPersistenceFile = new DatabaseDownloadPersistenceFile();
        return new DatabaseDownloadPersistence(contentResolver, databaseDownloadPersistenceBatch, databaseDownloadPersistenceFile);
    }

    private DatabaseDownloadPersistence(ContentResolver contentResolver,
                                        DatabaseDownloadPersistenceBatch downloadPersistenceBatch,
                                        DatabaseDownloadPersistenceFile databaseDownloadPersistenceFile) {
        this.contentResolver = contentResolver;
        this.downloadPersistenceBatch = downloadPersistenceBatch;
        this.databaseDownloadPersistenceFile = databaseDownloadPersistenceFile;
    }

    @Override
    public void startTransaction() {
        operations.clear();
    }

    @Override
    public void endAndExecuteTransaction() {
        try {
            contentResolver.applyBatch(ContentProvider.AUTHORITY, new ArrayList<>(operations));
            operations.clear();
        } catch (RemoteException | OperationApplicationException | IllegalArgumentException e ) {
            Log.e(e, "Unable to execute database operations");
        }
    }

    @Override
    public void persistBatch(BatchPersisted batchPersisted) {
        ContentProviderOperation operation = downloadPersistenceBatch.createInsertOperationFor(batchPersisted);
        operations.add(operation);
    }

    @Override
    public List<BatchPersisted> loadBatches() {
        return Collections.emptyList();
    }

    @Override
    public void persistFile(FilePersisted filePersisted) {
        ContentProviderOperation operation = databaseDownloadPersistenceFile.createInsertOperationFor(filePersisted);
        operations.add(operation);
    }

    @Override
    public List<FilePersisted> loadFiles(DownloadBatchId batchId) {
        return Collections.emptyList();
    }

    public static class ContentProvider extends SQLiteContentProviderImpl {
        private static final String AUTHORITY = "com.novoda.litedownloadmanager";
        private static final String URI_AUTHORITY = "content://com.novoda.litedownloadmanager/";
    }

    private static class DatabaseDownloadPersistenceBatch {

        ContentProviderOperation createInsertOperationFor(BatchPersisted batchPersisted) {
            ContentValues values = new ContentValues();
            DB.Batches.setBatchId(batchPersisted.getDownloadBatchId().getId(), values);
            DB.Batches.setStatus(batchPersisted.getDownloadBatchStatus().toRawValue(), values);

            Uri uri = Uri.parse(ContentProvider.URI_AUTHORITY)
                    .buildUpon()
                    .appendPath(DB.Tables.Batches)
                    .build();

            return ContentProviderOperation.newInsert(uri)
                    .withValues(values)
                    .build();
        }
    }

    private static class DatabaseDownloadPersistenceFile {
        ContentProviderOperation createInsertOperationFor(FilePersisted filePersisted) {
            ContentValues values = new ContentValues();
            DB.Files.setStatus(filePersisted.getStatus().toRawValue(), values);
            DB.Files.setBatchId(filePersisted.getDownloadBatchId().getId(), values);
            DB.Files.setTotalSize(String.valueOf(filePersisted.getFileSize().getTotalSize()), values);
            DB.Files.setCurrentSize(String.valueOf(filePersisted.getFileSize().getCurrentSize()), values);
            DB.Files.setFileId(filePersisted.getDownloadFileId().toRawId(), values);
            DB.Files.setFilename(filePersisted.getFileName().getName(), values);
            DB.Files.setUrl(filePersisted.getUrl(), values);

            Uri uri = Uri.parse(ContentProvider.URI_AUTHORITY)
                    .buildUpon()
                    .appendPath(DB.Tables.Files)
                    .build();

            return ContentProviderOperation.newInsert(uri)
                    .withValues(values)
                    .build();
        }
    }

}
