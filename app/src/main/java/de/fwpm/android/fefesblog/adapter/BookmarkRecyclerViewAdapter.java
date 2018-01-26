package de.fwpm.android.fefesblog.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

import de.fwpm.android.fefesblog.BlogPost;
import de.fwpm.android.fefesblog.R;
import de.fwpm.android.fefesblog.database.AppDatabase;
import de.fwpm.android.fefesblog.fragments.SettingFragment;

import static de.fwpm.android.fefesblog.fragments.NewPostsFragment.jumpToPosition;
import static de.fwpm.android.fefesblog.utils.CustomTextView.setTextViewHTML;

/**
 * Created by Untiak on 23.01.2018.
 */

public class BookmarkRecyclerViewAdapter extends RecyclerView.Adapter<BookmarkRecyclerViewAdapter.ViewHolder> {
    private static final String TAG = "BMRecyclerViewAdapter";
    private static int MAX_LINES = 6;

    private ArrayList<BlogPost> mData;
    private Context mContext;
    private OnItemClickListener mListener;

    public BookmarkRecyclerViewAdapter(Context context, final OnItemClickListener listener, final ArrayList<BlogPost> data) {

        mContext = context;
        mData = data;
        mListener = listener;

    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView mDate;
        private TextView mContent;
        private ImageButton mExpand;
        private ImageButton mBookmark;
        private ImageButton mShare;


        public ViewHolder(View itemView) {
            super(itemView);
            mContent = (TextView) itemView.findViewById(R.id.post_text);
            mDate = (TextView) itemView.findViewById(R.id.post_date);
            mExpand = (ImageButton) itemView.findViewById(R.id.expand);
            mBookmark = (ImageButton) itemView.findViewById(R.id.bookmark);
            mShare = (ImageButton) itemView.findViewById(R.id.share);
        }

        void setClickListener(final int position, final BlogPost blogPost) {

            final View.OnClickListener onClickListener = new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    mListener.onItemClick(position,blogPost);
                }
            };
            final View.OnClickListener onShareListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onShareClick(position,blogPost);
                }
            };
            mContent.setOnClickListener(onClickListener);
            mDate.setOnClickListener(onClickListener);
            mShare.setOnClickListener(onShareListener);
        }

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.simple_post_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final BlogPost blogPost = mData.get(position);

        closeContent(holder);

        setBookmarkIcon(holder, blogPost);

        String[] htmltext = blogPost.getHtmlText().split("</a>", 2);

        if (htmltext.length > 1)
            setTextViewHTML(holder.mContent, blogPost.getHtmlText().split("</a>", 2)[1]);
        else {
            holder.mContent.setText(blogPost.getText());
            Log.d(TAG, "onBindViewHolder: " + blogPost.getHtmlText());
        }

        holder.setClickListener(position,blogPost);

        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, d. MMMM yyyy", Locale.GERMANY);
        holder.mDate.setText(dateFormat.format(blogPost.getDate()));

        holder.mBookmark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                blogPost.setBookmarked(blogPost.isBookmarked() ? false : true);
                setBookmarkIcon(holder, blogPost);

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        AppDatabase.getInstance(mContext).blogPostDao().updateBlogPost(blogPost);
                    }
                }).start();

            }
        });

        holder.mExpand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (blogPost.isUpdate()) {

                    blogPost.setUpdate(false);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            AppDatabase.getInstance(mContext).blogPostDao().updateBlogPost(blogPost);
                        }
                    }).start();

                }

                if (holder.mContent.getMaxLines() == SettingFragment.getPreviewSize()) {
                    expandContent(holder);
                } else {
                    closeContent(holder);
                    jumpToPosition((position == 0) ? 0 : position - 1);
                }

            }
        });

    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public interface OnItemClickListener {
        void onItemClick(int position,BlogPost blogPost);
        void onShareClick(int position, BlogPost blogPost);
    }

    private void setBookmarkIcon(BookmarkRecyclerViewAdapter.ViewHolder holder, BlogPost blogPost) {
        if (blogPost.isBookmarked())
            holder.mBookmark.setImageResource(R.drawable.ic_bookmark_black_24dp);
        else holder.mBookmark.setImageResource(R.drawable.ic_bookmark_border_black_24dp);
    }

    private void expandContent(BookmarkRecyclerViewAdapter.ViewHolder holder) {
        holder.mContent.setMaxLines(Integer.MAX_VALUE);
        holder.mContent.setEllipsize(null);
        holder.mExpand.setImageResource(R.drawable.ic_keyboard_arrow_up_black_24dp);
    }

    private void closeContent(BookmarkRecyclerViewAdapter.ViewHolder holder) {
        holder.mContent.setMaxLines(SettingFragment.getPreviewSize());
        holder.mContent.setEllipsize(TextUtils.TruncateAt.END);
        holder.mExpand.setImageResource(R.drawable.ic_keyboard_arrow_down_black_24dp);
    }

}
