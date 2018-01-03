package utils.btprint;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Set;
import java.util.UUID;

/**
 Created by 张建宇 on 2017/12/1. */

public class SPrinter extends MyPrinterParent{
    private String charsetName = "GBK";

    private Handler mHandler;
    private int mState;
    public static final int STATE_OPENED = 12;
    private BluetoothDevice mDevice;
    private BluetoothAdapter mAdapter;
    private OutputStream dataOut;
    private UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private boolean isUnregist = true;

    private Context mContext;
    private static int MULTIPLE = 8;
    private static final int page_width = 48 * MULTIPLE;
    private static final int page_height = 75 * MULTIPLE;
    private static final int margin_horizontal = 2 * MULTIPLE;
    private static final int top_left_x = margin_horizontal;
    private static  final int margin_vertical = 2 * MULTIPLE;

    private MyBluePrinter.OnReceiveDataHandleEvent discoverListner;
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.e("zjy", "SPrinter->onReceive(): action==" + action);
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                discoverListner.OnReceive(device);
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                sendMsg(STATE_SCAN_FINISHED);
            } else if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                int extras1 = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -3);
                if (extras1 == BluetoothAdapter.STATE_ON) {
                    sendMsg(STATE_OPENED);
                }
            }
        }
    };

    public SPrinter(String charsetName, Handler mHandler, Context mContext,MyBluePrinter.OnReceiveDataHandleEvent discoverListner) {
        if (charsetName != null) {
            this.charsetName = charsetName;
        }
        this.mHandler = mHandler;
        this.mContext = mContext;
        this.discoverListner = discoverListner;
        this.mAdapter = BluetoothAdapter.getDefaultAdapter();
    }
    public SPrinter(Handler mHandler, Context mContext, MyBluePrinter.OnReceiveDataHandleEvent discoverListner) {
        this(null, mHandler, mContext, discoverListner);
    }

    public static class PBarcodeType{
        public static final int EAN13 = 1;
        public static final int EAN8 = 2;
        public static final int CODE39 = 3;
        public static final int CODE93 = 4;
        public static final int CODE128 = 5;
        public static final int CODABAR = 6;
        public static final int UPCA =8;
        public static final int UPCE = 9;

    }
    static class PAlign{
        public static final int START = 1;
        public static final int END = 2;
        public static final int CENTER = 3;
    }

    public static class Command {
        public static final int INIT_PRINTER = 0;
        public static final int WAKE_PRINTER = 1;
        public static final int PRINT_AND_RETURN_STANDARD = 2;
        public static final int PRINT_AND_NEWLINE = 3;
        public static final int PRINT_AND_ENTER = 4;
        public static final int MOVE_NEXT_TAB_POSITION = 5;
        public static final int DEF_LINE_SPACING = 6;
        public static final int PRINT_AND_WAKE_PAPER_BY_LNCH = 0;
        public static final int PRINT_AND_WAKE_PAPER_BY_LINE = 1;
        public static final int CLOCKWISE_ROTATE_90 = 4;
        public static final int ALIGN = 13;
        public static final int ALIGN_LEFT = 0;
        public static final int ALIGN_CENTER = 1;
        public static final int ALIGN_RIGHT = 2;
        public static final int LINE_HEIGHT = 10;
        public static final int CHARACTER_RIGHT_MARGIN = 11;
        public static final int UNDERLINE = 15;
        public static final int UNDERLINE_OFF = 16;
        public static final int UNDERLINE_ONE_DOTE = 17;
        public static final int UNDERLINE_TWO_DOTE = 18;
        public static final int FONT_MODE = 16;
        public static final int FONT_SIZE = 17;

        public Command() {
        }
    }

    public Set<BluetoothDevice> getBindedDevice() {
        Set<BluetoothDevice> bondedDevices = mAdapter.getBondedDevices();
        return bondedDevices;
    }
    public void drawBarCode(int start_x, int start_y, String text,int type, int linewidth, int height){
        drawBarCode(start_x, start_y, 0, 0, 0, 0, 0, text, type, linewidth, height);
    }
    public void drawBarCode(int area_start_x, int area_start_y, int area_end_x, int area_end_y, int xAlign, int  yAlign,
                            int start_y, String text, int type, int linewidth, int height) {
        byte xa;
        if (xAlign == PAlign.CENTER) {
            xa = 1;
        } else if (xAlign == PAlign.END) {
            xa = 2;
        } else {
            xa = 0;
        }
        if (yAlign == PAlign.CENTER) {
            start_y = area_start_y + (area_end_y - area_start_y - height) / 2;
        } else if (yAlign == PAlign.END) {
            start_y = area_end_y - height;
        } else {
            start_y = area_start_y;
        }
        String barcodeType = "128";
        if (type == PBarcodeType.CODABAR) {
            barcodeType = "CODABAR";
        } else if (type == PBarcodeType.CODE128) {
            barcodeType = "128";
        } else if (type == PBarcodeType.CODE39) {
            barcodeType = "39";
        } else if (type == PBarcodeType.CODE93) {
            barcodeType = "93";
        } else if (type == PBarcodeType.EAN8) {
            barcodeType = "EAN8";
        } else if (type == PBarcodeType.EAN13) {
            barcodeType = "EAN13";
        } else if (type == PBarcodeType.UPCA) {
            barcodeType = "UPCA";
        } else if (type == PBarcodeType.UPCE) {

            barcodeType = "UPCE";
        }
        String str = "BA " + area_start_x + " " + area_start_y + " " + area_end_x + " " + area_end_y + " " + xa + "\r\n";
        this.printText(str);
        String st1 = "B";
        String str2 = st1 + " " + barcodeType + " 1 " + linewidth + " " + height + " " + area_start_x + " " + start_y + " " +
                text + "\r\n";
        this.printText(str2);
        String str3 = "BA 0 0 0 0 3\r\n";
        this.printText(str3);
    }

    public void sendMsg(int what) {
        Message msg = mHandler.obtainMessage(what);
        msg.sendToTarget();
    }

    /** code128
     @param code
     @param lablePlace 条码内容显示位置，0：不显示，1：上面，2：下面，3：上下都有
     @param width
     @param height
     */
    public void printBarCode(String code, int lablePlace, int width, int height) {
        SPrintBarcode barcode2 = new SPrintBarcode((byte) 73, width, height,
                lablePlace, code);
        write(barcode2.getBarcodeData());
    }
    @Override
    public void open() {
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        mContext.registerReceiver(mReceiver, filter);
        Log.e("zjy", "SPrinter->open(): startRegister==");
        isUnregist = false;
//        mAdapter.enable();
        mContext.startActivity(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE));
    }

    @Override
    public void close() {
        mAdapter.disable();
        unRegisterReceiver();
    }

    @Override
    public void scan() {
        if (isUnregist) {
            IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
            filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
            mContext.registerReceiver(mReceiver, filter);
            Log.e("zjy", "SPrinter->open(): startRegister==");
            isUnregist = false;
        }
        if (mAdapter.isDiscovering()) {
            mAdapter.cancelDiscovery();
        }
        mAdapter.startDiscovery();
    }
    public static String label_set_page(int width, int height, int rotate) {
        String str = "! 0 200 200 " + height + " 1\r\nPW " + width + "\r\n";
        Log.i("fdh", str);
        return str;
    }

    public void pageSetup() {
        this.printText(label_set_page(page_width, page_height, 0));
    }
    @Override
    public void connect(String var1) {
        mDevice = mAdapter.getRemoteDevice(var1);
        BluetoothSocket btSocket = null;
        try {
            btSocket = mDevice.createRfcommSocketToServiceRecord(uuid);
            btSocket.connect();
            dataOut= btSocket.getOutputStream();
            sendMsg(STATE_CONNECTED);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean write(byte[] var1) {
        if (dataOut == null) {
            sendMsg(STATE_DISCONNECTED);
            return false;
        }
        try {
            dataOut.write(var1);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean isOpen() {
        return mAdapter.isEnabled();
    }

    @Override
    public boolean newLine() {
        return write(CMD_NEWLINE);
    }

    public boolean printText(String content) {
        byte[] data = null;
        try {
            data = content.getBytes(this.charsetName);
            write(data);
            return true;
        } catch (UnsupportedEncodingException var4) {
            var4.printStackTrace();
        }
        return false;
    }
    public void unRegisterReceiver(){
        if (!isUnregist) {
            mContext.unregisterReceiver(mReceiver);
            isUnregist = true;
        }
    }
    public void setPrinter(int command, int value) {
        byte[] arrayOfByte = new byte[3];
        switch(command) {
            case 0:
                arrayOfByte[0] = 27;
                arrayOfByte[1] = 74;
                break;
            case 1:
                arrayOfByte[0] = 27;
                arrayOfByte[1] = 100;
                break;
            case 4:
                arrayOfByte[0] = 27;
                arrayOfByte[1] = 86;
                break;
            case 11:
                arrayOfByte[0] = 27;
                arrayOfByte[1] = 32;
                break;
            case 13:
                arrayOfByte[0] = 27;
                arrayOfByte[1] = 97;
                if(value > 2 || value < 0) {
                    value = 0;
                }
        }

        arrayOfByte[2] = (byte)value;
        this.write(arrayOfByte);
    }

}
