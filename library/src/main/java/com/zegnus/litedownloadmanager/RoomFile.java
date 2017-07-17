package com.zegnus.litedownloadmanager;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;

import static android.arch.persistence.room.ForeignKey.CASCADE;

@Entity(
        foreignKeys = @ForeignKey(entity = RoomBatch.class, parentColumns = "batch_id", childColumns = "batch_id", onDelete = CASCADE),
        indices = {@Index("batch_id")}
)
class RoomFile {

    @PrimaryKey
    @ColumnInfo(name = "file_id")
    String id;

    @ColumnInfo(name = "batch_id")
    String batchId;

    @ColumnInfo(name = "file_name")
    String name;

    @ColumnInfo(name = "file_path")
    String path;

    @ColumnInfo(name = "total_size")
    long totalSize;

    @ColumnInfo(name = "url")
    String url;

    @ColumnInfo(name = "persistence_type")
    String persistenceType;
}
