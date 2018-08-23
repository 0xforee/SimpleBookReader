package org.foree.bookreader.base;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
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
    private Context mContext;

    // 0 to 1 adjusts the brightness from dark to full bright.
    private static float mAppBrightness = -1;

    private SharedPreferences mSharedPreference;

    public static final String MAGIC_SPLIT_KEY = "~_~";

    public static GlobalConfig getInstance() {
        return ourInstance;
    }

    private GlobalConfig() {
        mContext = BaseApplication.getInstance();
        mSharedPreference = PreferenceManager.getDefaultSharedPreferences(mContext);
    }

    public boolean isNightMode() {
        boolean isNight = mSharedPreference.getBoolean(SettingsActivity.KEY_PREF_NIGHT_MODE, false);
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
                mAppBrightness = Settings.System.getInt(mContext.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS) / 255f;
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
        boolean isSyncEnable = mSharedPreference.getBoolean(SettingsActivity.KEY_PREF_SYNC_ENABLE, false);
        Log.d(TAG, "isSyncEnable: " + isSyncEnable);
        return isSyncEnable;
    }

    public int getPageBackground() {
        int color = mContext.getResources().getColor(R.color.normal_page_background);
        return mSharedPreference.getInt(SettingsActivity.KEY_PREF_PAGE_BACKGROUND, color);
    }

    public void setPageBackground(int color) {
        mSharedPreference.edit().putInt(SettingsActivity.KEY_PREF_PAGE_BACKGROUND, color).apply();
    }

    public boolean skipUpdate(int newVersion){
        return mSharedPreference.getInt(SettingsActivity.KEY_IGNORE_UPDATE, -1) == newVersion;
    }

    public void ignoreVersion(int newVersion){
        mSharedPreference.edit().putInt(SettingsActivity.KEY_IGNORE_UPDATE, newVersion).apply();
    }

    public int getVersionCode(){
        try {
            PackageInfo info = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0);
            return info.versionCode;

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public String getVersionName(){
        try {
            PackageInfo info = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0);
            return info.versionName;

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return "";
    }
}
