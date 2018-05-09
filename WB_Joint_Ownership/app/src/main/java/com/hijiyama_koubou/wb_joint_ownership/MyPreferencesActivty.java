package com.hijiyama_koubou.wb_joint_ownership;

import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 * Created by hkuwayama on 2018/03/08.
 */

public class MyPreferencesActivty extends PreferenceActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getFragmentManager().beginTransaction()
				.replace(android.R.id.content, new MyPreferenceFragment())
				.commit();
	}
}