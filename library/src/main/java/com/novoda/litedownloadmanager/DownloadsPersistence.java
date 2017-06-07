package com.novoda.litedownloadmanager;

import java.util.List;

public interface DownloadsPersistence {

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
        private final DownloadBatchTitle downloadBatchTitle;
        private final DownloadBatchId downloadBatchId;
        private final DownloadBatchStatus.Status status;

        BatchPersisted(DownloadBatchTitle downloadBatchTitle, DownloadBatchId downloadBatchId, DownloadBatchStatus.Status status) {
            this.downloadBatchTitle = downloadBatchTitle;
            this.downloadBatchId = downloadBatchId;
            this.status = status;
        }

        public DownloadBatchId getDownloadBatchId() {
            return downloadBatchId;
        }

        public DownloadBatchStatus.Status getDownloadBatchStatus() {
            return status;
        }

        public DownloadBatchTitle getDownloadBatchTitle() {
            return downloadBatchTitle;
        }
    }

    class FilePersisted {
        private final DownloadBatchId downloadBatchId;
        private final DownloadFileId downloadFileId;
        private final FileName fileName;
        private final long totalFileSize;
        private final String url;
        private final FilePersistenceType filePersistenceType;

        FilePersisted(DownloadBatchId downloadBatchId,
                      DownloadFileId downloadFileId,
                      FileName fileName,
                      long totalFileSize,
                      String url,
                      FilePersistenceType filePersistenceType) {
            this.downloadBatchId = downloadBatchId;
            this.downloadFileId = downloadFileId;
            this.fileName = fileName;
            this.totalFileSize = totalFileSize;
            this.url = url;
            this.filePersistenceType = filePersistenceType;
        }

        public DownloadBatchId getDownloadBatchId() {
            return downloadBatchId;
        }

        public FileName getFileName() {
            return fileName;
        }

        public long getTotalFileSize() {
            return totalFileSize;
        }

        public String getUrl() {
            return url;
        }

        public DownloadFileId getDownloadFileId() {
            return downloadFileId;
        }

        FilePersistenceType getFilePersistenceType() {
            return filePersistenceType;
        }
    }
}
