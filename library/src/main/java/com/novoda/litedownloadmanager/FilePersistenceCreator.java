package com.novoda.litedownloadmanager;

import android.content.Context;

import com.novoda.notils.logger.simple.Log;

class FilePersistenceCreator {
    private final Context context;
    private final FilePersistenceType type;

    FilePersistenceCreator(Context context, FilePersistenceType type) {
        this.context = context.getApplicationContext();
        this.type = type;
    }

    FilePersistence create() {
        return create(type);
    }

    FilePersistence create(FilePersistenceType type) {
        switch (type) {
            case INTERNAL:
                return new InternalPhysicalFilePersistence(context);
            case EXTERNAL:
                return new ExternalPhysicalFilePersistence(context);
            case CUSTOM:
                Log.w("Persistence of type CUSTOM is not yet implemented");
                throw new IllegalStateException("Persistence of type CUSTOM is not yet implemented");
            default:
                Log.e("Persistence of type " + type + " is not supported");
                throw new IllegalStateException("Persistence of type " + type + " is not supported");
        }
    }
}
