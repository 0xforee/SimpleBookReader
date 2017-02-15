package org.foree.bookreader.ui.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import org.foree.bookreader.data.book.Chapter;
import org.foree.bookreader.ui.fragment.ReadFragment;

/**
 * Created by foree on 17-2-8.
 */

public class PageAdapter extends FragmentStatePagerAdapter {
    private Chapter chapter;

    public PageAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        return ReadFragment.newInstance(chapter.getChapterTitle(), chapter.getPages().get(position));
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    @Override
    public int getCount() {
        return chapter != null ? chapter.getPages().size() : 0;
    }

    public void setChapter(Chapter chapter) {
        this.chapter = chapter;
        notifyDataSetChanged();
    }

}
