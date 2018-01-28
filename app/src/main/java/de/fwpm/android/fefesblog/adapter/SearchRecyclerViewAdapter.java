package de.fwpm.android.fefesblog.adapter;

import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageButton;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

import de.fwpm.android.fefesblog.BlogPost;
import de.fwpm.android.fefesblog.R;
import de.fwpm.android.fefesblog.database.AppDatabase;
import de.fwpm.android.fefesblog.fragments.SettingFragment;
import de.fwpm.android.fefesblog.utils.PreventScrollTextView;

import static de.fwpm.android.fefesblog.fragments.NewPostsFragment.jumpToPosition;
import static de.fwpm.android.fefesblog.utils.CustomTextView.setTextViewHTML;

/**
 * Created by alex on 22.01.18.
 */

public class SearchRecyclerViewAdapter extends RecyclerView.Adapter<SearchRecyclerViewAdapter.ViewHolder> {

    private static final String TAG = "SVRecyclerViewAdapter";
    private static int MAX_LINES;

    private ArrayList<BlogPost> mData;
    private Context mContext;
    private OnItemClickListener mListener;

    public SearchRecyclerViewAdapter(Context context, final OnItemClickListener listener, final ArrayList<BlogPost> data) {

        mContext = context;
        mData = data;
        mListener = listener;
        MAX_LINES = PreferenceManager.getDefaultSharedPreferences(mContext).getInt(SettingFragment.PREVIEW_SIZE, 6);

    }

    public interface OnItemClickListener {
        void onItemClick(int position, BlogPost blogPost);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView mDate;
        private PreventScrollTextView mContent;
        private ImageButton mExpand;
        private ImageButton mBookmark;
        private ImageButton mShare;

        public ViewHolder(View itemView) {
            super(itemView);
            mContent = (PreventScrollTextView) itemView.findViewById(R.id.post_text);
            mDate = (TextView) itemView.findViewById(R.id.post_date);
            mExpand = (ImageButton) itemView.findViewById(R.id.expand);
            mBookmark = (ImageButton) itemView.findViewById(R.id.bookmark);
            mShare = (ImageButton) itemView.findViewById(R.id.share);
        }

        void setClickListener(final int position, final BlogPost blogPost) {

            final View.OnClickListener onClickListener = new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    mListener.onItemClick(position, blogPost);
                }
            };
            mContent.setOnClickListener(onClickListener);
            mDate.setOnClickListener(onClickListener);
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

        holder.mContent.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                if(holder.mContent.getLineCount() < MAX_LINES)
                    holder.mExpand.setVisibility(View.INVISIBLE);
                else
                    holder.mExpand.setVisibility(View.VISIBLE);
                //Todo: Handle new or update posts not expandable
                return true;
            }
        });

        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, d. MMMM yyyy", Locale.GERMANY);
        holder.mDate.setText(dateFormat.format(blogPost.getDate()));
        holder.setClickListener(position, blogPost);

        holder.mBookmark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                blogPost.setBookmarked(!blogPost.isBookmarked());
                setBookmarkIcon(holder, blogPost);

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        AppDatabase.getInstance(mContext).blogPostDao().insertBlogPost(blogPost);
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
                            AppDatabase.getInstance(mContext).blogPostDao().insertBlogPost(blogPost);
                        }
                    }).start();

                }

                if (holder.mContent.getMaxLines() == MAX_LINES) {
                    expandContent(holder);
                } else {
                    closeContent(holder);
                    jumpToPosition((position == 0) ? 0 : position - 1);
                }

            }
        });

        holder.mShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String postText = blogPost.getText();
                String preView = postText.length() > 99 ? postText.substring(4, 100) : postText.substring(4);
                Intent share = new Intent();
                share.setAction(Intent.ACTION_SEND);
                share.putExtra(Intent.EXTRA_TEXT, preView + "...\n\n" + blogPost.getUrl());
                share.setType("text/plain");
                mContext.startActivity(Intent.createChooser(share, mContext.getResources().getText(R.string.share_to)));
            }
        });

    }

    private void setBookmarkIcon(ViewHolder holder, BlogPost blogPost) {
        if (blogPost.isBookmarked())
            holder.mBookmark.setImageResource(R.drawable.ic_bookmark_black_24dp);
        else holder.mBookmark.setImageResource(R.drawable.ic_bookmark_border_black_24dp);
    }

    private void expandContent(ViewHolder holder) {
        holder.mContent.setMaxLines(Integer.MAX_VALUE);
        holder.mContent.setEllipsize(null);
        holder.mExpand.setImageResource(R.drawable.ic_keyboard_arrow_up_black_24dp);
    }

    private void closeContent(ViewHolder holder) {
        holder.mContent.setMaxLines(MAX_LINES);
        holder.mContent.setEllipsize(TextUtils.TruncateAt.END);
        holder.mExpand.setImageResource(R.drawable.ic_keyboard_arrow_down_black_24dp);
    }


    @Override
    public int getItemCount() {
        return mData.size();
    }

}
