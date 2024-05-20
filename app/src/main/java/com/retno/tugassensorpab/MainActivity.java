package com.retno.tugassensorpab;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor magnetometer;

    private TextView textAzimuth;
    private TextView textPitch;
    private TextView textRoll;

    private float[] accelerometerData = new float[3];
    private float[] magnetometerData = new float[3];

    private ImageView spotTop;
    private ImageView spotBottom;
    private ImageView spotRight;
    private ImageView spotLeft;
    private ImageView compassImage;

    private static final float DRIFT_THRESHOLD = 0.05f;
    private float currentAzimuth = 0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        textAzimuth = findViewById(R.id.value_azimuth);
        textPitch = findViewById(R.id.value_pitch);
        textRoll = findViewById(R.id.value_roll);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        spotTop = findViewById(R.id.spot_top);
        spotBottom = findViewById(R.id.spot_bottom);
        spotRight = findViewById(R.id.spot_right);
        spotLeft = findViewById(R.id.spot_left);
        compassImage = findViewById(R.id.compass_image);
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        }
        if (magnetometer != null) {
            sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        int sensorType = event.sensor.getType();
        switch (sensorType) {
            case Sensor.TYPE_ACCELEROMETER:
                accelerometerData = event.values.clone();
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                magnetometerData = event.values.clone();
                break;
            default:
                return;
        }

        float[] rotationMatrix = new float[9];
        boolean rotationOk = SensorManager.getRotationMatrix(rotationMatrix, null, accelerometerData, magnetometerData);

        float[] orientationValues = new float[3];
        if (rotationOk) {
            SensorManager.getOrientation(rotationMatrix, orientationValues);
        }

        float azimuth = (float) Math.toDegrees(orientationValues[0]);
        float pitch = (float) Math.toDegrees(orientationValues[1]);
        float roll = (float) Math.toDegrees(orientationValues[2]);

        textAzimuth.setText(getResources().getString(R.string.value_format, azimuth));
        textPitch.setText(getResources().getString(R.string.value_format, pitch));
        textRoll.setText(getResources().getString(R.string.value_format, roll));

        if (Math.abs(pitch) < DRIFT_THRESHOLD) {
            pitch = 0;
        }
        if (Math.abs(roll) < DRIFT_THRESHOLD) {
            roll = 0;
        }

        updateSpotVisibility(pitch, roll);
        rotateCompass(azimuth);
    }

    private void updateSpotVisibility(float pitch, float roll) {
        spotTop.setAlpha(0f);
        spotRight.setAlpha(0f);
        spotLeft.setAlpha(0f);
        spotBottom.setAlpha(0f);

        if (pitch > 0) {
            spotBottom.setAlpha(Math.abs(pitch / 90));  // Normalize for better visibility
            spotBottom.setColorFilter(Color.RED);
        } else {
            spotTop.setAlpha(Math.abs(pitch / 90));
            spotTop.setColorFilter(Color.GREEN);
        }

        if (roll > 0) {
            spotLeft.setAlpha(Math.abs(roll / 90));
            spotLeft.setColorFilter(Color.BLUE);
        } else {
            spotRight.setAlpha(Math.abs(roll / 90));
            spotRight.setColorFilter(Color.YELLOW);
        }
    }

    private void rotateCompass(float azimuth) {
        RotateAnimation rotateAnimation = new RotateAnimation(
                currentAzimuth,
                -azimuth,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);

        rotateAnimation.setDuration(500);
        rotateAnimation.setFillAfter(true);

        compassImage.startAnimation(rotateAnimation);
        currentAzimuth = -azimuth;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // No-op for this example
    }
}
