package com.zero.hm.effect.timewarpscan;

import static com.zero.hm.effect.timewarpscan.ScanActivity.speed.MODE_FAST;
import static com.zero.hm.effect.timewarpscan.ScanActivity.speed.MODE_NORMAL;
import static com.zero.hm.effect.timewarpscan.ScanActivity.speed.MODE_SLOW;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.Settings;
import android.util.Log;
import android.view.PixelCopy;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.zero.hm.effect.timewarpscan.databinding.ActivityScanBinding;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import pub.devrel.easypermissions.EasyPermissions;

public class ScanActivity extends AppCompatActivity
        implements Listener,
        EasyPermissions.PermissionCallbacks, SurfaceHolder.Callback {

    private ActivityScanBinding binding;

    private static final int FRONT_CAMERA = 1;
    private static final int BACK_CAMERA = 0;

    private static final int SPEED_SLOW = 4;
    private static final int SPEED_NORMAL = 8;
    private static final int SPEED_FAST = 16;

    private static final int REQUEST_CODE = 100;

    private int counterTime = 3;

    MediaRecorder recorder;
    SurfaceHolder holder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityScanBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        recorder = new MediaRecorder();
        initRecorder();

        holder = binding.cameraView.getHolder();
        holder.addCallback(this);
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        // cameraManager to interact with camera devices
        cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);

        // Exception is handled, because to check whether
        // the camera resource is being used by another
        // service or not.
        try {
            // O means back camera unit,
            // 1 means front camera unit
            getCameraID = cameraManager.getCameraIdList()[0];
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

        binding.cameraView.setListener(this);
        clickListeners();
    }

    private void initRecorder() {
        recorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
        recorder.setVideoSource(MediaRecorder.VideoSource.DEFAULT);

        CamcorderProfile cpLow = CamcorderProfile
                .get(CamcorderProfile.QUALITY_LOW);
        recorder.setProfile(cpLow);
        recorder.setOutputFile(GalaxyConstants.FILTER_IMAGE_SAVED_PATH + System.currentTimeMillis() + ".mp4");
        recorder.setMaxDuration(50000); // 50 seconds
        recorder.setMaxFileSize(5000000); // Approximately 5 megabytes
    }

    public void surfaceCreated(SurfaceHolder holder) {
        prepareRecorder();
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        recorder.stop();
        recorder.release();
    }

    private void prepareRecorder() {
        recorder.setPreviewDisplay(holder.getSurface());

        try {
            recorder.prepare();
        } catch (IllegalStateException e) {
            e.printStackTrace();
            finish();
        } catch (IOException e) {
            e.printStackTrace();
            finish();
        }
    }

    private Handler handler = new Handler();

    private void showPanel(int i) {
        binding.brightPanel.setVisibility(i == 1 ? View.VISIBLE : View.GONE);
        binding.speedPanel.setVisibility(i == 2 ? View.VISIBLE : View.GONE);
        binding.morePanel.setVisibility(i == 3 ? View.VISIBLE : View.GONE);
    }

    private CameraManager cameraManager;
    private String getCameraID;

    private int getDelayTime() {
        return Integer.parseInt(binding.delayButton.getText().toString().replace("s delay", ""));
    }

    private boolean getFlashState() {
        return binding.flashButton.getText().toString().equals("Flash on");
    }

    // when you click on button and torch open and
    // you do not close the torch again this code
    // will off the torch automatically
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void finish() {
        super.finish();
        try {
            // true sets the torch in OFF mode
            cameraManager.setTorchMode(getCameraID, false);

            // Inform the user about the flashlight
            // status using Toast message
        } catch (CameraAccessException e) {
            // prints stack trace on standard error
            // output error stream
            e.printStackTrace();
        }
    }

    private void setFlashState(boolean turnOn) {
        if (turnOn) {
            // Exception is handled, because to check
            // whether the camera resource is being used by
            // another service or not.
            try {
                // true sets the torch in ON mode
                cameraManager.setTorchMode(getCameraID, true);

                binding.flashButton.setCompoundDrawablesWithIntrinsicBounds(null,
                        getDrawable(R.drawable.ic_flash),
                        null, null);
                binding.flashButton.setText("Flash on");
                // Inform the user about the flashlight
                // status using Toast message
            } catch (Exception e) {
                // prints stack trace on standard error
                // output error stream
                e.printStackTrace();
            }
        } else {
            // Exception is handled, because to check
            // whether the camera resource is being used by
            // another service or not.
            try {
                // true sets the torch in OFF mode
                cameraManager.setTorchMode(getCameraID, false);

                binding.flashButton.setCompoundDrawablesWithIntrinsicBounds(null,
                        getDrawable(R.drawable.ic_flash_off),
                        null, null);
                binding.flashButton.setText("Flash off");
                // Inform the user about the flashlight
                // status using Toast message
            } catch (Exception e) {
                // prints stack trace on standard error
                // output error stream
                e.printStackTrace();
            }
        }
    }

    private void clickListeners() {

        binding.btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        binding.brightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (binding.brightPanel.getVisibility() == View.VISIBLE)
                    showPanel(0);
                else
                    showPanel(1);
            }
        });

        binding.speedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (binding.speedPanel.getVisibility() == View.VISIBLE)
                    showPanel(0);
                else
                    showPanel(2);
            }
        });

        binding.brightControl.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                //
                binding.cameraView.setBrightness(i);
                binding.brightText.setText((i + 20) / 4 + "");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                binding.brightControl.setProgress(seekBar.getProgress() / 4 * 4);
            }
        });

        binding.delayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (getDelayTime()) {
                    case 3:
                        binding.delayButton.setText("5s delay");
                        break;
                    case 5:
                        binding.delayButton.setText("10s delay");
                        break;
                    case 10:
                        binding.delayButton.setText("3s delay");
                        break;
                }
            }
        });

        binding.flashButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH))
                    setFlashState(!getFlashState());
                else
                    Toast.makeText(ScanActivity.this, "Your device has no flash", Toast.LENGTH_SHORT).show();
            }
        });

        binding.moreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (binding.morePanel.getVisibility() == View.VISIBLE)
                    showPanel(0);
                else
                    showPanel(3);
            }
        });

        binding.btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startOrStopScan(false);
            }
        });

        binding.btnCandidate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isRecording = !isRecording;
                binding.btnCamera.setImageResource(isRecording ? R.drawable.ic_recorder : R.drawable.ic_camera);
                binding.btnCandidate.setImageResource(isRecording ? R.drawable.ic_camera : R.drawable.ic_recorder);
            }
        });

        binding.btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.counterText.setVisibility(View.VISIBLE);
                counterTime = getDelayTime();
                new Thread(new Runnable() {
                    public void run() {
                        while (counterTime > 0) {
                            // Update the progress bar and display the
                            //current value in the text view
                            handler.post(new Runnable() {
                                public void run() {
                                    binding.counterText.setText(counterTime -- + "");
                                }
                            });
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                onScan();
                            }
                        });
                    }
                }).start();
        }});

        binding.btnSwap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onCameraSwitch();
            }
        });

    }
    String saveFilePath = "";

    private void saveImage() {
        try {
            recorder.stop();
            recorder.release();

            Camera2SurfaceView camera2SurfaceView = findViewById(R.id.camera_view);
            final Bitmap bitmap = Bitmap.createBitmap(
                    camera2SurfaceView.getWidth(),
                    camera2SurfaceView.getHeight(),
                    Bitmap.Config.ARGB_8888
            );

            // Create a handler thread to offload the processing of the image.
            final HandlerThread handlerThread = new HandlerThread("PixelCopier");
            handlerThread.start();
            PixelCopy.request(camera2SurfaceView, bitmap, (copyResult) -> {
                if (copyResult == PixelCopy.SUCCESS) {
                    saveFilePath = saveToInternalStorage(bitmap);
                } else {
                    Toast.makeText(ScanActivity.this, "Save image failed", Toast.LENGTH_SHORT).show();
                    return;
                }
                handlerThread.quitSafely();
            }, new Handler(handlerThread.getLooper()));
        } catch(Exception e) {

        }
        Toast.makeText(ScanActivity.this, "File saved successfully in " + saveFilePath, Toast.LENGTH_LONG).show();
    }

    private String saveToInternalStorage(Bitmap bitmapImage) {
        File directory = getDirectory();

        Log.d("TAG", "saveToInternalStorage: " + directory);

        if (directory != null) {
            File myPath = getFilePath(directory);

            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(myPath);
                // Use the compress method on the BitMap object to write image to the OutputStream
                bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return myPath.getAbsolutePath();
        }

        return null;
    }

    private File getFilePath(File directory) {
        return new File(directory, System.currentTimeMillis() + ".jpg");
    }

    private File getDirectory() {
        File directory;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            directory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString() + "/TimeWarp");
        } else {
            directory = new File(Environment.getExternalStorageDirectory().toString() + "/TimeWarp");
        }
        directory = new File(GalaxyConstants.FILTER_IMAGE_SAVED_PATH);

        Log.d("TAG", "getDirectory: " + directory);

        if (!directory.exists()) {
            // Make it, if it doesn't exit
            boolean success = directory.mkdirs();
            Log.d("TAG", "getDirectory: " + directory.canWrite());
            if (!success) {
                directory = null;
            }
        }

        return directory;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            startService(ScreenCaptureService.getStartIntent(this, resultCode, data, this));
        }
    }

    /****************************************** UI Widget Callbacks *******************************/
    private void startProjection() {
        MediaProjectionManager mProjectionManager =
                (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        startActivityForResult(mProjectionManager.createScreenCaptureIntent(), REQUEST_CODE);
//        startService(ScreenCaptureService.getStartIntent(this,
//                RESULT_OK, mProjectionManager.createScreenCaptureIntent(), this));
    }

    private void stopProjection() {
        startService(ScreenCaptureService.getStopIntent(this));
    }

    private boolean isRecording = false;

    public void onScan(){
        if (isRecording) {
            //
            recorder.start();
        }
        binding.counterText.setVisibility(View.GONE);
        binding.topbar.setVisibility(View.GONE);
        binding.bottombar.setVisibility(View.GONE);
        boolean isHorizontal = binding.effectMode.getCheckedRadioButtonId()
                == binding.horizontal.getId();
        binding.cameraView.setWarpMode(
                isHorizontal,
                binding.speed.getCheckedRadioButtonId()
                        == binding.normal.getId() ? SPEED_NORMAL : SPEED_FAST);
        startOrStopScan(true);
    }

    private void startOrStopScan(boolean isStart) {
        binding.cameraView.setScanVideo(isStart);
    }

    @Override
    public void imageSavedSuccessfully(final String filePath) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                stopProjection();
                Toast.makeText(ScanActivity.this, "File saved successfully in " + filePath, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void quitScan() {
        saveImage();
        binding.topbar.setVisibility(View.VISIBLE);
        binding.bottombar.setVisibility(View.VISIBLE);
    }

    @Override
    public void moveScanLine(int pixel, boolean isHorizontal) {
        binding.horizontalLineSeparator.setVisibility(isHorizontal ? View.GONE : View.VISIBLE);
        binding.verticalLineSeparator.setVisibility(isHorizontal ? View.VISIBLE : View.GONE);
        if (isHorizontal) {
            ConstraintLayout.LayoutParams layoutParams
                    = (ConstraintLayout.LayoutParams)
                    binding.verticalLineSeparator.getLayoutParams();
            layoutParams.setMargins(pixel,
                    0, 0, 0);
            binding.verticalLineSeparator.setLayoutParams(layoutParams);
        } else {
            ConstraintLayout.LayoutParams layoutParams
                    = (ConstraintLayout.LayoutParams)
                    binding.horizontalLineSeparator.getLayoutParams();
            layoutParams.setMargins(0,
                    pixel, 0, 0);
            binding.horizontalLineSeparator.setLayoutParams(layoutParams);
        }

    }

    public void onCameraSwitch(){
        if(binding.cameraView.isRearCameraActive(this)){
            switchCamera(this, FRONT_CAMERA);
        }else{
            switchCamera(this, BACK_CAMERA);
        }
        binding.cameraView.destroyAll();
        recreate();
    }

    public void switchCamera(Context context, int camera) {
        SharedPreferences sharedPref = context.getSharedPreferences("camera", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt("camera", camera);
        editor.apply();
    }

    private boolean hasWritePermissions() {
        return EasyPermissions.hasPermissions(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                || Build.VERSION.SDK_INT >= Build.VERSION_CODES.R;
    }

    private void requestExternalStoragePermission() {
        EasyPermissions.requestPermissions(
                this, "Please grant storage access to save file",
                100, android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        );
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {

    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {

    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            // Standard Android full-screen functionality.
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        super.onPointerCaptureChanged(hasCapture);
    }

    enum speed {
        MODE_NORMAL,
        MODE_SLOW,
        MODE_FAST;
    }
}