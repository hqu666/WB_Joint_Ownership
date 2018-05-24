package com.hijiyama_koubou.wb_joint_ownership;

import android.app.Application;
import io.socket.client.IO;
import io.socket.client.Socket;

import java.net.URISyntaxException;
//未使用
public class ChatApplication extends Application {

	private Socket mSocket;

	{
		try {
			mSocket = IO.socket(ChatConstants.CHAT_SERVER_URL);
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}

	public Socket getSocket() {
		return mSocket;
	}
}
