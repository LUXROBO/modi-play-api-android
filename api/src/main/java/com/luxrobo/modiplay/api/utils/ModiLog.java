/*
 * Developement Part, LUXROBO INC., SEOUL, KOREA
 * Copyright(c) 2018 by LUXROBO Inc.
 *
 * All rights reserved. No part of this work may be reproduced, stored in a
 * retrieval system, or transmitted by any means without prior written
 * Permission of LUXROBO Inc.
 */

package com.luxrobo.modiplay.api.utils;

import android.util.Log;


public class ModiLog {

    private static final String TAG = "###";
    public static final String NO_MESSAGE = "NO_MESSAGE";
    public static boolean DEBUG = true;
    public static boolean NEED_FILE_LOG = false;

    public static void showLog(boolean yes) {
        ModiLog.DEBUG = yes;
    }

    public static void needFileLog(boolean yes) {
        ModiLog.NEED_FILE_LOG = yes;
    }

    private static String getTag(String tag) {
        return (ModiStringUtil.isNullOrEmpty(tag)) ? TAG : "[" + tag + "] ";
    }

    public static void i(String msg) {
        if (DEBUG) {
            if (ModiStringUtil.isNullOrEmpty(msg)) msg = NO_MESSAGE;
            Log.i(TAG, msg);
        }
    }

    public static void i(String tag, String msg) {
        if (DEBUG) {
            if (ModiStringUtil.isNullOrEmpty(msg)) msg = NO_MESSAGE;
            Log.i(getTag(tag), msg);
        }
    }

    public static void d(String msg) {
        if (DEBUG) {
            if (ModiStringUtil.isNullOrEmpty(msg)) msg = NO_MESSAGE;
            Log.d(TAG, msg);
        }
    }

    public static void d(String tag, String msg) {
        if (DEBUG) {
            if (ModiStringUtil.isNullOrEmpty(msg)) msg = NO_MESSAGE;
            Log.d(getTag(tag), msg);
        }
    }

    public static void e(String msg) {
        if (DEBUG) {
            if (ModiStringUtil.isNullOrEmpty(msg)) msg = NO_MESSAGE;
            Log.e(TAG, msg);
        }
    }

    public static void e(String tag, String msg) {
        if (DEBUG) {
            if (ModiStringUtil.isNullOrEmpty(msg)) msg = NO_MESSAGE;
            Log.e(getTag(tag), msg);
        }
    }

    public static void w(String msg) {
        if (DEBUG) {
            if (ModiStringUtil.isNullOrEmpty(msg)) msg = NO_MESSAGE;
            Log.w(TAG, msg);
        }
    }

    public static void w(String tag, String msg) {
        if (DEBUG) {
            if (ModiStringUtil.isNullOrEmpty(msg)) msg = NO_MESSAGE;
            Log.w(getTag(tag), msg);
        }
    }

    public static void v(String msg) {
        if (DEBUG) {
            if (ModiStringUtil.isNullOrEmpty(msg)) msg = NO_MESSAGE;
            Log.v(TAG, msg);
        }
    }

    public static void v(String tag, String msg) {
        if (DEBUG) {
            if (ModiStringUtil.isNullOrEmpty(msg)) msg = NO_MESSAGE;
            Log.v(getTag(tag), msg);
        }
    }
}
