package de.fwpm.android.fefesblog.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import de.fwpm.android.fefesblog.BlogPost;
import de.fwpm.android.fefesblog.R;

/**
 * Created by alex on 20.01.18.
 */

public class NewPostsRecyclerViewAdapter extends RecyclerView.Adapter<NewPostsRecyclerViewAdapter.ViewHolder> {

    ArrayList<BlogPost> mData;
    Context mContext;
    OnItemClickListener mListener;
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


    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView mPostText;
        ImageView mImage;

        public ViewHolder(View itemView) {
            super(itemView);
            mPostText = (TextView) itemView.findViewById(R.id.post_text);
        }

        void setClickListener(final int position) {

            final View.OnClickListener onClickListener = new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    mListener.onItemClick(position);
                }
            };
            mPostText.setOnClickListener(onClickListener);
        }

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view  = LayoutInflater.from(mContext).inflate(R.layout.new_post_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {

        if(position >= getItemCount() -1)
            mOnBottomReachListener.onBottom(position);

        holder.mPostText.setText(mData.get(position).getText());

        holder.setClickListener(position);

    }

    @Override
    public int getItemCount() {
        return mData.size();
    }




}
