package org.foree.bookreader.data.event;

import org.foree.bookreader.data.book.Book;

/**
 * Created by foree on 17-2-24.
 */

public class BookUpdateEvent {
    private Book book;
    private boolean updateChapters;

    public BookUpdateEvent(Book book, boolean updateChapters) {
        this.book = book;
        this.updateChapters = updateChapters;
    }

    public Book getBook() {
        return book;
    }

    public boolean isUpdateChapters() {
        return updateChapters;
    }
}
