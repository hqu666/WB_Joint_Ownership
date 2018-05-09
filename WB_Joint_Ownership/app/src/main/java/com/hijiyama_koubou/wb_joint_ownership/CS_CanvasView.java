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

public class CS_CanvasView extends View {

	private Paint paint;
	private Path path;
	public Canvas myCanvas;

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
		String dbMsg = "";
		try {
			path = new Path();

			paint = new Paint();
			paint.setColor(0xFF008800);       					//0xFF008800 ; 蛍光グリーン
			paint.setStyle(Paint.Style.STROKE);
			paint.setStrokeJoin(Paint.Join.ROUND);
			paint.setStrokeCap(Paint.Cap.ROUND);
			paint.setStrokeWidth(1);
			myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
	}

	public CS_CanvasView(Context context) {
		super(context);
		final String TAG = "CS_CanvasView[CView]";
		String dbMsg = "";
		try {
			myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		final String TAG = "onDraw[CView]";
		String dbMsg = "";
		try {
			dbMsg += "REQUEST_CORD=" + REQUEST_CORD;

			switch ( REQUEST_CORD ) {
				case REQUEST_CLEAR:                //全消去
					canvas.drawColor(Color.WHITE , PorterDuff.Mode.CLEAR);                // 描画クリア
					path.reset();
					REQUEST_CORD = REQUEST_DROW_PATH;
					break;
				case REQUEST_DROW_PATH:                //フリーハンド
					canvas.drawPath(path , paint);
					break;
				case REQUEST_ADD_BITMAP:                //ビットマップ挿入
					canvas.drawBitmap(aBmp , upX , upY , ( Paint ) null); // image, x座標, y座標, Paintイタンス
					break;
			}
			myCanvas = canvas;
			dbMsg += ",myCanvas[" + myCanvas.getWidth() + "×" + myCanvas.getHeight() + "]";
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
					path = new Path();
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
			//			myLog(TAG , dbMsg);
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