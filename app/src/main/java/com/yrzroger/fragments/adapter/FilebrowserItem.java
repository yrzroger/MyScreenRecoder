package com.yrzroger.fragments.adapter;


import android.graphics.Bitmap;

import java.util.Formatter;
import java.util.Locale;

public class FilebrowserItem {

    public boolean mInfoAvailable = false;

    public String mFilename = "";
    public String mFileSuffix = "";


    public boolean mHasVideo = false;
    public String mVideomime = "";
    public double mFps = -1.0;
    public int mWidth = -1;
    public int mHeight = -1;

    public boolean mHasAudio = false;
    public String mAudiomime = "";
    public int mFs = 0;
    public int mChannels = 0;

    public long mDuration = 0;

    public Bitmap mThumbnail = null;
    static private StringBuilder mFormatBuilder;
    static private Formatter mFormatter;

    static {
        mFormatBuilder = new StringBuilder();
        mFormatter = new Formatter(mFormatBuilder, Locale.getDefault());
    }
    public FilebrowserItem() {

    }

    public FilebrowserItem(String filename) {
        mFilename = filename;
    }

    static public String stringForTime(long timeMs) {
        int totalSeconds = (int) (timeMs / 1000);
        int frac = (int) (timeMs % 1000);

        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;

        mFormatBuilder.setLength(0);

        return mFormatter.format("%02d:%02d:%02d.%03d", hours, minutes, seconds, frac).toString();
    }
}
