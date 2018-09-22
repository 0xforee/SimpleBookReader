package org.foree.bookreader.service;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import org.foree.bookreader.bean.book.Book;
import org.foree.bookreader.bean.dao.BookDao;
import org.foree.bookreader.parser.WebParser;


/**
 * @author foree
 * @date 2018/9/22
 */
public class BookAddService extends IntentService {
    private static final String TAG = "BookAddService";
    public static final String EXTRA_PARAM1 = "bookUrl";
    private static final boolean DEBUG = false;

    public BookAddService() {
        super("BookAddService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            // get book url first
            String bookUrl = intent.getStringExtra(EXTRA_PARAM1);

            Book book = WebParser.getInstance().getBookInfo(bookUrl);
            if (book != null) {
                // get chapters
                book.setChapters(WebParser.getInstance().getContents(bookUrl, book.getContentUrl()));
                BookDao bookDao = new BookDao(this);
                bookDao.addBook(book);
                if (DEBUG) {
                    Log.d(TAG, "[foree] add book End");
                }
            } else {
                Log.e(TAG, "[foree] onHandleIntent: book is null, add error");
            }

        }
    }
}
