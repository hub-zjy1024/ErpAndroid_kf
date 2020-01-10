package utils.btprint;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
    List<MyBtReceive2> listeners2 = new ArrayList<>();

    HashMap<String, BluetoothDevice> devCache = new HashMap<>();
    String nowDevMac;

    private static final String mName = BtHelper2.class.getName();

    private MyBtReceive2 myBtReceive2;
    private UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    public OutputStream dataOut;
    public InputStream dataIn;

    public void connect(String btMac) {
        cancelScan();
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
            //                    public void onConnectionStateChange(BluetoothGatt gatt, int status, int
            //                    newState) {
            //                        super.onConnectionStateChange(gatt, status, newState);
            //                        List<BluetoothGattService> services = gatt.getServices();
            //                        if (services.size() > 0) {
            //                            BluetoothGattService bluetoothGattService = services.get(0);
            //                            UUID uuid = bluetoothGattService.getUuid();
            //                        }
            //                    }
            //                    @Override
            //                    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            //                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build
            //                        .VERSION_CODES.JELLY_BEAN_MR2) {
            //                            final BluetoothGattService service = gatt.getService(uuid);
            //                            if (null != service) {
            //                                BluetoothGattCharacteristic read_characteristic = service
            //                                .getCharacteristic(uuid);
            //                                if (null != read_characteristic) {
            //                                    int properties = read_characteristic.getProperties();
            //                                    if ((properties | BluetoothGattCharacteristic
            //                                    .PROPERTY_NOTIFY) > 0) {
            //                                        gatt.setCharacteristicNotification
            //                                        (read_characteristic, true);
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
            Log.d("zjy", mName + "->connect(): connected1==");

        } catch (IOException e) {
            Log.w("zjy", mName + "->connect():m1 ==", e);
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
                Log.d("zjy", mName + "BtHelper2->connect(): connected2==");
            } catch (Exception e2) {
                Log.w("zjy", mName + "->connect():m2 ==", e2);
                try {
                    mmSocket.close();
                } catch (Exception ie) {
                    ie.printStackTrace();
                }
                sendMsg(BtHelper.STATE_DISCONNECTED);
            }
        } catch (Exception e) {
            sendMsg(BtHelper.STATE_DISCONNECTED);
            e.printStackTrace();
        }
    }

    void sendErrorMsg(String msg) {
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
        if (!listeners.contains(event)) {
            listeners.add(event);
        }
    }


    public void unRegister(Context mContext, MyBtReceive2 myBtReceive2) {
        super.unRegister(mContext, myBtReceive2);
        listeners2.remove(myBtReceive2);
    }

    public void register(Context mContext, MyBtReceive2 myBtReceive2) {
        super.register(mContext, myBtReceive2);
        if (!listeners2.contains(myBtReceive2)) {
            listeners2.add(myBtReceive2);
        }
    }

    private List<BluetoothDevice> devices = new ArrayList<>();
    public boolean debug = false;
    private BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();

    public boolean isRegisted = false;

    MyBtReceive btReceive;

    public void setContext(Context mContext) {
        weakCtx = new WeakReference<>(mContext);
    }

    public void onDeviceReceive(BluetoothDevice d) {
        for (int i = 0; i < listeners.size(); i++) {
            SPrinter.MListener mListener = listeners.get(i);
            mListener.onDeviceReceive(d);

        }
    }


    public void sendMsg(int what) {
        //        if (debug) {
        //            Log.d("zjy", mName+"->sendMsg(): Messaage==" + what);
        //        }
        for (int i = 0; i < listeners2.size(); i++) {
            MyBtReceive2 mListener = listeners2.get(i);
            Log.d("zjy", "send Listener->sendMsg(): ==" + mListener.toString() + ",what=" + what);
            mListener.onMsg(what);
        }
    }


    @Override
    public void closeConnect() {
        if (dataOut != null) {
            try {
                dataOut.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void close() {
        adapter.disable();
        devCache.clear();

    }

    public void unRegister() {
        if (btReceive != null) {
            try {
                Context temp = weakCtx.get();
                if (temp != null) {
                    temp.unregisterReceiver(btReceive);
                }
                Log.d("zjy", mName + "->unRegister(): unRegist==" + btReceive.toString());
            } catch (Throwable e) {
                e.printStackTrace();
                Log.w("zjy",
                        mName + "->unRegister(): unRegist failed==" + btReceive.toString());
            }
            listeners.clear();
            btReceive = null;
        }
        isRegisted = false;
    }

    public void register() {
        if (btReceive != null) {
            Log.w("zjy", mName + "->register(): has register==" + btReceive.toString());
            return;
        }
        btReceive = new MyBtReceive(this);
        Context temp = weakCtx.get();
        if (temp != null) {
            try {
                temp.registerReceiver(btReceive, mFilter);
                Log.d("zjy", mName + "->register(): register==" + btReceive.toString());
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
        isRegisted = true;
    }

    public void flush() {
        if (dataOut != null) {
            try {
                dataOut.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

