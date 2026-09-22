package com.example.android_assign;

import android.os.Bundle;
import android.widget.RadioGroup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;

import java.util.Locale;

public class SettingsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        findViewById(R.id.back_button).setOnClickListener(view -> finish());
        RadioGroup choices = findViewById(R.id.locale_choices);
        LocaleListCompat current = AppCompatDelegate.getApplicationLocales();
        Locale requested = current.isEmpty()
                ? getResources().getConfiguration().getLocales().get(0)
                : current.get(0);
        String tag = requested.getLanguage();
        if (tag.startsWith("en")) {
            choices.check(R.id.locale_us);
        } else if (tag.startsWith("ja")) {
            choices.check(R.id.locale_jp);
        } else {
            choices.check(R.id.locale_vn);
        }

        choices.setOnCheckedChangeListener((group, checkedId) -> {
            String newTag = checkedId == R.id.locale_us ? "en-US"
                    : checkedId == R.id.locale_jp ? "ja-JP" : "vi-VN";
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(newTag));
        });
    }
}
