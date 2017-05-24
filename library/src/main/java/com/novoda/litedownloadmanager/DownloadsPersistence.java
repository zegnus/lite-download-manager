package com.novoda.litedownloadmanager;

import java.util.List;

interface DownloadsPersistence {

    void startTransaction();

    void endTransaction();

    void transactionSuccess();

    void persistBatch(BatchPersisted batchPersisted);

    List<BatchPersisted> loadBatches();

    void persistFile(FilePersisted filePersisted);

    List<FilePersisted> loadFiles(DownloadBatchId batchId);

    void delete(DownloadBatchId downloadBatchId);

    void update(DownloadBatchId downloadBatchId, DownloadBatchStatus.Status status);

    class BatchPersisted {
        private final DownloadBatchId downloadBatchId;
        private final DownloadBatchStatus.Status status;

        BatchPersisted(DownloadBatchId downloadBatchId, DownloadBatchStatus.Status status) {
            this.downloadBatchId = downloadBatchId;
            this.status = status;
        }

        DownloadBatchId getDownloadBatchId() {
            return downloadBatchId;
        }

        DownloadBatchStatus.Status getDownloadBatchStatus() {
            return status;
        }
    }

    class FilePersisted {
        private final DownloadBatchId downloadBatchId;
        private final DownloadFileId downloadFileId;
        private final FileName fileName;
        private final long totalFileSize;
        private final String url;

        FilePersisted(DownloadBatchId downloadBatchId,
                      DownloadFileId downloadFileId,
                      FileName fileName,
                      long totalFileSize,
                      String url) {
            this.downloadBatchId = downloadBatchId;
            this.downloadFileId = downloadFileId;
            this.fileName = fileName;
            this.totalFileSize = totalFileSize;
            this.url = url;
        }

        DownloadBatchId getDownloadBatchId() {
            return downloadBatchId;
        }

        FileName getFileName() {
            return fileName;
        }

        long getTotalFileSize() {
            return totalFileSize;
        }

        String getUrl() {
            return url;
        }

        DownloadFileId getDownloadFileId() {
            return downloadFileId;
        }
    }
}
