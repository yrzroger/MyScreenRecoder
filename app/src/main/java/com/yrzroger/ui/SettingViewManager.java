package com.yrzroger.ui;

import android.content.Context;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;

/**
 * Created by roger_yu on 2018/04/03.
 */

public class SettingViewManager {
    private static SettingView mSettingView;

    private static WindowManager mWindowManager;


    public static void addSettingView(Context context) {
        if (mSettingView == null) {
            WindowManager windowManager = getWindowManager(context);
            int screenWidth = windowManager.getDefaultDisplay().getWidth();
            int screenHeight = windowManager.getDefaultDisplay().getHeight();
            mSettingView = new SettingView(context);
            LayoutParams params = new LayoutParams();
            params.x = 0;
            params.y = 0;
            params.width = WindowManager.LayoutParams.WRAP_CONTENT;
            params.height = WindowManager.LayoutParams.WRAP_CONTENT;
            params.gravity = Gravity.CENTER;
            params.type = LayoutParams.TYPE_PHONE;
            params.format = PixelFormat.RGBA_8888;
            params.flags = LayoutParams.FLAG_NOT_TOUCH_MODAL
                    | LayoutParams.FLAG_NOT_FOCUSABLE;
            mSettingView.setLayoutParams(params);
            windowManager.addView(mSettingView, params);
        }
    }

    public static void removeSettingView(Context context) {
        if (mSettingView != null) {
            WindowManager windowManager = getWindowManager(context);
            windowManager.removeView(mSettingView);
            mSettingView = null;
        }
    }

    private static WindowManager getWindowManager(Context context) {
        if (mWindowManager == null) {
            mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        }
        return mWindowManager;
    }

}
