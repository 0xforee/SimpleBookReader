package org.foree.bookreader.settings;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;

import org.foree.bookreader.R;
import org.foree.bookreader.base.GlobalConfig;
import org.foree.bookreader.update.UpdateAgent;

/**
 * Created by foree on 16-7-28.
 * 设置界面的内部实现
 */
public class SettingsFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preference_all);

        Preference checkUpdate = findPreference(SettingsActivity.KEY_PREF_CHECK_UPDATE);
        checkUpdate.setSummary(String.format(getString(R.string.update_version_name_title), GlobalConfig.getInstance().getVersionName()));
    }

    /**
     * {@inheritDoc}
     *
     * @param preferenceScreen
     * @param preference
     */
    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {

        if (preference.getKey().equals(SettingsActivity.KEY_PREF_CHECK_UPDATE)) {
            UpdateAgent.checkUpdate(getActivity(), true);
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }
}
