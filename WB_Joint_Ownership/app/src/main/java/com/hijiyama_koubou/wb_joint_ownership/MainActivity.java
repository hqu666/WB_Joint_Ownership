package com.hijiyama_koubou.wb_joint_ownership;

import android.Manifest;
import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import org.json.JSONArray;
import org.webrtc.PeerConnection;
import org.webrtc.VideoRenderer;

import java.util.ArrayList;
import java.util.LinkedList;

import io.skyway.Peer.Browser.Canvas;
import io.skyway.Peer.Browser.MediaConstraints;
import io.skyway.Peer.Browser.MediaStream;
import io.skyway.Peer.Browser.Navigator;
import io.skyway.Peer.CallOption;
import io.skyway.Peer.MediaConnection;
import io.skyway.Peer.OnCallback;
import io.skyway.Peer.Peer;
import io.skyway.Peer.PeerError;
import io.skyway.Peer.PeerOption;

//import android.support.v4.content.ContextCompat;

/**
 * MainActivity.java
 * ECL WebRTC p2p video-chat sample
 */
public class MainActivity extends Activity {

	private ImageButton main_memu_bt;    //メニュー表示ボタン
	private Button connect_bt;                                //接続ボタン
	private ImageButton main_setting_bt;                    //設定ボタン
	private ImageButton main_quit_bt;                        //終了ボタン
	//	private ImageButton switchCameraAction;                        //カメラ切替ボタン
	private Canvas canvasMain;         //受信モニター
	private Canvas canvasSub;                                //自己モニター
	private TextView tvOwnId;                                //自己ID
	private TextView tvPartnerId;                            //接続先ID
	private TextView conect_situation_tv;                    //接続状況
	private LinearLayout wh_paret;        //ホワイトボードツールボックス
//	private LinearLayout main_subview_ll;
	private ImageButton main_c2edit_bt;        //P1の書き込み/カメラ切り替えボタン
	private ImageButton main_all_clear2_bt;        //P1の書き込み全消去ボタン
	private boolean isNowWhitebord = false;        //現在ホワイトボード
	private boolean isAddWB = false;                        //ホワイトボード追加済み

	private CS_CanvasView main_whitebord;        //ホワイトボード        CS_CanvasView
	private ImageButton main_all_clear_bt;        //全消去
	private ImageButton main_edit_bt;                    //編修
//	private ImageButton main_camera_bt;                //カメラ
	private CS_CanvasView CSCV;    			//送信画面に組み込んだホワイトボード

	private ViewFlipper mFlipper;
	private ImageButton nextButton;                 // ViewFlipperの次（右）画面
	private ImageButton previousButton;         // ViewFlipperの前（左）画面
	private Animation mAnimRightIn;
	private Animation mAnimRightOut;
	private Animation mAnimLeftIn;
	private Animation mAnimLeftOut;

	private boolean isFrontCam = true;                        //現在フロントカメラ
	private MediaConstraints constraints;                                    //LocalStreamの状況

	private static final String TAG = MainActivity.class.getSimpleName();

	// Set your APIkey and Domain
	//	private static final String DOMAIN = "com.ntt";
//	private static final String DOMAIN = "coresoft-net.co.jp";
	private Peer _peer;                                                        //Peerオブジェクト
	private MediaStream _localStream;                                            // 自分自身のMediaStreamオブジェクト
	private MediaStream _remoteStream;                                        //相手のMediaStreamオブジェクト
	private MediaConnection _mediaConnection;                                    //  MediaConnectionオブジェクト
	private boolean _bConnected;

	private Handler _handler;

	public static SharedPreferences sharedPref;
	public SharedPreferences.Editor myEditor;
	public String service_id = "";                    //サービスサーバのID
	public String peer_id;
	public String partner_id;                                                //SkyWayに接続要求する相手端末のID
	public String API_KEY = "d099e20f-d67b-48be-a040-cee55e002cfb";        //SkyWayに接続する為のAPIキー
	public String sw_secret_key = "cpaEqSH6uuVZ42QeetVPyjUkYlUDPBGW";            //SkyWayでAPIキーと合わせて発行されるシークレットキー
	public String DOMAIN = "coresoft-net.co.jp";                    //SkyWayに登録した利用可能ドメイン
	public boolean isReadPref = false;

	/**
	 * このアプリケーションの設定ファイル読出し
	 **/
	public void readPref() {
		final String TAG = "readPref[MA]";
		String dbMsg = "許諾済み";//////////////////
		try {
			if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ) {                //(初回起動で)全パーミッションの許諾を取る
				dbMsg = "許諾確認";
				String[] PERMISSIONS = {Manifest.permission.ACCESS_NETWORK_STATE , Manifest.permission.ACCESS_WIFI_STATE , Manifest.permission.INTERNET , Manifest.permission.CAMERA , Manifest.permission.MODIFY_AUDIO_SETTINGS , Manifest.permission.RECORD_AUDIO , Manifest.permission.READ_EXTERNAL_STORAGE , Manifest.permission.MODIFY_AUDIO_SETTINGS};
				boolean isNeedParmissionReqest = false;
				for ( String permissionName : PERMISSIONS ) {
					dbMsg += "," + permissionName;
					int checkResalt = checkSelfPermission(permissionName);
					dbMsg += "=" + checkResalt;
					if ( checkResalt != PackageManager.PERMISSION_GRANTED ) {
						isNeedParmissionReqest = true;
					}
				}
				if ( isNeedParmissionReqest ) {
					dbMsg += "許諾処理へ";
					requestPermissions(PERMISSIONS , REQUEST_PREF);
					return;
				}
			}
			dbMsg += ",isReadPref=" + isReadPref;
			MyPreferenceFragment prefs = new MyPreferenceFragment();
			prefs.readPref(this);
			service_id = prefs.service_id;                    //サービスサーバのID
//			dbMsg = ",service_id=" + service_id;
			peer_id = prefs.peer_id;                        //SkyWayで取得しているこの端末のID
			dbMsg += ",peer_id=" + peer_id;
			partner_id = prefs.partner_id;                    //SkyWayに接続要求する相手端末のID
			dbMsg += ",partner_id=" + partner_id;
			API_KEY = prefs.API_KEY;                        //SkyWayに接続する為のAPIキー
//			dbMsg = ",API_KEY=" + API_KEY;
			sw_secret_key = prefs.sw_secret_key;                //SkyWayでAPIキーと合わせて発行されるシークレットキー
//			dbMsg += ",sw_secret_key=" + sw_secret_key;
			DOMAIN = prefs.DOMAIN;    //SkyWayに登録した利用可能ドメイン
//			dbMsg += ",DOMAIN=" + DOMAIN;
			sharedPref = PreferenceManager.getDefaultSharedPreferences(this);            //	getActivity().getBaseContext()
			myEditor = sharedPref.edit();
			//         setSaveParameter();                 //保存可能上限の確認と修正

			isReadPref = true;
			if ( _peer == null ) {
				peer_id = "";
				dbMsg += ">>" + peer_id;
				makeNewPear(peer_id);
			}
			myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
	}

	/**
	 * アプリ起動時の入り口；終了処理後のonDestroyの後でも再度、呼び出されるので構成パーツのID取得に留める
	 * ①new Peer(this , option);でサーバに接続
	 * ② PeerEventEnum.OPENでstartLocalStream()へ				// _peer.on.OPENに入らない場合は SkywayにApicationを追加する時；権限(APIキー認証を利用する)はOFFに
	 **/
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		final String TAG = "onCreate[MA]";
		String dbMsg = "";
		try {

			int orientation = getResources().getConfiguration().orientation;
			dbMsg += "orientation=" + orientation;
			if ( orientation == Configuration.ORIENTATION_LANDSCAPE ) {
				dbMsg += "=横向き";
			} else if ( orientation == Configuration.ORIENTATION_PORTRAIT ) {
				dbMsg += "=縦向き";
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);      //横向きに修正
			}
			Window wnd = getWindow();
			wnd.addFlags(Window.FEATURE_NO_TITLE);
			setContentView(R.layout.activity_main);

			final Activity activity = this;
			canvasMain = ( Canvas ) findViewById(R.id.svRemoteView);         //受信モニター
			canvasSub = ( Canvas ) findViewById(R.id.svLocalView);         //送信映像;自己モニター
			tvOwnId = ( TextView ) findViewById(R.id.tvOwnId);        //自己ID
			tvPartnerId = ( TextView ) findViewById(R.id.tvPartnerId);    //接続先ID
			connect_bt = ( Button ) findViewById(R.id.connect_bt);                    //接続ボタン
			conect_situation_tv = ( TextView ) findViewById(R.id.conect_situation_tv);    //接続状況
			main_setting_bt = ( ImageButton ) findViewById(R.id.main_setting_bt);                    //設定ボタン
//			switchCameraAction = ( ImageButton ) findViewById(R.id.switchCameraAction);    //カメラ切替ボタン
			 main_memu_bt = ( ImageButton ) findViewById(R.id.main_memu_bt);    //メニュー表示ボタン
			main_memu_bt.setVisibility(View.GONE);                                            //メニュー;非表示

//			main_subview_ll = ( LinearLayout ) findViewById(R.id.main_subview_ll);

			mFlipper = ( ViewFlipper ) findViewById(R.id.flipper);
			nextButton = ( ImageButton ) findViewById(R.id.vf_next_bt);                 // ViewFlipperの次（右）画面
			previousButton = ( ImageButton ) findViewById(R.id.vf_previous_bt);         // ViewFlipperの前（左）画面

			wh_paret = ( LinearLayout ) findViewById(R.id.wh_paret);        //ホワイトボードツールボックス
//			wh_paret.setVisibility(View.GONE);        //ホワイトボードツールボックス;非表示
			main_all_clear2_bt = ( ImageButton ) findViewById(R.id.main_all_clear2_bt);        //P1の書き込み全消去ボタン
			main_all_clear2_bt.setVisibility(View.GONE);                                                //P1の書き込み全消去ボタン;非表示
			main_c2edit_bt = ( ImageButton ) findViewById(R.id.main_c2edit_bt);        //P1の書き込み/カメラ切り替えボタン
			//
			main_whitebord = ( CS_CanvasView ) findViewById(R.id.main_whitebord);        //ホワイトボード             	Canvas	     CS_CanvasView
			main_all_clear_bt = ( ImageButton ) findViewById(R.id.main_all_clear_bt);        //全消去
			main_edit_bt = ( ImageButton ) findViewById(R.id.main_edit_bt);                    //編修
//			main_camera_bt = ( ImageButton ) findViewById(R.id.main_camera_bt);                //カメラ

			main_quit_bt = ( ImageButton ) findViewById(R.id.main_quit_bt);    //終了
			_handler = new Handler(Looper.getMainLooper());


//			try {
			readPref();
//				Thread.sleep(1000); //3000ミリ秒Sleepする
//			} catch (InterruptedException e) {
//			}
			myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
	}

	/**
	 * 全リソースの読み込みが終わってフォーカスが当てられた時
	 */
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		final String TAG = "onStart[MA]";
		String dbMsg = "hasFocus=" + hasFocus;
		try {
			laterCreate();
			myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
		final String TAG = "onStart[MA]";
		String dbMsg = "";
		try {
			// Disable Sleep and Screen Lock
			Window wnd = getWindow();
			wnd.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
			wnd.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
			myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
	}

	/**
	 * この時点では_peer.on.OPENに至っていない
	 */
	@Override
	protected void onResume() {
		super.onResume();
		final String TAG = "onResume[MA]";
		String dbMsg = "";
		try {
			int orientation = getResources().getConfiguration().orientation;
			dbMsg += "orientation=" + orientation;
			if ( orientation == Configuration.ORIENTATION_LANDSCAPE ) {
				dbMsg += "=横向き";
			} else if ( orientation == Configuration.ORIENTATION_PORTRAIT ) {
				dbMsg += "=縦向き";
			}

			dbMsg += ",peer_id=" + peer_id;
			if ( peer_id == null ) {
				peer_id = ( String ) tvOwnId.getText();
				dbMsg += ">>" + peer_id;
			}

			setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);            //APIL1;// Set volume control stream type to WebRTC audio.
			myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
	}

	@Override
	protected void onPause() {
		final String TAG = "onPause[MA]";
		String dbMsg = "";
		try {
			setVolumeControlStream(AudioManager.USE_DEFAULT_STREAM_TYPE);                //APIL1;// Set default volume control stream type.
			myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
		super.onPause();
	}

	@Override
	protected void onStop() {
		final String TAG = "onStop[MA]";
		String dbMsg = "";
		try {
			Window wnd = getWindow();
			wnd.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
			wnd.clearFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);                    // Enable Sleep and Screen Lock
			myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
		super.onStop();
	}

	@Override
	protected void onDestroy() {
//		destroyPeer();
		reConect();
		super.onDestroy();
	}

	static final int REQUEST_PREF = 100;                          //Prefarensからの戻り
	static final int REQUEST_SWOPEN = REQUEST_PREF + 1;        //skyway接続開始

	/**
	 * Cameraパーミッションが通った時点でstartLocalStream
	 */
	@Override
	public void onRequestPermissionsResult(int requestCode , String permissions[] , int[] grantResults) {
		final String TAG = "onRequestPermissionsResult[MA]";
		String dbMsg = "";
		try {
			dbMsg = "requestCode=" + requestCode;
			switch ( requestCode ) {
				case REQUEST_PREF:
					readPref();        //ループする？
					if ( _peer == null ) {
						makeNewPear("");
					}
//					String titolStr ="設定を作成します";
//					String mggStr ="バックキーなどで終了して再起動してください。";
//					messageShow( titolStr ,  mggStr);
//								Intent intent=new Intent();
//			intent.setClass(this, this.getClass());
//			this.startActivity(intent);
//			callQuit();
					break;
				case REQUEST_SWOPEN:            //☆peer.onのOPEN；  startLocalStreamを二重発生させるとクラッシュするので重複処理に注意
					dbMsg += "grantResults=" + grantResults.length + "件";
					dbMsg += ",[0]=" + grantResults[0];
					dbMsg += ",PERMISSION_GRANTED=" + PackageManager.PERMISSION_GRANTED;
					if ( grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED ) {
						startLocalStream();
					} else {
						Toast.makeText(this , "Failed to access the camera and microphone.\nclick allow when asked for permission." , Toast.LENGTH_LONG).show();
					}
					break;
			}


			myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
	}

	@Override
	public boolean onKeyDown(int keyCode , KeyEvent event) {
		final String TAG = "onKeyDown";
		String dbMsg = "開始";
		try {
			dbMsg = "keyCode=" + keyCode;//+",getDisplayLabel="+String.valueOf(MyEvent.getDisplayLabel())+",getAction="+MyEvent.getAction();////////////////////////////////
			myLog(TAG , dbMsg);
			switch ( keyCode ) {    //キーにデフォルト以外の動作を与えるもののみを記述★KEYCODE_MENUをここに書くとメニュー表示されない
				case KeyEvent.KEYCODE_BACK:            //4KEYCODE_BACK :keyCode；09SH: keyCode；4,MyEvent=KeyEvent{action=0 code=4 repeat=0 meta=0 scancode=158 mFlags=72}
//                    if (fragmentNo == mainFragmentNo) {
					callQuit();
//                    } else {
//                        callMain();
//                    }
					return true;
//				case KeyEvent.KEYCODE_HOME:            //3
////					ComponentName compNmae = startService(new Intent(MainActivity.this, NotificationChangeService.class));                           //     makeNotificationを持つクラスへ
////					dbMsg = "compNmae=" + compNmae;     //compNmae=ComponentInfo{hijiyama_koubou.com.residualquantityofthesleep/hijiyama_koubou.com.residualquantityofthesleep.NotificationChangeService}
////						NotificationManager mNotificationManager = ( NotificationManager ) mainActivity.getSystemService(NOTIFICATION_SERVICE);
////						mNotificationManager.cancel(NOTIFICATION_ID);            //サービスの停止時、通知内容を破棄する
//					myLog(TAG, dbMsg);
//					return true;
				default:
					return false;
			}
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
			return false;
		}
	}
	///////////////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * onCreateに有ったイベントなどの処理パート
	 * onCreateは終了処理後のonDestroyの後でも再度、呼び出されるので実データの割り付けなどを分離する
	 */
	public void laterCreate() {
		final String TAG = "laterCreate[MA]";
		String dbMsg = "";
		try {
			int orientation = getResources().getConfiguration().orientation;
			dbMsg += "orientation=" + orientation;
			if ( orientation == Configuration.ORIENTATION_LANDSCAPE ) {
				dbMsg += "=横向き";
			} else if ( orientation == Configuration.ORIENTATION_PORTRAIT ) {
				dbMsg += "=縦向き";
			}
			//ランタイムパーミッション処理は 	_peer.onのOPENで行う；  startLocalStreamを二重発生させるとクラッシュする

			main_setting_bt.setOnClickListener(new View.OnClickListener() {           //設定ボタン
				@Override
				public void onClick(View v) {
					final String TAG = "main_setting_bt[MA.onCr]";
					String dbMsg = "";
					try {
						Intent settingsIntent = new Intent(MainActivity.this , MyPreferencesActivty.class);
						//    startActivity( settingsIntent );
						startActivityForResult(settingsIntent , REQUEST_PREF);//		StartActivity(intent);
						myLog(TAG , dbMsg);
					} catch (Exception er) {
						myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
					}
				}
			});

			// Set GUI event listeners//////////////////////////////////////////////////  Set Peer event callbacks OPEN //
			connect_bt.setEnabled(true);                                             //接続ボタン
			connect_bt.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					final String TAG = "connect_bt[MA.onCr]";
					String dbMsg = "";
					try {
						v.setEnabled(false);
						dbMsg = "_bConnected=" + _bConnected;
						if ( !_bConnected ) {
							showPeerIDs();                            // Select remote peer & make a call
						} else {
							closeRemoteStream();                        // Hang up a call
							_mediaConnection.close();
							tvPartnerId.setText(getResources().getString(R.string.non_conect_now));
						}
						v.setEnabled(true);
						myLog(TAG , dbMsg);
					} catch (Exception er) {
						myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
					}
				}
			});

//			switchCameraAction.setText(getResources().getString(R.string.camera_switch_caption));       //カメラ切替ボタン
//			switchCameraAction.setOnClickListener(new View.OnClickListener() {
//				@Override
//				public void onClick(View v) {
//					final String TAG = "switchCameraAction[MA.onCr]";
//					String dbMsg = "";
//					try {
//						if ( null != _localStream ) {
//							Boolean result = _localStream.switchCamera();//常にFalse?
//							if ( true == result ) {
//								dbMsg += "Success";
//							} else {
//								dbMsg += "Failed";
//							}
////							MediaConstraints.CameraPositionEnum cameraPosition = constraints.cameraPosition;
////							dbMsg += ";cameraPosition" + cameraPosition.toString();
////							if ( cameraPosition==MediaConstraints.CameraPositionEnum.FRONT) {            				//cameraPositionFRONT
//							String cameraPosition = constraints.cameraPosition.toString();
//							dbMsg += ";cameraPosition=" + cameraPosition;
////							if ( cameraPosition.equals("cameraPositionFRONT")) {            				//
//							dbMsg += ";isFrontCam=" + isFrontCam;
////							if ( isFrontCam ) {                                   //現在フロント(サブ)カメラ//☆切替わるまでタイムラグがあるので見なし切替；切り替わった時のイベントは？
////								switchCameraAction.setText(getResources().getString(R.string.camera_switch_caption2sub));
////								isFrontCam = false;
////							} else {
////								switchCameraAction.setText(getResources().getString(R.string.camera_switch_caption2main));
////								isFrontCam = true;
////							}
//						}
//						myLog(TAG , dbMsg);
//					} catch (Exception er) {
//						myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
//					}
//				}
//			});

			main_quit_bt.setOnClickListener(new View.OnClickListener() {            //終了
				@Override
				public void onClick(View v) {
					final String TAG = "main_quit_bt[MA.onCr]";
					String dbMsg = "";
					try {
						callQuit();
						myLog(TAG , dbMsg);
					} catch (Exception er) {
						myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
					}
				}
			});
			//////画像送信テスト///////////////////////////////////////////////
//			canvasSub.setOnClickListener(new View.OnClickListener() {
//				@Override
//				public void onClick(View v) {
//					final String TAG = "canvasSub[MA.onCr]";
//					String dbMsg = "isNowWhitebord="+isNowWhitebord;
//					try {
//						if (! isNowWhitebord ) {
//							sendVeiwChange();
//						}
//						myLog(TAG , dbMsg);
//					} catch (Exception er) {
//						myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
//					}
//				}
//			});


//			main_camera_bt.setOnClickListener(new View.OnClickListener() {            //カメラ
//				@Override
//				public void onClick(View v) {
//					final String TAG = "main_camera_bt[MA.onCr]";
//					String dbMsg = "";
//					try {
//						sendVeiwChange();
//						myLog(TAG , dbMsg);
//					} catch (Exception er) {
//						myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
//					}
//				}
//			});

			ImageButton test_bt1 = ( ImageButton ) findViewById(R.id.test_bt1);
			test_bt1.setOnClickListener(new View.OnClickListener() {            //終了
				@Override
				public void onClick(View v) {
					final String TAG = "test_bt1[MA.onCr]";
					String dbMsg = "";
					try {
						ImageButton bt = ( ImageButton ) v;
						Bitmap bmp = (( BitmapDrawable ) bt.getDrawable()).getBitmap();
						dbMsg = "bmp[" + bmp.getWidth() + "×" + bmp.getHeight() + "]";
						whiteBordDrow(bmp);
						myLog(TAG , dbMsg);
					} catch (Exception er) {
						myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
					}
				}
			});

			//ViewFlipper /////http://android-dev-talk.blogspot.jp/2012/06/viewflipperview.html
			// Load animation
			mAnimRightIn = AnimationUtils.loadAnimation(this , R.anim.right_in);
			mAnimRightOut = AnimationUtils.loadAnimation(this , R.anim.right_out);
			mAnimLeftIn = AnimationUtils.loadAnimation(this , R.anim.left_in);
			mAnimLeftOut = AnimationUtils.loadAnimation(this , R.anim.left_out);
//			mFlipper.setAutoStart(true);     //自動でスライドショーを開始
//			mFlipper.setFlipInterval(2000);  //更新間隔(ms単位)
//
//			int pages[] = {R.id.page1 , R.id.page2};
//			for ( int page : pages ) {
//				LinearLayout layout = ( LinearLayout ) findViewById(page);
////				ImageView advertisement_l_IV = ( ImageView ) layout.findViewById(R.id.advertisement_l_IV);
////				ImageView advertisement_r_IV = ( ImageView ) layout.findViewById(R.id.advertisement_r_IV);
//				switch ( page ) {
//					case R.id.page1:
////						advertisement_l_IV.setImageResource(R.drawable.advertisement1);
////						advertisement_r_IV.setImageResource(R.drawable.sub_advertisement2);
//						break;
//					case R.id.page2:
//						int hbW = main_whitebord.getWidth();
//						dbMsg += ",whitebord{" + hbW;
//						int hbH = main_whitebord.getHeight();
//						dbMsg += "×" + hbH + "]";
//						break;
//				}
//			}
			nextButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					final String TAG = "nextButton[MA.onCr]";
					String dbMsg = "ホワイトボードへ";
					try {
						if ( !mFlipper.isFlipping() ) {
							mFlipper.setInAnimation(mAnimRightIn);
							mFlipper.setOutAnimation(mAnimLeftOut);
							mFlipper.showNext();
//						isFlipper =false;
						} else {
							mFlipper.stopFlipping();
//							toWhiteBorrb();

						}

						myLog(TAG , dbMsg);
					} catch (Exception er) {
						myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
					}
				}
			});

			previousButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					final String TAG = "previousButton[MA.onCr]";
					String dbMsg = "ビデオチャットへ";
					try {
						if ( !mFlipper.isFlipping() ) {
							mFlipper.setInAnimation(mAnimLeftIn);
							mFlipper.setOutAnimation(mAnimRightOut);
							mFlipper.showPrevious();
//						isFlipper =false;
						} else {
							mFlipper.stopFlipping();
//							toVideoChat();
						}
						myLog(TAG , dbMsg);
					} catch (Exception er) {
						myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
					}
				}
			});

			main_all_clear_bt.setOnClickListener(new View.OnClickListener() {        //全消去
				@Override
				public void onClick(View v) {
					final String TAG = "main_all_clear_bt[MA.onCr]";
					String dbMsg = "";
					try {
						if ( main_whitebord != null ) {
							main_whitebord.clearAll();
//							CS_CanvasView CCV = new CS_CanvasView(MainActivity.this);
//							CCV.clearAll();
						}
						myLog(TAG , dbMsg);
					} catch (Exception er) {
						myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
					}
				}
			});

			main_edit_bt.setOnClickListener(new View.OnClickListener() {                    //編修
				@Override
				public void onClick(View v) {
					final String TAG = "main_edit_bt[MA.onCr]";
					String dbMsg = "";
					try {
						if ( main_whitebord != null ) {
							main_whitebord.startFreeHand();
//							CS_CanvasView CCV = new CS_CanvasView(MainActivity.this);
//							CCV.startFreeHand();
						}
						myLog(TAG , dbMsg);
					} catch (Exception er) {
						myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
					}
				}
			});

			main_c2edit_bt.setOnClickListener(new View.OnClickListener() {                    //page1で送信画面のモード切替
				@Override
				public void onClick(View v) {                //P1の書き込み/カメラ切り替えボタン
					final String TAG = "main_edit_bt[MA.onCr]";
					String dbMsg = "P1の書き込み/カメラ切り替え";
					try {
						if ( isNowWhitebord ) {                                                //現在ホワイトボード
							toVideoChat();
							main_all_clear2_bt.setVisibility(View.GONE);                //全消去;非表示
							connect_bt.setVisibility(View.VISIBLE);                        //接続ボタン ;表示
							main_setting_bt.setVisibility(View.VISIBLE);                //設定ボタン;表示
							main_quit_bt.setVisibility(View.VISIBLE);                    //終了ボタン ;表示
						} else {
							toWhiteBorrb();
							connect_bt.setVisibility(View.GONE);                        //接続ボタン ;非表示
							main_setting_bt.setVisibility(View.GONE);                    //設定ボタン;非表示
							main_quit_bt.setVisibility(View.GONE);                    //終了ボタン ;非表示
							main_all_clear2_bt.setVisibility(View.VISIBLE);                //全消去;表示
						}

						myLog(TAG , dbMsg);
					} catch (Exception er) {
						myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
					}
				}
			});

			main_all_clear2_bt.setOnClickListener(new View.OnClickListener() {        //P1の書き込み全消去ボタン
				@Override
				public void onClick(View v) {
					final String TAG = "main_all_clear_bt[MA.onCr]";
					String dbMsg = "";
					try {
						if ( CSCV != null ) { ;    			//送信画面に組み込んだホワイトボード
							CSCV.clearAll();
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

	public void callQuit() {
		final String TAG = "callQuit[MA]";
		String dbMsg = "";
		try {
			destroyPeer();
			sharedPref = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);            //	getActivity().getBaseContext()
			myEditor = sharedPref.edit();
			myEditor.putString("peer_id_key" , "");      //使用した
			boolean kakikomi = myEditor.commit();
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

	/**
	 * 実験中；接続中にonDestroyが発生しても  peer_idからPeerオブジェクトを復元できれば画面の縦横切り替えを許諾できる
	 * ☆回転時はPeerがNullになっている
	 */
	public void reConect() {
		final String TAG = "reConect[MA]";
		String dbMsg = "";
		try {
//			destroyPeer();                    //回転時はPeerを保持出来たらここにマスク

			myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
	}

	////この端末のSkywayセッション/////////////////////////////////////////////////////////////////////////////////

	/**
	 * SkyWay接続トークンの作成
	 * <p>
	 * ☆SkywayにApicationを追加する時；権限(APIキー認証を利用する)はOFFにしないとOPENに入らない
	 */
	public void makeNewPear(String peerId) {
		final String TAG = "makeNewPear[MA]";
		String dbMsg = "";
		try {
//			if ( _peer == null ) {
//				peerId = "";
//			}
			dbMsg += "peerId=" + peerId;                 //501SO;_peer=io.skyway.Peer.Peer@4dc2c56	//701SH;io.skyway.Peer.Peer@4bb9f90  / bed288e/xKDnHnjB3n7gbvMZ   /
			if ( peerId.equals("") ) {
				dbMsg += ">>生成";
				// Initialize Peer ////////////////////////////////////////////////
				PeerOption option = new PeerOption();
				option.key = API_KEY;
				option.domain = DOMAIN;
				option.debug = Peer.DebugLevelEnum.ALL_LOGS;            //3		//https://webrtc.ecl.ntt.com/android-tutorial.html     Peerオブジェクトの作成
//			option.turn = true;
				dbMsg += "option.key=" + option.key + " ,domain= " + option.domain;
				_peer = new Peer(this , null , option);                             //①ここでサーバに接続　　、第二引数；peerId をNullにするとピア ID をサーバから取得する
				dbMsg += "  ,_peer=" + _peer;                 //501SO;_peer=io.skyway.Peer.Peer@4dc2c56	//701SH;io.skyway.Peer.Peer@4bb9f90  / bed288e/xKDnHnjB3n7gbvMZ   /
			}
			// Set Peer event callbacks OPEN//////////////////////////////////////////////// Initialize Peer //
			_peer.on(Peer.PeerEventEnum.OPEN , new OnCallback() {                        //②接続が成功すればここに
				@Override
				public void onCallback(Object object) {
					final String TAG = "_peer.on.OPEN[MA.onCr]";
					String dbMsg = "";
					try {
						peer_id = ( String ) object;                    // Show my ID		この端末の接続ID
						dbMsg = "peer_id=" + peer_id;
						tvOwnId.setText(peer_id);
						////_peer.on.Error[MA.onCr]: io.skyway.Peer.PeerError;type=UNKNOWN,typeString=network-error,message=No route to host,exception=null
						if ( peer_id != null ) {
							sharedPref = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);            //	getActivity().getBaseContext()
							myEditor = sharedPref.edit();
							myEditor.putString("peer_id_key" , peer_id);
							boolean kakikomi = myEditor.commit();
							dbMsg = dbMsg + ",書込み=" + kakikomi;//////////////////
							conect_situation_tv.setText("SkyWayからPeerIDを取得しました。");
						} else {
							conect_situation_tv.setText("SkyWayに接続できませんでした。少し待ってアプリを再起動してください。");
						}
						if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ) {                        // Request permissions
							String[] PERMISSIONS = {Manifest.permission.ACCESS_NETWORK_STATE , Manifest.permission.ACCESS_WIFI_STATE , Manifest.permission.INTERNET , Manifest.permission.CAMERA , Manifest.permission.MODIFY_AUDIO_SETTINGS , Manifest.permission.RECORD_AUDIO , Manifest.permission.READ_EXTERNAL_STORAGE , Manifest.permission.MODIFY_AUDIO_SETTINGS};
							requestPermissions(PERMISSIONS , REQUEST_SWOPEN);        //初回起動で全パーミッションの許諾を取る
						} else {
							startLocalStream();                        //ランタイムパーミッション以前の場合； Get a local MediaStream & show it
						}
						myLog(TAG , dbMsg);
					} catch (Exception er) {
						myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
					}
				}
			});

			_peer.on(Peer.PeerEventEnum.CALL , new OnCallback() {            // CALL (Incoming call)
				@Override
				public void onCallback(Object object) {
					final String TAG = "_peer.on.CALL[MA.onCr]";
					String dbMsg = "";
					try {
						if ( !(object instanceof MediaConnection) ) {
							dbMsg = " !(object instanceof MediaConnection) ";
							myLog(TAG , dbMsg);
							return;
						}
						_mediaConnection = ( MediaConnection ) object;
						setMediaCallbacks();
						_mediaConnection.answer(_localStream);
						_bConnected = true;
						updateActionButtonTitle();
						myLog(TAG , dbMsg);
					} catch (Exception er) {
						myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
					}
				}
			});

			_peer.on(Peer.PeerEventEnum.CLOSE , new OnCallback() {
				@Override
				public void onCallback(Object object) {
					final String TAG = "_peer.on.CLOSE[MA.onCr]";
					String dbMsg = "";
					myLog(TAG , "[_peer.On/Close]");
				}
			});

			_peer.on(Peer.PeerEventEnum.CONNECTION , new OnCallback() {
				@Override
				public void onCallback(Object object) {
					final String TAG = "_peer.on.CONNECTION[MA.onCr]";
					String dbMsg = "=";
					try {
						PeerConnection connection = ( PeerConnection ) object;
						String senderID = connection.getSenders().get(0).id();
						dbMsg = "senderID=" + senderID;
						conect_situation_tv.setText(getResources().getString(R.string.conect_now));            //接続状況
						myLog(TAG , dbMsg);
					} catch (Exception er) {
						myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
					}
				}
			});

			_peer.on(Peer.PeerEventEnum.DISCONNECTED , new OnCallback() {
				@Override
				public void onCallback(Object object) {
					final String TAG = "_peer.on.DISCONNECTED[MA.onCr]";
					String dbMsg = "";
					tvPartnerId.setText(getResources().getString(R.string.conectlist_titol));        //接続先ID
					conect_situation_tv.setText(getResources().getString(R.string.non_conect_now));            //接続状況

					partner_id = "";
					sharedPref = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);            //	getActivity().getBaseContext()
					myEditor = sharedPref.edit();
					myEditor.putString("partner_id_key" , partner_id);
					boolean kakikomi = myEditor.commit();
					dbMsg += ",書込み=" + kakikomi;//////////////////

					myLog(TAG , "[_peer.On/Disconnected]");
				}
			});
			_peer.on(Peer.PeerEventEnum.ERROR , new OnCallback() {
				@Override
				public void onCallback(Object object) {
					final String TAG = "_peer.on.Error[MA.onCr]";
					String dbMsg = "";
					PeerError error = ( PeerError ) object;                // io.skyway.Peer.PeerError;message="authToken" is invalid.
					dbMsg += "type=" + error.type;                            // typeString=authentication
					dbMsg += ",typeString=" + error.typeString;
					dbMsg += ",message=" + error.message;                    //    message="authToken" is invalid.,
					dbMsg += ",exception=" + error.exception;                // exception=null
					myErrorLog(TAG , error + ";" + dbMsg);
					conect_situation_tv.setText(error.message + ";" + getResources().getString(R.string.can_not_conect_msg3));
					//It doesn't look like you have permission to list peers IDs ｒ リストアップの許可が出ていない
				}
			});
			myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
	}

	private final int FP = ViewGroup.LayoutParams.MATCH_PARENT;
	private final int WC = ViewGroup.LayoutParams.WRAP_CONTENT;

	/**
	 * この端末の LocalStreamをstart
	 * ①カメラ映像・マイク音声取得に関するオプションを設定
	 * ②_localStreamを取得
	 * local MediaStream & show it
	 * ※  _localStream.getPeerId()は取得できない
	 * ☆二重に呼び出すとクラッシュする
	 */
	//この端末起動直後のskyway接続～サブカメラ取得
	void startLocalStream() {
		final String TAG = "startLocalStream[MA]";
		String dbMsg = "";
		try {
			dbMsg = "_peer=" + _peer;
			Navigator.initialize(_peer);
			constraints = new MediaConstraints();
			constraints.maxWidth = 640;       //org;960		>	1920	crash?
			constraints.maxHeight = 480;       //org;540		>	1080
			constraints.cameraPosition = MediaConstraints.CameraPositionEnum.FRONT;
			dbMsg += ",constraints;maxFrameRate=" + constraints.maxFrameRate+"[" + constraints.maxWidth+"×" + constraints.maxWidth+"]";
			_localStream = Navigator.getUserMedia(constraints);
			_localStream.addVideoRenderer(canvasSub , 0);         // addViewm前ならクラッシュしない
//			LinearLayout main_views_ll = ( LinearLayout ) findViewById(R.id.main_views_ll);
//			int hbW = main_views_ll.getWidth();
//			dbMsg += ",main_views_ll{" + hbW;
//			int hbH = main_views_ll.getHeight();
//			dbMsg += "×" + hbH + "]";
			int hbW = canvasSub.getWidth();
			dbMsg += ",canvasSub{" + hbW;
			int hbH = canvasSub.getHeight();
			dbMsg += "×" + hbH + "]";
			int childCount = canvasSub.getChildCount();
			dbMsg += ",getChild="+canvasSub.getChildAt(childCount-1).getClass().getName();

			ViewGroup.LayoutParams VGLP = new ViewGroup.LayoutParams(hbW , hbH);
			CSCV = new CS_CanvasView(this);
			canvasSub.addView(CSCV , 1 , VGLP);                            //上位階層にホワイトボードのviewを追加
			 childCount = canvasSub.getChildCount();
			dbMsg += ",childCount="+childCount;
			dbMsg += ",CS_CanvasView="+canvasSub.getChildAt(childCount-1).getClass().getName();
// で// mChildren[0]=org.webrtc.SurfaceViewRenderer　 mChildren[1]=com.hijiyama_koubou.wb_joint_ownership.CS_CanvasView

			int trackCount = _localStream.getVideoTracks();
			dbMsg += ",映像トラック=" + trackCount + "トラック";
			dbMsg += ">_localStream,id=" + _localStream.getPeerId();
			dbMsg += ",Label=" + _localStream.getLabel();
			dbMsg += ",VideoTracks=" + _localStream.getVideoTracks();


			tvPartnerId.setText(getResources().getString(R.string.conectlist_titol));        //接続先ID
			myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
	}


//	public void myAddVideoRenderer(CS_CanvasView canvas, int videoTrackNumber) {
////		if (null != canvas) {
////			if (this.getVideoTracks() > videoTrackNumber) {
//				if (!this.isRemote) {
//					this.localEglbase = this.provider.getLocalEglBase();
//					if (null == this.localEglbase) {
//						return;
//					}
//
//					canvas.init(this.localEglbase.getEglBaseContext());
//				} else {
//					this.remoteEglbase = this.provider.getRemoteEglbase();
//					if (null == this.remoteEglbase) {
//						return;
//					}
//
//					canvas.init(this.remoteEglbase.getEglBaseContext());
//				}
//
//				VideoRenderer videoRenderer = canvas.startRendering();
//				this.nativeAddVideoRenderer(videoRenderer, videoTrackNumber);
//				LinkedList<Canvas> renderers = (LinkedList)this.videoRenderers.get(videoTrackNumber);
//				if (null == renderers) {
//					renderers = new LinkedList();
//					this.videoRenderers.append(videoTrackNumber, renderers);
//				}
//
//				renderers.add(canvas);
////			}
////		}
//	}

	// Set callbacks for MediaConnection.MediaEvents     /////////////////

	/**
	 * 接続中のイベント取得
	 * STREAM; 接続先確定
	 * REMOVE_STREAM
	 * CLOSE;切断処理
	 * ERROR； PeerErrorを取得
	 **/
	void setMediaCallbacks() {
		//接続処理
		_mediaConnection.on(MediaConnection.MediaEventEnum.STREAM , new OnCallback() {
			@Override
			public void onCallback(Object object) {
				final String TAG = "MediaConnection.STREAM[MA]";
				String dbMsg = "";
				try {
					_remoteStream = ( MediaStream ) object;
//					Canvas canvas = ( Canvas ) findViewById(R.id.svRemoteView);                //相手からの映像
					_remoteStream.addVideoRenderer(canvasMain , 0);
					partner_id = _remoteStream.getPeerId();
					dbMsg = ",partner_id=" + partner_id;
					tvPartnerId.setText(partner_id);        //接続先ID
					sharedPref = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);            //	getActivity().getBaseContext()
					myEditor = sharedPref.edit();
					myEditor.putString("partner_id_key" , partner_id);
					boolean kakikomi = myEditor.commit();
					dbMsg = dbMsg + ",書込み=" + kakikomi;//////////////////

					String relabel = _remoteStream.getLabel();
					dbMsg = "label=" + relabel;

					conect_situation_tv.setText(relabel);            //接続状況
					myLog(TAG , dbMsg);
				} catch (Exception er) {
					myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
				}
			}
		});

		//切断後
		_mediaConnection.on(MediaConnection.MediaEventEnum.REMOVE_STREAM , new OnCallback() {
			@Override
			public void onCallback(Object object) {
				final String TAG = "MediaConnection.REMOVE_STREAM[MA]";
				String dbMsg = "";
				try {
					MediaStream stream = ( MediaStream ) object;
					dbMsg += "label=" + stream.getLabel();
					String rePeerId = stream.getPeerId();
					dbMsg += ",rePeerId=" + rePeerId;
					myLog(TAG , dbMsg);
				} catch (Exception er) {
					myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
				}
			}
		});

		 //切断処理
		_mediaConnection.on(MediaConnection.MediaEventEnum.CLOSE , new OnCallback() {
			@Override
			public void onCallback(Object object) {
				final String TAG = "MediaConnection.CLOSE[MA]";
				String dbMsg = "";
				try {
					closeRemoteStream();
					_bConnected = false;
					updateActionButtonTitle();
					tvPartnerId.setText(getResources().getString(R.string.non_conect_now));                         //接続先ID
					conect_situation_tv.setText(getResources().getString(R.string.non_conect_now));                //接続状況
					myLog(TAG , dbMsg);
				} catch (Exception er) {
					myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
				}
			}
		});

		_mediaConnection.on(MediaConnection.MediaEventEnum.ERROR , new OnCallback() {
			@Override
			public void onCallback(Object object) {
				final String TAG = "MediaConnection.ERROR[MA]";
				String dbMsg;
				PeerError error = ( PeerError ) object;
				dbMsg = "error;" + error;
				dbMsg += ",type=" + error.type;
				dbMsg += ",typeString=" + error.typeString;
				dbMsg += ",message=" + error.message;
				conect_situation_tv.setText(error.message);
				myErrorLog(TAG , dbMsg);
			}
		});
	}

	//切断処理　Clean up objects  ///////////////////////////
	private void destroyPeer() {
		final String TAG = "destroyPeer[MA]";
		String dbMsg = "";
		try {
			closeRemoteStream();

			if ( null != _localStream ) {
				Canvas canvas = ( Canvas ) findViewById(R.id.svLocalView);            //送信映像
				_localStream.removeVideoRenderer(canvas , 0);
				_localStream.close();
			}

			if ( null != _mediaConnection ) {
				if ( _mediaConnection.isOpen() ) {
					_mediaConnection.close();
				}
				unsetMediaCallbacks();
			}

			Navigator.terminate();

			if ( null != _peer ) {
				unsetPeerCallback(_peer);
				if ( !_peer.isDisconnected() ) {
					_peer.disconnect();
				}

				if ( !_peer.isDestroyed() ) {
					_peer.destroy();
				}

				_peer = null;
			}
			myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
	}

	// Unset callbacks for PeerEvents   ///////////////////////////////
	void unsetPeerCallback(Peer peer) {
		final String TAG = "unsetPeerCallback[MA]";
		String dbMsg = "";
		try {
			dbMsg = "_peer=" + _peer;
			if ( null != _peer ) {
				peer.on(Peer.PeerEventEnum.OPEN , null);
				peer.on(Peer.PeerEventEnum.CONNECTION , null);
				peer.on(Peer.PeerEventEnum.CALL , null);
				peer.on(Peer.PeerEventEnum.CLOSE , null);
				peer.on(Peer.PeerEventEnum.DISCONNECTED , null);
				peer.on(Peer.PeerEventEnum.ERROR , null);
			}
			myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
	}

	// Unset callbacks for MediaConnection.MediaEvents
	void unsetMediaCallbacks() {
		final String TAG = "unsetMediaCallbacks[MA]";
		String dbMsg = "";
		try {
			dbMsg = "_mediaConnection=" + _mediaConnection;
			if ( null != _mediaConnection ) {
				_mediaConnection.on(MediaConnection.MediaEventEnum.STREAM , null);
				_mediaConnection.on(MediaConnection.MediaEventEnum.REMOVE_STREAM , null);          //追加
				_mediaConnection.on(MediaConnection.MediaEventEnum.CLOSE , null);
				_mediaConnection.on(MediaConnection.MediaEventEnum.ERROR , null);
			}
			myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
	}

	// Close a remote MediaStream
	/**
	 *受信ストリームの表示先受信canvasから削除して 受信ストリームを閉じる
	 * */
	void closeRemoteStream() {
		final String TAG = "closeRemoteStream[MA]";
		String dbMsg = "";
		try {
			if ( null != _remoteStream ) {
//				Canvas canvas = ( Canvas ) findViewById(R.id.svRemoteView);
				_remoteStream.removeVideoRenderer(canvasMain , 0);
				_remoteStream.close();
			}
			myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
	}

	// Create a MediaConnection

	/**
	 * 選択されたPeerIDで接続開始
	 */
	void onPeerSelected(String strPeerId) {
		final String TAG = "onPeerSelected[MA]";
		String dbMsg = "";
		try {
			dbMsg = "接続先=" + strPeerId;
			if ( null != _peer ) {
				if ( null != _mediaConnection ) {
					dbMsg += ",_mediaConnection=" + _mediaConnection;
					_mediaConnection.close();
				}

				CallOption option = new CallOption();
				_mediaConnection = _peer.call(strPeerId , _localStream , option);                        //接続先を呼び出す；

				if ( null != _mediaConnection ) {                //_mediaConnectionがnullのまま
					setMediaCallbacks();
					_bConnected = true;
					dbMsg += ",id=" + _mediaConnection.connectionId();
					dbMsg += ",isOpen=" + _mediaConnection.isOpen();
					dbMsg += ",label=" + _mediaConnection.label();
					dbMsg += ",metadata=" + _mediaConnection.metadata();
					dbMsg += ",peer=" + _mediaConnection.peer();
					dbMsg += ",type=" + _mediaConnection.type();
				}
				tvPartnerId.setText(strPeerId);
				updateActionButtonTitle();
			}
			myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
	}

	/**
	 * 発信処理
	 * Listing all peers
	 * ☆SkywayにApicationを追加する時；listAllPeers APIを利用する をONにしないとshowPeerIDsで接続先リストが得られない
	 */
	void showPeerIDs() {
		final String TAG = "showPeerIDs[MA]";
		String dbMsg = "";
		try {
			dbMsg = "_peer=" + _peer + ",peer_id=" + peer_id;
			String titolStr = getResources().getString(R.string.can_not_conect_titol);
			String mggStr = "";
			if ( (null == _peer) ) {
				mggStr = getResources().getString(R.string.can_not_conect_msg1);        // "Your PeerID is null or invalid.";
				messageShow(titolStr , mggStr);
			} else if ( (null == peer_id) || (0 == peer_id.length()) ) {        //
				mggStr = getResources().getString(R.string.can_not_conect_msg2);        // 	mggStr =  "接続できる方がログインしていません。";
				messageShow(titolStr , mggStr);
			} else {
				// Get all IDs connected to the server
				final Context fContext = this;
				_peer.listAllPeers(new OnCallback() {                            //接続先のPeerID一覧を取得
					@Override
					public void onCallback(Object object) {
						final String TAG = "listAllPeers[MA]";
						String dbMsg = "";
						try {
							if ( !(object instanceof JSONArray) ) {
								return;
							}
							JSONArray peers = ( JSONArray ) object;
							ArrayList< String > _listPeerIds = new ArrayList<>();
							String peerId;
							dbMsg = "peers" + peers.length() + "件";
							for ( int i = 0 ; peers.length() > i ; i++ ) {                            // Exclude my own ID
								dbMsg += "(" + i + ")";
								try {
									peerId = peers.getString(i);
									dbMsg += peerId + "/" + peer_id;
									if ( !peer_id.equals(peerId) ) {
										_listPeerIds.add(peerId);
									}
								} catch (Exception er) {
									myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
								}
							}

							if ( 0 < _listPeerIds.size() ) {                        // Show IDs using DialogFragment
								FragmentManager mgr = getFragmentManager();
								PeerListDialogFragment dialog = new PeerListDialogFragment();
								dialog.setListener(new PeerListDialogFragment.PeerListDialogFragmentListener() {
									@Override
									public void onItemClick(final String item) {
										_handler.post(new Runnable() {
											@Override
											public void run() {
												onPeerSelected(item);
											}
										});
									}
								});
								dialog.setItems(_listPeerIds);
								dialog.show(mgr , "peerlist");
							} else {
								String titolStr = getResources().getString(R.string.can_not_conect_titol);
								String mggStr = getResources().getString(R.string.can_not_conect_msg2);
								messageShow(titolStr , mggStr);
							}
							myLog(TAG , dbMsg);
						} catch (Exception er) {
							myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
						}

					}
				});
			}
			dbMsg += ",msegStr=" + mggStr;
			myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
	}

	/**
	 * 接続ボタンのキャプション変更     	// Update actionButton title
	 */
	void updateActionButtonTitle() {
		_handler.post(new Runnable() {
			@Override
			public void run() {
				final String TAG = "updateActionButtonTitle[MA]";
				String dbMsg = "";
				try {
					Button connect_bt = ( Button ) findViewById(R.id.connect_bt);
					if ( null != connect_bt ) {
						if ( false == _bConnected ) {
//							connect_bt.seti.setImageResource(android:android.R.drawable.sym_call_incoming);
							connect_bt.setText(getResources().getString(R.string.call_bt_caption2conect));       //"Make Call"
						} else {
							connect_bt.setText(getResources().getString(R.string.call_bt_caption2disconect));                                                  //"Hang up"
						}
					}
					myLog(TAG , dbMsg);
				} catch (Exception er) {
					myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
				}
			}
		});
	}

	///////////////////////////////////////////////////////////////////////////////////
	public void toWhiteBorrb() {
		final String TAG = "toWhiteBorrb[MA]";
		String dbMsg = "";
		try {
			dbMsg += ",isNowWhitebord=" + isNowWhitebord;
			isNowWhitebord = true;
			if ( _localStream != null ) {


				boolean isVideoEnable = _localStream.getEnableVideoTrack(0);
				dbMsg += ",Video;Enable=" + isVideoEnable;
				_localStream.setEnableVideoTrack(0 , false);        //映像トラック停止
				isVideoEnable = _localStream.getEnableVideoTrack(0);
				dbMsg += ">>" + isVideoEnable;
				int trackCount = _localStream.getVideoTracks();
				dbMsg += ",映像トラック=" + trackCount + "トラック";
				//				_localStream.removeVideoRenderer(canvasSub,0);
//				_localStream.addVideoRenderer(main_whitebord , 0);        //アクティブになっていないと追加できない？(io.skyway.Peer.Browser.Canvas)
//videoRenderers.mValues[0].mChildren[1]に
//				if ( !isAddWB ) {                        //ホワイトボード追加済み
//					int hbW = main_whitebord.getWidth();
//					dbMsg += ",whitebord{" + hbW;
//					int hbH = main_whitebord.getHeight();
//					dbMsg += "×" + hbH + "]";
//					ViewGroup.LayoutParams VGLP = new ViewGroup.LayoutParams(hbW , hbH);
//					CS_CanvasView CSCV = new CS_CanvasView(this);
//					main_whitebord.addView(CSCV , 1 , VGLP);                            //上位階層にホワイトボードのviewを追加
//					isAddWB = true;
//				}
//				isVideoEnable = _localStream.getEnableVideoTrack(0);
//				dbMsg += ",ホワイトボード;Enable=" + isVideoEnable;
//				_localStream.setEnableVideoTrack(0 , true);        //ホワイトボードトラック開始
//				isVideoEnable = _localStream.getEnableVideoTrack(0);
//				dbMsg += ">>" + isVideoEnable;
			}
			myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
	}

	public void toVideoChat() {
		final String TAG = "toVideoChat[MA]";
		String dbMsg = "";
		try {
			dbMsg += ",isNowWhitebord=" + isNowWhitebord;
			isNowWhitebord = false;
			if ( _localStream != null ) {
				int trackCount = _localStream.getVideoTracks();
				dbMsg += ",Video=" + trackCount + "トラック";
//				boolean isVideoEnable = _localStream.getEnableVideoTrack(1);
//				dbMsg += ",ホワイトボード;Enable=" + isVideoEnable;
//				_localStream.setEnableVideoTrack(0 , false);        //ホワイトボードトラック停止
//				isVideoEnable = _localStream.getEnableVideoTrack(0);
//				dbMsg += ">>" + isVideoEnable;
//				_localStream.removeVideoRenderer(main_whitebord,0);
//				_localStream.addVideoRenderer( canvasSub, 0);        //アクティブになっていないと追加できない？(io.skyway.Peer.Browser.Canvas)         canvasSub

				boolean isVideoEnable = _localStream.getEnableVideoTrack(0);
				dbMsg += ",Video;Enable=" + isVideoEnable;
				_localStream.setEnableVideoTrack(0 , true);        //映像トラック再生
				isVideoEnable = _localStream.getEnableVideoTrack(0);
				dbMsg += ">>" + isVideoEnable;
			}

			myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
	}


	public void sendVeiwChange() {
		final String TAG = "sendVeiwChange[MA]";
		String dbMsg = "";
		try {
			dbMsg += ",isNowWhitebord=" + isNowWhitebord;
			int trackCount = _localStream.getVideoTracks();
			dbMsg += ",Video=" + trackCount + "トラック";
			boolean isVideoEnable = _localStream.getEnableVideoTrack(0);
			dbMsg += ",Enable=" + isVideoEnable;
			if ( isNowWhitebord ) {
				toVideoChat();
			} else {
				toWhiteBorrb();
			}
			myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
	}

	Bitmap wrBmp;

	/**
	 *
	 * */
	public void whiteBordDrow(Bitmap bmp) {
		final String TAG = "whiteBordDrow[MA]";
		String dbMsg = "";
		try {
			dbMsg = "bmp[" + bmp.getWidth() + "×" + bmp.getHeight() + "]";
//			if ( main_whitebord != null ) {
//				CS_CanvasView CCV = new CS_CanvasView(this);
//				CCV.addBitMap(bmp);
//			}
			myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
	}

	///////////////////////////////////////////////////////////////////////////////////
	public void messageShow(String titolStr , String mggStr) {
		CS_Util UTIL = new CS_Util();
		UTIL.messageShow(titolStr , mggStr , MainActivity.this);
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
//SkyWayを使いこなすために		https://www.slideshare.net/iwashi86/skyway-how-to-use-skyway-webrtc
//


//①セッションが始まらない
// SkywayにApicationを追加する時；権限(APIキー認証を利用する)はOFFに

//②クラッシュ
//1) カメラの接続が切れている// ession 0: Exception while stopping repeating:  android.hardware.camera2.CameraAccessException: CAMERA_DISCONNECTED (2): cancelRequest:340: Camera device no longer alive
//2) Caused by: android.os.ServiceSpecificException: cancelRequest:340: Camera device no longer alive (code 4)
//3)E/SurfaceView: Exception configuring surface； java.lang.IllegalStateException: Not on main thread!
//4)System: Uncaught exception thrown by finalizer
//5)System: android.view.ViewRootImpl$CalledFromWrongThreadException: Only the original thread that created a view hierarchy can touch its views.
//6)A/libc: Fatal signal 11 (SIGSEGV), code 1, fault addr 0x54 in tid 28625 (Camera SurfaceT)

//③接続先リストが得られない③SkywayにApicationを追加する時；listAllPeers APIを利用する をONにしないとshowPeerIDsで接続先リストが得られない

//④接続後501SOで発生するクラッシュ
// 1)com.ntt.ecl.webrtc.sample_p2p_videochat E/ACodec: [OMX.qcom.video.encoder.vp8] storeMetaDataInBuffers (output) failed w/ err -1010
// 2)call to OpenGL ES API with no current context (logged once per thread)
// 3)libc: Fatal signal 11 (SIGSEGV), code 1, fault addr 0x418 in tid 9204 (EglRenderer)
// 4)E/CameraCaptureSession: Session 0: Exception while stopping repeating:
//    android.hardware.camera2.CameraAccessException: The camera device is removable and has been disconnected from the Android device, or the camera service has shut down the connection due to a higher-priority access request for the camera device.

// >> 接続前に　Veiwサイズを取得し
//⑤接続後701SHがスリープする


//課題
//カメラ切替のタイムラグ；切り替わった時のイベントは？
//Android8ではonStopのスリープ防止が機能していない
//自己モニターにイベントをセットするとホワイトボードとして描画するときに誤動作する

//挙動確認
//701SHを起動しっぱなしにしてもセッションが切れる事は無く再接続できる
// クラッシュなどでアプリが停止すると接続中のPeerIDはクリアされる（つなぎっぱなしにはならない）
//TURN (GB) は Signaling (回)が100回（*30秒）で0.001、
// _localStream.addVideoRendererで複数のcanvasを割り付ける事は出来るが  _localStreamとして創出できるのは1トラックだけ