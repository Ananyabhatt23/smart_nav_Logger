package com.accord.smart_nav_logger.data

import android.os.Build
import androidx.annotation.RequiresApi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject

class LoggingRepository @Inject constructor(
                    private val sharedNmeaMessageManager:SharedNmeaMessageManager,
                    private val sharedHamsaMessageManager: SharedHamsaMessageManager,
                    private val sharedNmeaManager: SharedNmeaManager,
                    private val locationManager: SharedLocationManager,
                    ) {



    /**
     * Observable flow for location updates
     */
    @ExperimentalCoroutinesApi
    fun getNmea() = sharedNmeaMessageManager.nmeaFlow()

    /**
     * Observable flow for GnssStatus updates
     */
    @ExperimentalCoroutinesApi
    fun getHamsa() = sharedHamsaMessageManager.hamsaFlow()

    /**
     * Observable flow for NMEA updates
     */
    @RequiresApi(Build.VERSION_CODES.N)
    @ExperimentalCoroutinesApi
    fun getNmeam() = sharedNmeaManager.nmeamFlow()

    @ExperimentalCoroutinesApi
    fun getLocation()=locationManager.locationFlow()

}