package de.fwpm.android.fefesblog.adapter;

import android.content.Context;
import android.preference.PreferenceManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageButton;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import de.fwpm.android.fefesblog.BlogPost;
import de.fwpm.android.fefesblog.R;
import de.fwpm.android.fefesblog.SearchActivity;
import de.fwpm.android.fefesblog.fragments.SettingFragment;
import de.fwpm.android.fefesblog.utils.PreventScrollTextView;

import static de.fwpm.android.fefesblog.utils.CustomTextView.setTextViewHTML;

/**
 * Created by alex on 22.01.18.
 */

public class SearchRecyclerViewAdapter extends RecyclerView.Adapter<SearchRecyclerViewAdapter.ViewHolder> {

    private static int MAX_LINES;

    private List<BlogPost> mData;
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
        void onItemShare(int position, BlogPost blogPost);
        void onBookmarkClick(int position, BlogPost blogPost);
    }

    public void dataChanged(List<BlogPost> newData) {
        mData = newData;
        notifyDataSetChanged();
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
            final View.OnClickListener onItemShareListener = new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    mListener.onItemShare(position, blogPost);
                }
            };
            final View.OnClickListener onBookmarkListener = new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    mListener.onBookmarkClick(position, blogPost);
                    setBookmarkIcon(blogPost);
                }
            };
            final View.OnClickListener onExpandListener = new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    if (mContent.getMaxLines() == MAX_LINES) {
                        expandContent();
                    } else {
                        closeContent();
                        ((SearchActivity) mContext).jumpToPosition(position - 1);
                    }
                }
            };
            mContent.setOnClickListener(onClickListener);
            mDate.setOnClickListener(onClickListener);
            mShare.setOnClickListener(onItemShareListener);
            mBookmark.setOnClickListener(onBookmarkListener);
            mExpand.setOnClickListener(onExpandListener);
        }

        private void expandContent() {
            mContent.setMaxLines(Integer.MAX_VALUE);
            mContent.setEllipsize(null);
            mExpand.setImageResource(R.drawable.ic_stat_keyboard_arrow_up);
        }

        private void closeContent() {
            mContent.setMaxLines(MAX_LINES);
            mContent.setEllipsize(TextUtils.TruncateAt.END);
            mExpand.setImageResource(R.drawable.ic_stat_keyboard_arrow_down);
        }

        private void setBookmarkIcon(BlogPost blogPost) {
            if (blogPost.isBookmarked())
                mBookmark.setImageResource(R.drawable.ic_stat_bookmark);
            else mBookmark.setImageResource(R.drawable.ic_stat_bookmark_border);
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

        holder.closeContent();

        holder.setBookmarkIcon(blogPost);

        String[] htmltext = blogPost.getHtmlText().split("</a>", 2);

        if (htmltext.length > 1)
            setTextViewHTML(holder.mContent, blogPost.getHtmlText().split("</a>", 2)[1]);
        else {
            holder.mContent.setText(blogPost.getText());
        }

        holder.mContent.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                if(holder.mContent.getLineCount() <= MAX_LINES)
                    holder.mExpand.setVisibility(View.INVISIBLE);
                else
                    holder.mExpand.setVisibility(View.VISIBLE);
                return true;
            }
        });

        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, d. MMMM yyyy", Locale.GERMANY);
        holder.mDate.setText(dateFormat.format(blogPost.getDate()));
        holder.setClickListener(position, blogPost);

    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

}
