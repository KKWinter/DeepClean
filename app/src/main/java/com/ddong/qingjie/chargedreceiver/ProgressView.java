package com.ddong.qingjie.chargedreceiver;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;

import com.ddong.qingjie.R;
import com.ddong.qingjie.utils.Utils;


public class ProgressView extends View {

    private Context context;

    /**
     * 自定义view的容器宽高
     */
    private int measureWidth;       //1080
    private int measureHeigth;      //450

    /**
     * 画笔
     */
    private Paint rotatePaint;
    private Paint ringPaint;
    private Paint circlePaint;
    private Paint perPaint;
    private Paint textPaint;

    /**
     * 内外圆弧的半径
     */
    private float rotateRadius;   //px
    private float rotateStroke;
    private float ringRadius;
    private float ringStroke;
    private float ringPadding;
    private float circleRadius;

    /**
     * 当前电量和文字提示
     */
    private int mProgress = 0;
    private String desc;


    public ProgressView(Context context) {
        super(context);
        initView(context);
    }

    public ProgressView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public ProgressView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(Context context){
        this.context = context;



        rotateRadius = dip2px(115);//345f;   //px
        rotateStroke = dip2px(3);  //15f;
        ringStroke = dip2px(2);     //12f;
        ringPadding = dip2px(6);   //18f;
        ringRadius = rotateRadius - ringPadding - rotateStroke/2 - ringStroke/2;//315f;
        circleRadius = ringRadius - ringStroke/2;

        /**
         * 外旋转进度画笔
         */
        rotatePaint = new Paint();

        Shader shader = new SweepGradient(rotateRadius,rotateRadius,
                new int[]{Utils.getColor(R.color.charge_bg_blue),Utils.getColor(R.color.white),Utils.getColor(R.color.white)},null);
        rotatePaint.setShader(shader);
        rotatePaint.setAntiAlias(true);
        rotatePaint.setStyle(Style.STROKE);
        rotatePaint.setStrokeWidth(rotateStroke);

        /**
         * 内圆环画笔
         */
        ringPaint = new Paint();
        ringPaint.setColor(Utils.getColor(R.color.light_blue));
        ringPaint.setAntiAlias(true);
        ringPaint.setStyle(Style.STROKE);
        ringPaint.setStrokeWidth(ringStroke);

        /**
         * 进度圆画笔
         */
        circlePaint = new Paint();
        circlePaint.setColor(Utils.getColor(R.color.light_blue));
        circlePaint.setAntiAlias(true);
        circlePaint.setStyle(Style.FILL);


        /**
         * 百分比画笔
         */
        Typeface face = Typeface.createFromAsset(context.getAssets(),"Roboto-Light.ttf");

        perPaint = new Paint();
        perPaint.setAntiAlias(true);
        perPaint.setStyle(Style.FILL);
        perPaint.setColor(Color.WHITE);
        perPaint.setTextSize(rotateRadius/2);
        perPaint.setTypeface(face);

        /**
         * 充电时间画笔
         */
        textPaint = new Paint();
        textPaint.setAntiAlias(true);
        textPaint.setStyle(Style.FILL);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(rotateRadius/6);
        textPaint.setTypeface(face);

    }


    @Override
    protected void onDraw(Canvas canvas) {
        float mXCenter = rotateRadius;
        float mYCenter = rotateRadius;

        canvas.save();
        canvas.translate(measureWidth/2 - rotateRadius - rotateStroke/2,measureHeigth/2 - rotateRadius - rotateStroke/2);     //左上角位置

        canvas.save();
        canvas.rotate(rotateDegree,rotateRadius,rotateRadius);
        //rotate
        RectF rotateOval = new RectF(mXCenter - rotateRadius,mYCenter - rotateRadius,mXCenter + rotateRadius,mYCenter + rotateRadius);
        canvas.drawArc(rotateOval, 0f, 270f, false, rotatePaint);
        Bitmap bitmap = BitmapFactory.decodeResource(Utils.getResources(),R.drawable.point);
        int left = dip2px(15);
        int top = dip2px(19);
        canvas.drawBitmap(bitmap,rotateRadius - left,-top,rotatePaint);
        canvas.restore();

        //ring
        canvas.drawCircle(mXCenter,mYCenter,ringRadius,ringPaint);

        //circle
        RectF circleOval = new RectF(mXCenter - circleRadius,mYCenter - circleRadius,mXCenter + circleRadius,mYCenter + circleRadius);
        canvas.drawArc(circleOval, 90 - ((float) mProgress / 100) * 360 / 2, ((float) mProgress / 100) * 360, false, circlePaint);

        //per
        String num = String.valueOf(mProgress);
        perPaint.setTextSize(rotateRadius/2);
        FontMetrics fNum = perPaint.getFontMetrics();
        float mNumHeight = fNum.descent - fNum.ascent;
        float mNumWidth = perPaint.measureText(num, 0, num.length());
        canvas.drawText(num, mXCenter - mNumWidth/2 -dip2px(9), mYCenter, perPaint);   //从左下角开始画

        String per = "%";
        perPaint.setTextSize(rotateRadius/5);
        canvas.drawText(per,mXCenter + mNumWidth/2 -dip2px(9), mYCenter, perPaint);

        //text
        String text = desc;
        FontMetrics fText = textPaint.getFontMetrics();
        float mTextHeight = fText.descent - fText.ascent;
        float mTextWidth = textPaint.measureText(text, 0, text.length());
        canvas.drawText(text,mXCenter - mTextWidth/2,mYCenter + 2*mTextHeight,textPaint);


        canvas.restore();
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        measureWidth = MeasureSpec.getSize(widthMeasureSpec);       //1080
        measureHeigth = MeasureSpec.getSize(heightMeasureSpec);     //
        setMeasuredDimension(measureWidth, measureHeigth);
    }


    public void setProgress(int progress,String leftTime) {
        mProgress = progress;

        if (progress == 100){       //充满之后
            isStop = true;
            //文字提示已经充满
            desc = context.getResources().getString(R.string.over);
        }else{
            isStop = false;
            //文字提示剩余时间
            desc = Utils.getString(R.string.left) + " " + leftTime;
        }

        if (thread == null){
            rotateRunnable = new RotateRunnable(handler);
            thread = new Thread(rotateRunnable);
            thread.start();
        }

        postInvalidate();
    }

    public void destoryRotate(){
        isStop = true;
    }

    private RotateRunnable rotateRunnable;
    private Thread thread;
    private boolean isStop = false;
    private float rotateDegree = 0;

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {

            rotateDegree = rotateDegree + 2;
            postInvalidate();

        }
    };




    class RotateRunnable implements Runnable{
        private Handler handler;

        public RotateRunnable(Handler _handler){
            handler = _handler;
        }

        @Override
        public void run() {

            while(!isStop){

                handler.sendEmptyMessage(0);

                try {
                    Thread.sleep(20);
                }catch (Exception e){
                    e.printStackTrace();
                }

            }

        }

    }


    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    private int dip2px(float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
}
