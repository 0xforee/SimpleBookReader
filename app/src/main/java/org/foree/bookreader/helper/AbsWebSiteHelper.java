package org.foree.bookreader.helper;

import org.foree.bookreader.book.Book;
import org.foree.bookreader.net.NetCallback;

/**
 * Created by foree on 16-7-26.
 */
public abstract class AbsWebSiteHelper {
    public abstract String getWebsiteCharSet();
    public abstract void getNovel(NetCallback<Book> netCallback);
    public abstract void getChapterContent(String chapter_url, String webChar, NetCallback<String> netCallback);
}
