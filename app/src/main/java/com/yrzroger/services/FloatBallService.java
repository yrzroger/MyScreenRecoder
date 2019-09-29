package com.yrzroger.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.yrzroger.screenrecord.AudioEncodeConfig;
import com.yrzroger.ui.FloatBallView;
import com.yrzroger.ui.FloatWindowManager;
import com.yrzroger.screenrecord.ScreenRecorder;
import com.yrzroger.ui.SettingView;
import com.yrzroger.screenrecord.VideoEncodeConfig;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by roger_yu on 2018/04/03.
 */

public class FloatBallService extends Service {
    public static final String TAG = "FloatBallService";
    public static final int TYPE_ADD = 0;
    public static final int TYPE_DEL = 1;

    private MediaProjection mMediaProjection = null;
    private ScreenRecorder mRecorder;
    private Handler mHandler = new MyHandler();
    private Intent mData;
    private int mCode;


    private int mResolution = SettingView.RESOLUTION_720P;
    private int mAudioSource = SettingView.AUDIO_SYSTEM;
    private int mOrientation = SettingView.ORIENTATION_V;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

            int type = intent.getIntExtra("type", -1);
            mCode = intent.getIntExtra("code", -1);
            mData = intent.getParcelableExtra("data");
            if (type == TYPE_ADD) {
                FloatWindowManager.addBallView(this);
                setBallViewEventListener();
            } else {
                FloatWindowManager.removeBallView(this);
            }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void setBallViewEventListener() {
        FloatBallView view = FloatWindowManager.getFloatBallView();
        if(view != null) {
            view.setEventListener(new FloatBallView.EventListener() {
                @Override
                public void onEvent(int code) {
                    switch(code) {
                        case 0:
                            if(mRecorder == null)
                                startRecorder();
                            else
                                stopRecorder();

                            break;
                    }

                }
            });
        }
    }

    private AudioEncodeConfig createAudioConfig() {
        if(mAudioSource == SettingView.AUDIO_MIC)
            return new AudioEncodeConfig(null, MediaFormat.MIMETYPE_AUDIO_AAC, 80000, 44100, 2, MediaCodecInfo.CodecProfileLevel.AACObjectMain);
        else
            return null;
    }

    private VideoEncodeConfig createVideoConfig() {
        getUserSettingInfo();
        int width=720, height=1280;
        if(mOrientation == SettingView.ORIENTATION_V) {
            if (mResolution == SettingView.RESOLUTION_480P) {
                width = 480;
                height = 640;
            } else if (mResolution == SettingView.RESOLUTION_1080P) {
                width = 1080;
                height = 1920;
            } else {
                width = 720;
                height = 1280;
            }
        }
        else{
            if (mResolution == SettingView.RESOLUTION_480P) {
                width = 640;
                height = 480;
            } else if (mResolution == SettingView.RESOLUTION_1080P) {
                width = 1920;
                height = 1080;
            } else {
                width = 1280;
                height = 720;
            }
        }

        MediaCodecInfo.CodecProfileLevel tmp = new MediaCodecInfo.CodecProfileLevel();
        return new VideoEncodeConfig(width, height, width*height*15,
                30, 1, null, MediaFormat.MIMETYPE_VIDEO_AVC, tmp);
    }

    private ScreenRecorder newRecorder(MediaProjection mediaProjection, VideoEncodeConfig video,
                                       AudioEncodeConfig audio, File output) {
        ScreenRecorder r = new ScreenRecorder(video, audio,
                1, mediaProjection, output.getAbsolutePath());
        r.setCallback(new ScreenRecorder.Callback() {

            @Override
            public void onStop(Throwable error) {
                Log.i(TAG, "onStop");
                Message message = Message.obtain(mHandler, MyHandler.HIDE_RECORD_TIME);
                message.sendToTarget();
            }

            @Override
            public void onStart() {
                Log.i(TAG, "onStart");
                Message message = Message.obtain(mHandler, MyHandler.SHOW_RECORD_TIME);
                message.sendToTarget();
            }

            @Override
            public void onRecording(long presentationTimeUs) {
                Log.i(TAG, "onRecording "+presentationTimeUs);
                Message message = Message.obtain(mHandler, MyHandler.SET_RECORD_TIME);
                Bundle data = new Bundle();
                data.putLong("time", presentationTimeUs/1000);
                message.setData(data);
                message.sendToTarget();
            }
        });
        return r;
    }
    private static File getSavingDir() {

        String sdcard_dir = Environment.getExternalStorageDirectory().getPath();
        File destDir = new File(sdcard_dir+"/ScreenRecorder");
        if (!destDir.exists()) {
            destDir.mkdirs();
        }
        return destDir;

    }
    public void startRecorder() {
        VideoEncodeConfig video = createVideoConfig();
        AudioEncodeConfig audio = createAudioConfig(); // audio can be null
        File dir = getSavingDir();
        if (!dir.exists() && !dir.mkdirs()) {

            return;
        }
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US);
        final File file = new File(dir, "VID_" + format.format(new Date()) + ".mp4");
        Log.i(TAG, "Create recorder with :" + video + " \n " + audio + "\n " + file);
        mMediaProjection = createMediaProjection();
        mRecorder = newRecorder(mMediaProjection, video, audio, file);
        mRecorder.start();
    }

    private void stopRecorder() {
        if (mRecorder != null) {
            mRecorder.quit();
        }
        mRecorder = null;
    }
    private MediaProjection createMediaProjection() {
        Log.i(TAG, "Create MediaProjection");
        return ((MediaProjectionManager) getApplicationContext().getSystemService(Context.MEDIA_PROJECTION_SERVICE)).getMediaProjection(mCode, mData);
    }

    class MyHandler extends Handler {
        public static final int SET_RECORD_TIME= 0;
        public static final int SHOW_RECORD_TIME= 1;
        public static final int HIDE_RECORD_TIME= 2;
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case SET_RECORD_TIME:
                    FloatWindowManager.showRecordTime();
                    FloatWindowManager.setRecordTime(msg.getData().getLong("time"));
                    break;
                case SHOW_RECORD_TIME:
                    FloatWindowManager.showRecordTime();
                    break;
                case HIDE_RECORD_TIME:
                    FloatWindowManager.hideRecordTime();
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * 读取用户设置信息
     */
    private void getUserSettingInfo() {
        SharedPreferences userSettings = getSharedPreferences("setting", Context.MODE_PRIVATE);
        mResolution = userSettings.getInt("resolution", SettingView.RESOLUTION_720P);
        mAudioSource = userSettings.getInt("audio", SettingView.AUDIO_SYSTEM);
        mOrientation = userSettings.getInt("orientation", SettingView.ORIENTATION_V);
    }
}
