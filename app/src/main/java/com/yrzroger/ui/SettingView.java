package com.yrzroger.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

import com.yrzroger.R;


/**
 * Created by Test on 2018/4/3.
 */

public class SettingView extends LinearLayout {
    public static final String TAG = "SettingView";

    public static final int RESOLUTION_480P = 0;
    public static final int RESOLUTION_720P = 1;
    public static final int RESOLUTION_1080P = 2;

    public static final int AUDIO_MIC = 0;
    public static final int AUDIO_SYSTEM = 1;

    public static final int ORIENTATION_V = 0; // 竖屏
    public static final int ORIENTATION_H = 1; // 横屏

    private int mResolution = RESOLUTION_720P;
    private int mAudioSource = AUDIO_SYSTEM;
    private int mOrientation = ORIENTATION_V;

    private Context mContext;
    private WindowManager mWindowManager;
    private ImageButton mBtnCancel;
    private ImageButton mBtnOk;
    private RadioGroup mRadioGroupResolution;
    private RadioGroup mRadioGroupAudio;
    private RadioGroup mRadioGroupOrientation;

    public SettingView(Context context) {
        super(context);
        mContext = context;
        mWindowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        initView();
    }

    private void initView() {
        inflate(getContext(), R.layout.layout_setting, this);
        mBtnCancel = (ImageButton)findViewById(R.id.btn_cancel);
        mBtnOk = (ImageButton)findViewById(R.id.btn_ok);
        mRadioGroupResolution = (RadioGroup)findViewById(R.id.rgResolution);
        mRadioGroupAudio = (RadioGroup)findViewById(R.id.rgAudio);
        mRadioGroupOrientation = (RadioGroup)findViewById(R.id.rgOrientation);

        getUserSettingInfo();
        switch(mResolution) {
            case RESOLUTION_480P:
                mRadioGroupResolution.check(R.id.rbt480P);
                break;
            case RESOLUTION_720P:
                mRadioGroupResolution.check(R.id.rbt720P);
                break;
            case RESOLUTION_1080P:
                mRadioGroupResolution.check(R.id.rbt1080P);
                break;
            default:
                mRadioGroupResolution.check(R.id.rbt720P);
                break;
        }

        switch(mAudioSource) {
            case AUDIO_MIC:
                mRadioGroupAudio.check(R.id.rbtAudioMIC);
                break;
            case AUDIO_SYSTEM:
                mRadioGroupAudio.check(R.id.rbtAudioSystem);
                break;
            default:
                mRadioGroupAudio.check(R.id.rbtAudioSystem);
                break;
        }

        switch(mOrientation) {
            case ORIENTATION_H:
                mRadioGroupOrientation.check(R.id.rbtOrientationHorizontal);
                break;
            case ORIENTATION_V:
                mRadioGroupOrientation.check(R.id.rbtOrientationVertical);
                break;
            default:
                mRadioGroupOrientation.check(R.id.rbtOrientationVertical);
                break;
        }

        mBtnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toRemove();
            }
        });
        mBtnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setUserSettingInfo();
                toRemove();
            }
        });

        mRadioGroupResolution.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (group.getCheckedRadioButtonId()) {
                    case R.id.rbt480P:
                        mResolution = RESOLUTION_480P;
                        break;
                    case R.id.rbt720P:
                        mResolution = RESOLUTION_720P;
                        break;
                    case R.id.rbt1080P:
                        mResolution = RESOLUTION_1080P;
                        break;
                    default:
                        mResolution = RESOLUTION_720P;
                        break;
                }
            }}
        );
        mRadioGroupAudio.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (group.getCheckedRadioButtonId()) {
                    case R.id.rbtAudioMIC:
                        mAudioSource = AUDIO_MIC;
                        break;
                    case R.id.rbtAudioSystem:
                        mAudioSource = AUDIO_SYSTEM;
                        break;
                    default:
                        mAudioSource = AUDIO_SYSTEM;
                        break;
                }
            }}
        );
        mRadioGroupOrientation.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (group.getCheckedRadioButtonId()) {
                    case R.id.rbtOrientationHorizontal:
                        mOrientation = ORIENTATION_H;
                        break;
                    case R.id.rbtOrientationVertical:
                        mOrientation = ORIENTATION_V;
                        break;
                    default:
                        mOrientation = ORIENTATION_V;
                        break;
                }
            }}
        );
    }

    /**
     * 移除设置对话框
     */
    private void toRemove() {
        SettingViewManager.removeSettingView(getContext());
    }

    /**
     * 读取用户设置信息
     */
    private void getUserSettingInfo() {
        SharedPreferences userSettings = mContext.getSharedPreferences("setting", Context.MODE_PRIVATE);
        mResolution = userSettings.getInt("resolution", RESOLUTION_720P);
        mAudioSource = userSettings.getInt("audio", AUDIO_SYSTEM);
        mOrientation = userSettings.getInt("orientation", ORIENTATION_V);
    }
    /**
     * 写入用户设置信息
     */
    private void setUserSettingInfo() {
        SharedPreferences userSettings = mContext.getSharedPreferences("setting", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = userSettings.edit();
        editor.putInt("resolution", mResolution);
        editor.putInt("audio", mAudioSource);
        editor.putInt("orientation", mOrientation);
        editor.commit();
    }
}
