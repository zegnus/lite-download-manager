package com.novoda.litedownloadmanager;

import com.novoda.litedownloadmanager.DownloadError.Error;

class DownloadFile {

    private final String url;
    private final DownloadFileStatus downloadFileStatus;
    private final FileName fileName;
    private final FileSizeRequester fileSizeRequester;
    private final FilePersistence filePersistence;
    private final DownloadsPersistence downloadsPersistence;

    private final Downloader downloader;
    private FileSize fileSize;

    DownloadFile(String url,
                 DownloadFileStatus downloadFileStatus,
                 FileName fileName,
                 FileSize fileSize,
                 FileSizeRequester fileSizeRequester,
                 FilePersistence filePersistence,
                 Downloader downloader,
                 DownloadsPersistence downloadsPersistence) {
        this.url = url;
        this.downloadFileStatus = downloadFileStatus;
        this.fileName = fileName;
        this.fileSizeRequester = fileSizeRequester;
        this.filePersistence = filePersistence;
        this.downloader = downloader;
        this.fileSize = fileSize;
        this.downloadsPersistence = downloadsPersistence;
    }

    void download(final Callback callback) {
        callback.onUpdate(downloadFileStatus);

        if (fileSize.getCurrentSize() == fileSize.getTotalSize()) {
            return;
        }

        moveStatusToDownloadingIfQueued();

        fileSize = requestTotalFileSizeIfNecessary(fileSize);
        if (fileSize.isTotalSizeUnknown()) {
            updateAndFeedbackWithStatus(Error.FILE_TOTAL_SIZE_REQUEST_FAILED, callback);
            return;
        }

        FilePersistence.Status status = filePersistence.create(fileName, fileSize);
        if (status.isMarkedAsError()) {
            Error error = filePersistence.convertError(status);
            updateAndFeedbackWithStatus(error, callback);
            return;
        }

        fileSize.setCurrentSize(filePersistence.getCurrentSize());

        downloader.startDownloading(url, fileSize, new Downloader.Callback() {
            @Override
            public void onBytesRead(byte[] buffer, int bytesRead) {
                boolean success = filePersistence.write(buffer, 0, bytesRead);
                if (!success) {
                    updateAndFeedbackWithStatus(Error.FILE_CANNOT_BE_WRITTEN, callback);
                }

                if (downloadFileStatus.isMarkedAsDownloading()) {
                    fileSize.addToCurrentSize(bytesRead);
                    downloadFileStatus.update(fileSize);
                    callback.onUpdate(downloadFileStatus);
                }
            }

            @Override
            public void onError() {
                updateAndFeedbackWithStatus(Error.CANNOT_DOWNLOAD_FILE, callback);
            }

            @Override
            public void onDownloadFinished() {
                filePersistence.close();
                if (downloadFileStatus.isMarkedForDeletion()) {
                    filePersistence.delete();
                }
            }
        });
    }

    private FileSize requestTotalFileSizeIfNecessary(FileSize fileSize) {
        FileSize updatedFileSize = fileSize.copy();

        if (fileSize.isTotalSizeUnknown()) {
            FileSize requestFileSize = fileSizeRequester.requestFileSize(url);
            if (requestFileSize.isTotalSizeKnown()) {
                updatedFileSize.setTotalSize(requestFileSize.getTotalSize());
            }
        }

        return updatedFileSize;
    }

    private void updateAndFeedbackWithStatus(Error error, Callback callback) {
        downloadFileStatus.markAsError(error);
        callback.onUpdate(downloadFileStatus);
    }

    private void moveStatusToDownloadingIfQueued() {
        if (downloadFileStatus.isMarkedAsQueued()) {
            downloadFileStatus.markAsDownloading();
        }
    }

    void pause() {
        downloadFileStatus.isMarkedAsPaused();
        downloader.stopDownloading();
    }

    void resume() {
        downloadFileStatus.markAsQueued();
    }

    void delete() {
        if (downloadFileStatus.isMarkedAsDownloading()) {
            downloadFileStatus.markForDeletion();
            downloader.stopDownloading();
        } else {
            filePersistence.delete();
        }
    }

    long getTotalSize() {
        if (fileSize.isTotalSizeUnknown()) {
            FileSize requestFileSize = fileSizeRequester.requestFileSize(url);
            fileSize.setTotalSize(requestFileSize.getTotalSize());
        }

        return fileSize.getTotalSize();
    }

    void persistFileWith(DownloadBatchId downloadBatchId) {
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

    interface Callback {

        void onUpdate(DownloadFileStatus downloadFileStatus);
    }
}
