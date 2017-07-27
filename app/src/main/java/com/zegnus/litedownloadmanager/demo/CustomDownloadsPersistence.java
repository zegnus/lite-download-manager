package com.zegnus.litedownloadmanager.demo;

import com.novoda.notils.logger.simple.Log;
import com.zegnus.litedownloadmanager.DownloadBatchId;
import com.zegnus.litedownloadmanager.DownloadBatchStatus;
import com.zegnus.litedownloadmanager.DownloadsBatchPersisted;
import com.zegnus.litedownloadmanager.DownloadsFilePersisted;
import com.zegnus.litedownloadmanager.DownloadsPersistence;

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
    public void persistBatch(DownloadsBatchPersisted batchPersisted) {
        Log.v("Persist batch id: " + batchPersisted.downloadBatchId() + ", status: " + batchPersisted.downloadBatchStatus());
    }

    @Override
    public List<DownloadsBatchPersisted> loadBatches() {
        Log.v("Load batches");
        return Collections.emptyList();
    }

    @Override
    public void persistFile(DownloadsFilePersisted filePersisted) {
        Log.v("Persist file id: " + filePersisted.downloadFileId());
    }

    @Override
    public List<DownloadsFilePersisted> loadFiles(DownloadBatchId batchId) {
        Log.v("Load files for batch id: " + batchId);
        return Collections.emptyList();
    }

    @Override
    public void delete(DownloadBatchId downloadBatchId) {
        Log.v("Delete batch id: " + downloadBatchId.stringValue());
    }

    @Override
    public void update(DownloadBatchId downloadBatchId, DownloadBatchStatus.Status status) {
        Log.v("update batch id: " + downloadBatchId.stringValue() + " with status: " + status);
    }
}
