package com.yrzroger;

import android.app.Application;
import android.content.Intent;

/*This class is used to share data between Activity and Service.*/

public class MyApplication extends Application {

	private static MyApplication instance;
	private static Intent mActivityResultIntent;
	private static int mActivityResultCode;
	@Override
	public void onCreate() {
		super.onCreate();
		instance = this;
	}

	public static MyApplication getInstance(){
		// 因为我们程序运行后，Application是首先初始化的
		return instance;
	}

	public static void setActivityResult(Intent data, int resultCode) {
		mActivityResultIntent = data;
		mActivityResultCode = resultCode;
	}

	public static Intent getActivityResultIntent() {
		return mActivityResultIntent;
	}

	public static int getActivityResultCode() {
		return mActivityResultCode;
	}

}
