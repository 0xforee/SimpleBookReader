package org.foree.bookreader.bean.book;

import java.util.Date;

/**
 * @author foree
 * @date 2018/7/13
 * @description 书源信息
 */
public class Source {
    private String sourceId;
    private String sourceName;
    private String sourceLink;
    private String lastChapter;
    private int chapterCount;
    private boolean isCharge;
    private boolean starting;
    private String host;
    private Date updated;

    public String getSourceId() {
        return sourceId;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    public String getSourceName() {
        return sourceName;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    public String getSourceLink() {
        return sourceLink;
    }

    public void setSourceLink(String sourceLink) {
        this.sourceLink = sourceLink;
    }

    public String getLastChapter() {
        return lastChapter;
    }

    public void setLastChapter(String lastChapter) {
        this.lastChapter = lastChapter;
    }

    public int getChapterCount() {
        return chapterCount;
    }

    public void setChapterCount(int chapterCount) {
        this.chapterCount = chapterCount;
    }

    public boolean isCharge() {
        return isCharge;
    }

    public void setCharge(boolean charge) {
        isCharge = charge;
    }

    public boolean isStarting() {
        return starting;
    }

    public void setStarting(boolean starting) {
        this.starting = starting;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Date getUpdated() {
        return updated;
    }

    public void setUpdated(Date updated) {
        this.updated = updated;
    }
}
