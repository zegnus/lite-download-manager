package com.zegnus.litedownloadmanager;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
interface RoomFileDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(RoomFile roomFile);

    @Query("SELECT * FROM RoomFile WHERE RoomFile.batch_id = :batchId")
    List<RoomFile> loadAllFilesFor(String batchId);
}
