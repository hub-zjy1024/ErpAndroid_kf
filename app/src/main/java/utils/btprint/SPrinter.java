package utils.btprint;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 Created by 张建宇 on 2017/12/1. */

public class SPrinter extends MyPrinterParent {
    private String charsetName = "GBK";
    private static SPrinter printer;
    private BluetoothDevice mDevice;
    private OutputStream dataOut;
    private UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static int MULTIPLE = 8;
    private static final int page_width = 48 * MULTIPLE;
    private static final int page_height = 75 * MULTIPLE;
    private static final int margin_horizontal = 2 * MULTIPLE;
    private static final int top_left_x = margin_horizontal;
    private static final int margin_vertical = 2 * MULTIPLE;
    private static final byte[] CMD_INIT = new byte[]{27, 64};
    private InputStream dataIn;
    private BtHelper helper;
    private SPrinter(Context mContext) {
        helper = new BtHelper(mContext) ;
    }

    public SPrinter(BtHelper helper) {
        this.helper = helper;
    }

    public interface MListener {
        void sendMsg(int what);

        void onDeviceReceive(BluetoothDevice d);
    }

    public synchronized static SPrinter getPrinter(Context mContext, final MListener event) {
        if (printer == null) {
            printer = new SPrinter(new BtHelper(mContext) {
                @Override
                public void sendMsg(int what) {
                    super.sendMsg(what);
                    event.sendMsg(what);
                }

                @Override
                public void onDeviceReceive(BluetoothDevice d) {
                    super.onDeviceReceive(d);
                    event.onDeviceReceive(d);
                }
            });
            return printer;
        }
        printer.helper = new BtHelper(mContext) {
            @Override
            public void sendMsg(int what) {
                super.sendMsg(what);
                event.sendMsg(what);
            }

            @Override
            public void onDeviceReceive(BluetoothDevice d) {
                super.onDeviceReceive(d);
                event.onDeviceReceive(d);
            }
        };
        return printer;
    }
    public synchronized static SPrinter getPrinter() {
//        return printer;
        return SPrinter2.getPrinter();
    }

    public static class PBarcodeType {
        public static final int EAN13 = 1;
        public static final int EAN8 = 2;
        public static final int CODE39 = 3;
        public static final int CODE93 = 4;
        public static final int CODE128 = 5;
        public static final int CODABAR = 6;
        public static final int UPCA = 8;
        public static final int UPCE = 9;

    }

    static class PAlign {
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

    public int getPrintingStatus(StringBuffer Msg, int timeout) {
        int timeDiff = 500;
        int ret = 0;
        boolean var5 = false;
        try {
            byte[] tempReadBytes = new byte[16];
            int readLen = this.read(tempReadBytes);
            if (readLen != 0) {
                Log.e("fdh", "read() old data this.tempReadBytes!=null!");
            }

            for (int i = 0; i < 3; ++i) {
                boolean ok = this.write(new byte[]{29, 40, 72, 6, 0, 48, 48, 49, 50, 51, 52});
            }
            int retStatus;
            if (ret < 0) {
            } else {
                Thread.sleep(100L);
                byte[] pReadBytes = new byte[16];
                int count = (timeout - 100 - 200) / timeDiff;
                Log.e("fdh", "read count=" + count + "次");
                int readLen0;
                for (readLen0 = 0; count > 0; --count) {
                    readLen0 = this.read(pReadBytes);
                    if (readLen0 != 0) {
                        Log.d("fdh", "read1  is not null!");
                        break;
                    }

                    Log.d("fdh", "sleep(500)...");
                    Thread.sleep((long) timeDiff);
                    Log.d("fdh", " 第" + (timeout / timeDiff - count) + "次读");
                }

                Log.i("PrinterInstance", "readLen0:" + readLen0);

                for (int i = 0; i < readLen0; ++i) {
                    Log.i("PrinterInstance", String.valueOf(pReadBytes[i]));
                }

                if (readLen0 == 0) {
                    Log.d("fdh", "timeout and read pReadBytes is null!");
                    retStatus = this.getCurrentStatus();
                    Log.e("fdh", "read failed! and getPrinterStatus is:" + retStatus);
                    if (retStatus == 0) {
                        Msg.append("未打印完成，正在打印中！");
                        return -2;
                    }

                    if (retStatus == -2) {
                        Msg.append("未打印完成，因为缺纸！");
                        return -3;
                    }

                    if (retStatus == -4) {
                        Msg.append("打印未完成，纸舱盖开盖！");
                        return -4;
                    }

                    if (retStatus == -1) {
                        Msg.append("打印未完成，与打印机通信失败!");
                        return -5;
                    }
                }

                if (readLen0 < 7) {
                    Log.e("fdh", "pReadBytes.length!=7 pReadBytes.length=" + pReadBytes.length);
                    return -7;
                } else {
                    Log.e("fdh", "读到7个以上数据是:");
                    if (pReadBytes[0] == 55 && pReadBytes[1] == 34 && pReadBytes[2] == 49 && pReadBytes[3] == 50 &&
                            pReadBytes[4] == 51 && pReadBytes[5] == 52 && pReadBytes[6] == 0) {
                        Log.e("fdh", "打印已经完成:");
                        Msg.append("当前打印已经完成");
                        return 0;
                    } else {
                        Msg.append("接收数据格式不正确！");
                        return -7;
                    }
                }
            }
        } catch (Exception var12) {
            var12.printStackTrace();
            Log.e("fdh", "fdh at PrinterInstance.java getPrintingStatus() Exception! ex.getMessage()=" + var12.getMessage());
            Msg.append("未知异常！");
            return -1;
        }
        return 0;
    }

    public int getPaperStatus() {
        int retCode = -1;
        int readLen = -1;
        write(new byte[]{16, 4, 4});
        byte[] buffer = new byte[16];
        readLen = this.read(buffer);
        for (byte aBuffer : buffer) {
            Log.e("zjy", "SPrinter->getPaperStatus(): ==byte" + aBuffer);
        }
        return -1;
    }

    public int getCurrentStatus() {
        int readLen = -1;
        byte retByte = -1;

        int i;
        for (i = 0; i < 2; ++i) {
            this.write(new byte[]{16, 4, 4});
            try {
                Thread.sleep(100L);
            } catch (InterruptedException var7) {
                var7.printStackTrace();
            }
        }

        byte[] buffer;
        int k;
        label127:
        for (i = 0; i < 3; ++i) {
            buffer = new byte[16];
            readLen = this.read(buffer);
            Log.i("PrinterInstance", "readLen:" + readLen);
            if (readLen > 1 && readLen < 65535) {
                k = 0;
                while (true) {
                    if (k >= readLen) {
                        break label127;
                    }
                    Log.i("status1", "b" + k + ":" + buffer[k]);
                    if (buffer[k] != 0) {
                        retByte = buffer[k];
                    }
                    ++k;
                }
            } else if (readLen == 1) {
                if ((retByte = buffer[0]) != 0) {
                    Log.i("status1", "b:" + retByte);
                    retByte = buffer[0];
                    break;
                }

                Log.i("status1", "收到0");
            } else if (readLen == 65535) {
                return -1;
            }
        }

        if (readLen == 0) {
            return -1;
        } else {
            Log.e("PrinterInstance", "retByte0：  " + retByte);
            if ((retByte & 96) == 96) {
                return -2;
            } else if ((retByte & 12) == 12) {
                return -3;
            } else {
                for (i = 0; i < 2; ++i) {
                    this.write(new byte[]{16, 4, 2});

                    try {
                        Thread.sleep(100L);
                    } catch (InterruptedException var6) {
                        var6.printStackTrace();
                    }
                }

                label103:
                for (i = 0; i < 3; ++i) {
                    buffer = new byte[16];
                    readLen = this.read(buffer);
                    Log.i("PrinterInstance", "readLen:" + readLen);
                    if (readLen > 1 && readLen < 65535) {
                        k = 0;

                        while (true) {
                            if (k >= readLen) {
                                break label103;
                            }

                            Log.i("status1", "b" + k + ":" + buffer[k]);
                            if (buffer[k] != 0) {
                                retByte = buffer[k];
                            }

                            ++k;
                        }
                    }

                    if (readLen == 1) {
                        if ((retByte = buffer[0]) != 0) {
                            Log.i("status1", "b:" + retByte);
                            retByte = buffer[0];
                            break;
                        }

                        Log.i("status1", "收到0");
                    } else if (readLen == 65535) {
                        return -1;
                    }
                }

                if (readLen == 0) {
                    return -1;
                } else {
                    Log.e("PrinterInstance", "retByte1：  " + retByte);
                    return (retByte & 4) == 4 ? -4 : 0;
                }
            }
        }
    }

    public int read(byte[] buffer) {
        int readLen = -1;
        try {
            if (dataIn != null && (readLen = dataIn.available()) > 0) {
                readLen = dataIn.read(buffer);
            }
        } catch (IOException var4) {
            Log.e("zjy", "BluetoothPort read error");
            var4.printStackTrace();
        } catch (Exception var5) {
            var5.printStackTrace();
        }
        Log.w("BluetoothPort", "read length:" + readLen);
        return readLen;
    }

    public Set<BluetoothDevice> getBindedDevice() {
        Set<BluetoothDevice> bondedDevices = new HashSet<>(helper.getBindedDevices());
        return bondedDevices;
    }

    public void drawBarCode(int start_x, int start_y, String text, int type, int linewidth, int height) {
        drawBarCode(start_x, start_y, 0, 0, 0, 0, 0, text, type, linewidth, height);
    }

    public void drawBarCode(int area_start_x, int area_start_y, int area_end_x, int area_end_y, int xAlign, int yAlign,
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
        helper.sendMsg(what);
    }

    /**
     code128
     @param code
     @param lablePlace 条码内容显示位置，0：不显示，1：上面，2：下面，3：上下都有
     @param width
     @param height      */
    public void printBarCode(String code, int lablePlace, int width, int height) {
        SPrintBarcode barcode2 = new SPrintBarcode((byte) 73, width, height,
                lablePlace, code);
        write(barcode2.getBarcodeData());
    }

    @Override
    public void open() {
        helper.openBt();
    }

    @Override
    public void close() {
        helper.close();
    }

    public void closeConnect() {
        helper.closeConnect();
        if (dataOut != null) {
            try {
                dataOut.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            dataOut = null;
        }
    }

    public void closeBt() {
        helper.closeBt();
    }

    public synchronized void closeSocket() {
        try {
            if (dataOut != null) {
                dataOut.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void scan() {
        registeBroadCast();
        helper.startScan();
    }

    public void stopScan() {
        helper.cancelScan();
    }

    public static String label_set_page(int width, int height, int rotate) {
        String str = "! 0 200 200 " + height + " 1\r\nPW " + width + "\r\n";
        Log.i("fdh", str);
        return str;
    }

    public void registeBroadCast() {
        synchronized (this) {
            helper.register();
        }
    }

    public void registeBroadCast(Context mContext) {
        synchronized (this) {
            helper.register(mContext);
        }
    }

    public void pageSetup() {
        this.printText(label_set_page(page_width, page_height, 0));
    }

    @Override
    public void connect(String var1) {
        mDevice = helper.getDeviceByMac(var1);
        BluetoothSocket btSocket = null;
        try {
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
            sendMsg(BtHelper.STATE_CONNECTED);
        } catch (IOException e) {
            sendMsg(BtHelper.STATE_DISCONNECTED);
            e.printStackTrace();
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
        return false;
    }

    @Override
    public boolean isOpen() {
        return helper.isOpen();
    }

    @Override
    public boolean newLine() {
        return write(CMD_NEWLINE);
    }
    public boolean initPrinter() {
        return write(CMD_INIT_PRINTER);
    }
    public boolean newLine(int lines) {
        for(int i=0;i<lines;i++) {
            newLine();
        }
        return true;
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

    public void unRegisterReceiver() {
        synchronized (this){
            helper.unRegister();
        }
    }

    public void unRegisterReceiver(Context mContext) {
        synchronized (this) {
            helper.unRegister(mContext);
        }
    }

    public void setPrinter(int command, int value) {
        byte[] arrayOfByte = new byte[3];
        switch (command) {
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
                if (value > 2 || value < 0) {
                    value = 0;
                }
        }
        arrayOfByte[2] = (byte) value;
        this.write(arrayOfByte);
    }

}
