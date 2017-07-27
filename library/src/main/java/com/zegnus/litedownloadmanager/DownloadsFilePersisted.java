package com.zegnus.litedownloadmanager;

public class DownloadsFilePersisted {

    private final DownloadBatchId downloadBatchId;
    private final DownloadFileId downloadFileId;
    private final FileName fileName;
    private final FilePath filePath;
    private final long totalFileSize;
    private final String url;
    private final FilePersistenceType filePersistenceType;

    DownloadsFilePersisted(DownloadBatchId downloadBatchId,
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
}
