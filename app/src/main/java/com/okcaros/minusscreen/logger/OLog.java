package com.okcaros.minusscreen.logger;

import com.orhanobut.logger.Logger;

public class OLog {
    public static String formatString(String tag) {
        return "[" + tag + "] ";
    }

    public static void e(String tag, Throwable e, Object... args) {
        // 调试模式下采用转换输出
        e.printStackTrace();
        Logger.e(formatString(tag) + e.getMessage(), e, args);
    }

    public static void e(String tag, String message, Object... args) {
        Logger.e(formatString(tag) + message, args);
    }

    public static void d(String tag, String message, Object... args) {
        Logger.d(formatString(tag) + message, args);
    }

    public static void w(String tag, String message, Object... args) {
        Logger.w(formatString(tag) + message, args);
    }

    public static void i(String tag, String message, Object... args) {
        Logger.i(formatString(tag) + message, args);
    }
}
