package org.foree.bookreader.bean.book;

import java.util.List;

/**
 * Created by foree on 16-7-26.
 */
public class Book {
    private String bookName;
    // android sqlite中数据是弱类型，所以用String存取时间，然后再转格式
    private String updateTime;
    private String category;
    private String author;
    private String bookUrl;
    private int pageIndex;

    // set content url
    private String contentUrl;

    private String bookCoverUrl;
    private List<Chapter> chapters;
    private String description;
    private Chapter newestChapter;
    // recentChapterId默认为-1，在openBook时检查病初始化
    private String recentChapterUrl;

    public Book() {
    }

    public Book(String bookName, String bookUrl) {
        this(bookName, bookUrl, "", "", "", "", 0, "", "", "");
    }

    public Book(String bookName, String bookUrl, String updateTime, String category, String author,
                String description, int pageIndex, String recentChapterUrl, String bookCoverUrl, String contentUrl) {
        this.bookUrl = bookUrl;
        this.bookName = bookName;
        this.updateTime = updateTime;
        this.category = category;
        this.author = author;
        this.description = description;
        this.pageIndex = pageIndex;
        this.recentChapterUrl = recentChapterUrl;
        this.bookCoverUrl = bookCoverUrl;
        this.contentUrl = contentUrl;
    }

    public String getBookName() {
        return bookName;
    }

    public void setBookName(String bookName) {
        this.bookName = bookName;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public List<Chapter> getChapters() {
        return chapters;
    }

    public void setChapters(List<Chapter> chapterList) {
        this.chapters = chapterList;
    }

    public Chapter getNewestChapter() {
        return newestChapter;
    }

    public void setNewestChapter(Chapter newestChapter) {
        this.newestChapter = newestChapter;
    }

    public String getBookCoverUrl() {
        return bookCoverUrl;
    }

    public void setBookCoverUrl(String bookCoverUrl) {
        this.bookCoverUrl = bookCoverUrl;
    }

    public String getBookUrl() {
        return bookUrl;
    }

    public void setBookUrl(String bookUrl) {
        this.bookUrl = bookUrl;
    }

    public String getRecentChapterUrl() {
        return recentChapterUrl;
    }

    public void setRecentChapterUrl(String recentChapterUrl) {
        this.recentChapterUrl = recentChapterUrl;
    }

    public String getContentUrl() {
        return contentUrl;
    }

    public void setContentUrl(String contentUrl) {
        this.contentUrl = contentUrl;
    }

    public int getPageIndex() {
        return pageIndex;
    }

    public void setPageIndex(int pageIndex) {
        this.pageIndex = pageIndex;
    }
}
