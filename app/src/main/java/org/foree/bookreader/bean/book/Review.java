package org.foree.bookreader.bean.book;

import java.util.Date;

/**
 * @author foree
 * @date 2018/7/28
 * @description 书评条目
 */
public class Review {
    private String id;
    private Date updated;
    private Date created;
    private int commentCount;
    private String content;
    private String title;
    private int likeCount;
    private Author author;

    /**
     * 书籍评论的读者
     */
    public static class Author{
        private String id;
        private String avatar;
        private String nickname;
        private int lv;

        public Author() {
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getAvatar() {
            return avatar;
        }

        public void setAvatar(String avatar) {
            this.avatar = avatar;
        }

        public String getNickname() {
            return nickname;
        }

        public void setNickname(String nickname) {
            this.nickname = nickname;
        }

        public int getLv() {
            return lv;
        }

        public void setLv(int lv) {
            this.lv = lv;
        }

        @Override
        public String toString() {
            return "Author{" +
                    "id='" + id + '\'' +
                    ", avatar='" + avatar + '\'' +
                    ", nickname='" + nickname + '\'' +
                    ", lv=" + lv +
                    '}';
        }
    }

    public Review() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getUpdated() {
        return updated;
    }

    public void setUpdated(Date updated) {
        this.updated = updated;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public int getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(int commentCount) {
        this.commentCount = commentCount;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(int likeCount) {
        this.likeCount = likeCount;
    }

    public Author getAuthor() {
        return author;
    }

    public void setAuthor(Author author) {
        this.author = author;
    }

    @Override
    public String toString() {
        return "Review{" +
                "id='" + id + '\'' +
                ", updated='" + updated + '\'' +
                ", created='" + created + '\'' +
                ", commentCount=" + commentCount +
                ", content='" + content + '\'' +
                ", title='" + title + '\'' +
                ", likeCount=" + likeCount +
                ", author=" + author +
                '}';
    }
}
