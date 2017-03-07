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
    private static final boolean DEBUG = false;

    private int displayWidth;
    private int displayHeight;

    private float startX = 0;
    private float startY = 0;
    private boolean scrolled = false;

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


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startX = event.getX();
                startY = event.getY();
                scrolled = false;
                break;
            case MotionEvent.ACTION_UP:
                // 点击事件
                if (startX == event.getX() && startY == event.getY()) {

                    if (!isMenuArea(event)) {
                        if (isPrePageArea(event)) {
                            if (getCurrentItem() > 0) {
                                if (DEBUG) Log.d(TAG, "上一页");
                                setCurrentItem(getCurrentItem() - 1, false);
                            } else {
                                if (DEBUG) Log.d(TAG, "上一章");
                                if (onPageAreaClickListener != null) {
                                    onPageAreaClickListener.onPreChapterClick();
                                }
                            }
                        } else {
                            if (getAdapter() != null)
                                if (getCurrentItem() < getAdapter().getCount() - 1) {
                                    if (DEBUG) Log.d(TAG, "下一页");
                                    setCurrentItem(getCurrentItem() + 1, false);
                                } else {
                                    // 切换下一章
                                    if (DEBUG) Log.d(TAG, "下一章");
                                    if (onPageAreaClickListener != null) {
                                        onPageAreaClickListener.onNextChapterClick();
                                    }
                                }
                        }
                    } else {
                        if (DEBUG) Log.d(TAG, "呼出菜单");
                        if (onPageAreaClickListener != null) {
                            onPageAreaClickListener.onMediumAreaClick();
                        }
                    }
                }


                break;
            case MotionEvent.ACTION_MOVE:
                if (!scrolled) {
                    if (getCurrentItem() == 0 && startX < event.getX()) {
                        scrolled = true;
                        if (onPageAreaClickListener != null) {
                            onPageAreaClickListener.onPreChapterClick();
                        }
                        if (DEBUG) Log.d(TAG, "滑动上一章");

                    } else if (getCurrentItem() == getAdapter().getCount() - 1 && startX > event.getX()) {
                        scrolled = true;
                        if (onPageAreaClickListener != null) {
                            onPageAreaClickListener.onNextChapterClick();
                        }
                        if (DEBUG) Log.d(TAG, "滑动下一章");
                    }
                }
                break;
        }
        if (DEBUG) Log.d(TAG, "x = " + event.getX() + ", y = " + event.getY());

        return true;
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

        void onPreChapterClick();

        void onNextChapterClick();
    }
}

