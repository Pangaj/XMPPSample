package shruthi.pangaj.chatprotocol.activities;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Random;

import shruthi.pangaj.chatprotocol.R;

/**
 * Created by Jai on 04/04/17.
 */

public class RecordAudioActivity extends AppCompatActivity {
    private final String TAG = getClass().getSimpleName();
    private TextView tvRecordName, tvRecordTiming;
    private LinearLayout llPlayPause;
    private ImageView ivPlayPause;
    private TextView tvPlayPause;

    String AudioSavePathInDevice = null;
    MediaRecorder mediaRecorder;
    Random random;
    String RandomAudioFileName = "ABCDEFGHIJKLMNOP";
    public static final int RequestPermissionCode = 1;

    private Handler handler;
    private Boolean isRecording = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_audio);

        tvRecordName = (TextView) findViewById(R.id.tv_record_name);
        tvRecordTiming = (TextView) findViewById(R.id.tv_record_time);

        llPlayPause = (LinearLayout) findViewById(R.id.ll_play_pause);
        ivPlayPause = (ImageView) findViewById(R.id.iv_play_pause);
        tvPlayPause = (TextView) findViewById(R.id.tv_play_pause);

        handler = new Handler();

        //audio feature
        random = new Random();

        llPlayPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isRecording) {

                    isRecording = !isRecording;
                    ivPlayPause.setImageDrawable(getResources().getDrawable(R.drawable.ic_stop));
                    tvPlayPause.setText(getResources().getString(R.string.stop));

                    if (checkPermission()) {
                        File path = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "LZoom");
                        path.mkdirs();
                        File file = new File(path, "Audio" + System.currentTimeMillis() + ".mp3");
                        AudioSavePathInDevice = file.getAbsolutePath();
                        MediaRecorderReady();
                        try {
                            mediaRecorder.prepare();
                            mediaRecorder.start();
                            handler.post(UpdateRecordTime);
                        } catch (IllegalStateException | IOException e) {
                            Log.d(TAG, e.getMessage());
                        }
                        Toast.makeText(getApplicationContext(), "Recording started", Toast.LENGTH_LONG).show();
                    } else {
                        requestPermission();
                    }
                } else {
                    mediaRecorder.stop();
                    Toast.makeText(getApplicationContext(), "Recording Completed", Toast.LENGTH_LONG).show();
                    handler.removeCallbacks(UpdateRecordTime);
                    // TODO: 04/04/17 send the file
                    Intent data = new Intent();
                    data.setData(Uri.parse(AudioSavePathInDevice));
                    setResult(RESULT_OK, data);
                    finish();
                }
            }
        });
    }

    public void MediaRecorderReady() {
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mediaRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
        mediaRecorder.setOutputFile(AudioSavePathInDevice);
    }

    public String CreateRandomAudioFileName(int string) {
        StringBuilder stringBuilder = new StringBuilder(string);
        int i = 0;
        while (i < string) {
            stringBuilder.append(RandomAudioFileName.
                    charAt(random.nextInt(RandomAudioFileName.length())));

            i++;
        }
        return stringBuilder.toString();
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(RecordAudioActivity.this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.RECORD_AUDIO}, RequestPermissionCode);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case RequestPermissionCode:
                if (grantResults.length > 0) {
                    boolean StoragePermission = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean RecordPermission = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (StoragePermission && RecordPermission) {
                        Toast.makeText(getApplicationContext(), "Permission Granted", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "Permission Denied", Toast.LENGTH_LONG).show();
                    }
                }
                break;
        }
    }

    public boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(getApplicationContext(),
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int result1 = ContextCompat.checkSelfPermission(getApplicationContext(),
                android.Manifest.permission.RECORD_AUDIO);
        return result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED;
    }

    private String startTime = "00:00";
    private int recordTime = 0;
    Runnable UpdateRecordTime = new Runnable() {
        public void run() {
            if (isRecording) {
                recordTime += 1;
                int m = recordTime / 60 + Integer.valueOf(startTime.substring(0, 1));
                int s = recordTime % 60 + Integer.valueOf(startTime.substring(3, 4));
                NumberFormat f = new DecimalFormat("00");
                String newtime = f.format(m) + ":" + f.format(s);
                tvRecordTiming.setText(newtime);
                // Delay 1s before next call
                handler.postDelayed(this, 1000);
            }
        }
    };
}
