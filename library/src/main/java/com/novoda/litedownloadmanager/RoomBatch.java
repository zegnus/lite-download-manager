package com.novoda.litedownloadmanager;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;

@Entity(indices={@Index("batch_id")})
class RoomBatch {

    @PrimaryKey
    @ColumnInfo(name = "batch_id")
    public int downloadBatchId;

    @ColumnInfo(name = "batch_title")
    public String downloadBatchTitle;

    @ColumnInfo(name = "batch_status")
    public String status;
}
