package com.accord.smart_nav_logger.commport;

import android.os.HandlerThread;
import android.serialport.SerialPort;


import com.accord.smart_nav_logger.data.SharedHamsaMessageManager;
import com.accord.smart_nav_logger.data.SharedNmeaMessageManager;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;


public class SerialPortManager {

    private static final String TAG = "SerialPortManager";


    private static String COMMPORT_MXC0 = "/dev/ttymxc0";
    private static String COMMPORT_MXC2 = "/dev/ttymxc2";
    private static String COMMPORT_MXC0_Baudrate = "38400";
    private static String COMMPORT_MXC2_Baudrate = "115200";

    private SerialPort mSerialPortL1;
    private SerialPort mSerialPortL5;



    private SharedNmeaMessageManager mReadThreadL1;
    private OutputStream mOutputStreamL1;
    private HandlerThread mWriteThreadL1;


    private SharedHamsaMessageManager mReadThreadL5;
    private OutputStream mOutputStreamL5;
    private HandlerThread mWriteThreadL5;


    private static class InstanceHolder {
        public static SerialPortManager sManager = new SerialPortManager();
    }

    public static SerialPortManager instance() {
        return InstanceHolder.sManager;
    }


    private SerialPortManager() {
    }


    public SerialPort openL1() {
        return openL1(COMMPORT_MXC0, COMMPORT_MXC0_Baudrate);
    }

    public SerialPort openL5() {
        return openL5(COMMPORT_MXC2, COMMPORT_MXC2_Baudrate);
    }


    public SerialPort openL1(String devicePath, String baudrateString) {
        if (mSerialPortL1 != null) {
            closeL1();
        }
        try {
            File device = new File(devicePath);
            int baurate = Integer.parseInt(baudrateString);
            mSerialPortL1 = new SerialPort(device, baurate);
           /* mReadThreadL1 = new SharedNmeaMessageManager(mSerialPortL1.getInputStream());
            mReadThreadL1.t1.start();
*/
            return mSerialPortL1;
        } catch (Throwable tr) {
            closeL1();
            return null;
        }
    }


    public SerialPort openL5(String devicePath, String baudrateString) {
        if (mSerialPortL5 != null) {
            closeL5();
        }
        try {
            File device = new File(devicePath);
            int baurate = Integer.parseInt(baudrateString);
            mSerialPortL5 = new SerialPort(device, baurate);
          /*  mReadThreadL5 = new SharedHamsaMessageManager(mSerialPortL5.getInputStream());
            mReadThreadL5.t1.start();
           */ return mSerialPortL5;
        } catch (Throwable tr) {
            closeL5();
            return null;
        }
    }






    public void closeL1() {
        if (mReadThreadL1 != null) {
           // mReadThreadL1.t1.stop();
        }
        if (mOutputStreamL1 != null) {
            try {
                mOutputStreamL1.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (mWriteThreadL1 != null) {
            mWriteThreadL1.quit();
        }

        if (mSerialPortL1 != null) {
            mSerialPortL1.close();
            mSerialPortL1 = null;
        }
    }
    private void sendDataL1(byte[] datas) throws Exception {
        mOutputStreamL1.write(datas);
    }



    public void closeL5() {
        if (mReadThreadL5 != null) {
            //mReadThreadL5.t1.stop();
        }
        if (mOutputStreamL5 != null) {
            try {
                mOutputStreamL5.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (mWriteThreadL5!= null) {
            mWriteThreadL5.quit();
        }

        if (mSerialPortL5 != null) {
            mSerialPortL5.close();
            mSerialPortL5 = null;
        }
    }



}
