package com.novoda.library;

import java.util.List;

class DatabaseDownloadPersistence implements DownloadsPersistence {

    @Override
    public void startTransaction() {

    }

    @Override
    public void endAndExecuteTransaction() {

    }

    @Override
    public void persistBatch(BatchPersisted batchPersisted) {

    }

    @Override
    public List<BatchPersisted> loadBatches() {
        return null;
    }

    @Override
    public void persistFile(FilePersisted filePersisted) {

    }

    @Override
    public List<FilePersisted> loadFiles(DownloadBatchId batchId) {
        return null;
    }
}
