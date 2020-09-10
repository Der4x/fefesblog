package de.fwpm.android.fefesblog.adapter;

import android.content.Context;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import de.fwpm.android.fefesblog.BlogPost;
import de.fwpm.android.fefesblog.R;
import de.fwpm.android.fefesblog.database.AppDatabase;
import de.fwpm.android.fefesblog.fragments.SettingFragment;
import de.fwpm.android.fefesblog.utils.PreventScrollTextView;

import static de.fwpm.android.fefesblog.utils.CustomTextView.setTextViewHTML;

/**
 * Created by Untiak on 23.01.2018.
 */

public class BookmarkRecyclerViewAdapter extends RecyclerView.Adapter<BookmarkRecyclerViewAdapter.ViewHolder> {
    private static final String TAG = "BMRecyclerViewAdapter";
    public static int MAX_LINES;

    private List<BlogPost> mData;
    private Context mContext;
    private OnItemClickListener mListener;
    private static ArrayList<Integer> expandedItems;

    public BookmarkRecyclerViewAdapter(Context context, final OnItemClickListener listener, final ArrayList<BlogPost> data) {

        mContext = context;
        mData = data;
        mListener = listener;
        MAX_LINES = PreferenceManager.getDefaultSharedPreferences(mContext).getInt(SettingFragment.PREVIEW_SIZE, 6);
        expandedItems = new ArrayList<>();

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
                    mListener.onItemClick(position,blogPost);
                }
            };
            final View.OnClickListener onShareListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onShareClick(position,blogPost);
                }
            };
            final View.OnClickListener onBookmarkListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onBookMarkClick(position,blogPost);
                }
            };
            final View.OnClickListener onExpandListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (mContent.getMaxLines() == MAX_LINES) {
                        expandContent();
                        expandedItems.add(position);
                    } else {
                        closeContent();
                        expandedItems.remove((Integer) position);
                        mListener.onExpandListener(position-1);

                    }
                }
            };
            mContent.setOnClickListener(onClickListener);
            mDate.setOnClickListener(onClickListener);
            mShare.setOnClickListener(onShareListener);
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

    }

    public void dataChanged(List<BlogPost> newData) {

        mData = newData;
        notifyDataSetChanged();

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.simple_post_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final BlogPost blogPost = mData.get(position);

        if(expandedItems.contains(position)) holder.expandContent();
        else holder.closeContent();

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

        holder.mContent.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                if(holder.mContent.getLineCount() <= MAX_LINES)
                    holder.mExpand.setVisibility(View.INVISIBLE);
                else
                    holder.mExpand.setVisibility(View.VISIBLE);
                //Todo: Handle new or update posts not expandable
                return true;
            }
        });

    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    @Override
    public long getItemId(int position) {
        return mData.get(position).getUrl().hashCode();
    }

    private void setBookmarkIcon(BookmarkRecyclerViewAdapter.ViewHolder holder, BlogPost blogPost) {
        if (blogPost.isBookmarked())
            holder.mBookmark.setImageResource(R.drawable.ic_stat_bookmark);
        else holder.mBookmark.setImageResource(R.drawable.ic_stat_bookmark_border);
    }

    public interface OnItemClickListener {
        void onItemClick(int position,BlogPost blogPost);
        void onBookMarkClick(int position, BlogPost blogPost);
        void onShareClick(int position, BlogPost blogPost);
        void onExpandListener(int scrollTo);
    }

}
