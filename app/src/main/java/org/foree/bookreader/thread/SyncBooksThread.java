package org.foree.bookreader.thread;

import android.util.Log;

import org.foree.bookreader.bean.book.Book;
import org.foree.bookreader.bean.book.Chapter;
import org.foree.bookreader.bean.dao.BookDao;
import org.foree.bookreader.bean.event.BookUpdateEvent;
import org.foree.bookreader.parser.WebParser;
import org.foree.bookreader.utils.DateUtils;
import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by foree on 17-2-27.
 */

public class SyncBooksThread extends Thread {
    private static final String TAG = SyncBooksThread.class.getSimpleName();
    private BookDao bookDao;

    public SyncBooksThread(BookDao bookDao) {
        this.bookDao = bookDao;
    }

    @Override
    public void run() {
        long startTime = System.currentTimeMillis();
        ArrayList<Book> updatedBooks = new ArrayList<>();
        List<Book> books = bookDao.getAllBooks();

        try {
            for (Book oldBook : books) {
                if (oldBook != null) {
                    final Book newBook = WebParser.getInstance().getBookInfo(oldBook.getBookUrl());
                    if (newBook.getUpdateTime().after(oldBook.getUpdateTime())) {
                        List<Chapter> chapters = WebParser.getInstance().getContents(newBook.getBookUrl(), newBook.getContentUrl());
                        if (chapters != null) {
                            bookDao.insertChapters(oldBook.getBookUrl(), chapters);
                        }

                        bookDao.updateBookTime(newBook.getBookUrl(), DateUtils.formatDateToString(newBook.getUpdateTime()));
                        updatedBooks.add(newBook);
                    }
                }

            }
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }

        // notify update anyway（不能总是更新，如果检查失败，发送空内容）
        EventBus.getDefault().post(new BookUpdateEvent(updatedBooks));

        Log.d(TAG, "costs " + (System.currentTimeMillis() - startTime) + " ms to check update, updated " + updatedBooks.size());
    }
}
