package org.foree.bookreader.bean.book;

import java.util.Date;
import java.util.List;

/**
 * Created by foree on 16-7-26.
 */
public class Book {
    private String bookName;
    // android sqlite中数据是弱类型，所以用String存取时间，然后再转格式
    private Date updateTime;
    /**
     * 用户点击进入书籍的时间
     * 1. 和updateTime比较来判断是否显示小圆点
     * 2. 用于书架书籍的排序
     */
    private Date modifiedTime;
    private String category;
    private String author;
    private String bookUrl;
    /**
     * 当前阅读到的章节的分页
     */
    private int pageIndex;

    /**
     * contentUrl = sourceId默认为第一个（因为zhuishuapi是第一个，默认不可用，所以第一和第二交换）
     */
    private String contentUrl;

    private String bookCoverUrl;
    private List<Chapter> chapters;
    private String description;
    private Chapter newestChapter;
    /**
     * recentChapterId默认为-1，在openBook时检查病初始化
     */
    private String recentChapterUrl;
    private String rectentChapterTitle;

    /**
     * 是否连载
     */
    private boolean isSerial;
    /**
     * 书籍字数
     */
    private int wordCount;
    /**
     * 用于标记来源的key，一般为host url
     */
    private String sourceKey;

    public Book() {
    }

    public Book(String bookName, String bookUrl) {
        this(bookName, bookUrl, "", "", "");
    }

    public Book(String bookName, String bookUrl, String bookCoverUrl, String category, String description) {
        this(bookName, bookUrl, new Date(), category, "", description, 0, "", bookCoverUrl, "");
    }

    public Book(String bookName, String bookUrl, Date updateTime, String category, String author,
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

    public Book(String bookName, String bookUrl, Date updateTime, Date modifiedTime, String category, String author,
                String description, int pageIndex, String recentChapterUrl, String bookCoverUrl, String contentUrl) {
        this.bookName = bookName;
        this.updateTime = updateTime;
        this.modifiedTime = modifiedTime;
        this.category = category;
        this.author = author;
        this.bookUrl = bookUrl;
        this.pageIndex = pageIndex;
        this.contentUrl = contentUrl;
        this.bookCoverUrl = bookCoverUrl;
        this.description = description;
        this.recentChapterUrl = recentChapterUrl;
    }

    public String getBookName() {
        return bookName;
    }

    public void setBookName(String bookName) {
        this.bookName = bookName;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
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

    public Date getModifiedTime() {
        return modifiedTime;
    }

    public void setModifiedTime(Date modifiedTime) {
        this.modifiedTime = modifiedTime;
    }

    public String getRectentChapterTitle() {
        return rectentChapterTitle;
    }

    public void setRectentChapterTitle(String rectentChapterTitle) {
        this.rectentChapterTitle = rectentChapterTitle;
    }

    public boolean isSerial() {
        return isSerial;
    }

    public void setSerial(boolean serial) {
        isSerial = serial;
    }

    public int getWordCount() {
        return wordCount;
    }

    public void setWordCount(int wordCount) {
        this.wordCount = wordCount;
    }

    public String getSourceKey() {
        return sourceKey;
    }

    public void setSourceKey(String sourceKey) {
        this.sourceKey = sourceKey;
    }
}
