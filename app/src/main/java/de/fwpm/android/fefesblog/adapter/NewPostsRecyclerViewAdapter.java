package de.fwpm.android.fefesblog.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import de.fwpm.android.fefesblog.BlogPost;
import de.fwpm.android.fefesblog.PinnedHeaderItemDecoration;
import de.fwpm.android.fefesblog.R;

/**
 * Created by alex on 20.01.18.
 */

public class NewPostsRecyclerViewAdapter extends RecyclerView.Adapter<NewPostsRecyclerViewAdapter.ViewHolder> implements PinnedHeaderItemDecoration.PinnedHeaderAdapter {

    ArrayList<BlogPost> mData;
    Context mContext;
    static OnItemClickListener mListener;
    OnBottomReachListener mOnBottomReachListener;

    public NewPostsRecyclerViewAdapter(Context context, final OnItemClickListener listener, final OnBottomReachListener onBottomReachListener ,final ArrayList<BlogPost> data) {

        mContext = context;
        mData = data;
        mListener = listener;
        mOnBottomReachListener = onBottomReachListener;

    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public interface OnBottomReachListener {
        void onBottom(int position);
    }

    static abstract class ViewHolder extends RecyclerView.ViewHolder {

        private final int mViewType;

        public ViewHolder(View itemView, int viewType) {
            super(itemView);
            mViewType = viewType;
        }

        public static ViewHolder createViewHolder(ViewGroup parent, int viewType) {
            final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View root;

            switch (viewType) {
                case BlogPost.TYPE_SECTION:
                    root = inflater.inflate(R.layout.new_post_section, parent, false);
                    return new SectionViewHolder(root, viewType);

                case BlogPost.TYPE_DATA:
                    root = inflater.inflate(R.layout.new_post_item, parent, false);
                    return new DataViewHolder(root, viewType);

                default:
                    return null;
            }
        }

        abstract public void bindItem(NewPostsRecyclerViewAdapter adapter, BlogPost blogPost, int position);

        abstract public void setClickListener(final int position);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return ViewHolder.createViewHolder(parent, viewType);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {

        if(position >= getItemCount() -1)
            mOnBottomReachListener.onBottom(position);

        final BlogPost item = mData.get(position);
        viewHolder.bindItem(this, item, position);
        viewHolder.setClickListener(position);

    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    @Override
    public int getItemViewType(int position) {
        return mData.get(position).type;
    }

    @Override
    public boolean isPinnedViewType(int viewType) {
        if (viewType == BlogPost.TYPE_SECTION) {
            return true;
        } else {
            return false;
        }
    }



    static class DataViewHolder extends ViewHolder {
        private TextView mContent;
        private ImageButton mExpand;

        public DataViewHolder(View itemView, int viewType) {
            super(itemView, viewType);
            mContent = (TextView) itemView.findViewById(R.id.post_text);
            mExpand = (ImageButton) itemView.findViewById(R.id.expand);
        }

        public void setClickListener(final int position) {

            final View.OnClickListener onClickListener = new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    mListener.onItemClick(position);
                }
            };
            mContent.setOnClickListener(onClickListener);
        }

        @Override
        public void bindItem(NewPostsRecyclerViewAdapter adapter, BlogPost blogPost, int position) {

            mContent.setText(Html.fromHtml(blogPost.getHtmlText().split("</a>", 2)[1]));
            closeContent();

            mExpand.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(mContent.getMaxLines() == 6) {
                        expandContent();
                    } else {
                        closeContent();
                    }

                }
            });

        }

        private void expandContent() {
            mContent.setMaxLines(Integer.MAX_VALUE);
            mContent.setEllipsize(null);
            mExpand.setImageResource(R.drawable.ic_keyboard_arrow_up_black_24dp);
        }

        private void closeContent() {
            mContent.setMaxLines(6);
            mContent.setEllipsize(TextUtils.TruncateAt.END);
            mExpand.setImageResource(R.drawable.ic_keyboard_arrow_down_black_24dp);
        }

    }

    static class SectionViewHolder extends ViewHolder {
        private TextView mSectionLabel;

        public SectionViewHolder(View itemView, int viewType) {
            super(itemView, viewType);
            mSectionLabel = (TextView) itemView.findViewById(R.id.section_label);
        }

        public void setClickListener(final int position) {

        }

        @Override
        public void bindItem(NewPostsRecyclerViewAdapter adapter, BlogPost blogPost, int position) {
            mSectionLabel.setText(blogPost.getDate().toString());
        }
    }


}
