package com.zegnus.litedownloadmanager;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
interface RoomBatchDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(RoomBatch roomBatch);

    @Query("SELECT * FROM RoomBatch")
    List<RoomBatch> loadAll();

    @Query("SELECT * FROM RoomBatch WHERE RoomBatch.batch_id = :batchId")
    RoomBatch load(String batchId);

    @Delete
    void delete(RoomBatch... roomBatches);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void update(RoomBatch... roomBatches);
}
