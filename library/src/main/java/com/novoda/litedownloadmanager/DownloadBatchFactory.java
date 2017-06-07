package com.novoda.litedownloadmanager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

final class DownloadBatchFactory {

    private DownloadBatchFactory() {
        // non instantiable factory class
    }

    static DownloadBatch newInstance(Batch batch,
                                     FileSizeRequester fileSizeRequester,
                                     FilePersistenceCreator filePersistenceCreator,
                                     FileDownloader fileDownloader,
                                     DownloadsBatchPersistence downloadsBatchPersistence,
                                     DownloadsFilePersistence downloadsFilePersistence,
                                     DownloadBatchNotification downloadBatchNotification) {
        DownloadBatchTitle downloadBatchTitle = DownloadBatchTitle.from(batch);
        DownloadBatchId downloadBatchId = DownloadBatchId.from(batch);
        List<String> fileUrls = batch.getFileUrls();
        List<DownloadFile> downloadFiles = new ArrayList<>(fileUrls.size());

        for (String fileUrl : fileUrls) {
            FileSize fileSize = FileSize.Unknown();
            DownloadFileId downloadFileId = DownloadFileId.from(batch);
            DownloadError downloadError = new DownloadError();
            DownloadFileStatus downloadFileStatus = new DownloadFileStatus(
                    downloadFileId,
                    DownloadFileStatus.Status.QUEUED,
                    fileSize,
                    downloadError
            );
            FileName fileName = FileName.from(batch, fileUrl);

            FilePersistence filePersistence = filePersistenceCreator.create();
            DownloadFile downloadFile = new DownloadFile(
                    downloadBatchId,
                    fileUrl,
                    downloadFileStatus,
                    fileName,
                    fileSize,
                    fileSizeRequester,
                    filePersistence,
                    fileDownloader,
                    downloadsFilePersistence
            );
            downloadFiles.add(downloadFile);
        }

        DownloadBatchStatus downloadBatchStatus = new DownloadBatchStatus(
                downloadsBatchPersistence,
                downloadBatchNotification,
                downloadBatchId,
                downloadBatchTitle,
                DownloadBatchStatus.Status.QUEUED
        );

        return new DownloadBatch(
                downloadBatchTitle,
                downloadBatchId,
                downloadFiles,
                new HashMap<DownloadFileId, Long>(),
                downloadBatchStatus,
                downloadsBatchPersistence
        );
    }

    static DownloadBatch newInstance(DownloadBatchTitle downloadBatchTitle,
                                     DownloadBatchId downloadBatchId,
                                     List<DownloadFile> downloadFiles,
                                     DownloadBatchStatus downloadBatchStatus,
                                     DownloadsBatchPersistence downloadsBatchPersistence) {
        return new DownloadBatch(
                downloadBatchTitle,
                downloadBatchId,
                downloadFiles,
                new HashMap<DownloadFileId, Long>(),
                downloadBatchStatus,
                downloadsBatchPersistence
        );
    }
}
