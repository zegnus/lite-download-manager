package com.novoda.litedownloadmanager.demo;

import com.novoda.litedownloadmanager.DownloadBatchId;
import com.novoda.litedownloadmanager.DownloadBatchStatus;
import com.novoda.litedownloadmanager.DownloadsPersistence;
import com.novoda.notils.logger.simple.Log;

import java.util.Collections;
import java.util.List;

public class CustomDownloadsPersistence implements DownloadsPersistence {

    @Override
    public void startTransaction() {
        Log.v("Start Transaction");
    }

    @Override
    public void endTransaction() {
        Log.v("End Transaction");
    }

    @Override
    public void transactionSuccess() {
        Log.v("Transaction success");
    }

    @Override
    public void persistBatch(BatchPersisted batchPersisted) {
        Log.v("Persist batch id: " + batchPersisted.getDownloadBatchId() + ", status: " + batchPersisted.getDownloadBatchStatus());
    }

    @Override
    public List<BatchPersisted> loadBatches() {
        Log.v("Load batches");
        return Collections.emptyList();
    }

    @Override
    public void persistFile(FilePersisted filePersisted) {
        Log.v("Persist file id: " + filePersisted.getDownloadFileId());
    }

    @Override
    public List<FilePersisted> loadFiles(DownloadBatchId batchId) {
        Log.v("Load files for batch id: " + batchId);
        return Collections.emptyList();
    }

    @Override
    public void delete(DownloadBatchId downloadBatchId) {
        Log.v("Delete batch id: " + downloadBatchId.getId());
    }

    @Override
    public void update(DownloadBatchId downloadBatchId, DownloadBatchStatus.Status status) {
        Log.v("update batch id: " + downloadBatchId.getId() + " with status: " + status);
    }
}
