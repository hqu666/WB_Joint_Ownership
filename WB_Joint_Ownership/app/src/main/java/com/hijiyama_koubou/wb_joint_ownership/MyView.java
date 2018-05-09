package com.hijiyama_koubou.wb_joint_ownership;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.view.View;

public class MyView extends View {

	public Paint paint;
	public Canvas canvas;

	public MyView(Context context, AttributeSet attrs) {
		super(context, attrs);
		paint = new Paint();
	}

	public MyView(Context context) {
		super(context);
		paint = new Paint();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		final String TAG = "onDraw[MyView]";
		String dbMsg = "";
		try {
			this.canvas = canvas;
			int canWidth = canvas.getWidth();
			int cantHeight = canvas.getHeight();
			dbMsg = "canvas[" + canWidth+"×" + cantHeight +"]";
//			testDrow();
			myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
	}


	protected void testDrow() {
		final String TAG = "testDrow[MyView]";
		String dbMsg = "";
		try {
			int canWidth = canvas.getWidth();
			int cantHeight = canvas.getHeight();
			dbMsg = "canvas[" + canWidth+"×" + cantHeight +"]";

			// 背景、半透明
			canvas.drawColor(Color.argb(125, 0, 0, 255));

			// 円
			paint.setColor(Color.argb(255, 68, 125, 255));
			paint.setStrokeWidth(10);
			paint.setAntiAlias(true);
			paint.setStyle(Paint.Style.STROKE);
			// (x1,y1,r,paint) 中心x1座標, 中心y1座標, r半径
			canvas.drawCircle(canWidth/3, cantHeight/3, canWidth/6, paint);

			// 矩形
			paint.setColor(Color.argb(255, 255, 0, 255));
			paint.setStrokeWidth(10);
			paint.setStyle(Paint.Style.STROKE);
			// (x1,y1,x2,y2,paint) 左上の座標(x1,y1), 右下の座標(x2,y2)
			canvas.drawRect(canWidth/3, cantHeight/3, canWidth/3, cantHeight/3, paint);

			// 線
			paint.setStrokeWidth(15);
			paint.setColor(Color.argb(255, 0, 255, 120));
			// (x1,y1,x2,y2,paint) 始点の座標(x1,y1), 終点の座標(x2,y2)
			canvas.drawLine(canWidth/12, cantHeight/12, canWidth*9/10, cantHeight*11/12, paint);
			myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
	}

	protected void addBitMap(Bitmap bmp) {
		final String TAG = "addBitMap[MyView]";
		String dbMsg = "";
		try {
			int canWidth = canvas.getWidth();
			int cantHeight = canvas.getHeight();
			dbMsg = "canvas[" + canWidth+"×" + cantHeight +"]";
			// 描画クリア
			canvas.drawColor(0, PorterDuff.Mode.CLEAR);
			// 背景、白
//			canvas.drawColor(Color.argb(255, 255, 255, 255));

//			int bmWidth = bmp.getWidth();
//			int bmHeight = bmp.getHeight();
//			dbMsg = "bmp[" +bmWidth + "×" +bmHeight+ "]";
////			Bitmap new_bitmap = Bitmap.createBitmap(bmWidth, bmHeight, Bitmap.Config.ARGB_8888);
////			Canvas canvas = new Canvas(new_bitmap);
//			canvas.drawBitmap(bmp, bmWidth, bmHeight, (Paint)null); // image, x座標, y座標, Paintイタンス
			myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
	}



	///////////////////////////////////////////////////////////////////////////////////
	public static void myLog(String TAG , String dbMsg) {
		CS_Util UTIL = new CS_Util();
		UTIL.myLog(TAG , dbMsg);
	}

	public static void myErrorLog(String TAG , String dbMsg) {
		CS_Util UTIL = new CS_Util();
		UTIL.myErrorLog(TAG , dbMsg);
	}
}