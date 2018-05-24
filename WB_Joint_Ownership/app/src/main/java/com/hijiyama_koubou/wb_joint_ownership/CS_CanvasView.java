package com.hijiyama_koubou.wb_joint_ownership;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;

import org.webrtc.EglBase;
import org.webrtc.RendererCommon;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoRenderer;
import org.webrtc.RendererCommon.RendererEvents;
import org.webrtc.RendererCommon.ScalingType;


import io.skyway.Peer.Browser.MediaStream;


public class CS_CanvasView extends View {        //org; View	から　io.skyway.Peer.Browser.Canvas	に合わせる
	// extends FrameLayout implements RendererEvents
	//extends FrameLayout implements org.webrtc.RendererCommon.RendererEvents
	private Context context;
	private Paint paint;                        //ペン
	private int penColor = 0xFF008800;        //蛍光グリーン
	private float penWidth = 2;


	private Paint eraserPaint;                //消しゴム
	private int eraserColor = Color.WHITE;        //背景色に揃える
	private float eraserWidth = 50.0f;

	private Path path;

	float upX;
	float upY;

	static final int REQUEST_CLEAR = 500;                            //全消去
	static final int REQUEST_DROW_PATH = REQUEST_CLEAR + 1;            //フリーハンド
	static final int REQUEST_ADD_BITMAP = REQUEST_DROW_PATH + 1;            //ビットマップ挿入
	public int REQUEST_CORD = REQUEST_DROW_PATH;


	/**
	 * xmlに書き込む場合
	 */
	public CS_CanvasView(Context context , AttributeSet attrs) {
		super(context , attrs);
		final String TAG = "CS_CanvasView[CView]";
		String dbMsg = "xmlから";
		try {
			commonCon(context);
			myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
	}

	public CS_CanvasView(Context context) {
		super(context);
		final String TAG = "CS_CanvasView[CView]";
		String dbMsg = "メソッド内から";
		try {
			commonCon(context);
			myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
	}

//	public CS_CanvasView(Context context , AttributeSet attrs , int defStyleAttr) {
//		super(context , attrs , defStyleAttr);
//		final String TAG = "CS_CanvasView[CView]";
//		String dbMsg = "？";
//		try {
//			commonCon(context);
//			myLog(TAG , dbMsg);
//		} catch (Exception er) {
//			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
//		}
//	}

	public void commonCon(Context context) {
		final String TAG = "commonCon[CView]";
		String dbMsg = "";
		try {
			this.context = context;
			InitCanva();
			myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
	}

	public void InitCanva() {
		final String TAG = "InitCanva[CView]";
		String dbMsg = "";
		try {
			path = new Path();
			paint = new Paint();
			dbMsg += ",ペン；" + penColor;
			paint.setColor(penColor);                        //
			paint.setStyle(Paint.Style.STROKE);
			paint.setStrokeJoin(Paint.Join.ROUND);
			paint.setStrokeCap(Paint.Cap.ROUND);
			dbMsg += "," + penWidth + "px";
			paint.setStrokeWidth(penWidth);

			eraserPaint = new Paint();                //消しゴム
			dbMsg += ",消しゴム；" + eraserColor;
			eraserPaint.setColor(eraserColor);
			dbMsg += "," + eraserWidth + "px";
			eraserPaint.setStrokeWidth(eraserWidth);

			myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
	}

	/**
	 * View の canvas操作
	 */
	@Override
	protected void onDraw(Canvas canvas) {
		final String TAG = "onDraw[CView]";
		String dbMsg = "";
		try {
			dbMsg += "REQUEST_CORD=" + REQUEST_CORD;
			canvasDraw(canvas);
//			myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
	}

	/**
	 * ViewGroup の canvas操作
	 */
	@Override
	protected void dispatchDraw(Canvas canvas) {
		super.dispatchDraw(canvas);
		final String TAG = "dispatchDraw[CView]";
		String dbMsg = "";
		try {
			canvasDraw(canvas);
			myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
	}

	public void canvasDraw(Canvas canvas) {
		final String TAG = "canvasDraw[CView]";
		String dbMsg = "";
		try {
			dbMsg += "REQUEST_CORD=" + REQUEST_CORD;
			int caWidth = canvas.getWidth();
			int caHeight = canvas.getHeight();
			dbMsg += ".canvas[" + caWidth + "" + caHeight + "]";
			switch ( REQUEST_CORD ) {
				case REQUEST_CLEAR:                //全消去
					canvas.drawColor(eraserColor , PorterDuff.Mode.CLEAR);                // 描画クリア
					path.reset();
					canvas.drawRect(0 , 0 , caWidth , caHeight , eraserPaint);        //?真っ黒になるので背景色に塗りなおす
					REQUEST_CORD = REQUEST_DROW_PATH;
					break;
				case REQUEST_DROW_PATH:                //フリーハンド
					canvas.drawPath(path , paint);
					break;
				case REQUEST_ADD_BITMAP:                //ビットマップ挿入
					canvas.drawBitmap(aBmp , upX , upY , ( Paint ) null); // image, x座標, y座標, Paintイタンス
					break;
			}
//			myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		final String TAG = "onTouchEvent[CView]";
		String dbMsg = "";
		try {
			float x = event.getX();
			float y = event.getY();
			dbMsg += "myCanvas[" + x + "×" + y + "]";
			switch ( REQUEST_CORD ) {
				case REQUEST_CLEAR:                        //全消去
//					path = new Path();
					break;
				case REQUEST_DROW_PATH:                        //フリーハンド
					switch ( event.getAction() ) {
						case MotionEvent.ACTION_DOWN:
							path.moveTo(x , y);
							invalidate();
							break;
						case MotionEvent.ACTION_MOVE:
							path.lineTo(x , y);
							invalidate();
							break;
						case MotionEvent.ACTION_UP:
							path.lineTo(x , y);
							invalidate();                        //onDrawを発生させて描画実行
							break;
					}
					break;
				case REQUEST_ADD_BITMAP:                        //ビットマップ挿入
					upX = x;
					upY = y;
					invalidate();                        //onDrawを発生させて描画実行
					REQUEST_CORD = 0;
					break;
				default:
					upX = x;
					upY = y;
					invalidate();                        //onDrawを発生させて描画実行
//					REQUEST_CORD = 0;
					break;
			}
//						myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
		return true;
	}

	public Bitmap aBmp;

	public void addBitMap(Bitmap bmp) {
		final String TAG = "addBitMap[CView]";
		String dbMsg = "";
		try {
			REQUEST_CORD = REQUEST_ADD_BITMAP;
			aBmp = bmp;
			myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
	}

	public void startFreeHand() {
		final String TAG = "startFreeHand[CView]";
		String dbMsg = "";
		try {
			REQUEST_CORD = REQUEST_DROW_PATH;
			myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
	}


	public void clearAll() {
		final String TAG = "clearAll[CView]";
		String dbMsg = "";
		try {
			REQUEST_CORD = REQUEST_CLEAR;
			invalidate();                        //onDrawを発生させて描画実行
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

//		Canvas と Path による手書き View の簡単な実装		http://android.keicode.com/basics/ui-canvas-path.php


//FrameLayout implements RendererCommon.RendererEvents では   onTouchEventは発生しても  onDrawが発生しない

//webRTC for android	https://qiita.com/nakadoribooks/items/7950e29ad3b751ddab12