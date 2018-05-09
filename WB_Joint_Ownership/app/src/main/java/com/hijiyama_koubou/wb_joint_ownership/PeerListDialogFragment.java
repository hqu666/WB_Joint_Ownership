package com.hijiyama_koubou.wb_joint_ownership;


import android.app.DialogFragment;
import android.content.Context;
import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

/**
 * PeerListDialogFragment.java
 */
public class PeerListDialogFragment extends DialogFragment implements AdapterView.OnItemClickListener {

	public interface PeerListDialogFragmentListener {
		void onItemClick(String item);
	}

	private ListView _lvList;

	private PeerListDialogFragmentListener _listener;
	private ArrayList< String > _items;


	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater , ViewGroup container , Bundle savedInstanceState) {
		final String TAG = "onCreateView[PDF]";
		String dbMsg = "";
		View vwDialog = null;
		try {
			Window window = getDialog().getWindow();
			window.requestFeature(Window.FEATURE_NO_TITLE);
			Context context = inflater.getContext();
			WindowManager wm = ( WindowManager ) context.getSystemService(Context.WINDOW_SERVICE);
			Display display = wm.getDefaultDisplay();
			Point ptSize = new Point();
			display.getSize(ptSize);
			window.setLayout(ptSize.x * 2 / 3 , ptSize.y * 2 / 3);
			vwDialog = inflater.inflate(R.layout.fragment_dialog_peerlist , container , false);
			_lvList = ( ListView ) vwDialog.findViewById(R.id.listView);
			_lvList.setOnItemClickListener(this);
			myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
		return vwDialog;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		final String TAG = "onActivityCreated[PDF]";
		String dbMsg = "";
		try {
			ArrayAdapter< String > adapter = new ArrayAdapter< String >(getActivity() , android.R.layout.simple_list_item_1 , _items);
			_lvList.setAdapter(adapter);
			myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
	}

	@Override
	public void onDestroyView() {
		final String TAG = "onDestroyView[PDF]";
		String dbMsg = "";
		try {
			_listener = null;
			_lvList = null;
			_items = null;
			myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
		super.onDestroyView();
	}

	@Override
	public void onItemClick(AdapterView< ? > parent , View view , int position , long id) {
		final String TAG = "onItemClick[PDF]";
		String dbMsg = "id="+id;
		try {
			dbMsg += ",position="+position;
			if ( null != _listener ) {
				String item = _items.get(position);
				dbMsg += ",item="+item;
				_listener.onItemClick(item);
			}
			myLog(TAG , dbMsg);
			dismiss();
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
	}

	public void setListener(PeerListDialogFragmentListener listener) {
		final String TAG = "setListener[PDF]";
		String dbMsg = "";
		try {
			_listener = listener;
			myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
	}

	public void setItems(ArrayList< String > list) {
		final String TAG = "setItems[PDF]";
		String dbMsg = "";
		try {
			_items = list;
			myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
	}

	///////////////////////////////////////////////////////////////////////////////////
//	public void messageShow(String titolStr , String mggStr) {
//		CS_Util UTIL = new CS_Util();
//		UTIL.messageShow(titolStr , mggStr , this);
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