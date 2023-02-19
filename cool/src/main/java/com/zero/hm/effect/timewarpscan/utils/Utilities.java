package com.zero.hm.effect.timewarpscan.utils;

import static com.zero.hm.effect.timewarpscan.ScanActivity.ACTION_UPDATE_UI;
import static com.zero.hm.effect.timewarpscan.ScanActivity.IS_FLASHLIGHT_ON;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.util.Log;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.zero.hm.effect.timewarpscan.preference.SharedPref;

public class Utilities {

    private static CameraManager mCameraManager;

    public static boolean startFlashlight(Context context, SharedPref preference) {
        if (isCameraSupportedForFlash(context)) {
            if (flashLightOn(context)) {
                return preference.setValue(IS_FLASHLIGHT_ON, true);
            }
        }
        return false;
    }

    public static boolean stopFlashlight(Context context, SharedPref preference) {
        if (flashLightOff(context)) {
            return preference.setValue(IS_FLASHLIGHT_ON, false);
        }
        return false;
    }

    private static void initCameraManagerInstance(Context context) {
        if (mCameraManager == null) {
            mCameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        }
    }

    private static boolean flashLightOn(Context context) {
        try {
            if (mCameraManager == null) {
                initCameraManagerInstance(context);
            }
            String cameraId = mCameraManager.getCameraIdList()[0];
            mCameraManager.setTorchMode(cameraId, true);
            // Sending broadcast to update the UI
            LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(ACTION_UPDATE_UI));
        } catch (CameraAccessException e) {
            return false;
        }
        return true;
    }

    private static boolean flashLightOff(Context context) {
        try {
            if (mCameraManager == null) {
                initCameraManagerInstance(context);
            }
            String cameraId = mCameraManager.getCameraIdList()[0];
            mCameraManager.setTorchMode(cameraId, false);
            // Sending broadcast to update the UI
            LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(ACTION_UPDATE_UI));
        } catch (CameraAccessException e) {
            return false;
        }
        return true;
    }

    public static boolean isCameraPermissionGranted(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean isCameraSupportedForFlash(Context context) {
        final boolean hasCameraFlash
                = context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);

        if (!hasCameraFlash) {
            String message = "No flash available";
            showToast(context, message);
        }
        return  hasCameraFlash;
    }

    public static void showToast (Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
}