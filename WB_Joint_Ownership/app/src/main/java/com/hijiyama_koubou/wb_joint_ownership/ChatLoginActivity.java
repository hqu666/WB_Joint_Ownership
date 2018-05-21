package com.hijiyama_koubou.wb_joint_ownership;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;

/**
 * A login screen that offers login via username.
 */
public class ChatLoginActivity extends Activity {

	private EditText mUsernameView;
	private String mUsername;
	private Socket mSocket;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		final String TAG = "onCreate[CLA]";
		String dbMsg = "";
		try {
			getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);		//ソフトウェアキーボードが出ているときにAction Barが隠れたりしないようにする
			setContentView(R.layout.chat_activity_main);                          //android.view.InflateException: Binary XML file line #9: Binary XML file line #9: Error inflating class fragment
			ChatApplication app = ( ChatApplication ) getApplication();
			mSocket = app.getSocket();

			mUsernameView = ( EditText ) findViewById(R.id.username_input);            // Set up the login form.
			mUsernameView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
				@Override
				public boolean onEditorAction(TextView textView , int id , KeyEvent keyEvent) {
					if ( id == R.id.login || id == EditorInfo.IME_NULL ) {
						attemptLogin();
						return true;
					}
					return false;
				}
			});

			Button signInButton = ( Button ) findViewById(R.id.sign_in_button);
			signInButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view) {
					attemptLogin();
				}
			});

			mSocket.on("login" , onLogin);
			myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		final String TAG = "onDestroy[CLA]";
		String dbMsg = "";
		try {
			mSocket.off("login" , onLogin);
			myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
	}

	/**
	 * Attempts to sign in the account specified by the login form.
	 * If there are form errors (invalid username, missing fields, etc.), the errors are presented and no actual login attempt is made.
	 */
	private void attemptLogin() {
		final String TAG = "attemptLogin[CLA]";
		String dbMsg = "";
		try {
			mUsernameView.setError(null);                                        // Reset errors.
			String username = mUsernameView.getText().toString().trim();            // Store values at the time of the login attempt.
			dbMsg += ",username=" + username;
			// Check for a valid username.
			if ( TextUtils.isEmpty(username) ) {
				// There was an error; don't attempt login and focus the first
				// form field with an error.
				mUsernameView.setError(getString(R.string.error_field_required));
				mUsernameView.requestFocus();
				return;
			}
			mUsername = username;
			mSocket.emit("add user" , username);            // perform the user login attempt.
			myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
	}

	private Emitter.Listener onLogin = new Emitter.Listener() {
		@Override
		public void call(Object... args) {
			final String TAG = "onLogin[CLA]";
			String dbMsg = "";
			try {
				JSONObject data = ( JSONObject ) args[0];

				int numUsers;
				try {
					numUsers = data.getInt("numUsers");
				} catch (JSONException e) {
					return;
				}

				Intent intent = new Intent();
				intent.putExtra("username" , mUsername);
				intent.putExtra("numUsers" , numUsers);
				setResult(RESULT_OK , intent);
				finish();
				myLog(TAG , dbMsg);
			} catch (Exception er) {
				myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
			}
		}
	};
	///////////////////////////////////////////////////////////////////////////////////
//	public void messageShow(String titolStr , String mggStr) {
//		CS_Util UTIL = new CS_Util();
//		UTIL.messageShow(titolStr , mggStr , ChatMainFragment.this);
//	}

	public static void myLog(String TAG , String dbMsg) {
		CS_Util UTIL = new CS_Util();
		UTIL.myLog(TAG , dbMsg);
	}

	public static void myErrorLog(String TAG , String dbMsg) {
		CS_Util UTIL = new CS_Util();
		UTIL.myErrorLog(TAG , dbMsg);
	}
}

