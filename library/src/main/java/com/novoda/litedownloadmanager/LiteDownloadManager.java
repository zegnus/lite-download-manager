package com.novoda.litedownloadmanager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

class LiteDownloadManager implements LiteDownloadManagerCommands {

    private final Object waitForDownloadService;
    private final ExecutorService executor;
    private final Map<DownloadBatchId, DownloadBatch> downloadBatchMap;
    private final List<DownloadBatchCallback> callbacks;
    private final FileOperations fileOperations;
    private final DownloadsBatchPersistence downloadsBatchPersistence;
    private final NotificationCreator notificationCreator;
    private final LiteDownloadManagerDownloader downloader;

    private DownloadService downloadService;

    LiteDownloadManager(Object waitForDownloadService,
                        ExecutorService executor,
                        Map<DownloadBatchId, DownloadBatch> downloadBatchMap,
                        List<DownloadBatchCallback> callbacks,
                        FileOperations fileOperations,
                        DownloadsBatchPersistence downloadsBatchPersistence,
                        NotificationCreator notificationCreator,
                        LiteDownloadManagerDownloader downloader) {
        this.waitForDownloadService = waitForDownloadService;
        this.executor = executor;
        this.downloadBatchMap = downloadBatchMap;
        this.callbacks = callbacks;
        this.fileOperations = fileOperations;
        this.downloadsBatchPersistence = downloadsBatchPersistence;
        this.notificationCreator = notificationCreator;
        this.downloader = downloader;
    }

    void initialise(DownloadService downloadService) {
        setDownloadService(downloadService);
    }

    private void setDownloadService(DownloadService downloadService) {
        this.downloadService = downloadService;
        downloader.setDownloadService(downloadService);
        synchronized (waitForDownloadService) {
            waitForDownloadService.notifyAll();
        }
    }

    @Override
    public void submitAllStoredDownloads(AllStoredDownloadsSubmittedCallback callback) {
        downloadsBatchPersistence.loadAsync(fileOperations, notificationCreator, loadBatchesCallback(callback));
    }

    private DownloadsBatchPersistence.LoadBatchesCallback loadBatchesCallback(final AllStoredDownloadsSubmittedCallback callback) {
        return new DownloadsBatchPersistence.LoadBatchesCallback() {
            @Override
            public void onLoaded(List<DownloadBatch> downloadBatches) {
                for (DownloadBatch downloadBatch : downloadBatches) {
                    DownloadBatchId id = downloadBatch.getId();
                    if (downloadBatchMap.containsKey(id)) {
                        resume(id);
                    } else {
                        downloader.download(downloadBatch, downloadBatchMap);
                    }
                }

                callback.onAllDownloadsSubmitted();
            }
        };
    }

    @Override
    public void download(Batch batch) {
        downloader.download(batch, downloadBatchMap);
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

        downloader.download(downloadBatch, downloadBatchMap);
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
        executor.submit(new Runnable() {
            @Override
            public void run() {
                waitForDownloadService();
                executeGetAllDownloadBatchStatuses(callback);
            }
        });
    }

    private void waitForDownloadService() {
        try {
            synchronized (waitForDownloadService) {
                waitForDownloadService.wait();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void executeGetAllDownloadBatchStatuses(AllBatchStatusesCallback callback) {
        List<DownloadBatchStatus> downloadBatchStatuses = new ArrayList<>(downloadBatchMap.size());

        for (DownloadBatch downloadBatch : downloadBatchMap.values()) {
            downloadBatchStatuses.add(downloadBatch.status());
        }

        callback.onReceived(downloadBatchStatuses);
    }
}
