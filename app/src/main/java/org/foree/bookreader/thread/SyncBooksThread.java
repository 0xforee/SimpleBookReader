package org.foree.bookreader.thread;

import android.util.Log;

import org.foree.bookreader.bean.book.Book;
import org.foree.bookreader.bean.book.Chapter;
import org.foree.bookreader.bean.dao.BookDao;
import org.foree.bookreader.bean.event.BookUpdateEvent;
import org.foree.bookreader.parser.AbsWebParser;
import org.foree.bookreader.parser.WebParserManager;
import org.foree.bookreader.utils.DateUtils;
import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

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
        int updatedNum = 0;
        List<Book> books = bookDao.getAllBooks();

        // init thread about
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        final List<Callable<Boolean>> tasks = new ArrayList<>();
        final List<Callable<Boolean>> chapterTasks = new ArrayList<>();
        List<Future<Boolean>> futures;
        // add task
        for (final Book oldBook : books) {
            if (oldBook != null) {
                Callable<Boolean> callable = new Callable<Boolean>() {
                    @Override
                    public Boolean call() throws Exception {

                        final AbsWebParser webParser = WebParserManager.getInstance().getWebParser(oldBook.getBookUrl());
                        final Book newBook = webParser.getBookInfo(oldBook.getBookUrl());

                        if (DateUtils.isNewer(oldBook.getUpdateTime(), newBook.getUpdateTime())) {
                            // update chapters
                            chapterTasks.add(new Callable<Boolean>() {
                                @Override
                                public Boolean call() throws Exception {
                                    List<Chapter> chapters = webParser.getChapterList(newBook.getBookUrl(), newBook.getContentUrl());
                                    if (chapters != null) {
                                        bookDao.insertChapters(chapters);
                                    }
                                    return false;
                                }
                            });

                            bookDao.updateBookTime(newBook.getBookUrl(), newBook.getUpdateTime());

                            return true;
                        }
                        return false;
                    }
                };

                tasks.add(callable);
            }
        }


        // results
        try {

            // 1. check update
            futures = executor.invokeAll(tasks);

            // 2. get results
            for (Future<Boolean> future : futures) {
                if (future.get()) {
                    updatedNum++;
                }
            }
            // 3. notify update
            EventBus.getDefault().post(new BookUpdateEvent(updatedNum));

            // 3. sync chapters
            executor.invokeAll(chapterTasks);


        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        executor.shutdown();


        Log.d(TAG, "costs " + (System.currentTimeMillis() - startTime) + " ms to check update, updated " + updatedNum);

    }
}
