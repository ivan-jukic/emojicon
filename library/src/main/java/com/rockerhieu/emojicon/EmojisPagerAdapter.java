package com.rockerhieu.emojicon;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.List;

public class EmojisPagerAdapter extends FragmentPagerAdapter {

    private List<EmojiconGridFragment> fragments;
    private int[] tabIconIds = {
            R.drawable.ic_emoji_recent,
            R.drawable.ic_emoji_people,
            R.drawable.ic_emoji_nature,
            R.drawable.ic_emoji_objects,
            R.drawable.ic_emoji_places,
            R.drawable.ic_emoji_symbols
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