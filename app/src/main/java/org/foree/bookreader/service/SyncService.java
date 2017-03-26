package org.foree.bookreader.service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import org.foree.bookreader.R;
import org.foree.bookreader.bean.dao.BookDao;
import org.foree.bookreader.bean.event.BookUpdateEvent;
import org.foree.bookreader.homepage.BookShelfActivity;
import org.foree.bookreader.thread.SyncBooksThread;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class SyncService extends Service {
    public static final String TAG = SyncService.class.getSimpleName();

    public SyncService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // start sync
        BookDao bookDao = new BookDao(getApplicationContext());
        SyncBooksThread syncBooksThread = new SyncBooksThread(bookDao);

        syncBooksThread.start();
        return super.onStartCommand(intent, flags, startId);

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(final BookUpdateEvent bookUpdateEvent) {
        Log.d(TAG, "on SyncService: updated = " + bookUpdateEvent.getUpdatedNum());

        int updatedNovelNum = bookUpdateEvent.getUpdatedNum();

        // update UI
        if (updatedNovelNum > 0) {
            String title = updatedNovelNum + "本小说更新啦";
            String message = bookUpdateEvent.getUpdateBooksName();

            // send notification
            sendNotification(title, message);
        }

    }

    private void sendNotification(String title, String message) {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle(title)
                .setContentText(message);

        Intent resultIntent = new Intent(this, BookShelfActivity.class);
        resultIntent.putExtra("back", true);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, resultIntent, 0);
        mBuilder.setContentIntent(pendingIntent);

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(0, mBuilder.build());
    }
}
