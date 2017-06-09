package com.novoda.litedownloadmanager;

import java.util.ArrayList;
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

    void persistAsync(final DownloadBatchTitle downloadBatchTitle,
                      final DownloadBatchId downloadBatchId,
                      final DownloadBatchStatus.Status status,
                      final List<DownloadFile> downloadFiles) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                downloadsPersistence.startTransaction();

                try {
                    DownloadsPersistence.BatchPersisted batchPersisted = new DownloadsPersistence.BatchPersisted(
                            downloadBatchTitle,
                            downloadBatchId,
                            status
                    );
                    downloadsPersistence.persistBatch(batchPersisted);

                    for (DownloadFile downloadFile : downloadFiles) {
                        downloadFile.persistSync();
                    }

                    downloadsPersistence.transactionSuccess();
                } finally {
                    downloadsPersistence.endTransaction();
                }
            }
        });
    }

    void loadAsync(final FileSizeRequester fileSizeRequester,
                   final FilePersistenceCreator filePersistenceCreator,
                   final FileDownloader fileDownloader,
                   final DownloadsBatchPersistence downloadsBatchPersistence,
                   final NotificationCreator notificationCreator,
                   final LoadBatchesCallback callback) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                List<DownloadsPersistence.BatchPersisted> batchPersistedList = downloadsPersistence.loadBatches();

                List<DownloadBatch> downloadBatches = new ArrayList<>(batchPersistedList.size());
                for (DownloadsPersistence.BatchPersisted batchPersisted : batchPersistedList) {
                    DownloadBatchStatus.Status status = batchPersisted.getDownloadBatchStatus();
                    DownloadBatchId downloadBatchId = batchPersisted.getDownloadBatchId();
                    DownloadBatchTitle downloadBatchTitle = batchPersisted.getDownloadBatchTitle();
                    DownloadBatchStatus downloadBatchStatus = new DownloadBatchStatus(
                            downloadsBatchPersistence,
                            notificationCreator,
                            downloadBatchId,
                            downloadBatchTitle,
                            status
                    );

                    List<DownloadFile> downloadFiles = downloadsFilePersistence.loadSync(
                            downloadBatchId,
                            status,
                            fileSizeRequester,
                            filePersistenceCreator,
                            fileDownloader,
                            downloadsFilePersistence
                    );

                    DownloadBatch downloadBatch = DownloadBatchFactory.newInstance(
                            downloadBatchTitle,
                            downloadBatchId,
                            downloadFiles,
                            downloadBatchStatus,
                            DownloadsBatchPersistence.this
                    );

                    downloadBatches.add(downloadBatch);
                }

                callback.onLoaded(downloadBatches);
            }
        });
    }

    void deleteAsync(final DownloadBatchId downloadBatchId) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                downloadsPersistence.startTransaction();
                try {
                    downloadsPersistence.delete(downloadBatchId);
                    downloadsPersistence.transactionSuccess();
                } finally {
                    downloadsPersistence.endTransaction();
                }
            }
        });
    }

    void updateStatusAsync(final DownloadBatchId downloadBatchId, final DownloadBatchStatus.Status status) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                downloadsPersistence.startTransaction();
                try {
                    downloadsPersistence.update(downloadBatchId, status);
                    downloadsPersistence.transactionSuccess();
                } finally {
                    downloadsPersistence.endTransaction();
                }
            }
        });
    }

    interface LoadBatchesCallback {

        void onLoaded(List<DownloadBatch> downloadBatches);
    }
}
