package org.foree.bookreader.service;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import org.foree.bookreader.R;
import org.foree.bookreader.base.GlobalConfig;
import org.foree.bookreader.bean.book.Book;
import org.foree.bookreader.bean.book.Chapter;
import org.foree.bookreader.bean.book.SearchHotWord;
import org.foree.bookreader.bean.dao.BookDao;
import org.foree.bookreader.bean.event.BookUpdateEvent;
import org.foree.bookreader.homepage.BookShelfActivity;
import org.foree.bookreader.net.NetCallback;
import org.foree.bookreader.parser.WebParser;
import org.foree.bookreader.utils.DateUtils;
import org.foree.bookreader.utils.FileUtils;
import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * @author foree
 * @date 2018/9/22
 */
public class SyncService extends IntentService {
    private static final String TAG = "SyncService";
    public static final String ACTION_SYNC = "org.foree.bookreader.service.sync";
    public static final String EXTRA_NOTIFY = "notify";

    public static final String ACTION_ADD = "org.foree.bookreader.service.add";
    public static final String EXTRA_PARAM_BOOK_URL = "bookUrl";

    public static final String ACTION_HOTWORD = "org.foree.bookreader.service.hotwords";
    private static final boolean DEBUG = true;

    private BookDao mBookDao;

    public SyncService() {
        super("SyncService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mBookDao = new BookDao(this);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_SYNC.equals(action)) {
                handleActionSync(intent);
            } else if (ACTION_ADD.equals(action)) {
                handleActionAdd(intent);
            } else if (ACTION_HOTWORD.equals(action)) {
                handleActionHotword(intent);
            }
        }
    }

    private void handleActionHotword(Intent intent) {
        File externalDir = getExternalFilesDir("");
        if (externalDir != null) {
            try {
                if (externalDir.isDirectory()) {
                    final File hotwords = new File(externalDir, GlobalConfig.FILE_NAME_SEARCH_HOTWORD);
                    boolean shouldSync = !hotwords.exists() || System.currentTimeMillis() - hotwords.lastModified() > 1000 * 60;
                    if (!hotwords.exists()) {
                        hotwords.createNewFile();
                    }

                    if (shouldSync) {
                        WebParser.getInstance().getHotWordsAsync("", new NetCallback<List<SearchHotWord>>() {
                            @Override
                            public void onSuccess(final List<SearchHotWord> data) {
                                StringBuilder sb = new StringBuilder();
                                for (int i = 0; i < data.size(); i++) {
                                    sb.append(data.get(i).getWord()).append(" ");
                                }
                                try {
                                    FileUtils.writeFile(hotwords, sb.toString());
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void onFail(String msg) {

                            }
                        });
                    }

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Log.e(TAG, "handleActionHotword: exteranl dir is null");
        }
    }

    private void handleActionSync(Intent intent) {
        boolean notify = intent.getBooleanExtra(EXTRA_NOTIFY, false);

        long startTime = System.currentTimeMillis();
        ArrayList<Book> updatedBooks = new ArrayList<>();
        List<Book> books = mBookDao.getAllBooks();

        try {
            for (Book oldBook : books) {
                if (oldBook != null) {
                    final Book newBook = WebParser.getInstance().getBookInfo(oldBook.getBookUrl());
                    if (newBook.getUpdateTime().after(oldBook.getUpdateTime())) {
                        Log.d(TAG, "get chapters");
                        List<Chapter> chapters = WebParser.getInstance().getContents(newBook.getBookUrl(), newBook.getContentUrl());
                        if (chapters != null) {
                            // TODO: 检查切换源会不会导致原有的章节缓存没有删除
                            mBookDao.insertChapters(oldBook.getBookUrl(), chapters);
                        }

                        mBookDao.updateBookTime(newBook.getBookUrl(), DateUtils.formatDateToString(newBook.getUpdateTime()));
                        updatedBooks.add(newBook);
                    }
                }

            }
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }

        // notify update anyway（不能总是更新，如果检查失败，发送空内容）
        BookUpdateEvent bookEvent = new BookUpdateEvent(updatedBooks);
        Log.d(TAG, "post event bus");
        EventBus.getDefault().post(bookEvent);

        Log.d(TAG, "costs " + (System.currentTimeMillis() - startTime) + " ms to check update, updated " + updatedBooks.size());

        // update UI
        if (notify && updatedBooks.size() > 0) {
            String title = updatedBooks.size() + "本小说更新啦";
            String message = bookEvent.getUpdateBooksName();

            // send notification
            sendNotification(title, message);
        }
    }

    private void handleActionAdd(Intent intent) {
        // get book url first
        String bookUrl = intent.getStringExtra(EXTRA_PARAM_BOOK_URL);

        Book book = WebParser.getInstance().getBookInfo(bookUrl);
        if (book != null) {
            book.setUpdateTime(new Date());
            // get chapters
            book.setChapters(WebParser.getInstance().getContents(bookUrl, book.getContentUrl()));
            mBookDao.addBook(book);
            if (DEBUG) {
                Log.d(TAG, "[foree] add book End");
            }
        } else {
            Log.e(TAG, "[foree] onHandleIntent: book is null, add error");
        }

    }

    private void sendNotification(String title, String message) {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle(title)
                .setContentText(message)
                .setDefaults(Notification.DEFAULT_ALL);

        Intent resultIntent = new Intent(this, BookShelfActivity.class);
        resultIntent.putExtra("back", true);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(pendingIntent);

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        Notification notification = mBuilder.build();
        notification.flags |= Notification.FLAG_AUTO_CANCEL;

        manager.notify(0, notification);
    }
}
