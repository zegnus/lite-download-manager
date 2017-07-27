package com.zegnus.litedownloadmanager;

import java.util.List;

public interface DownloadsPersistence {

    void startTransaction();

    void endTransaction();

    void transactionSuccess();

    void persistBatch(DownloadsBatchPersisted batchPersisted);

    List<DownloadsBatchPersisted> loadBatches();

    void persistFile(DownloadsFilePersisted filePersisted);

    List<DownloadsFilePersisted> loadFiles(DownloadBatchId batchId);

    void delete(DownloadBatchId downloadBatchId);

    void update(DownloadBatchId downloadBatchId, DownloadBatchStatus.Status status);

}
