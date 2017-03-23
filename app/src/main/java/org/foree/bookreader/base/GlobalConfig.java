package org.foree.bookreader.base;

import android.content.Context;
import android.content.SharedPreferences;
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
        SharedPreferences preferences = BaseApplication.getInstance().getSharedPreferences(SettingsActivity.PREF_NAME, Context.MODE_PRIVATE);
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
}
