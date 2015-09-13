package com.ivanjukic.emojicon.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;


public class EmojiconUtil {

    public static Point getDisplaySize(Context context) {
        /// Get display size...
        WindowManager wm = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size;
    }


    public static float dpToPx(float dp, Context context) {
        if (null != context) {
            Resources resources = context.getResources();
            DisplayMetrics metrics = resources.getDisplayMetrics();
            return dp * (metrics.densityDpi / 160.f);
        } else {
            return 0.f;
        }
    }


    public static float pxToDp(int px, Context context) {
        if (null != context) {
            Resources resources = context.getResources();
            DisplayMetrics metrics = resources.getDisplayMetrics();
            return px * 160.f / metrics.densityDpi;
        } else {
            return 0.f;
        }
    }
}
