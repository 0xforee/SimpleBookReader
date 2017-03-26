package org.foree.bookreader.bean.event;

import org.foree.bookreader.bean.book.Book;

import java.util.ArrayList;

/**
 * Created by foree on 17-2-24.
 */

public class BookUpdateEvent {
    private ArrayList<Book> updateBooks;

    public BookUpdateEvent(ArrayList<Book> updateBooks) {
        this.updateBooks = updateBooks;
    }

    public int getUpdatedNum() {
        return updateBooks.size();
    }

    public String getUpdateBooksName() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < updateBooks.size(); i++) {
            sb.append("《").append(updateBooks.get(i).getBookName()).append("》");
            if (i != updateBooks.size() - 1){
                sb.append(",");
            }
        }

        return sb.toString();
    }
}
