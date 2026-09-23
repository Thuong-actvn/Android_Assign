package com.example.android_assign;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.widget.ListView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    private static final long UPDATE_INTERVAL_MS = 3_000L;
    private static final float MIN_DISTANCE_METERS = 1f;

    private final ArrayList<LocationRecord> locationRecords = new ArrayList<>();
    private final ExecutorService geocoderExecutor = Executors.newSingleThreadExecutor();

    private LocationManager locationManager;
    private LocationAdapter locationAdapter;
    private TextView statusText;
    private MaterialButton toggleButton;
    private boolean isListening;
    private boolean wantsUpdates = true;
    private boolean permissionWasRequested;

    private final LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(@NonNull Location location) {
            addLocationToList(location);
        }
    };

    private final ActivityResultLauncher<String[]> permissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                permissionWasRequested = true;
                if (hasLocationPermission()) {
                    startLocationUpdates();
                } else {
                    showPermissionDenied();
                }
            });

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

        locationManager = getSystemService(LocationManager.class);
        statusText = findViewById(R.id.statusText);
        toggleButton = findViewById(R.id.toggleButton);

        ListView locationList = findViewById(R.id.locationList);
        locationAdapter = new LocationAdapter(this, locationRecords);
        locationList.setAdapter(locationAdapter);
        locationList.setEmptyView(findViewById(R.id.emptyText));

        toggleButton.setOnClickListener(v -> {
            if (isListening) {
                wantsUpdates = false;
                stopLocationUpdates();
                statusText.setText(R.string.status_stopped);
                toggleButton.setText(R.string.action_start);
            } else {
                wantsUpdates = true;
                if (hasLocationPermission()) {
                    startLocationUpdates();
                } else {
                    requestLocationPermission();
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!wantsUpdates) {
            return;
        }

        if (hasLocationPermission()) {
            startLocationUpdates();
        } else if (!permissionWasRequested) {
            requestLocationPermission();
        } else {
            showPermissionDenied();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (isListening) {
            locationManager.removeUpdates(locationListener);
            isListening = false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        geocoderExecutor.shutdownNow();
    }

    private void requestLocationPermission() {
        statusText.setText(R.string.status_waiting_permission);
        permissionLauncher.launch(new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        });
    }

    private boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    private boolean hasFineLocationPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void startLocationUpdates() {
        if (!hasLocationPermission()) {
            return;
        }

        boolean requestedProvider = false;
        try {
            if (hasFineLocationPermission()
                    && locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        UPDATE_INTERVAL_MS,
                        MIN_DISTANCE_METERS,
                        locationListener,
                        Looper.getMainLooper());
                requestedProvider = true;
            }

            if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                locationManager.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER,
                        UPDATE_INTERVAL_MS,
                        MIN_DISTANCE_METERS,
                        locationListener,
                        Looper.getMainLooper());
                requestedProvider = true;
            }
        } catch (SecurityException ignored) {
            showPermissionDenied();
            return;
        }

        isListening = requestedProvider;
        if (requestedProvider) {
            statusText.setText(R.string.status_updating);
            toggleButton.setText(R.string.action_stop);
        } else {
            statusText.setText(R.string.status_provider_off);
            toggleButton.setText(R.string.action_start);
        }
    }

    private void stopLocationUpdates() {
        if (isListening) {
            locationManager.removeUpdates(locationListener);
            isListening = false;
        }
    }

    private void showPermissionDenied() {
        statusText.setText(R.string.status_permission_denied);
        toggleButton.setText(R.string.action_start);
    }

    private void addLocationToList(Location location) {
        long recordedAt = location.getTime() > 0 ? location.getTime() : System.currentTimeMillis();
        String formattedTime = new SimpleDateFormat(
                "dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(new Date(recordedAt));

        LocationRecord record = new LocationRecord(
                location.getLatitude(),
                location.getLongitude(),
                getString(R.string.address_loading),
                formattedTime);

        locationRecords.add(0, record);
        locationAdapter.notifyDataSetChanged();
        findAddress(record);
    }

    private void findAddress(LocationRecord record) {
        geocoderExecutor.execute(() -> {
            String result = getString(R.string.address_unknown);
            if (Geocoder.isPresent()) {
                Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                try {
                    List<Address> addresses = geocoder.getFromLocation(
                            record.latitude, record.longitude, 1);
                    if (addresses != null && !addresses.isEmpty()) {
                        Address address = addresses.get(0);
                        String addressLine = address.getAddressLine(0);
                        if (addressLine != null && !addressLine.isBlank()) {
                            result = addressLine;
                        }
                    }
                } catch (IOException | IllegalArgumentException ignored) {
                    // Giữ nội dung "Không tìm thấy địa chỉ" nếu Geocoder thất bại.
                }
            }

            String finalResult = result;
            runOnUiThread(() -> {
                record.address = finalResult;
                locationAdapter.notifyDataSetChanged();
            });
        });
    }
}
