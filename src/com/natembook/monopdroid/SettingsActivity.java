package com.natembook.monopdroid;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import com.natembook.monopdroid.R;

public class SettingsActivity extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.addPreferencesFromResource(R.xml.settings);
    }
}
