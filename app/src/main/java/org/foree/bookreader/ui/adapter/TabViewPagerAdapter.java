package org.foree.bookreader.ui.adapter;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import org.foree.bookreader.R;
import org.foree.bookreader.ui.fragment.BookShelfFragment;
import org.foree.bookreader.ui.fragment.BookStoreFragment;

/**
 * Created by foree on 17-3-6.
 */

public class TabViewPagerAdapter extends FragmentPagerAdapter {
    private Context context;
    private String[] titles;

    public TabViewPagerAdapter(FragmentManager fm, Context context) {
        super(fm);
        this.context = context;

        titles = new String[]{
                context.getResources().getString(R.string.tab_book_shelf),
                context.getResources().getString(R.string.tab_book_store)
        };
    }

    @Override
    public Fragment getItem(int position) {
        if (position == 0) {
            return BookShelfFragment.newInstance();
        }

        return BookStoreFragment.newInstance();
    }

    @Override
    public int getCount() {
        return titles.length;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return titles[position];
    }
}
