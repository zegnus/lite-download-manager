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
        private final FileSize fileSize;
        private final String url;
        private final DownloadFileStatus.Status status;

        FilePersisted(DownloadBatchId downloadBatchId,
                      DownloadFileId downloadFileId,
                      FileName fileName,
                      FileSize fileSize,
                      String url,
                      DownloadFileStatus.Status status) {
            this.downloadBatchId = downloadBatchId;
            this.downloadFileId = downloadFileId;
            this.fileName = fileName;
            this.fileSize = fileSize;
            this.url = url;
            this.status = status;
        }

        DownloadBatchId getDownloadBatchId() {
            return downloadBatchId;
        }

        FileName getFileName() {
            return fileName;
        }

        FileSize getFileSize() {
            return fileSize;
        }

        String getUrl() {
            return url;
        }

        DownloadFileStatus.Status getStatus() {
            return status;
        }

        DownloadFileId getDownloadFileId() {
            return downloadFileId;
        }
    }
}
