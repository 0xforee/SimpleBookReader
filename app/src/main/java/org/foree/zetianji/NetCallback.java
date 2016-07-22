package org.foree.zetianji;

/**
 * Created by foree on 16-7-19.
 * 数据回调函数
 */
public interface NetCallback {
    void onSuccess(String data);
    void onFail(String msg);
}
