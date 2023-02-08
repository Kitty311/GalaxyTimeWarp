package com.zero.hm.effect.timewarpscan;

import android.content.Context;
import android.os.Environment;

public class GalaxyConstants {

    public static String APP_FOLDER = "GalaxyTimeWarp";

    public static String FILTER_IMAGE_SAVED_PATH =
            (Environment.getExternalStorageDirectory().getAbsolutePath()
                    + "/" + APP_FOLDER + "/");

    public static String GetExploreVideoSavedPath(Context context) {
        return FILTER_IMAGE_SAVED_PATH + "/GalaxyExploreVideos/";
    }

}
