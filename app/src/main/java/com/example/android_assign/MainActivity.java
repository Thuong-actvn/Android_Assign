package com.example.android_assign;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Lấy fragment bản đồ trong layout và chờ bản đồ sẵn sàng.
        SupportMapFragment mapFragment = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        // 1. Áp dụng giao diện Retro từ file JSON.
        try {
            boolean success = googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style));
            if (!success) {
                Log.e("CityTour", "Không thể áp dụng giao diện bản đồ.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e("CityTour", "Không tìm thấy file map_style.json.", e);
        }

        // 2. Khai báo tọa độ của 3 địa điểm tại TP. Hồ Chí Minh.
        LatLng choBenThanh = new LatLng(10.7725, 106.6980);
        LatLng dinhDocLap = new LatLng(10.7777, 106.6961);
        LatLng benNhaRong = new LatLng(10.7682, 106.7069);

        // 3. Thêm marker với icon tùy chỉnh. Chạm marker để xem tên địa điểm.
        BitmapDescriptor icon = createMarkerIcon();
        googleMap.addMarker(new MarkerOptions().position(choBenThanh)
                .title(getString(R.string.ben_thanh_name)).icon(icon));
        googleMap.addMarker(new MarkerOptions().position(dinhDocLap)
                .title(getString(R.string.independence_palace_name)).icon(icon));
        googleMap.addMarker(new MarkerOptions().position(benNhaRong)
                .title(getString(R.string.nha_rong_name)).icon(icon));

        googleMap.getUiSettings().setZoomControlsEnabled(true);

        // 4. Tạo vùng chứa cả 3 điểm và zoom sau khi bản đồ tải xong.
        LatLngBounds bounds = new LatLngBounds.Builder()
                .include(choBenThanh).include(dinhDocLap).include(benNhaRong).build();
        googleMap.setOnMapLoadedCallback(() -> {
            int padding = (int) (48 * getResources().getDisplayMetrics().density);
            googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding));
        });
    }

    // Google Maps cần Bitmap, nên chuyển icon vector thành Bitmap trước.
    private BitmapDescriptor createMarkerIcon() {
        Drawable drawable = getDrawable(R.drawable.ic_tour_marker);
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }
}
