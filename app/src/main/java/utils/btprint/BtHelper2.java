package utils.btprint;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Created by 张建宇 on 2019/9/5.
 */
public class BtHelper2 extends BtHelper {
    public BtHelper2(Context mContext) {
        super(mContext);
    }

    public BtHelper2(Context mContext, SPrinter.MListener listener) {
        super(mContext);
        listeners.add(listener);
    }

    List<SPrinter.MListener> listeners = new ArrayList<>();
    HashMap<String, BluetoothDevice> devCache = new HashMap<>();
    String nowDevMac;


    private UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    public OutputStream dataOut;
    public InputStream dataIn;

    void connect(String btMac) {
        BluetoothDevice mDevice = null;
        try {
            if (btMac == null) {
                throw new Exception("mac地址为null");
            }

            mDevice = devCache.get(btMac);

            if (mDevice == null) {
                boolean b = BluetoothAdapter.checkBluetoothAddress(btMac);
                if (!b) {
                    throw new Exception("非法蓝牙地址");
                }
                mDevice = getDeviceByMac(btMac);
            }
            BluetoothSocket btSocket = null;
         /*   if (btMac.equals(nowDevMac)) {
                sendMsg(BtHelper.STATE_CONNECTED);
                return;
            }*/

            if (dataOut != null) {
                dataOut.close();
            }
            if (dataIn != null) {
                dataIn.close();
            }
            btSocket = mDevice.createRfcommSocketToServiceRecord(uuid);
            btSocket.connect();
                        dataOut = btSocket.getOutputStream();
                        dataIn = btSocket.getInputStream();
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
//                mDevice.connectGatt(mContext, false, new BluetoothGattCallback() {
//                    @Override
//                    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
//                        super.onConnectionStateChange(gatt, status, newState);
//                        List<BluetoothGattService> services = gatt.getServices();
//                        if (services.size() > 0) {
//                            BluetoothGattService bluetoothGattService = services.get(0);
//                            UUID uuid = bluetoothGattService.getUuid();
//                        }
//                    }
//                    @Override
//                    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
//                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
//                            final BluetoothGattService service = gatt.getService(uuid);
//                            if (null != service) {
//                                BluetoothGattCharacteristic read_characteristic = service.getCharacteristic(uuid);
//                                if (null != read_characteristic) {
//                                    int properties = read_characteristic.getProperties();
//                                    if ((properties | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
//                                        gatt.setCharacteristicNotification(read_characteristic, true);
//                                    }
//                                }
//                            }
//                        }
//                    }
//                });
//            }
            nowDevMac = btMac;
            devCache.put(btMac, mDevice);
            sendMsg(BtHelper.STATE_CONNECTED);
        } catch (IOException e) {
            Log.e("zjy", getClass() + "->connect():m1 ==" , e);
            BluetoothSocket mmSocket = null;
            try {
                Method m = mDevice.getClass().getMethod("createRfcommSocket",
                        new Class[]{int.class});
                mmSocket = (BluetoothSocket) m.invoke(mDevice, 1);
                mmSocket.connect();
                dataOut = mmSocket.getOutputStream();
                dataIn = mmSocket.getInputStream();
                nowDevMac = btMac;
                devCache.put(btMac, mDevice);
                sendMsg(BtHelper.STATE_CONNECTED);
            } catch (Exception e2) {
                Log.e("zjy", getClass() + "->connect():m2 ==" , e2);
                try {
                    mmSocket.close();
                } catch (Exception ie) {
                    ie.printStackTrace();
                }
            }
            sendMsg(BtHelper.STATE_DISCONNECTED);

        } catch (Exception e) {
            sendMsg(BtHelper.STATE_DISCONNECTED);
            e.printStackTrace();
        }
    }

    void sendErrorMsg( String msg) {
        for (int i = 0; i < listeners.size(); i++) {
            SPrinter.MListener mListener = listeners.get(i);
        }
    }

    @Override
    public boolean write(byte[] var1) {
        if (dataOut == null) {
            return false;
        }
        try {
            dataOut.write(var1);
            return true;
        } catch (IOException e) {
            sendMsg(STATE_DISCONNECTED);
            e.printStackTrace();
        }
        return super.write(var1);
    }

    public void addListener(SPrinter.MListener event) {
        listeners.add(event);
    }


    public static final int STATE_CONNECTED = 1;
    public static final int STATE_DISCONNECTED = 2;
    public final static int STATE_SCAN_FINISHED = 3;
    public final static int STATE_OPENED = 4;
    private List<BluetoothDevice> devices = new ArrayList<>();
    public boolean debug = false;
    private BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();

    public boolean isRegisted = false;


    public void startScan() {
        cancelScan();
        adapter.startDiscovery();
    }

    public void cancelScan() {
        if (adapter.isDiscovering()) {
            adapter.cancelDiscovery();
        }
    }

    MyBtReceive btReceive;

    public void setContext(Context mContext) {
        this.mContext = mContext;
    }

    static class MyBtReceive extends BroadcastReceiver {
        private BtHelper2 helper;

        public MyBtReceive(BtHelper2 helper) {
            this.helper = helper;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.e("zjy", "BtHelper2->onReceive(): Receiver==" + action);
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
        for (int i = 0; i < listeners.size(); i++) {
            SPrinter.MListener mListener = listeners.get(i);
            mListener.onDeviceReceive(d);

        }
    }

    public List<BluetoothDevice> getBindedDevices() {
        Set<BluetoothDevice> bondedDevices = adapter.getBondedDevices();
        devices.clear();
        devices.addAll(bondedDevices);
        return devices;
    }

    public void sendMsg(int what) {
        if (debug) {
            Log.e("zjy", "BtHelper2->sendMsg(): Messaage==" + what);
        }
        for (int i = 0; i < listeners.size(); i++) {
            SPrinter.MListener mListener = listeners.get(i);
            mListener.sendMsg(what);
        }
    }

    public void openBt() {
        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        mContext.startActivity(intent);
    }

    @Override
    public void closeConnect() {
        if (dataOut != null) {
            try {
                dataIn.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void close() {
        adapter.disable();
        unRegister();
        devCache.clear();

    }


    public boolean isOpen() {
        return adapter.isEnabled();
    }

    public BluetoothDevice getDeviceByMac(String mac) {
        return adapter.getRemoteDevice(mac);
    }

    public void unRegister() {
        if (btReceive != null) {
            try {
                mContext.unregisterReceiver(btReceive);
            } catch (Throwable e) {
                e.printStackTrace();
            }
            listeners.clear();
            btReceive = null;
        }
        Log.e("zjy", "BtHelper2->unRegister(): unRegist==" + toString());
        isRegisted = false;
    }

    public void register() {
        if (btReceive != null) {
            return;
        }
        btReceive = new MyBtReceive(this);
        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        mContext.registerReceiver(btReceive, filter);
        Log.e("zjy", "BtHelper2->register(): register==" + toString());
        isRegisted = true;
    }
    
}

