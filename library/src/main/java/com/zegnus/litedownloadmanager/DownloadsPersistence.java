package com.zegnus.litedownloadmanager;

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

    void update(DownloadBatchId downloadBatchId, LiteDownloadBatchStatus.Status status);

    class BatchPersisted {
        private final DownloadBatchTitle downloadBatchTitle;
        private final DownloadBatchId downloadBatchId;
        private final LiteDownloadBatchStatus.Status status;

        BatchPersisted(DownloadBatchTitle downloadBatchTitle, DownloadBatchId downloadBatchId, LiteDownloadBatchStatus.Status status) {
            this.downloadBatchTitle = downloadBatchTitle;
            this.downloadBatchId = downloadBatchId;
            this.status = status;
        }

        public DownloadBatchId getDownloadBatchId() {
            return downloadBatchId;
        }

        public LiteDownloadBatchStatus.Status getDownloadBatchStatus() {
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
        private final FilePath filePath;
        private final long totalFileSize;
        private final String url;
        private final FilePersistenceType filePersistenceType;

        FilePersisted(DownloadBatchId downloadBatchId,
                      DownloadFileId downloadFileId,
                      FileName fileName,
                      FilePath filePath,
                      long totalFileSize,
                      String url,
                      FilePersistenceType filePersistenceType) {
            this.downloadBatchId = downloadBatchId;
            this.downloadFileId = downloadFileId;
            this.fileName = fileName;
            this.filePath = filePath;
            this.totalFileSize = totalFileSize;
            this.url = url;
            this.filePersistenceType = filePersistenceType;
        }

        public DownloadBatchId downloadBatchId() {
            return downloadBatchId;
        }

        public FileName fileName() {
            return fileName;
        }

        public FilePath filePath() {
            return filePath;
        }

        public long totalFileSize() {
            return totalFileSize;
        }

        public String url() {
            return url;
        }

        public DownloadFileId downloadFileId() {
            return downloadFileId;
        }

        FilePersistenceType filePersistenceType() {
            return filePersistenceType;
        }

        @Override
        public String toString() {
            return "FilePersisted{" +
                    "downloadBatchId=" + downloadBatchId +
                    ", downloadFileId=" + downloadFileId +
                    ", fileName=" + fileName +
                    ", filePath=" + filePath +
                    ", totalFileSize=" + totalFileSize +
                    ", url='" + url + '\'' +
                    ", filePersistenceType=" + filePersistenceType +
                    '}';
        }
    }
}
