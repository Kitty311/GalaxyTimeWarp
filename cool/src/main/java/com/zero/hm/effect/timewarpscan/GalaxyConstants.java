package com.zero.hm.effect.timewarpscan;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Environment;

public class GalaxyConstants {

    public static String APP_FOLDER = "GalaxyTimeWarp";

    public static Intent recordIntent;
    public static ComponentName curService;
    public static MediaProjectionManager projectionManager;

    public static String GetSavePath(Context context) {
        return context.getFilesDir().getAbsolutePath() + "/" + APP_FOLDER + "/";
    }
}
