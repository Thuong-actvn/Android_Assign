package com.example.android_assign;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private SensorManager sensorManager;
    private Sensor senSorAccelerometer;

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

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        List<Sensor> listSensor = sensorManager.getSensorList(Sensor.TYPE_ALL);

        StringBuilder sensorText = new StringBuilder();
        for(Sensor sensor : listSensor) {
            sensorText
                    .append(sensor.getName())
                    .append(System.getProperty(System.lineSeparator()));
        }

        TextView sensorTV = findViewById(R.id.sensorTextView);
        sensorTV.setText(sensorText);

        senSorAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        TextView currentSensorTV = findViewById(R.id.CurrentSensorTextView);
        ShakeDetector shakeDetector =
                new ShakeDetector(
                      sensorManager,
                        () -> {
                            Toast.makeText(this, "Shake", Toast.LENGTH_SHORT).show();
                        },
                        currentSensorTV
                );
        shakeDetector.start();

        // Em đã test Shake trên máy thật hoạt động tốt,
        // nhưng trên Virtual Sensor thì có lắc sao thì vẫn không đủ ngưỡng lắc để hiện Toast
    }

//    @Override
//    public void onAccuracyChanged(Sensor sensor, int accuracy) {
//
//    }

//    @Override
//    public void onSensorChanged(SensorEvent event) {
//        int sensorType = event.sensor.getType();
//        float[] currentSensor = event.values;
//        TextView currentSensorTV = findViewById(R.id.CurrentSensorTextView);
//        switch (sensorType) {
//            case Sensor.TYPE_ACCELEROMETER:
//                currentSensorTV.setText(String.format(Locale.getDefault(),"Acc: %.2f | %.2f | %.2f", currentSensor[0], currentSensor[1], currentSensor[2]));
//                break;
//            default:
//        }
//    }

    @Override
    protected void onStart() {
        super.onStart();
//        if(senSorAccelerometer != null) {
//            sensorManager.registerListener(this, senSorAccelerometer, SensorManager.SENSOR_DELAY_UI);
//        }
    }

    @Override
    protected void onStop() {
        super.onStop();
//        sensorManager.unregisterListener(this);
    }
}