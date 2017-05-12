package com.novoda.library;

import android.content.Context;

interface DownloadServiceCommands {

    void download(DownloadBatch downloadBatch, DownloadBatch.Callback callback, Context context);
}
