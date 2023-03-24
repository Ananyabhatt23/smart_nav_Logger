package com.accord.smart_nav_logger.util

import android.Manifest
import android.app.Activity
import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import com.accord.smart_nav_logger.R
import java.lang.System.exit

object LibUtils {

    /**
     * Shows the dialog to explain why location permissions are needed
     *
     * NOTE - this dialog can't be managed under the old dialog framework as the method
     * ActivityCompat.shouldShowRequestPermissionRationale() always returns false.
     */
    fun showLocationPermissionDialog(activity: Activity) {
        val builder = AlertDialog.Builder(activity)
            .setTitle(R.string.title_location_permission)
            .setMessage(R.string.text_location_permission)
            .setCancelable(false)
            .setPositiveButton(
                R.string.ok
            ) { dialog: DialogInterface?, which: Int ->
                // Request permissions from the user
                ActivityCompat.requestPermissions(
                    activity,
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ),
                    PermissionUtil.LOCATION_PERMISSION_REQUEST
                )
            }
            .setNegativeButton(
                R.string.exit
            ) { dialog: DialogInterface?, which: Int ->
                // Exit app
                activity.finish()
            }
        builder.create().show()
    }

}