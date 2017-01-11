package org.foree.bookreader.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import org.foree.bookreader.R;
import org.foree.bookreader.book.Article;
import org.foree.bookreader.book.Book;
import org.foree.bookreader.book.Chapter;
import org.foree.bookreader.dao.BookDao;
import org.foree.bookreader.net.NetCallback;
import org.foree.bookreader.ui.fragment.ItemListAdapter;
import org.foree.bookreader.website.BiQuGeWebInfo;
import org.foree.bookreader.website.WebInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by foree on 16-7-21.
 */
public class ArticleActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener{
    private static final String TAG = ArticleActivity.class.getSimpleName();
    TextView tvContent,tvTitle;
    FloatingActionButton turnNightMode;
    SwipeRefreshLayout mSwipeRefreshLayout;
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }
    };
    String chapterUrl, bookUrl;
    private RecyclerView mRecyclerView;
    private ItemListAdapter mAdapter;
    private List<Chapter> chapterList = new ArrayList<>();
    boolean turnFlag = true;
    private PopupWindow popupWindow;
    private View rootView;
    private ListView lv;
    private ListAdapter lvAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

        tvContent = (TextView)findViewById(R.id.tv_content);
        tvTitle = (TextView)findViewById(R.id.tv_title);
        Bundle bundle = getIntent().getExtras();
        chapterUrl = bundle.getString("url");
        bookUrl = bundle.getString("book_url");
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_ly);
        mSwipeRefreshLayout.setOnRefreshListener(this);

        // get FloatActionButton
        turnNightMode = (FloatingActionButton)findViewById(R.id.fab);
        /*turnNightMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(turnFlag) {
                    // night Mode
                    tvContent.setTextColor(getResources().getColor(R.color.nightTextColor));
                    tvTitle.setTextColor(getResources().getColor(R.color.nightTextColor));
                    mSwipeRefreshLayout.setBackgroundColor(getResources().getColor(R.color.nightBackground));
                    turnFlag = false;
                }else{
                    // day Mode
                    tvContent.setTextColor(getResources().getColor(R.color.dayTextColor));
                    tvTitle.setTextColor(getResources().getColor(R.color.dayTextColor));
                    mSwipeRefreshLayout.setBackgroundColor(getResources().getColor(R.color.dayBackground));
                    turnFlag = true;
                }
            }
        });
*/
        turnNightMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(popupWindow == null)
                    showPopup();
                else
                    popupWindow.showAtLocation(rootView, Gravity.BOTTOM, 0, 0);
            }
        });

        syncArticleContent();

    }

    private void showPopup(){
        BookDao bookDao = new BookDao(this);
        View view = LayoutInflater.from(this).inflate(R.layout.popupwindow_layout, null);
        rootView = LayoutInflater.from(this).inflate(R.layout.activity_article, null);

        DisplayMetrics dp = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dp);

        popupWindow = new PopupWindow(this);
        popupWindow.setContentView(view);
        popupWindow.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        popupWindow.setHeight(dp.heightPixels / 4 * 3);

        popupWindow.showAtLocation(rootView, Gravity.BOTTOM, 0 ,0);
        popupWindow.setOutsideTouchable(true);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.rv_item_list);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this,
                DividerItemDecoration.VERTICAL_LIST));

        chapterList = bookDao.findChapterListByBookUrl(bookUrl);

        setUpRecyclerViewAdapter();
    }

    private void setUpRecyclerViewAdapter() {
        mAdapter = new ItemListAdapter(this, chapterList);
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(new ItemListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {

                Intent intent = new Intent(ArticleActivity.this, ArticleActivity.class);
                Bundle bundle = new Bundle();
                //bundle.putSerializable("chapter", chapterList.get(position));
                bundle.putString("url",chapterList.get(position).getChapterUrl());
                bundle.putString("book_url", chapterList.get(position).getBookUrl());
                intent.putExtras(bundle);
                startActivity(intent);
            }

            @Override
            public void onItemLongClick(View view, int position) {

            }
        });
    }

    private void syncArticleContent(){
        mSwipeRefreshLayout.setRefreshing(true);
        WebInfo webInfo = new BiQuGeWebInfo();
        webInfo.getArticle(chapterUrl, new NetCallback<Article>() {
            @Override
            public void onSuccess(Article data) {
                updateUI(data);
            }

            @Override
            public void onFail(String msg) {

            }
        });
    }

    private void updateUI(final Article article){
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(false);
                if( article != null){
                    // use textView format
                    tvContent.setText(Html.fromHtml(article.getContents()));
                    tvTitle.setText(Html.fromHtml(article.getTitle()));
                    Snackbar.make(mSwipeRefreshLayout, R.string.load_success, Snackbar.LENGTH_SHORT).show();
                }else{
                    Snackbar.make(mSwipeRefreshLayout, R.string.load_fail , Snackbar.LENGTH_LONG).show();
                }
            }
        }, 0);


    }

    @Override
    public void onRefresh() {
        syncArticleContent();
    }
}
