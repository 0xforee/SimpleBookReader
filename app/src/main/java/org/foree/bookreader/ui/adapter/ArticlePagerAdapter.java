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

    @Override
    public void setPrimaryItem(ViewGroup container, int position, Object object) {
        super.setPrimaryItem(container, position, object);
        Log.d(TAG, "setPrimaryItem");
        if (position == 1) {
            if (mIsChanged) {
                if (mPager != null) {
                    mPager.onRefreshPage();
                }
                mIsChanged = false;
            }
        } else {
            if (mPager != null) {
                mPager.onDataChanged(position - 1);
                mIsChanged = true;
            }

            // 用于始终固定页面到1
            mViewPager.setCurrentItem(1, false);
        }
    }
}