package org.foree.zetianji.ui.fragment;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.foree.zetianji.ui.activity.ArticleActivity;
import org.foree.zetianji.base.BaseApplication;
import org.foree.zetianji.net.NetCallback;
import org.foree.zetianji.R;
import org.foree.zetianji.book.Chapter;
import org.foree.zetianji.book.Novel;
import org.foree.zetianji.dao.NovelDao;
import org.foree.zetianji.helper.BQGWebSiteHelper;
import org.foree.zetianji.helper.WebSiteInfo;
import org.foree.zetianji.utils.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Created by foree on 16-7-20.
 */
public class ItemListFragment extends Fragment{
    private static final String KEY_ID = "websiteId";
    private static final String TAG = ItemListFragment.class.getSimpleName();

    private RecyclerView mRecyclerView;
    private ItemListAdapter mAdapter;
    private List<Chapter> chapterList;
    BQGWebSiteHelper absWebSiteHelper;
    WebSiteInfo webSiteInfo;
    TextView tvUpdate;
    NovelDao novelDao;

    public ItemListFragment() {
        // Required empty public constructor
    }

    public static ItemListFragment newInstance(long id) {
        ItemListFragment f = new ItemListFragment();

        Bundle args = new Bundle();

        args.putLong(KEY_ID, id);
        f.setArguments(args);

        return (f);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LinearLayout linearLayout = (LinearLayout)inflater.inflate(R.layout.fragment_itemlist, container, false);

        return linearLayout;
    }


    private void downloadChapter(final Chapter chapter){
        absWebSiteHelper.getChapterContent(chapter.getUrl(), webSiteInfo.getWeb_char(), new NetCallback<String>() {
            @Override
            public void onSuccess(String data) {
                File chapterCache = new File(BaseApplication.getInstance().getCacheDirString()
                        + File.separator + chapter.getUrl());
                try {
                    FileUtils.writeFile(chapterCache, data);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFail(String msg) {

            }
        });
    }


}
