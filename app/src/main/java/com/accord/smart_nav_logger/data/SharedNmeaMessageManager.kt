package com.accord.smart_nav_logger.data

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.serialport.SerialPort
import android.util.Log
import com.accord.smart_nav_logger.data.config.Companion.COMMPORT_MXC0
import com.accord.smart_nav_logger.data.config.Companion.COMMPORT_MXC0_Baudrate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.isActive
import java.io.File
import java.io.InputStream
import java.nio.charset.StandardCharsets

class SharedNmeaMessageManager constructor(
    context: Context,
    externalScope: CoroutineScope,
    prefs: SharedPreferences
) {
    lateinit var externalScope: CoroutineScope
    lateinit var fd: InputStream
    val buffer = ByteArray(4096)


    /*  init {
          try {

          } catch (e: Exception) {
              e.printStackTrace()
          }
      }*/


    @ExperimentalCoroutinesApi
    @SuppressLint("MissingPermission")
    private val _NmeaUpdtaes = callbackFlow {
        val device = File(COMMPORT_MXC0)
        val baurate = COMMPORT_MXC0_Baudrate.toInt()
        val serialPort = SerialPort(device, baurate)
        fd = serialPort.inputStream
        Log.d("Comport_read_Nmea", "" + fd.read())
        // Continuously read data from the port
        while (isActive) {
            val numBytes = fd.read(buffer)
            if (numBytes > 0) {
                // Send the received bytes to the downstream collectors
                val data = buffer.copyOfRange(0, numBytes)
                trySend(data)
            }
            delay(10)
        }
        awaitClose()
    }.shareIn(
        externalScope,
        replay = 0,
        started = SharingStarted.WhileSubscribed()
    )


    /**
     * A flow of sensor orientations
     */
    @ExperimentalCoroutinesApi
    fun nmeaFlow(): Flow<ByteArray> {
        return _NmeaUpdtaes
    }

}