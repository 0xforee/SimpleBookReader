package org.foree.zetianji.helper;

/**
 * Created by foree on 16-8-12.
 */
public class WebSiteInfo {
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    private long id;
    private String web_char;
    private String host_url;
    private String index_page;
    private String name;

    public WebSiteInfo(){}

    public WebSiteInfo(String name, String host_url, String index_page, String web_char){
        this.name = name;
        this.host_url = host_url;
        this.index_page = index_page;
        this.web_char = web_char;
    }

    public WebSiteInfo(long id, String name, String host_url, String index_page, String web_char){
        this.id = id;
        this.name = name;
        this.host_url = host_url;
        this.index_page = index_page;
        this.web_char = web_char;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public String getWeb_char() {
        return web_char;
    }

    public void setWeb_char(String web_char) {
        this.web_char = web_char;
    }

    public String getHost_url() {
        return host_url;
    }

    public void setHost_url(String host_url) {
        this.host_url = host_url;
    }

    public String getIndex_page() {
        return index_page;
    }

    public void setIndex_page(String index_page) {
        this.index_page = index_page;
    }
}
