package com.novoda.library;

import android.content.Context;

import com.novoda.library.DownloadError.Error;
import com.squareup.okhttp.OkHttpClient;

import java.util.concurrent.TimeUnit;

public class DownloadFile {

    private final String url;
    private final DownloadFileStatus downloadFileStatus;
    private final FileSizeRequester fileSizeRequester;
    private final Persistence persistence;
    private final Downloader downloader;
    private final FileName fileName;

    private FileSize fileSize;

    public static DownloadFile newInstance(Context context, String id, String url) {
        FileSize fileSize = FileSize.Unknown();
        DownloadFileId downloadFileId = DownloadFileId.from(id);
        DownloadError downloadError = new DownloadError();
        DownloadFileStatus downloadFileStatus = new DownloadFileStatus(downloadFileId, DownloadFileStatus.Status.QUEUED, fileSize, downloadError);
        OkHttpClient httpClient = new OkHttpClient();
        httpClient.setConnectTimeout(5, TimeUnit.SECONDS);
        httpClient.setWriteTimeout(5, TimeUnit.SECONDS);
        httpClient.setReadTimeout(5, TimeUnit.SECONDS);
        Persistence persistence = PersistenceCreator.createInternalPhysicalPersistence(context);
        FileSizeRequester fileSizeRequester = new NetworkFileSizeRequester(httpClient);
        Downloader downloader = new NetworkDownloader(httpClient);
        FileName fileName = FileName.fromUrl(url);

        return new DownloadFile(
                url,
                downloadFileStatus,
                fileSizeRequester,
                persistence,
                downloader,
                fileName,
                fileSize
        );
    }

    DownloadFile(String url,
                 DownloadFileStatus downloadFileStatus,
                 FileSizeRequester fileSizeRequester,
                 Persistence persistence,
                 Downloader downloader, FileName fileName, FileSize fileSize) {
        this.url = url;
        this.downloadFileStatus = downloadFileStatus;
        this.fileSizeRequester = fileSizeRequester;
        this.persistence = persistence;
        this.downloader = downloader;
        this.fileName = fileName;
        this.fileSize = fileSize;
    }

    void download(final Callback callback) {
        callback.onUpdate(downloadFileStatus);

        moveStatusToDownloadingIfQueued();

        fileSize = requestTotalFileSizeIfNecessary(fileSize);
        if (fileSize.isTotalSizeUnknown()) {
            updateAndFeedbackWithStatus(Error.FILE_TOTAL_SIZE_REQUEST_FAILED, callback);
            return;
        }

        Persistence.Status status = persistence.create(fileName, fileSize);
        if (status.isMarkedAsError()) {
            updateAndFeedbackWithStatus(Error.FILE_CANNOT_BE_CREATED_LOCALLY_INSUFFICIENT_FREE_SPACE, callback);
            return;
        }

        fileSize.setCurrentSize(persistence.getCurrentSize());

        downloader.startDownloading(url, fileSize, new Downloader.Callback() {
            @Override
            public void onBytesRead(byte[] buffer, int bytesRead) {
                boolean success = persistence.write(buffer, 0, bytesRead);
                if (!success) {
                    updateAndFeedbackWithStatus(Error.CANNOT_WRITE, callback);
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
            public void onDownloadStopped() {
                persistence.close();
                if (downloadFileStatus.isMarkedForDeletion()) {
                    persistence.delete();
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
            persistence.delete();
        }
    }

    long getTotalSize() {
        if (fileSize.isTotalSizeUnknown()) {
            FileSize requestFileSize = fileSizeRequester.requestFileSize(url);
            fileSize.setTotalSize(requestFileSize.getTotalSize());
        }

        return fileSize.getTotalSize();
    }

    interface Callback {

        void onUpdate(DownloadFileStatus downloadFileStatus);
    }
}
