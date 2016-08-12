package org.foree.zetianji;

import android.app.Application;
import android.os.Environment;
import android.util.Log;

import java.io.File;

/**
 * Created by foree on 16-7-19.
 */
public class BaseApplication extends Application{
    private static final String TAG = BaseApplication.class.getSimpleName();
    private static BaseApplication mInstance;

    public static final String SDCardPath = Environment.getExternalStorageDirectory().getAbsolutePath();
    public static final String myApplicationName = "zetianji";
    public static final String myApplicationDirPath = SDCardPath + File.separator + myApplicationName;
    // 应用程序数据目录名
    public static final String myApplicationConfigsName = "configs";
    // 应用程序缓存目录名
    public static final String myApplicationCacheName = "cache";

    public static synchronized BaseApplication getInstance() {
        return mInstance;
    }

    @Override
    public void onCreate(){
        super.onCreate();
        mInstance = this;

        initApplicationDir();

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

    public String getCacheDirString(){
        return myApplicationDirPath + File.separator + myApplicationCacheName;
    }
}
