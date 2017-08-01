package com.zegnus.litedownloadmanager;

import android.content.Context;

class DownloadsNetworkRecoveryCreator {

    private static DownloadsNetworkRecovery INSTANCE;

    static DownloadsNetworkRecovery createDisabled() {
        return DownloadsNetworkRecovery.DISABLED;
    }

    static void createEnabled(Context context, LiteDownloadManager liteDownloadManager, ConnectionType connectionType) {
        DownloadsNetworkRecoveryCreator.INSTANCE = new LiteDownloadsNetworkRecoveryEnabled(context, liteDownloadManager, connectionType);
    }

    static DownloadsNetworkRecovery getInstance() {
        if (INSTANCE == null) {
            throw new IllegalStateException("There is no instance available, make sure you call DownloadsNetworkRecoveryCreator.create(...) first");
        } else {
            return INSTANCE;
        }
    }
}
