package org.foree.zetianji.book;

import java.util.List;

/**
 * Created by foree on 16-7-26.
 */
public class Novel{
    private String book_name;
    private String update_time;
    private String category;
    private String author;
    private List<Chapter> chapter_list;
    private Chapter newest_chapter;

    public String getBook_name() {
        return book_name;
    }

    public void setBook_name(String book_name) {
        this.book_name = book_name;
    }

    public String getUpdate_time() {
        return update_time;
    }

    public void setUpdate_time(String update_time) {
        this.update_time = update_time;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public List<Chapter> getChapter_list() {
        return chapter_list;
    }

    public void setChapter_list(List<Chapter> chapter_list) {
        this.chapter_list = chapter_list;
    }

    public Chapter getNewest_chapter() {
        return newest_chapter;
    }

    public void setNewest_chapter(Chapter newest_chapter) {
        this.newest_chapter = newest_chapter;
    }
}
