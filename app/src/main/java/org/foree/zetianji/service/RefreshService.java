package org.foree.zetianji.service;

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

import org.foree.zetianji.R;
import org.foree.zetianji.base.BaseApplication;
import org.foree.zetianji.book.Chapter;
import org.foree.zetianji.book.Novel;
import org.foree.zetianji.dao.NovelDao;
import org.foree.zetianji.helper.BQGWebSiteHelper;
import org.foree.zetianji.helper.WebSiteInfo;
import org.foree.zetianji.net.NetCallback;
import org.foree.zetianji.utils.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class RefreshService extends Service {
    private static final String TAG = RefreshService.class.getSimpleName();

    private StreamCallBack mCallBack;
    SharedPreferences sp;
    BQGWebSiteHelper absWebSiteHelper;
    WebSiteInfo webSiteInfo;
    NovelDao novelDao;
    Notification notification;
    int successCount = 0;
    int failCount = 0;
    RemoteViews contentView;
    List<Chapter> chapterList;
    NotificationManager notificationManager;
    private MyBinder mBinder = new MyBinder();

    public RefreshService() {
    }

    Handler myHandler = new H();
    private class H extends Handler{
        private static final int MSG_DOWNLOAD_CHAPTER_DOWN = 0;
        private static final int MSG_DOWNLOAD_OK = 1;
        private static final int MSG_START_DOWNLOAD = 2;

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case MSG_START_DOWNLOAD:
                case MSG_DOWNLOAD_CHAPTER_DOWN:
                    downloadChapter(chapterList.get(successCount+failCount));
                    updateNotification(chapterList.size(), successCount+failCount+1);
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
        novelDao = new NovelDao(this);
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

    private void updateNotification(int chapterCounts, int updateCount) {
        Log.d(TAG, "updateNotification: " + updateCount);
        contentView.setTextViewText(R.id.notificationPercent,updateCount + "/" + chapterCounts);
        contentView.setProgressBar(R.id.notificationProgress, chapterCounts, updateCount, false);
        notification.contentView = contentView;
        notificationManager.notify(R.layout.notification_download, notification);
    }

    public void updateNovelInfo(final long id){
        // downloadNovel
        webSiteInfo = novelDao.findWebSiteById(id);
        absWebSiteHelper  = new BQGWebSiteHelper(webSiteInfo);
        absWebSiteHelper.getNovel(new NetCallback<Novel>() {
            @Override
            public void onSuccess(Novel data) {
                mCallBack.notifyUpdateCallBack(data);
            }

            @Override
            public void onFail(String msg) {
            }
        });

    }

    public void downloadNovel(List<Chapter> downloadList){
        // downloadNovel

        chapterList = downloadList;

        // create notification
        createNotification(chapterList.size());

        // post sync done
        Message msg = new Message();
        msg.what = H.MSG_DOWNLOAD_CHAPTER_DOWN;
        msg.arg1 = chapterList.size();

        myHandler.sendMessage(msg);

    }

    // sync data from server
    private void downloadChapter(final Chapter chapter) {
        // TODO:和数据库组合
        Thread downloadThread = new Thread() {
            @Override
            public void run() {
                Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
                    absWebSiteHelper.getChapterContent(chapter.getUrl(), webSiteInfo.getWeb_char(), new NetCallback<String>() {
                        @Override
                        public void onSuccess(String data) {
                            File chapterCache = new File(BaseApplication.getInstance().getCacheDirString()
                                    + File.separator + FileUtils.encodeUrl(chapter.getUrl()));
                            try {
                                if( data != null)
                                    FileUtils.writeFile(chapterCache, data);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            // post sync done
                            Message msg = new Message();
                            if (successCount == chapterList.size()-1)
                                msg.what = H.MSG_DOWNLOAD_OK;
                            else
                                msg.what = H.MSG_DOWNLOAD_CHAPTER_DOWN;

                            successCount++;
                            myHandler.sendMessage(msg);

                        }

                        @Override
                        public void onFail(String msg) {
                            failCount++;
                            Message msgError = new Message();
                            msgError.what = H.MSG_DOWNLOAD_CHAPTER_DOWN;
                            myHandler.sendMessage(msgError);
                        }
                    });
                }
        };
        downloadThread.start();
    }

    public interface StreamCallBack {
        // 数据同步结束，更新UI
        void notifyUpdateCallBack(Novel data);
    }
}
