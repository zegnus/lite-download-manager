package com.novoda.library;

class PersistanceCreator {

    static Persistence createInternalPhysicalPersistance() {
        return new InternalPhysicalPersistence();
    }
}
