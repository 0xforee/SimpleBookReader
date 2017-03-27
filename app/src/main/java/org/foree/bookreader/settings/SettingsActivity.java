package org.foree.bookreader.settings;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import org.foree.bookreader.R;
import org.foree.bookreader.base.GlobalConfig;

/**
 * Created by foree on 16-7-28.
 * 设置界面
 */
public class SettingsActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String TAG = SettingsActivity.class.getSimpleName();

    /**
     * keys for Application preference
     */
    public static final String PREF_NAME = "settings";

    // night mode
    public static final String KEY_PREF_NIGHT_MODE = "pref_key_night_mode";

    // offline
    public static final String KEY_PREF_OFFLINE_ENABLE = "pref_key_offline_enable";
    public static final String KEY_PREF_OFFLINE_OFFSET = "pref_key_offline_offset";
    public static final String KEY_PREF_OFFLINE_WIFI_ONLY = "pref_key_offline_wifi_only";

    // sync data
    public static final String KEY_PREF_SYNC_ENABLE = "pref_key_sync_enable";
    public static final String KEY_PREF_SYNC_NOTIFICATION = "pref_key_sync_notification";
    public static final String KEY_PREF_SYNC_PRE_LOAD = "pref_key_sync_pre_load";
    public static final String KEY_PREF_SYNC_FREQUENCY = "pref_key_sync_frequency";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getFragmentManager().beginTransaction()
                .replace(R.id.content_main, new SettingsFragment())
                .commit();

    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        switch (key) {
            case KEY_PREF_NIGHT_MODE:
                GlobalConfig.getInstance().changeTheme();
                recreate();
                break;
            default:
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);
    }
}
