package org.foree.bookreader.base;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatDelegate;
import android.util.Log;

import org.foree.bookreader.bean.book.Book;
import org.foree.bookreader.bean.dao.BookDao;
import org.foree.bookreader.receiver.BootReceiver;
import org.foree.bookreader.service.SyncService;
import org.foree.bookreader.settings.SettingsActivity;

import java.io.File;
import java.util.List;

import cn.bmob.v3.Bmob;
import cn.bmob.v3.BmobInstallation;
import cn.bmob.v3.BmobInstallationManager;
import cn.bmob.v3.InstallationListener;
import cn.bmob.v3.exception.BmobException;

/**
 * Created by foree on 16-7-19.
 */
public class BaseApplication extends Application {
    private static final String TAG = BaseApplication.class.getSimpleName();
    private static final boolean DEBUG = false;
    private static BaseApplication mInstance;

    public static final String SDCardPath = Environment.getExternalStorageDirectory().getAbsolutePath();
    public static final String myApplicationName = "bookreader";
    public static final String myApplicationDirPath = SDCardPath + File.separator + myApplicationName;
    // 应用程序数据目录名
    public static final String myApplicationConfigsName = "configs";
    // 应用程序缓存目录名
    public static final String myApplicationCacheName = "cache";

    public static synchronized BaseApplication getInstance() {
        return mInstance;
    }

    private AlarmManager alarmManager;
    private PendingIntent alarmIntent;
    private ComponentName receiver;
    private PackageManager pm;

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;

        // night mode
        if (GlobalConfig.getInstance().isNightMode()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        //init background data sync
        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, SyncService.class);
        intent.setAction(SyncService.ACTION_SYNC);
        intent.putExtra(SyncService.EXTRA_NOTIFY, true);
        alarmIntent = PendingIntent.getService(this, 0, intent, 0);

        if (GlobalConfig.getInstance().isSyncEnable()) {
            startAlarm(true);
        }

        // init boot receiver
        receiver = new ComponentName(this, BootReceiver.class);
        pm = this.getPackageManager();
        if (pm.getComponentEnabledSetting(receiver) != PackageManager.COMPONENT_ENABLED_STATE_ENABLED) {
            setReceiverEnable(true);
        }

        // init bmob
        Bmob.initialize(this, "15398f4ba7ca3337f4b6cff8756ebe35", "release");
        BmobInstallationManager.getInstance().initialize(new InstallationListener<BmobInstallation>() {
            @Override
            public void done(BmobInstallation bmobInstallation, BmobException e) {
                if (e == null) {
                    Log.d(TAG, bmobInstallation.getObjectId() + "-" + bmobInstallation.getInstallationId());
                } else {
                    Log.e(TAG, e.getMessage());
                }
            }
        });

        // fix old book url style
        fixBookUrlOldStyle(this);
    }

    private long getInterval() {
        int select = Integer.valueOf(PreferenceManager.getDefaultSharedPreferences(this).getString(SettingsActivity.KEY_PREF_SYNC_FREQUENCY, "60"));

        return select * 60 * 1000;
    }

    public void startAlarm(boolean start) {
        if (start) {
            if (DEBUG) {
                alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                        SystemClock.elapsedRealtime() + 10000,
                        60000, alarmIntent);
            } else {
                alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                        SystemClock.elapsedRealtime() + AlarmManager.INTERVAL_FIFTEEN_MINUTES,
                        getInterval(), alarmIntent);
            }
            Log.d(TAG, "startAlarm: " + alarmIntent.toString());
        } else {
            alarmManager.cancel(alarmIntent);
            Log.d(TAG, "cancelAlarm: " + alarmIntent.toString());
        }
    }

    public void setReceiverEnable(boolean enable) {
        if (enable) {
            pm.setComponentEnabledSetting(receiver,
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP);
        } else {
            pm.setComponentEnabledSetting(receiver,
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP);
        }
    }

    private void fixBookUrlOldStyle(final Context context) {
        if (GlobalConfig.getInstance().getVersionCode() <= 9) {
            new Thread() {
                @Override
                public void run() {
                    super.run();
                    BookDao bookDao = new BookDao(context);
                    List<Book> allBooks = bookDao.getAllBooks();
                    for (Book book : allBooks) {
                        if (!book.getBookUrl().contains(GlobalConfig.MAGIC_SPLIT_KEY)) {
                            String oldBookUrl = book.getBookUrl();

                            // update bookUrl firstly

                            bookDao.updateOldBookStyle(oldBookUrl);

                            // update content url secondly
                        }
                    }
                }
            }.start();
        }
    }


}
