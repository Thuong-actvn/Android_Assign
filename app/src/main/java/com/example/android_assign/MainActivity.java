package com.example.android_assign;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class MainActivity extends AppCompatActivity {
    private EditText titleInput;
    private EditText contentInput;
    private NoteViewModel noteViewModel;
    private NoteAdapter noteAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        View root = findViewById(R.id.main);
        int padding = root.getPaddingLeft();
        ViewCompat.setOnApplyWindowInsetsListener(root, (view, windowInsets) -> {
            Insets bars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(padding + bars.left, padding + bars.top,
                    padding + bars.right, padding + bars.bottom);
            return windowInsets;
        });

        titleInput = findViewById(R.id.titleInput);
        contentInput = findViewById(R.id.contentInput);
        RecyclerView notesRecyclerView = findViewById(R.id.notesRecyclerView);
        TextView emptyText = findViewById(R.id.emptyText);

        noteAdapter = new NoteAdapter();
        notesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        notesRecyclerView.setAdapter(noteAdapter);
        notesRecyclerView.addItemDecoration(
                new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        noteViewModel = new ViewModelProvider(this).get(NoteViewModel.class);

        // LiveData tự cập nhật danh sách khi dữ liệu trong Room thay đổi.
        noteViewModel.getAllNotes().observe(this, notes -> {
            noteAdapter.setNotes(notes);
            emptyText.setVisibility(notes.isEmpty() ? View.VISIBLE : View.GONE);
        });

        findViewById(R.id.addButton).setOnClickListener(v -> addNote());

        // Vuốt ghi chú sang trái để xóa qua ViewModel.
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(
                new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
                    @Override
                    public boolean onMove(@NonNull RecyclerView recyclerView,
                                          @NonNull RecyclerView.ViewHolder viewHolder,
                                          @NonNull RecyclerView.ViewHolder target) {
                        return false;
                    }

                    @Override
                    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                        int position = viewHolder.getBindingAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            Note note = noteAdapter.getNote(position);
                            noteViewModel.delete(note);
                            Toast.makeText(MainActivity.this, R.string.note_deleted,
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
        itemTouchHelper.attachToRecyclerView(notesRecyclerView);
    }

    private void addNote() {
        String title = titleInput.getText().toString().trim();
        String content = contentInput.getText().toString().trim();
        if (title.isEmpty()) {
            titleInput.setError(getString(R.string.title_required));
            return;
        }

        Note note = new Note();
        note.title = title;
        note.content = content;
        note.createdAt = System.currentTimeMillis();
        noteViewModel.insert(note);

        titleInput.setText("");
        contentInput.setText("");
    }
}
