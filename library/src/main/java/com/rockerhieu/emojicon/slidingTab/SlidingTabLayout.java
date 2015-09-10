/*
 * Copyright 2014 Google Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.rockerhieu.emojicon.slidingTab;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.rockerhieu.emojicon.EmojisPagerAdapter;
import com.rockerhieu.emojicon.R;

/**
 * To be used with ViewPager to provide a tab indicator component which give constant feedback as to
 * the user's scroll progress.
 * <p>
 * To use the component, simply add it to your view hierarchy. Then in your
 * {@link android.app.Activity} or {@link android.support.v4.app.Fragment} call
 * {@link #setViewPager(ViewPager)} providing it the ViewPager this layout is being used for.
 * <p>
 * The colors can be customized in two ways. The first and simplest is to provide an array of colors
 * via {@link #setSelectedIndicatorColors(int...)}. The
 * alternative is via the {@link TabColorizer} interface which provides you complete control over
 * which color is used for any individual position.
 * <p>
 */
public class SlidingTabLayout extends HorizontalScrollView {

    /**
     * Allows complete control over the colors drawn in the tab layout. Set with
     * {@link #setCustomTabColorizer(TabColorizer)}.
     */
    public interface TabColorizer {
        /**
         * @return return the color of the indicator used when {@code position} is selected.
         */
        int getIndicatorColor(int position);
    }

    protected static final int TITLE_OFFSET_DIPS = 24;
    protected static final int TAB_VIEW_PADDING_DIPS = 16;

    protected int mTitleOffset;
    protected int mTabWidth = -1;

    protected Context mContext;
    protected ViewPager mViewPager;
    protected SparseArray<String> mContentDescriptions = new SparseArray<String>();
    protected ViewPager.OnPageChangeListener mViewPagerPageChangeListener;
    protected final SlidingTabStrip mTabStrip;


    /**
     * Basic constructor
     * @param context - context in which tab layout is used
     */
    public SlidingTabLayout(Context context) {
        this(context, null);
    }


    /**
     *
     * @param context - context in which tab layout is used
     * @param attrs - attrs
     */
    public SlidingTabLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        mContext = context;
    }


    /**
     *
     * @param context  - context in which tab layout is used
     * @param attrs - attr
     * @param defStyle - style
     */
    public SlidingTabLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
        // Disable the visible Scroll Bar...
        setHorizontalScrollBarEnabled(false);
        // Make sure that the Tab Strips fills this View...
        setFillViewport(true);
        mTitleOffset = (int)(TITLE_OFFSET_DIPS * getResources().getDisplayMetrics().density);
        mTabStrip = new SlidingTabStrip(context);
        addView(mTabStrip, LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
    }


    /**
     * Set the custom {@link TabColorizer} to be used.
     *
     * If you only require simple customization then you can use
     * {@link #setSelectedIndicatorColors(int...)} to achieve
     * similar effects.
     */
    public void setCustomTabColorizer(TabColorizer tabColorizer) {
        mTabStrip.setCustomTabColorizer(tabColorizer);
    }


    /**
     * Sets the width for the tabs.
     * @param width - width in pixels
     */
    public void setTabWidth(int width) {
        mTabWidth = width;
    }


    /**
     * Sets the colors to be used for indicating the selected tab. These colors are treated as a
     * circular array. Providing one color will mean that all tabs are indicated with the same color.
     */
    public void setSelectedIndicatorColors(int... colors) {
        mTabStrip.setSelectedIndicatorColors(colors);
    }


    /**
     * Set the {@link ViewPager.OnPageChangeListener}. When using {@link SlidingTabLayout} you are
     * required to set any {@link ViewPager.OnPageChangeListener} through this method. This is so
     * that the layout can update it's scroll position correctly.
     *
     * @see ViewPager#setOnPageChangeListener(ViewPager.OnPageChangeListener)
     */
    public void setOnPageChangeListener(ViewPager.OnPageChangeListener listener) {
        mViewPagerPageChangeListener = listener;
    }


    /**
     * Sets the associated view pager. Note that the assumption here is that the pager content
     * (number of tabs and tab titles) does not change after this call has been made.
     */
    public void setViewPager(ViewPager viewPager) {
        mTabStrip.removeAllViews();
        mViewPager = viewPager;
        if (viewPager != null) {
            viewPager.addOnPageChangeListener(new InternalViewPagerListener());
            populateTabStrip();
        }
    }


    /**
     * This method was custom added by Ivan, based on the post from
     * http://stackoverflow.com/questions/28125794/slidingtablayout-with-icons-only/28134763#28134763
     * @param context
     * @return
     */
    protected ImageView createDefaultImageView(Context context) {
        ImageView imageView = new ImageView(context);

        int padding = (int) (TAB_VIEW_PADDING_DIPS * getResources().getDisplayMetrics().density);
        imageView.setPadding(padding, padding, padding, padding);

        int width = (int) (getResources().getDisplayMetrics().widthPixels / mViewPager.getAdapter().getCount());
        imageView.setMinimumWidth(width);

        return imageView;
    }


    /**
     * Method which populates tab strip with icon tabs, which represent each page with icons.
     */
    private void populateTabStrip() {
        final EmojisPagerAdapter adapter = (EmojisPagerAdapter) mViewPager.getAdapter();
        final View.OnClickListener tabClickListener = new TabClickListener();
        View tabView;

        for (int i = 0; i < adapter.getCount(); i++) {
            ImageView tabIconView = null;

            /// Initialize tab view, get ImageView
            tabView = createDefaultImageView(getContext());
            if (ImageView.class.isInstance(tabView)) {
                tabIconView = (ImageView)tabView;
            }

            //Drawable draw = res.getDrawable(adapter.getDrawableId(i)); --> Deprecated
            Drawable draw = ResourcesCompat.getDrawable(getResources(), adapter.getDrawableId(i), null);

            /// Set icon for the tab...
            if (null != tabIconView) {
                tabIconView.setImageDrawable(draw);
                if (mViewPager.getCurrentItem() == i) {
                    tabIconView.setSelected(true);
                }
            }

            /// Create new layout parameters for this tab which is ImageView element...
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
            /// Set width for the element if its defined...
            if (0 < mTabWidth) {
                lp.width = mTabWidth;
            } else {
                lp.width = 0;
                lp.weight = 1;
            }
            /// Set layout parameters for this tab.
            tabView.setLayoutParams(lp);

            /// Attach on click listener and add tab to view...
            tabView.setOnClickListener(tabClickListener);
            mTabStrip.addView(tabView);
        }
    }


    /**
     * Currently not being used...
     * @param i - tab index
     * @param desc - description
     */
    public void setContentDescription(int i, String desc) {
        mContentDescriptions.put(i, desc);
    }


    /**
     * ...
     */
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        if (mViewPager != null) {
            scrollToTab(mViewPager.getCurrentItem(), 0);
        }
    }


    /**
     * Slide to tab.
     * @param tabIndex - tab to scroll to
     * @param positionOffset - current offset between pages
     */
    private void scrollToTab(int tabIndex, int positionOffset) {
        final int tabStripChildCount = mTabStrip.getChildCount();
        if (tabStripChildCount == 0 || tabIndex < 0 || tabIndex >= tabStripChildCount) {
            return;
        }

        View selectedChild = mTabStrip.getChildAt(tabIndex);
        if (selectedChild != null) {
            int targetScrollX = selectedChild.getLeft() + positionOffset;

            if (tabIndex > 0 || positionOffset > 0) {
                // If we're not at the first child and are mid-scroll, make sure we obey the offset
                targetScrollX -= mTitleOffset;
            }

            scrollTo(targetScrollX, 0);
        }
    }


    /**
     * ...
     */
    private class InternalViewPagerListener implements ViewPager.OnPageChangeListener {
        private int mScrollState;

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            int tabStripChildCount = mTabStrip.getChildCount();
            if ((tabStripChildCount == 0) || (position < 0) || (position >= tabStripChildCount)) {
                return;
            }

            mTabStrip.onViewPagerPageChanged(position, positionOffset);

            View selectedTitle = mTabStrip.getChildAt(position);
            int extraOffset = (selectedTitle != null)
                    ? (int) (positionOffset * selectedTitle.getWidth())
                    : 0;
            scrollToTab(position, extraOffset);

            if (mViewPagerPageChangeListener != null) {
                mViewPagerPageChangeListener.onPageScrolled(position, positionOffset,
                        positionOffsetPixels);
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            mScrollState = state;

            if (mViewPagerPageChangeListener != null) {
                mViewPagerPageChangeListener.onPageScrollStateChanged(state);
            }
        }

        @Override
        public void onPageSelected(int position)
        {
            if (mScrollState == ViewPager.SCROLL_STATE_IDLE) {
                mTabStrip.onViewPagerPageChanged(position, 0f);
                scrollToTab(position, 0);
            }

            for (int i = 0; i < mTabStrip.getChildCount(); i++) {
                mTabStrip.getChildAt(i).setSelected(false);
            }

            mTabStrip.getChildAt(position).setSelected(true);

            if (mViewPagerPageChangeListener != null) {
                mViewPagerPageChangeListener.onPageSelected(position);
            }
        }

    }


    /**
     * ...
     */
    private class TabClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            for (int i = 0; i < mTabStrip.getChildCount(); i++) {
                if (v == mTabStrip.getChildAt(i)) {
                    mViewPager.setCurrentItem(i);
                    return;
                }
            }
        }
    }
}