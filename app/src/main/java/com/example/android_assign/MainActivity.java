package com.example.android_assign;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;

import java.text.NumberFormat;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    // Giá mẫu cho cùng bốn sản phẩm ở Việt Nam, Mỹ và Nhật Bản.
    private static final double[] PRICES_VN = {35000, 28000, 12000, 45000};
    private static final double[] PRICES_US = {1.50, 2.25, 0.75, 3.50};
    private static final double[] PRICES_JP = {220, 300, 120, 480};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.settings_button).setOnClickListener(
                view -> startActivity(new Intent(this, SettingsActivity.class)));
        showProducts();
    }

    private void showProducts() {
        LocaleListCompat selectedLocales = AppCompatDelegate.getApplicationLocales();
        Locale requested = selectedLocales.isEmpty()
                ? getResources().getConfiguration().getLocales().get(0)
                : selectedLocales.get(0);
        String tag = requested.getLanguage().equals("en") ? "en-US"
                : requested.getLanguage().equals("ja") ? "ja-JP" : "vi-VN";
        Locale locale = Locale.forLanguageTag(tag);
        NumberFormat money = NumberFormat.getCurrencyInstance(locale);

        double[] prices;
        switch (locale.getLanguage()) {
            case "en":
                prices = PRICES_US;
                break;
            case "ja":
                prices = PRICES_JP;
                break;
            default:
                prices = PRICES_VN;
                break;
        }

        String[] products = getResources().getStringArray(R.array.products);
        LinearLayout list = findViewById(R.id.product_list);
        list.removeAllViews();
        int spacing = Math.round(12 * getResources().getDisplayMetrics().density);
        double total = 0;

        for (int i = 0; i < products.length; i++) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(0, spacing, 0, spacing);

            TextView name = new TextView(this);
            name.setText(products[i]);
            name.setTextSize(18);
            row.addView(name, new LinearLayout.LayoutParams(0,
                    LinearLayout.LayoutParams.WRAP_CONTENT, 1));

            TextView price = new TextView(this);
            price.setText(money.format(prices[i]));
            price.setTextSize(18);
            price.setPadding(spacing, 0, 0, 0);
            row.addView(price);

            list.addView(row);
            total += prices[i];
        }

        TextView totalView = findViewById(R.id.total_amount);
        totalView.setText(getString(R.string.total_amount, money.format(total)));
    }
}
