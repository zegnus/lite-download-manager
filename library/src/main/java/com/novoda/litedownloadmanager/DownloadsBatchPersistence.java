package com.novoda.litedownloadmanager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executor;

class DownloadsBatchPersistence {

    private final Executor executor;
    private final DownloadsFilePersistence downloadsFilePersistence;
    private final DownloadsPersistence downloadsPersistence;

    DownloadsBatchPersistence(Executor executor, DownloadsFilePersistence downloadsFilePersistence, DownloadsPersistence downloadsPersistence) {
        this.executor = executor;
        this.downloadsFilePersistence = downloadsFilePersistence;
        this.downloadsPersistence = downloadsPersistence;
    }

    void persistAsync(final DownloadBatchId downloadBatchId, final DownloadBatchStatus.Status status, final List<DownloadFile> downloadFiles) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                downloadsPersistence.startTransaction();

                try {
                    DownloadsPersistence.BatchPersisted batchPersisted = new DownloadsPersistence.BatchPersisted(downloadBatchId, status);
                    downloadsPersistence.persistBatch(batchPersisted);

                    for (DownloadFile downloadFile : downloadFiles) {
                        downloadFile.persistSync(downloadBatchId);
                    }

                    downloadsPersistence.transactionSuccess();
                } finally {
                    downloadsPersistence.endTransaction();
                }
            }
        });
    }

    void loadAsync(final FileSizeRequester fileSizeRequester,
                   final PersistenceCreator persistenceCreator,
                   final Downloader downloader,
                   final LoadBatchesCallback callback) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                List<DownloadsPersistence.BatchPersisted> batchPersistedList = downloadsPersistence.loadBatches();

                List<DownloadBatch> downloadBatches = new ArrayList<>(batchPersistedList.size());
                for (DownloadsPersistence.BatchPersisted batchPersisted : batchPersistedList) {
                    DownloadBatchStatus.Status status = batchPersisted.getDownloadBatchStatus();
                    DownloadBatchId downloadBatchId = batchPersisted.getDownloadBatchId();
                    DownloadBatchStatus downloadBatchStatus = new DownloadBatchStatus(downloadBatchId, status);
                    FilePersistence filePersistence = persistenceCreator.create();

                    List<DownloadFile> downloadFiles = downloadsFilePersistence.loadSync(
                            downloadBatchId,
                            fileSizeRequester,
                            filePersistence,
                            downloader,
                            downloadsFilePersistence
                    );

                    DownloadBatch downloadBatch = new DownloadBatch(
                            downloadBatchId,
                            downloadFiles,
                            new HashMap<DownloadFileId, Long>(),
                            downloadBatchStatus,
                            DownloadsBatchPersistence.this
                    );

                    downloadBatches.add(downloadBatch);
                }

                callback.onLoaded(downloadBatches);
            }
        });
    }

    interface LoadBatchesCallback {

        void onLoaded(List<DownloadBatch> downloadBatches);
    }
}