package com.yrzroger.screenrecord;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.Surface;

import com.yrzroger.ui.SettingView;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by roger_yu on 2018/4/4.
 */

public class VideoEncodeThread extends Thread {
    public static final String TAG = "VideoEncodeThread";

    private int mFrameRate = 30;
    private int mBitrateMode = 0;
    private int mProfile = 8;
    private int mLevel = 8192;
    private int mWidth = 720;
    private int mHeight = 1280;
    private MediaMuxer mMediaMuxer = null;
    private MediaCodec mEncoder = null;
    private Surface mInputSurface = null;
    private HandlerThread mEncoderCallbackThread = null;
    private Handler mEncoderHandler = null;
    private boolean mEncodeErr = false;
    private boolean mStartRealease = false;
    private int mVideoTrackIdx;
    public boolean mMuxerFlag = false;
    public boolean mTrackReady = false;
    private boolean eosThread = false;

    private int mResolution = SettingView.RESOLUTION_720P;
    private int mOrientation= SettingView.ORIENTATION_V;

    public VideoEncodeThread(MediaMuxer muxer, int resolution, int orientation) {
        setVideoWidthAndHeight(resolution, orientation);
        mMediaMuxer = muxer;
    }
    @Override
    public void run() {

    }

    /**
     * 根据视频分辨率及录屏方向设置视频的宽高
     * @param resolution
     * @param orientation
     */
    public void setVideoWidthAndHeight(int resolution, int orientation) {
        if(orientation == SettingView.ORIENTATION_V) {
            if(resolution == SettingView.RESOLUTION_480P) {
                mWidth = 480;
                mHeight = 640;
            } else if(resolution == SettingView.RESOLUTION_720P) {
                mWidth = 720;
                mHeight = 1280;
            } else {
                mWidth = 1080;
                mHeight = 1920;
            }
        } else {
            if(resolution == SettingView.RESOLUTION_480P) {
                mWidth = 640;
                mHeight = 480;
            } else if(resolution == SettingView.RESOLUTION_720P) {
                mWidth = 1280;
                mHeight = 720;
            } else {
                mWidth = 1920;
                mHeight = 1080;
            }
        }
    }

    /**
     * 配置视频编码器
     */
    private void setupVideoEncoder(){
        MediaFormat format = MediaFormat.createVideoFormat("video/avc", mWidth, mHeight);
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
        format.setInteger(MediaFormat.KEY_BIT_RATE, mWidth*mHeight*15);
        format.setInteger(MediaFormat.KEY_FRAME_RATE, mFrameRate);
        format.setInteger(MediaFormat.KEY_CAPTURE_RATE, mFrameRate);
        format.setInteger(MediaFormat.KEY_REPEAT_PREVIOUS_FRAME_AFTER, 2000000 / mFrameRate);
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1); // 1 seconds between I-frames
        format.setInteger(MediaFormat.KEY_BITRATE_MODE, mBitrateMode);
        format.setInteger(MediaFormat.KEY_PROFILE, mProfile);
        format.setInteger(MediaFormat.KEY_LEVEL, mLevel);

        try {
            mEncoder = MediaCodec.createEncoderByType("video/avc");
        } catch (IOException e) {
            Log.e(TAG, "Create encoder codec from type ERROR !!!");
        }
        mEncoderCallbackThread = new HandlerThread("EncoderHanlderThread");
        mEncoderCallbackThread.start();
        mEncoderHandler = new Handler(mEncoderCallbackThread.getLooper());

        setupEncoderCallback(mEncoderHandler);

        mEncoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        mInputSurface = mEncoder.createInputSurface();
        mEncoder.start();
    }

    /**
     * 释放视频编码器资源
     */
    public void releaseResource() {
        if(mStartRealease == false) {
            mStartRealease = true;
            if (mEncoder != null) {
                if(mEncodeErr == false) {
                    mEncoder.flush();
                    mEncoder.stop();
                }
                mEncoder.release();
                mEncoder = null;
            }
            if (mInputSurface != null) {
                mInputSurface.release();
                mInputSurface = null;
            }
            if(mEncoderCallbackThread != null) {
                mEncoderCallbackThread.quitSafely();
                try {
                    mEncoderCallbackThread.join();
                } catch(InterruptedException  ex) {
                }
            }
        }
    }

    /**
     * 处理视频编码器回调事件
     * @param handle
     */
    private void setupEncoderCallback(Handler handle) {
        mEncoder.setCallback(new MediaCodec.Callback() {
            @Override
            public void onInputBufferAvailable(MediaCodec mc, int inputBufferId) {
                //We use encoder input surface, nothing to do
            }

            @Override
            public void onOutputBufferAvailable(MediaCodec mc, int outputBufferId, MediaCodec.BufferInfo info)
            {
                if(mEncodeErr == true || mStartRealease == true)
                    return;
                ByteBuffer buffer = mc.getOutputBuffer(outputBufferId);
                buffer.position(info.offset);
                buffer.limit(info.offset + info.size);
                if(mMuxerFlag == true && mMediaMuxer != null && eosThread == false)
                    mMediaMuxer.writeSampleData(mVideoTrackIdx, buffer, info);
                //Log.d(TAG, "Muxer size " + info.size + " PTS " + info.presentationTimeUs);
                mc.releaseOutputBuffer(outputBufferId, false);
            }

            @Override
            public void onOutputFormatChanged(MediaCodec mc, MediaFormat format) {
                if(mTrackReady == false) {
                    mVideoTrackIdx = mMediaMuxer.addTrack(mEncoder.getOutputFormat());
                    mTrackReady = true;
                    int errTimeout = 20;
                    while(mMuxerFlag == false && eosThread != true) {
                        try {
                            Thread.sleep(1000);
                            errTimeout--;
                            if(errTimeout < 0)
                                releaseResource();
                            Log.d(TAG, "Waiting audio track ready");
                        } catch(InterruptedException  ex) {
                        }
                    }
                }
            }

            @Override
            public void onError(MediaCodec codec, MediaCodec.CodecException e) {
                mEncodeErr = true;
                e.printStackTrace();
            }
        }, handle);
    }
}
