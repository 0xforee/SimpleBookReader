package org.foree.bookreader.bean.event;

import org.foree.bookreader.bean.book.Chapter;

/**
 * Created by foree on 17-2-7.
 */

public class PaginationEvent {

    private Chapter chapter;
    private boolean current;

    public PaginationEvent(Chapter chapter, boolean current) {
        this.current = current;
        this.chapter = chapter;
    }

    public Chapter getChapter() {
        return chapter;
    }

    public boolean isCurrent(){
        return current;
    }
}
