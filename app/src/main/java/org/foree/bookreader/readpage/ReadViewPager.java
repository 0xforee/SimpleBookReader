package org.foree.bookreader.readpage;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.WindowManager;

import org.foree.bookreader.readpage.pageareaalgorithm.PageAreaAlgorithmContext;
import org.foree.bookreader.settings.SettingsActivity;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;

/**
 * Created by foree on 17-2-8.
 * 自定义ViewPager
 * 1. 点击中间呼出菜单
 * 2. 点击左右区域上下翻页，首页末页切换章节
 * 3. 首页末页可以滑动切换章节
 */

public class ReadViewPager extends ViewPager implements SharedPreferences.OnSharedPreferenceChangeListener{
    private static final String TAG = ReadViewPager.class.getSimpleName();
    private static final boolean DEBUG = false;

    private float mStartX = 0;
    private float mStartY = 0;
    private boolean mClick;
    private Context mContext;
    private PageAreaAlgorithmContext mAlgorithm;
    private WindowManager mManager;

    private onPageAreaClickListener onPageAreaClickListener;

    public ReadViewPager(Context context) {
        this(context, null);
    }

    public ReadViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        mManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);

        initTouchMode();

    }

    private void initTouchMode() {
        // init touch mode
        String defaultValue = PreferenceManager.getDefaultSharedPreferences(mContext).
                getString(SettingsActivity.KEY_TOUCH_MODE_TYPE, PageAreaAlgorithmContext.ALGORITHM.A.getType());
        for(PageAreaAlgorithmContext.ALGORITHM type: PageAreaAlgorithmContext.ALGORITHM.values()){
            if(type.getType().equals(defaultValue)){
                mAlgorithm = new PageAreaAlgorithmContext(type);
            }
        }
        PreferenceManager.getDefaultSharedPreferences(mContext).registerOnSharedPreferenceChangeListener(this);

        updateScreenSize();
    }

    private void updateScreenSize() {
        DisplayMetrics metrics = new DisplayMetrics();
        mManager.getDefaultDisplay().getMetrics(metrics);

        mAlgorithm.updateScreenSize(metrics.widthPixels, metrics.heightPixels);
    }

    private boolean mPreScrollDisable, mPostScrollDisable;

    public void setPreScrollDisable(boolean state) {
        if (DEBUG) {
            Log.d(TAG, "[foree] setPreScrollDisable: " + state);
        }
        mPreScrollDisable = state;
    }

    public void setPostScrollDisable(boolean state) {
        if (DEBUG) {
            Log.d(TAG, "[foree] setPostScrollDisable: " + state);
        }
        mPostScrollDisable = state;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (DEBUG) {
            Log.d(TAG, "[foree] onTouchEvent: mPreScrollDisable = " + mPreScrollDisable + ", mPostScrollDisable = " + mPostScrollDisable);
        }
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
                    if (!mAlgorithm.isMenuArea(ev.getX(), ev.getY())) {
                        if (mAlgorithm.isPreArea(ev.getX(), ev.getY())) {
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

    public void setOnPageAreaClickListener(onPageAreaClickListener onPageAreaClickListener) {
        this.onPageAreaClickListener = onPageAreaClickListener;
    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if(key.equals(SettingsActivity.KEY_TOUCH_MODE_TYPE)){
            initTouchMode();
        }
    }

    public interface onPageAreaClickListener {
        /**
         * 菜单区域点击回调
         */
        void onMediumAreaClick();
    }

    private void setPopulate(int position) {
        try {
            Class<?> clazz = this.getClass().getSuperclass();
            Field field = clazz.getDeclaredField("mPopulatePending");
            field.setAccessible(true);
            field.setBoolean(this, true);

            Method method = clazz.getDeclaredMethod("setCurrentItemInternal", int.class, boolean.class, boolean.class);
            method.setAccessible(true);
            method.invoke(this, position, false, true);

            // must refresh again
            Method populate = clazz.getDeclaredMethod("populate");
            populate.setAccessible(true);
            populate.invoke(this);

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

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        updateScreenSize();
    }
}

