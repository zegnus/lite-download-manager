package com.novoda.library;

import com.novoda.library.DownloadError.Error;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;

public class DownloadFile {

    private static final int BUFFER_SIZE = 8 * 512;
    private static final int ZERO_BYTES = 0;

    private final DownloadFileId downloadFileId;
    private final String url;
    private final String localPath;
    private final DownloadFileStatus downloadFileStatus;
    private final FileDeleter fileDeleter;
    private final FileSizeRequester fileSizeRequester;
    private final OkHttpClient httpClient;

    private FileSize fileSize;

    public static DownloadFile newInstance(String id, String url) {
        FileSize fileSize = FileSize.Unknown();
        DownloadFileId downloadFileId = DownloadFileId.from(id);
        DownloadError downloadError = new DownloadError();
        DownloadFileStatus downloadFileStatus = new DownloadFileStatus(downloadFileId, DownloadFileStatus.Status.QUEUED, fileSize, downloadError);
        FileDeleter fileDeleter = new FileDeleter();
        String localPath = "";
        OkHttpClient httpClient = new OkHttpClient();
        FileSizeRequester fileSizeRequester = new NetworkFileSizeRequester(httpClient);

        return new DownloadFile(downloadFileId, url, localPath, downloadFileStatus, fileDeleter, fileSizeRequester, httpClient, fileSize);
    }

    DownloadFile(DownloadFileId downloadFileId,
                 String url,
                 String localPath,
                 DownloadFileStatus downloadFileStatus,
                 FileDeleter fileDeleter,
                 FileSizeRequester fileSizeRequester,
                 OkHttpClient httpClient,
                 FileSize fileSize) {
        this.url = url;
        this.downloadFileId = downloadFileId;
        this.localPath = localPath;
        this.downloadFileStatus = downloadFileStatus;
        this.fileDeleter = fileDeleter;
        this.fileSizeRequester = fileSizeRequester;
        this.httpClient = httpClient;
        this.fileSize = fileSize;
    }

    void download(Callback callback) {
        callback.onUpdate(downloadFileStatus);

        if (fileSize.isTotalSizeUnknown()) {
            FileSize requestFileSize = fileSizeRequester.requestFileSize(url);
            if (requestFileSize.isTotalSizeUnknown()) {
                downloadFileStatus.markAsError(Error.FILE_TOTAL_SIZE_REQUEST_FAILED);
                callback.onUpdate(downloadFileStatus);
                return;
            }
        }

        moveStatusToDownloadingIfQueued();

        File file = new File(localPath);

        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
            downloadFileStatus.markAsError(Error.FILE_CANNOT_BE_CREATED_LOCALLY);
            callback.onUpdate(downloadFileStatus);
        }

        Request.Builder requestBuilder = new Request.Builder().url(url);
        if (fileSize.areBytesDownloadedKnown()) {
            requestBuilder.addHeader("Range", "bytes=" + fileSize.getCurrentSize() + "-" + fileSize.getTotalSize());
        }

        Call call = httpClient.newCall(requestBuilder.build());
        Response response = null;
        try {
            response = call.execute();
            if (response.code() == HttpURLConnection.HTTP_OK) {
                InputStream in = response.body().byteStream();
                OutputStream out = new FileOutputStream(file, fileSize.areBytesDownloadedKnown());
                    byte[] buffer = new byte[BUFFER_SIZE];
                    int readLast = 0;
                    while (downloadFileStatus.isMarkedAsDownloading() && readLast != -1) {
                        try {
                            readLast = in.read(buffer);
                        } catch(IOException e) {
                            downloadFileStatus.markAsError(Error.CANNOT_READ_FROM_STREAM);
                            callback.onUpdate(downloadFileStatus);
                        }

                        if (readLast != 0 && readLast != -1) {
                            try {
                                out.write(buffer, 0, readLast);
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
                int errorCode = response.code();
                downloadFileStatus.setErrorCode(errorCode);
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
            return ZERO_BYTES;
        } else {
            return fileSize.getTotalSize();
        }
    }

    interface Callback {

        void onUpdate(DownloadFileStatus downloadFileStatus);
    }
}
