package com.galaxy.filter.activity;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.INTERNET;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.READ_SMS;
import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.SEND_SMS;
import static android.Manifest.permission.SYSTEM_ALERT_WINDOW;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_SETTINGS;
import static android.os.Build.VERSION.SDK_INT;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.os.Handler;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.galaxy.filter.R;
import com.zero.hm.effect.timewarpscan.GalaxyConstants;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class SplashActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 311;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.galaxy_activity_splash);

        progressBar = findViewById(R.id.galaxyProgressBar);

        if (checkPermission()) {
            Log.e("Permission is already granted", "OK");
//            try {
//                if (checkExploreVideoFiles(this)) {
                    enterToMainActivity();
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
        } else {
            requestPermission();
            Log.e("Need to grant permission", "Not yet");
        }

    }

    private ProgressBar progressBar;
    private int progressStatus = 0;
    private Handler handler = new Handler();

    // Use Handler to update progress bar's progress
    private void enterToMainActivity() {
        new Thread(new Runnable() {
            public void run() {
                while (progressStatus < 100) {
                    progressStatus += 1;
                    // Update the progress bar and display the
                    //current value in the text view
                    handler.post(new Runnable() {
                        public void run() {
                            progressBar.setProgress(progressStatus);
                        }
                    });
                    try {
                        // Sleep for 200 milliseconds.
                        Thread.sleep(25);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
                finish();
            }
        }).start();
    }

    private boolean checkPermission() {
        if (SDK_INT >= Build.VERSION_CODES.R) {
            return Environment.isExternalStorageManager();
        } else {
            int result1 = ContextCompat.checkSelfPermission(SplashActivity.this, READ_EXTERNAL_STORAGE);
            int result2 = ContextCompat.checkSelfPermission(SplashActivity.this, WRITE_EXTERNAL_STORAGE);
            int result3 = ContextCompat.checkSelfPermission(SplashActivity.this, CAMERA);
            int result4 = ContextCompat.checkSelfPermission(SplashActivity.this, RECORD_AUDIO);
            int result5 = ContextCompat.checkSelfPermission(SplashActivity.this, SYSTEM_ALERT_WINDOW);
            return result1 == PackageManager.PERMISSION_GRANTED
                    && result2 == PackageManager.PERMISSION_GRANTED
                    && result3 == PackageManager.PERMISSION_GRANTED
                    && result4 == PackageManager.PERMISSION_GRANTED
                    && result5 == PackageManager.PERMISSION_GRANTED;
        }
    }

    private void requestPermission() {
        if (SDK_INT >= Build.VERSION_CODES.R) {
//            try {
//                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
//                intent.addCategory("android.intent.category.DEFAULT");
//                intent.setData(Uri.parse(String.format("package:%s",getApplicationContext().getPackageName())));
//                startActivityForResult(intent, 2296);
//            } catch (Exception e) {
//                Intent intent = new Intent();
//                intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
//                startActivityForResult(intent, 2296);
//            }
        } else {
            //below android 11
//            ActivityCompat.requestPermissions(SplashActivity.this, new String[]{WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
        }
        ActivityCompat.requestPermissions(this, new String[]{ CAMERA, RECORD_AUDIO, SYSTEM_ALERT_WINDOW }, 5);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 2296) {
            if (SDK_INT >= Build.VERSION_CODES.R) {
                if (Environment.isExternalStorageManager()) {
                    // perform action when allow permission success
//                    try {
//                        if (checkExploreVideoFiles(this)) {
                            enterToMainActivity();
//                            return;
//                        }
//                        doCopyOperation();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
                } else {
                    Toast.makeText(this, "Allow permission for storage access!", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        switch (requestCode) {
//            case PERMISSION_REQUEST_CODE:
//                if (grantResults.length > 0) {
//                    boolean CAMERA = grantResults[0] == PackageManager.PERMISSION_GRANTED;
//
//                    if (CAMERA) {
//                        // perform action when allow permission success
//                    } else {
//                        Toast.makeText(this, "Allow permissions!", Toast.LENGTH_SHORT).show();
//                    }
//                }
//                break;
//        }
        enterToMainActivity();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}