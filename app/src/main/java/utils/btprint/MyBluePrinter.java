package utils.btprint;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

/**
 Created by 张建宇 on 2017/4/18. */

public class MyBluePrinter extends MyPrinterParent {
    private BluetoothAdapter adapter;
    private Context context;
    private int mState = 5;
    private boolean debug = true;
    private static String TAG = "MyBluePrinter";
    private ServerThread mServerThread;
    private int imageWidth = 40;
    private float defBelowSize = 9;

    private StateCheckThread stateCheckThread;
    private ConnectToDeviceThread connectToDeviceThread;
    private boolean bufferFull = false;
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final String NAME = "BTPrinter";
    private Handler mHandler;
    public static MyBluePrinter mPrinter;
    public static final int STATE_CONNECTED = MyPrinterParent.STATE_CONNECTED;
    public static final int STATE_DISCONNECTED = MyPrinterParent.STATE_DISCONNECTED;
    public static final int STATE_SCAN_FINISHED = MyPrinterParent.STATE_SCAN_FINISHED;
    private static String address = "";
    private boolean showDialog = false;
    private boolean isUnregist = true;
    public OnReceiveDataHandleEvent discoverListner;
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.e("zjy", "MyBluePrinter->onReceive(): receive==" + action);
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Log.e("zjy", "MyBluePrinter->onReceive(): deviceName==" + device.getName() + "=" + device.getAddress());
                discoverListner.OnReceive(device);
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                sendMsg(STATE_SCAN_FINISHED);
            }
        }
    };

    public MyBluePrinter(Context context, Handler mHandler, OnReceiveDataHandleEvent discoverListner) {
        this.context = context;
        this.mHandler = mHandler;
        this.discoverListner = discoverListner;
        //        BluetoothManager manager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        //        adapter = manager.getAdapter();
        this.adapter = BluetoothAdapter.getDefaultAdapter();
    }

    public boolean isOpen() {
        return adapter.isEnabled();
    }

    private void sendMsg(int what) {
        mHandler.sendEmptyMessage(what);
    }

    private void open(boolean withAlert) {
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        context.registerReceiver(mReceiver, filter);
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        context.registerReceiver(mReceiver, filter);
        isUnregist = false;
        if (!isOpen()) {
            if (withAlert) {
                Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                context.startActivity(intent);
            } else {
                adapter.enable();
            }
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        mServerThread = new ServerThread();
        mServerThread.start();
        Log.e("zjy", "MyBluePrinter->open(): Open==" + adapter.isEnabled());
    }

    public void initServe() throws IOException {
        BluetoothServerSocket mmServerSocket = adapter.listenUsingRfcommWithServiceRecord("BTPrinter", MY_UUID);
        mmServerSocket.accept();
    }

    public void close() {
        if (connectToDeviceThread != null) {
            BluetoothSocket bluesocket = connectToDeviceThread.getmSocket();
            if (bluesocket != null) {
                try {
                    bluesocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        unregistReceiver();
        adapter.disable();
        address = "";
    }

    public synchronized boolean write(byte[] toWrite) {
        if (mState == STATE_CONNECTED) {
            OutputStream outputStream = null;
            try {
                outputStream = connectToDeviceThread.getmSocket().getOutputStream();
                outputStream.write(toWrite);
                return true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public boolean newLine() {
        return write(CMD_NEWLINE);
    }

    public String getAddress() {
        return address;
    }

    @Override
    public void open() {
        open(showDialog);
    }

    public synchronized void printBarCode(Context mContext, String msg, int height) {
        printBarCode(mContext, msg, height, false);
    }

    public synchronized void printBarCode(Context mContext, String msg, int height, boolean showCodeBelow) {
        printBarCode(mContext, msg, height, showCodeBelow, defBelowSize);
    }

    public synchronized void printBarCodeWithDifferentBelow(Context mContext, String msg, int height, String belowStr) {
        Bitmap btMap = BarcodeCreater.creatBarcode(mContext, msg,
                imageWidth * 8, height, belowStr, defBelowSize);
        printBitmap(btMap);
    }

    public synchronized void printBarCode(Context mContext, String msg, int height, boolean showCodeBelow, float belowSize) {
        Bitmap btMap = null;
        if (showCodeBelow) {
            btMap = BarcodeCreater.creatBarcode(mContext, msg,
                    imageWidth * 8, height, msg, belowSize);
        } else {
            btMap = BarcodeCreater.creatBarcode(mContext, msg,
                    imageWidth * 8, height, null, belowSize);

        }
        printBitmap(btMap);
    }

    /**
     @param flag 0左对齐，1,居中对齐，2，右对齐
     @return
     */

    public synchronized boolean cmd_AlignFlag(int flag) {
        return write(new byte[]{0x1B, 0x61, (byte) flag});
    }

    public void unregistReceiver() {
        if (!isUnregist) {
            context.unregisterReceiver(mReceiver);
            isUnregist = true;
        }

    }

    @Override
    public void scan() {
        if (adapter.isDiscovering()) {
            adapter.cancelDiscovery();
        }
        adapter.startDiscovery();
    }

    public void connect(String device) {
        if (!address.equals(device)) {
            connectToDeviceThread = new ConnectToDeviceThread(device);
            connectToDeviceThread.start();
        } else {
            Message msg = mHandler.obtainMessage(STATE_CONNECTED, "1");
            mHandler.sendMessage(msg);
        }
    }

    public boolean isShowDialog() {
        return showDialog;
    }

    public void setShowDialog(boolean showDialog) {
        this.showDialog = showDialog;
    }


    private class ServerThread extends Thread {
        private BluetoothServerSocket mmServerSocket;

        public ServerThread() {
            try {
                mmServerSocket = adapter.listenUsingRfcommWithServiceRecord("BTPrinter", MY_UUID);
                Log.e("zjy", "MyBluePrinter->ServerThread():server socket==");
            } catch (IOException var4) {
                Log.e(TAG, "listen() failed", var4);
            }
        }

        public void run() {
            if (debug) {
                Log.e(TAG, "BEGIN mServerThread" + this);
            }
            setName("ServerThread");
            try {
                if (mmServerSocket != null) {
                    BluetoothSocket btClient = mmServerSocket.accept();
                }
            } catch (IOException var4) {
                var4.printStackTrace();
            }
        }

        public void cancel() {
            if (debug) {
                Log.e(TAG, "closeSocket " + this);
            }
            try {
                mmServerSocket.close();
            } catch (IOException var2) {
                Log.e(TAG, "close() of server failed", var2);
            }

        }
    }


    private class ConnectToDeviceThread extends Thread {
        private BluetoothSocket mSocket;

        public ConnectToDeviceThread(String device) {
            try {
                BluetoothDevice d = adapter.getRemoteDevice(device);
                this.mSocket = d.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            super.run();
            try {
                if (mSocket == null) {
                    Log.e("zjy", "MyBluePrinter->run(): created btclient failed==");
                    sendMsg(STATE_DISCONNECTED);
                    return;
                }
                Log.e("zjy", "MyBluePrinter->run(): hasConnected==" + mSocket.isConnected());
                if (mSocket.isConnected()) {
                    mState = STATE_CONNECTED;
                    mHandler.sendEmptyMessage(STATE_CONNECTED);
                    return;
                }
                mSocket.connect();
                address = mSocket.getRemoteDevice().getAddress();
                mState = STATE_CONNECTED;
                mHandler.sendEmptyMessage(STATE_CONNECTED);
            } catch (IOException e) {
                e.printStackTrace();
                mHandler.sendEmptyMessage(STATE_DISCONNECTED);
            }
        }

        public BluetoothSocket getmSocket() {
            return mSocket;
        }
    }

    private class StateCheckThread extends Thread {
        private BluetoothSocket mSocket;
        private boolean cancel = false;

        public StateCheckThread(BluetoothSocket mSocket) {
            this.mSocket = mSocket;
        }

        @Override
        public void run() {
            while (!cancel) {
                try {
                    byte[] buffer = new byte[1024];
                    InputStream inputStream = mSocket.getInputStream();
                    int bytes = inputStream.read(buffer);
                    if (bytes <= 0) {
                        Log.e("zjy", "MyBluePrinter->StateCheckThread: inputStream null==");
                    }
                    Log.e("zjy", "MyBluePrinter->StateCheckThread: inputStream buffer[0]==" + buffer[0]);
                    if (buffer[0] != 19) {
                        if (buffer[0] != 17) {
                        } else {
                            bufferFull = false;
                            Log.e(TAG, "0x11:");
                        }
                    } else {
                        //缓存区满了
                        bufferFull = true;
                        Log.e(TAG, "0x13:");
                    }
                    try {
                        Thread.sleep(3 * 1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    mState = STATE_DISCONNECTED;
                    try {
                        mSocket.connect();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                    sendMsg(mState);
                }
            }
        }

        public void setmSocket(BluetoothSocket mSocket) {
            this.mSocket = mSocket;
        }

        public void setCancel(boolean cancel) {
            this.cancel = cancel;
        }
    }

    public Set<BluetoothDevice> getBindedDevice() {
        Set<BluetoothDevice> bondedDevices = adapter.getBondedDevices();
        return bondedDevices;
    }

    public interface OnReceiveDataHandleEvent {
        void OnReceive(BluetoothDevice var1);
    }
}
