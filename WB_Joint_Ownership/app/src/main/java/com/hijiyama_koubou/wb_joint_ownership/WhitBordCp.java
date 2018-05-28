package com.hijiyama_koubou.wb_joint_ownership;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

public class WhitBordCp extends Activity {
	private Context context;
	private Activity activity;
	private CS_CanvasView wb_whitebord;

	private Spinner wb_mode_sp;                    //描画種別選択
	private ImageButton wb_color_bt;            //色選択
	private Spinner wb_width_sp;                    //太さ選択
	private Spinner wb_linecaps_sp;                    //先端形状
	private TextView wb_info_tv;            //情報表示
	private ImageButton wb_all_clear_bt;        //全消去
	private ColorPickerDialog mColorPickerDialog;

	public String selectMode;
	public String selectCaps = "round";
	public int selectWidth = 5;
	public int selectColor = Color.GREEN;

	public WhitBordCp(final Context context , Activity activity) {                        // , CS_CanvasView wb_whitebord
//		super(context);
		final String TAG = "WhitBordCp[WBC]";
		String dbMsg = "メソッド内から";
		try {
			this.context = context;
			this.activity = activity;

			wb_whitebord = ( CS_CanvasView ) activity.findViewById(R.id.wb_whitebord);        //ホワイトボード             	Canvas	     CS_CanvasView
			if ( wb_whitebord == null ) {
				wb_whitebord = ( CS_CanvasView ) activity.findViewById(R.id.main_whitebord);
//			main_whitebord = ( CS_CanvasView ) findViewById(R.id.main_whitebord);        //ホワイトボード             	Canvas	     CS_CanvasView
			}

			wb_mode_sp = ( Spinner ) activity.findViewById(R.id.wb_mode_sp);                    //描画種別選択
			wb_color_bt = ( ImageButton ) activity.findViewById(R.id.wb_color_bt);            //色選択
			wb_width_sp = ( Spinner ) activity.findViewById(R.id.wb_width_sp);                    //太さ選択
			wb_linecaps_sp = ( Spinner ) activity.findViewById(R.id.wb_linecaps_sp);                    //先端形状
//			wb_info_tv = ( TextView ) activity.findViewById(R.id.wb_info_tv);            //情報表示
			wb_all_clear_bt = ( ImageButton ) activity.findViewById(R.id.wb_all_clear_bt);        //全消去

			//色選択
			wb_color_bt.setBackgroundColor(selectColor);
			wb_color_bt.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					final String TAG = "wb_color_bt[WBC]";
					String dbMsg = "";
					try {
						mColorPickerDialog = new ColorPickerDialog(context , new ColorPickerDialog.OnColorChangedListener() {
							@Override
							public void colorChanged(int color) {
								final String TAG = "wb_color_bt[WBC]";
								String dbMsg = "";
								selectColor = color;
								dbMsg = "selectColor=" + selectColor;
								wb_color_bt.setBackgroundColor(selectColor);
//								if ( wb_whitebord != null ) {
									wb_whitebord.setPenColor(selectColor);
//								}
								myLog(TAG , dbMsg);
							}
						} , selectColor);
						mColorPickerDialog.show();
						myLog(TAG , dbMsg);
					} catch (Exception er) {
						myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
					}
				}
			});

			wb_mode_sp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
				@Override
				public void onItemSelected(AdapterView< ? > parent , View view , int position , long id) {
					final String TAG = "wb_mode_sp[WBC]";
					String dbMsg = "";
					try {
						dbMsg = ",position=" + position + ",id=" + id;
						Spinner spinner = ( Spinner ) parent;
						if ( spinner.isFocusable() == false ) { //
							dbMsg += "isFocusable=false";
							spinner.setFocusable(true);
						} else {
							String item = ( String ) spinner.getSelectedItem();
							dbMsg += ",item=" + item;
							String[] items = WhitBordCp.this.activity.getResources().getStringArray(R.array.typeSelectValList);
							selectMode = items[position];
							dbMsg += ",selectMode=" + selectMode;
						}
						myLog(TAG , dbMsg);
					} catch (Exception er) {
						myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
					}
				}

				@Override
				public void onNothingSelected(AdapterView< ? > arg0) {
				}
			});

			wb_width_sp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
				@Override
				public void onItemSelected(AdapterView< ? > parent , View view , int position , long id) {
					final String TAG = "wb_width_sp[WBC]";
					String dbMsg = "";
					try {

						dbMsg = "position=" + position + "id=" + id;
						Spinner spinner = ( Spinner ) parent;
						if ( spinner.isFocusable() == false ) { // 起動時に一度呼ばれてしまう
							dbMsg += "isFocusable=false";
							spinner.setFocusable(true);
						} else {
							String item = ( String ) spinner.getSelectedItem();
							dbMsg += "item=" + item;
							selectWidth = Integer.parseInt(item);
							dbMsg += "selectWidth=" + selectWidth;
							if ( selectWidth < 1 ) {
								selectWidth = 1;
							}
							if ( wb_whitebord != null ) {
								wb_whitebord.setPenWidth(selectWidth);
								int _width = ( int ) wb_whitebord.getPenWidth();
								dbMsg += ">emit>" + _width;
							}
						}
						myLog(TAG , dbMsg);
					} catch (Exception er) {
						myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
					}
				}

				@Override
				public void onNothingSelected(AdapterView< ? > arg0) {
				}
			});

			wb_linecaps_sp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
				@Override
				public void onItemSelected(AdapterView< ? > parent , View view , int position , long id) {
					final String TAG = "wb_linecaps_sp[WBC]";
					String dbMsg = "";
					try {
						dbMsg = ",position=" + position + ",id=" + id;
						Spinner spinner = ( Spinner ) parent;
						if ( spinner.isFocusable() == false ) { // 起動時に一度呼ばれてしまう
							dbMsg += "isFocusable=false";
							spinner.setFocusable(true);
						} else {
							String item = ( String ) spinner.getSelectedItem();
							dbMsg += ",item=" + item;
							String[] items = WhitBordCp.this.activity.getResources().getStringArray(R.array.lineCapSelecttValList);
							selectCaps = items[position];
							dbMsg += ",selectCaps=" + selectCaps;
							if ( wb_whitebord != null ) {
								wb_whitebord.setPenCap(selectCaps);                             //先端形状
							}
						}
						myLog(TAG , dbMsg);
					} catch (Exception er) {
						myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
					}
				}

				@Override
				public void onNothingSelected(AdapterView< ? > arg0) {
				}
			});

			String[] rList = activity.getResources().getStringArray(R.array.lineWidthSelectList);
			int selP = 0;// rList.
			for ( String rStr : rList ) {
				int rInt = Integer.parseInt(rStr);
				if ( rInt == selectWidth ) {
					break;
				}
				selP++;
			}
			wb_width_sp.setSelection(selP);

			String[] rList2 = activity.getResources().getStringArray(R.array.lineCapSelecttValList);
			selP = 0;// rList.
			for ( String rStr : rList2 ) {
				if ( rStr.equals(selectCaps) ) {
					break;
				}
				selP++;
			}
			wb_linecaps_sp.setSelection(selP);


			myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
	}

	/**
	 * Spinnerは起動時に一度呼ばれてしまう
	 */
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		final String TAG = "onWindowFocusChanged[WBC]";
		String dbMsg = "hasFocus=" + hasFocus;
		try {
			if ( hasFocus ) {

//				wb_all_clear_bt.setOnClickListener(new View.OnClickListener() {        //全消去
//					@Override
//					public void onClick(View v) {
//						final String TAG = "wb_all_clear_bt[WBC]";
//						String dbMsg = "";
//						try {
//							if ( wb_whitebord != null ) {
//								wb_whitebord.clearAll();
//							}
//							mSocket.emit("allclear" , "");     //共有webページに全消去命令送信
//							myLog(TAG , dbMsg);
//						} catch (Exception er) {
//							myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
//						}
//					}
//				});

			}
			myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
	}


	///////////////////////////////////////////////////////////////////////////////////
	public void messageShow(String titolStr , String mggStr) {
		CS_Util UTIL = new CS_Util();
		UTIL.messageShow(titolStr , mggStr , WhitBordCp.this);
	}

	public static void myLog(String TAG , String dbMsg) {
		CS_Util UTIL = new CS_Util();
		UTIL.myLog(TAG , dbMsg);
	}

	public static void myErrorLog(String TAG , String dbMsg) {
		CS_Util UTIL = new CS_Util();
		UTIL.myErrorLog(TAG , dbMsg);
	}
}
