package com.accord.smart_nav_logger

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.accord.smart_nav_logger.App.Companion.prefs
import com.accord.smart_nav_logger.data.LoggingRepository
import com.accord.smart_nav_logger.util.FIleLogger
import com.accord.smart_nav_logger.util.NMEALoggingManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.nio.charset.StandardCharsets
import javax.inject.Inject

@AndroidEntryPoint
class MainService:LifecycleService() {

    private  val TAG = "MainService"

    private lateinit var notificationManager: NotificationManager

    // Repository of location data that the service will observe, injected via Hilt
    @Inject
    lateinit var repository: LoggingRepository


    private var nmeaFlow: Job? = null
    private var nmeanewFlow: Job? = null
    private var hamsaFlow: Job? = null
    private var lcationFlow: Job? = null

    private val localBinder = LocalBinder()

    lateinit var nmeaLoggingManager: NMEALoggingManager

    override fun onCreate() {
        super.onCreate()

        Log.d(TAG, "onCreate()")
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nmeaLoggingManager= NMEALoggingManager()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        Log.d(TAG, "onStartCommand()")

      /*  val cancelLocationTrackingFromNotification =
            intent?.getBooleanExtra(EXTRA_CANCEL_LOCATION_TRACKING_FROM_NOTIFICATION, false)

        if (cancelLocationTrackingFromNotification == true) {
            unsubscribeToLocationUpdates()
        } else {
            if (!isStarted) {
                isStarted = true
                GlobalScope.launch(Dispatchers.IO) {
                    initLogging()
                }
                try {
                    observeFlows()
                } catch (unlikely: Exception) {
                    PreferenceUtils.saveTrackingStarted(false, prefs)
                    Log.e(TAG, "Exception registering for updates: $unlikely")
                }
*/

        try {
            observeFlows()
        } catch (unlikely: Exception) {
            Log.e(TAG, "Exception registering for updates: $unlikely")
        }
                // We may have been restarted by the system. Manage our lifetime accordingly.
                createNotificationChannel()
                startForeground(NOTIFICATION_ID, buildNotification(""))

        // Tells the system to recreate the service after it's been killed.
        return super.onStartCommand(intent, flags, START_NOT_STICKY)
    }

    @SuppressLint("NewApi")
    @ExperimentalCoroutinesApi
    private fun observeFlows() {

        observeNmeaFlow()
        observeNmeanewFlow()
        observeHamsaeFlow()
        observeLocationFlow()

    }

    private fun observeLocationFlow() {
        if(lcationFlow?.isActive==true)
        {
            return
        }
        lcationFlow=repository.getLocation().flowWithLifecycle(lifecycle,Lifecycle.State.STARTED).onEach {



            GlobalScope.launch(Dispatchers.IO) {
                Log.d(TAG, "Service NMEA: $it.")

            }
        }.launchIn(lifecycleScope)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    @OptIn(DelicateCoroutinesApi::class)
    private fun observeNmeanewFlow() {

        if(nmeanewFlow?.isActive==true)
        {
            return
        }
        nmeanewFlow=repository.getNmeam().flowWithLifecycle(lifecycle,Lifecycle.State.STARTED).onEach {

            GlobalScope.launch(Dispatchers.IO) {

                Log.d(TAG, "onNmea:$it")
// Show location in notification
                notificationManager.notify(
                    NOTIFICATION_ID,
                    buildNotification(it)
                )
             }
        }.launchIn(lifecycleScope)

    }

    @RequiresApi(Build.VERSION_CODES.O)
    @ExperimentalCoroutinesApi
    private fun observeHamsaeFlow() {

        if(hamsaFlow?.isActive==true)
        {
            return
        }
        hamsaFlow=repository.getHamsa().flowWithLifecycle(lifecycle,Lifecycle.State.STARTED).onEach {

            GlobalScope.launch(Dispatchers.IO) {

                val message = String(it, StandardCharsets.ISO_8859_1)
                Log.d(TAG, "onNmea:$message")

              //  FIleLogger.createRootDirectory()
// Show location in notification
                notificationManager.notify(
                    NOTIFICATION_ID,
                    buildNotification(message)
                )
            }

        }.launchIn(lifecycleScope)

    }

    @ExperimentalCoroutinesApi
    private fun observeNmeaFlow() {
        if (nmeaFlow?.isActive == true) {
            // If we're already observing updates, don't register again
            return
        }
        // Observe via Flow as they are generated by the repository
        nmeaFlow = repository.getNmea()
            .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
            .onEach {
                GlobalScope.launch(Dispatchers.IO) {
                    val message = String(it, StandardCharsets.ISO_8859_1)
                    Log.d(TAG, "onNmea:$message")

                    // Show location in notification
                    notificationManager.notify(
                        NOTIFICATION_ID,
                        buildNotification(message)
                    )

                }
            }
            .launchIn(lifecycleScope)
    }

    override fun onBind(intent: Intent): IBinder? {
        super.onBind(intent)
        return localBinder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        return super.onUnbind(intent)
    }

    override fun onRebind(intent: Intent?) {
        super.onRebind(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
    }


    fun subscribeToLocationUpdates() {
        Log.d(TAG, "subscribeToLocationUpdates()")
        PreferenceUtils.saveTrackingStarted(true, prefs)



        // Binding to this service doesn't actually trigger onStartCommand(). That is needed to
        // ensure this Service can be promoted to a foreground service, i.e., the service needs to
        // be officially started (which we do here).
        startService(Intent(applicationContext, MainService::class.java))
    }

    fun unsubscribeToLocationUpdates() {

        PreferenceUtils.saveTrackingStarted(false, prefs)

        Log.d(TAG, "unsubscribeToLocationUpdates()")

        try {
            cancelFlows()
            stopSelf()
            removeOngoingActivityNotification()
        } catch (unlikely: SecurityException) {
            Log.e(TAG, "Lost location permissions. Couldn't remove updates. $unlikely")
        }
    }


    private fun removeOngoingActivityNotification() {
     //   if (isForeground) {
            Log.d(TAG, "Removing ongoing activity notification")
     //       isForeground = false
            stopForeground(true)
      //  }
    }


    private fun cancelFlows() {
        hamsaFlow?.cancel()
        nmeaFlow?.cancel()
        nmeanewFlow?.cancel()
        lcationFlow?.cancel()

    }

    /**
     * Class used for the client Binder.  Since this service runs in the same process as its
     * clients, we don't need to deal with IPC.
     */
    inner class LocalBinder : Binder() {
        val service: MainService
            get() = this@MainService
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                NOTIFICATION_CHANNEL,
                getString(R.string.app_name),
                NotificationManager.IMPORTANCE_LOW
            )

            // Adds NotificationChannel to system. Attempting to create an
            // existing notification channel with its original values performs
            // no operation, so it's safe to perform the below sequence.
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

    /*
    * Generates a BIG_TEXT_STYLE Notification that represent latest location.
    */
    private fun buildNotification(nmea:String): Notification {
        val titleText = "Smart_nav_Logger"
        val summaryText = "$nmea"


        /*val titleText = satellites.toNotificationTitle(app)
        val summaryText = location?.toNotificationSummary(app, prefs) ?: getString(R.string.no_location_text)
*/

        // 2. Build the BIG_TEXT_STYLE.
        val bigTextStyle = NotificationCompat.BigTextStyle()
            .bigText(summaryText)
            .setBigContentTitle(titleText)

        // 3. Set up main Intent/Pending Intents for notification.
        val launchActivityIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            // NOTE: The above causes the activity/viewmodel to be recreated from scratch for Accuracy when it's already visible
            // and the notification is tapped (strangely if it's destroyed Accuracy viewmodel seems to keep it's state)
            // FLAG_ACTIVITY_REORDER_TO_FRONT seems like it should work, but if this is used then onResume() is called
            // again (and onPause() is never called). This seems to freeze up Status into a blank state because GNSS inits again.
        }
        val openActivityPendingIntent = PendingIntent.getActivity(
            applicationContext,
            System.currentTimeMillis().toInt(),
            launchActivityIntent,
            0
        )

        val cancelIntent = Intent(this, MainService::class.java).apply {
            putExtra(EXTRA_CANCEL_LOCATION_TRACKING_FROM_NOTIFICATION, true)
        }
        val stopServicePendingIntent = PendingIntent.getService(
            applicationContext,
            System.currentTimeMillis().toInt(),
            cancelIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        // 4. Build and issue the notification.
        // Notification Channel Id is ignored for Android pre O (26).
        val notificationCompatBuilder =
            NotificationCompat.Builder(applicationContext, NOTIFICATION_CHANNEL)

        return notificationCompatBuilder
            .setStyle(bigTextStyle)
            .setContentTitle(titleText)
            .setContentText(summaryText)
            .setSmallIcon(R.drawable.ic_sat_notification)
            .setColor(ContextCompat.getColor(this, R.color.purple_700))
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setOngoing(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(openActivityPendingIntent)
            .addAction(
                R.drawable.ic_baseline_launch_24, getString(R.string.open),
                openActivityPendingIntent
            )
            .addAction(
                R.drawable.ic_baseline_cancel_24,
                getString(R.string.stop),
                stopServicePendingIntent
            )
            .build()
    }

    companion object{


        private const val PACKAGE_NAME = "com.accord.smart_nav_logger"
        private const val EXTRA_CANCEL_LOCATION_TRACKING_FROM_NOTIFICATION =
            "$PACKAGE_NAME.extra.CANCEL_LOCATION_TRACKING_FROM_NOTIFICATION"
        private const val NOTIFICATION_ID = 12345678
        private const val NOTIFICATION_CHANNEL = "gsptest_channel_01"

    }

}