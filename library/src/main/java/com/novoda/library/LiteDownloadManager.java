package com.novoda.library;

class LiteDownloadManager implements LiteDownloadManagerCommands {

    private final DownloadServiceCommands downloadService;

    LiteDownloadManager(DownloadServiceCommands downloadService) {
        this.downloadService = downloadService;
    }

    @Override
    public void download(DownloadBatch downloadBatch, DownloadBatch.Callback callback) {
        downloadService.download(downloadBatch, callback);
    }
}
