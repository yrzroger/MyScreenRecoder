package com.yrzroger.screenrecord;

import android.media.MediaFormat;

import java.util.Objects;

/**
 * @author yrom
 * @version 2017/12/3
 */
public class AudioEncodeConfig {
    public final String codecName;
    public final String mimeType;
    public final int bitRate;
    public final int sampleRate;
    public final int channelCount;
    public final int profile;

    public AudioEncodeConfig(String codecName, String mimeType,
                             int bitRate, int sampleRate, int channelCount, int profile) {
        this.codecName = codecName;
        this.mimeType = Objects.requireNonNull(mimeType);
        this.bitRate = bitRate;
        this.sampleRate = sampleRate;
        this.channelCount = channelCount;
        this.profile = profile;
    }

    MediaFormat toFormat() {
        MediaFormat format = MediaFormat.createAudioFormat(mimeType, sampleRate, channelCount);
        format.setInteger(MediaFormat.KEY_AAC_PROFILE, profile);
        format.setInteger(MediaFormat.KEY_BIT_RATE, bitRate);
        //format.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, 4096 * 4);
        return format;
    }

    @Override
    public String toString() {
        return "AudioEncodeConfig{" +
                "codecName='" + codecName + '\'' +
                ", mimeType='" + mimeType + '\'' +
                ", bitRate=" + bitRate +
                ", sampleRate=" + sampleRate +
                ", channelCount=" + channelCount +
                ", profile=" + profile +
                '}';
    }
}
