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

    }
}
