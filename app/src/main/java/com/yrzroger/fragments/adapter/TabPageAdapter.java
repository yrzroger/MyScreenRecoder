package com.yrzroger.fragments.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.ViewGroup;

import java.util.List;

/**
 * Created by lt on 2015/12/1.
 * app������������������
 */
public class TabPageAdapter extends FragmentPagerAdapter{

    private List<Fragment> fragments;
    public TabPageAdapter(FragmentManager fm,List<Fragment> fragments) {
        super(fm);
        this.fragments = fragments;
    }

    @Override
    public Fragment getItem(int position) {
        return fragments.get(position);
    }

    @Override
    public int getCount() {
        return fragments.size();
    }

    /**
     * ��д������Fragment����
     */
    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {

    }
}
