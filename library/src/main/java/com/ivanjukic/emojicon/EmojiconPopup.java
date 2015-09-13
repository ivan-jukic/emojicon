/*
 * Copyright 2014 Ivan Jukic
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

import com.ivanjukic.emojicon.EmojiconPopupGridView.OnEmojiconClickedListener;
import com.ivanjukic.emojicon.adapters.EmojiconPopupPagerAdapter;
import com.ivanjukic.emojicon.recent.EmojiconRecentGridFragment;
import com.ivanjukic.emojicon.recent.EmojiconRecentGridView;
import com.ivanjukic.emojicon.recent.EmojiconRecentManager;
import com.ivanjukic.emojicon.recent.EmojiconRecent;
import com.ivanjukic.emojicon.emoji.Emojicon;
import com.ivanjukic.emojicon.emoji.Nature;
import com.ivanjukic.emojicon.emoji.Objects;
import com.ivanjukic.emojicon.emoji.People;
import com.ivanjukic.emojicon.emoji.Places;
import com.ivanjukic.emojicon.emoji.Symbols;
import com.ivanjukic.emojicon.slidingTab.SlidingTabLayout;
import com.ivanjukic.emojicon.utils.EmojiconUtil;

import java.util.Arrays;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupWindow;


public class EmojiconPopup extends PopupWindow implements ViewPager.OnPageChangeListener, EmojiconRecent {

    protected int mEmojiTabLastSelectedIndex = -1;
    private int keyboardHeight = 0;
    private Boolean pendingOpen = false;
    private Boolean isOpened = false;

    protected View rootView;
    protected Context mContext;
    protected View mView;
    protected ViewPager mEmojisPager;
    protected EmojiconPopupPagerAdapter mEmojisAdapter;
    protected EmojiconRecentManager mRecentManager;
    protected OnEmojiconBackspaceClickedListener mOnEmojiconBackspaceClickedListener;
    protected OnSoftKeyboardOpenCloseListener mOnSoftKeyboardOpenCloseListener;

    public OnEmojiconClickedListener onEmojiconClickedListener;


    /**
     * Constructor
     * @param rootView	The top most layout in your view hierarchy. The difference of this view and
     *                  the screen height will be used to calculate the keyboard height.
     * @param mContext The context of current activity.
     */
    public EmojiconPopup(View rootView, Context mContext){
        super(mContext);
        this.mContext = mContext;
        this.rootView = rootView;
        View customView = createCustomView();
        setContentView(customView);
        setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        setSize(LayoutParams.MATCH_PARENT, (int) mContext.getResources().getDimension(R.dimen.keyboard_height));
    }


    /**
     * Set the listener for the event of keyboard opening or closing.
     */
    public void setOnSoftKeyboardOpenCloseListener(OnSoftKeyboardOpenCloseListener listener){
        this.mOnSoftKeyboardOpenCloseListener = listener;
    }


    /**
     * Set the listener for the event when any of the emojicon is clicked
     */
    public void setOnEmojiconClickedListener(OnEmojiconClickedListener listener){
        this.onEmojiconClickedListener = listener;
    }


    /**
     * Set the listener for the event when backspace on emojicon popup is clicked
     */
    public void setOnEmojiconBackspaceClickedListener(OnEmojiconBackspaceClickedListener listener){
        this.mOnEmojiconBackspaceClickedListener = listener;
    }


    /**
     * Use this function to show the emoji popup.
     * NOTE: Since, the soft keyboard sizes are variable on different android devices, the
     * library needs you to open the soft keyboard at least once before calling this function.
     * If that is not possible see showAtBottomPending() function.
     */
    public void showAtBottom(){
        showAtLocation(rootView, Gravity.BOTTOM, 0, 0);
    }


    /**
     * Use this function when the soft keyboard has not been opened yet. This
     * will show the emoji popup after the keyboard is up next time.
     * Generally, you will be calling InputMethodManager.showSoftInput function after
     * calling this function.
     */
    public void showAtBottomPending() {
        if (isKeyBoardOpen()) {
            showAtBottom();
        } else {
            pendingOpen = true;
        }
    }

    /**
     *
     * @return Returns true if the soft keyboard is open, false otherwise.
     */
    public Boolean isKeyBoardOpen(){
        return isOpened;
    }


    /**
     * Dismiss the popup...
     */
    @Override
    public void dismiss() {
        super.dismiss();
        EmojiconRecentManager.getInstance(mContext).saveRecents();
    }


    /**
     * Call this function to resize the emoji popup according to your soft keyboard size
     */
    public void setSizeForSoftKeyboard(){
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {

                /// Get display size and calculate the size of the keyboard based
                /// on the difference between the height of the screen and the
                /// height of the root view.
                WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
                Display display = wm.getDefaultDisplay();
                Point size = new Point();
                display.getSize(size);

                /// Get the rectangle size of the root view.
                Rect r = new Rect();
                rootView.getWindowVisibleDisplayFrame(r);

                /// Calculate the size difference...
                int screenSizeDifference = size.y - r.bottom;

                if (screenSizeDifference > 400) {
                    keyboardHeight = screenSizeDifference;
                    setSize(LayoutParams.MATCH_PARENT, keyboardHeight);

                    if (!isOpened) {
                        if (mOnSoftKeyboardOpenCloseListener != null) {
                            mOnSoftKeyboardOpenCloseListener.onKeyboardOpen(keyboardHeight);
                        }
                    }

                    isOpened = true;
                    if (pendingOpen) {
                        showAtBottom();
                        pendingOpen = false;
                    }
                } else {
                    isOpened = false;
                    if (mOnSoftKeyboardOpenCloseListener != null) {
                        mOnSoftKeyboardOpenCloseListener.onKeyboardClose();
                    }
                }
            }
        });
    }


    /**
     * Manually set the popup window size
     * @param width Width of the popup
     * @param height Height of the popup
     */
    public void setSize(int width, int height){
        setWidth(width);
        setHeight(height);
    }


    /**
     * ...
     * @return - inflated view...
     */
    private View createCustomView() {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        mView = inflater.inflate(R.layout.emojicons, null, false);

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
                if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE) {
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
        mEmojisAdapter = new EmojiconPopupPagerAdapter(Arrays.asList(
                new EmojiconRecentGridView(mContext, null, null, this),
                new EmojiconPopupGridView(mContext, People.DATA, recent, this),
                new EmojiconPopupGridView(mContext, Nature.DATA, recent, this),
                new EmojiconPopupGridView(mContext, Objects.DATA, recent, this),
                new EmojiconPopupGridView(mContext, Places.DATA, recent, this),
                new EmojiconPopupGridView(mContext, Symbols.DATA, recent, this)
        ));

        /// Set pager adapter.
        mEmojisPager.setAdapter(mEmojisAdapter);

        /// Initialize tab layout...
        final SlidingTabLayout tabs = (SlidingTabLayout)mView.findViewById(R.id.emojis_tab_view);
        tabs.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
            @Override
            public int getIndicatorColor(int position) {
                return mContext.getResources().getColor(R.color.tabsScrollColor);
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
                Point display = EmojiconUtil.getDisplaySize(mContext);

                /// We need display metrics to calculate the available width for our tab elements...
                /// To successfully calculate this, we also need the width of the backspace button.
                ImageButton backspace = (ImageButton) mView.findViewById(R.id.emojis_backspace);
                int tabWidth = (int) Math.floor(
                        (display.x - backspace.getMeasuredWidth()) / mEmojisAdapter.getCount()
                );
                tabs.setIsPopup(true);
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


    @Override
    public void addRecentEmoji(Context context, Emojicon emojicon) {
        if (null != mEmojisPager) {
            EmojiconRecentGridView fragment = ((EmojiconPopupPagerAdapter)mEmojisPager.getAdapter()).getRecentFragment();
            fragment.addRecentEmoji(context, emojicon);
        }
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
     * @param editText
     */
    public static void backspace(EditText editText) {
        KeyEvent event = new KeyEvent(0, 0, 0, KeyEvent.KEYCODE_DEL, 0, 0, 0, 0, KeyEvent.KEYCODE_ENDCALL);
        editText.dispatchKeyEvent(event);
    }


    @Override
    public void onPageSelected(int i) {
        if (mEmojiTabLastSelectedIndex == i) {
            return;
        }
        if (0 <= i && 5 >= i) {
            mEmojiTabLastSelectedIndex = i;
            mRecentManager.setRecentPage(i);
        }
    }


    @Override
    public void onPageScrollStateChanged(int i) {
    }


    @Override
    public void onPageScrolled(int i, float v, int i2) {
    }


    public interface OnEmojiconBackspaceClickedListener {
        void onEmojiconBackspaceClicked(View v);
    }


    public interface OnSoftKeyboardOpenCloseListener{
        void onKeyboardOpen(int keyboardHeight);
        void onKeyboardClose();
    }
}