package com.novoda.litedownloadmanager;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.PrimaryKey;

import static android.arch.persistence.room.ForeignKey.CASCADE;

@Entity(foreignKeys = @ForeignKey(entity = RoomBatch.class, parentColumns = "batch_id", childColumns = "batch_id", onDelete = CASCADE))
class RoomFile {

    @PrimaryKey
    @ColumnInfo(name = "file_id")
    String fileId;

    @ColumnInfo(name = "batch_id")
    String batchId;

    @ColumnInfo(name = "file_name")
    String name;

    @ColumnInfo(name = "file_status")
    String status;

    @ColumnInfo(name = "current_size")
    long currentSize;

    @ColumnInfo(name = "total_size")
    long totalSize;

    @ColumnInfo(name = "url")
    String url;
}
