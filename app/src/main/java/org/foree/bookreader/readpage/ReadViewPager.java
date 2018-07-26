package org.foree.bookreader.readpage;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.WindowManager;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

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
    private boolean mClick;

    private onPageAreaClickListener onPageAreaClickListener;

    public ReadViewPager(Context context) {
        this(context, null);
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

    public void setPreScrollDisable(boolean state) {
        Log.d(TAG, "[foree] setPreScrollDisable: " + state);
        mPreScrollDisable = state;
    }

    public void setPostScrollDisable(boolean state) {
        Log.d(TAG, "[foree] setPostScrollDisable: " + state);
        mPostScrollDisable = state;
    }

    /**
     * Call this view's OnClickListener, if it is defined.  Performs all normal
     * actions associated with clicking: reporting accessibility event, playing
     * a sound, etc.
     *
     * @return True there was an assigned OnClickListener that was called, false
     * otherwise is returned.
     */
    @Override
    public boolean performClick() {
        Log.d(TAG, "[foree] performClick: ");
        return super.performClick();
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        Log.d(TAG, "[foree] onTouchEvent: mPreScrollDisable = " + mPreScrollDisable + ", mPostScrollDisable = " + mPostScrollDisable);
        switch (ev.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                mStartX = ev.getX();
                mStartY = ev.getY();
                mClick = true;
                break;
            case MotionEvent.ACTION_MOVE:
                float offset = ev.getX() - mStartX;
                if (offset < 0) {
                    mClick = false;
                    if (DEBUG) Log.d(TAG, "[foree] onTouchEvent: 向右滑动");
                    if (mPostScrollDisable) {
                        return true;
                    }
                } else if (offset > 0) {
                    mClick = false;
                    if (DEBUG) Log.d(TAG, "[foree] onTouchEvent: 向左滑动");
                    if (mPreScrollDisable) {
                        return true;
                    }
                } else {
                    if (DEBUG) Log.d(TAG, "[foree] onTouchEvent: Click");
                    mClick = true;
                }
                break;
            case MotionEvent.ACTION_UP:
                if (mClick) {
                    // menu click
                    if (!isMenuArea(ev)) {
                        if (isPrePageArea(ev)) {
                            if (DEBUG) Log.d(TAG, "上一页");
                            if (!mPreScrollDisable) {
                                setPopulate(getCurrentItem() - 1);
                            }
                        } else {
                            if (DEBUG) Log.d(TAG, "下一页");
                            if (!mPostScrollDisable) {
                                setPopulate(getCurrentItem() + 1);
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

    private void setPopulate(int position){
        try {
            Class<?> clazz = this.getClass().getSuperclass();
            Field field = clazz.getDeclaredField("mPopulatePending");
            field.setAccessible(true);
            field.setBoolean(this, true);

            Method method = clazz.getDeclaredMethod("setCurrentItemInternal", int.class, boolean.class, boolean.class);
            method.setAccessible(true);
            method.invoke(this, position, false, true);

        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}

