package utils.btprint;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.util.Log;

import java.io.InputStream;
import java.lang.reflect.Constructor;

import utils.btprint.suofang.SuoFangPrinter;

/**
 * Created by 张建宇 on 2019/9/5.
 */
public class SPrinter2 extends SPrinter {
    public SPrinter2(BtHelper helper) {
        super(helper);
    }

    private SPrinter2(Context mContext, final SPrinter.MListener event) {
        super(helper);
        helper = new BtHelper2(mContext, event);
    }
    public static BtHelper2 helper;
    private String charsetName = "GBK";
    private static SPrinter printer;
    private static int MULTIPLE = 8;
    private static final int page_width = 48 * MULTIPLE;
    private static final int page_height = 75 * MULTIPLE;
    private static final int margin_horizontal = 2 * MULTIPLE;
    private static final int top_left_x = margin_horizontal;
    private static final int margin_vertical = 2 * MULTIPLE;
    private static final byte[] CMD_INIT = new byte[]{27, 64};
    private InputStream dataIn;
  /*  private BtHelper helper;*/

    public synchronized static SPrinter getPrinter(Context mContext, final SPrinter.MListener event) {
        if (printer == null) {
            printer = new SPrinter2(mContext, event);
        } else {
            helper.addListener(event);
            helper.setContext(mContext);
        }
        return printer;
    }

    public synchronized static SPrinter getPrinter(Context mContext) {
        if (printer == null) {
            BtHelper2 helper2 = new BtHelper2(mContext);
            printer = new SPrinter2(helper2);
            helper = helper2;
//            printer = new SPrinter2(mContext);
        } else {
            helper.setContext(mContext);
        }
        return printer;
    }

    public synchronized static void findPrinter(BluetoothDevice device) {
        findPrinter(device.getName());
    }
    public synchronized static void findPrinter(String printer) {
        if (SuoFangPrinter.isSuoFang(printer)) {
            setPrinter(SuoFangPrinter.class);
        }
    }
    public synchronized static void setPrinter(Class mCla) {
        try {
            Constructor constructor = mCla.getConstructor(BtHelper.class);
            printer = (SPrinter) constructor.newInstance(helper);
            Log.e("zjy", "SPrinter2->setPrinter(): init Printer==" + mCla.getName());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void closeConnect() {
        helper.closeConnect();
    }

    public synchronized static SPrinter getPrinter() {
        return printer;
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

    public synchronized void closeSocket() {
    }

    @Override
    public void connect(String var1) {
        helper.connect(var1);
    }

    @Override
    public boolean write(byte[] var1) {
        return helper.write(var1);
    }

    @Override
    public boolean isOpen() {
        return helper.isOpen();
    }

    public boolean newLine(int lines) {
        for(int i=0;i<lines;i++) {
            newLine();
        }
        return true;
    }

}
