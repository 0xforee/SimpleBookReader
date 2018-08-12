package org.foree.bookreader.readpage;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import org.foree.bookreader.R;
import org.foree.bookreader.base.BaseActivity;
import org.foree.bookreader.common.DividerItemDecoration;
import org.foree.bookreader.common.BaseRecyclerAdapter;
import org.foree.bookreader.readpage.pageareaalgorithm.PageAreaAlgorithmContext.ALGORITHM;
import org.foree.bookreader.settings.SettingsActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * @author foree
 * @date 2018/8/11
 * @description
 */
public class TouchModeSelectorActivity extends BaseActivity {

    private ImageView mIvPreview;
    private RecyclerView mRvSelector;
    private RecycleAdapter mAdapter;
    protected List<DataHolder> mSelectorItem;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_touch_mode_selector);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        initViews();

        mRvSelector.post(new Runnable() {
            @Override
            public void run() {
                initData();
            }
        });

    }

    private void initViews() {
        mIvPreview = (ImageView) findViewById(R.id.iv_preview);
        mRvSelector = (RecyclerView) findViewById(R.id.rv_selector);

        mAdapter = new RecycleAdapter(this);
        mRvSelector.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        mRvSelector.setItemAnimator(new DefaultItemAnimator());
        mRvSelector.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.HORIZONTAL_LIST));

        mAdapter.setOnItemClickListener(new BaseRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                setSelection(position, true);
            }

            @Override
            public void onItemLongClick(View view, int position) {

            }
        });

        mRvSelector.setAdapter(mAdapter);

        mIvPreview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void initData() {
        // FIXME: getDrawable很耗时，后续使用自定义View绘制
        mSelectorItem = new ArrayList<>();
        mSelectorItem.add(new DataHolder(ALGORITHM.A, getDrawable(R.drawable.algorithm_a)));
        mSelectorItem.add(new DataHolder(ALGORITHM.B, getDrawable(R.drawable.algorithm_b)));
        mSelectorItem.add(new DataHolder(ALGORITHM.C, getDrawable(R.drawable.algorithm_c)));

        // get default touch mode
        String defaultValue = PreferenceManager.getDefaultSharedPreferences(this).
                getString(SettingsActivity.KEY_TOUCH_MODE_TYPE, ALGORITHM.A.getType());

        for (int i = 0; i < mSelectorItem.size(); i++) {
            if (mSelectorItem.get(i).getAlgorithm().getType().equals(defaultValue)) {
                setSelection(i, false);
            }
        }
    }

    private void setSelection(final int position, boolean changed) {
        mAdapter.setSelected(position);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mIvPreview.setImageDrawable(mSelectorItem.get(position).getResDrawable());
            }
        });
        mAdapter.notifyDataSetChanged();

        if (changed) {
            PreferenceManager.getDefaultSharedPreferences(this).edit().
                    putString(SettingsActivity.KEY_TOUCH_MODE_TYPE, mSelectorItem.get(position).getAlgorithm().getType()).apply();
        }
    }


    class RecycleAdapter extends BaseRecyclerAdapter<RecycleAdapter.ViewHolder> {
        private Context mContext;
        private int mSelectedPosition;

        public RecycleAdapter(Context context) {
            mContext = context;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.rv_touch_mode_holder, parent, false));
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            super.onBindViewHolder(holder, position);
            holder.mTextView.setText(mSelectorItem.get(position).getAlgorithm().getType());
            if (position == mSelectedPosition) {
                holder.mIvItem.setBackgroundColor(getResources().getColor(R.color.md_grey_900));
            } else {
                holder.mIvItem.setBackground(null);
            }
        }

        @Override
        public int getItemCount() {
            return mSelectorItem != null ? mSelectorItem.size() : 0;
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            private ImageView mIvItem;
            private TextView mTextView;

            public ViewHolder(View itemView) {
                super(itemView);
                mIvItem = (ImageView) itemView.findViewById(R.id.iv_selector_item);
                mTextView = (TextView) itemView.findViewById(R.id.tv_mode_name);
            }
        }

        public void setSelected(int position) {
            mSelectedPosition = position;
        }
    }

    class DataHolder {
        private ALGORITHM algorithm;
        private Drawable resDrawable;

        public DataHolder(ALGORITHM algorithm, Drawable resDrawable) {
            this.algorithm = algorithm;
            this.resDrawable = resDrawable;
        }

        public ALGORITHM getAlgorithm() {
            return algorithm;
        }

        public Drawable getResDrawable() {
            return resDrawable;
        }
    }
}
