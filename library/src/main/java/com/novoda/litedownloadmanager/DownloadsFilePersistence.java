package com.novoda.litedownloadmanager;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

class DownloadsFilePersistence {

    private final DownloadsPersistence downloadsPersistence;

    DownloadsFilePersistence(DownloadsPersistence downloadsPersistence) {
        this.downloadsPersistence = downloadsPersistence;
    }

    void persistSync(DownloadBatchId downloadBatchId,
                     FileName fileName,
                     FileSize fileSize,
                     String url,
                     DownloadFileId downloadFileId,
                     FilePersistenceType filePersistenceType) {
        DownloadsPersistence.FilePersisted filePersisted = new DownloadsPersistence.FilePersisted(
                downloadBatchId,
                downloadFileId,
                fileName,
                fileSize.getTotalSize(),
                url,
                filePersistenceType
        );

        downloadsPersistence.persistFile(filePersisted);
    }

    List<DownloadFile> loadSync(DownloadBatchId batchId,
                                DownloadBatchStatus.Status batchStatus,
                                FileOperations fileOperations,
                                DownloadsFilePersistence downloadsFilePersistence) {
        List<DownloadsPersistence.FilePersisted> filePersistedList = downloadsPersistence.loadFiles(batchId);

        List<DownloadFile> downloadFiles = new ArrayList<>(filePersistedList.size());
        for (DownloadsPersistence.FilePersisted filePersisted : filePersistedList) {

            DownloadFileId downloadFileId = filePersisted.getDownloadFileId();
            FileName fileName = filePersisted.getFileName();
            FileSize fileSize = FileSize.Total(filePersisted.getTotalFileSize());
            String url = filePersisted.getUrl();

            DownloadFileStatus downloadFileStatus = new DownloadFileStatus(
                    downloadFileId,
                    getFileStatusFrom(batchStatus),
                    fileSize,
                    new DownloadError()
            );

            FileSizeRequester fileSizeRequester = fileOperations.fileSizeRequester();
            FileDownloader fileDownloader = fileOperations.fileDownloader();
            FilePersistenceCreator filePersistenceCreator = fileOperations.filePersistenceCreator();

            DownloadFile downloadFile = new DownloadFile(
                    batchId,
                    url,
                    downloadFileStatus,
                    fileName,
                    fileSize,
                    fileDownloader,
                    fileSizeRequester,
                    filePersistenceCreator.create(filePersisted.getFilePersistenceType()),
                    downloadsFilePersistence
            );

            downloadFiles.add(downloadFile);
        }

        return downloadFiles;
    }

    private DownloadFileStatus.Status getFileStatusFrom(DownloadBatchStatus.Status batchStatus) {
        switch (batchStatus) {
            case QUEUED:
                return DownloadFileStatus.Status.QUEUED;
            case DOWNLOADING:
                return DownloadFileStatus.Status.DOWNLOADING;
            case PAUSED:
                return DownloadFileStatus.Status.PAUSED;
            case ERROR:
                return DownloadFileStatus.Status.ERROR;
            case DELETION:
                return DownloadFileStatus.Status.DELETION;
            case DOWNLOADED:
                return DownloadFileStatus.Status.DOWNLOADING;
        }

        throw new InvalidParameterException("Batch status " + batchStatus + " is unsupported");
    }
}
