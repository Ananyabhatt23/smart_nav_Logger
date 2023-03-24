package com.accord.smart_nav_logger

import android.app.Application
import android.content.Intent
import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.util.Log
import com.accord.smart_nav_logger.commport.SerialPortManager
import com.accord.smart_nav_logger.data.SharedNmeaMessageManager
import dagger.hilt.android.HiltAndroidApp


@HiltAndroidApp
class App : Application(){

    companion object {
        lateinit var app: Application
            private set

        lateinit var prefs: SharedPreferences
            private set
    }

    override fun onCreate() {
        super.onCreate()
        app = this
        prefs = PreferenceManager.getDefaultSharedPreferences(this)

     //   SerialPortManager.instance().openL1()
      //  SerialPortManager.instance().openL5()


        //try catch to bypass "Not allowed to start service Intent" exception when device is locked when run
      /*  try {
            startService(Intent(applicationContext, MainService::class.java))
        } catch (e: Exception) {
            e.printStackTrace()
        }*/
    }


}