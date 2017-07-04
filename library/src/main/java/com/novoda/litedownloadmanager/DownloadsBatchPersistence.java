package com.novoda.litedownloadmanager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

class DownloadsBatchPersistence implements DownloadsBatchStatusPersistence {

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

    void loadAsync(final FileOperations fileOperations,
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
                            notificationCreator,
                            downloadBatchId,
                            downloadBatchTitle,
                            status
                    );

                    List<DownloadFile> downloadFiles = downloadsFilePersistence.loadSync(
                            downloadBatchId,
                            status,
                            fileOperations,
                            downloadsFilePersistence
                    );

                    long currentBytesDownloaded = 0;
                    long totalBatchSizeBytes = 0;
                    for (DownloadFile downloadFile : downloadFiles) {
                        currentBytesDownloaded += downloadFile.getCurrentDownloadedBytes();
                        totalBatchSizeBytes += downloadFile.getTotalSize();
                    }
                    downloadBatchStatus.update(currentBytesDownloaded, totalBatchSizeBytes);

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

    @Override
    public void updateStatusAsync(final DownloadBatchId downloadBatchId, final DownloadBatchStatus.Status status) {
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
