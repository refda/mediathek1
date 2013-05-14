package de.janrenz.app.mediathek;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class SettingsActivity extends PreferenceActivity {
	public static String HIDE_COPYBUTTON = "pref_hide_copyurl_button";
	public static String SHOW_LONG_DESC = "pref_always_show_long_desc";
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }
}