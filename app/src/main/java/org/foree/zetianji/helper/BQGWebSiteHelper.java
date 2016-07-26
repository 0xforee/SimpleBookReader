package org.foree.zetianji.helper;

/**
 * Created by foree on 16-7-26.
 */
public class BQGWebSiteHelper extends AbsWebSiteHelper{
    private String websiteCharSet = "utf-8";
    private String host_url = "http://www.biquge.com";
    private String index_page="/0_168/";

    public String getWebsiteCharSet() {
        return websiteCharSet;
    }

    public String getHost_url() {
        return host_url;
    }

    public String getIndex_page() {
        return index_page;
    }
}
