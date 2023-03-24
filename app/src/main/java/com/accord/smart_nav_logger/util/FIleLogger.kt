package com.accord.smart_nav_logger.util

import java.io.File

object FIleLogger {
    private const val _ROOT_FOLDER_NAME_ = "smartNav20"
    private const val _ROOT_SYS = "/sdcard"
    var _SMART_NAV_ROOT = File(_ROOT_SYS + File.separator + _ROOT_FOLDER_NAME_)
    var _SMART_NAV_ROOT_MAPS =
        File(_ROOT_SYS + File.separator + _ROOT_FOLDER_NAME_ + File.separator + "maps")
    var _SMART_NAV_ROOT_DATABASE =
        File(_ROOT_SYS + File.separator + _ROOT_FOLDER_NAME_ + File.separator + "database")

    //to log NMEA data
    var _SMART_NAV_NMEA_LOG = File(_SMART_NAV_ROOT, File.separator + "nmea" + File.separator)




}