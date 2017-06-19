package com.novoda.litedownloadmanager;

import android.os.Handler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class LiteDownloadManager implements LiteDownloadManagerCommands {

    private static final Object WAIT_FOR_DOWNLOAD_SERVICE = new Object();
    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();

    private final Handler callbackHandler;
    private final Map<DownloadBatchId, DownloadBatch> downloadBatchMap;
    private final List<DownloadBatchCallback> callbacks;
    private final FileOperations fileOperations;
    private final DownloadsBatchPersistence downloadsBatchPersistence;
    private final DownloadsFilePersistence downloadsFilePersistence;
    private final NotificationCreator notificationCreator;

    private DownloadServiceCommands downloadService;

    LiteDownloadManager(Handler callbackHandler,
                        Map<DownloadBatchId, DownloadBatch> downloadBatchMap,
                        List<DownloadBatchCallback> callbacks,
                        FileOperations fileOperations,
                        DownloadsBatchPersistence downloadsBatchPersistence,
                        DownloadsFilePersistence downloadsFilePersistence,
                        NotificationCreator notificationCreator) {
        this.callbackHandler = callbackHandler;
        this.downloadBatchMap = downloadBatchMap;
        this.callbacks = callbacks;
        this.fileOperations = fileOperations;
        this.downloadsBatchPersistence = downloadsBatchPersistence;
        this.downloadsFilePersistence = downloadsFilePersistence;
        this.notificationCreator = notificationCreator;
    }

    void initialise(final DownloadServiceCommands downloadService) {
        setDownloadService(downloadService);
    }

    private void setDownloadService(DownloadServiceCommands downloadService) {
        this.downloadService = downloadService;
        synchronized (WAIT_FOR_DOWNLOAD_SERVICE) {
            WAIT_FOR_DOWNLOAD_SERVICE.notifyAll();
        }
    }

    @Override
    public void submitAllStoredDownloads(final AllStoredDownloadsSubmittedCallback callback) {
        downloadsBatchPersistence.loadAsync(
                fileOperations,
                notificationCreator,
                new DownloadsBatchPersistence.LoadBatchesCallback() {
                    @Override
                    public void onLoaded(List<DownloadBatch> downloadBatches) {
                        for (DownloadBatch downloadBatch : downloadBatches) {
                            DownloadBatchId id = downloadBatch.getId();
                            if (downloadBatchMap.containsKey(id)) {
                                resume(id);
                            } else {
                                download(downloadBatch);
                            }
                        }

                        callback.onAllDownloadsSubmited();
                    }
                }
        );
    }

    @Override
    public void download(Batch batch) {
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
        download(downloadBatch);
    }

    private void download(DownloadBatch downloadBatch) {
        downloadBatchMap.put(downloadBatch.getId(), downloadBatch);
        if (downloadService == null) {
            ensureDownloadServiceExistsAndDownload(downloadBatch);
        } else {
            executeDownload(downloadBatch);
        }
    }

    private void ensureDownloadServiceExistsAndDownload(final DownloadBatch downloadBatch) {
        EXECUTOR.submit(new Runnable() {
            @Override
            public void run() {
                waitForDownloadService();
                executeDownload(downloadBatch);
            }
        });
    }

    private void executeDownload(final DownloadBatch downloadBatch) {
        updateStatusToQueuedIfNeeded(downloadBatch);
        downloadService.download(downloadBatch, new DownloadBatchCallback() {
            @Override
            public void onUpdate(final DownloadBatchStatus downloadBatchStatus) {
                callbackHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        for (DownloadBatchCallback callback : callbacks) {
                            callback.onUpdate(downloadBatchStatus);
                        }
                        updateNotification(downloadBatchStatus);
                    }
                });
            }
        });
    }

    private void updateStatusToQueuedIfNeeded(DownloadBatch downloadBatch) {
        DownloadBatchStatus downloadBatchStatus = downloadBatch.status();

        if (!downloadBatchStatus.isMarkedAsPaused()) {
            downloadBatchStatus.markAsQueued(downloadsBatchPersistence);
        }
    }

    private void updateNotification(DownloadBatchStatus downloadBatchStatus) {
        NotificationInformation notificationInformation = downloadBatchStatus.createNotification();
        if (downloadBatchStatus.isMarkedAsDownloading()) {
            downloadService.updateNotification(notificationInformation);
        } else {
            downloadService.makeNotificationDismissible(notificationInformation);
        }
    }

    private void waitForDownloadService() {
        if (downloadService == null) {
            try {
                synchronized (WAIT_FOR_DOWNLOAD_SERVICE) {
                    WAIT_FOR_DOWNLOAD_SERVICE.wait();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void pause(DownloadBatchId downloadBatchId) {
        DownloadBatch downloadBatch = downloadBatchMap.get(downloadBatchId);
        if (downloadBatch == null) {
            return;
        }
        downloadBatch.pause();
    }

    @Override
    public void resume(DownloadBatchId downloadBatchId) {
        DownloadBatch downloadBatch = downloadBatchMap.get(downloadBatchId);
        if (downloadBatch == null) {
            return;
        }
        downloadBatchMap.remove(downloadBatchId);
        downloadBatch.resume();

        download(downloadBatch);
    }

    @Override
    public void delete(DownloadBatchId downloadBatchId) {
        DownloadBatch downloadBatch = downloadBatchMap.get(downloadBatchId);
        if (downloadBatch == null) {
            return;
        }
        downloadBatchMap.remove(downloadBatchId);
        downloadBatch.delete();
    }

    @Override
    public void addDownloadBatchCallback(DownloadBatchCallback downloadBatchCallback) {
        callbacks.add(downloadBatchCallback);
    }

    @Override
    public void removeDownloadBatchCallback(DownloadBatchCallback downloadBatchCallback) {
        if (callbacks.contains(downloadBatchCallback)) {
            callbacks.remove(downloadBatchCallback);
        }
    }

    @Override
    public void getAllDownloadBatchStatuses(AllBatchStatusesCallback callback) {
        if (downloadService == null) {
            ensureDownloadServiceExistsAndGetAllDownloadBatchStatuses(callback);
        } else {
            executeGetAllDownloadBatchStatuses(callback);
        }
    }

    private void ensureDownloadServiceExistsAndGetAllDownloadBatchStatuses(final AllBatchStatusesCallback callback) {
        EXECUTOR.submit(new Runnable() {
            @Override
            public void run() {
                waitForDownloadService();
                executeGetAllDownloadBatchStatuses(callback);
            }
        });
    }

    private void executeGetAllDownloadBatchStatuses(AllBatchStatusesCallback callback) {
        List<DownloadBatchStatus> downloadBatchStatuses = new ArrayList<>(downloadBatchMap.size());

        for (DownloadBatch downloadBatch : downloadBatchMap.values()) {
            downloadBatchStatuses.add(downloadBatch.status());
        }

        callback.onReceived(downloadBatchStatuses);
    }
}
