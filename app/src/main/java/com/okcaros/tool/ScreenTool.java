package com.okcaros.tool;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;
import android.view.WindowMetrics;

public class ScreenTool {
    public static class ScreenInfo {
        // 屏幕全部区域
        public int realWidth;
        public int realHeight;
        public float realDensity;

        // 除去状态栏、导航栏区域
        public int width;
        public int height;
        public float density;

        public int statusBarSize = 0;
        public int navBarSize = 0;
        public boolean navBarVertical = false;  // 导航栏在左边或者右边

        public boolean isWideScreen() {
            return (float) realWidth / realHeight >= 2.5f;
        }

        public boolean isVerticalScreen() {
            return (float) realWidth / realHeight <= 1f;
        }

        @Override
        public String toString() {
            return "ScreenInfo{" +
                    "realWidth=" + realWidth +
                    ", realHeight=" + realHeight +
                    ", realDensity=" + realDensity +
                    ", width=" + width +
                    ", height=" + height +
                    ", density=" + density +
                    ", statusBarSize=" + statusBarSize +
                    ", navBarSize=" + navBarSize +
                    ", navBarVertical=" + navBarVertical +
                    '}';
        }
    }

    public static ScreenInfo getScreenInfo(Context context) {
        ScreenInfo screenInfo = new ScreenInfo();
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        Display display = windowManager.getDefaultDisplay();
        display.getMetrics(displayMetrics);
        screenInfo.width = displayMetrics.widthPixels;
        screenInfo.height = displayMetrics.heightPixels;
        screenInfo.density = displayMetrics.density;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowMetrics windowMetrics = windowManager.getCurrentWindowMetrics();
            Rect bounds = windowMetrics.getBounds();
            screenInfo.realWidth = bounds.width();
            screenInfo.realHeight = bounds.height();

            DisplayMetrics realMetrics = new DisplayMetrics();
            display.getRealMetrics(realMetrics);
            screenInfo.realDensity = realMetrics.density;
        } else {
            DisplayMetrics outMetrics = new DisplayMetrics();
            windowManager.getDefaultDisplay().getRealMetrics(outMetrics);
            screenInfo.realWidth = outMetrics.widthPixels;
            screenInfo.realHeight = outMetrics.heightPixels;
            screenInfo.realDensity = outMetrics.density;
        }

        Resources resources = context.getResources();
        final int statusBarHeightId = resources.getIdentifier("status_bar_height", "dimen", "android");
        if (statusBarHeightId > 0) {
            int sHeight = resources.getDimensionPixelSize(statusBarHeightId);
            screenInfo.statusBarSize = sHeight;
        }

        boolean hasNavBar = false;
        if (
                (screenInfo.height + screenInfo.statusBarSize) != screenInfo.realHeight
                        ||
                        screenInfo.width != screenInfo.realWidth
        ) {
            hasNavBar = true;
        }
        if (hasNavBar) {
            int navBarSizeId;
            if ((screenInfo.height + screenInfo.statusBarSize) != screenInfo.realHeight) {
                // 导航栏在左边或者右边
                screenInfo.navBarVertical = true;
                navBarSizeId = resources.getIdentifier("navigation_bar_width", "dimen", "android");
            } else {
                // 导航栏在下边
                screenInfo.navBarVertical = false;
                navBarSizeId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
            }
            if (navBarSizeId != 0) {
                screenInfo.navBarSize = resources.getDimensionPixelSize(navBarSizeId);
            }
        }

        return screenInfo;
    }
}