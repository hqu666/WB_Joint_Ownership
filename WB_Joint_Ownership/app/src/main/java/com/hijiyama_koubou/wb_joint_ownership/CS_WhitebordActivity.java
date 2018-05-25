package com.hijiyama_koubou.wb_joint_ownership;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Build;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import io.socket.IOAcknowledge;
import io.socket.IOCallback;
import io.socket.SocketIO;
import io.socket.SocketIOException;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URISyntaxException;

import static android.content.ContentValues.TAG;

/*
５/２４課題
	htmlとの双方にカラーピック作成＞＞双方の差異確認
	カラーが渡せるまで、双方その時点の選択値で
	  function onColorUpdate(e) に直接送信

	太さ　も同様
	文字送信

	URL
	をtoolbarに表示
	Naitive間通信
* */

public class CS_WhitebordActivity extends Activity {             //AppCompatActivity
	private CS_CanvasView wb_whitebord;        //ホワイトボード        CS_CanvasView
	private ImageButton wb_all_clear_bt;        //全消去
	private ImageButton wb_mode_bt;                    //編修
	private	Button wb_line_width_bt;			//太さ選択
	private ImageButton wb_color_bt ;			//色選択
	private TextView wb_info_tv ;			//情報表示

	public int selectWidth = 5;
	public int selectColor = Color.BLACK;
	private	ColorPickerDialog mColorPickerDialog;
	/////SocketIO////Androidでsocket.io		  https://kinjouj.github.io/2014/01/android-socketio.html
	Handler mHandler;
	//	ArrayAdapter< string > mAdapter;
//	SocketThread mThread;
	private boolean mTyping = false;
	private Handler mTypingHandler = new Handler();
	private String mUsername;
	private Socket mSocket;
	private Boolean isConnected = true;
	private Boolean drawing;

	public class SocketIOData {
		public float x0;
		public float y0;
		public float x1;
		public float y1;
		public int color;
	}                // drawLine(data.x0 * w, data.y0 * h, data.x1 * w, data.y1 * h, data.color); に渡すデータ

	public SocketIOData _SocketIOData;
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
			wb_line_width_bt = ( Button ) findViewById(R.id.wb_line_width_bt);			//太さ選択
			wb_color_bt = ( ImageButton ) findViewById(R.id.wb_color_bt);			//色選択
			wb_info_tv = ( TextView ) findViewById(R.id.wb_info_tv);			//情報表示

			/////SocketIO///////////////////////////////////
			mSocket = getSocket();

			mSocket.on(Socket.EVENT_CONNECT , onConnect);
			mSocket.on(Socket.EVENT_DISCONNECT , onDisconnect);
			mSocket.on(Socket.EVENT_CONNECT_ERROR , onConnectError);
			mSocket.on(Socket.EVENT_CONNECT_TIMEOUT , onConnectError);
			mSocket.on("new message" , onNewMessage);
			mSocket.on("user joined" , onUserJoined);
			mSocket.on("user left" , onUserLeft);
			mSocket.on("typing" , onTyping);
			mSocket.on("stop typing" , onStopTyping);
			mSocket.connect();

			_SocketIOData = new SocketIOData();
			_SocketIOData.color = selectColor;
			;
//			mHandler = new Handler();
//		mAdapter = new ArrayAdapter< string >(this , android.R.layout.simple_list_item_1);
			//色選択
			wb_color_bt.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
				final String TAG = "wb_color_bt[WB]";
				String dbMsg = "";
				try {
					mColorPickerDialog = new ColorPickerDialog(CS_WhitebordActivity.this,
						new ColorPickerDialog.OnColorChangedListener() {
							@Override
							public void colorChanged(int color) {
								selectColor = color;
								wb_whitebord.setPenColor(selectColor);
							}
						},selectColor);
					mColorPickerDialog.show();
					myLog(TAG , dbMsg);
				} catch (Exception er) {
					myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
				}
				}
			});

			wb_line_width_bt.setOnClickListener(new View.OnClickListener() {    			//太さ選択
				@Override
				public void onClick(View v) {
				final String TAG = "wb_color_bt[WB]";
				String dbMsg = "";
				try {
					final CharSequence[] items = {"1","5","10","20","50"};
					dbMsg += ">>" + items.length + "件";
					AlertDialog.Builder listDlg = new AlertDialog.Builder(CS_WhitebordActivity.this);
					listDlg.setTitle("タップして選択");
					listDlg.setItems(items , new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog , int which) {
							// リスト選択時の処理
							// which は、選択されたアイテムのインデックス
							selectWidth = Integer.parseInt( items[which]+"");            //	editView.getText().toString();
							if(selectWidth<1){
								selectWidth = 1;
							}
							wb_whitebord.setPenWidth(selectWidth);
						}
					});
					listDlg.create().show();                // 表示					myLog(TAG , dbMsg);
				} catch (Exception er) {
					myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
					//android.view.WindowManager$BadTokenException: Unable to add window -- token null is not valid; is your activity running?
				}
				}
			});


			wb_all_clear_bt.setOnClickListener(new View.OnClickListener() {        //全消去
				@Override
				public void onClick(View v) {
					final String TAG = "wb_all_clear_bt[WB]";
					String dbMsg = "";
					try {
						if ( wb_whitebord != null ) {
							wb_whitebord.clearAll();
						}
						mSocket.emit("allclear" , "");     //共有webページに全消去命令送信
						myLog(TAG , dbMsg);
					} catch (Exception er) {
						myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
					}
				}
			});

			wb_whitebord.setOnTouchListener(new View.OnTouchListener() {
				@Override
				public boolean onTouch(View v , MotionEvent event) {
					final String TAG = "onTouch[WB]";
					String dbMsg = "";
					try {
						int action = event.getAction();
						dbMsg = "action=" + action;
						float eventX = event.getX();
						float eventY = event.getY();
						dbMsg += "(" + eventX + " , " + eventY + ")";
						switch ( action ) {
							case MotionEvent.ACTION_DOWN:     //0
								drawing = true;
								_SocketIOData.x0 = eventX;
								_SocketIOData.y0 = eventY;
								break;
							case MotionEvent.ACTION_MOVE:     //2
								if ( drawing ) {
									drawLine(_SocketIOData.x0 , _SocketIOData.y0 , eventX , eventY , _SocketIOData.color);
									_SocketIOData.x0 = eventX;
									_SocketIOData.y0 = eventY;
								}
								break;
							case MotionEvent.ACTION_UP:    //1
								if ( drawing ) {
									drawing = false;
									drawLine(_SocketIOData.x0 , _SocketIOData.y0 , eventX , eventY , _SocketIOData.color);
								}
								break;
						}
						myLog(TAG , dbMsg);
					} catch (Exception er) {
						myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
					}
					return false;
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
//			if ( mThread == null ) {
//				mThread = new SocketThread();
//				mThread.start();
//			}
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
//			if ( mThread != null ) {
//				mThread.cancel();
//				mThread = null;
//			}
			myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
	}

	@Override
	public boolean onKeyDown(int keyCode , KeyEvent event) {
		final String TAG = "onKeyDown[WA]";
		String dbMsg = "";
		boolean retBool = true;
		try {
			dbMsg = "keyCode=" + keyCode;//+",getDisplayLabel="+String.valueOf(event.getDisplayLabel())+",getAction="+event.getAction();////////////////////////////////
			switch ( keyCode ) {    //キーにデフォルト以外の動作を与えるもののみを記述★KEYCODE_MENUをここに書くとメニュー表示されない
//				case KeyEvent.KEYCODE_DPAD_UP:        //マルチガイド上；19
//					//	wZoomUp();						//ズームアップして上限に達すればfalse
//					if ( !myNFV_S_Pref.getBoolean("prefKouseiD_PadUMU" , false) ) {        //キーの利用が無効になっていたら
//						pNFVeditor.putBoolean("prefKouseiD_PadUMU" , true);            //キーの利用を有効にして
//					}
//				case KeyEvent.KEYCODE_DPAD_DOWN:    //マルチガイド下；20
//					//	wZoomDown();					//ズームダウンして下限に達すればfalse
//					if ( !myNFV_S_Pref.getBoolean("prefKouseiD_PadUMU" , false) ) {        //キーの利用が無効になっていたら
//						pNFVeditor.putBoolean("prefKouseiD_PadUMU" , true);            //キーの利用を有効にして
//					}
//				case KeyEvent.KEYCODE_DPAD_LEFT:    //マルチガイド左；21
//					wForward();                        //ページ履歴で1つ後のページに移動する					return true;
//					if ( !myNFV_S_Pref.getBoolean("prefKouseiD_PadUMU" , false) ) {        //キーの利用が無効になっていたら
//						pNFVeditor.putBoolean("prefKouseiD_PadUMU" , true);            //キーの利用を有効にして
//					}
//				case KeyEvent.KEYCODE_DPAD_RIGHT:    //マルチガイド右；22
//					wGoBack();                    //ページ履歴で1つ前のページに移動する
//					if ( !myNFV_S_Pref.getBoolean("prefKouseiD_PadUMU" , false) ) {        //キーの利用が無効になっていたら
//						pNFVeditor.putBoolean("prefKouseiD_PadUMU" , true);            //キーの利用を有効にして
//					}
//				case KeyEvent.KEYCODE_VOLUME_UP:    //24
//					wZoomUp();                        //ズームアップして上限に達すればfalse
//				case KeyEvent.KEYCODE_VOLUME_DOWN:    //25
//					wZoomDown();                    //ズームダウンして下限に達すればfalse
				case KeyEvent.KEYCODE_BACK:            //4KEYCODE_BACK :keyCode；09SH: keyCode；4,event=KeyEvent{action=0 code=4 repeat=0 meta=0 scancode=158 mFlags=72}
					callQuit();
				default:
					retBool = false;
			}
			myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
		return retBool;
	}

	public void callQuit() {
		final String TAG = "callQuit[WA]";
		String dbMsg = "";
		try {
			mSocket.disconnect();

			mSocket.off(Socket.EVENT_CONNECT , onConnect);
			mSocket.off(Socket.EVENT_DISCONNECT , onDisconnect);
			mSocket.off(Socket.EVENT_CONNECT_ERROR , onConnectError);
			mSocket.off(Socket.EVENT_CONNECT_TIMEOUT , onConnectError);
			mSocket.off("new message" , onNewMessage);
			mSocket.off("user joined" , onUserJoined);
			mSocket.off("user left" , onUserLeft);
			mSocket.off("typing" , onTyping);
			mSocket.off("stop typing" , onStopTyping);

			this.finish();
			if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ) {
				finishAndRemoveTask();                      //アプリケーションのタスクを消去する事でデバッガーも停止する。
			} else {
				moveTaskToBack(true);                       //ホームボタン相当でアプリケーション全体が中断状態
			}
			myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
	}

	/////SocketIO///////////////////////////////////
	public static final String CHAT_SERVER_URL = "http://ec2-52-197-173-40.ap-northeast-1.compute.amazonaws.com:3080/";

	/**
	 * URLからSocket作成
	 * <p>
	 * 呼び出しはonCreate
	 */
	public Socket getSocket() {
		final String TAG = "getSocket[WA]";
		String dbMsg = "";
		Socket _mSocket;
		try {
			wb_info_tv.setText(CHAT_SERVER_URL);			//情報表示
			_mSocket = IO.socket(CHAT_SERVER_URL);
		} catch (URISyntaxException er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
			throw new RuntimeException(er);
		}
		return _mSocket;
	}

	public void drawLine(float x0 , float y0 , float x1 , float y1 , int color) {
		final String TAG = "drawLine[WA]";
		String dbMsg = "";
		try {
			int cw = wb_whitebord.getWidth();
			int ch = wb_whitebord.getHeight();
			dbMsg = "whitebord[" + cw + " , " + ch + "]";
			SocketIOData sioData = new SocketIOData();
			sioData.x0 = x0;	// / cw;
			sioData.y0 = y0;	// / ch;
			sioData.x1 = x1;	//	 / cw;
			sioData.y1 = y1;	// / ch;
			sioData.color = color;
			dbMsg += ",(" + sioData.x0 + " , " + sioData.y0 + ")～(" + sioData.x1 + " , " + sioData.y1 + ")" + sioData.color;
			mSocket.emit("drawing" ,sioData);     //共有webページに全消去命令送信           { x0,y0 ,x1,y1,color}
			myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
			throw new RuntimeException(er);
		}
	}

	private void addLog(String message) {
		final String TAG = "addLog[WA]";
		String dbMsg = "";
		try {
//			mMessages.add(new Message.Builder(Message.TYPE_LOG).message(message).build());
//			mAdapter.notifyItemInserted(mMessages.size() - 1);
//			scrollToBottom();
			myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
	}

	private void addParticipantsLog(int numUsers) {
		final String TAG = "addParticipantsLog[WA]";
		String dbMsg = "";
		try {
			addLog(getResources().getQuantityString(R.plurals.message_participants , numUsers , numUsers));
			myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
	}

//	private void addMessage(String username , String message) {
//		final String TAG = "addMessage[WA]";
//		String dbMsg = "";
//		try {
//			mMessages.add(new Message.Builder(Message.TYPE_MESSAGE).username(username).message(message).build());
//			mAdapter.notifyItemInserted(mMessages.size() - 1);
//			scrollToBottom();
//			myLog(TAG , dbMsg);
//		} catch (Exception er) {
//			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
//		}
//	}

	private void addTyping(String username) {
		final String TAG = "addTyping[WA]";
		String dbMsg = "";
		try {
//			mMessages.add(new Message.Builder(Message.TYPE_ACTION).username(username).build());
//			mAdapter.notifyItemInserted(mMessages.size() - 1);
//			scrollToBottom();
			myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
	}

	private void removeTyping(String username) {
		final String TAG = "removeTyping[WA]";
		String dbMsg = "";
		try {
//			for ( int i = mMessages.size() - 1 ; i >= 0 ; i-- ) {
//				Message message = mMessages.get(i);
//				if ( message.getType() == Message.TYPE_ACTION && message.getUsername().equals(username) ) {
//					mMessages.remove(i);
//					mAdapter.notifyItemRemoved(i);
//				}
//			}
			myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
	}

	private void attemptSend() {
		final String TAG = "attemptSend[WA]";
		String dbMsg = "";
		try {
			if ( null == mUsername )
				return;
			if ( !mSocket.connected() )
				return;

			mTyping = false;

//			String message = mInputMessageView.getText().toString().trim();
//			if ( TextUtils.isEmpty(message) ) {
//				mInputMessageView.requestFocus();
//				return;
//			}
//
//			mInputMessageView.setText("");
//			addMessage(mUsername , message);
//
//			// perform the sending message attempt.
//			mSocket.emit("new message" , message);
			myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
	}

	private void startSignIn() {
		final String TAG = "startSignIn[WA]";
		String dbMsg = "";
		try {
			mUsername = null;
			///インプットダイアログに置換え
			String titolStr = "ニックネーム設定";
			String mggStr = "チャット画面に表示する名前を設定して下さい（それでログインできます。）";
			final EditText editView = new EditText(this);
			new AlertDialog.Builder(this).setIcon(android.R.drawable.ic_dialog_info).setTitle(titolStr).setMessage(mggStr).setView(editView).setPositiveButton("OK" , new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog , int whichButton) {
					mUsername = editView.getText().toString();
					if ( mUsername == null ) {
						startSignIn();
					}
				}
			}).setNegativeButton("キャンセル" , new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog , int whichButton) {
					startSignIn();
				}
			}).show();

//			Intent intent = new Intent( this.getParent(), ChatLoginActivity.class);			//で開くが Attempt to invoke virtual method 'java.lang.String android.content.Context.getPackageName()' on a null object reference
//			// getParent()    getContext()                    this.getApplicationContext()
//			startActivityForResult(intent , REQUEST_LOGIN);
			dbMsg = "mUsername=" + mUsername;
			myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
	}

	private void leave() {
		final String TAG = "leave[WA]";
		String dbMsg = "";
		try {
			mUsername = null;
			mSocket.disconnect();
			mSocket.connect();
			startSignIn();
			myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
	}

//	private void scrollToBottom() {
//		final String TAG = "scrollToBottom[WA]";
//		String dbMsg = "";
//		try {
//			mMessagesView.scrollToPosition(mAdapter.getItemCount() - 1);
//			myLog(TAG , dbMsg);
//		} catch (Exception er) {
//			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
//		}
//	}

	private Emitter.Listener onConnect = new Emitter.Listener() {
		@Override
		public void call(Object... args) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					final String TAG = "onConnect[WA]";
					String dbMsg = "";
					try {
						if ( !isConnected ) {
							if ( null != mUsername )
								mSocket.emit("add user" , mUsername);
							Toast.makeText(getApplicationContext() , R.string.connect , Toast.LENGTH_LONG).show();
							isConnected = true;
						}
						myLog(TAG , dbMsg);
					} catch (Exception er) {
						myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
					}
				}
			});
		}
	};

	private Emitter.Listener onDisconnect = new Emitter.Listener() {
		@Override
		public void call(Object... args) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					final String TAG = "onDisconnect[WA]";
					String dbMsg = "";
					try {
						isConnected = false;
						Toast.makeText(getApplicationContext() , R.string.disconnect , Toast.LENGTH_LONG).show();
						myLog(TAG , dbMsg);
					} catch (Exception er) {
						myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
					}
				}
			});
		}
	};

	private Emitter.Listener onConnectError = new Emitter.Listener() {
		@Override
		public void call(Object... args) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					final String TAG = "onConnectError[WA]";
					String dbMsg = "";
					try {
						Toast.makeText(getApplicationContext() , R.string.error_connect , Toast.LENGTH_LONG).show();
						myLog(TAG , dbMsg);
					} catch (Exception er) {
						myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
					}
				}
			});
		}
	};

	private Emitter.Listener onNewMessage = new Emitter.Listener() {
		@Override
		public void call(final Object... args) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					final String TAG = "onNewMessage[WA]";
					String dbMsg = "";
					try {
						JSONObject data = ( JSONObject ) args[0];
						String username;
						String message;
						try {
							username = data.getString("username");
							message = data.getString("message");
						} catch (JSONException e) {
							Log.e(TAG , e.getMessage());
							return;
						}

						removeTyping(username);
//						addMessage(username , message);
						myLog(TAG , dbMsg);
					} catch (Exception er) {
						myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
					}
				}
			});
		}
	};

	private Emitter.Listener onUserJoined = new Emitter.Listener() {
		@Override
		public void call(final Object... args) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					final String TAG = "onUserJoined[WA]";
					String dbMsg = "";
					try {
						JSONObject data = ( JSONObject ) args[0];
						String username;
						int numUsers;
						try {
							username = data.getString("username");
							numUsers = data.getInt("numUsers");
						} catch (JSONException e) {
							Log.e(TAG , e.getMessage());
							return;
						}

						addLog(getResources().getString(R.string.message_user_joined , username));
						addParticipantsLog(numUsers);
						myLog(TAG , dbMsg);
					} catch (Exception er) {
						myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
					}
				}
			});
		}
	};

	private Emitter.Listener onUserLeft = new Emitter.Listener() {
		@Override
		public void call(final Object... args) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					final String TAG = "onUserLeft[WA]";
					String dbMsg = "";
					try {
						JSONObject data = ( JSONObject ) args[0];
						String username;
						int numUsers;
						try {
							username = data.getString("username");
							numUsers = data.getInt("numUsers");
						} catch (JSONException e) {
							Log.e(TAG , e.getMessage());
							return;
						}

						addLog(getResources().getString(R.string.message_user_left , username));
						addParticipantsLog(numUsers);
						removeTyping(username);
						myLog(TAG , dbMsg);
					} catch (Exception er) {
						myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
					}
				}
			});
		}
	};

	private Emitter.Listener onTyping = new Emitter.Listener() {
		@Override
		public void call(final Object... args) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					final String TAG = "onTyping[WA]";
					String dbMsg = "";
					try {
						JSONObject data = ( JSONObject ) args[0];
						String username;
						try {
							username = data.getString("username");
						} catch (JSONException e) {
							Log.e(TAG , e.getMessage());
							return;
						}
						addTyping(username);
						myLog(TAG , dbMsg);
					} catch (Exception er) {
						myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
					}
				}
			});
		}
	};

	private Emitter.Listener onStopTyping = new Emitter.Listener() {
		@Override
		public void call(final Object... args) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					final String TAG = "onStopTyping[WA]";
					String dbMsg = "";
					try {
						JSONObject data = ( JSONObject ) args[0];
						String username;
						try {
							username = data.getString("username");
						} catch (JSONException e) {
							Log.e(TAG , e.getMessage());
							return;
						}
						removeTyping(username);
						myLog(TAG , dbMsg);
					} catch (Exception er) {
						myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
					}
				}
			});
		}
	};

	private Runnable onTypingTimeout = new Runnable() {
		@Override
		public void run() {
			final String TAG = "onTypingTimeout[WA]";
			String dbMsg = "";
			try {
				if ( !mTyping )
					return;

				mTyping = false;
				mSocket.emit("stop typing");
				myLog(TAG , dbMsg);
			} catch (Exception er) {
				myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
			}
		}
	};

//	SocketIO mSocket;
//	private class SocketThread extends Thread {
//		@Override
//		public void run() {
//			final String TAG = "run[WA]";
//			String dbMsg = "mSocket != null ";
//			try {
//				if ( mSocket == null ) {
////					String urlStr = "http://ec2-52-197-173-40.ap-northeast-1.compute.amazonaws.com:3080/";
////					String urlStr = "http://[socket.io.server.ip]:8080";
//					dbMsg = "urlStr= " + CHAT_SERVER_URL;
//					mSocket = new SocketIO(CHAT_SERVER_URL);
//
//					mSocket.connect(new IOCallback() {
//						@Override
//						public void onConnect() {                      //サーバとの接続が確立されたとき
//							final String TAG = "onConnect[WA]";
//							String dbMsg = "";
//							try {
//								mSocket.send("android");      								// connectしたらAndroidからsend
//								myLog(TAG , dbMsg);
//							} catch (Exception er) {
//								myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
//							}
//						}
//
//						@Override
//						public void onDisconnect() {                   			//サーバとの接続が切断されたとき
//							final String TAG = "onDisconnect[WA]";
//							String dbMsg = "";
//							try {
//								myLog(TAG , dbMsg);
//							} catch (Exception er) {
//								myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
//							}
//						}
//
//						@Override
//						public void onError(SocketIOException e) {
//							final String TAG = "onError[WA]";
//							myErrorLog(TAG , "エラー発生；" + e.getMessage());
//							//Error while handshaking
//						}
//
//						@Override
//						public void on(String eventName , IOAcknowledge ack , Object... args) {          		//イベントを受信したとき
//							final String TAG = "on[WA]";
//							String dbMsg = "";
//							try {
//								dbMsg += ".eventName=" + eventName;
//								for ( Object arg : args ) {
//									if ( !(arg instanceof JSONObject) )
//										continue;
//									JSONObject json = ( JSONObject ) arg;
//									onMessage(json , null);
//								}
//								myLog(TAG , dbMsg);
//							} catch (Exception er) {
//								myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
//							}
//						}
//
//						@Override
//						public void onMessage(String msg , IOAcknowledge ack) {
//							final String TAG = "onMessage;String[WA]";
//							String dbMsg = "";
//							try {
//								dbMsg += ".msg=" + msg;
//								update(msg);
//								myLog(TAG , dbMsg);
//							} catch (Exception er) {
//								myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
//							}
//						}
//
//						@Override
//						public void onMessage(JSONObject data , IOAcknowledge ack) {     		//JSONを受信したとき
//							final String TAG = "onMessage;JSONObject[WA]";
//							String dbMsg = "";
//							try {
//								dbMsg = "onMessage(JSON)";
//								if ( !data.has("msg") )
//									return;
//								try {
//									String msg = ( String ) data.get("msg");
//									dbMsg += ".msg=" + msg;
//									update(msg);
//								} catch (JSONException er) {
//									myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
//								}
//								myLog(TAG , dbMsg);
//							} catch (Exception er) {
//								myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
//							}
//						}
//
//						private void update(final String data) {
//							mHandler.post(new Thread() {
//								@Override
//								public void run() {
//									final String TAG = "run[WA]";
//									String dbMsg = "";
//									try {
//										dbMsg = "data~" + data;
////										mAdapter.add(data);
////										mAdapter.notifyDataSetChanged();
//										myLog(TAG , dbMsg);
//									} catch (Exception er) {
//										myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
//									}
//								}
//							});
//						}
//					});
//				}
//				myLog(TAG , dbMsg);
//			} catch (MalformedURLException er) {
//				myErrorLog(TAG , "エラー発生；" + er);
//			}
//		}
//
//		public void cancel() {
//			final String TAG = "cancel[WA]";
//			String dbMsg = "";
//			try {
//				if ( mSocket != null ) {
//					mSocket.disconnect();
//					mSocket = null;
//				}
//				myLog(TAG , dbMsg);
//			} catch (Exception er) {
//				myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
//			}
//		}
//	}
//

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
 */