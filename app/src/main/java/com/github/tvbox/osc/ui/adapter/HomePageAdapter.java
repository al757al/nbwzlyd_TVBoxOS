package com.github.tvbox.osc.ui.adapter;

import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentTransaction;

import com.github.tvbox.osc.base.BaseLazyFragment;

import java.util.List;

/**
 * @user acer
 * @date 2018/12/4
 */

public class HomePageAdapter extends FragmentPagerAdapter {
    public FragmentManager fragmentManager;
    public List<BaseLazyFragment> list;

    public HomePageAdapter(FragmentManager fm) {
        super(fm);
    }

    public HomePageAdapter(FragmentManager fm, List<BaseLazyFragment> list) {
        super(fm);
        this.fragmentManager = fm;
        this.list = list;
    }

    public void setFragments(List<BaseLazyFragment> fragments) {
        if (list != null) {
            FragmentTransaction ft = fragmentManager.beginTransaction();
            for (Fragment f : this.list) {
                ft.remove(f);
            }
            ft.commit();
            ft = null;
            fragmentManager.executePendingTransactions();
        }
        this.list = fragments;
        notifyDataSetChanged();
    }

    public void clear() {
        list.clear();
        notifyDataSetChanged();
    }

    @Override
    public Fragment getItem(int position) {
        return list.get(position);
    }

    @Override
    public int getCount() {
        return list != null ? list.size() : 0;
    }

    @Override
    public Fragment instantiateItem(ViewGroup container, int position) {
        Fragment fragment = (Fragment) super.instantiateItem(container, position);
        fragmentManager.beginTransaction().show(fragment).commitAllowingStateLoss();
        return fragment;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        // super.destroyItem(container, position, object);
        if (position >= list.size()) {
            return;
        }
        Fragment fragment = list.get(position);
        fragmentManager.beginTransaction().hide(fragment).commitAllowingStateLoss();
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }
}
