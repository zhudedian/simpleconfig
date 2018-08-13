package com.edong.simpleconfig.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Region;
import android.os.Build;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import com.edong.simpleconfig.R;


/**
 * Created by yd on 2018/5/18.
 */

public class WifiSignalView extends View {

    private Paint mPaint;
    private float radius1, radius2;
    private float paintWidth = 4f;
    private float width,height;
    private float height2;
    private float paddingLeft,paddingRight,paddingTop,paddingBottom;

    private int color = 0;
    private boolean isLocked = true;
    private int level = 100;


    public WifiSignalView(Context context) {
        this(context, null);
    }

    public WifiSignalView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WifiSignalView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(Color.WHITE);
        mPaint.setAlpha(255);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(paintWidth);
        mPaint.setStrokeCap(Paint.Cap.ROUND);//没有
        mPaint.setStrokeJoin(Paint.Join.BEVEL);//直线


    }

    public void setState(int level,boolean isLocked){
        this.level = level;
        this.isLocked = isLocked;
        postInvalidate();
    }
    private void init() {
        paintWidth = getWidth()*0.05f;
        mPaint.setStrokeWidth(paintWidth);
        paddingLeft = getPaddingLeft()+paintWidth;
        paddingRight = getPaddingRight()+paintWidth;
        paddingTop = getPaddingTop()+paintWidth;
        paddingBottom = getPaddingBottom()+paintWidth;

        width = getWidth()-paddingLeft-paddingRight;
        height = getHeight()-paddingTop-paddingBottom;

        height2 = width*0.8f;


    }
    public void setColor(int color){
        this.color = color;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        init();
        canvas.translate(paddingLeft,paddingTop);
        mPaint.setStyle(Paint.Style.STROKE);
        float angle = (float) aSin(width/2/height2);
        if (level>=90){
            mPaint.setColor(color==0? Color.WHITE:color);
        }else {
            mPaint.setColor(getResources().getColor(R.color.wifi_signal_gray));
        }
        RectF oval = new RectF(width*0.5f-height2,(width-height2)*0.5f,width*0.5f+height2,width*0.5f+height2*1.5f);
        canvas.drawArc(oval,-90-angle,angle*2,false,mPaint);

        if (level>=80){
            mPaint.setColor(color==0? Color.WHITE:color);
        }else {
            mPaint.setColor(getResources().getColor(R.color.wifi_signal_gray));
        }
        oval = new RectF(width*0.5f-height2*0.66f,(width-height2)*0.5f+height2*0.33f,width*0.5f+height2*0.66f,width*0.5f+height2*1.16f);
        canvas.drawArc(oval,-90-angle,isLocked?angle*1.55f:angle*2,false,mPaint);

        if (level>=70){
            mPaint.setColor(color==0? Color.WHITE:color);
        }else {
            mPaint.setColor(getResources().getColor(R.color.wifi_signal_gray));
        }
        oval = new RectF(width*0.5f-height2*0.34f,(width-height2)*0.5f+height2*0.66f,width*0.5f+height2*0.33f,width*0.5f+height2*0.83f);
        canvas.drawArc(oval,-90-angle,isLocked?angle*1.45f:angle*2,false,mPaint);

        if (level>=60){
            mPaint.setColor(color==0? Color.WHITE:color);
        }else {
            mPaint.setColor(getResources().getColor(R.color.wifi_signal_gray));
        }
        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        canvas.drawCircle(width/2,width*0.5f+height2*0.5f,paintWidth,mPaint);

        if(isLocked){
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setColor(color==0? Color.WHITE:color);
            Path path1= new Path();
            Path path2= new Path();
            path1.addCircle(width*0.83f,width*0.56f,width*0.08f, Path.Direction.CCW);
            oval = new RectF(width*0.68f,width*0.58f,width*0.98f,width*0.5f+height2*0.45f);
            path2.addRoundRect(oval,width*0.1f,width*0.1f, Path.Direction.CCW);
            canvas.drawRect(oval,mPaint);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                path1.op(path2, Path.Op.DIFFERENCE);
            }else {
                canvas.clipPath(path2, Region.Op.DIFFERENCE);
            }
            canvas.drawPath(path1,mPaint);

        }
    }

    private double aSin(double sin){
        return Math.asin(sin)*180/ Math.PI;
    }
}
