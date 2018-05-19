package com.hijiyama_koubou.wb_joint_ownership;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

public class CS_WhitebordActivity extends Activity {             //AppCompatActivity
	private CS_CanvasView wb_whitebord;        //ホワイトボード        CS_CanvasView
	private ImageButton wb_all_clear_bt;        //全消去
	private ImageButton wb_mode_bt;                    //編修


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_whitebord);         //activity_whitebord          wb_tisement   ではイベント動作しない
		wb_whitebord = ( CS_CanvasView ) findViewById(R.id.wb_whitebord);        //ホワイトボード             	Canvas	     CS_CanvasView
		wb_all_clear_bt = ( ImageButton ) findViewById(R.id.wb_all_clear_bt);        //全消去
		wb_mode_bt = ( ImageButton ) findViewById(R.id.wb_mode_bt);                    //編修
//		ImageButton	ImageButton = ( ImageButton ) findViewById(R.id.vf_previous_bt);         // ViewFlipperの前（左）画面
//		ImageButton.setVisibility(View.GONE);


		wb_all_clear_bt.setOnClickListener(new View.OnClickListener() {        //全消去
			@Override
			public void onClick(View v) {
				final String TAG = "wb_all_clear_bt[WB]";
				String dbMsg = "";
				try {
					if ( wb_whitebord != null ) {
						wb_whitebord.clearAll();
					}
					myLog(TAG , dbMsg);
				} catch (Exception er) {
					myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
				}
			}
		});

		wb_mode_bt.setOnClickListener(new View.OnClickListener() {                    //編修
			@Override
			public void onClick(View v) {
				final String TAG = "wb_mode_bt[WB]";
				String dbMsg = "";
				try {
					if ( wb_whitebord != null ) {
						wb_whitebord.startFreeHand();
					}
					myLog(TAG , dbMsg);
				} catch (Exception er) {
					myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
				}
			}
		});


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
