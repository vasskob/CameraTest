package com.example.vasskob.mycamera.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferencesManager {
    private static SharedPreferences mSharedPreferences;
    private static Context mContext = null;
    private static final String KEY_IS_FIRST_LAUNCH = "IS_FIRST_LAUNCH";

    private static SharedPreferences getPref(Context context) {
        String PREFS_NAME = "PREFS_NAME";
        if (mSharedPreferences != null) {
            return mSharedPreferences;
        }
        mContext = context;
        mSharedPreferences = mContext.getSharedPreferences(PREFS_NAME, 0);
        return mSharedPreferences;
    }

    public static void setIsFirstLaunch(Context context, boolean isFirstLaunch) {
        mSharedPreferences = getPref(context);
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putBoolean(KEY_IS_FIRST_LAUNCH, isFirstLaunch);
        // Commit the edits!
        editor.apply();
    }

    public static boolean isFirstLaunch(Context context) {
        mSharedPreferences = getPref(context);
        return mSharedPreferences.getBoolean(KEY_IS_FIRST_LAUNCH, true);
    }

    public static void setCameraPreference(Context context, String prefs) {
        mSharedPreferences = getPref(context);
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(CameraUtils.BACK_CAMERA_QUALITY, prefs);
        editor.apply();
    }

    public static String getCameraPreference(Context context) {
        mSharedPreferences = getPref(context);
        return mSharedPreferences.getString(CameraUtils.BACK_CAMERA_QUALITY, "");
    }
}
