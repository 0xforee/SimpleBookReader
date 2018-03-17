package org.foree.bookreader.thread;

import android.util.Log;

import org.foree.bookreader.bean.book.Book;
import org.foree.bookreader.bean.book.Chapter;
import org.foree.bookreader.bean.dao.BookDao;
import org.foree.bookreader.bean.event.BookUpdateEvent;
import org.foree.bookreader.parser.WebParserProxy;
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
        ArrayList<Book> updatedBooks = new ArrayList<>();
        List<Book> books = bookDao.getAllBooks();

        // init thread about
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        final List<Callable<Book>> tasks = new ArrayList<>();
        final List<Callable<Boolean>> chapterTasks = new ArrayList<>();
        List<Future<Book>> futures;
        // add task
        for (final Book oldBook : books) {
            if (oldBook != null) {
                Callable<Book> callable = new Callable<Book>() {
                    @Override
                    public Book call() throws Exception {

                        final Book newBook = WebParserProxy.getInstance().getBookInfo(oldBook.getBookUrl());

                        if (DateUtils.isNewer(oldBook.getUpdateTime(), newBook.getUpdateTime())) {
                            // update chapters
                            chapterTasks.add(new Callable<Boolean>() {
                                @Override
                                public Boolean call() throws Exception {
                                    List<Chapter> chapters = WebParserProxy.getInstance().getContents(newBook.getBookUrl(), newBook.getContentUrl());
                                    if (chapters != null) {
                                        bookDao.insertChapters(chapters);
                                    }
                                    return false;
                                }
                            });

                            bookDao.updateBookTime(newBook.getBookUrl(), newBook.getUpdateTime());

                            return newBook;
                        }
                        return null;
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
            for (Future<Book> future : futures) {
                if (future.get() != null) {
                    updatedBooks.add(future.get());
                }
            }
            // 3. notify update
            EventBus.getDefault().post(new BookUpdateEvent(updatedBooks));

            // 3. sync chapters
            executor.invokeAll(chapterTasks);


        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        executor.shutdown();


        Log.d(TAG, "costs " + (System.currentTimeMillis() - startTime) + " ms to check update, updated " + updatedBooks.size());

    }
}
