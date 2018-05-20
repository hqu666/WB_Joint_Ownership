package com.hijiyama_koubou.wb_joint_ownership;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
///org///
import io.socket.client.IO;
import io.socket.client.Socket;       //     java.net.Socketとコンフリクトする
import io.socket.emitter.Emitter;      //WebSocket
///org///
//		import java.net.MalformedURLException;

/**
 * A chat fragment containing messages view and input form.
 */
public class ChatMainFragment extends Fragment {

	private static final String TAG = "MainFragment";

	private static final int REQUEST_LOGIN = 0;

	private static final int TYPING_TIMER_LENGTH = 600;

	private RecyclerView mMessagesView;
	private EditText mInputMessageView;
	private List< Message > mMessages = new ArrayList< Message >();
	private RecyclerView.Adapter mAdapter;
	private boolean mTyping = false;
	private Handler mTypingHandler = new Handler();
	private String mUsername;
	private Socket mSocket;

	private Boolean isConnected = true;

	public ChatMainFragment() {
		super();
	}


	// This event fires 1st, before creation of fragment or any views
	// The onAttach method is called when the Fragment instance is associated with an Activity.
	// This does not mean the Activity is fully initialized.
	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		mAdapter = new ChatMessageAdapter(context , mMessages);
		if ( context instanceof Activity ) {
			//this.listener = (MainActivity) context;
		}
	}

	public Socket getSocket() {
		Socket _mSocket;
		try {
			_mSocket = IO.socket(ChatConstants.CHAT_SERVER_URL);
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
		return _mSocket;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		final String TAG = "onCreate[CF]";
		String dbMsg = "";
		try {
			setHasOptionsMenu(true);

			//	ChatApplication app =( ChatApplication ) getActivity().getApplication();    // Attempt to invoke virtual method 'android.content.Context android.app.Activity.getApplicationContext()' on a null object reference
////			ChatApplication app =( ChatApplication ) getActivity().getParent().getApplicationContext();    // Attempt to invoke virtual method 'android.content.Context android.app.Activity.getApplicationContext()' on a null object reference
//		    			//getApplication()	ChatApplication;android.app.Application cannot be cast to com.hijiyama_koubou.wb_joint_ownership.ChatApplication
//	//			ChatApplication app =( ChatApplication ) getActivity().getApplication(); 	//getApplication()	ChatApplication;android.app.Application cannot be cast to com.hijiyama_koubou.wb_joint_ownership.ChatApplication
//			//new ChatApplication();		//
			//		mSocket = app.getSocket();
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

			startSignIn();
			myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater , ViewGroup container , Bundle savedInstanceState) {
		final String TAG = "onCreateView[CF]";
		String dbMsg = "";
		try {
			myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
		return inflater.inflate(R.layout.chat_fragment_main , container , false);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		final String TAG = "onDestroy[CF]";
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
			myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
	}

	@Override
	public void onViewCreated(View view , Bundle savedInstanceState) {
		super.onViewCreated(view , savedInstanceState);
		final String TAG = "onCreate[CF]";
		String dbMsg = "";
		try {
			mMessagesView = ( RecyclerView ) view.findViewById(R.id.messages);
			mMessagesView.setLayoutManager(new LinearLayoutManager(getActivity()));
			mMessagesView.setAdapter(mAdapter);

			mInputMessageView = ( EditText ) view.findViewById(R.id.message_input);
			mInputMessageView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
				@Override
				public boolean onEditorAction(TextView v , int id , KeyEvent event) {
					if ( id == R.id.send || id == EditorInfo.IME_NULL ) {
						attemptSend();
						return true;
					}
					return false;
				}
			});
			mInputMessageView.addTextChangedListener(new TextWatcher() {
				@Override
				public void beforeTextChanged(CharSequence s , int start , int count , int after) {
					final String TAG = "beforeTextChanged[CF]";
					String dbMsg = "";
					try {
						myLog(TAG , dbMsg);
					} catch (Exception er) {
						myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
					}
				}

				@Override
				public void onTextChanged(CharSequence s , int start , int before , int count) {
					final String TAG = "onTextChanged[CF]";
					String dbMsg = "";
					try {

						if ( null == mUsername )
							return;
						if ( !mSocket.connected() )
							return;

						if ( !mTyping ) {
							mTyping = true;
							mSocket.emit("typing");
						}

						mTypingHandler.removeCallbacks(onTypingTimeout);
						mTypingHandler.postDelayed(onTypingTimeout , TYPING_TIMER_LENGTH);
						myLog(TAG , dbMsg);
					} catch (Exception er) {
						myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
					}
				}

				@Override
				public void afterTextChanged(Editable s) {
					final String TAG = "afterTextChanged[CF]";
					String dbMsg = "";
					try {
						myLog(TAG , dbMsg);
					} catch (Exception er) {
						myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
					}
				}
			});

			ImageButton sendButton = ( ImageButton ) view.findViewById(R.id.send_button);
			sendButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					attemptSend();
				}
			});

			myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
	}

	@Override
	public void onActivityResult(int requestCode , int resultCode , Intent data) {
		super.onActivityResult(requestCode , resultCode , data);
		final String TAG = "onActivityResult[CF]";
		String dbMsg = "";
		try {

			if ( Activity.RESULT_OK != resultCode ) {
				getActivity().finish();
				return;
			}

			mUsername = data.getStringExtra("username");
			int numUsers = data.getIntExtra("numUsers" , 1);

			addLog(getResources().getString(R.string.message_welcome));
			addParticipantsLog(numUsers);

			myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu , MenuInflater inflater) {
		final String TAG = "onCreateOptionsMenu[CF]";
		String dbMsg = "";
		try {
			inflater.inflate(R.menu.menu_chat , menu);            // Inflate the menu; this adds items to the action bar if it is present.
			myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		final String TAG = "onOptionsItemSelected[CF]";
		String dbMsg = "";
		try {
			// Handle action bar item clicks here. The action bar will
			// automatically handle clicks on the Home/Up button, so long
			// as you specify a parent activity in AndroidManifest.xml.
			int id = item.getItemId();
			//noinspection SimplifiableIfStatement
			if ( id == R.id.action_leave ) {
				leave();
				return true;
			}
			myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
		return super.onOptionsItemSelected(item);
	}

	private void addLog(String message) {
		final String TAG = "addLog[CF]";
		String dbMsg = "";
		try {
			mMessages.add(new Message.Builder(Message.TYPE_LOG).message(message).build());
			mAdapter.notifyItemInserted(mMessages.size() - 1);
			scrollToBottom();
			myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
	}

	private void addParticipantsLog(int numUsers) {
		final String TAG = "addParticipantsLog[CF]";
		String dbMsg = "";
		try {
			addLog(getResources().getQuantityString(R.plurals.message_participants , numUsers , numUsers));
			myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
	}

	private void addMessage(String username , String message) {
		final String TAG = "addMessage[CF]";
		String dbMsg = "";
		try {
			mMessages.add(new Message.Builder(Message.TYPE_MESSAGE).username(username).message(message).build());
			mAdapter.notifyItemInserted(mMessages.size() - 1);
			scrollToBottom();
			myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
	}

	private void addTyping(String username) {
		final String TAG = "addTyping[CF]";
		String dbMsg = "";
		try {
			mMessages.add(new Message.Builder(Message.TYPE_ACTION).username(username).build());
			mAdapter.notifyItemInserted(mMessages.size() - 1);
			scrollToBottom();
			myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
	}

	private void removeTyping(String username) {
		final String TAG = "removeTyping[CF]";
		String dbMsg = "";
		try {
			for ( int i = mMessages.size() - 1 ; i >= 0 ; i-- ) {
				Message message = mMessages.get(i);
				if ( message.getType() == Message.TYPE_ACTION && message.getUsername().equals(username) ) {
					mMessages.remove(i);
					mAdapter.notifyItemRemoved(i);
				}
			}
			myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
	}

	private void attemptSend() {
		final String TAG = "attemptSend[CF]";
		String dbMsg = "";
		try {
			if ( null == mUsername )
				return;
			if ( !mSocket.connected() )
				return;

			mTyping = false;

			String message = mInputMessageView.getText().toString().trim();
			if ( TextUtils.isEmpty(message) ) {
				mInputMessageView.requestFocus();
				return;
			}

			mInputMessageView.setText("");
			addMessage(mUsername , message);

			// perform the sending message attempt.
			mSocket.emit("new message" , message);
			myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
	}

	private void startSignIn() {
		final String TAG = "startSignIn[CF]";
		String dbMsg = "";
		try {
			mUsername = null;
			Intent intent = new Intent( this.getActivity().getParent(), ChatLoginActivity.class);			//で開くが Attempt to invoke virtual method 'java.lang.String android.content.Context.getPackageName()' on a null object reference
			// getActivity().getParent()    getContext()                    this.getActivity().getApplicationContext()
			startActivityForResult(intent , REQUEST_LOGIN);
			myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
	}

	private void leave() {
		final String TAG = "leave[CF]";
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

	private void scrollToBottom() {
		final String TAG = "scrollToBottom[CF]";
		String dbMsg = "";
		try {
			mMessagesView.scrollToPosition(mAdapter.getItemCount() - 1);
			myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
	}

	private Emitter.Listener onConnect = new Emitter.Listener() {
		@Override
		public void call(Object... args) {
			getActivity().runOnUiThread(new Runnable() {
				@Override
				public void run() {
					final String TAG = "onConnect[CF]";
					String dbMsg = "";
					try {
						if ( !isConnected ) {
							if ( null != mUsername )
								mSocket.emit("add user" , mUsername);
							Toast.makeText(getActivity().getApplicationContext() , R.string.connect , Toast.LENGTH_LONG).show();
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
			getActivity().runOnUiThread(new Runnable() {
				@Override
				public void run() {
					final String TAG = "onDisconnect[CF]";
					String dbMsg = "";
					try {
						isConnected = false;
						Toast.makeText(getActivity().getApplicationContext() , R.string.disconnect , Toast.LENGTH_LONG).show();
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
			getActivity().runOnUiThread(new Runnable() {
				@Override
				public void run() {
					final String TAG = "onConnectError[CF]";
					String dbMsg = "";
					try {
						Toast.makeText(getActivity().getApplicationContext() , R.string.error_connect , Toast.LENGTH_LONG).show();
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
			getActivity().runOnUiThread(new Runnable() {
				@Override
				public void run() {
					final String TAG = "onNewMessage[CF]";
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
						addMessage(username , message);
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
			getActivity().runOnUiThread(new Runnable() {
				@Override
				public void run() {
					final String TAG = "onUserJoined[CF]";
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
			getActivity().runOnUiThread(new Runnable() {
				@Override
				public void run() {
					final String TAG = "onUserLeft[CF]";
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
			getActivity().runOnUiThread(new Runnable() {
				@Override
				public void run() {
					final String TAG = "onTyping[CF]";
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
			getActivity().runOnUiThread(new Runnable() {
				@Override
				public void run() {
					final String TAG = "onStopTyping[CF]";
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
			final String TAG = "onTypingTimeout[CF]";
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

