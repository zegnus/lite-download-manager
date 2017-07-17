package com.zegnus.litedownloadmanager;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

@Database(entities = {RoomBatch.class, RoomFile.class}, version = 1)
abstract class RoomAppDatabase extends RoomDatabase {

    abstract RoomBatchDao roomBatchDao();

    abstract RoomFileDao roomFileDao();
}
