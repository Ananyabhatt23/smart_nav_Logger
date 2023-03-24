/*
 * Copyright 2009 Cedric Priscal
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package android.serialport;

import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public final class SerialPort {

    private static final String TAG = "SerialPort";

    public static final String DEFAULT_SU_PATH = "/system/xbin/su";

    private static String sSuPath = DEFAULT_SU_PATH;
    private File device;
    private int baudrate;
    private int dataBits;
    private int parity;
    private int stopBits;
    private int flags;

    /**
     * Set the su binary path, the default su binary path is {@link #DEFAULT_SU_PATH}
     *
     * @param suPath su binary path
     */
    public static void setSuPath(@Nullable String suPath) {
        if (suPath == null) {
            return;
        }
        sSuPath = suPath;
    }

    /**
     * Get the su binary path
     *
     * @return
     */
    @NonNull
    public static String getSuPath() {
        return sSuPath;
    }

    /*
     * Do not remove or rename the field mFd: it is used by native method close();
     */
    private FileDescriptor mFd;
    private FileInputStream mFileInputStream;
    private FileOutputStream mFileOutputStream;

    /**
     * serial port
     *
     * @param device Serial device file
     * @param baudrate baud rate
     * @param dataBits Data bits; default 8, optional value is 5~8
     * @param parity Parity; 0: no parity (NONE, default); 1: odd parity (ODD); 2: even parity (EVEN)
     * @param stopBits Stop bit; default 1; 1: 1 stop bit; 2: 2 stop bit
     * @param flags Default 0
     * @throws SecurityException
     * @throws IOException
     */
    public SerialPort(@NonNull File device, int baudrate, int dataBits, int parity, int stopBits,
        int flags) throws SecurityException, IOException {

        this.device = device;
        this.baudrate = baudrate;
        this.dataBits = dataBits;
        this.parity = parity;
        this.stopBits = stopBits;
        this.flags = flags;

        /* Check access permission */
        if (!device.canRead() || !device.canWrite()) {
            try {
                /* Missing read/write permission, trying to chmod the file */
                Process su;
                su = Runtime.getRuntime().exec(sSuPath);
                String cmd = "chmod 666 " + device.getAbsolutePath() + "\n" + "exit\n";
                su.getOutputStream().write(cmd.getBytes());
                if ((su.waitFor() != 0) || !device.canRead() || !device.canWrite()) {
                    throw new SecurityException();
                }
            } catch (Exception e) {
                e.printStackTrace();
                throw new SecurityException();
            }
        }

        mFd = open(device.getAbsolutePath(), baudrate, dataBits, parity, stopBits, flags);
        if (mFd == null) {
            Log.e(TAG, "native open returns null");
            throw new IOException();
        }
        mFileInputStream = new FileInputStream(mFd);
        mFileOutputStream = new FileOutputStream(mFd);
    }

    /**
     * Serial port, default 8n1
     *
     * @param device Serial device file
     * @param baudrate baud rate
     * @throws SecurityException
     * @throws IOException
     */
    public SerialPort(@NonNull File device, int baudrate) throws SecurityException, IOException {
        this(device, baudrate, 8, 0, 1, 0);
    }

    /**
     *
     *
     * @param device Serial device file
     * @param baudrate baud rate
     * @param dataBits Data bits; default 8, optional value is 5~8
     * @param parity Parity; 0: no parity (NONE, default); 1: odd parity (ODD); 2: even parity (EVEN)
     * @param stopBits Stop bit; default 1; 1: 1 stop bit; 2: 2 stop bit
     * @throws SecurityException
     * @throws IOException
     */
    public SerialPort(@NonNull File device, int baudrate, int dataBits, int parity, int stopBits)
        throws SecurityException, IOException {
        this(device, baudrate, dataBits, parity, stopBits, 0);
    }

    // Getters and setters
    @NonNull
    public InputStream getInputStream() {
        return mFileInputStream;
    }

    @NonNull
    public OutputStream getOutputStream() {
        return mFileOutputStream;
    }

    /** Serial device file */
    @NonNull
    public File getDevice() {
        return device;
    }

    /** baud rate */
    public int getBaudrate() {
        return baudrate;
    }

    /** Data bits; default 8, optional value is 5~8 */
    public int getDataBits() {
        return dataBits;
    }

    /** Parity; 0: no parity (NONE, default); 1: odd parity (ODD); 2: even parity (EVEN) */
    public int getParity() {
        return parity;
    }

    /** Stop bit; default 1; 1: 1 stop bit; 2: 2 stop bit */
    public int getStopBits() {
        return stopBits;
    }

    public int getFlags() {
        return flags;
    }

    // JNI
    private native FileDescriptor open(String absolutePath, int baudrate, int dataBits, int parity,
        int stopBits, int flags);

    public native void close();

    /*Close stream and serial port, already try-catch */
    public void tryClose() {
        try {
            mFileInputStream.close();
        } catch (IOException e) {
            //e.printStackTrace();
        }

        try {
            mFileOutputStream.close();
        } catch (IOException e) {
            //e.printStackTrace();
        }

        try {
            close();
        } catch (Exception e) {
            //e.printStackTrace();
        }
    }

    static {
        System.loadLibrary("serial_port");
    }

    public static Builder newBuilder(File device, int baudrate) {
        return new Builder(device, baudrate);
    }

    public static Builder newBuilder(String devicePath, int baudrate) {
        return new Builder(devicePath, baudrate);
    }

    public final static class Builder {

        private File device;
        private int baudrate;
        private int dataBits = 8;
        private int parity = 0;
        private int stopBits = 1;
        private int flags = 0;

        private Builder(File device, int baudrate) {
            this.device = device;
            this.baudrate = baudrate;
        }

        private Builder(String devicePath, int baudrate) {
            this(new File(devicePath), baudrate);
        }

        /**
         * data bit
         *
         * @param dataBits The default is 8, the optional value is 5~8
         * @return
         */
        public Builder dataBits(int dataBits) {
            this.dataBits = dataBits;
            return this;
        }

        /**
         * Check Digit
         *
         * @param parity 0:No parity (NONE, default); 1: odd parity (ODD); 2: even parity (EVEN)
         * @return
         */
        public Builder parity(int parity) {
            this.parity = parity;
            return this;
        }

        /**
         * stop bit
         *
         * @param stopBits Default 1; 1: 1 stop bit; 2: 2 stop bits
         * @return
         */
        public Builder stopBits(int stopBits) {
            this.stopBits = stopBits;
            return this;
        }

        /**
         * the sign
         *
         * @param flags Default 0
         * @return
         */
        public Builder flags(int flags) {
            this.flags = flags;
            return this;
        }

        /**
         * Open and return the serial port
         *
         * @return
         * @throws SecurityException
         * @throws IOException
         */
        public SerialPort build() throws SecurityException, IOException {
            return new SerialPort(device, baudrate, dataBits, parity, stopBits, flags);
        }
    }
}
