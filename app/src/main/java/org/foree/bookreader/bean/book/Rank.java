package org.foree.bookreader.bean.book;

/**
 * @author foree
 * @date 2018/8/18
 * @description
 */
public class Rank {
    private String id;
    private String title;
    private String cover;
    private boolean collapse;
    private String monthRank;
    private String totalRank;
    private String shortTitle;
    private String group;

    private Rank(Builder builder) {
        id = builder.id;
        title = builder.title;
        cover = builder.cover;
        collapse = builder.collapse;
        monthRank = builder.monthRank;
        totalRank = builder.totalRank;
        shortTitle = builder.shortTitle;
        group = builder.group;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getCover() {
        return cover;
    }

    public boolean isCollapse() {
        return collapse;
    }

    public String getMonthRank() {
        return monthRank;
    }

    public String getTotalRank() {
        return totalRank;
    }

    public String getShortTitle() {
        return shortTitle;
    }

    public String getGroup() {
        return group;
    }

    @Override
    public String toString() {
        return "Rank{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", cover='" + cover + '\'' +
                ", collapse=" + collapse +
                ", monthRank='" + monthRank + '\'' +
                ", totalRank='" + totalRank + '\'' +
                ", shortTitle='" + shortTitle + '\'' +
                ", group='" + group + '\'' +
                '}';
    }

    public static final class Builder {
        private String id;
        private String title;
        private String cover;
        private boolean collapse;
        private String monthRank;
        private String totalRank;
        private String shortTitle;
        private String group;

        public Builder() {
        }

        private Builder(Builder builder) {
            group = builder.group;
        }

        public Builder id(String val) {
            id = val;
            return this;
        }

        public Builder title(String val) {
            title = val;
            return this;
        }

        public Builder cover(String val) {
            cover = val;
            return this;
        }

        public Builder collapse(boolean val) {
            collapse = val;
            return this;
        }

        public Builder monthRank(String val) {
            monthRank = val;
            return this;
        }

        public Builder totalRank(String val) {
            totalRank = val;
            return this;
        }

        public Builder shortTitle(String val) {
            shortTitle = val;
            return this;
        }

        public Builder group(String val) {
            group = val;
            return this;
        }

        public Rank build() {
            return new Rank(this);
        }
    }
}
