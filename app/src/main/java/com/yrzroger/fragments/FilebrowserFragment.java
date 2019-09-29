package com.yrzroger.fragments;

import android.graphics.SurfaceTexture;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.yrzroger.MyApplication;
import com.yrzroger.R;
import com.yrzroger.fragments.adapter.CommonSettings;
import com.yrzroger.fragments.adapter.FilebrowserItem;
import com.yrzroger.fragments.adapter.FilebrowserItemAdapter;
import com.yrzroger.ui.DragVideoView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.logging.Logger;


public class FilebrowserFragment extends Fragment implements DragVideoView.Callback, MediaPlayer.OnPreparedListener, TextureView.SurfaceTextureListener{

	private static final Logger log = Logger.getLogger(FilebrowserFragment.class.getSimpleName());
	private final static int MEDIA_VIDEO = 0;
	private final static int MEDIA_STREAM = 2;

	private ListView mFileListView;
	private TextView mPathField;

	private String mCurrentDirName = null;
	private File mCurrentDir = null;

	private final ArrayList<FilebrowserItem> mFileListItems = new ArrayList<>();
	private FilebrowserItemAdapter mFileListAdapter;

	private Thread mGetMediaInfoThread = null;
	private boolean mGetMediaInfoThreadRunning = false;

	private View mView;
	private DragVideoView mDragVideoView;
	private ListView mDetailInfoListView;
	private TextureView mVideoView;
	private MediaPlayer mMediaPlayer = null;
	private Surface mSurface = null;

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mView = inflater.inflate(R.layout.filebrowser_fragment, container, false);
		start();
		return mView;
	}

	@Override
	public void onPause() {
		log.info("onPause()");
		super.onPause();
	}

	@Override
	public void onStop() {
		log.info("onStop()");
		super.onStop();
	}

	@Override
	public void onDestroy() {
		mGetMediaInfoThreadRunning = false;
		if (mGetMediaInfoThread != null) {
			try {
				mGetMediaInfoThread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		mGetMediaInfoThread = null;

        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.stop();
        }
        mMediaPlayer.release();
		super.onDestroy();
	}


	private void start() {

		loadState();

		mPathField = (TextView) mView.findViewById(R.id.PathField);

		mFileListAdapter = new FilebrowserItemAdapter(MyApplication.getInstance(), R.layout.filebrowser_item, mFileListItems);
		mFileListView = (ListView) mView.findViewById(R.id.listview);
		mFileListView.setAdapter(mFileListAdapter);

		mFileListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
				String file = mFileListAdapter.getItem(position).mFilename;

				if (file != null && file.contains("..")) { // switch to parent directory
					mCurrentDirName = mCurrentDirName.substring(0, mCurrentDirName.lastIndexOf("/"));
					mCurrentDir = new File(mCurrentDirName);
					updateFileList(MEDIA_VIDEO);
					return;
				} else {
					String tmpDirName = mCurrentDirName + "/" + file;
					File tmpDir = new File(tmpDirName);

					if (tmpDir.isDirectory()) { //switch to child directory
						mCurrentDirName = tmpDirName;
						mCurrentDir = tmpDir;
						updateFileList(MEDIA_VIDEO);
						return;
					}
				}

				if (!mFileListItems.isEmpty()) {
					if (mFileListItems.get(0).mFilename.equals("..")) {
						CommonSettings.filebrowser_items = new ArrayList<>(mFileListItems.subList(1, mFileListItems.size()));
						CommonSettings.filebrowser_itemsposition = position - 1;
					} else {
						CommonSettings.filebrowser_items = new ArrayList<>(mFileListItems);
						CommonSettings.filebrowser_itemsposition = position;
					}
					String filename = CommonSettings.filebrowser_items.get(CommonSettings.filebrowser_itemsposition).mFilename;
					String[] splitfilename = filename.split("\\.");
					if (splitfilename.length > 0) {
						CommonSettings.filebrowser_items.get(CommonSettings.filebrowser_itemsposition).mFileSuffix = splitfilename[splitfilename.length - 1];
					}
					if (CommonSettings.general_supported_media_suffixes.contains(CommonSettings.filebrowser_items.get(CommonSettings.filebrowser_itemsposition).mFileSuffix)) {
						if (!mFileListItems.get(position).mInfoAvailable) {
							getMediaInfo(position);
						}
					}

				} else {
					return;
				}

				CommonSettings.filebrowser_itemspath = mCurrentDirName;

				FilebrowserItem item = CommonSettings.filebrowser_items.get(CommonSettings.filebrowser_itemsposition);
				if(item.mHasVideo) {
					String fileFullPath = mCurrentDirName + "/" + item.mFilename;
					log.info("play video:"+fileFullPath);

					mDragVideoView.show();

					if (mMediaPlayer != null && mMediaPlayer.isPlaying()){
						mMediaPlayer.stop();
						mMediaPlayer.release();
						mMediaPlayer = null;
					}
					mMediaPlayer = new MediaPlayer();
					mMediaPlayer.setOnPreparedListener(FilebrowserFragment.this);

					mMediaPlayer.setSurface(mSurface);
					try {
						mMediaPlayer.setDataSource(fileFullPath);
						mMediaPlayer.prepare();
					} catch (Exception e) {
						e.printStackTrace();
					}
					mMediaPlayer.start();

					ArrayList info = new ArrayList<String>();

					info.add("文件名: "+item.mFilename);
					info.add("分辨率: "+item.mWidth+"*"+item.mHeight);
					info.add("时长: "+FilebrowserItem.stringForTime(item.mDuration));

					ArrayAdapter<String> infoAdapter = new ArrayAdapter<String>
							(MyApplication.getInstance(), R.layout.simple_list_item_0, info);
					mDetailInfoListView.setAdapter(infoAdapter);
				}

			}
		});

		mFileListView.setOnKeyListener(new View.OnKeyListener() {
			@Override
			public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {

				if (keyCode != KeyEvent.KEYCODE_ENTER &&
						keyCode != KeyEvent.KEYCODE_BACK &&
						keyCode != KeyEvent.KEYCODE_DPAD_CENTER &&
						keyCode != KeyEvent.KEYCODE_BUTTON_B &&
						keyCode != KeyEvent.KEYCODE_BUTTON_A) {
					return keyEvent.getAction() != KeyEvent.ACTION_DOWN /*|| onKeyDown(keyCode, keyEvent)*/;
				} else {
					return false;
				}
			}
		});

		mDragVideoView = (DragVideoView) mView.findViewById(R.id.drag_view);
		mDragVideoView.setCallback(this);
		mDetailInfoListView = (ListView) mView.findViewById(R.id.lv_info);
		mVideoView = (TextureView) mView.findViewById(R.id.video_view);
		mVideoView.setSurfaceTextureListener(this);
		mCurrentDir = new File(mCurrentDirName);

		updateFileList(MEDIA_VIDEO);
	}

	@Override
	public void onDisappear(int direct) {
		//DragVideoView消失时的回调函数
        log.info("DragVideoView onDisappear");
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            mMediaPlayer.stop();
			mMediaPlayer.release();
			mMediaPlayer = null;
        }

		mFileListView.setAlpha(1.0f);
		mFileListView.setVisibility(View.VISIBLE);
	}
	@Override
	public void onPrepared(MediaPlayer mp) {
		mMediaPlayer.setLooping(true);
	}

	@Override
	public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
		mSurface = new Surface(surface);
	}

	@Override
	public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
		log.info(">> onSurfaceTextureSizeChanged width=" + width + ", height=" + height);
		if (width == 537 && height == 302) {
			mFileListView.setAlpha(1.0f);
		} else {
			float f = (float) ((1.0 - ((float)width/1080))* 1.0f);
			log.info(">> onSurfaceTextureSizeChanged f=" + f );
			mFileListView.setAlpha(f);
		}
		mFileListView.setVisibility(View.VISIBLE);
	}

	@Override
	public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
		return true;
	}

	@Override
	public void onSurfaceTextureUpdated(SurfaceTexture surface) {

	}
	private void updateFileList(int type) {
		mGetMediaInfoThreadRunning = false;
		if (mGetMediaInfoThread != null) {
			try {
				mGetMediaInfoThread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		mGetMediaInfoThread = null;
		mFileListAdapter.clear();
		mFileListAdapter.notifyDataSetInvalidated();

		if (type == MEDIA_STREAM) {
			mPathField.setVisibility(View.GONE);
		} else {
			mPathField.setVisibility(View.VISIBLE);
			scanDirectory(mCurrentDir);
			CommonSettings.general_lastpath = mCurrentDirName;
		}
		mFileListAdapter.notifyDataSetChanged();
	}


	private void scanDirectory(File f) {
		mFileListItems.clear();
		mPathField.setText(f.getAbsolutePath());
		File[] items = f.listFiles();

		ArrayList<String> dirs = new ArrayList<>();
		ArrayList<String> files = new ArrayList<>();

		for (File ff : items) {
			if (ff.isDirectory()) {
				dirs.add(ff.getName());
			} else {
				files.add(ff.getName());
			}
		}

		Collections.sort(dirs);
		Collections.sort(files);

		dirs.addAll(files);
		if (!f.getName().equalsIgnoreCase("ScreenRecorder")) {
			dirs.add(0, " ..");
		}

		for (String el : dirs) {
			mFileListItems.add(new FilebrowserItem(el));
		}

		mGetMediaInfoThread = new Thread(new Runnable() {
			@Override
			public void run() {

				int size = mFileListItems.size();
				int f = 0;
				while (mGetMediaInfoThreadRunning && (f < size)) {

					String filename = mFileListItems.get(f).mFilename;
					String[] splitfilename = filename.split("\\.");
					if (splitfilename.length > 0) {
						mFileListItems.get(f).mFileSuffix = splitfilename[splitfilename.length - 1];
					}
					if (CommonSettings.general_supported_media_suffixes.contains(mFileListItems.get(f).mFileSuffix)) {
						if (mFileListItems.get(f).mFps == -1.0) {
							getMediaInfo(f);
							getActivity().runOnUiThread(new Runnable() {
								@Override
								public void run() {
									mFileListAdapter.notifyDataSetChanged();
								}
							});
						}
					}
					f++;
				}
				mGetMediaInfoThreadRunning = false;
			}
		});

		mGetMediaInfoThreadRunning = true;
		mGetMediaInfoThread.start();
	}

	private void getMediaInfo(int position) {
		FilebrowserItem item = mFileListItems.get(position);

		item.mInfoAvailable = true;

		String fileFullPath = mCurrentDirName + "/" + item.mFilename;

		log.info("file = " + fileFullPath);
		MediaExtractor mediaExtractor = new MediaExtractor();
		try {
			mediaExtractor.setDataSource(fileFullPath);
		} catch (IOException e) {
			e.printStackTrace();
		}

		int numTracks = mediaExtractor.getTrackCount();
		log.info("numTracks = " + numTracks);

		int audioTrackIndex = -1;
		int videoTrackIndex = -1;

		for (int i = 0; i < numTracks; i++) {

			MediaFormat format = mediaExtractor.getTrackFormat(i);
			log.info("format of track " + i + ": " + format);

			String mime = null;
			if (format.containsKey(MediaFormat.KEY_MIME)) {
				mime = format.getString(MediaFormat.KEY_MIME);
				//log.info("mime of track " + i + ": " + mime);
			}

			if (format.containsKey(MediaFormat.KEY_DURATION)) {
				long duration = format.getLong(MediaFormat.KEY_DURATION) / 1000;
				if (duration > item.mDuration) {
					item.mDuration = duration;
				}
			}
			if (mime != null) {
				if (mime.startsWith("video/")) {
					item.mHasVideo = true;
					if (format.containsKey(MediaFormat.KEY_WIDTH)) {
						item.mWidth = format.getInteger(MediaFormat.KEY_WIDTH);
					}
					if (format.containsKey(MediaFormat.KEY_HEIGHT)) {
						item.mHeight = format.getInteger(MediaFormat.KEY_HEIGHT);
					}
				}
			}
		}

		//创建MediaMetadataRetriever对象
		MediaMetadataRetriever mmr = new MediaMetadataRetriever();
		//绑定资源
		mmr.setDataSource(fileFullPath);
		//获取第一帧图像的bitmap对象
		item.mThumbnail = mmr.getFrameAtTime(100);

		mediaExtractor.release();
	}

	private void loadState() {
		CommonSettings.getInstance().loadState(MyApplication.getInstance());

		mCurrentDirName = CommonSettings.general_lastpath;
		File f = new File(mCurrentDirName);
		if (!f.exists()) {
			mCurrentDirName = getString(R.string.RootPath);
			File dir = new File(mCurrentDirName);
			if(!dir.exists())
				dir.mkdir();

		}
		log.info("mCurrentDirName="+mCurrentDirName);
	}

}
