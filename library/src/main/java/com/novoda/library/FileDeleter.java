package com.novoda.library;

import com.novoda.notils.logger.simple.Log;

class FileDeleter {

    void delete(DownloadFileId downloadFileId) {
        Log.v("FileDeleter.delete: " + downloadFileId.toRawId());
    }
}
