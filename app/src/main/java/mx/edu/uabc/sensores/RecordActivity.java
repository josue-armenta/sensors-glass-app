package mx.edu.uabc.sensores;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

import com.tinder.scarlet.Stream;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import mx.edu.uabc.sensores.clients.SensoresClient;
import mx.edu.uabc.sensores.models.EventMessage;

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
        SensoresClient mSensoresClient = SensoresClient.getInstance();

        mSensoresClient.observe().start(new Stream.Observer<EventMessage>() {
            @Override
            public void onNext(EventMessage eventMessage) {

                Log.i("Event", "Type: " + eventMessage.getType() + ", Command: " + eventMessage.getCommand());

                if (eventMessage.getType().equals("record")) {
                    onKeyDown(KeyEvent.KEYCODE_DPAD_CENTER, null);
                }
            }

            @Override
            public void onError(Throwable throwable) {

            }

            @Override
            public void onComplete() {

            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {

            recording = !recording;

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    recordingLabel.setVisibility(recording ? View.VISIBLE : View.INVISIBLE);
                }
            });

            if (recording) {

                File path = Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);

                String basename = new SimpleDateFormat("yyyyMMddHHmmss",
                        Locale.getDefault()).format(new Date());

                String ext;

                if (position == OPEN_RECORD)
                    ext = "_trip.csv";
                else
                    ext = "_trn.csv";

                String fileName = basename + ext;

                File file = new File(path, fileName);

                try {
                    file.createNewFile();
                } catch (IOException ignored) {
                }

                try {
                    os = new FileOutputStream(file);
                } catch (FileNotFoundException ignored) {

                }

            } else {
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

        String str;

        if (position == OPEN_RECORD) {

            str = timestamp + "," +
                    pitch + "," +
                    roll + "\n";
        } else {

            str = pitch + "," +
                    roll + "," +
                    position + "\n";
        }

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