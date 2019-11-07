package com.zegnus.litedownloadmanager;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {RoomBatch.class, RoomFile.class}, version = 1, exportSchema = false)
abstract class RoomAppDatabase extends RoomDatabase {

    abstract RoomBatchDao roomBatchDao();

    abstract RoomFileDao roomFileDao();
}
