package org.foree.bookreader.ui.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import org.foree.bookreader.ui.fragment.ArticleFragment;

import java.util.ArrayList;

/**
 * Created by foree on 17-2-8.
 */

public class PageAdapter extends FragmentStatePagerAdapter {
    private ArrayList<CharSequence> mPages;
    private String title;

    public PageAdapter(FragmentManager fm) {
        super(fm);
        mPages = new ArrayList<>();
    }

    @Override
    public Fragment getItem(int position) {
        return ArticleFragment.newInstance(title, mPages.get(position).toString());
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    @Override
    public int getCount() {
        return mPages.size();
    }

    public void setPages(ArrayList<CharSequence> pages) {
        mPages.clear();
        mPages.addAll(pages);
        notifyDataSetChanged();
    }

    public void setTitle(String title) {
        this.title = title;
    }

}
