package com.novoda.library;

import android.content.Context;

import com.novoda.library.DownloadError.Error;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.concurrent.TimeUnit;

public class DownloadFile {

    private static final int BUFFER_SIZE = 8 * 512;

    private final DownloadFileId downloadFileId;
    private final String url;
    private final DownloadFileStatus downloadFileStatus;
    private final FileDeleter fileDeleter;
    private final FileSizeRequester fileSizeRequester;
    private final OkHttpClient httpClient;
    private final Persistence persistence;

    private FileSize fileSize;

    public static DownloadFile newInstance(String id, String url) {
        FileSize fileSize = FileSize.Unknown();
        DownloadFileId downloadFileId = DownloadFileId.from(id);
        DownloadError downloadError = new DownloadError();
        DownloadFileStatus downloadFileStatus = new DownloadFileStatus(downloadFileId, DownloadFileStatus.Status.QUEUED, fileSize, downloadError);
        FileDeleter fileDeleter = new FileDeleter();
        OkHttpClient httpClient = new OkHttpClient();
        httpClient.setConnectTimeout(5, TimeUnit.SECONDS);
        httpClient.setWriteTimeout(5, TimeUnit.SECONDS);
        httpClient.setReadTimeout(5, TimeUnit.SECONDS);
        Persistence persistence = PersistanceCreator.createInternalPhysicalPersistance();
        FileSizeRequester fileSizeRequester = new NetworkFileSizeRequester(httpClient);

        return new DownloadFile(
                downloadFileId,
                url,
                downloadFileStatus,
                fileDeleter,
                fileSizeRequester,
                httpClient,
                persistence,
                fileSize
        );
    }

    DownloadFile(DownloadFileId downloadFileId,
                 String url,
                 DownloadFileStatus downloadFileStatus,
                 FileDeleter fileDeleter,
                 FileSizeRequester fileSizeRequester,
                 OkHttpClient httpClient,
                 Persistence persistence,
                 FileSize fileSize) {
        this.url = url;
        this.downloadFileId = downloadFileId;
        this.downloadFileStatus = downloadFileStatus;
        this.fileDeleter = fileDeleter;
        this.fileSizeRequester = fileSizeRequester;
        this.httpClient = httpClient;
        this.persistence = persistence;
        this.fileSize = fileSize;
    }

    void download(Callback callback, Context context) {
        callback.onUpdate(downloadFileStatus);

        moveStatusToDownloadingIfQueued();

        if (fileSize.isTotalSizeUnknown()) {
            FileSize requestFileSize = fileSizeRequester.requestFileSize(url);
            if (requestFileSize.isTotalSizeUnknown()) {
                downloadFileStatus.markAsError(Error.FILE_TOTAL_SIZE_REQUEST_FAILED);
                callback.onUpdate(downloadFileStatus);
                return;
            }

            fileSize.setTotalSize(requestFileSize.getTotalSize());
        }

        FileName fileName = FileName.fromUrl(url);
        Persistence.Status status = persistence.create(fileName, context, fileSize);
        if (status.isMarkedAsError()) {
            downloadFileStatus.markAsError(Error.FILE_CANNOT_BE_CREATED_LOCALLY_INSUFFICIENT_FREE_SPACE);
            callback.onUpdate(downloadFileStatus);
            return;
        }

        fileSize.setCurrentSize(persistence.getCurrentSize());

        Request.Builder requestBuilder = new Request.Builder().url(url);
        if (fileSize.areBytesDownloadedKnown()) {
            requestBuilder.addHeader("Range", "bytes=" + fileSize.getCurrentSize() + "-" + fileSize.getTotalSize());
        }

        Call call = httpClient.newCall(requestBuilder.build());
        Response response = null;
        try {
            response = call.execute();
            int responseCode = response.code();
            if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_PARTIAL) {
                InputStream in = response.body().byteStream();
                    byte[] buffer = new byte[BUFFER_SIZE];
                    int readLast = 0;
                    while (downloadFileStatus.isMarkedAsDownloading() && readLast != -1) {
                        readLast = in.read(buffer);

                        if (readLast != 0 && readLast != -1) {
                            try {
                                persistence.write(buffer, 0, readLast);
                            } catch (IOException e) {
                                downloadFileStatus.markAsError(Error.CANNOT_WRITE);
                                callback.onUpdate(downloadFileStatus);
                            }

                            if (downloadFileStatus.isMarkedAsDownloading()) {
                                fileSize.addToCurrentSize(readLast);
                                downloadFileStatus.update(fileSize);
                                callback.onUpdate(downloadFileStatus);
                            }
                        }
                    }
            } else {
                downloadFileStatus.markAsError(Error.CANNOT_DOWNLOAD_FILE_FROM_NETWORK);
                downloadFileStatus.setErrorCode(responseCode);
                callback.onUpdate(downloadFileStatus);
            }
        } catch (IOException e) {
            e.printStackTrace();
            downloadFileStatus.markAsError(Error.CONNECTION_FAILURE);
            callback.onUpdate(downloadFileStatus);
        } finally {
            try {
                if (response != null) {
                    response.body().close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        persistence.close();

        if (downloadFileStatus.isMarkedForDeletion()) {
            fileDeleter.delete(downloadFileId);
        }
    }

    private void moveStatusToDownloadingIfQueued() {
        if (downloadFileStatus.isMarkedAsQueued()) {
            downloadFileStatus.markAsDownloading();
        }
    }

    void pause() {
        downloadFileStatus.isMarkedAsPaused();
    }

    void resume() {
        downloadFileStatus.markAsQueued();
    }

    void delete() {
        downloadFileStatus.markForDeletion();
    }

    long getTotalSize() {
        if (fileSize.isTotalSizeUnknown()) {
            FileSize requestFileSize = fileSizeRequester.requestFileSize(url);
            fileSize.setTotalSize(requestFileSize.getTotalSize());
        }

        return fileSize.getTotalSize();
    }

    boolean isMarkedAsError() {
        return downloadFileStatus.isMarkedAsError();
    }

    interface Callback {

        void onUpdate(DownloadFileStatus downloadFileStatus);
    }
}
