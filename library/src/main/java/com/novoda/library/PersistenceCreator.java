package com.novoda.library;

import android.content.Context;

class PersistenceCreator {

    static Persistence createInternalPhysicalPersistence(Context context) {

        return new InternalPhysicalPersistence(context);
    }
}
