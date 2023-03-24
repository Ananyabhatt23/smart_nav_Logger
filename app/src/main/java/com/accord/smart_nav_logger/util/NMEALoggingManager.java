package com.accord.smart_nav_logger.util;

import android.util.Log;


import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class NMEALoggingManager {


    private static final String _ROOT_FOLDER_NAME_ = "smartNavlogger20";
    private static final String _ROOT_SYS = "/sdcard";
    public static File _SMART_NAV_ROOT = new File(_ROOT_SYS + File.separator + _ROOT_FOLDER_NAME_);
    //to log NMEA data
    public static File _SMART_NAV_NMEA_LOG = new File(_SMART_NAV_ROOT, File.separator + "nmea" + File.separator);


    private static final String EXTENSION = ".anf";
    public static final String current = "current_log" + EXTENSION;
    private static final File currentFile = new File(_SMART_NAV_NMEA_LOG, current);
    private static final int MAX_CACHE_SECONDS = 1024;
    private static final int CACHE_LINES_PER_SEC = 4;
    private static final double MAX_FILE_SIZE_MB = 250 * 1024 * 1024; // = 500 Mega bytes to store 100 hours of data

    private static final String TAG = NMEALoggingManager.class.getSimpleName();
    private static final boolean ZIP_AND_STORE = true;
    private final SimpleDateFormat dateTimeSimpleDateFormat = new SimpleDateFormat("dd_MM_yyyy'T'HH_mm_ss", Locale.getDefault());

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final StringBuilder nmeaStringBuilder = new StringBuilder();
    private FileWriter mNmeaFileWriterSplit;
    private BufferedWriter mNmeaWriteLogSplit;


    private boolean createTempNmeaFile() {

        try {
            boolean created = false;
            if (!currentFile.exists()) {
                created = currentFile.createNewFile();
            }

            try {
                mNmeaFileWriterSplit = new FileWriter(currentFile, true);
                mNmeaWriteLogSplit = new BufferedWriter(mNmeaFileWriterSplit);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return created;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    public  void logNmea(String nmea) {
/*

        if (nmeaStringBuilder.length() < MAX_CACHE_SECONDS * CACHE_LINES_PER_SEC) {
            nmeaStringBuilder.append(nmea);
            return;
        }
*/

        Log.d("NmeaLogging", "" + nmeaStringBuilder.toString());

        nmeaStringBuilder.append(nmea);
        log(nmeaStringBuilder.toString());
        nmeaStringBuilder.setLength(0);
    }


    int valuate = 1;
    boolean firstTimeLog = false;

    public void logNmeaOnDuration(String nmea, long duration) {


        //  Log.i("logNmeaOnDuration", "logNMEA: " + nmea);
        if (valuate < (duration / 1000) && !firstTimeLog) {
            //Log.i("logNmeaOnDuration", "duration: " + duration + "   curr: " + valuate);
            valuate++;
        } else {
            nmeaStringBuilder.append(nmea);
            log(nmeaStringBuilder.toString());
            nmeaStringBuilder.setLength(0);
            //String systemtime = dateTimeSimpleDateFormat.format(new Date(System.currentTimeMillis()));
           // Log.i("logNmeaOnDuration", "Write : " + "   valuate: " + valuate +" duration: "+duration+" systetime:"+systemtime);
            valuate = 1;
            firstTimeLog = false;
        }
    }

/*
    public void logNmeaOnDuration(String nmea, long duration) {
        if (!loggingEnabled) {
            return;
        }
        Log.d("LOGGING_TIME_Trace", "if : " + mLastClickTime / 1000 +
                " duration : " + (SystemClock.elapsedRealtime() - mLastClickTime) / 1000);
        if ((SystemClock.elapsedRealtime() - mLastClickTime) < duration) {
            return;
        }
        Log.d("LOGGING_TIME_Trace", "else : " + mLastClickTime / 1000 +
                " duration : " + (SystemClock.elapsedRealtime() - mLastClickTime) / 1000);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                handler.removeCallbacks(this);
                mLastClickTime = SystemClock.elapsedRealtime();
            }
        }, 2000);

        nmeaStringBuilder.append(nmea);
        log(nmeaStringBuilder.toString());
        */
/*if(isLatLongAvailale(nmeaStringBuilder.toString()))
        {
            log(nmeaStringBuilder.toString());

        }*//*

        nmeaStringBuilder.setLength(0);
    }
*/

    private Boolean isLatLongAvailale(String msg) {

        if (msg != null && msg.startsWith("$GNGGA")) {
            String[] nmea = msg.split(",");

            if (nmea[2] != null && !nmea[2].isEmpty() && nmea[4] != null && !nmea[4].isEmpty()) {

                if (Double.parseDouble(nmea[2]) == 0 && Double.parseDouble(nmea[4]) == 0) {
                    return false;

                }
            }

        } else if (msg != null && msg.startsWith("$GNGLL")) {

            String[] nmea = msg.split(",");

            if (nmea[1] != null && !nmea[1].isEmpty() && nmea[3] != null && !nmea[3].isEmpty()) {


                if (Double.parseDouble(nmea[1]) == 0 && Double.parseDouble(nmea[3]) == 0) {
                    return false;

                }
            }

        } else if (msg != null && msg.startsWith("$GNRMC")) {
            String[] nmea = msg.split(",");

            if (nmea[3] != null && !nmea[3].isEmpty() && nmea[5] != null && !nmea[5].isEmpty()) {


                if (Double.parseDouble(nmea[3]) == 0 && Double.parseDouble(nmea[5]) == 0) {
                    return false;

                }
            }
        }
        return true;
    }

    public synchronized void  log(String nmeaBulk) {
        if (nmeaBulk != null) {
                try {

                    if (mNmeaWriteLogSplit == null) {
                        createTempNmeaFile();
                    }else {
                        mNmeaWriteLogSplit.append(nmeaBulk);
                        mNmeaWriteLogSplit.flush();
                    }


                } catch (IOException e) {
                    e.printStackTrace();
                }
                Log.i("Nmea file size", String.valueOf(currentFile.length()));
                if (!currentFile.exists()) {
                    createTempNmeaFile();
                }
                if (currentFile.length() > MAX_FILE_SIZE_MB) {
                    //       Log.i("Nmea file", "separated");
                    stopLogging();
                    createTempNmeaFile();
                    startLogging();
                }

        } else {
            // Log.d("NmeaNotLogging:-",""+(nmeaBulk!=null)+" "+(mNmeaWriteLogSplit!=null) );
        }
    }


    /*
     *
     * Zips a file at a location and places the resulting zip file at the toLocation
     * Example: zipFileAtPath("downloads/myfolder", "downloads/myFolder.zip");
     */
    private boolean zipFileAtPath(File sourcePath, File toLocation) {
        final int BUFFER = 2048;

        File sourceFile = new File(sourcePath.getAbsolutePath());
        try {
            BufferedInputStream origin;
            FileOutputStream dest = new FileOutputStream(toLocation);
            ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));

            {
                byte data[] = new byte[BUFFER];
                FileInputStream fi = new FileInputStream(sourcePath);
                origin = new BufferedInputStream(fi, BUFFER);
                ZipEntry entry = new ZipEntry(getLastPathComponent(sourcePath.getAbsolutePath()));
                entry.setTime(sourceFile.lastModified()); // to keep modification time after unzipping
                out.putNextEntry(entry);
                int count;
                while ((count = origin.read(data, 0, BUFFER)) != -1) {
                    out.write(data, 0, count);
                }
            }
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private String getLastPathComponent(String filePath) {
        String[] segments = filePath.split("/");
        if (segments.length == 0)
            return "";
        return segments[segments.length - 1];
    }

    private void closeStreams() {
        try {
            mNmeaFileWriterSplit.close();
            mNmeaWriteLogSplit.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void startLogging() {

        //stopLogging();
       // saveTempFileAsNmea();
        createTempNmeaFile();
        firstTimeLog = true;
        valuate = 0;
    }

    public boolean stopLogging() {
        nmeaStringBuilder.setLength(0);
        try {
            if (mNmeaFileWriterSplit != null &&
                    mNmeaWriteLogSplit != null) {
                mNmeaFileWriterSplit.close();
                mNmeaWriteLogSplit.close();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return true;
    }



}
