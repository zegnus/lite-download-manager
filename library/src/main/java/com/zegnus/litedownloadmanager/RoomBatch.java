package com.zegnus.litedownloadmanager;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(indices={@Index("batch_id")})
class RoomBatch {

    @NonNull
    @PrimaryKey
    @ColumnInfo(name = "batch_id")
    public String id;

    @ColumnInfo(name = "batch_title")
    public String title;

    @ColumnInfo(name = "batch_status")
    public String status;
}
