package com.example.android_assign;

import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    private EditText titleInput;
    private EditText contentInput;
    private LinearLayout notesContainer;
    private NoteDao noteDao;
    private final ExecutorService databaseExecutor = Executors.newSingleThreadExecutor();

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
        notesContainer = findViewById(R.id.notesContainer);
        noteDao = AppDatabase.getInstance(getApplicationContext()).noteDao();

        findViewById(R.id.addButton).setOnClickListener(v -> addNote());
        noteDao.getAllNotes().observe(this, this::showNotes);
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
        databaseExecutor.execute(() -> noteDao.insert(note));

        titleInput.setText("");
        contentInput.setText("");
    }

    private void showNotes(List<Note> notes) {
        notesContainer.removeAllViews();
        if (notes.isEmpty()) {
            TextView emptyText = new TextView(this);
            emptyText.setText(R.string.empty_notes);
            notesContainer.addView(emptyText);
            return;
        }

        DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
        float swipeDistance = 80 * getResources().getDisplayMetrics().density;
        for (Note note : notes) {
            TextView noteView = new TextView(this);
            String date = dateFormat.format(new Date(note.createdAt));
            String noteText = note.title + "\n" + note.content + "\n"
                    + getString(R.string.created_at, date);
            noteView.setText(noteText);
            noteView.setTextSize(16);
            int padding = (int) (12 * getResources().getDisplayMetrics().density);
            noteView.setPadding(padding, padding, padding, padding);
            noteView.setBackgroundResource(android.R.drawable.list_selector_background);
            // Vuốt sang trái để xóa; vuốt dọc để cuộn danh sách.
            noteView.setOnTouchListener(new View.OnTouchListener() {
                private float startX;
                private float startY;

                @Override
                public boolean onTouch(View view, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        startX = event.getX();
                        startY = event.getY();
                        return true;
                    }
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        float deltaX = event.getX() - startX;
                        float deltaY = event.getY() - startY;
                        if (deltaX < -swipeDistance && Math.abs(deltaX) > Math.abs(deltaY)) {
                            databaseExecutor.execute(() -> noteDao.delete(note));
                            Toast.makeText(MainActivity.this, R.string.note_deleted,
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            view.performClick();
                        }
                        return true;
                    }
                    return true;
                }
            });
            notesContainer.addView(noteView);

            View divider = new View(this);
            divider.setBackgroundResource(android.R.color.darker_gray);
            notesContainer.addView(divider, new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    (int) getResources().getDisplayMetrics().density
            ));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        databaseExecutor.shutdown();
    }
}
