package org.foree.bookreader.data.event;

import org.foree.bookreader.data.book.Article;

/**
 * Created by foree on 17-2-7.
 */

public class PaginationEvent {

    private Article article;
    private int state;

    // loading state
    public static final int STATE_FAILED = -1;
    public static final int STATE_LOADING = 0;
    public static final int STATE_SUCCESS = 1;

    public PaginationEvent(Article article, int state) {
        this.article = article;
        this.state = state;
    }

    public int getState() {
        return state;
    }

    public Article getArticle() {
        return article;
    }
}
