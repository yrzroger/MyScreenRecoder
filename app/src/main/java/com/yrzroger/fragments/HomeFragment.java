package com.yrzroger.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.yrzroger.R;
import com.yrzroger.MyApplication;
import com.yrzroger.services.FloatBallService;

import static android.os.Build.VERSION_CODES.M;

/**
 * Created by roger_yu on 2018/06/02.
 */
public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment";
    private static final int REQUEST_OVERLAY = 0;
	private View mView;
    private Button mBtnSwitch;
    private boolean isWorking = false;
    private Intent mData;
    private int mRequestCode;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mView = inflater.inflate(R.layout.home_fragment, container, false);
        initView();
		return mView;
	}
    /**
     * 初始化View组件并并绑定事件监听器
     */
    private void initView() {


        mBtnSwitch = (Button) mView.findViewById(R.id.btnSwitch);

        mBtnSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= M) {
                    if (!Settings.canDrawOverlays(MyApplication.getInstance())) {
                        showRequestOverlayDialog();
                        return;
                    }
                }

                isWorking = !isWorking;
                if(isWorking) {
                    mBtnSwitch.setText("隐藏悬浮窗");

                    mRequestCode = MyApplication.getActivityResultCode();
                    mData = MyApplication.getActivityResultIntent();
                    Intent intent = new Intent(MyApplication.getInstance(), FloatBallService.class);
                    intent.putExtra("code", mRequestCode);
                    intent.putExtra("data", mData);
                    intent.putExtra("type", FloatBallService.TYPE_ADD);
                    MyApplication.getInstance().startService(intent);
                }
                else {
                    mBtnSwitch.setText("开启悬浮窗");
                    Intent intent = new Intent(MyApplication.getInstance(), FloatBallService.class);
                    Bundle data = new Bundle();
                    data.putInt("type", FloatBallService.TYPE_DEL);
                    intent.putExtras(data);
                    MyApplication.getInstance().startService(intent);
                }

            }
        });
    }

    private void showRequestOverlayDialog(){
        final AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
        dialog.setIcon(R.drawable.ic_launcher);
        dialog.setTitle("悬浮窗权限");
        dialog.setMessage("检测到您的手机没有授权悬浮窗权限，请前往开启后才能使用悬浮窗功能");
        dialog.setPositiveButton("去开启", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + MyApplication.getInstance().getPackageName()));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivityForResult(intent, REQUEST_OVERLAY);
            }
        });
        dialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //...To-do
            }
        });
        dialog.show();
    }
}



