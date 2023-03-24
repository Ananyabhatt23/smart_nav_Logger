package com.accord.smart_nav_logger.data

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.serialport.SerialPort
import android.util.Log
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

open class SharedHamsaMessageManager constructor(
    context: Context,
    externalScope: CoroutineScope,
    prefs: SharedPreferences) {

    lateinit var  externalScope: CoroutineScope
    lateinit var  fd: InputStream
    val  buffer = ByteArray(4096)

    @ExperimentalCoroutinesApi
    @SuppressLint("MissingPermission")
    private val _HamsaUpdtaes = callbackFlow {


        val device = File(config.COMMPORT_MXC2)
        val baurate = config.COMMPORT_MXC2_Baudrate.toInt()
        val serialPort   = SerialPort(device, baurate)
        Log.d("Comport_read",""+serialPort.inputStream.read())
        fd=serialPort.inputStream

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
           /* } catch (e: Exception) {
                // Handle any exceptions that occur
                close(e)
            } finally {
                // Cleanup: close the input and output streams
                inputStream.close()
                outputStream.close()
            }
            // Cleanup: close the flow
            awaitClose()
        }.flowOn(Dispatchers.IO)
*/

                  /*  val length = fd.read(buffer)

                    val data = buffer.copyOf(length)

                    trySend(data)*/




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
    fun hamsaFlow(): Flow<ByteArray> {
        return _HamsaUpdtaes
    }


}