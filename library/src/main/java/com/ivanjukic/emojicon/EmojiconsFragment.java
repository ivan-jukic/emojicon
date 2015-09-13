/*
 * Copyright 2014 Hieu Rocker
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ivanjukic.emojicon;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.*;
import android.widget.EditText;
import android.widget.ImageButton;

import com.ivanjukic.emojicon.adapters.EmojiconFragmentPagerAdapter;
import com.ivanjukic.emojicon.emoji.*;
import com.ivanjukic.emojicon.recent.EmojiconRecent;
import com.ivanjukic.emojicon.recent.EmojiconRecentGridFragment;
import com.ivanjukic.emojicon.recent.EmojiconRecentManager;
import com.ivanjukic.emojicon.slidingTab.SlidingTabLayout;
import com.ivanjukic.emojicon.utils.EmojiconUtil;

import java.util.Arrays;


public class EmojiconsFragment extends Fragment implements ViewPager.OnPageChangeListener, EmojiconRecent {
    protected static final String USE_SYSTEM_DEFAULT_KEY = "useSystemDefaults";

    protected View mView;
    protected ViewPager mEmojisPager;
    protected OnEmojiconBackspaceClickedListener mOnEmojiconBackspaceClickedListener;
    protected PagerAdapter mEmojisAdapter;
    protected EmojiconRecentManager mRecentManager;
    protected int mEmojiTabLastSelectedIndex = -1;
    protected boolean mUseSystemDefault = false;

    /**
     * Static method to create a new Emojicons fragment.
     * @param useSystemDefault - use system default icons.
     * @return - fragment
     */
    public static EmojiconsFragment newInstance(boolean useSystemDefault) {
        EmojiconsFragment fragment = new EmojiconsFragment();
        Bundle bundle = new Bundle();
        bundle.putBoolean(USE_SYSTEM_DEFAULT_KEY, useSystemDefault);
        fragment.setArguments(bundle);
        return fragment;
    }


    /**
     * Method which inflates the layout for the fragment, initializes tabs and sliding tab
     * layout / strip. It also attaches an event to the backspace functionality, and
     * when called, checks which tab was last selected and selects it by default (first tab
     * if none was selected before that).
     * @param inflater - layout inflater service
     * @param container - container for the layout
     * @param savedInstanceState - previous state of the fragment
     * @return - view
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        /// Inflate the view for the fragment.
        mView = inflater.inflate(R.layout.emojicons, container, false);

        mRecentManager = EmojiconRecentManager.getInstance(mView.getContext());

        /// Get reference to the view pager
        mEmojisPager = (ViewPager) mView.findViewById(R.id.emojis_pager);

        /// Previous value > mEmojisPager.setOnPageChangeListener(this); -> DEPRECATED
        mEmojisPager.addOnPageChangeListener(this);

        /// Setup icons tabs
        setupTabs();

        /// Set backspace functionality for the emojis...
        final ImageButton backspace = (ImageButton)mView.findViewById(R.id.emojis_backspace);
        backspace.setOnTouchListener(new RepeatListener(700, 50, new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (mOnEmojiconBackspaceClickedListener != null) {
                        mOnEmojiconBackspaceClickedListener.onEmojiconBackspaceClicked(v);
                    }
                    backspace.setImageResource(R.drawable.ic_backspace_darkgrey_24dp);
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    backspace.setImageResource(R.drawable.ic_backspace_grey_24dp);
                }
                return true;
            }
        }));

        return mView;
    }


    /**
     * ...
     */
    private void setupTabs() {
        // Handle recent...
        EmojiconRecent recent = this;
        mEmojisAdapter = new EmojiconFragmentPagerAdapter(getFragmentManager(), Arrays.asList(
                EmojiconRecentGridFragment.newInstance(mUseSystemDefault),
                EmojiconGridFragment.newInstance(People.DATA, recent, mUseSystemDefault),
                EmojiconGridFragment.newInstance(Nature.DATA, recent, mUseSystemDefault),
                EmojiconGridFragment.newInstance(Objects.DATA, recent, mUseSystemDefault),
                EmojiconGridFragment.newInstance(Places.DATA, recent, mUseSystemDefault),
                EmojiconGridFragment.newInstance(Symbols.DATA, recent, mUseSystemDefault)
        ));

        /// Set pager adapter.
        mEmojisPager.setAdapter(mEmojisAdapter);

        /// Initialize tab layout...
        final SlidingTabLayout tabs = (SlidingTabLayout) mView.findViewById(R.id.emojis_tab_view);
        tabs.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
            @Override
            public int getIndicatorColor(int position) {
                return getResources().getColor(R.color.tabsScrollColor);
            }
        });

        /// Detect when the view is ready and calculate single tab width!
        ViewTreeObserver vto = mView.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                    mView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                } else {
                    mView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }

                /// Get display metrics.
                Point display = EmojiconUtil.getDisplaySize(getActivity());

                /// We need display metrics to calculate the available width for our tab elements...
                /// To successfully calculate this, we also need the width of the backspace button.
                ImageButton backspace = (ImageButton) mView.findViewById(R.id.emojis_backspace);
                int tabWidth = (int) Math.floor(
                        (display.x - backspace.getMeasuredWidth()) / mEmojisAdapter.getCount()
                );
                tabs.setTabWidth(tabWidth);
                tabs.setViewPager(mEmojisPager);
                setLastSelectedPage();
            }
        });
    }


    /**
     * Read from the recent manager, which page was the last one selected.
     */
    protected void setLastSelectedPage() {
        /// Get last selected page
        //mRecentManager = EmojiconRecentManager.getInstance(mView.getContext());
        int page = mRecentManager.getRecentPage();

        // last page was recents, check if there are recents to use, if none was found, go to page 1
        if (page == 0 && mRecentManager.size() == 0) {
            page = 1;
        }
        if (page == 0) {
            onPageSelected(page);
        } else {
            mEmojisPager.setCurrentItem(page, false);
        }
    }


    /**
     *
     * @param activity
     */
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (getActivity() instanceof OnEmojiconBackspaceClickedListener) {
            mOnEmojiconBackspaceClickedListener = (OnEmojiconBackspaceClickedListener) getActivity();
        } else if(getParentFragment() instanceof  OnEmojiconBackspaceClickedListener) {
            mOnEmojiconBackspaceClickedListener = (OnEmojiconBackspaceClickedListener) getParentFragment();
        } else {
            throw new IllegalArgumentException(activity + " must implement interface " + OnEmojiconBackspaceClickedListener.class.getSimpleName());
        }
    }


    /**
     *
     */
    @Override
    public void onDetach() {
        mOnEmojiconBackspaceClickedListener = null;
        super.onDetach();
    }


    /**
     *
     * @param editText
     * @param emojicon
     */
    public static void input(EditText editText, Emojicon emojicon) {
        if (editText == null || emojicon == null) {
            return;
        }
        int start = editText.getSelectionStart();
        int end = editText.getSelectionEnd();
        if (start < 0) {
            editText.append(emojicon.getEmoji());
        } else {
            editText.getText().replace(Math.min(start, end), Math.max(start, end), emojicon.getEmoji(), 0, emojicon.getEmoji().length());
        }
    }


    /**
     *
     * @param context
     * @param emojicon
     */
    @Override
    public void addRecentEmoji(Context context, Emojicon emojicon) {
        if (null != mEmojisPager) {
            EmojiconRecentGridFragment fragment = (EmojiconRecentGridFragment) mEmojisAdapter.instantiateItem(mEmojisPager, 0);
            fragment.addRecentEmoji(context, emojicon);
        }
    }


    /**
     *
     * @param editText
     */
    public static void backspace(EditText editText) {
        KeyEvent event = new KeyEvent(0, 0, 0, KeyEvent.KEYCODE_DEL, 0, 0, 0, 0, KeyEvent.KEYCODE_ENDCALL);
        editText.dispatchKeyEvent(event);
    }


    /**
     *
     * @param i
     */
    @Override
    public void onPageSelected(int i) {
        if (mEmojiTabLastSelectedIndex == i) {
            return;
        }
        if (0 <= i &&  5 >= i) {
            /// TODO: check if this commented code is necessary
           /* if (mEmojiTabLastSelectedIndex >= 0 && mEmojiTabLastSelectedIndex < mEmojiTabs.length) {
                mEmojiTabs[mEmojiTabLastSelectedIndex].setSelected(false);
            }
            mEmojiTabs[i].setSelected(true);*/
            mEmojiTabLastSelectedIndex = i;
            mRecentManager.setRecentPage(i);
        }
    }


    /**
     *
     * @param i
     * @param v
     * @param i2
     */
    @Override
    public void onPageScrolled(int i, float v, int i2) {
    }


    /**
     *
     * @param i
     */
    @Override
    public void onPageScrollStateChanged(int i) {
    }


    /**
     * Interface for backspace clicked listener.
     */
    public interface OnEmojiconBackspaceClickedListener {
        void onEmojiconBackspaceClicked(View v);
    }


    /**
     * Executed on fragment created...
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mUseSystemDefault = getArguments().getBoolean(USE_SYSTEM_DEFAULT_KEY);
        } else {
            mUseSystemDefault = false;
        }
    }
}
