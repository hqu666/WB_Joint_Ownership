package com.hijiyama_koubou.wb_joint_ownership;

import android.app.Activity;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;

import io.socket.IOAcknowledge;
import io.socket.IOCallback;
import io.socket.SocketIO;
import io.socket.SocketIOException;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;

import static android.content.ContentValues.TAG;


public class CS_WhitebordActivity extends Activity {             //AppCompatActivity
	private CS_CanvasView wb_whitebord;        //ホワイトボード        CS_CanvasView
	private ImageButton wb_all_clear_bt;        //全消去
	private ImageButton wb_mode_bt;                    //編修

 /////SocketIO////Androidでsocket.io		  https://kinjouj.github.io/2014/01/android-socketio.html
	Handler mHandler;
	//	ArrayAdapter< string > mAdapter;
	SocketThread mThread;
	SocketIO mSocket;
 ////////////////////////////////////

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		final String TAG = "onCreate[WA]";
		String dbMsg = "";
		try {
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
			requestWindowFeature(Window.FEATURE_NO_TITLE);

			setContentView(R.layout.activity_whitebord);         //activity_whitebord          wb_tisement   ではイベント動作しない

			wb_whitebord = ( CS_CanvasView ) findViewById(R.id.wb_whitebord);        //ホワイトボード             	Canvas	     CS_CanvasView
			wb_all_clear_bt = ( ImageButton ) findViewById(R.id.wb_all_clear_bt);        //全消去
			wb_mode_bt = ( ImageButton ) findViewById(R.id.wb_mode_bt);                    //編修


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
			/////SocketIO///////////////////////////////////
			mHandler = new Handler();
//		mAdapter = new ArrayAdapter< string >(this , android.R.layout.simple_list_item_1);
 			myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
	}


	@Override
	public void onStart() {
		super.onStart();
		final String TAG = "onStart[WA]";
		String dbMsg = "";
		try {
			/////SocketIO///////////////////////////////////
			if ( mThread == null ) {
				mThread = new SocketThread();
				mThread.start();
			}
			myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
	}

	@Override
	public void onStop() {
		super.onStop();
		final String TAG = "onStop[WA]";
		String dbMsg = "";
		try {
			/////SocketIO///////////////////////////////////
			if ( mThread != null ) {
				mThread.cancel();
				mThread = null;
			}
			myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
	}

	/////SocketIO///////////////////////////////////
	private class SocketThread extends Thread {
		@Override
		public void run() {
			final String TAG = "run[WA]";
			String dbMsg = "mSocket != null ";
			try {
				if ( mSocket == null ) {
					String urlStr = "http://ec2-52-197-173-40.ap-northeast-1.compute.amazonaws.com:3080/";
//					String urlStr = "http://[socket.io.server.ip]:8080";
					dbMsg = "urlStr= " + urlStr;
					mSocket = new SocketIO(urlStr);

					mSocket.connect(new IOCallback() {
						@Override
						public void onConnect() {                      //サーバとの接続が確立されたとき
							final String TAG = "onConnect[WA]";
							String dbMsg = "";
							try {
								mSocket.send("android");      								// connectしたらAndroidからsend
								myLog(TAG , dbMsg);
							} catch (Exception er) {
								myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
							}
						}

						@Override
						public void onDisconnect() {                   			//サーバとの接続が切断されたとき
							final String TAG = "onDisconnect[WA]";
							String dbMsg = "";
							try {
								myLog(TAG , dbMsg);
							} catch (Exception er) {
								myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
							}
						}

						@Override
						public void onError(SocketIOException e) {
							final String TAG = "onError[WA]";
							myErrorLog(TAG , "エラー発生；" + e.getMessage());
							//Error while handshaking
						}

						@Override
						public void on(String eventName , IOAcknowledge ack , Object... args) {          		//イベントを受信したとき
							final String TAG = "on[WA]";
							String dbMsg = "";
							try {
								dbMsg += ".eventName=" + eventName;
								for ( Object arg : args ) {
									if ( !(arg instanceof JSONObject) )
										continue;
									JSONObject json = ( JSONObject ) arg;
									onMessage(json , null);
								}
								myLog(TAG , dbMsg);
							} catch (Exception er) {
								myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
							}
						}

						@Override
						public void onMessage(String msg , IOAcknowledge ack) {
							final String TAG = "onMessage;String[WA]";
							String dbMsg = "";
							try {
								dbMsg += ".msg=" + msg;
								update(msg);
								myLog(TAG , dbMsg);
							} catch (Exception er) {
								myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
							}
						}

						@Override
						public void onMessage(JSONObject data , IOAcknowledge ack) {     		//JSONを受信したとき
							final String TAG = "onMessage;JSONObject[WA]";
							String dbMsg = "";
							try {
								dbMsg = "onMessage(JSON)";
								if ( !data.has("msg") )
									return;
								try {
									String msg = ( String ) data.get("msg");
									dbMsg += ".msg=" + msg;
									update(msg);
								} catch (JSONException er) {
									myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
								}
								myLog(TAG , dbMsg);
							} catch (Exception er) {
								myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
							}
						}

						private void update(final String data) {
							mHandler.post(new Thread() {
								@Override
								public void run() {
									final String TAG = "run[WA]";
									String dbMsg = "";
									try {
										dbMsg = "data~" + data;
//										mAdapter.add(data);
//										mAdapter.notifyDataSetChanged();
										myLog(TAG , dbMsg);
									} catch (Exception er) {
										myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
									}
								}
							});
						}
					});
				}
				myLog(TAG , dbMsg);
			} catch (MalformedURLException er) {
				myErrorLog(TAG , "エラー発生；" + er);
			}
		}

		public void cancel() {
			final String TAG = "cancel[WA]";
			String dbMsg = "";
			try {
				if ( mSocket != null ) {
					mSocket.disconnect();
					mSocket = null;
				}
				myLog(TAG , dbMsg);
			} catch (Exception er) {
				myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
			}
		}
	}


	//UTIL/////////////////////////////////////////////////////////////////////////////SocketIO//
	public static void myLog(String TAG , String dbMsg) {
		CS_Util UTIL = new CS_Util();
		UTIL.myLog(TAG , dbMsg);
	}

	public static void myErrorLog(String TAG , String dbMsg) {
		CS_Util UTIL = new CS_Util();
		UTIL.myErrorLog(TAG , dbMsg);
	}
}


/**
 * Socket.IO-client Java		    								https://github.com/socketio/socket.io-client-java
 * 2014-01-11		Androidでsocket.io											https://kinjouj.github.io/2014/01/android-socketio.html
 * 2012年12月30日	リアルタイム通信へ挑戦 														http://blog.shonanshachu.com/2012/12/android.html
 * 2017年03月16日	Android とNode.js とsocket.io　簡単なチャットやり取り		 https://qiita.com/sirokitune999/items/5c058873e4f7bff2db1f
 * Native Socket.IO and Android		https://socket.io/blog/native-socket-io-and-android/
 *
 */