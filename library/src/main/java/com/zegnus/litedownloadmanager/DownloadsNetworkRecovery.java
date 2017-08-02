package com.zegnus.litedownloadmanager;

interface DownloadsNetworkRecovery {

    void scheduleRecovery();

    DownloadsNetworkRecovery DISABLED = new DownloadsNetworkRecovery() {
        @Override
        public void scheduleRecovery() {
            // do-nothing
        }
    };
}
