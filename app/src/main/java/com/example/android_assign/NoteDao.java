package com.example.android_assign;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface NoteDao {
    // Thêm/sửa/xóa chạy ở luồng nền trong Java; nếu dùng Kotlin thì khai báo suspend.
    @Insert
    void insert(Note note);

    @Update
    void update(Note note);

    @Delete
    void delete(Note note);

    // Danh sách tự cập nhật bằng LiveData trong Java; nếu dùng Kotlin thì trả về Flow<List<Note>>.
    @Query("SELECT * FROM notes ORDER BY createdAt DESC, id DESC")
    LiveData<List<Note>> getAllNotes();
}
