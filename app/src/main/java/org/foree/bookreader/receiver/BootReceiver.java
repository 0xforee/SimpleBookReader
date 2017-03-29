package org.foree.bookreader.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.foree.bookreader.base.BaseApplication;

/**
 * Created by foree on 17-3-29.
 */

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            BaseApplication.getInstance().startAlarm(true);
        }
    }
}
