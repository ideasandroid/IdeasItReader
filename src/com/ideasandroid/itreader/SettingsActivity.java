package com.ideasandroid.itreader;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.view.KeyEvent;

public class SettingsActivity extends PreferenceActivity implements
		Preference.OnPreferenceChangeListener {
	private final int SEARCH_REQUEST_CODE = 0;
	private SharedPreferences settings;
	EditTextPreference timeoutReaded ;
	EditTextPreference timeoutNoReaded ;
	EditTextPreference refreshRate ;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getPreferenceManager().setSharedPreferencesName("ideasrss");
		addPreferencesFromResource(R.xml.preferences);
		settings = getPreferenceManager().getSharedPreferences();
		timeoutReaded=(EditTextPreference)findPreference("ideasrss.timeout.readed");
		timeoutReaded.setOnPreferenceChangeListener(this);
		timeoutReaded.setSummary(settings.getString("ideasrss.timeout.readed","2")+"天");
		timeoutNoReaded=(EditTextPreference)findPreference("ideasrss.timeout.noreaded");
		timeoutNoReaded.setOnPreferenceChangeListener(this);
		timeoutNoReaded.setSummary(settings.getString("ideasrss.timeout.noreaded","4")+"天");
		refreshRate=(EditTextPreference)findPreference("ideasrss.refresh.rate");
		refreshRate.setOnPreferenceChangeListener(this);
		refreshRate.setSummary(settings.getString("ideasrss.refresh.rate","1")+"小时");
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			setResult(SEARCH_REQUEST_CODE, null);
			this.finish();
		}
		return super.onKeyDown(keyCode, event);
	}

	public boolean onPreferenceChange(Preference preference, Object newValue) {
		// TODO Auto-generated method stub
		if(preference.getKey().equals("ideasrss.timeout.readed")){
			timeoutReaded.setSummary(newValue+"天");
		}else if(preference.getKey().equals("ideasrss.timeout.noreaded")){
			timeoutNoReaded.setSummary(newValue+"天");
		}else if(preference.getKey().equals("ideasrss.refresh.rate")){
			refreshRate.setSummary(newValue+"小时");
		}
		return true;
	}
}
