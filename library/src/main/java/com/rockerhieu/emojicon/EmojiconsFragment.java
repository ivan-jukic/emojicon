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

package com.rockerhieu.emojicon;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.*;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.rockerhieu.emojicon.emoji.*;
import com.rockerhieu.emojicon.recents.EmojiconRecents;
import com.rockerhieu.emojicon.recents.EmojiconRecentsGridFragment;
import com.rockerhieu.emojicon.recents.EmojiconRecentsManager;
import com.rockerhieu.emojicon.slidingTab.SlidingTabLayout;

import java.util.Arrays;

/**
 * @author Hieu Rocker (rockerhieu@gmail.com).
 */
public class EmojiconsFragment extends Fragment implements ViewPager.OnPageChangeListener, EmojiconRecents {
    private static final String USE_SYSTEM_DEFAULT_KEY = "useSystemDefaults";

    private int mEmojiTabLastSelectedIndex = -1;
    private boolean mUseSystemDefault = false;

    private OnEmojiconBackspaceClickedListener mOnEmojiconBackspaceClickedListener;
    private View[] mEmojiTabs;
    private PagerAdapter mEmojisAdapter;
    private EmojiconRecentsManager mRecentsManager;

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
     *
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.emojicons, container, false);

        /// Get reference to the view pager.
        final ViewPager emojisPager = (ViewPager) view.findViewById(R.id.emojis_pager);

        /// Previous value > emojisPager.setOnPageChangeListener(this); -> DEPRECATED
        emojisPager.addOnPageChangeListener(this);

        // we handle recents
        EmojiconRecents recents = this;
        mEmojisAdapter = new EmojisPagerAdapter(getFragmentManager(), Arrays.asList(
                EmojiconRecentsGridFragment.newInstance(mUseSystemDefault),
                EmojiconGridFragment.newInstance(People.DATA, recents, mUseSystemDefault),
                EmojiconGridFragment.newInstance(Nature.DATA, recents, mUseSystemDefault),
                EmojiconGridFragment.newInstance(Objects.DATA, recents, mUseSystemDefault),
                EmojiconGridFragment.newInstance(Places.DATA, recents, mUseSystemDefault),
                EmojiconGridFragment.newInstance(Symbols.DATA, recents, mUseSystemDefault)
        ));

        /// Set pager adapter...
        emojisPager.setAdapter(mEmojisAdapter);

        SlidingTabLayout tabs = (SlidingTabLayout)view.findViewById(R.id.emojis_tab_view);
        tabs.setCustomTabView(R.layout.emojicons_tab, R.id.tab_text_content);
        tabs.setUseIconsInTabs(true);
        tabs.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
            @Override
            public int getIndicatorColor(int position) {
                return getResources().getColor(R.color.tabsScrollColor);
            }
        });
        tabs.setViewPager(emojisPager);

        /// Set backspace functionality for the emoji's...
        view.findViewById(R.id.emojis_backspace).setOnTouchListener(new RepeatListener(1000, 50, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnEmojiconBackspaceClickedListener != null) {
                    mOnEmojiconBackspaceClickedListener.onEmojiconBackspaceClicked(v);
                }
            }
        }));

        // get last selected page
        mRecentsManager = EmojiconRecentsManager.getInstance(view.getContext());
        int page = mRecentsManager.getRecentPage();

        // last page was recents, check if there are recents to use, if none was found, go to page 1
        if (page == 0 && mRecentsManager.size() == 0) {
            page = 1;
        }

        if (page == 0) {
            onPageSelected(page);
        } else {
            emojisPager.setCurrentItem(page, false);
        }

        return view;
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
        final ViewPager emojisPager = (ViewPager) getView().findViewById(R.id.emojis_pager);
        EmojiconRecentsGridFragment fragment = (EmojiconRecentsGridFragment) mEmojisAdapter.instantiateItem(emojisPager, 0);
        fragment.addRecentEmoji(context, emojicon);
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
    public void onPageSelected(int i) {
        if (mEmojiTabLastSelectedIndex == i) {
            return;
        }
        switch (i) {
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
                /*if (mEmojiTabLastSelectedIndex >= 0 && mEmojiTabLastSelectedIndex < mEmojiTabs.length) {
                    mEmojiTabs[mEmojiTabLastSelectedIndex].setSelected(false);
                }
                mEmojiTabs[i].setSelected(true);*/
                mEmojiTabLastSelectedIndex = i;
                mRecentsManager.setRecentPage(i);
                break;
        }
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
