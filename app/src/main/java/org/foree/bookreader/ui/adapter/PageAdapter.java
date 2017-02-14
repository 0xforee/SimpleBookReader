package org.foree.bookreader.ui.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import org.foree.bookreader.data.book.Article;
import org.foree.bookreader.ui.fragment.ArticleFragment;

/**
 * Created by foree on 17-2-8.
 */

public class PageAdapter extends FragmentStatePagerAdapter {
    private Article article;

    public PageAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        return ArticleFragment.newInstance(article.getTitle(), article.getPages().get(position));
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    @Override
    public int getCount() {
        return article != null ? article.getPages().size() : 0;
    }

    public void setArticle(Article article) {
        this.article = article;
        notifyDataSetChanged();
    }

}
