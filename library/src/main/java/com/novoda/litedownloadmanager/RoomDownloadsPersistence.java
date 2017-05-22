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
        roomBatch.downloadBatchId = batchPersisted.getDownloadBatchId().getId();
        roomBatch.status = batchPersisted.getDownloadBatchStatus().toRawValue();

        database.roomBatchDao().insert(roomBatch);
    }

    @Override
    public List<BatchPersisted> loadBatches() {
        List<RoomBatch> roomBatches = database.roomBatchDao().loadAll();

        List<BatchPersisted> batchPersistedList = new ArrayList<>(roomBatches.size());
        for (RoomBatch roomBatch : roomBatches) {
            BatchPersisted batchPersisted = new BatchPersisted(
                    DownloadBatchId.from(roomBatch.downloadBatchId),
                    DownloadBatchStatus.Status.from(roomBatch.status)
            );
            batchPersistedList.add(batchPersisted);
        }

        return batchPersistedList;
    }

    @Override
    public void persistFile(FilePersisted filePersisted) {
        RoomFile roomFile = new RoomFile();
        roomFile.currentSize = filePersisted.getFileSize().getCurrentSize();
        roomFile.totalSize = filePersisted.getFileSize().getTotalSize();
        roomFile.batchId = filePersisted.getDownloadBatchId().getId();
        roomFile.url = filePersisted.getUrl();
        roomFile.name = filePersisted.getFileName().getName();
        roomFile.fileId = filePersisted.getDownloadFileId().toRawId();
        roomFile.status = filePersisted.getStatus().toRawValue();

        database.roomFileDao().insert(roomFile);
    }

    @Override
    public List<FilePersisted> loadFiles(DownloadBatchId batchId) {
        List<RoomFile> roomFiles = database.roomFileDao().loadAllFilesFor(batchId.getId());
        List<FilePersisted> filePersistedList = new ArrayList<>(roomFiles.size());
        for (RoomFile roomFile : roomFiles) {
            FilePersisted filePersisted = new FilePersisted(
                    DownloadBatchId.from(roomFile.batchId),
                    DownloadFileId.from(roomFile.fileId),
                    new FileName(roomFile.name),
                    new FileSize(roomFile.currentSize, roomFile.totalSize),
                    roomFile.url,
                    DownloadFileStatus.Status.from(roomFile.status)
            );
            filePersistedList.add(filePersisted);
        }

        return filePersistedList;
    }
}
