package org.foree.zetianji.fragment;

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

import org.foree.zetianji.ArticleActivity;
import org.foree.zetianji.BaseApplication;
import org.foree.zetianji.NetCallback;
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
    NovelDao rssDao;

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

        // database
        rssDao = new NovelDao(getActivity());

        mRecyclerView = (RecyclerView) linearLayout.findViewById(R.id.rv_item_list);
        tvUpdate = (TextView)linearLayout.findViewById(R.id.tv_update);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this.getActivity()));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(),
                DividerItemDecoration.VERTICAL_LIST));

        syncDate();

        return linearLayout;
    }

    private void initAdapter() {
        mAdapter = new ItemListAdapter(getActivity(), chapterList);
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(new ItemListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Toast.makeText(getActivity(), position + "", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getActivity(), ArticleActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("chapter", chapterList.get(position));
                bundle.putString("web_char",absWebSiteHelper.getWebsiteCharSet());
                intent.putExtras(bundle);

                startActivity(intent);
            }

            @Override
            public void onItemLongClick(View view, int position) {

            }
        });
    }

    private void syncDate(){

        long id = getArguments().getLong(KEY_ID);
        // getChapterList
        webSiteInfo = rssDao.findWebSiteById(id);
        absWebSiteHelper  = new BQGWebSiteHelper(webSiteInfo);
        absWebSiteHelper.getNovel(new NetCallback<Novel>() {
            @Override
            public void onSuccess(Novel data) {
                if( data.getUpdate_time() != null){
                    tvUpdate.setText(getResources().getString(R.string.update_string) + data.getUpdate_time());
                }
                chapterList = data.getChapter_list();
                initAdapter();

            }

            @Override
            public void onFail(String msg) {
                Toast.makeText(getActivity(), "getContentListError: " + msg, Toast.LENGTH_LONG).show();
            }
        });
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

    class DividerItemDecoration extends RecyclerView.ItemDecoration {
        private final int[] ATTRS = new int[]{
                android.R.attr.listDivider
        };

        public static final int HORIZONTAL_LIST = LinearLayoutManager.HORIZONTAL;

        public static final int VERTICAL_LIST = LinearLayoutManager.VERTICAL;

        private Drawable mDivider;

        private int mOrientation;

        public DividerItemDecoration(Context context, int orientation) {
            final TypedArray a = context.obtainStyledAttributes(ATTRS);
            mDivider = a.getDrawable(0);
            a.recycle();
            setOrientation(orientation);
        }

        public void setOrientation(int orientation) {
            if (orientation != HORIZONTAL_LIST && orientation != VERTICAL_LIST) {
                throw new IllegalArgumentException("invalid orientation");
            }
            mOrientation = orientation;
        }

        @Override
        public void onDraw(Canvas c, RecyclerView parent) {
            if (mOrientation == VERTICAL_LIST) {
                drawVertical(c, parent);
            } else {
                drawHorizontal(c, parent);
            }

        }


        public void drawVertical(Canvas c, RecyclerView parent) {
            final int left = parent.getPaddingLeft();
            final int right = parent.getWidth() - parent.getPaddingRight();

            final int childCount = parent.getChildCount();
            for (int i = 0; i < childCount; i++) {
                final View child = parent.getChildAt(i);
                android.support.v7.widget.RecyclerView v = new android.support.v7.widget.RecyclerView(parent.getContext());
                final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child
                        .getLayoutParams();
                final int top = child.getBottom() + params.bottomMargin;
                final int bottom = top + mDivider.getIntrinsicHeight();
                mDivider.setBounds(left, top, right, bottom);
                mDivider.draw(c);
            }
        }

        public void drawHorizontal(Canvas c, RecyclerView parent) {
            final int top = parent.getPaddingTop();
            final int bottom = parent.getHeight() - parent.getPaddingBottom();

            final int childCount = parent.getChildCount();
            for (int i = 0; i < childCount; i++) {
                final View child = parent.getChildAt(i);
                final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child
                        .getLayoutParams();
                final int left = child.getRight() + params.rightMargin;
                final int right = left + mDivider.getIntrinsicHeight();
                mDivider.setBounds(left, top, right, bottom);
                mDivider.draw(c);
            }
        }

        @Override
        public void getItemOffsets(Rect outRect, int itemPosition, RecyclerView parent) {
            if (mOrientation == VERTICAL_LIST) {
                outRect.set(0, 0, 0, mDivider.getIntrinsicHeight());
            } else {
                outRect.set(0, 0, mDivider.getIntrinsicWidth(), 0);
            }
        }
    }
}
