package com.novoda.litedownloadmanager;

import android.content.Context;

import com.novoda.notils.logger.simple.Log;

class PersistenceCreator {

    enum Type {
        INTERNAL,
        EXTERNAL,
        CUSTOM
    }

    private final Context context;
    private final Type type;

    PersistenceCreator(Context context, Type type) {
        this.context = context.getApplicationContext();
        this.type = type;
    }

    FilePersistence create() {
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
