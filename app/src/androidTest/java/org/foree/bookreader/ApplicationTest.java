package org.foree.bookreader;

import android.app.Application;
import android.test.ApplicationTestCase;
import android.util.Log;

import org.foree.bookreader.bean.book.Book;
import org.foree.bookreader.bean.dao.BookDao;
import org.foree.bookreader.net.NetCallback;
import org.foree.bookreader.parser.AbsWebParser;
import org.foree.bookreader.parser.WebParserManager;

import java.util.List;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class ApplicationTest extends ApplicationTestCase<Application> {
    private static final String TAG = ApplicationTest.class.getSimpleName();

    public ApplicationTest() {
        super(Application.class);

    }

    public void testAddBookUnique() {
        BookDao bookDao = new BookDao(getContext());

        // 测试book_url是不是unique
        Book book = new Book("hahah1", "http://m.bxwx9.org/b/98/98289/");
        Book book1 = new Book("hahahh2", "http://m.bxwx9.org/b/98/98289/");
        bookDao.addBook(book);
        bookDao.addBook(book1);

        List<Book> bookList = bookDao.getAllBooks();
        assertEquals(bookList.size(), 4);
    }

    public void testRemoveBook() {
        BookDao bookDao = new BookDao(getContext());

        String book_url = "http://m.bxwx9.org/b/98/98289/";
        bookDao.removeBook(book_url);
    }

    public void testParseBookInfo() {
        String url = "http://www.biquge.com/0_168/";
        AbsWebParser absWebParser = WebParserManager.getInstance().getWebParser(url);
        absWebParser.getBookInfo(url, new NetCallback<Book>() {
            @Override
            public void onSuccess(Book data) {

            }

            @Override
            public void onFail(String msg) {

            }
        });
    }

    public void testGetChapterByUrl() {
        BookDao bookDao = new BookDao(getContext());
        String chapterUrl = "http://www.biquge.com/14_14929/8194590.html";
        String nextChapterUrl = bookDao.getChapterUrl(-1, chapterUrl);

        Log.d(TAG, "nextChapterUrl = " + nextChapterUrl);
    }
}