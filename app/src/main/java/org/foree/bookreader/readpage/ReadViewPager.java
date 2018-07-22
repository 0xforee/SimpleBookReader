package org.foree.bookreader.readpage;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.WindowManager;

/**
 * Created by foree on 17-2-8.
 * 自定义ViewPager
 * 1. 点击中间呼出菜单
 * 2. 点击左右区域上下翻页，首页末页切换章节
 * 3. 首页末页可以滑动切换章节
 */

public class ReadViewPager extends ViewPager {
    private static final String TAG = ReadViewPager.class.getSimpleName();
    private static final boolean DEBUG = true;

    private int displayWidth;
    private int displayHeight;

    private float mStartX = 0;
    private float mStartY = 0;

    private onPageAreaClickListener onPageAreaClickListener;

    public ReadViewPager(Context context) {
        super(context);
    }

    public ReadViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(metrics);

        displayHeight = metrics.heightPixels;
        displayWidth = metrics.widthPixels;
    }

    private boolean mPreScrollDisable, mPostScrollDisable;

    public void setPreScrollDisable(boolean state){
        Log.d(TAG, "[foree] setPreScrollDisable: " + state);
        mPreScrollDisable = state;
    }

    public void setPostScrollDisable(boolean state){
        Log.d(TAG, "[foree] setPostScrollDisable: " + state);
        mPostScrollDisable = state;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        switch (ev.getAction()){
            case MotionEvent.ACTION_DOWN:
                mStartX = ev.getX();
                mStartY = ev.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                float offset = ev.getX() - mStartX;
                Log.d(TAG, "[foree] onTouchEvent: offset = " + offset);
                if (offset < 0){
                    Log.d(TAG, "[foree] onTouchEvent: 向右滑动");
                    if(mPostScrollDisable){
                        return true;
                    }
                }else if (offset > 0){
                    Log.d(TAG, "[foree] onTouchEvent: 向左滑动");
                    if(mPreScrollDisable){
                        return true;
                    }
                }else{

                    // menu click
                    if (!isMenuArea(ev)) {
                        if (isPrePageArea(ev)) {
                            if (DEBUG) Log.d(TAG, "上一页");
                            setCurrentItem(getCurrentItem() - 1, false);
                        } else {
                            if (DEBUG) Log.d(TAG, "下一页");
                            setCurrentItem(getCurrentItem() + 1, false);

                        }
                    } else {
                        if (DEBUG) Log.d(TAG, "呼出菜单");
                        if (onPageAreaClickListener != null) {
                            onPageAreaClickListener.onMediumAreaClick();
                        }
                    }
                }
                break;
            default:

        }

        if (DEBUG) Log.d(TAG, "x = " + ev.getX() + ", y = " + ev.getY());

        return super.onTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return super.onInterceptTouchEvent(ev);
    }

    private boolean isMenuArea(MotionEvent event) {
        // 取横5分竖3分屏幕的区域
        if (event.getX() > displayWidth * 2 / 5 && event.getX() < displayWidth * 3 / 5) {
            if (event.getY() > displayHeight / 3 && event.getY() < displayHeight * 2 / 3) {
                return true;
            }
        }
        return false;
    }

    private boolean isPrePageArea(MotionEvent event) {
        // 取屏幕左半边区域
        return (event.getX() < displayWidth / 2);
    }

    private boolean isNextPageArea(MotionEvent event) {
        return (event.getX() > displayWidth / 2);
    }

    public void setOnPageAreaClickListener(onPageAreaClickListener onPageAreaClickListener) {
        this.onPageAreaClickListener = onPageAreaClickListener;
    }

    public interface onPageAreaClickListener {
        void onMediumAreaClick();
    }
}

