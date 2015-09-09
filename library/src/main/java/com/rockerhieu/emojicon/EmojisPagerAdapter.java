package com.rockerhieu.emojicon;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.List;

public class EmojisPagerAdapter extends FragmentPagerAdapter {

    private List<EmojiconGridFragment> fragments;
    private int[] tabIconIds = {
            R.drawable.ic_access_time_black_18dp,
            R.drawable.ic_mood_black_18dp,
            R.drawable.ic_filter_vintage_black_18dp,
            R.drawable.ic_toys_black_18dp,
            R.drawable.ic_directions_car_black_18dp,
            R.drawable.ic_change_history_black_18dp,
    };

    public EmojisPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    public EmojisPagerAdapter(FragmentManager fm, List<EmojiconGridFragment> fragments) {
        super(fm);
        this.fragments = fragments;
    }

    @Override
    public Fragment getItem(int i) {
        return fragments.get(i);
    }

    @Override
    public int getCount() {
        return fragments.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return "Test";
    }

    public int getDrawableId(int position) {
        return tabIconIds[position];
    }
}