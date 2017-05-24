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
    private final FileSizeRequester fileSizeRequester;
    private final PersistenceCreator persistenceCreator;
    private final Downloader downloader;
    private final DownloadsBatchPersistence downloadsBatchPersistence;
    private final DownloadsFilePersistence downloadsFilePersistence;

    private DownloadServiceCommands downloadService;

    LiteDownloadManager(Handler callbackHandler,
                        Map<DownloadBatchId, DownloadBatch> downloadBatchMap,
                        List<DownloadBatchCallback> callbacks,
                        FileSizeRequester fileSizeRequester,
                        PersistenceCreator persistenceCreator,
                        Downloader downloader,
                        DownloadsBatchPersistence downloadsBatchPersistence,
                        DownloadsFilePersistence downloadsFilePersistence) {
        this.callbackHandler = callbackHandler;
        this.downloadBatchMap = downloadBatchMap;
        this.callbacks = callbacks;
        this.fileSizeRequester = fileSizeRequester;
        this.persistenceCreator = persistenceCreator;
        this.downloader = downloader;
        this.downloadsBatchPersistence = downloadsBatchPersistence;
        this.downloadsFilePersistence = downloadsFilePersistence;
    }

    void initialise(final DownloadServiceCommands downloadService) {
        downloadsBatchPersistence.loadAsync(
                fileSizeRequester,
                persistenceCreator,
                downloader,
                downloadsBatchPersistence,
                new DownloadsBatchPersistence.LoadBatchesCallback() {
                    @Override
                    public void onLoaded(List<DownloadBatch> downloadBatches) {
                        for (DownloadBatch downloadBatch : downloadBatches) {
                            download(downloadBatch);
                        }

                        setDownloadService(downloadService);
                    }
                }
        );
    }

    private void setDownloadService(DownloadServiceCommands downloadService) {
        this.downloadService = downloadService;
        synchronized (WAIT_FOR_DOWNLOAD_SERVICE) {
            WAIT_FOR_DOWNLOAD_SERVICE.notifyAll();
        }
    }

    @Override
    public DownloadBatchId download(Batch batch) {
        DownloadBatch downloadBatch = DownloadBatch.newInstance(
                batch,
                fileSizeRequester,
                persistenceCreator,
                downloader,
                downloadsBatchPersistence,
                downloadsFilePersistence);
        downloadBatch.persist();
        download(downloadBatch);
        return downloadBatch.getId();
    }

    private void download(DownloadBatch downloadBatch) {
        downloadBatchMap.put(downloadBatch.getId(), downloadBatch);
        if (downloadService == null) {
            ensureDownloadServiceExistsAndProceed(downloadBatch);
        } else {
            executeDownload(downloadBatch);
        }
    }

    private void ensureDownloadServiceExistsAndProceed(final DownloadBatch downloadBatch) {
        EXECUTOR.submit(new Runnable() {
            @Override
            public void run() {
                waitForDownloadService();
                executeDownload(downloadBatch);
            }
        });
    }

    private void executeDownload(final DownloadBatch downloadBatch) {
        downloadService.download(downloadBatch, new DownloadBatchCallback() {
            @Override
            public void onUpdate(final DownloadBatchStatus downloadBatchStatus) {
                callbackHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        for (DownloadBatchCallback callback : callbacks) {
                            callback.onUpdate(downloadBatchStatus);
                        }
                    }
                });
            }
        });
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
    public List<DownloadBatchStatus> getAllDownloadBatchStatuses() {
        List<DownloadBatchStatus> downloadBatchStatuses = new ArrayList<>(downloadBatchMap.size());

        for (DownloadBatch downloadBatch : downloadBatchMap.values()) {
            downloadBatchStatuses.add(downloadBatch.getDownloadBatchStatus());
        }

        return downloadBatchStatuses;
    }
}
