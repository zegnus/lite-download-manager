package com.novoda.library;

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

    Persistence create() {
        switch (type) {
            case INTERNAL:
                return new InternalPhysicalPersistence(context);
            case EXTERNAL:
                return new ExternalPhysicalPersistence(context);
            case CUSTOM:
                // TODO
                Log.w("Persistance of type CUSTOM is not yet implemented");
                throw new IllegalStateException("Persistence of type CUSTOM is not yet implemented");
            default:
                Log.e("Persistance of type " + type + " is not supported");
                throw new IllegalStateException("Persistence of type " + type + " is not supported");
        }
    }
}
