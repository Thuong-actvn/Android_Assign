package com.example.android_assign;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "notes")
public class Note {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @NonNull
    public String title = "";

    @NonNull
    public String content = "";

    // Thời gian tạo, lưu dạng timestamp (mili giây).
    public long createdAt;
}
