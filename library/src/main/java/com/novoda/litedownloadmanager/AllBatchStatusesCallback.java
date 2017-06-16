package com.novoda.litedownloadmanager;

import java.util.List;

public interface AllBatchStatusesCallback {

    void onReceived(List<DownloadBatchStatus> downloadBatchStatuses);
}
