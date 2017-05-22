package com.novoda.litedownloadmanager;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;

@Dao
interface RoomBatchDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(RoomBatch roomBatch);

    @Query("SELECT * FROM RoomBatch")
    List<RoomBatch> loadAll();
}
