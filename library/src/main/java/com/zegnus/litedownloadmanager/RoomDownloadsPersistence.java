package com.zegnus.litedownloadmanager;

import androidx.room.Room;
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
    public void persistBatch(final DownloadsBatchPersisted batchPersisted) {
        RoomBatch roomBatch = new RoomBatch();
        roomBatch.id = batchPersisted.downloadBatchId().stringValue();
        roomBatch.status = batchPersisted.downloadBatchStatus().toRawValue();
        roomBatch.title = batchPersisted.downloadBatchTitle().asString();

        database.roomBatchDao().insert(roomBatch);
    }

    @Override
    public List<DownloadsBatchPersisted> loadBatches() {
        List<RoomBatch> roomBatches = database.roomBatchDao().loadAll();

        List<DownloadsBatchPersisted> batchPersistedList = new ArrayList<>(roomBatches.size());
        for (RoomBatch roomBatch : roomBatches) {
            DownloadsBatchPersisted batchPersisted = new LiteDownloadsBatchPersisted(
                    DownloadBatchTitleCreator.createFrom(roomBatch.title),
                    DownloadBatchIdCreator.createFrom(roomBatch.id),
                    DownloadBatchStatus.Status.from(roomBatch.status)
            );
            batchPersistedList.add(batchPersisted);
        }

        return batchPersistedList;
    }

    @Override
    public void persistFile(DownloadsFilePersisted filePersisted) {
        RoomFile roomFile = new RoomFile();
        roomFile.totalSize = filePersisted.totalFileSize();
        roomFile.batchId = filePersisted.downloadBatchId().stringValue();
        roomFile.url = filePersisted.url();
        roomFile.name = filePersisted.fileName().name();
        roomFile.path = filePersisted.filePath().path();
        roomFile.id = filePersisted.downloadFileId().toRawId();
        roomFile.persistenceType = filePersisted.filePersistenceType().toRawValue();

        database.roomFileDao().insert(roomFile);
    }

    @Override
    public List<DownloadsFilePersisted> loadFiles(DownloadBatchId downloadBatchId) {
        List<RoomFile> roomFiles = database.roomFileDao().loadAllFilesFor(downloadBatchId.stringValue());
        List<DownloadsFilePersisted> filePersistedList = new ArrayList<>(roomFiles.size());
        for (RoomFile roomFile : roomFiles) {
            DownloadsFilePersisted filePersisted = new LiteDownloadsFilePersisted(
                    DownloadBatchIdCreator.createFrom(roomFile.batchId),
                    DownloadFileId.from(roomFile.id),
                    LiteFileName.from(roomFile.name),
                    FilePathCreator.create(roomFile.path),
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
