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
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class RecordActivity extends Activity implements SensorEventListener {

    public static final String POINT_POSITION = "POINT_POSITION";

    private static final int SENSOR_DELAY_MICROS = 20 * 1000; //50hz
    private static final double TO_DEGREES = 180 / Math.PI;

    private boolean recording = false;

    private int position;

    private SensorManager mSensorManager;
    private Sensor mAccelerometerSensor;
    private Sensor mMagnetometerSensor;
    private float[] mAccelerometerData = new float[3];
    private float[] mMagnetometerData = new float[3];

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
        pointPosition.setText("S" + String.valueOf(position));

        zAxisLabel = (TextView) findViewById(R.id.txtZ);
        xAxisLabel = (TextView) findViewById(R.id.txtX);
        yAxisLabel = (TextView) findViewById(R.id.txtY);
        recordingLabel = (TextView) findViewById(R.id.recording);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometerSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagnetometerSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
            recording = !recording;

            if (recording) {

                File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
                File file = new File(path, "Sensors.csv");
                try {
                    boolean created = file.createNewFile();
                } catch (IOException ignored) {
                    //meh
                }

                try {
                    os = new FileOutputStream(file, true);
                } catch (FileNotFoundException ignored) {

                }

                recordingLabel.setVisibility(View.VISIBLE);
            } else {
                recordingLabel.setVisibility(View.INVISIBLE);
                if (os != null)
                    try {
                        os.close();
                    } catch (IOException ignored) {
                        //meh
                    }
            }

            return true;
        } else if (keyCode == KeyEvent.KEYCODE_BACK) {
            onBackPressed();
            return true;
        }

        return false;
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        int sensorType = sensorEvent.sensor.getType();

        switch (sensorType) {
            case Sensor.TYPE_ACCELEROMETER:
                mAccelerometerData = sensorEvent.values.clone();
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                mMagnetometerData = sensorEvent.values.clone();
                break;
            default:
                return;
        }

        float[] rotationMatrix = new float[9];
        boolean rotationOk = SensorManager.getRotationMatrix(rotationMatrix,
                null, mAccelerometerData, mMagnetometerData);

        float orientationValues[] = new float[3];

        if (rotationOk) {
            SensorManager.getOrientation(rotationMatrix, orientationValues);

            double z = orientationValues[0] * TO_DEGREES;
            double x = orientationValues[1] * TO_DEGREES;
            double y = orientationValues[2] * TO_DEGREES;

            if (recording)
                recordData(sensorEvent.timestamp, x, y, z, position);

            zAxisLabel.setText("Z: " + String.valueOf(z));
            xAxisLabel.setText("X: " + String.valueOf(x));
            yAxisLabel.setText("Y: " + String.valueOf(y));
        }
    }

    private void recordData(long timestamp, double x, double y, double z, int position) {

        String str = String.valueOf(timestamp) + "," +
                String.valueOf(x) + "," +
                String.valueOf(y) + "," +
                String.valueOf(z) + "," +
                String.valueOf(position) + "\n";

        if (os != null)
            try {
                os.write(str.getBytes());
            } catch (IOException e) {
                Log.e("RecordActivity", e.getMessage());
            }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mSensorManager.registerListener(this,
                mAccelerometerSensor, SENSOR_DELAY_MICROS);
        mSensorManager.registerListener(this,
                mMagnetometerSensor, SENSOR_DELAY_MICROS);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mSensorManager.unregisterListener(this);

        if (os != null)
            try {
                os.close();
            } catch (IOException e) {
                Log.e("RecordActivity", e.getMessage());
            }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}