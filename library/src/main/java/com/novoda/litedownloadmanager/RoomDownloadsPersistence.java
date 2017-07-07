package com.novoda.litedownloadmanager;

import android.arch.persistence.room.Room;
import android.content.Context;

import java.util.ArrayList;
import java.util.List;

class RoomDownloadsPersistence implements DownloadsPersistence {

    private final RoomAppDatabase database;

    static RoomDownloadsPersistence newInstance(Context context) {
        RoomAppDatabase database = Room.databaseBuilder(
                context.getApplicationContext(),
                RoomAppDatabase.class,
                "database-litedownloadmanager"
        ).build();
        return new RoomDownloadsPersistence(database);
    }

    RoomDownloadsPersistence(RoomAppDatabase database) {
        this.database = database;
    }

    @Override
    public void startTransaction() {
        database.beginTransaction();
    }

    @Override
    public void endTransaction() {
        database.endTransaction();
    }

    @Override
    public void transactionSuccess() {
        database.setTransactionSuccessful();
    }

    @Override
    public void persistBatch(final BatchPersisted batchPersisted) {
        RoomBatch roomBatch = new RoomBatch();
        roomBatch.id = batchPersisted.getDownloadBatchId().stringValue();
        roomBatch.status = batchPersisted.getDownloadBatchStatus().toRawValue();
        roomBatch.title = batchPersisted.getDownloadBatchTitle().toString();

        database.roomBatchDao().insert(roomBatch);
    }

    @Override
    public List<BatchPersisted> loadBatches() {
        List<RoomBatch> roomBatches = database.roomBatchDao().loadAll();

        List<BatchPersisted> batchPersistedList = new ArrayList<>(roomBatches.size());
        for (RoomBatch roomBatch : roomBatches) {
            BatchPersisted batchPersisted = new BatchPersisted(
                    DownloadBatchTitleCreator.createFrom(roomBatch.title),
                    DownloadBatchIdCreator.createFrom(roomBatch.id),
                    DownloadBatchStatus.Status.from(roomBatch.status)
            );
            batchPersistedList.add(batchPersisted);
        }

        return batchPersistedList;
    }

    @Override
    public void persistFile(FilePersisted filePersisted) {
        RoomFile roomFile = new RoomFile();
        roomFile.totalSize = filePersisted.totalFileSize();
        roomFile.batchId = filePersisted.downloadBatchId().stringValue();
        roomFile.url = filePersisted.url();
        roomFile.name = filePersisted.fileName().getName();
        roomFile.path = filePersisted.filePath().path();
        roomFile.path = filePersisted.filePath().path();
        roomFile.id = filePersisted.downloadFileId().toRawId();
        roomFile.persistenceType = filePersisted.filePersistenceType().toRawValue();

        database.roomFileDao().insert(roomFile);
    }

    @Override
    public List<FilePersisted> loadFiles(DownloadBatchId downloadBatchId) {
        List<RoomFile> roomFiles = database.roomFileDao().loadAllFilesFor(downloadBatchId.stringValue());
        List<FilePersisted> filePersistedList = new ArrayList<>(roomFiles.size());
        for (RoomFile roomFile : roomFiles) {
            FilePersisted filePersisted = new FilePersisted(
                    DownloadBatchIdCreator.createFrom(roomFile.batchId),
                    DownloadFileId.from(roomFile.id),
                    FileName.from(roomFile.name),
                    FilePath.newInstance(roomFile.path),
                    roomFile.totalSize,
                    roomFile.url,
                    FilePersistenceType.from(roomFile.persistenceType)
            );
            filePersistedList.add(filePersisted);
        }

        return filePersistedList;
    }

    @Override
    public void delete(DownloadBatchId downloadBatchId) {
        RoomBatch roomBatch = database.roomBatchDao().load(downloadBatchId.stringValue());
        database.roomBatchDao().delete(roomBatch);
    }

    @Override
    public void update(DownloadBatchId downloadBatchId, DownloadBatchStatus.Status status) {
        RoomBatch roomBatch = database.roomBatchDao().load(downloadBatchId.stringValue());
        roomBatch.status = status.toRawValue();
        database.roomBatchDao().update(roomBatch);
    }
}
