package mx.edu.uabc.sensores;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class RecordActivity extends Activity implements SensorEventListener {

    private static final String TAG = RecordActivity.class.getSimpleName();
    public static final int OPEN_RECORD = 999;
    public static final String POINT_POSITION = "POINT_POSITION";

    private static final int SENSOR_DELAY = 40000;

    private long accelerometer_ts = 0;
    private long magnetometer_ts = 0;
    private long sensors_ts = 0;

    private boolean recording = false;

    private int position;

    private SensorManager mSensorManager;
    private float[] accelerometerReading = new float[3];
    private float[] magnetometerReading = new float[3];

    private final float[] rotationMatrix = new float[9];
    private final float[] orientationAngles = new float[3];

    private Sensor mAccelerometerSensor;
    private Sensor mMagnetometerSensor;

    private TextView zAxisLabel;
    private TextView xAxisLabel;
    private TextView yAxisLabel;
    private TextView recordingLabel;

    private FileOutputStream os;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_record);

        Intent intent = getIntent();
        position = intent.getIntExtra(POINT_POSITION, 0);

        TextView pointPosition = (TextView) findViewById(R.id.pointPosition);

        if (position == OPEN_RECORD)
            pointPosition.setText(getString(R.string.open_record));
        else
            pointPosition.setText(getString(R.string.point_position, position));

        zAxisLabel = (TextView) findViewById(R.id.txtZ);
        xAxisLabel = (TextView) findViewById(R.id.txtX);
        yAxisLabel = (TextView) findViewById(R.id.txtY);
        recordingLabel = (TextView) findViewById(R.id.recording);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (mSensorManager != null) {
            mAccelerometerSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            mMagnetometerSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
            recording = !recording;

            if (recording) {

                File path = Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);

                String fileName = new SimpleDateFormat("yyyyMMddHHmmss'.csv'",
                        Locale.getDefault()).format(new Date());

                File file = new File(path, fileName);

                try {
                    file.createNewFile();
                } catch (IOException ignored) {
                }

                try {
                    os = new FileOutputStream(file);
                } catch (FileNotFoundException ignored) {

                }

                recordingLabel.setVisibility(View.VISIBLE);

            } else {
                recordingLabel.setVisibility(View.INVISIBLE);
                closeFiles();
            }

            return true;
        } else if (keyCode == KeyEvent.KEYCODE_BACK) {
            onBackPressed();
            return true;
        }

        return false;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, accelerometerReading,
                    0, accelerometerReading.length);
            accelerometer_ts = event.timestamp;
        } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, magnetometerReading,
                    0, magnetometerReading.length);
            magnetometer_ts = event.timestamp;
        }

        if (accelerometer_ts == magnetometer_ts) {
            sensors_ts = (System.currentTimeMillis() - SystemClock.elapsedRealtime()) * 1000000
                    + accelerometer_ts;
            updateOrientationAngles();

            if (recording)
                recordData(sensors_ts, orientationAngles[1], orientationAngles[2],
                        orientationAngles[0], position);

            updateScreen();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        // Nothing to do here
    }

    private void updateScreen() {

        double azimuth = orientationAngles[0];
        double pitch = orientationAngles[1];
        double roll = orientationAngles[2];

        final String z = "Z: " + azimuth;
        final String x = "X: " + pitch;
        final String y = "Y: " + roll + "\n" + sensors_ts;

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                zAxisLabel.setText(z);
                xAxisLabel.setText(x);
                yAxisLabel.setText(y);
            }
        });
    }

    public void updateOrientationAngles() {
        SensorManager.getRotationMatrix(rotationMatrix, null,
                accelerometerReading, magnetometerReading);
        SensorManager.getOrientation(rotationMatrix, orientationAngles);

        orientationAngles[0] = 180 + (float) Math.toDegrees(orientationAngles[0]);
        orientationAngles[1] = 90 + (float) Math.toDegrees(orientationAngles[1]);
        orientationAngles[2] = (float) Math.toDegrees(orientationAngles[2]);
    }

    private void recordData(long timestamp, double x, double y, double z, int position) {

        String str = timestamp + "," +
                x + "," +
                y + "," +
                z + "," +
                position + "\n";

        if (os != null)
            try {
                os.write(str.getBytes());
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
            }
    }

    private void closeFiles() {

        if (os != null)
            try {
                os.close();
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
            }
    }

    @Override
    protected void onResume() {
        super.onResume();

        mSensorManager.registerListener(this, mAccelerometerSensor,
                SENSOR_DELAY);
        mSensorManager.registerListener(this, mMagnetometerSensor,
                SENSOR_DELAY);
    }

    @Override
    protected void onPause() {
        super.onPause();

        mSensorManager.unregisterListener(this);
        closeFiles();
    }

}