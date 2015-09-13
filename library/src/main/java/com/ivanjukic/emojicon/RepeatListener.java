package com.ivanjukic.emojicon;

import android.os.Handler;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.View;

/**
 * A class, that can be used as a TouchListener on any view (e.g. a Button).
 * It cyclically runs a clickListener, emulating keyboard-like behaviour. First
 * click is fired immediately, next before initialInterval, and subsequent before
 * normalInterval.
 * <p/>
 * <p>Interval is scheduled before the onClick completes, so it has to run fast.
 * If it runs slow, it does not generate skipped onClicks.
 */
public class RepeatListener implements View.OnTouchListener {

    protected Handler handler = new Handler();

    protected View downView;
    protected int initialInterval;
    protected int normalInterval;
    protected View.OnClickListener clickListener = null;
    protected View.OnTouchListener touchListener = null;
    protected MotionEvent mLastMotionEvent;

    protected Runnable handlerRunnable = new Runnable() {
        @Override
        public void run() {
            if (downView == null) {
                return;
            }
            handler.removeCallbacksAndMessages(downView);
            handler.postAtTime(this, downView, SystemClock.uptimeMillis() + normalInterval);
            if (null != clickListener) {
                clickListener.onClick(downView);
            } else if (null != touchListener) {
                touchListener.onTouch(downView, mLastMotionEvent);
            }
        }
    };


    /**
     * @param initialInterval The interval before first click event
     * @param normalInterval  The interval before second and subsequent click
     *                        events
     * @param clickListener   The OnClickListener, that will be called
     *                        periodically
     */
    public RepeatListener(int initialInterval, int normalInterval, View.OnClickListener clickListener) {
        if (clickListener == null)
            throw new IllegalArgumentException("null runnable");
        if (initialInterval < 0 || normalInterval < 0)
            throw new IllegalArgumentException("negative interval");

        this.initialInterval = initialInterval;
        this.normalInterval = normalInterval;
        this.clickListener = clickListener;
    }


    public RepeatListener(int initialInterval, int normalInterval, View.OnTouchListener touchListener) {
        if (touchListener == null) {
            throw new IllegalArgumentException("null runnable");
        }
        if (initialInterval < 0 || normalInterval < 0) {
            throw new IllegalArgumentException("negative interval");
        }

        this.initialInterval = initialInterval;
        this.normalInterval = normalInterval;
        this.touchListener = touchListener;
    }


    public boolean onTouch(View view, MotionEvent motionEvent) {
        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downView = view;
                handler.removeCallbacks(handlerRunnable);
                handler.postAtTime(handlerRunnable, downView, SystemClock.uptimeMillis() + initialInterval);
                if (null != clickListener) {
                    clickListener.onClick(view);
                } else if (null != touchListener) {
                    mLastMotionEvent = motionEvent;
                    touchListener.onTouch(view, motionEvent);
                }
                return true;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_OUTSIDE:
                if (null != touchListener) {
                    touchListener.onTouch(view, motionEvent);
                }
                handler.removeCallbacksAndMessages(downView);
                downView = null;
                mLastMotionEvent = null;
                return true;
        }
        return false;
    }
}