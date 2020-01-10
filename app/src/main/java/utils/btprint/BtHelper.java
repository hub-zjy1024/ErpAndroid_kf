package utils.btprint;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import java.lang.ref.WeakReference;
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
    public final static int STATE_REGISTER_Failed = 6;
    public final static int STATE_UnREGISTER_Failed = 7;
    private List<BluetoothDevice> devices;
    public boolean debug = false;
    private BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
    public WeakReference<Context> weakCtx;
    private static final String lName = BtHelper.class.getName();

    protected IntentFilter mFilter;

    protected MyBtReceive2 cacheListener;
    public boolean isRegisted = false;

    public BtHelper(Context mContext) {
        weakCtx = new WeakReference<>(mContext);
        devices = new ArrayList<>();
        mFilter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        mFilter.addAction(BluetoothDevice.ACTION_FOUND);
        mFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
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
                }else if (extras1 ==BluetoothAdapter.STATE_OFF) {
                    helper.sendMsg(STATE_DISCONNECTED);
            }
            }
        }
    }

    public abstract static class MyBtReceive2 extends BroadcastReceiver {

        public abstract void onMsg(int msg);

        public abstract void onDeviceReceive(BluetoothDevice device);

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d("zjy", toString() + "->onReceive(): Receiver==" + action);
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Log.d("zjy", toString() + "->onDeviceReceive(): device==" + device.toString());
                onDeviceReceive(device);
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                onMsg(STATE_SCAN_FINISHED);
            } else if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                int extras1 = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -3);
                if (extras1 == BluetoothAdapter.STATE_ON) {
                    onMsg(STATE_OPENED);
                } else if (extras1 == BluetoothAdapter.STATE_OFF) {
                    onMsg(STATE_DISCONNECTED);
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
        if (cacheListener != null) {
            cacheListener.onMsg(what);
        }
    }

    public void openBt() {
        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        Context temp = weakCtx.get();
        if (temp != null) {
            temp.startActivity(intent);
        }
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
            Context temp = weakCtx.get();
            if (temp != null) {
                try {
                    temp.unregisterReceiver(listener);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
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
        Context temp = weakCtx.get();
        if (temp != null) {
            try {
                temp.registerReceiver(listener, mFilter);
                Log.e("zjy", "BtHelper->register(): register==" + toString());
                isRegisted = true;
            } catch (Throwable a) {
                a.printStackTrace();
            }
        }
    }

    public void unRegister(Context mContext) {
        if (listener != null) {
            mContext.unregisterReceiver(listener);
            listener = null;
        }
        Log.e("zjy", "BtHelper->unRegister(): unRegist==" + toString());
        isRegisted = false;
    }

    public void register(Context mContext, MyBtReceive2 myBtReceive2) {
        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        try {
            mContext.registerReceiver(myBtReceive2, filter);
            cacheListener = myBtReceive2;
            Log.d("zjy", lName+"->register(): register bt2==" + myBtReceive2.toString());
        } catch (Throwable e) {
            myBtReceive2.onMsg(STATE_REGISTER_Failed);
            e.printStackTrace();
        }
    }

    public void unRegister(Context mContext, MyBtReceive2 myBtReceive2) {
        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        try {
            mContext.unregisterReceiver( myBtReceive2);
            Log.d("zjy", lName+"->unRegister(): unregister bt2==" + myBtReceive2.toString());
        } catch (Throwable e) {
            myBtReceive2.onMsg(STATE_REGISTER_Failed);
            e.printStackTrace();
        }
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
