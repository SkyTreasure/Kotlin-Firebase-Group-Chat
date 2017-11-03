package io.skytreasure.kotlingroupchat.chat.ui.widget;

import android.util.Log;

/**
 * Created by LongLT
 */
public class Logger {
    private static final boolean debug = true;
    private static final int TRACE_CALLER_COUNT = 2;

    public static void v() {
        if (debug) {
            Log.v(getClassName(), getFunctionName());
        }
    }

    public static void v(String msg) {
        if (debug) {
            Log.v(getClassName(), getFunctionName() + ", " + nonNull(msg));
        }
    }

    public static void d() {
        if (debug) {
            Log.d(getClassName(), getFunctionName());
        }
    }

    public static void enter() {
        if (debug) {
            Log.d(getClassName(), getFunctionName() + ".enter <");
        }
    }

    public static void exit() {
        if (debug) {
            Log.d(getClassName(), getFunctionName() + ".exit >");
        }
    }

    public static void d(String msg) {
        if (debug) {
            Log.d(getClassName(), getFunctionName() + ", " + nonNull(msg));
        }
    }

    public static void i() {
        if (debug) {
            Log.i(getClassName(), getFunctionName());
        }
    }

    public static void i(String msg) {
        if (debug) {
            Log.i(getClassName(), getFunctionName() + ", " + nonNull(msg));
        }
    }

    public static void w(String msg) {
        if (debug) {
            Log.w(getClassName(), getFunctionName() + ", " + nonNull(msg));
        }
    }

    public static void w(String msg, Throwable e) {
        if (debug) {
            Log.w(getClassName(), getFunctionName() + ", " + nonNull(msg), e);
        }
    }

    public static void e(String msg) {
        if (debug) {
            Log.e(getClassName(), getFunctionName() + ", " + nonNull(msg));
        }
    }

    public static void e(String msg, Throwable e) {
        if (debug) {
            Log.e(getClassName(), getFunctionName() + ", " + nonNull(msg), e);
        }
    }

    private static String nonNull(String s) {
        if (s == null) {
            return "(null)";
        }
        return s;
    }

    private static String getClassName() {
        String fn = "";
        try {
            fn = new Throwable().getStackTrace()[TRACE_CALLER_COUNT].getClassName();
        } catch (Exception e) {
        }

        return fn;
    }

    private static String getFunctionName() {
        String fn = "";
        try {
            fn = new Throwable().getStackTrace()[TRACE_CALLER_COUNT].getMethodName();
        } catch (Exception e) {
        }

        return fn;
    }
}

