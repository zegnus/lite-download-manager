package com.novoda.litedownloadmanager;

import java.util.List;

public interface LiteDownloadManagerCommands {

    DownloadBatchId download(Batch batch);

    void pause(DownloadBatchId downloadBatchId);

    void resume(DownloadBatchId downloadBatchId);

    void delete(DownloadBatchId downloadBatchId);

    void addDownloadBatchCallback(DownloadBatchCallback downloadBatchCallback);

    void removeDownloadBatchCallback(DownloadBatchCallback downloadBatchCallback);

    List<DownloadBatchStatus> getAllDownloadBatchStatuses();
}
