package org.foree.bookreader.pagination;

/**
 * Created by foree on 17-2-7.
 */

public class ArticleRequest implements Comparable {
    private String url;
    private PaginationArgs paginationArgs;

    public ArticleRequest(String url, PaginationArgs paginationArgs) {
        this.url = url;
        this.paginationArgs = paginationArgs;
    }

    public String getUrl() {
        return url;
    }

    public PaginationArgs getPaginationArgs() {
        return paginationArgs;
    }

    @Override
    public int compareTo(Object another) {
        return 0;
    }
}
