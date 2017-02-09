package org.foree.bookreader.data.cache;

import org.foree.bookreader.data.book.Article;

/**
 * Created by foree on 17-2-6.
 */

public abstract class ArticleCache {
    public abstract Article get(String chapterUrl);

    public abstract void put(String chapterUrl, Article article);
}
