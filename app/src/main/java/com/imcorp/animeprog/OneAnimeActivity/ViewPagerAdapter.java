package com.imcorp.animeprog.OneAnimeActivity;


import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.imcorp.animeprog.OneAnimeActivity.Fragments.MyFragment;

import java.util.ArrayList;

public class ViewPagerAdapter extends FragmentPagerAdapter {
    private final ArrayList<MyFragment> fragments = new ArrayList<>(2);
    private final ArrayList<String> titles = new ArrayList<>(2);
    public ViewPagerAdapter(FragmentManager fm){
        super(fm);
    }
    @Override
    public Fragment getItem(int position) {
        return (Fragment)fragments.get(position);
    }
    @Override
    public int getCount() {
        return fragments.size();
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return titles.get(position);
    }
    public void addFragment(MyFragment fragment,String title){
        fragments.add(fragment);
        titles.add(title);
    }
}
