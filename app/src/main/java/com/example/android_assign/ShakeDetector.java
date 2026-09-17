package com.example.android_assign;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.widget.TextView;

import java.util.LinkedList;
import java.util.Locale;
import java.util.Objects;
import java.util.Queue;

public class ShakeDetector implements SensorEventListener {

    // Seismic/Square parameters
    private static final float ACCELERATION_THRESHOLD = 13.0f;
    private static final long MIN_WINDOW_NS = 250_000_000L; // 0.25s
    private static final long MAX_WINDOW_NS = 500_000_000L; // 0.5s
    private static final float REQUIRED_PERCENTAGE = 0.75f;

    private final SensorManager sensorManager;
    private final Sensor accelerometer;
    private final OnShakeListener listener;
    private final TextView currentSensorTV;

    private final Queue<Sample> samples = new LinkedList<>();

    public interface OnShakeListener {
        void onShake();
    }

    public ShakeDetector(
            SensorManager sensorManager,
            OnShakeListener listener,
            TextView currentSensorTV
    ) {
        this.sensorManager = sensorManager;
        this.listener = listener;
        this.accelerometer =
                sensorManager.getDefaultSensor(
                        Sensor.TYPE_ACCELEROMETER
                );
        this.currentSensorTV = currentSensorTV;
    }

    public void start() {
        if (accelerometer != null) {
            sensorManager.registerListener(
                    this,
                    accelerometer,
                    SensorManager.SENSOR_DELAY_GAME
            );
        }
    }

    public void stop() {
        sensorManager.unregisterListener(this);
        samples.clear();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        if (event.sensor.getType() != Sensor.TYPE_ACCELEROMETER) {
            return;
        }

        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];

        //Show on screen
        currentSensorTV.setText(String.format(Locale.getDefault(),"Acc: %.2f | %.2f | %.2f", x, y, z));

        // Calculate total acceleration magnitude
        float magnitude = (float) Math.sqrt(
                x * x + y * y + z * z
        );

        boolean accelerating =
                magnitude > ACCELERATION_THRESHOLD;

        long timestamp = event.timestamp;

        samples.add(
                new Sample(timestamp, accelerating)
        );

        // Remove samples older than 0.5 seconds
        while (true) {
            Sample oldest = samples.peek();

            if (oldest == null) {
                break;
            }

            if (timestamp - oldest.timestamp <= MAX_WINDOW_NS) {
                break;
            }

            samples.poll();
        }

        // Need at least 0.25 seconds of data
        Sample oldest = samples.peek();

        if (oldest == null) {
            return;
        }

        if (timestamp - oldest.timestamp < MIN_WINDOW_NS) {
            return;
        }

        // Calculate percentage of accelerating samples
        int acceleratingCount = 0;

        for (Sample sample : samples) {
            if (sample.accelerating) {
                acceleratingCount++;
            }
        }

        float percentage =
                (float) acceleratingCount / samples.size();

        if (percentage >= REQUIRED_PERCENTAGE) {
            listener.onShake();

            // Reset after detecting a shake
            samples.clear();
        }
    }

    @Override
    public void onAccuracyChanged(
            Sensor sensor,
            int accuracy
    ) {
        // Not needed
    }

    private static class Sample {

        final long timestamp;
        final boolean accelerating;

        Sample(
                long timestamp,
                boolean accelerating
        ) {
            this.timestamp = timestamp;
            this.accelerating = accelerating;
        }
    }
}

