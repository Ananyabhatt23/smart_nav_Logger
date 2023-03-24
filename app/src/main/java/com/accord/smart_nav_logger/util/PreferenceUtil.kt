package com.accord.smart_nav_logger.util

import android.content.Context
import android.content.SharedPreferences
import com.accord.smart_nav_logger.R

object PreferenceUtil {

    const val SECONDS_TO_MILLISECONDS = 1000

    val METERS = "1"
    val METERS_PER_SECOND = "1"
    val KILOMETERS_PER_HOUR = "2"


    /**
     * Returns the minTime between location updates used for the LocationListener in milliseconds
     */
    fun minTimeMillis(context: Context, prefs: SharedPreferences): Long {
        val minTimeDouble: Double =
            prefs.getString(context.getString(R.string.pref_key_gps_min_time), "1")
                ?.toDouble() ?: 1.0
        return (minTimeDouble * SECONDS_TO_MILLISECONDS).toLong()
    }

    /**
     * Returns the minDistance between location updates used for the LocationLitsener in meters
     */
    fun minDistance(context: Context, prefs: SharedPreferences): Float {
        return prefs.getString(context.getString(R.string.pref_key_gps_min_distance), "0") ?.toFloat() ?: 0.0f
    }


}