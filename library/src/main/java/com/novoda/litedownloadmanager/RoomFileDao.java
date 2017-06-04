package com.novoda.litedownloadmanager;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;

@Dao
interface RoomFileDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(RoomFile roomFile);

    @Query("SELECT * FROM RoomFile WHERE RoomFile.batch_id = :batchId")
    List<RoomFile> loadAllFilesFor(int batchId);
}
