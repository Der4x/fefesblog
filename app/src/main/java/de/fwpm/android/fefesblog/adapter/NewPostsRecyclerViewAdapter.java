package de.fwpm.android.fefesblog.adapter;

import android.content.Context;
import android.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
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
import de.fwpm.android.fefesblog.utils.PinnedHeaderItemDecoration;
import de.fwpm.android.fefesblog.utils.PreventScrollTextView;

import static de.fwpm.android.fefesblog.fragments.NewPostsFragment.jumpToPosition;
import static de.fwpm.android.fefesblog.utils.CustomTextView.setTextViewHTML;

/**
 * Created by alex on 20.01.18.
 */

public class NewPostsRecyclerViewAdapter extends RecyclerView.Adapter<NewPostsRecyclerViewAdapter.ViewHolder> implements PinnedHeaderItemDecoration.PinnedHeaderAdapter {

    private static final String TAG = "NPRecyclerViewAdapter";
    public static int MAX_LINES;

    ArrayList<BlogPost> mData;
    Context mContext;
    static OnItemClickListener mListener;
    OnBottomReachListener mOnBottomReachListener;


    public NewPostsRecyclerViewAdapter(Context context, final OnItemClickListener listener, final OnBottomReachListener onBottomReachListener ,final ArrayList<BlogPost> data) {

        mContext = context;
        mData = data;
        mListener = listener;
        mOnBottomReachListener = onBottomReachListener;
        MAX_LINES = PreferenceManager.getDefaultSharedPreferences(mContext).getInt(SettingFragment.PREVIEW_SIZE, 6);

    }

    public interface OnItemClickListener {

        void onItemClick(int position, BlogPost blogPost);

        void onShareClick(int position, BlogPost blogPost);

    }

    public interface OnBottomReachListener {
        void onBottom(int position);
    }

    public interface OnBlogPostClickListener {

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

        abstract public void setClickListener(final BlogPost blogPost,final int position);
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
        viewHolder.setClickListener(item,position);

    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    @Override
    public int getItemViewType(int position) {

        if(mData.size() > position) return mData.get(position).type;
        else return BlogPost.TYPE_DATA;
        //TODO: Debug error

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
        private PreventScrollTextView mContent;
        private TextView mUpdateBanner;
        private ImageButton mExpand;
        private ImageButton mBookmark;
        private ImageButton mShare;


        public DataViewHolder(View itemView, int viewType) {
            super(itemView, viewType);
            mContent = (PreventScrollTextView) itemView.findViewById(R.id.post_text);
            mExpand = (ImageButton) itemView.findViewById(R.id.expand);
            mBookmark = (ImageButton) itemView.findViewById(R.id.bookmark);
            mUpdateBanner = (TextView) itemView.findViewById(R.id.update_banner);
            mShare = (ImageButton) itemView.findViewById(R.id.share);

        }

        public void setClickListener(final BlogPost blogPost,final int position) {

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
            mShare.setOnClickListener(onShareListener);
        }

        @Override
        public void bindItem(final NewPostsRecyclerViewAdapter adapter, final BlogPost blogPost, final int position) {

            setTextViewHTML(mContent, blogPost.getHtmlText().split("</a>", 2)[1]);
            setBanner(blogPost);
            closeContent();
            setBookmarkIcon(blogPost.isBookmarked());

            mContent.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    if(mContent.getLineCount() <= MAX_LINES)
                        mExpand.setVisibility(View.INVISIBLE);
                    else
                        mExpand.setVisibility(View.VISIBLE);
                    //Todo: Handle new or update posts not expandable
                    return true;
                }
            });

            mBookmark.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    blogPost.setBookmarked(blogPost.isBookmarked() ? false : true);
                    setBookmarkIcon(blogPost.isBookmarked());

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            AppDatabase.getInstance(adapter.mContext).blogPostDao().updateBlogPost(blogPost);
                        }
                    }).start();

                }
            });

            mExpand.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if(blogPost.isUpdate() || !blogPost.isHasBeenRead()) {

                        blogPost.setUpdate(false);
                        blogPost.setHasBeenRead(true);
                        setBanner(blogPost);
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                AppDatabase.getInstance(adapter.mContext).blogPostDao().updateBlogPost(blogPost);
                            }
                        }).start();

                    }

                    if(mContent.getMaxLines() == MAX_LINES) {
                        expandContent();
                    } else {
                        closeContent();
                        jumpToPosition((position == 0) ? 0 : position-1);
                    }

                }
            });

        }

        private void setBanner(BlogPost blogPost) {

            mUpdateBanner.setVisibility(View.INVISIBLE);
            if(!blogPost.isHasBeenRead()) {
                mUpdateBanner.setText("NEU!");
                mUpdateBanner.setVisibility(View.VISIBLE);

            } else if(blogPost.isUpdate()) {
                mUpdateBanner.setText("Update!");
                mUpdateBanner.setVisibility(View.VISIBLE);
            }
        }

        private void setBookmarkIcon(boolean isBookmarked) {

            if(isBookmarked) mBookmark.setImageResource(R.drawable.ic_stat_bookmark);
            else mBookmark.setImageResource(R.drawable.ic_stat_bookmark_border);

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

    static class SectionViewHolder extends ViewHolder {
        private TextView mSectionLabel;

        public SectionViewHolder(View itemView, int viewType) {
            super(itemView, viewType);
            mSectionLabel = (TextView) itemView.findViewById(R.id.section_label);
        }


        @Override
        public void bindItem(NewPostsRecyclerViewAdapter adapter, BlogPost blogPost, int position) {

            SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, d. MMMM yyyy", Locale.GERMANY);
            mSectionLabel.setText(dateFormat.format(blogPost.getDate()));

        }

        @Override
        public void setClickListener(BlogPost blogPost, int position) {

        }
    }

}
