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

public class RecordActivity extends Activity implements Orientation.Listener {

    private static final String TAG = RecordActivity.class.getSimpleName();
    public static final int OPEN_RECORD = 999;
    public static final String POINT_POSITION = "POINT_POSITION";

    private Orientation mOrientation;

    private TextView xAxisLabel;
    private TextView yAxisLabel;
    private TextView recordingLabel;

    private boolean recording = false;
    private int position;
    private FileOutputStream os;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);

        xAxisLabel = (TextView) findViewById(R.id.txtX);
        yAxisLabel = (TextView) findViewById(R.id.txtY);
        recordingLabel = (TextView) findViewById(R.id.recording);

        Intent intent = getIntent();
        position = intent.getIntExtra(POINT_POSITION, 0);

        TextView pointPosition = (TextView) findViewById(R.id.pointPosition);

        if (position == OPEN_RECORD)
            pointPosition.setText(getString(R.string.open_record));
        else
            pointPosition.setText(getString(R.string.point_position, position));

        mOrientation = new Orientation(this);
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

    private void recordData(long timestamp, double pitch, double roll, int position) {

        String str = timestamp + "," +
                pitch + "," +
                roll + "," +
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
    protected void onStart() {
        super.onStart();
        mOrientation.startListening(this);
    }

    @Override
    protected void onStop() {
        super.onStop();

        mOrientation.stopListening();
        closeFiles();
    }

    @Override
    public void onOrientationChanged(long timestamp, float pitch, float roll) {

        if (recording)
            recordData(timestamp, pitch, roll, position);

        String pitchStr = "Pitch: " + String.format(Locale.getDefault(), "%.02f", pitch);
        String rollStr = "Roll: " + String.format(Locale.getDefault(), "%.02f", roll);

        xAxisLabel.setText(pitchStr);
        yAxisLabel.setText(rollStr);

    }
}