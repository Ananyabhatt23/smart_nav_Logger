package com.accord.smart_nav_logger;

import android.annotation.TargetApi;
import android.content.SharedPreferences;

public class PreferenceUtils {

    public static final String KEY_SERVICE_TRACKING_ENABLED = "tracking_foreground_location";
    public static final String KEY_Logging_ENABLED = "KEY_Logging_ENABLED";
    public static final int CAPABILITY_UNKNOWN = -1;
    public static final int CAPABILITY_NOT_SUPPORTED = 0;
    public static final int CAPABILITY_SUPPORTED = 1;
    public static final int CAPABILITY_LOCATION_DISABLED = 2;

    /**
     * Returns true if service location tracking is active, and false if it is not
     * @return true if service location tracking is active, and false if it is not
     */
    public static boolean isTrackingStarted(SharedPreferences prefs) {
        return prefs.getBoolean(KEY_SERVICE_TRACKING_ENABLED, false);
    }

    /**
     * Saves the provided value as the current service location tracking state
     * @param value true if service location tracking is active, and false if it is not
     */
    public static void saveTrackingStarted(boolean value, SharedPreferences prefs) {
        saveBoolean(KEY_SERVICE_TRACKING_ENABLED, value, prefs);
    }


    /**
     * Saves the provided value as the current service location tracking state
     * @param value true if service location tracking is active, and false if it is not
     */
    public static void saveLoggingStarted(boolean value, SharedPreferences prefs) {
        saveBoolean(KEY_Logging_ENABLED, value, prefs);
    }

    /**
     * Returns true if service location tracking is active, and false if it is not
     * @return true if service location tracking is active, and false if it is not
     */
    public static boolean isLoggingStarted(SharedPreferences prefs) {
        return prefs.getBoolean(KEY_Logging_ENABLED, false);
    }


    @TargetApi(9)
    public static void saveBoolean(String key, boolean value, SharedPreferences prefs) {
        SharedPreferences.Editor edit = prefs.edit();
        edit.putBoolean(key, value);
        edit.apply();
    }

    @TargetApi(9)
    public static void saveInt(String key, int value, SharedPreferences prefs) {
        SharedPreferences.Editor edit = prefs.edit();
        edit.putInt(key, value);
        edit.apply();
    }

    public static int getInt(String key, int defaultValue, SharedPreferences prefs) {
        return prefs.getInt(key, defaultValue);
    }


}
