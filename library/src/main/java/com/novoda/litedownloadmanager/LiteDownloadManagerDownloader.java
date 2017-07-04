package com.novoda.litedownloadmanager;

import android.os.Handler;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

class LiteDownloadManagerDownloader {

    private final Object waitForDownloadService;
    private final ExecutorService executor;
    private final Handler callbackHandler;
    private final FileOperations fileOperations;
    private final DownloadsBatchPersistence downloadsBatchPersistence;
    private final DownloadsFilePersistence downloadsFilePersistence;
    private final NotificationCreator notificationCreator;
    private final List<DownloadBatchCallback> callbacks;

    private DownloadServiceCommands downloadService;

    LiteDownloadManagerDownloader(Object waitForDownloadService,
                                  ExecutorService executor,
                                  Handler callbackHandler,
                                  FileOperations fileOperations,
                                  DownloadsBatchPersistence downloadsBatchPersistence,
                                  DownloadsFilePersistence downloadsFilePersistence,
                                  NotificationCreator notificationCreator,
                                  List<DownloadBatchCallback> callbacks) {
        this.waitForDownloadService = waitForDownloadService;
        this.executor = executor;
        this.callbackHandler = callbackHandler;
        this.fileOperations = fileOperations;
        this.downloadsBatchPersistence = downloadsBatchPersistence;
        this.downloadsFilePersistence = downloadsFilePersistence;
        this.notificationCreator = notificationCreator;
        this.callbacks = callbacks;
    }

    public void download(Batch batch, Map<DownloadBatchId, DownloadBatch> downloadBatchMap) {
        DownloadBatch runningDownloadBatch = downloadBatchMap.get(batch.getDownloadBatchId());
        if (runningDownloadBatch != null) {
            return;
        }

        DownloadBatch downloadBatch = DownloadBatchFactory.newInstance(
                batch,
                fileOperations,
                downloadsBatchPersistence,
                downloadsFilePersistence,
                notificationCreator
        );

        downloadBatch.persist();
        download(downloadBatch, downloadBatchMap);
    }

    public void download(DownloadBatch downloadBatch, Map<DownloadBatchId, DownloadBatch> downloadBatchMap) {
        downloadBatchMap.put(downloadBatch.getId(), downloadBatch);
        if (downloadService == null) {
            ensureDownloadServiceExistsAndDownload(downloadBatch);
        } else {
            executeDownload(downloadBatch);
        }
    }

    private void ensureDownloadServiceExistsAndDownload(final DownloadBatch downloadBatch) {
        executor.submit(new Runnable() {
            @Override
            public void run() {
                waitForDownloadService();
                executeDownload(downloadBatch);
            }
        });
    }

    private void waitForDownloadService() {
        try {
            synchronized (waitForDownloadService) {
                if (downloadService == null) {
                    waitForDownloadService.wait();
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void executeDownload(final DownloadBatch downloadBatch) {
        updateStatusToQueuedIfNeeded(downloadBatch);
        downloadService.download(downloadBatch, downloadBatchCallback());
    }

    private void updateStatusToQueuedIfNeeded(DownloadBatch downloadBatch) {
        DownloadBatchStatus downloadBatchStatus = downloadBatch.status();

        if (!downloadBatchStatus.isMarkedAsPaused()) {
            downloadBatchStatus.markAsQueued(downloadsBatchPersistence);
        }
    }

    private DownloadBatchCallback downloadBatchCallback() {
        return new DownloadBatchCallback() {
            @Override
            public void onUpdate(final DownloadBatchStatus downloadBatchStatus) {
                callbackHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        for (DownloadBatchCallback callback : callbacks) {
                            callback.onUpdate(downloadBatchStatus);
                        }
                        updateNotification(downloadBatchStatus, downloadService);
                    }
                });
            }
        };
    }

    private void updateNotification(DownloadBatchStatus downloadBatchStatus, DownloadServiceCommands downloadService) {
        NotificationInformation notificationInformation = downloadBatchStatus.createNotification();
        if (downloadBatchStatus.isMarkedAsDownloading()) {
            downloadService.updateNotification(notificationInformation);
        } else {
            downloadService.makeNotificationDismissible(notificationInformation);
        }
    }

    void setDownloadService(DownloadServiceCommands downloadService) {
        this.downloadService = downloadService;
    }
}
