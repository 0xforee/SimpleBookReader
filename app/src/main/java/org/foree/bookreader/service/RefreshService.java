package org.foree.bookreader.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.RemoteViews;

import org.foree.bookreader.R;
import org.foree.bookreader.base.BaseApplication;
import org.foree.bookreader.book.Article;
import org.foree.bookreader.book.Book;
import org.foree.bookreader.book.Chapter;
import org.foree.bookreader.dao.BookDao;
import org.foree.bookreader.net.NetCallback;
import org.foree.bookreader.utils.FileUtils;
import org.foree.bookreader.website.BiQuGeWebInfo;
import org.foree.bookreader.website.WebInfo;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class RefreshService extends Service {
    private static final String TAG = RefreshService.class.getSimpleName();

    private StreamCallBack mCallBack;
    SharedPreferences sp;
    BookDao bookDao;
    Notification notification;
    int successCount = 0;
    int failCount = 0;
    Thread downloadThread;
    RemoteViews contentView;
    List<Chapter> chapterList;
    NotificationManager notificationManager;
    private MyBinder mBinder = new MyBinder();

    public RefreshService() {
    }

    Handler myHandler = new H();
    private class H extends Handler{
        private static final int MSG_UPDATE_CHAPTER_NOTIFICATION = 0;
        private static final int MSG_DOWNLOAD_OK = 1;
        private static final int MSG_START_DOWNLOAD = 2;

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case MSG_START_DOWNLOAD:
                case MSG_UPDATE_CHAPTER_NOTIFICATION:
                    updateNotification(successCount++);
                    break;
                case MSG_DOWNLOAD_OK:
                    contentView.setTextViewText(R.id.notificationPercent, "Success:" + successCount + "/Fail:" + failCount);
                    notification.flags = Notification.FLAG_AUTO_CANCEL;
                    notificationManager.notify(R.layout.notification_download, notification);
                    break;

            }
            super.handleMessage(msg);
        }
    }

    public class MyBinder extends Binder {
        public RefreshService getService(){
            return RefreshService.this;
        }
    }
    public void registerCallBack(StreamCallBack callback){
        mCallBack = callback;
    }

    public void unregisterCallBack(){
        mCallBack = null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");
        bookDao = new BookDao(this);
        sp = PreferenceManager.getDefaultSharedPreferences(BaseApplication.getInstance());
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        super.onDestroy();
        downloadThread.interrupt();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private void createNotification(int chapterCounts){
        notification = new Notification(R.drawable.ic_launcher, "正在下载", System.currentTimeMillis());
        notification.flags = Notification.FLAG_ONGOING_EVENT;

        contentView = new RemoteViews(getPackageName(), R.layout.notification_download);
        contentView.setTextViewText(R.id.notificationTitle, getString(R.string.is_downing));
        contentView.setTextViewText(R.id.notificationPercent, "0%");
        contentView.setProgressBar(R.id.notificationProgress, chapterCounts, 0, false);

        notification.contentView = contentView;

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(R.layout.notification_download, notification);
    }

    private void updateNotification(int currentCount) {
        Log.d(TAG, "updateNotification: " + currentCount++);
        contentView.setTextViewText(R.id.notificationPercent,currentCount + "/" + chapterList.size());
        contentView.setProgressBar(R.id.notificationProgress, chapterList.size(), currentCount, false);
        notification.contentView = contentView;
        notificationManager.notify(R.layout.notification_download, notification);
    }

    // TODO:可能无法准确获取到是否完全下载完成
    public void downloadNovel(List<Chapter> downloadList){
        // downloadNovel

        chapterList = downloadList;

        // create notification
        createNotification(chapterList.size());

        // download chapters
        downloadThread = new Thread(){
            @Override
            public void run() {
                super.run();
                Thread.currentThread().setPriority(Thread.MIN_PRIORITY);

                for(Chapter chapter: chapterList){
                    downloadChapter(chapter);
                }

                // downloadAllNovelDone
                myHandler.sendEmptyMessage(H.MSG_DOWNLOAD_OK);
            }
        };

        downloadThread.start();
    }

    // sync data from server
    private void downloadChapter(final Chapter chapter) {
        WebInfo webInfo = new BiQuGeWebInfo();
        webInfo.getArticle(chapter.getChapterUrl(), new NetCallback<Article>() {
            @Override
            public void onSuccess(Article data) {
                File chapterCache = new File(BaseApplication.getInstance().getCacheDirString()
                        + File.separator + FileUtils.encodeUrl(chapter.getChapterUrl()));
                try {
                    if( data != null)
                        FileUtils.writeFile(chapterCache, data.getContents());
                } catch (IOException e) {
                    e.printStackTrace();
                }

                // update db download column

                // post sync done
                myHandler.sendEmptyMessage(H.MSG_UPDATE_CHAPTER_NOTIFICATION);

            }

            @Override
            public void onFail(String msg) {

            }
        });
    }

    public interface StreamCallBack {
        // 数据同步结束，更新UI
        void notifyUpdateCallBack(Book data);
    }
}
