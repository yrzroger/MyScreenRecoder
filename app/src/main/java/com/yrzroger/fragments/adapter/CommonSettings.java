package com.yrzroger.fragments.adapter;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.logging.Logger;

public class CommonSettings {

    private static final Logger log = Logger.getLogger(CommonSettings.class.getSimpleName());

    private static CommonSettings mInstance = null;

    private CommonSettings() {
    }

    public static CommonSettings getInstance() {
        if (mInstance == null) {
            mInstance = new CommonSettings();
        }
        return mInstance;
    }

    public void release() {
        if (mInstance != null) {
            mInstance = null;
        }
    }

    /**
     * FileBrowser settings
     **/
    public static ArrayList<FilebrowserItem> filebrowser_items = null;
    public static int filebrowser_itemsposition = 0;
    public static String filebrowser_itemspath = "";

    public static final String FACTORY_DEFAULT_GENERAL_LASTPATH = "/sdcard/ScreenRecorder";
    public static String general_lastpath = "";

    public static final ArrayList<String> general_supported_media_suffixes = new ArrayList<String>() {
        {
            add("wav");
            add("ts");
            add("rawpkts");
            add("mp4");
            add("mp3");
        }
    };

    public void loadState(Context context) {

        SharedPreferences preferences = context.getSharedPreferences("PREFERENCES", Context.MODE_PRIVATE);

        general_lastpath = preferences.getString("general_lastpath", FACTORY_DEFAULT_GENERAL_LASTPATH);
    }


    public void saveState(Context context) {

        SharedPreferences preferences = context.getSharedPreferences("PREFERENCES", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putString("general_lastpath", general_lastpath);

        editor.commit();
    }

}
