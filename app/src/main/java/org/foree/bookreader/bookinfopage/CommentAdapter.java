package org.foree.bookreader.bookinfopage;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.foree.bookreader.R;
import org.foree.bookreader.bean.book.Review;

import java.util.List;

/**
 * @author foree
 * @date 2018/7/28
 * @description
 */
public class CommentAdapter extends BaseAdapter {
    private Context mContext;
    private List<Review> mReviews;

    public CommentAdapter(Context context, List<Review> reviews) {
        mContext = context;
        mReviews = reviews;
    }

    /**
     * How many items are in the data set represented by this Adapter.
     *
     * @return Count of items.
     */
    @Override
    public int getCount() {
        return mReviews.size();
    }

    /**
     * Get the data item associated with the specified position in the data set.
     *
     * @param position Position of the item whose data we want within the adapter's
     *                 data set.
     * @return The data at the specified position.
     */
    @Override
    public Object getItem(int position) {
        return mReviews.get(position);
    }

    /**
     * Get the row id associated with the specified position in the list.
     *
     * @param position The position of the item within the adapter's data set whose row id we want.
     * @return The id of the item at the specified position.
     */
    @Override
    public long getItemId(int position) {
        return position;
    }

    /**
     * Get a View that displays the data at the specified position in the data set. You can either
     * create a View manually or inflate it from an XML layout file. When the View is inflated, the
     * parent View (GridView, ListView...) will apply default layout parameters unless you use
     * {@link LayoutInflater#inflate(int, ViewGroup, boolean)}
     * to specify a root view and to prevent attachment to the root.
     *
     * @param position    The position of the item within the adapter's data set of the item whose view
     *                    we want.
     * @param convertView The old view to reuse, if possible. Note: You should check that this view
     *                    is non-null and of an appropriate type before using. If it is not possible to convert
     *                    this view to display the correct data, this method can create a new view.
     *                    Heterogeneous lists can specify their number of view types, so that this View is
     *                    always of the right type (see {@link #getViewTypeCount()} and
     *                    {@link #getItemViewType(int)}).
     * @param parent      The parent that this view will eventually be attached to
     * @return A View corresponding to the data at the specified position.
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View root;
        if(convertView != null){
            root = convertView;
        }else{
            root = LayoutInflater.from(mContext).inflate(R.layout.lv_comment_review, parent, false);
        }

        ImageView avatar = (ImageView) root.findViewById(R.id.iv_review_avatar);
        TextView nickname = (TextView) root.findViewById(R.id.tv_review_nickname);
        TextView content = (TextView) root.findViewById(R.id.tv_review_content);
        TextView time = (TextView) root.findViewById(R.id.tv_review_time);
        TextView likeCount = (TextView) root.findViewById(R.id.tv_review_like_count);

        Review review = mReviews.get(position);

        Glide.with(mContext).load(review.getAuthor().getAvatar()).into(avatar);
        nickname.setText(review.getAuthor().getNickname());
        content.setText(review.getContent());
        time.setText(review.getUpdated());
        likeCount.setText(review.getLikeCount() + "");

        return root;
    }
}
