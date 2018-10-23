package com.b1b.js.erpandroid_kf.myview;

/**
 * Created by 张建宇 on 2019/7/24.
 * <br/>
 * 预览数据处理工具类
 * <br/>
 * android手机从摄像头采集的预览数据一般都是NV21，
 * 存储顺序是先存Y，再VU交替存储， NV21存储顺序是先存Y值，
 * 再VU交替存储：YYYYVUVUVU，以 4X4图片为例子，占用内存为 4X
 * 4X 3/2=24个字节
 * <br/>
 * <br/>
 * YUV和RGB转换<br/>
 * <br/>
 * Y      =  (0.257 * R) + (0.504 * G) + (0.098 * B) + 16
 * <br/>
 * Cr = V =  (0.439 * R) - (0.368 * G) - (0.071 * B) + 128
 * <br/>
 * Cb = U = -(0.148 * R) - (0.291 * G) + (0.439 * B) + 128
 * <br/>
 *
 * B = 1.164(Y - 16) + 2.018(U - 128)
 * <br/>
 * G = 1.164(Y - 16) - 0.813(V - 128) - 0.391(U - 128)
 * <br/>
 * R = 1.164(Y - 16) + 1.596(V - 128)
 */
public class CameraDataConverter {
    /**
     * YV12格式与YU12基本相同，首先是所有Y值，然后是所有V值，最后是所有U值
     *
     * @param input
     * @param width
     * @param height
     */
    public byte[] YV12toNV21(final byte[] input, final int width, final int height) {
        final byte[] output = new byte[input.length];
        long startMs = System.currentTimeMillis();
        final int frameSize = width * height;
        final int qFrameSize = frameSize / 4;
        final int tempFrameSize = frameSize + qFrameSize;

        System.arraycopy(input, 0, output, 0, frameSize); // Y

        for (int i = 0; i < qFrameSize; i++) {
            output[frameSize + i * 2] = input[frameSize + i]; // Cb (U)
            output[frameSize + i * 2 + 1] = input[tempFrameSize + i]; // Cr (V)
        }
        return output;
    }

    /**
     * YU12
     * 在android平台下也叫作I420格式，首先是所有Y值，然后是所有U值，最后是所有V值。
     *
     * @param input
     * @param width
     * @param height
     */
    //I420 To NV21
    private byte[] I420ToNV21(final byte[] input, final int width, final int height) {
        //long startMs = System.currentTimeMillis();
        final byte[] output = new byte[input.length];
        final int frameSize = width * height;
        final int qFrameSize = frameSize / 4;
        final int tempFrameSize = frameSize * 5 / 4;

        System.arraycopy(input, 0, output, 0, frameSize); // Y
        for (int i = 0; i < qFrameSize; i++) {
            int mpoi = i * 2;
            output[frameSize + mpoi] = input[tempFrameSize + i]; // Cb (U)
            output[frameSize + mpoi + 1] = input[frameSize + i]; // Cr (V)
        }
        return output;
    }


    /**
     * NV21存储顺序，先存Y，然后VU交替
     * NV12与NV21类似，也属于YUV420SP格式，NV12存储顺序是先存Y值，再UV交替存储
     *
     * @return
     */
    byte[] NV12toNv21(byte[] data, int w, int h) {
        int ySize = w * h;
        byte[] out = new byte[ySize * 3 / 2];
        int uSize = ySize / 4;
        int vSize = uSize;
        System.arraycopy(data, 0, out, 0, ySize); // Y
        //u,v
        for (int i = ySize; i < out.length - 1; i++) {
            out[i] = data[i + 1];
            out[i + 1] = data[i];
        }
        return out;
    }

    byte[] rotateYUV420Degree180(byte[] data, int w, int h) {
        int imgSize = w * h;
        int len = imgSize * 3 / 2;//yuv数组长度是图片尺寸的1.5倍
        byte[] yuv = new byte[len];
        int i = 0;
        int count = 0;
        //y
        for (i = imgSize - 1; i >= 0; i--) {
            yuv[count++] = data[i];
        }
        //u,v
        for (i = len - 1; i >= imgSize; i -= 2) {
            yuv[count++] = data[i - 1];
            yuv[count++] = data[i];
        }
        return yuv;
    }

    byte[] rotateYUV420Degree90(byte[] data, int imageWidth, int imageHeight) {
        byte[] yuv = new byte[imageWidth * imageHeight * 3 / 2];
        // Rotate the Y luma
        int i = 0;
        for (int x = 0; x < imageWidth; x++) {
            for (int y = imageHeight - 1; y >= 0; y--) {
                yuv[i] = data[y * imageWidth + x];
                i++;
            }
        }
        // Rotate the U and V color components
        i = imageWidth * imageHeight * 3 / 2 - 1;
        for (int x = imageWidth - 1; x > 0; x = x - 2) {
            for (int y = 0; y < imageHeight / 2; y++) {
                yuv[i] = data[(imageWidth * imageHeight) + (y * imageWidth) + x];
                i--;
                yuv[i] = data[(imageWidth * imageHeight) + (y * imageWidth) + (x - 1)];
                i--;
            }
        }
        return yuv;
    }

    byte[] YUV420spRotate270(byte[] src, int width, int height) {
        int count = 0;
        int uvHeight = height >> 1;
        int imgSize = width * height;
        byte[] des = new byte[imgSize * 3 >> 1];
        //copy y
        for (int j = width - 1; j >= 0; j--) {
            for (int i = 0; i < height; i++) {
                des[count++] = src[width * i + j];
            }
        }
        //u,v
        for (int j = width - 1; j > 0; j -= 2) {
            for (int i = 0; i < uvHeight; i++) {
                des[count++] = src[imgSize + width * i + j - 1];
                des[count++] = src[imgSize + width * i + j];
            }
        }
        return des;
    }

    /**
     * NV21裁剪  算法效率 3ms
     *
     * @param src    源数据
     * @param width  源宽
     * @param height 源高
     * @param left   顶点坐标
     * @param top    顶点坐标
     * @param clip_w 裁剪后的宽
     * @param clip_h 裁剪后的高
     * @return 裁剪后的数据
     */
    byte[] clipNV21(byte[] src, int width, int height, int left, int top, int clip_w, int clip_h) {
        if (left > width || top > height) {
            return null;
        }
        //取偶
        int x = left / 2 * 2, y = top / 2 * 2;
        int w = clip_w / 2 * 2, h = clip_h / 2 * 2;
        int y_unit = w * h;
        int uv = y_unit / 2;
        byte[] nData = new byte[y_unit + uv];
        int uv_index_dst = w * h - y / 2 * w;
        int uv_index_src = width * height + x;
        int srcPos0 = y * width;
        int destPos0 = 0;
        int uvSrcPos0 = uv_index_src;
        int uvDestPos0 = uv_index_dst;
        for (int i = y; i < y + h; i++) {
            System.arraycopy(src, srcPos0 + x, nData, destPos0, w);//y内存块复制
            srcPos0 += width;
            destPos0 += w;
            if ((i & 1) == 0) {
                System.arraycopy(src, uvSrcPos0, nData, uvDestPos0, w);//uv内存块复制
                uvSrcPos0 += width;
                uvDestPos0 += w;
            }
        }
        return nData;
    }
}
