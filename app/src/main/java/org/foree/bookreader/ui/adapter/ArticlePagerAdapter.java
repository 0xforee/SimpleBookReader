package org.foree.bookreader.ui.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.ViewGroup;

/**
 * Created by foree on 17-1-15.
 */

public class ArticlePagerAdapter extends FragmentPagerAdapter {
    private static final String TAG = UnlimitedPager.class.getSimpleName();

    public interface UnlimitedPager {
        public void onRefreshPage();

        public void onDataChanged(int offset);

        public Fragment getItem(int position);
    }

    private ViewPager mViewPager = null;
    private UnlimitedPager mPager = null;
    private boolean mIsChanged = false;

    public ArticlePagerAdapter(ViewPager viewPager, FragmentManager fm) {
        super(fm);
        mViewPager = viewPager;
    }

    public void setPage(UnlimitedPager pager) {
        mPager = pager;
        if (mPager != null) {
            //mPager.onRefreshPage();
        }
    }

    @Override
    public Fragment getItem(int position) {
        return mPager.getItem(position);
    }

    @Override
    public int getCount() {
        return 3;
    }

    // 每次切换的时候都会调用（系统会调用setCurrentItem来切换页面，所以手动调用setCurrentItem也会调用这个函数）
    // 启动时依次调用 0 0 0，向左滑动时，系统会依次调用position 1 0 0,向右 1 2 2
    // 启动依次调用setPage,onDataChange,onRefreshPage，切换依次调用onDataChange,onRefreshPage
    @Override
    public void setPrimaryItem(ViewGroup container, int position, Object object) {
        super.setPrimaryItem(container, position, object);
        Log.d(TAG, "setPrimaryItem");
        if (position == 1) {
            if (mIsChanged) {
                if (mPager != null) {
                    // 更新左右页面的内容
                    mPager.onRefreshPage();
                }
                mIsChanged = false;
            }
        } else {
            if (mPager != null) {
                // 用于左右滑动的偏移量
                mPager.onDataChanged(position - 1);
                mIsChanged = true;
            }

            // setCurrentItem用于切换到指定页面，false表示立即切换，无动画效果
            mViewPager.setCurrentItem(1, false);
        }
    }
}