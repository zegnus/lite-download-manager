package com.zegnus.litedownloadmanager;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import static androidx.room.ForeignKey.CASCADE;

@Entity(
        foreignKeys = @ForeignKey(entity = RoomBatch.class, parentColumns = "batch_id", childColumns = "batch_id", onDelete = CASCADE),
        indices = {@Index("batch_id")}
)
class RoomFile {

    @NonNull
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
