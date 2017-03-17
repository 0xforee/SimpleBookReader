package org.foree.bookreader.base;

import android.app.Application;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import org.foree.bookreader.R;

import java.io.File;

/**
 * Created by foree on 16-7-19.
 */
public class BaseApplication extends Application {
    private static final String TAG = BaseApplication.class.getSimpleName();
    private static BaseApplication mInstance;

    public static final String SDCardPath = Environment.getExternalStorageDirectory().getAbsolutePath();
    public static final String myApplicationName = "bookreader";
    public static final String myApplicationDirPath = SDCardPath + File.separator + myApplicationName;
    // 应用程序数据目录名
    public static final String myApplicationConfigsName = "configs";
    // 应用程序缓存目录名
    public static final String myApplicationCacheName = "cache";
    public DisplayImageOptions options;

    public static synchronized BaseApplication getInstance() {
        return mInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;

        initApplicationDir();
        initWebSites();
        initImageLoader();

    }

    private void initImageLoader() {
        ImageLoaderConfiguration config = ImageLoaderConfiguration.createDefault(this);

        ImageLoader.getInstance().init(config);

    }

    public DisplayImageOptions getDisplayImageOptions(){
        if(options == null) {
            // init displayOptions
            options = new DisplayImageOptions.Builder()
                    .showImageOnLoading(R.drawable.book_cover_holder)
                    .showImageOnFail(R.drawable.book_cover_holder)
                    .showImageForEmptyUri(R.drawable.book_cover_holder)
                    .cacheInMemory(true)
                    .cacheOnDisk(true)
                    .build();
        }
        return options;
    }

    public void initApplicationDir() {

        //如果当前Sdcard已经挂载，应用程序目录与缓存目录是否建立完成
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            //应用程序目录
            File myDateDir = new File(myApplicationDirPath);
            if (!myDateDir.exists())
                if (!myDateDir.mkdir()) {
                    Log.e(TAG, "创建应用程序目录失败");
                }


            //配置目录
            File myCacheDir = new File(myApplicationDirPath + File.separator + myApplicationConfigsName);
            if (!myCacheDir.exists())
                if (!myCacheDir.mkdir()) {
                    Log.e(TAG, "创建cache目录失败");
                }
            //缓存目录
            File mySourceDir = new File(myApplicationDirPath + File.separator + myApplicationCacheName);
            if (!mySourceDir.exists())
                if (!mySourceDir.mkdir()) {
                    Log.e(TAG, "创建data目录失败");
                }
        }

        Log.v(TAG, "环境变量初始化成功");

    }

    public String getCacheDirString() {
        return myApplicationDirPath + File.separator + myApplicationCacheName;
    }

    private void initWebSites() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(BaseApplication.getInstance());
        if (sp.getBoolean("first_run", true)) {

            sp.edit().putBoolean("first_run", false).apply();
        }
    }
}
