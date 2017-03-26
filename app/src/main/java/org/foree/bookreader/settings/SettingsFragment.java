package org.foree.bookreader.settings;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import org.foree.bookreader.R;

/**
 * Created by foree on 16-7-28.
 * 设置界面的内部实现
 */
public class SettingsFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preference_all);
    }
}
