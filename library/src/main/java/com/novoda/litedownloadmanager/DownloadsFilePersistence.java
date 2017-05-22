package com.novoda.litedownloadmanager;

import java.util.ArrayList;
import java.util.List;

class DownloadsFilePersistence {

    private final DownloadsPersistence downloadsPersistence;

    DownloadsFilePersistence(DownloadsPersistence downloadsPersistence) {
        this.downloadsPersistence = downloadsPersistence;
    }

    void persistSync(DownloadBatchId downloadBatchId, FileName fileName, FileSize fileSize, String url, DownloadFileStatus downloadFileStatus) {
        DownloadsPersistence.FilePersisted filePersisted = new DownloadsPersistence.FilePersisted(
                downloadBatchId,
                downloadFileStatus.getDownloadFileId(),
                fileName,
                fileSize,
                url,
                downloadFileStatus.getStatus()
        );

        downloadsPersistence.persistFile(filePersisted);
    }

    List<DownloadFile> loadSync(DownloadBatchId batchId,
                                FileSizeRequester fileSizeRequester,
                                FilePersistence filePersistence,
                                Downloader downloader,
                                DownloadsFilePersistence downloadsFilePersistence) {
        List<DownloadsPersistence.FilePersisted> filePersistedList = downloadsPersistence.loadFiles(batchId);

        List<DownloadFile> downloadFiles = new ArrayList<>(filePersistedList.size());
        for (DownloadsPersistence.FilePersisted filePersisted : filePersistedList) {

            DownloadFileStatus.Status status = filePersisted.getStatus();
            DownloadFileId downloadFileId = filePersisted.getDownloadFileId();
            FileName fileName = filePersisted.getFileName();
            FileSize fileSize = filePersisted.getFileSize();
            String url = filePersisted.getUrl();

            DownloadFileStatus downloadFileStatus = new DownloadFileStatus(downloadFileId, status, fileSize, new DownloadError());

            DownloadFile downloadFile = new DownloadFile(
                    url,
                    downloadFileStatus,
                    fileName,
                    fileSize,
                    fileSizeRequester,
                    filePersistence,
                    downloader,
                    downloadsFilePersistence
            );
            downloadFiles.add(downloadFile);
        }

        return downloadFiles;

    }
}
