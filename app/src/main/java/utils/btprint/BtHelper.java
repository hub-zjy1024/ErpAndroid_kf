package utils.btprint;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 Created by 张建宇 on 2018/3/21. */
public class BtHelper {
    public static final int STATE_CONNECTED = 1;
    public static final int STATE_DISCONNECTED = 2;
    public static final int STATE_ERROR = 5;
    public final static int STATE_SCAN_FINISHED = 3;
    public final static int STATE_OPENED = 4;
    private List<BluetoothDevice> devices;
    public boolean debug = false;
    private BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
    public Context mContext;

    public boolean isRegisted = false;

    public BtHelper(Context mContext) {
        this.mContext = mContext;
        devices = new ArrayList<>();
    }

    private MyBtReceive listener;

    public void startScan() {
        cancelScan();
        adapter.startDiscovery();
    }

    public void cancelScan() {
        if (adapter.isDiscovering()) {
            adapter.cancelDiscovery();
        }
    }

    public boolean write(byte[] var1) {
        return false;
    }

    static class MyBtReceive extends BroadcastReceiver {
        private BtHelper helper;

        public MyBtReceive(BtHelper helper) {
            this.helper = helper;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.e("zjy", "BtHelper->onReceive(): Receiver==" + action);
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                helper.onDeviceReceive(device);
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                helper.sendMsg(STATE_SCAN_FINISHED);
            } else if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                int extras1 = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -3);
                if (extras1 == BluetoothAdapter.STATE_ON) {
                    helper.sendMsg(STATE_OPENED);
                }
            }
        }
    }

    public void onDeviceReceive(BluetoothDevice d) {

    }

    public List<BluetoothDevice> getBindedDevices() {
        Set<BluetoothDevice> bondedDevices = adapter.getBondedDevices();
        devices.clear();
        devices.addAll(bondedDevices);
        return devices;
    }

    public void sendMsg(int what) {
        if (debug) {
            Log.e("zjy", "BtHelper->sendMsg(): Messaage==" + what);
        }
    }

    public void openBt() {
        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        mContext.startActivity(intent);
    }

    public void closeBt() {
        adapter.disable();
    }

    public void closeConnect() {
    }

    public void close() {
        adapter.disable();
        unRegister();
    }


    public boolean isOpen() {
        return adapter.isEnabled();
    }

    public BluetoothDevice getDeviceByMac(String mac) {
        return adapter.getRemoteDevice(mac);
    }

    public void unRegister() {
        if (listener != null) {
            mContext.unregisterReceiver(listener);
            listener = null;
        }
        Log.e("zjy", "BtHelper->unRegister(): unRegist==" + toString());
        isRegisted = false;
    }

    public void register() {
        if (listener != null) {
            return;
        }
        listener = new MyBtReceive(this);
        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        mContext.registerReceiver(listener, filter);
        Log.e("zjy", "BtHelper->register(): register==" + toString());
        isRegisted = true;
    }

    public void unRegister(Context mContext) {
        if (listener != null) {
            mContext.unregisterReceiver(listener);
            listener = null;
        }
        Log.e("zjy", "BtHelper->unRegister(): unRegist==" + toString());
        isRegisted = false;
    }

    public void register(Context mContext) {
        if (listener != null) {
            return;
        }
        listener = new MyBtReceive(this);
        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        mContext.registerReceiver(listener, filter);
        Log.e("zjy", "BtHelper->register(): register==" + toString());
        isRegisted = true;
    }
}
