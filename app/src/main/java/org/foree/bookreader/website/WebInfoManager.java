package org.foree.bookreader.website;

/**
 * Created by foree on 17-1-18.
 * 管理WebInfo（小说源）的切换逻辑
 */
public class WebInfoManager {
    private static WebInfo webInfo;
    private static WebInfoManager ourInstance = new WebInfoManager();

    public static WebInfoManager getInstance() {
        return ourInstance;
    }

    private WebInfoManager() {
    }

    public static WebInfo getWebInfo(){
        if( webInfo == null){
            webInfo = new BiQuGeWebInfo();
        }

        return webInfo;
    }
}
