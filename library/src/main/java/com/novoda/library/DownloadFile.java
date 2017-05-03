package com.novoda.library;

public class DownloadFile {

    private static final int BUFFER_SIZE = 5000;
    private static final int NETWORK_COST = 10;

    private final DownloadFileId downloadFileId;
    private final String url;
    private final DownloadFileStatus downloadFileStatus;
    private final FileDeleter fileDeleter;
    private final FileSizeRequester fileSizeRequester;

    private long bytesDownloaded;
    private long totalFileSizeBytes;

    public static DownloadFile newInstance(String id, String url) {
        DownloadFileId downloadFileId = DownloadFileId.from(id);
        DownloadFileStatus downloadFileStatus = new DownloadFileStatus(downloadFileId, DownloadFileStatus.Status.QUEUED);
        FileDeleter fileDeleter = new FileDeleter();
        FileSizeRequester fileSizeRequester = new FileSizeRequester();
        return new DownloadFile(downloadFileId, url, downloadFileStatus, fileDeleter, fileSizeRequester);
    }

    DownloadFile(DownloadFileId downloadFileId,
                 String url,
                 DownloadFileStatus downloadFileStatus,
                 FileDeleter fileDeleter,
                 FileSizeRequester fileSizeRequester) {
        this.url = url;
        this.downloadFileId = downloadFileId;
        this.downloadFileStatus = downloadFileStatus;
        this.fileDeleter = fileDeleter;
        this.fileSizeRequester = fileSizeRequester;
    }

    void download(Callback callback) {
        callback.onUpdate(downloadFileStatus);

        totalFileSizeBytes = getTotalSize();

        moveStatusToDownloadingIfQueued();

        while (downloadFileStatus.isMarkedAsDownloading() && bytesDownloaded < totalFileSizeBytes) {
            try {
                Thread.sleep(NETWORK_COST);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if (downloadFileStatus.isMarkedAsDownloading()) {
                bytesDownloaded += BUFFER_SIZE;
                downloadFileStatus.update(bytesDownloaded, totalFileSizeBytes);
                callback.onUpdate(downloadFileStatus);
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

    long getTotalSize() {
        if (totalFileSizeBytes == 0) {
            totalFileSizeBytes = fileSizeRequester.requestFileSize(url);
        }

        return totalFileSizeBytes;
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

    interface Callback {

        void onUpdate(DownloadFileStatus downloadFileStatus);
    }
}
