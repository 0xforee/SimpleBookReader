package org.foree.zetianjia;

import android.app.Application;

/**
 * Created by foree on 16-7-19.
 */
public class BaseApplication extends Application{
    private static BaseApplication mInstance;

    @Override
    public void onCreate(){
        super.onCreate();
        mInstance = this;
    }
    public static synchronized BaseApplication getInstance() {
        return mInstance;
    }
}
