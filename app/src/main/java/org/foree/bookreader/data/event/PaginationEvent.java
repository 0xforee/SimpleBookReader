package org.foree.bookreader.data.event;

import org.foree.bookreader.data.book.Chapter;

/**
 * Created by foree on 17-2-7.
 */

public class PaginationEvent {

    private Chapter chapter;

    public PaginationEvent(Chapter chapter) {
        this.chapter = chapter;
    }

    public Chapter getChapter() {
        return chapter;
    }
}
