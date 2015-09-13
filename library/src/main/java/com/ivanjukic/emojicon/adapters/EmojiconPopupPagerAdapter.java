package com.ivanjukic.emojicon.adapters;

import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;

import com.ivanjukic.emojicon.EmojiconPopupGridView;
import com.ivanjukic.emojicon.R;
import com.ivanjukic.emojicon.recent.EmojiconRecentGridView;

import java.util.List;

public class EmojiconPopupPagerAdapter extends PagerAdapter implements EmojiconPagerAdapterInterface {

    private List<EmojiconPopupGridView> views;
    private int[] tabIconIds = {
            R.drawable.ic_emoji_recent,
            R.drawable.ic_emoji_people,
            R.drawable.ic_emoji_nature,
            R.drawable.ic_emoji_objects,
            R.drawable.ic_emoji_places,
            R.drawable.ic_emoji_symbols
    };


    public EmojiconRecentGridView getRecentFragment(){
        for (EmojiconPopupGridView it : views) {
            if(it instanceof EmojiconRecentGridView)
                return (EmojiconRecentGridView)it;
        }
        return null;
    }


    public EmojiconPopupPagerAdapter(List<EmojiconPopupGridView> views) {
        super();
        this.views = views;
    }


    @Override
    public int getCount() {
        return views.size();
    }


    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View v = views.get(position).rootView;
        ((ViewPager)container).addView(v, 0);
        return v;
    }


    @Override
    public void destroyItem(ViewGroup container, int position, Object view) {
        ((ViewPager)container).removeView((View) view);
    }


    @Override
    public boolean isViewFromObject(View view, Object key) {
        return key == view;
    }


    public int getDrawableId(int position) {
        return tabIconIds[position];
    }
}
