package com.yrzroger.fragments.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.yrzroger.R;

import java.util.ArrayList;

public class FilebrowserItemAdapter extends ArrayAdapter<FilebrowserItem> {
    public static final String TAG = "FilebrowserItemAdapter";


    private Context mContext;

    public FilebrowserItemAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
        mContext = context;
    }

    public FilebrowserItemAdapter(Context context, int resource, ArrayList<FilebrowserItem> items) {
        super(context, resource, items);

        mContext = context;


    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View v = convertView;

        if (v == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(getContext());
            v = vi.inflate(R.layout.filebrowser_item, null);
        }

        TextView tvfile = (TextView)v.findViewById(R.id.file);
        TextView tvduration = (TextView)v.findViewById(R.id.duration);

        RelativeLayout rlvideoinfo = (RelativeLayout)v.findViewById(R.id.videoinfo);
        ImageView thumbnail= (ImageView)v.findViewById(R.id.videoThumbnail);


        FilebrowserItem p = getItem(position);

        if (p != null) {
            if (CommonSettings.general_supported_media_suffixes.contains(p.mFileSuffix)) {
                tvfile.setText(p.mFilename);
                tvfile.setVisibility(View.VISIBLE);

                if (p.mDuration == 0) {
                    tvduration.setVisibility(View.INVISIBLE);
                } else {
                    tvduration.setText(FilebrowserItem.stringForTime(p.mDuration));
                    tvduration.setVisibility(View.VISIBLE);
                }

                if (p.mHasVideo) {
                    rlvideoinfo.setVisibility(View.VISIBLE);

                } else {
                    rlvideoinfo.setVisibility(View.INVISIBLE);
                }
                if(p.mThumbnail != null) {
                    Log.i(TAG, "setImageBitmap");
                    thumbnail.setImageBitmap(p.mThumbnail);
                    thumbnail.setVisibility(View.VISIBLE);
                }





            } else {
                tvfile.setText(p.mFilename);
                tvfile.setVisibility(View.VISIBLE);
                tvduration.setVisibility(View.GONE);

                rlvideoinfo.setVisibility(View.GONE);

            }
        }

        return v;
    }

}
