package com.example.android_assign;

import android.app.Application;

import androidx.lifecycle.LiveData;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NoteRepository {
    private final NoteDao noteDao;
    private final LiveData<List<Note>> allNotes;

    // Dùng chung một luồng nền để thêm, sửa, xóa ghi chú.
    private static final ExecutorService databaseExecutor = Executors.newSingleThreadExecutor();

    public NoteRepository(Application application) {
        noteDao = AppDatabase.getInstance(application).noteDao();
        allNotes = noteDao.getAllNotes();
    }

    public LiveData<List<Note>> getAllNotes() {
        return allNotes;
    }

    public void insert(Note note) {
        databaseExecutor.execute(() -> noteDao.insert(note));
    }

    public void update(Note note) {
        databaseExecutor.execute(() -> noteDao.update(note));
    }

    public void delete(Note note) {
        databaseExecutor.execute(() -> noteDao.delete(note));
    }
}
