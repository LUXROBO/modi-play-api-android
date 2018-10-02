/*
 * Developement Part, Luxrobo INC., SEOUL, KOREA
 * Copyright(c) 2018 by Luxrobo Inc.
 *
 * All rights reserved. No part of this work may be reproduced, stored in a
 * retrieval system, or transmitted by any means without prior written
 * Permission of Luxrobo Inc.
 */

package com.luxrobo.modiplay.api.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.luxrobo.modiplay.api.core.ModiConstants;


public class ModiPreference {

    private SharedPreferences preferences;
    private Context context;

    private ModiPreference() {
    }

    private static class Singleton {
        private static final ModiPreference instance = new ModiPreference();
    }

    public static ModiPreference getInstance() {
        return Singleton.instance;
    }

    private void setPreferences(Context context) {
        if (this.preferences == null) {
            this.preferences = context.getSharedPreferences(ModiConstants.PREF_CONFIG_MODI, Context.MODE_PRIVATE);
        }
    }

    public void init(Context context) {
        this.context = context;
        setPreferences(context);
    }

    public void setData(String key, String data) {
        if (this.preferences != null) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(key, data);
            editor.commit();
        }
    }

    public void setData(String key, boolean data) {
        if (this.preferences != null) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean(key, data);
            editor.commit();
        }
    }

    public String getString(String key) {
        String str = null;

        if (this.preferences == null) {
            setPreferences(this.context);
        }

        if (this.preferences != null) {
            str = preferences.getString(key, null);
        }

        return str;
    }

    public boolean getBoolean(String key) {
        boolean data = false;

        if (this.preferences == null) {
            setPreferences(this.context);
        }

        if (this.preferences != null) {
            data = preferences.getBoolean(key, false);
        }

        return data;
    }

    public void setDeviceAddress(String deviceAddress) {
        if (this.preferences != null) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(ModiConstants.KEY_DEVICE_ADDR, deviceAddress);
            editor.commit();
        }
    }

    public String getDeviceAddress() {
        String deviceAddress = null;

        if (this.preferences == null) {
            setPreferences(this.context);
        }

        if (this.preferences != null) {
            deviceAddress = preferences.getString(ModiConstants.KEY_DEVICE_ADDR, null);
        }

        return deviceAddress;
    }
}
