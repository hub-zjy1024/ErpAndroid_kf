package com.b1b.js.erpandroid_kf.myview;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by 张建宇 on 2019/7/13.
 */
public class ScanRectView extends View {
    public int parentWidth;
    public int parentHeight;

    private Paint mPaint;
    private Paint cornerPaint;
    int borderStroke = 10;
    private Paint borderRect;
    private int mColor = Color.parseColor("#00BCD4");
    /**
     * 扫描线宽度
     */
    private float scanLineWitdh = 5;
    /**
     * 扫描线颜色
     */
    private int scanLineColor = Color.parseColor("#f44336");
    /**
     * 方框线条宽度
     */
    int borderRectStroke = 2;
    /**
     * 四角的长度
     */
    int cornerlength = 50;

    private int linePos = 0;
    private boolean isScan = true;

    private int realHeight;
    private int realWidth;
    /**
     * 遮罩颜色，0.4的阴影
     */
    private int layerColor;


    /**
     * 遮罩框位置
     */
    Rect center;

    /**
     * 绘制四角时使用的位置
     */
    Rect drawCenter;

    /**
     * Simple constructor to use when creating a view from code.
     *
     * @param context The Context the view is running in, through which it can
     *                access the current theme, resources, etc.
     */
    public ScanRectView(Context context, int width, int height) {
        this(context);
        this.parentWidth = width;
        this.parentHeight = height;

    }


    /**
     * Simple constructor to use when creating a view from code.
     *
     * @param context The Context the view is running in, through which it can
     *                access the current theme, resources, etc.
     */
    public ScanRectView(Context context) {
        this(context, null);
    }

    /**
     * Constructor that is called when inflating a view from XML. This is called
     * when a view is being constructed from an XML file, supplying attributes
     * that were specified in the XML file. This version uses a default style of
     * 0, so the only attribute values applied are those in the Context's Theme
     * and the given AttributeSet.
     * <p>
     * <p>
     * The method onFinishInflate() will be called after all children have been
     * added.
     *
     * @param context The Context the view is running in, through which it can
     *                access the current theme, resources, etc.
     * @param attrs   The attributes of the XML tag that is inflating the view.
     */
    public ScanRectView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    /**
     * Perform inflation from XML and apply a class-specific base style from a
     * theme attribute. This constructor of View allows subclasses to use their
     * own base style when they are inflating. For example, a Button class's
     * constructor would call this version of the super class constructor and
     * supply <code>R.attr.buttonStyle</code> for <var>defStyleAttr</var>; this
     * allows the theme's button style to modify all of the base view attributes
     * (in particular its background) as well as the Button class's attributes.
     *
     * @param context      The Context the view is running in, through which it can
     *                     access the current theme, resources, etc.
     * @param attrs        The attributes of the XML tag that is inflating the view.
     * @param defStyleAttr An attribute in the current theme that contains a
     *                     reference to a style resource that supplies default values for
     *                     the view. Can be 0 to not look for defaults.
     */
    public ScanRectView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mPaint = new Paint();
        layerColor = Color.argb((int) (255 * 0.4), 0, 0, 0);
        mPaint.setColor(layerColor);
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);

        cornerPaint = new Paint();
        cornerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        cornerPaint.setColor(mColor);
        cornerPaint.setStyle(Paint.Style.STROKE);
        cornerPaint.setStrokeWidth(borderStroke);

        borderRect = new Paint();
        borderRect.setAntiAlias(true);
        borderRect.setStrokeWidth(borderRectStroke);
        borderRect.setColor(mColor);
    }




    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (parentWidth == 0 || parentHeight == 0) {
            parentWidth = MeasureSpec.getSize(widthMeasureSpec);
            parentHeight = MeasureSpec.getSize(heightMeasureSpec);
        }
    /*   如果阴影遮罩不完全，请使用测量的自身宽高
       int selfWidth = getMeasuredWidth();
        int selfHeight = getMeasuredHeight();
       parentWidth= selfWidth;
        parentHeight=selfHeight;*/
        realWidth = parentWidth * 5 / 8;
        //        realHeight = parentHeight * 5 / 8;
        realHeight = realWidth;
        int rectHeight = realHeight;
        int top = (int) (parentHeight * 0.1);
        int bottom = top + rectHeight;
        int left_right = (parentWidth - realWidth) / 2;
        int right = left_right + realWidth;
        if (center == null) {
            center = new Rect(left_right, top, right, bottom);
        } else {
            center.left = left_right;
            center.top = top;
            center.right = right;
            center.bottom = bottom;
        }
        drawCenter = getInnerCorner(center, borderStroke);
    }

    private Rect getOuterCorner(Rect center, int borderStroke) {
        int offset = borderStroke / 2;
        Rect tempRect = new Rect(center.left - offset, center.top - offset,
                center.right + offset,
                center.bottom + offset);
        return tempRect;
    }

    private Rect getInnerCorner(Rect center, int borderStroke) {
        int offset = borderStroke / 2;
        Rect tempRect = new Rect(center.left + offset, center.top + offset,
                center.right - offset,
                center.bottom - offset);
        return tempRect;
    }

    @Override
    protected void onDraw(Canvas canvas) {

        int rWidth = center.width();
        int rHeight = center.height();
        int top_bottom = center.top;
        int left_right = center.left;
        int right = center.right;

        int bottom = center.bottom;


        mPaint.setColor(layerColor);
        //上半矩形
        canvas.drawRect(0, 0, parentWidth, top_bottom, mPaint);
        //        //左侧矩形
        canvas.drawRect(0, top_bottom, left_right, parentHeight, mPaint);
        //        //右侧矩形
        canvas.drawRect(right, top_bottom, parentWidth, parentHeight, mPaint);

        //底部矩形
        canvas.drawRect(left_right, bottom, right, parentHeight, mPaint);

        int lineY = linePos + top_bottom;

        mPaint.setStrokeWidth(10);
        mPaint.setColor(Color.parseColor("#169fe6"));

        int offset = borderStroke / 2;
        Rect newCenter = drawCenter;
        //四角
        //横向
        canvas.drawLine(newCenter.left - offset, newCenter.top, newCenter.left + cornerlength, newCenter.top,
                cornerPaint);
        canvas.drawLine(newCenter.right + offset, newCenter.top, newCenter.right - cornerlength,
                newCenter.top,
                cornerPaint);
        canvas.drawLine(newCenter.left - offset, newCenter.bottom, newCenter.left + cornerlength,
                newCenter.bottom,
                cornerPaint);
        canvas.drawLine(newCenter.right + offset, newCenter.bottom, newCenter.right - cornerlength,
                newCenter.bottom,
                cornerPaint);
        //纵向
        canvas.drawLine(newCenter.left, newCenter.top, newCenter.left, newCenter.top + cornerlength,
                cornerPaint);
        canvas.drawLine(newCenter.left, newCenter.bottom, newCenter.left, newCenter.bottom - cornerlength,
                cornerPaint);
        canvas.drawLine(newCenter.right, newCenter.top, newCenter.right, newCenter.top + cornerlength,
                cornerPaint);
        canvas.drawLine(newCenter.right, newCenter.bottom, newCenter.right, newCenter.bottom - cornerlength
                , cornerPaint);

        //扫码框四边
        canvas.drawLine(center.left, center.top, center.left, center.bottom
                , borderRect);
        canvas.drawLine(center.right, center.top, center.right, center.bottom
                , borderRect);
        canvas.drawLine(center.left, center.top, center.right, center.top
                , borderRect);
        canvas.drawLine(center.left, center.bottom, center.right, center.bottom
                , borderRect);

        mPaint.setStrokeWidth(scanLineWitdh);

        mPaint.setColor(scanLineColor);
        if (linePos >= realHeight) {
            linePos = 0;
            resetAnim();
        }
        canvas.drawLine(left_right, lineY, right, lineY, mPaint);
        //        linePos += 3;
        //        invalidate();
        if (linePos > 20) {
            int TemplineY = lineY - 10;
            mPaint.setStrokeWidth(3);
            int horLines = 5;

            int vetLineMargin = 10;
            int vetLines = rWidth / vetLineMargin;
            //            for (int i = 0; i < horLines; i++) {
            //                if (TemplineY >= top_bottom) {
            //                    canvas.drawLine(left_right, TemplineY, rWidth + left_right, TemplineY,
            //                    mPaint);
            //                    for(int k=0;k<vetLines;k++) {
            //                        int vlX = vetLines * vetLineMargin;
            //                        int realX = left_right + vlX;
            //                        canvas.drawLine(realX, TemplineY, realX, TemplineY - 10, mPaint);
            //                    }
            //                }
            //                TemplineY -= 10;
            //            }
            //            mPaint.setStrokeWidth(2);
            //            for(int k=0;k<vetLines;k++) {
            //                int vlX = vetLines * vetLineMargin;
            //                int realX = left_right + vlX;
            //                canvas.drawLine(realX, TemplineY, realX, TemplineY - 10, mPaint);
            //            }
        }
        if (isScan) {
            startScan();
        }
    }

    Rect getFramingRectInView() {
        return center;
    }

    public Rect getFramingRectInPreview(int preWidth, int preHeight) {
        //按比例截取预览部分

        int cropW = realWidth * preWidth / parentWidth;
        int cropH = realHeight * preHeight / parentHeight;
        int top = preHeight * center.top / parentHeight;
        int left_right = preWidth * center.left / parentWidth;
        return new Rect(left_right, top, left_right + cropW, top + cropH);
        //        return preViewRect;
    }

    private ObjectAnimator animator;

    public void setLinePos(int linePos) {
        this.linePos = linePos;
        postInvalidate();
    }

    public int getLinePos() {
        return linePos;
    }


    public void stopScan() {
        if (animator != null) {
            animator.cancel();
            animator.removeAllListeners();
            animator = null;
            setLinePos(0);
        }
        isScan = false;
    }

    public void resetAnim() {
        animator = ObjectAnimator.ofInt(this, "linePos", 0, realHeight);
        animator.setDuration(2000);
        animator.start();
    }

    public void startScan() {
        isScan = true;
        if (animator == null) {
            animator = ObjectAnimator.ofInt(this, "linePos", 0, realHeight);
            animator.setDuration(2000);
            animator.start();
        }
    }
}
