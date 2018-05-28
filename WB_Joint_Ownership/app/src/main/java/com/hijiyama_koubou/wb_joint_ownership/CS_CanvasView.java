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
import android.widget.ImageButton;
import android.widget.Spinner;

import org.webrtc.EglBase;
import org.webrtc.RendererCommon;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoRenderer;
import org.webrtc.RendererCommon.RendererEvents;
import org.webrtc.RendererCommon.ScalingType;


import java.util.ArrayList;
import java.util.List;

import io.skyway.Peer.Browser.MediaStream;


public class CS_CanvasView extends View {        //org; View	から　io.skyway.Peer.Browser.Canvas	に合わせる
	// extends FrameLayout implements RendererEvents
	//extends FrameLayout implements org.webrtc.RendererCommon.RendererEvents
	private Context context;

	private Paint paint;                        //ペン
	//	public int penColor = 0xFF008800;        //蛍光グリーン
	public int selectColor = 0xFF008800;        //蛍光グリーン

	//	private float penWidth = 5;
	public float selectWidth = 5;
	public String selectLineCap = "";
	public String[] lineCapList;

	private Paint eraserPaint;                //消しゴム
	private int eraserColor = Color.WHITE;        //背景色に揃える
	private float eraserWidth = 50.0f;

	//	private Path path;
	public List< PathObject > pathIist;

	class PathObject {
		Path path;
		Paint paint;
	}

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


	public void commonCon(final Context context) {
		final String TAG = "commonCon[CView]";
		String dbMsg = "";
		try {
			this.context = context;
           			lineCapList = context.getResources().getStringArray(R.array.lineCapSelecttValList);
			selectLineCap = lineCapList[0];
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
			pathIist = new ArrayList< PathObject >();
			paint = new Paint();
			dbMsg += ",ペン；" + selectColor;
			paint.setColor(selectColor);                        //
			paint.setStyle(Paint.Style.STROKE);
			paint.setStrokeJoin(Paint.Join.ROUND);
			paint.setStrokeCap(Paint.Cap.ROUND);
			dbMsg += "," + selectWidth + "px";
			paint.setStrokeWidth(selectWidth);

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
	 * ペンの色変更
	 */
	public void setPenColor(int selectColor) {
		final String TAG = "setPenColor[CView]";
		String dbMsg = "";
		try {
			dbMsg = "selectColor=" + selectColor;
			this.selectColor = selectColor;
			paint = new Paint();
			dbMsg += ",ペン；" + selectColor;
			paint.setColor(selectColor);                        //
			paint.setStyle(Paint.Style.STROKE);
			paint.setStrokeJoin(Paint.Join.ROUND);
			if ( selectLineCap.equals(lineCapList[0]) ) {     //round
				paint.setStrokeCap(Paint.Cap.ROUND);
			} else if ( selectLineCap.equals(lineCapList[1]) ) {     //square
				paint.setStrokeCap(Paint.Cap.SQUARE);
			} else if ( selectLineCap.equals(lineCapList[2]) ) {     //butt
				paint.setStrokeCap(Paint.Cap.BUTT);
			}
			dbMsg += "," + selectWidth + "px";
			paint.setStrokeWidth(selectWidth);
			myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
	}

	public int getPenColor() {
		final String TAG = "setPenColor[CView]";
		String dbMsg = "";
		try {
			dbMsg = "selectColor=" + selectColor;
			myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
		return selectColor;
	}


	/**
	 * ペンの太さ変更
	 */
	public void setPenWidth(int _selectWidth) {
		final String TAG = "setPenWidth[CView]";
		String dbMsg = "";
		try {
			dbMsg = "selectWidth=" + selectWidth;
			selectWidth = _selectWidth;
			paint = new Paint();
			dbMsg += ",ペン；selectColor=" + selectColor;
			paint.setColor(selectColor);                        //
			paint.setStyle(Paint.Style.STROKE);
			paint.setStrokeJoin(Paint.Join.ROUND);
			if ( selectLineCap.equals(lineCapList[0]) ) {     //round
				paint.setStrokeCap(Paint.Cap.ROUND);
			} else if ( selectLineCap.equals(lineCapList[1]) ) {     //square
				paint.setStrokeCap(Paint.Cap.SQUARE);
			} else if ( selectLineCap.equals(lineCapList[2]) ) {     //butt
				paint.setStrokeCap(Paint.Cap.BUTT);
			}
			dbMsg += ",selectWidth=" + selectWidth + "px";
			paint.setStrokeWidth(selectWidth);
			myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
	}

	public float getPenWidth() {
		final String TAG = "getPenWidth[CView]";
		String dbMsg = "";
		try {
			dbMsg = "selectWidth=" + selectWidth;
			myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
		return selectWidth;
	}

	/**
	 * ペンの先端形状
	 */
	public void setPenCap(String lineCap) {
		final String TAG = "setPenCap[CView]";
		String dbMsg = "";
		try {
			dbMsg = "this.selectLineCap=" + this.selectLineCap;
			dbMsg += "lineCap=" + lineCap;
			if ( !lineCap.equals(this.selectLineCap) ) {
				this.selectLineCap = lineCap;
				paint = new Paint();
				paint.setColor(selectColor);                        //
				paint.setStyle(Paint.Style.STROKE);
				paint.setStrokeJoin(Paint.Join.ROUND);
				if ( selectLineCap.equals(lineCapList[0]) ) {     //round
					paint.setStrokeCap(Paint.Cap.ROUND);
				} else if ( selectLineCap.equals(lineCapList[1]) ) {     //square
					paint.setStrokeCap(Paint.Cap.SQUARE);
				} else if ( selectLineCap.equals(lineCapList[2]) ) {     //butt
					paint.setStrokeCap(Paint.Cap.BUTT);
				}
				dbMsg += "," + selectWidth + "px";
				paint.setStrokeWidth(selectWidth);
			}
			myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
	}

	public String getPenCap() {
		final String TAG = "getPenCap[CView]";
		String dbMsg = "";
		try {
			dbMsg = "selectLineCap=" + selectLineCap;
			myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
		return selectLineCap;
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
					for ( PathObject pathObject : pathIist ) {
						pathObject.path.reset();
					}
					canvas.drawRect(0 , 0 , caWidth , caHeight , eraserPaint);        //?真っ黒になるので背景色に塗りなおす
					REQUEST_CORD = REQUEST_DROW_PATH;
					break;
				case REQUEST_DROW_PATH:                //フリーハンド
					for ( PathObject pathObject : pathIist ) {
						canvas.drawPath(pathObject.path , pathObject.paint);
					}
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

	/**
	 * 受信/送信前のイベント処理
	 */
	public void drawPathLine(float xPoint , float yPoint , int addColor , float addWidth , String addCap , int action) {
		final String TAG = "drawPathLine[CView]";
		String dbMsg = "";
		try {

			dbMsg = "action=" + action + "(" + xPoint + " , " + yPoint + ")";
			switch ( action ) {
				case MotionEvent.ACTION_DOWN:   //0
					PathObject pathObject = new PathObject();
					Path path = new Path();
					path.moveTo(xPoint , yPoint);
					pathObject.path = path;

					Paint addPaint = new Paint();
					dbMsg += ",addColor=" + addColor;
					addPaint.setColor(addColor);                        //
					addPaint.setStyle(Paint.Style.STROKE);
					addPaint.setStrokeJoin(Paint.Join.ROUND);
					dbMsg += ",addCap=" + addCap;
					if ( addCap.equals(lineCapList[0]) ) {     //round
						addPaint.setStrokeCap(Paint.Cap.ROUND);
					} else if ( addCap.equals(lineCapList[1]) ) {     //square
						paint.setStrokeCap(Paint.Cap.SQUARE);
					} else if ( addCap.equals(lineCapList[2]) ) {     //butt
						addPaint.setStrokeCap(Paint.Cap.BUTT);
					}
					dbMsg += ",addWidth=" + addWidth + "px";
					addPaint.setStrokeWidth(addWidth);
					pathObject.paint = addPaint;

					pathIist.add(pathObject);

					invalidate();
					break;
				case MotionEvent.ACTION_MOVE:   //2
					pathIist.get(pathIist.size() - 1).path.lineTo(xPoint , yPoint);
					invalidate();
					break;
				case MotionEvent.ACTION_UP:     //1
					pathIist.get(pathIist.size() - 1).path.lineTo(xPoint , yPoint);
					invalidate();                        //onDrawを発生させて描画実行
					break;
			}
			myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		final String TAG = "onTouchEvent[CView]";
		String dbMsg = "";
		try {
			float xPoint = event.getX();
			float yPoint = event.getY();
			dbMsg += "myCanvas[" + xPoint + "×" + yPoint + "]";
			switch ( REQUEST_CORD ) {
				case REQUEST_CLEAR:                        //全消去
					break;
				case REQUEST_DROW_PATH:                        //フリーハンド
					drawPathLine(xPoint , yPoint , selectColor , selectWidth , selectLineCap , event.getAction());
					break;
				case REQUEST_ADD_BITMAP:                        //ビットマップ挿入
					upX = xPoint;
					upY = yPoint;
					invalidate();                        //onDrawを発生させて描画実行
					REQUEST_CORD = 0;
					break;
				default:
					upX = xPoint;
					upY = yPoint;
					invalidate();                        //onDrawを発生させて描画実行
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