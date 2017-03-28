package org.foree.bookreader.base;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatDelegate;
import android.util.Log;

import org.foree.bookreader.settings.SettingsActivity;

/**
 * Created by foree on 17-3-22.
 */
public class GlobalConfig {
    private static GlobalConfig ourInstance = new GlobalConfig();
    private static String TAG = GlobalConfig.class.getSimpleName();

    public static GlobalConfig getInstance() {
        return ourInstance;
    }

    private GlobalConfig() {
    }

    public boolean isNightMode() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(BaseApplication.getInstance());
        boolean isNight = preferences.getBoolean(SettingsActivity.KEY_PREF_NIGHT_MODE, false);
        Log.d(TAG, "isNightMode: " + isNight);
        return isNight;
    }

    public void changeTheme() {
        if (isNightMode()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    public boolean isSyncEnable(){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(BaseApplication.getInstance());
        boolean isSyncEnable = preferences.getBoolean(SettingsActivity.KEY_PREF_SYNC_ENABLE, false);
        Log.d(TAG, "isSyncEnable: " + isSyncEnable);
        return isSyncEnable;
    }
}
