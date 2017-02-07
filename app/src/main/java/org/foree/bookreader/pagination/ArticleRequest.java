package org.foree.bookreader.pagination;

/**
 * Created by foree on 17-2-7.
 */

public class ArticleRequest implements Comparable{
    private Pagination pagination;
    private String url;

    public ArticleRequest(Pagination pagination, String url) {
        this.pagination = pagination;
        this.url = url;
    }

    public Pagination getPagination() {
        return pagination;
    }

    public String getUrl() {
        return url;
    }

    public void setPagination(Pagination newValue) {
        this.pagination = newValue;
    }

    @Override
    public int compareTo(Object another) {
        return 0;
    }
}
