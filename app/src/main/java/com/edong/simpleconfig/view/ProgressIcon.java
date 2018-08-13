package com.edong.simpleconfig.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;


import com.edong.simpleconfig.R;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by yd on 2018/5/16.
 */

public class ProgressIcon extends View {
    private Paint mPaint;
    private boolean isStartedAnimate = false;
    private float radius1, radius2,radius3;
    private float radius;
    private float paintWidth = 3.5f;
    private float width,height;
    private float paddingLeft,paddingRight,paddingTop,paddingBottom;
    private ValueAnimator animator;
    private int type = FRESHING;
    private boolean isShowing = true;

    public static final int FRESHING = 1;
    public static final int FRESHED = 2;
    @IntDef({FRESHING,FRESHED})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Type{
    }


    public ProgressIcon(Context context) {
        this(context, null);
    }

    public ProgressIcon(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ProgressIcon(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(getResources().getColor(R.color.freshview));
        mPaint.setAlpha(255);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(paintWidth);
        mPaint.setStrokeCap(Paint.Cap.ROUND);//没有
        mPaint.setStrokeJoin(Paint.Join.BEVEL);//直线
        startAnimator();
    }


    private void init() {
        paddingLeft = getPaddingLeft()+paintWidth;
        paddingRight = getPaddingRight()+paintWidth;
        paddingTop = getPaddingTop()+paintWidth;
        paddingBottom = getPaddingBottom()+paintWidth;

        width = getWidth()-paddingLeft-paddingRight;
        height = getHeight()-paddingTop-paddingBottom;

        radius = width*0.1f;



    }
    public void show(@Type int type){
        this.type = type;
        isShowing = true;
        setVisibility(VISIBLE);
        if (type == FRESHED){
            if (animator!=null){
                animator.cancel();
                animator = null;
            }
        }else if (type == FRESHING){
            isStartedAnimate = false;
        }
        postInvalidate();
    }
    public void dismiss(){
        isShowing = false;
        if (animator!=null){
            animator.cancel();
            animator = null;
        }
        setVisibility(GONE);
    }
    public boolean isShowing(){
        return isShowing;
    }
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        init();
        canvas.translate(paddingLeft, paddingTop);
        if (type == FRESHED) {
            drawFreshIcon(canvas);
        }else if (type == FRESHING){
            drawFreshing(canvas);
        }


    }

    private void drawFreshing(Canvas canvas){
        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        if (radius1!=0) {
            canvas.drawCircle(radius, height * 0.5f, radius1, mPaint);
        }
        if (radius2!=0) {
            canvas.drawCircle(radius * 5, height * 0.5f, radius2, mPaint);
        }
        if (radius3!=0) {
            canvas.drawCircle(radius * 9, height * 0.5f, radius3, mPaint);
        }

    }
    private void drawFreshIcon(Canvas canvas){
        mPaint.setStyle(Paint.Style.STROKE);
        RectF oval = new RectF( 0, 0, width*0.9f, height*0.9f);
        canvas.drawArc(oval,30,330,false,mPaint);
        Path path = new Path();
        path.moveTo(width*0.8f,height*0.35f);
        path.lineTo(width*0.9f,height*0.45f);
        path.lineTo(width,height*0.35f);
        canvas.drawPath(path,mPaint);
    }
    private void startAnimator() {
        if (isStartedAnimate){
            return;
        }
        isStartedAnimate = true;
        animator = ValueAnimator.ofFloat(0,4);
        animator.setDuration(2000);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setInterpolator(new LinearInterpolator());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                /**
                 * 通过这样一个监听事件，我们就可以获取
                 * 到ValueAnimator每一步所产生的值。
                 *
                 * 通过调用getAnimatedValue()获取到每个时间因子所产生的Value。
                 * */
                float value =  (float)animation.getAnimatedValue();
                calculateRadiu(value);
                postInvalidate();
            }
        });
        animator.start();
    }

    private void calculateRadiu(float value){
        if (value<=0.5f){
            radius1 = radius*value;
            radius2 = 0;
            radius3 = 0;
        }else if (value<=1.0f){
            radius1 = radius*value;
            radius2 = radius*(value-0.5f);
            radius3 = 0;
        }else if (value<=1.5f){
            radius1 = radius*1;
            radius2 = radius*(value-0.5f);
            radius3 = radius*(value-1.0f);
        }else if (value<=2.0f){
            radius1 = radius*1;
            radius2 = radius*1;
            radius3 = radius*(value-1.0f);
        }else if (value<=2.5f){
            radius1 = radius*(1-(value-2));
            radius2 = radius*1;
            radius3 = radius*1;
        }else if (value<=3.0f){
            radius1 = radius*(1-(value-2));
            radius2 = radius*(1-(value-2.5f));
            radius3 = radius*1;
        }else if (value<=3.5f){
            radius1 = 0;
            radius2 = radius*(1-(value-2.5f));
            radius3 = radius*(1-(value-3));
        }else if (value<=4.0f){
            radius1 = 0;
            radius2 = 0;
            radius3 = radius*(1-(value-3));
        }
    }
}
