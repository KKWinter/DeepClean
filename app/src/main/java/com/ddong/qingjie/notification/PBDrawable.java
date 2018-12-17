package com.ddong.qingjie.notification;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;

import com.ddong.qingjie.R;

public class PBDrawable extends Drawable {
	
	private int progress;
	private Context context;

	private Paint mPaint;

	public PBDrawable(Context _context,int _progress){
		this.progress = _progress;
		this.context = _context;

		//初始化画笔
		mPaint = new Paint();
	}

	@Override
	public void draw(Canvas canvas) {
		//画画板view的背景色
		canvas.drawColor(Color.TRANSPARENT);

		int centre = canvas.getWidth()/2; //获取圆心的x坐标
		int radius = centre - 3; 		  //圆环的半径

		//画背景圆环
		mPaint.setColor(context.getResources().getColor(R.color.view_gray)); //设置圆环的颜色
		mPaint.setStyle(Paint.Style.STROKE); //设置空心
		mPaint.setStrokeWidth(2); 			 //设置圆环的宽度
		mPaint.setAntiAlias(true);  		 //消除锯齿
		canvas.drawCircle(centre, centre, radius, mPaint); //画出圆环

		//画前景进度圆弧
		mPaint.setStrokeWidth(2); //设置圆环的宽度
		mPaint.setColor(context.getResources().getColor(R.color.bg_green));  //设置进度的颜色
		mPaint.setStyle(Paint.Style.STROKE);
		mPaint.setAntiAlias(true);
		RectF oval = new RectF(centre - radius, centre - radius, centre
				+ radius, centre + radius);  								//用于定义的圆弧的形状和大小的界限
		canvas.drawArc(oval, 0, 360 * progress / 100, false, mPaint); 		 //根据进度画圆弧
	}

	@Override
	public void setAlpha(int alpha) {

	}

	@Override
	public void setColorFilter(ColorFilter cf) {

	}

	@Override
	public int getOpacity() {
		return 0;
	}

	@Override
	public int getIntrinsicHeight() {
		return 50;
	}

	@Override
	public int getIntrinsicWidth() {
		return 50;
	}


}
