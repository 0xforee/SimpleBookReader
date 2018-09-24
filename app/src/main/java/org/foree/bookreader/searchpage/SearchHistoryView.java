package org.foree.bookreader.searchpage;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import org.foree.bookreader.R;

/**
 * @author foree
 * @date 2018/9/24
 * @description 搜索建议，搜索历史，在进入搜索界面的时候出现，执行搜索的时候隐藏
 */
public class SearchHistoryView extends ScrollView {
    ListView mHistory;

    TextView mTvHotTitle, mTvHistoryTitle;

    SearchHotWordsView mHotWordsView;
    LinearLayout mHistoryLayout;

    SearchWordClickCallback mClickCallback;

    private String[] mHotwords;

    int mMaxWords;

    public SearchHistoryView(Context context) {
        this(context, null);
    }

    public SearchHistoryView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mMaxWords = getResources().getInteger(R.integer.search_hotwords_max_words);
    }

    public void setOnClickCallback(SearchWordClickCallback clickCallback) {
        mClickCallback = clickCallback;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mTvHotTitle = (TextView) findViewById(R.id.tv_hotword_title);
        mTvHistoryTitle = (TextView) findViewById(R.id.tv_history_title);
        mHotWordsView = (SearchHotWordsView) findViewById(R.id.ll_hotwords);
        mHistoryLayout = (LinearLayout) findViewById(R.id.ll_history);

//        inflateFromSearchHot(mStrings);
    }

    public void inflateFromSearchHot(String[] hotWords) {
        if (mHotwords != hotWords) {
            mHotwords = hotWords;
            mHotWordsView.removeAllViews();

            if (mHotwords == null || mHotwords.length == 0) {
                mTvHotTitle.setVisibility(GONE);
                mHotWordsView.setVisibility(GONE);
            } else {
                mTvHotTitle.setVisibility(VISIBLE);
                mHotWordsView.setVisibility(VISIBLE);

                int wordsNum = Math.min(mMaxWords, mHotwords.length);
                for (int i = 0; i < wordsNum; i++) {
                    final TextView textView = new TextView(getContext());
                    final String hotword = mHotwords[i];
                    textView.setTextColor(Color.BLACK);
                    textView.setEnabled(true);
                    textView.setClickable(true);
                    textView.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mClickCallback.onClick(hotword);
                        }
                    });
                    textView.setBackgroundResource(R.drawable.tv_hotword_background_list);
                    textView.setPadding(30, 20, 30, 20);
                    textView.setText(hotword);

                    ViewGroup.LayoutParams layoutParams1 = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

                    mHotWordsView.addView(textView, layoutParams1);
                }

            }
        }
    }

    public interface SearchWordClickCallback {
        /**
         * 按钮之后
         *
         * @param word 需要传入的关键字
         */
        void onClick(String word);
    }

}
