package org.foree.bookreader.data.event;

import org.foree.bookreader.data.book.Chapter;

/**
 * Created by foree on 17-2-7.
 */

public class PaginationEvent {

    private Chapter chapter;
    private int state;

    // loading state
    public static final int STATE_FAILED = -1;
    public static final int STATE_LOADING = 0;
    public static final int STATE_SUCCESS = 1;

    public PaginationEvent(Chapter chapter, int state) {
        this.chapter = chapter;
        this.state = state;
    }

    public int getState() {
        return state;
    }

    public Chapter getChapter() {
        return chapter;
    }
}
