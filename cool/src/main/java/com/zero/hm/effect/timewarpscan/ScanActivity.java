package com.zero.hm.effect.timewarpscan;

import static com.zero.hm.effect.timewarpscan.GalaxyConstants.recordIntent;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaRecorder;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.PixelCopy;
import android.view.View;
import android.view.WindowManager;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.zero.hm.effect.timewarpscan.databinding.ActivityScanBinding;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import pub.devrel.easypermissions.EasyPermissions;

public class ScanActivity extends AppCompatActivity
        implements Listener,
        EasyPermissions.PermissionCallbacks {

    private static final int RECORD_REQUEST_CODE = 412;
    private ActivityScanBinding binding;

    private static final int FRONT_CAMERA = 1;
    private static final int BACK_CAMERA = 0;

    private static final int SPEED_SLOW = 4;
    private static final int SPEED_NORMAL = 8;
    private static final int SPEED_FAST = 16;

    private static final int CAPTURE_REQUEST_CODE = 311;

    private int counterTime = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityScanBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Intent intent = new Intent(this, RecordService.class);
            intent.setAction("STARTFOREGROUND_ACTION");
            GalaxyConstants.curService = startForegroundService(intent);
        }

        // cameraManager to interact with camera devices
        cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);

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

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        mScreenDensity = metrics.densityDpi;
        width = metrics.widthPixels;
        height = metrics.heightPixels;

        if (GalaxyConstants.projectionManager == null) {
            mProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
            GalaxyConstants.projectionManager = mProjectionManager;
        } else {
            mProjectionManager = GalaxyConstants.projectionManager;
        }

    }

    private final MediaRecorder.OnErrorListener mOnErrorListener = new MediaRecorder.OnErrorListener() {

        @Override
        public void onError(MediaRecorder mr, int what, int extra) {
            Log.e(TAG, "MediaRecorder error: type = " + what + ", code = " + extra);
        }
    };

    /////////////////////////
    int width, height;
    private Timer timer = new Timer();
    private TimerTask timerTask;

    private static final String TAG = "MainActivity";
    private String videofile= "";
    private int mScreenDensity;
    private MediaProjectionManager mProjectionManager;
    private MediaProjection mMediaProjection;
    private VirtualDisplay mVirtualDisplay;
    private MediaProjectionCallback mMediaProjectionCallback;
    private MediaRecorder mMediaRecorder;
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    private void shareScreen() {
        if (recordIntent == null) {
            startActivityForResult(mProjectionManager.createScreenCaptureIntent(), RECORD_REQUEST_CODE);
            return;
        }

        doScreenRecording(RESULT_OK, recordIntent);
    }

    private void initRecorder() {
        mMediaRecorder = new MediaRecorder();
        try {

            File file = new File(GalaxyConstants.GetSavePath(this));
            if(!file.exists()) {
                file.mkdirs();
            }

            Log.e("e","Before");
            videofile= GalaxyConstants.GetSavePath(this) + System.currentTimeMillis() + ".mp4";
            File file1 = new File(videofile);
            Log.e("e","after");

            FileWriter fileWriter = new FileWriter(file1);
            fileWriter.append("");
            fileWriter.flush();
            fileWriter.close();
            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
            mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            Log.e("e","after");
            mMediaRecorder.setVideoSize(width, height);
            mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
            mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mMediaRecorder.setVideoEncodingBitRate(512 * 1000);
            mMediaRecorder.setVideoFrameRate(30);
            int rotation = getWindowManager().getDefaultDisplay().getRotation();
            int orientation = ORIENTATIONS.get(rotation + 90);
            mMediaRecorder.setOrientationHint(orientation);
            mMediaRecorder.setOutputFile(videofile);
            mMediaRecorder.prepare();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void stopScreenSharing(boolean isSuccess) {
        if (mVirtualDisplay == null || mMediaRecorder == null) {
            return;
        }
        try {
            mMediaRecorder.stop();
            mMediaRecorder.reset();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (isSuccess)
                startPreviewActivity();
            else
                Log.e("ScanActivity", "Stop Screen Failed");
        }
        mVirtualDisplay.release();
        destroyMediaProjection();
        isRecording = false;

    }

    private void destroyMediaProjection() {
        if (mMediaProjection != null) {
            mMediaProjection.unregisterCallback(mMediaProjectionCallback);
            mMediaProjection.stop();
            mMediaProjection = null;
        }

        Log.i(TAG, "MediaProjection Stopped");
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

    // when you click on button and torch open and
    // you do not close the torch again this code
    // will off the torch automatically
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void finish() {
        super.finish();
//        Intent intent = new Intent(this, RecordService.class);
//        intent.setAction("STOPFOREGROUND_ACTION");
//        startService(intent);
    }

    private boolean isRecording = false;

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
                if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
//                    performFlashLightButtonClick();
                    switchFlash();
                }
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
                if (isRecording) {
                    initRecorder();
                    shareScreen();
                } else {

                    File file = new File(GalaxyConstants.GetSavePath(ScanActivity.this));
                    if(!file.exists()) {
                        file.mkdirs();
                    }
                    imageFile = GalaxyConstants.GetSavePath(ScanActivity.this) + System.currentTimeMillis() + ".jpg";

                    countdown(0, null);
                }
        }});

        binding.btnSwap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onCameraSwitch();
            }
        });

    }

    private void countdown(int resultCode, Intent data) {

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
                        if (isRecording) {
                            doScreenRecording(resultCode, data);
                        }
                        onScan();
                    }
                });
            }
        }).start();
    }

    private void saveImage() {
        try {
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
                    saveToInternalStorage(bitmap);
                } else {
                    Toast.makeText(ScanActivity.this, "Save image failed", Toast.LENGTH_SHORT).show();
                    return;
                }
                handlerThread.quitSafely();
            }, new Handler(handlerThread.getLooper()));
        } catch(Exception e) {

        }
//        Toast.makeText(ScanActivity.this, "Saved file path: " + imageFile, Toast.LENGTH_LONG).show();
    }

    private void saveToInternalStorage(Bitmap bitmapImage) {
        File myPath = new File(imageFile);

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

                startPreviewActivity();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String imageFile;

    private File getDirectory() {
        File directory = new File(GalaxyConstants.GetSavePath(this));

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
    protected void onStop() {
        super.onStop();
        stopScreenSharing(false);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == CAPTURE_REQUEST_CODE) {
                startService(ScreenCaptureService.getStartIntent(this, resultCode, data, this));
            } else if (requestCode == RECORD_REQUEST_CODE) {
                recordIntent = data;
                countdown(resultCode, data);
            }
        }
    }

    private void doScreenRecording(int resultCode, Intent data) {

        mMediaProjectionCallback = new MediaProjectionCallback();
        mMediaProjection = mProjectionManager.getMediaProjection(resultCode, data);
        timerTask = new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        timerTask = new TimerTask() {
                            @Override
                            public void run() {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mMediaProjection.registerCallback(mMediaProjectionCallback, null);
                                        mVirtualDisplay = mMediaProjection.createVirtualDisplay("MainActivity", width, height, mScreenDensity,
                                                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, mMediaRecorder.getSurface(), null, null);
                                        mMediaRecorder.start();
                                        doScan();
                                    }
                                });
                            }
                        };
                        timer.schedule(timerTask, (int)(100));
                    }
                });
            }
        };
        timer.schedule(timerTask,(int)(100));
    }

    private class MediaProjectionCallback extends MediaProjection.Callback {
        @Override
        public void onStop() {
            try {
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void quitScan() {
        if (isRecording)
            stopScreenSharing(true);
        else
            saveImage();

        binding.topbar.setVisibility(View.VISIBLE);
        binding.bottombar.setVisibility(View.VISIBLE);
    }

    private boolean isTorchOn = false;
    public void switchFlash() {
        isTorchOn = !isTorchOn;

        binding.flashButton.setCompoundDrawablesWithIntrinsicBounds(null,
                isTorchOn ? getDrawable(R.drawable.ic_flash)
                        : getDrawable(R.drawable.ic_flash_off), null, null);
        binding.cameraView.takePreview(isTorchOn);
    }

    private void startPreviewActivity() {
        finish();
        Intent intent = new Intent(this, PreviewActivity.class);
        intent.putExtra("file_path", isRecording ? videofile : imageFile);
        startActivity(intent);
        mMediaProjection.stop();
        mMediaProjection.unregisterCallback(mMediaProjectionCallback);
    }

    public void onScan(){
        doScan();
    }

    private void doScan() {
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

    private void stopProjection() {
        startService(ScreenCaptureService.getStopIntent(this));
    }

    @Override
    public void imageSavedSuccessfully(final String filePath) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                stopProjection();
            }
        });
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
        finish();
        startActivity(new Intent(this, ScanActivity.class));
    }

    public void switchCamera(Context context, int camera) {
        SharedPreferences sharedPref = context.getSharedPreferences("camera", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt("camera", camera);
        editor.apply();
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

}