package org.foree.bookreader.net;

/**
 * Created by foree on 16-7-19.
 * 数据回调函数
 */
public interface NetCallback<T> {
    void onSuccess(T data);

    void onFail(String msg);
}
