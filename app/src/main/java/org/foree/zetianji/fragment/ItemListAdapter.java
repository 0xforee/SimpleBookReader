package org.foree.zetianji.fragment;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import org.foree.zetianji.R;

import org.foree.zetianji.book.Chapter;

import java.util.List;

/**
 * Created by foree on 16-7-22.
 */
public class ItemListAdapter extends RecyclerView.Adapter<ItemListAdapter.MyViewHolder>{
    private LayoutInflater mLayoutInflater;
    private List<Chapter> chapterList;

    public ItemListAdapter(Context context, List<Chapter> itemList){
        mLayoutInflater = LayoutInflater.from(context);
        chapterList = itemList;
    }

    public interface OnItemClickListener{
        void onItemClick(View view, int position);
        void onItemLongClick(View view, int position);
    }

    private OnItemClickListener mOnItemClickListener;

    public void setOnItemClickListener(OnItemClickListener mOnItemClickListener)
    {
        this.mOnItemClickListener = mOnItemClickListener;
    }

    @Override
    public ItemListAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        MyViewHolder holder = new MyViewHolder(mLayoutInflater.inflate(R.layout.item_list_holder, parent, false));

        return holder;
    }

    @Override
    public void onBindViewHolder(final ItemListAdapter.MyViewHolder holder, int position) {
        holder.tvTitle.setText(chapterList.get(position).getTitle());

        // 如果设置了回调，则设置点击事件
        if (mOnItemClickListener != null)
        {
            holder.itemView.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    int pos = holder.getLayoutPosition();
                    mOnItemClickListener.onItemClick(holder.itemView, pos);
                }
            });

            holder.itemView.setOnLongClickListener(new View.OnLongClickListener()
            {
                @Override
                public boolean onLongClick(View v)
                {
                    int pos = holder.getLayoutPosition();
                    mOnItemClickListener.onItemLongClick(holder.itemView, pos);
                    return false;
                }
            });
        }
    }
    @Override
    public int getItemCount() {
        return chapterList.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder{
        TextView tvTitle;
        TextView tvSummary;
        TextView tvPublished;

        public MyViewHolder(View view){
            super(view);
            tvTitle = (TextView)view.findViewById(R.id.tv_item_title);
            tvSummary = (TextView)view.findViewById(R.id.tv_item_summary);
            tvPublished = (TextView)view.findViewById(R.id.tv_item_published);
        }
    }
}

