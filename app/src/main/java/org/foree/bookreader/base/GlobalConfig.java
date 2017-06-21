package org.foree.bookreader.base;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v7.app.AppCompatDelegate;
import android.util.Log;

import org.foree.bookreader.R;
import org.foree.bookreader.settings.SettingsActivity;

/**
 * Created by foree on 17-3-22.
 */
public class GlobalConfig {
    private static GlobalConfig ourInstance = new GlobalConfig();
    private static String TAG = GlobalConfig.class.getSimpleName();

    // 0 to 1 adjusts the brightness from dark to full bright.
    private static float mAppBrightness = -1;

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

    public float getAppBrightness() {
        if (mAppBrightness == -1) {
            try {
                // get system brightness value
                mAppBrightness = Settings.System.getInt(BaseApplication.getInstance().getContentResolver(), Settings.System.SCREEN_BRIGHTNESS) / 255f;
            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
            }
        }
        return mAppBrightness;
    }

    public void setAppBrightness(float brightness) {
        mAppBrightness = brightness;
    }

    public boolean isSyncEnable() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(BaseApplication.getInstance());
        boolean isSyncEnable = preferences.getBoolean(SettingsActivity.KEY_PREF_SYNC_ENABLE, false);
        Log.d(TAG, "isSyncEnable: " + isSyncEnable);
        return isSyncEnable;
    }

    public int getPageBackground() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(BaseApplication.getInstance());
        int index = preferences.getInt(SettingsActivity.KEY_PREF_PAGE_BACKGROUND, 0);
        int color = R.color.normal_page_background;
        switch (index) {
            case 0:
                color = R.color.normal_page_background;
                break;
            case 1:
                color = R.color.classical_page_background;
                break;
            case 2:
                color = R.color.eye_mode_page_background;
                break;
            case 3:
                color = R.color.night_page_background;
                break;
        }

        return BaseApplication.getInstance().getResources().getColor(color);
    }
}
