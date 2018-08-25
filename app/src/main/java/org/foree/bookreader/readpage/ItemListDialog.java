package org.foree.bookreader.readpage;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Process;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;

import org.foree.bookreader.R;
import org.foree.bookreader.base.GlobalConfig;
import org.foree.bookreader.bean.book.Book;
import org.foree.bookreader.bean.book.Chapter;
import org.foree.bookreader.bean.book.Source;
import org.foree.bookreader.parser.WebParser;

import java.util.List;

/**
 * @author foree
 * @date 2018/8/25
 * @description 切换书源和章节列表
 */
public class ItemListDialog extends Dialog {
    private AbstractItemListAdapter mAdapter;
    private OnItemClickListener mClickListener;
    private String mTitle = "";
    private ListView mlvList;
    private List data;
    private View mRootView;
    private Context mContext;
    private Book mBook;
    private Handler mHandler;
    private int mId;
    private String mCurrentItem;
    private ProgressBar mProgress;

    private ItemListDialog(@NonNull Context context) {
        this(context, R.style.contentDialogStyle);
    }

    private ItemListDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);

        mContext = context;

        HandlerThread handlerThread = new HandlerThread("sync", Process.THREAD_PRIORITY_BACKGROUND);
        handlerThread.start();
        mHandler = new Handler(handlerThread.getLooper());
    }

    private ItemListDialog(Builder builder) {
        this(builder.mContext);
        mAdapter = builder.mAdapter;
        mClickListener = builder.mClickListener;
        mTitle = builder.mTitle;
        mContext = builder.mContext;
        mBook = builder.mBook;
        mId = builder.id;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_content_layout, null);

        setTitle(mTitle);
        setContentView(view);

        DisplayMetrics dp = new DisplayMetrics();
        WindowManager windowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getMetrics(dp);
        Window dialogWindow = getWindow();
        if (dialogWindow != null) {
            WindowManager.LayoutParams lp = dialogWindow.getAttributes();
            dialogWindow.setGravity(Gravity.BOTTOM);

            lp.x = 0;
            lp.y = 0;
            lp.width = dp.widthPixels;
            lp.height = dp.heightPixels / 5 * 4;

            dialogWindow.setAttributes(lp);
        }

        setCanceledOnTouchOutside(true);

        mlvList = (ListView) view.findViewById(R.id.rv_item_list);
        mlvList.setAdapter(mAdapter);
        mlvList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mAdapter.setSelectedPosition(position);
                mAdapter.notifyDataSetChanged();
                dismiss();

                if (mAdapter instanceof CusChapterListAdapter) {
                    mClickListener.onItemClick(position, ((Chapter) data.get(position)).getChapterUrl());
                } else {
                    mClickListener.onItemClick(position, ((Source) data.get(position)).getSourceId());
                }
            }
        });

        mProgress = (ProgressBar) view.findViewById(R.id.pg_progress);
    }

    /**
     * Called when the dialog is starting.
     */
    @Override
    protected void onStart() {
        super.onStart();
        mProgress.setVisibility(View.VISIBLE);

        if (mAdapter instanceof CusChapterListAdapter) {
            mCurrentItem = mBook.getRecentChapterUrl();

            // 请求章节资源
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    data = WebParser.getInstance().getContents(mBook.getBookUrl(), mBook.getContentUrl());
                    mAdapter.updateData(data);
                    mlvList.post(new Runnable() {
                        @Override
                        public void run() {
                            for (int i = 0; i < data.size(); i++) {
                                Chapter chapter = (Chapter) data.get(i);
                                if (mCurrentItem.equals(chapter.getChapterUrl())) {
                                    setSelectedPosition(i);
                                }
                            }
                            mAdapter.notifyDataSetChanged();
                            mProgress.setVisibility(View.GONE);
                        }
                    });

                }
            });
        } else {
            mCurrentItem = mBook.getContentUrl();

            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    data = WebParser.getInstance().getBookSource(mBook.getBookUrl(),
                            mBook.getBookName() + GlobalConfig.MAGIC_SPLIT_KEY + mBook.getAuthor());
                    mAdapter.updateData(data);

                    mlvList.post(new Runnable() {
                        @Override
                        public void run() {
                            for (int i = 0; i < data.size(); i++) {
                                Source source = (Source) data.get(i);
                                if (mCurrentItem.equals(source.getSourceId())) {
                                    setSelectedPosition(i);
                                }
                            }
                            mAdapter.notifyDataSetChanged();
                            mProgress.setVisibility(View.GONE);
                        }
                    });

                }
            });
        }


    }


    /**
     * Called to tell you that you're stopping.
     */
    @Override
    protected void onStop() {
        super.onStop();
        // 销毁资源
    }

    private void setSelectedPosition(int position) {
        mAdapter.setSelectedPosition(position);
        mlvList.setSelection(position-2);
        mAdapter.notifyDataSetChanged();

    }

    public int getId() {
        return mId;
    }

    public void updateBook(Book book) {
        mBook = book;
    }

    public interface OnItemClickListener {
        /**
         * 在列表中的子项被点击之后
         *
         * @param position location
         * @param value    需要更新的值
         */
        void onItemClick(int position, String value);
    }

    public static final class Builder {
        private AbstractItemListAdapter mAdapter;
        private OnItemClickListener mClickListener;
        private String mTitle;
        private Context mContext;
        private Book mBook;
        private int id;

        public Builder(Context context) {
            mContext = context;
        }

        public Builder withAdapter(AbstractItemListAdapter val) {
            mAdapter = val;
            return this;
        }

        public Builder withClickListener(OnItemClickListener val) {
            mClickListener = val;
            return this;
        }

        public Builder withTitle(String val) {
            mTitle = val;
            return this;
        }

        public Builder withBook(Book val) {
            mBook = val;
            return this;
        }

        public Builder withId(int id) {
            this.id = id;
            return this;
        }

        public ItemListDialog build() {
            return new ItemListDialog(this);
        }
    }
}
