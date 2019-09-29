package com.yrzroger.activities;

import java.util.ArrayList;
import java.util.List;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Toast;

import com.yrzroger.R;
import com.yrzroger.MyApplication;
import com.yrzroger.fragments.adapter.TabPageAdapter;
import com.yrzroger.fragments.FilebrowserFragment;
import com.yrzroger.fragments.HomeFragment;

import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.os.Build.VERSION_CODES.M;

/**
 * Created by roger_yu on 2018/06/02.
 */
public class MainActivity extends FragmentActivity implements
		OnPageChangeListener, OnCheckedChangeListener {

    private static final String TAG = "MainActivity";
    private static final int REQUEST_PERMISSIONS = 1;
    private static final int REQUEST_MEDIA_PROJECTION = 2;

	private ViewPager mViewPager;
	private RadioGroup mRadioGroup;
	private List<Fragment> mFragments = new ArrayList<Fragment>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		init();
		initView();
		selectPage(0); // 默认选中主页

        if(hasPermissions())
            startCaptureIntent();
        else if(Build.VERSION.SDK_INT >= M)
            requestPermissions();
        else {
            Toast.makeText(this, "应用没有存储及麦克风权限", Toast.LENGTH_SHORT).show();
        }
	}

	protected void init() {
		Fragment homeFragment = new HomeFragment();
		Fragment classifyFragment = new FilebrowserFragment();
		mFragments.add(homeFragment);
		mFragments.add(classifyFragment);
	}

	private void initView() {
		mRadioGroup = (RadioGroup) findViewById(R.id.radioGroup);
		mViewPager = (ViewPager) findViewById(R.id.viewPager);
		mRadioGroup.setOnCheckedChangeListener(this);
		TabPageAdapter tabPageAdapter = new TabPageAdapter(getSupportFragmentManager(), mFragments);
		mViewPager.setAdapter(tabPageAdapter);
		mViewPager.setOnPageChangeListener(this);
	}

	/**
	 * 选择某页
	 * @param position 页面的位置
	 */
	private void selectPage(int position) {
		mViewPager.setCurrentItem(position, false);
        RadioButton select = (RadioButton) mRadioGroup.getChildAt(position);
        select.setChecked(true);
	}

	@Override
	public void onPageScrolled(int position, float positionOffset,
			int positionOffsetPixels) {

	}

	@Override
	public void onPageSelected(int position) {
		selectPage(position);
	}

	@Override
	public void onPageScrollStateChanged(int state) {

	}

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		switch (checkedId) {
			case R.id.btn_home:         // 主页选中
				selectPage(0);
				break;
			case R.id.btn_filebrowser:  // 文件浏览选中
				selectPage(1);
				break;
            default:
                selectPage(0);
		}
	}

    /**
     * 判断是否具有存储及麦克风权限
     * @return true or false
     */
    private boolean hasPermissions() {
        boolean result = true;
        if(ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            result = false;
        if(ContextCompat.checkSelfPermission(this, RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED)
            result = false;
        return result;
    }

    private void startCaptureIntent() {
        MediaProjectionManager mediaProjectionManager = (MediaProjectionManager) getApplicationContext().getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        Intent captureIntent = mediaProjectionManager.createScreenCaptureIntent();
        startActivityForResult(captureIntent, REQUEST_MEDIA_PROJECTION);
    }

    /**
     * 请求存储及麦克风权限
     */
    @TargetApi(M)
    private void requestPermissions() {
        String[] permissions = new String[]{WRITE_EXTERNAL_STORAGE, RECORD_AUDIO};
        ActivityCompat.requestPermissions(this, permissions, REQUEST_PERMISSIONS);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_PERMISSIONS) {
            int granted = PackageManager.PERMISSION_GRANTED;
            for (int r : grantResults) {
                granted |= r;
            }
            if (granted == PackageManager.PERMISSION_GRANTED) {
                startCaptureIntent();
            } else {
                Toast.makeText(this, "请开启存储及麦克风权限", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_MEDIA_PROJECTION) {
			Log.i(TAG, "onActivityResult REQUEST_MEDIA_PROJECTION");
            MyApplication.setActivityResult(data, resultCode);
        }
    }
}
