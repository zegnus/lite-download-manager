package com.novoda.litedownloadmanager;

class FileOperations {

    private final FilePersistenceCreator filePersistenceCreator;
    private final FileSizeRequester fileSizeRequester;
    private final FileDownloader fileDownloader;

    FileOperations(FilePersistenceCreator filePersistenceCreator, FileSizeRequester fileSizeRequester, FileDownloader fileDownloader) {
        this.filePersistenceCreator = filePersistenceCreator;
        this.fileSizeRequester = fileSizeRequester;
        this.fileDownloader = fileDownloader;
    }

    FilePersistence createPersistence() {
        return filePersistenceCreator.create();
    }

    FilePersistence createPersistence(FilePersistenceType filePersistenceType) {
        return filePersistenceCreator.create(filePersistenceType);
    }

    void startDownloading(String url, FileSize fileSize, FileDownloader.Callback callback) {
        fileDownloader.startDownloading(url, fileSize, callback);
    }

    void stopDownloading() {
        fileDownloader.stopDownloading();
    }

    FileSize requestFileSize(String url) {
        return fileSizeRequester.requestFileSize(url);
    }
}
