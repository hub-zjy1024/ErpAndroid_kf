package utils.btprint.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import utils.btprint.BarcodeCreater;


/**
 * Created by 张建宇 on 2019/12/16.
 */
public class PrinterUtils2 {
    public static int width = 312;
    public static int algn_left = 0;
    public static int algn_center = 1;
    public static int algn_right = 2;

    private int height =255;
    public static   byte[] startTextMode = new byte[]{28, 38};
    public static   byte[] exitTextMode = new byte[]{28, 46};
    public static    byte[] newLinen = new byte[]{27  , 100 ,1 };
    private static final byte DLE = 16;
    private static final byte ESC = 27;
    private static final byte FS = 28;
    private static final byte GS = 29;
    public int getWidth() {
        return width;
    }

    /**
     *  * 打印二维码
     *  *
     *  * @param qrData 二维码的内容
     *  * @throws IOException
     *  
     */
    public byte[] qrCode(String qrData) throws IOException {
        int moduleSize = 8;
        String encoding = "utf-8";
        int length = qrData.getBytes().length;
        ByteArrayOutputStream writer = new ByteArrayOutputStream();
        byte[] temp = "(k".getBytes();
        byte[] qrData2 = qrData.getBytes();
        //打印二维码矩阵
        writer.write(0x1D);// init
        writer.write(temp);// adjust height of barcode
        writer.write(length + 3); // pl
        writer.write(0); // ph
        writer.write(49); // cn
        writer.write(80); // fn
        writer.write(48); //
        writer.write(qrData2);

        writer.write(0x1D);
        writer.write(temp);
        writer.write(3);
        writer.write(0);
        writer.write(49);
        writer.write(69);
        writer.write(48);

        writer.write(0x1D);
        writer.write(temp);
        writer.write(3);
        writer.write(0);
        writer.write(49);
        writer.write(67);
        writer.write(moduleSize);

        writer.write(0x1D);
        writer.write(temp);
        writer.write(3); // pl
        writer.write(0); // ph
        writer.write(49); // cn
        writer.write(81); // fn
        writer.write(48); // m
        writer.flush();
        return writer.toByteArray();
    }

    /**
     * 进纸并全部切割
     * @return
     * @throws IOException      
     */
    public static byte[] feedAndCut2() {
        return new byte[]{GS, 86, (byte) 1};

    }

    public static Bitmap newBarCode(Context context, String code, int lablePlace, int width, int height) {
        Bitmap bmap = BarcodeCreater.creatBarcode(context, code,
                width, height, code, 12);
        return bmap;
    }

    public static Bitmap newBarCode(Context context, String code, int lablePlace, int lableSize, int width,
                                    int height) {
        Bitmap bmap = BarcodeCreater.creatBarcode(context, code,
                width, height, code, lablePlace);
        return bmap;
    }
    public byte[] feedAndCut() throws IOException {
        ByteArrayOutputStream writer = new ByteArrayOutputStream();
        writer.write(0x1D);
        writer.write(86);
        writer.write(65);
        //    writer.write(0);
        //切纸前走纸多少
        writer.write(100);
        writer.flush();
        return writer.toByteArray();
        //另外一种切纸的方式
        //    byte[] bytes = {29, 86, 0};
        //    socketOut.write(bytes);
    }
    // if (m == 66) {
    //        return new byte[]{GS, 86, 66, (byte) n};
    //    } else {
    //        return new byte[]{GS, 86, (byte) m};
    //    }

    /**
     * 图片灰度的转化
     * @param r
     * @param g
     * @param b
     * @return
     */
    private static int RGB2Gray(int r, int g, int b){
        int gray = (int) (0.29900 * r + 0.58700 * g + 0.11400 * b);  //灰度转化公式
        return  gray;
    }
    public static byte px2Byte2(int x, int y, Bitmap bit) {
        byte b;
        if (y >= bit.getHeight()) {
            return 0;
        }
        int pixel = bit.getPixel(x, y);
        int red = (pixel & 0x00ff0000) >> 16; // 取高两位
        int green = (pixel & 0x0000ff00) >> 8; // 取中两位
        int blue = pixel & 0x000000ff; // 取低两位
        int gray = RGB2Gray(red, green, blue);
        if (bit.hasAlpha()) {
            int color = pixel;
            //得到alpha通道的值
            int alpha = Color.alpha(color);
            //得到图像的像素RGB的值
            red = Color.red(color);
            green = Color.green(color);
            blue = Color.blue(color);
            final float offset = alpha / 255.0f;
            // 根据透明度将白色与原色叠加
            red = 0xFF + (int) Math.ceil((red - 0xFF) * offset);
            green = 0xFF + (int) Math.ceil((green - 0xFF) * offset);
            blue = 0xFF + (int) Math.ceil((blue - 0xFF) * offset);
            // 接近白色改为白色。其余黑色
            if (red > 160 && green > 160 && blue > 160)
                b = 0;
            else
                b = 1;
        }else {
            if (gray < 128) {
                b = 1;
            } else {
                b = 0;
            }
        }
        return b;
    }

    /**
     * Aligns all the data in one line to the specified position
     * n selects the justification as follows:
     * n=0,48 : Left justification
     * n=1,49 : Centering
     * n=2,50 : Right justification
     * ESC a n
     *
     * @param n 0≤n≤2,48≤n≤50 default 0
     * @return command
     */
    public static byte[] selectAlign(int n) {
        return new byte[]{ESC, 97, (byte) n};
    }

    public static List<byte[]> PrintBitmap(Bitmap bit) {
        return PrintBitmap(bit, 1);
    }

    public static List<byte[]> PrintBitmap(Bitmap bit, int margin) {
        Bitmap tempBit = scalingBitmap(bit, width);
        List<byte[]> mdata = new ArrayList<>();
        // 发送打印图片前导指令
        byte[] start = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x1B,
                0x40, 0x1B, 0x33, 0x00};
        byte[] draw2PxPoint = Newdraw2PxPoint(tempBit);
        // 发送结束指令
        byte[] end = {0x1d, 0x4c, 0x1f, 0x00};
        mdata.add(start);
        byte[] alingin = selectAlign(margin);
        mdata.add(alingin);
        mdata.add(draw2PxPoint);
        mdata.add(end);
        return mdata;
    }

    public static byte[] Newdraw2PxPoint(Bitmap bit) {
        int width = bit.getWidth();
        int height = bit.getHeight();
        float size = 3 * 8;
        int mHeight = (int) Math.ceil(height /size);
        int len = (int) ((width + 6) * mHeight * size);
//        Log.e("zjy", PrinterUtils2.class.getName() + "->draw2PxPoint(): mWidth==");
        byte[] data = new byte[len];
        int k = 0;
        byte nl = (byte) (width % 256);
        byte nh = (byte) (width / 256);
        for (int j = 0; j < mHeight; j++) {
            data[k++] = 0x1B;
            data[k++] = 0x2A;
            data[k++] = 33; // m=33时，选择24点双密度打印，分辨率达到200DPI。
            data[k++] = nl;
            data[k++] = nh;
            for (int i = 0; i < width; i++) {
                for (int m = 0; m < 3; m++) {
                    for (int n = 0; n < 8; n++) {
                        byte b = px2Byte2(i, j * 24 + m * 8 + n, bit);
                        if (b == 1) {
                            data[k] += (byte) (128 >> (n % 8));
                        }
                    }
                    k++;
                }
            }
            //            data[k++] = 10;
        }
        return data;
    }
    public static Bitmap grayScale(final Bitmap bitmap) {
        //0 BT709
        Bitmap matrix = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(matrix);
        Paint paint = new Paint();
        ColorMatrix colorMatrix = new ColorMatrix();
        //传入一个大于1的数字将增加饱和度，而传入一个0～1之间的数字会减少饱和度。0值将产生一幅灰度图像
        //Android ColorMatrix 默认的灰阶计算采用下面的BT709标准
        colorMatrix.setSaturation(0f);
        ColorMatrixColorFilter colorMatrixColorFilter = new ColorMatrixColorFilter(colorMatrix);
        paint.setColorFilter(colorMatrixColorFilter);
        canvas.drawBitmap(bitmap, 0f, 0f, paint);
        canvas.save();
        canvas.restore();
        return matrix;
    }


    public static Bitmap getImage(Bitmap bitmap) {
        Bitmap target;
        target = Bitmap.createBitmap(bitmap);
        Canvas canvas = new Canvas(target);
        int w = bitmap.getWidth();
        int height = bitmap.getWidth();
        Paint paint = new Paint();
        for (int i = 0; i < w; i++) {
            for (int j = 0; j < height; j++) {
                int x = w * j + i;
                int y = j;
                canvas.drawPoint(i, j, paint);
            }
        }
        return target;
    }

    public static Bitmap getImage2(){
        int w = 352;
        int h = 250;
        Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(Color.BLACK);
        paint.setTextSize(20);
        //        canvas.drawColor(Color.WHITE);
        canvas.drawColor(Color.parseColor("#00ffffff"));
        int fisrtLen=50;
        int lineDur=2;
        int marginHorizontal = 72;
        int marginRight = 68;
        int veticalMargin = 50;
        Log.d("zjy", "PrintService->getImage():t3==");
        Paint.FontMetrics metrics= paint.getFontMetrics();
        //        fontMetrics.bottom - fontMetrics.top + fontMetrics.leading;
        float mHeight=metrics.bottom-metrics.top+metrics.leading;
        fisrtLen = (int) (veticalMargin + mHeight);
        Log.d("zjy", "PrintService->getImage(): firstHeight==" + fisrtLen);
        for(int i=0;i<5;i++){
            Log.d(PrinterUtils2.class.getName(), "textY: " + fisrtLen);
            canvas.drawText("esc-printer" + i, marginHorizontal, fisrtLen, paint);
            fisrtLen+=mHeight+lineDur;
        }
        paint.setStrokeWidth(2);
        canvas.drawLine(marginHorizontal , veticalMargin, marginHorizontal, h-veticalMargin, paint);
        canvas.drawLine(w - marginHorizontal, veticalMargin, w - marginHorizontal, h - veticalMargin, paint);
        //横向
        canvas.drawLine(marginHorizontal, veticalMargin, w - marginHorizontal, veticalMargin, paint);
        canvas.drawLine(marginHorizontal, h - veticalMargin, w - marginHorizontal, h - veticalMargin, paint);
        canvas.save();
        canvas.restore();
        return  bitmap;
    }

    public static void savedImage(String path, Bitmap bitmap) {
        FileOutputStream fio = null;
        try {
            fio = new FileOutputStream(path);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100,fio );
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }finally {
            if (fio != null) {
                try {
                    fio.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    public static Bitmap getImage() {
        int w = 352;
//        int h = 180;
        int h = 250;
        Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(Color.BLACK);
        paint.setTextSize(22);
        canvas.drawColor(Color.WHITE);
//        canvas.drawColor(Color.parseColor("#00ffffff"));
        int fisrtLen = 50;
//        int lineDur = 2;
        int lineDur = 0;
        int marginHorizontal = 16;
        int marginRight = 68;
        int veticalMargin = 16;
        Paint.FontMetrics metrics = paint.getFontMetrics();
        //        fontMetrics.bottom - fontMetrics.top + fontMetrics.leading;
        float mHeight = metrics.bottom - metrics.top + metrics.leading;
        fisrtLen = (int) (veticalMargin + mHeight);
        int textleft = 2;
        int vMaxHeight = h - veticalMargin;
        for (int i = 0; fisrtLen < vMaxHeight; i++) {
            Log.d(PrinterUtils2.class.getName(), "textY: " + fisrtLen);
            canvas.drawText("printer-esc" + i, textleft + marginHorizontal, fisrtLen, paint);
            fisrtLen += mHeight + lineDur;
        }
        paint.setStrokeWidth(2);
        canvas.drawLine(marginHorizontal, veticalMargin, marginHorizontal, h - veticalMargin, paint);
        canvas.drawLine(w - marginHorizontal, veticalMargin, w - marginHorizontal, h - veticalMargin, paint);
        //横向
        canvas.drawLine(marginHorizontal, veticalMargin, w - marginHorizontal, veticalMargin, paint);
        canvas.drawLine(marginHorizontal, h - veticalMargin, w - marginHorizontal, h - veticalMargin, paint);
        canvas.save();
        canvas.restore();
        return bitmap;
    }

    /**
     * 缩放图片
     *
     * @param res      资源
     * @param id       ID
     * @param maxWidth 最大宽
     * @return 缩放后的图片
     */
    public static Bitmap scalingBitmap(Resources res, int id, int maxWidth) {
        if (res == null)
            return null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;// 设置只量取宽高
        BitmapFactory.decodeResource(res, id, options);// 量取宽高
        options.inJustDecodeBounds = false;
        // 粗略缩放
        if (maxWidth > 0 && options.outWidth > maxWidth) {
            // 超过限定宽
            double ratio = options.outWidth / (double) maxWidth;// 计算缩放比
            int sampleSize = (int) Math.floor(ratio);// 向下取整，保证缩放后不会低于最大宽高
            if (sampleSize > 1) {
                options.inSampleSize = sampleSize;// 设置缩放比，原图的几分之一
            }
        }
        try {
            Bitmap image = BitmapFactory.decodeResource(res, id, options);
            final int width = image.getWidth();
            final int height = image.getHeight();
            // 精确缩放
            if (maxWidth <= 0 || width <= maxWidth) {
                return image;
            }
            final float scale = maxWidth / (float) width;
            Matrix matrix = new Matrix();
            matrix.postScale(scale, scale);
            Bitmap resizeImage = Bitmap.createBitmap(image, 0, 0, width, height, matrix, true);
            image.recycle();
            return resizeImage;
        } catch (OutOfMemoryError e) {
            return null;
        }
    }

    //    ESC　K　n　　　　　　　　　　　　                   打印并反向走纸
    public static byte[] CMD_ROLLBACK = new byte[]{27, 75, 0};

    public static void addBorder2(Bitmap bitmap) {
        Canvas canvas = new Canvas(bitmap);
        int padding = 2;
        int tempW = bitmap.getWidth();
        int tempH = bitmap.getHeight();
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(Color.BLACK);
        paint.setTextSize(20);
        paint.setStrokeWidth(2);
        paint.setStrokeMiter(2);
        canvas.drawLine(padding, padding, tempW - padding, padding, paint);
        canvas.drawLine(padding, tempH - padding, tempW - padding, tempH - padding, paint);

        canvas.drawLine(padding, padding, padding, tempH - padding, paint);
        canvas.drawLine(tempW - padding, padding, tempW - padding, tempH - padding, paint);
        canvas.save();
        canvas.restore();
    }
    public static void addBorder(Bitmap bitmap) {
        Canvas canvas = new Canvas(bitmap);
        int padding = 2;
        int tempW = bitmap.getWidth();
        int tempH = bitmap.getHeight();
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(Color.BLACK);
        paint.setTextSize(20);
        paint.setStrokeWidth(2);
        canvas.drawLine(padding, padding, tempW - padding, padding, paint);
        canvas.drawLine(padding, tempH - padding, tempW - padding, tempH - padding, paint);

        canvas.drawLine(padding, padding, padding, tempH - padding, paint);
        canvas.drawLine(tempW - padding, padding, tempW - padding, tempH - padding, paint);
        canvas.save();
        canvas.restore();
    }

    /**
     * 缩放图片
     *
     * @param maxWidth 最大宽
     * @return 缩放后的图片
     */
    public static Bitmap scalingBitmap(Bitmap image, int maxWidth) {
        try {
            final int width = image.getWidth();
            final int height = image.getHeight();
            // 精确缩放
            if (maxWidth <= 0 || width <= maxWidth) {
                return image;
            }
            final float scale = maxWidth / (float) width;
            Matrix matrix = new Matrix();
            matrix.postScale(scale, scale);
            Bitmap resizeImage = Bitmap.createBitmap(image, 0, 0, width, height, matrix, true);
            return resizeImage;
        } catch (OutOfMemoryError e) {
            return null;
        }
    }


    public List<byte[]> bitmapToDataList(Bitmap image, int parting) {
        // 宽命令
        String widthHexString = Integer.toHexString(width % 8 == 0 ? width / 8 : (width / 8 + 1));
        if (widthHexString.length() > 2) {
            // 超过2040像素才会到达这里
            return null;
        } else if (widthHexString.length() == 1) {
            widthHexString = "0" + widthHexString;
        }
        widthHexString += "00";

        // 每行字节数(除以8，不足补0)
        String zeroStr = "";
        int zeroCount = width % 8;
        if (zeroCount > 0) {
            for (int i = 0; i < (8 - zeroCount); i++) {
                zeroStr += "0";
            }
        }
        ArrayList<String> commandList = new ArrayList<>();
        // 高度每parting像素进行一次分割
        int time = height % parting == 0 ? height / parting : (height / parting + 1);// 循环打印次数
        for (int t = 0; t < time; t++) {
            int partHeight = t == time - 1 ? height % parting : parting;// 分段高度

            // 高命令
            String heightHexString = Integer.toHexString(partHeight);
            if (heightHexString.length() > 2) {
                // 超过255像素才会到达这里
                return null;
            } else if (heightHexString.length() == 1) {
                heightHexString = "0" + heightHexString;
            }
            heightHexString += "00";

            // 宽高指令
            String commandHexString = "1D763000";
            commandList.add(commandHexString + widthHexString + heightHexString);

            ArrayList<String> list = new ArrayList<>(); //binaryString list
            StringBuilder sb = new StringBuilder();
            // 像素二值化，非黑即白
            for (int i = 0; i < partHeight; i++) {
                sb.delete(0, sb.length());
                for (int j = 0; j < width; j++) {
                    // 实际在图片中的高度
                    int startHeight = t * parting + i;
                    //得到当前像素的值
                    int color = image.getPixel(j, startHeight);
                    int red, green, blue;
                    if (image.hasAlpha()) {
                        //得到alpha通道的值
                        int alpha = Color.alpha(color);
                        //得到图像的像素RGB的值
                        red = Color.red(color);
                        green = Color.green(color);
                        blue = Color.blue(color);
                        final float offset = alpha / 255.0f;
                        // 根据透明度将白色与原色叠加
                        red = 0xFF + (int) Math.ceil((red - 0xFF) * offset);
                        green = 0xFF + (int) Math.ceil((green - 0xFF) * offset);
                        blue = 0xFF + (int) Math.ceil((blue - 0xFF) * offset);
                    } else {
                        //得到图像的像素RGB的值
                        red = Color.red(color);
                        green = Color.green(color);
                        blue = Color.blue(color);
                    }
                    // 接近白色改为白色。其余黑色
                    if (red > 160 && green > 160 && blue > 160)
                        sb.append("0");
                    else
                        sb.append("1");
                }
                // 每一行结束时，补充剩余的0
                if (zeroCount > 0) {
                    sb.append(zeroStr);
                }
                list.add(sb.toString());
            }
            // binaryStr每8位调用一次转换方法，再拼合
            ArrayList<String> bmpHexList = new ArrayList<>();
            for (String binaryStr : list) {
                sb.delete(0, sb.length());
                for (int i = 0; i < binaryStr.length(); i += 8) {
                    String str = binaryStr.substring(i, i + 8);
                    // 2进制转成16进制
                    String hexString = binaryStrToHexString(str);
                    sb.append(hexString);
                }
                bmpHexList.add(sb.toString());
            }

            // 数据指令
            commandList.addAll(bmpHexList);
        }
        ArrayList<byte[]> data = new ArrayList<>();
        for (String hexStr : commandList) {
            data.add(hexStringToBytes(hexStr));
        }
        return data;
    }

    /**
     * 解码图片
     *
     * @param image   图片
     * @param parting 高度分割值
     * @return 数据流
     */
    public static ArrayList<byte[]> decodeBitmapToDataList(Bitmap image, int parting) {
        int maxHeight = 76 * 380;
        int width1 = image.getWidth();
        if (parting <= 0 || parting > 255)
            parting = 255;
        if (image == null)
            return null;
        final int width = width1;
        final int height = image.getHeight();

        int count0 = 0;
        int count1 = 0;
        if (width <= 0 || height <= 0)
            return null;
        if (width > 2040) {
            // 8位9针，宽度限制2040像素（但一般纸张都没法打印那么宽，但并不影响打印）
            final float scale = 2040 / (float) width;
            Matrix matrix = new Matrix();
            matrix.postScale(scale, scale);
            Bitmap resizeImage;
            try {
                resizeImage = Bitmap.createBitmap(image, 0, 0, width, height, matrix, true);
            } catch (OutOfMemoryError e) {
                return null;
            }
            ArrayList<byte[]> data = decodeBitmapToDataList(resizeImage, parting);
            resizeImage.recycle();
            return data;
        }
        // 宽命令
        String widthHexString = Integer.toHexString(width % 8 == 0 ? width / 8 : (width / 8 + 1));
        if (widthHexString.length() > 2) {
            // 超过2040像素才会到达这里
            return null;
        } else if (widthHexString.length() == 1) {
            widthHexString = "0" + widthHexString;
        }
        widthHexString += "00";

        // 每行字节数(除以8，不足补0)
        String zeroStr = "";
        int zeroCount = width % 8;
        if (zeroCount > 0) {
            for (int i = 0; i < (8 - zeroCount); i++) {
                zeroStr += "0";
            }
        }
        ArrayList<String> commandList = new ArrayList<>();
        // 高度每parting像素进行一次分割
        int time = height % parting == 0 ? height / parting : (height / parting + 1);// 循环打印次数
        int totalHeight = height;

        for (int t = 0; t < time; t++) {
            int partHeight = t == time - 1 ? height % parting : parting;// 分段高度

            // 高命令
            String heightHexString = Integer.toHexString(partHeight);
            if (heightHexString.length() > 2) {
                // 超过255像素才会到达这里
                return null;
            } else if (heightHexString.length() == 1) {
                heightHexString = "0" + heightHexString;
            }
            heightHexString += "00";

            // 宽高指令
            String commandHexString = "1D763000";
            commandList.add(commandHexString + widthHexString + heightHexString);

            ArrayList<String> list = new ArrayList<>(); //binaryString list
            StringBuilder sb = new StringBuilder();
            // 像素二值化，非黑即白
            for (int i = 0; i < partHeight; i++) {
                sb.delete(0, sb.length());
                for (int j = 0; j < width; j++) {
                    // 实际在图片中的高度
                    int startHeight = t * parting + i;
                    //得到当前像素的值
                    int color = image.getPixel(j, startHeight);
                    int red, green, blue;
                    if (image.hasAlpha()) {
                        //得到alpha通道的值
                        int alpha = Color.alpha(color);
                        //得到图像的像素RGB的值
                        red = Color.red(color);
                        green = Color.green(color);
                        blue = Color.blue(color);
                        final float offset = alpha / 255.0f;
                        // 根据透明度将白色与原色叠加
                        red = 0xFF + (int) Math.ceil((red - 0xFF) * offset);
                        green = 0xFF + (int) Math.ceil((green - 0xFF) * offset);
                        blue = 0xFF + (int) Math.ceil((blue - 0xFF) * offset);
                    } else {
                        //得到图像的像素RGB的值
                        red = Color.red(color);
                        green = Color.green(color);
                        blue = Color.blue(color);
                    }
                    // 接近白色改为白色。其余黑色
                    if (red > 160 && green > 160 && blue > 160) {
                        count0++;
                        sb.append("0");
                    } else {
                        count1++;
                        sb.append("1");
                    }
                }
                // 每一行结束时，补充剩余的0
                if (zeroCount > 0) {
                    sb.append(zeroStr);
                }
                list.add(sb.toString());
            }
            // binaryStr每8位调用一次转换方法，再拼合
            ArrayList<String> bmpHexList = new ArrayList<>();
            for (String binaryStr : list) {
                sb.delete(0, sb.length());
                for (int i = 0; i < binaryStr.length(); i += 8) {
                    String str = binaryStr.substring(i, i + 8);
                    // 2进制转成16进制
                    String hexString = binaryStrToHexString(str);
                    sb.append(hexString);
                }
                bmpHexList.add(sb.toString());
            }

            // 数据指令
            commandList.addAll(bmpHexList);
        }
        Log.d("zjy", PrinterUtils2.class.getName()+"->decodeBitmapToDataList():  ==c0" + count0 + "\t" + count1);
        int finalW = (zeroCount + width) * height;
        int extraW = zeroCount * height + count0 + count1;
        Log.d("zjy",
                PrinterUtils2.class.getName() + "->decodeBitmapToDataList(): totoal==" + finalW + "\t" + extraW);

        ArrayList<byte[]> data = new ArrayList<>();
        for (String hexStr : commandList) {
            data.add(hexStringToBytes(hexStr));
        }
        return data;
    }

    /**
     * 16进制串转byte数组
     *
     * @param hexString 16进制串
     * @return byte数组
     */
    public static byte[] hexStringToBytes(String hexString) {
        if (hexString == null || hexString.equals("")) {
            return null;
        }
        hexString = hexString.toUpperCase();
        int length = hexString.length() / 2;
        char[] hexChars = hexString.toCharArray();
        byte[] d = new byte[length];
        for (int i = 0; i < length; i++) {
            int pos = i * 2;
            d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
        }
        return d;
    }

    private static String hexStr = "0123456789ABCDEF";
    private static String[] binaryArray = {"0000", "0001", "0010", "0011",
            "0100", "0101", "0110", "0111", "1000", "1001", "1010", "1011",
            "1100", "1101", "1110", "1111"};

    /**
     * 16进制char 转 byte
     *
     * @param c char
     * @return byte
     */
    private static byte charToByte(char c) {
        return (byte) hexStr.indexOf(c);
    }

    /**
     * 2进制转成16进制
     *
     * @param binaryStr 2进制串
     * @return 16进制串
     */
    @SuppressWarnings("unused")
    public static String binaryStrToHexString(String binaryStr) {
        String hex = "";
        String f4 = binaryStr.substring(0, 4);
        String b4 = binaryStr.substring(4, 8);
        for (int i = 0; i < binaryArray.length; i++) {
            if (f4.equals(binaryArray[i]))
                hex += hexStr.substring(i, i + 1);
        }
        for (int i = 0; i < binaryArray.length; i++) {
            if (b4.equals(binaryArray[i]))
                hex += hexStr.substring(i, i + 1);
        }
        return hex;
    }

}
