package de.fwpm.android.fefesblog.adapter;

import android.content.Context;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

import de.fwpm.android.fefesblog.BlogPost;
import de.fwpm.android.fefesblog.R;
import de.fwpm.android.fefesblog.database.AppDatabase;
import de.fwpm.android.fefesblog.fragments.SettingFragment;
import de.fwpm.android.fefesblog.utils.HeaderItemDecoration;
import de.fwpm.android.fefesblog.utils.PreventScrollTextView;

import static de.fwpm.android.fefesblog.fragments.NewPostsFragment.jumpToPosition;
import static de.fwpm.android.fefesblog.utils.CustomTextView.setTextViewHTML;
import static de.fwpm.android.fefesblog.utils.PreventScrollTextView.dpToPx;

/**
 * Created by alex on 20.01.18.
 */

public class NewPostsRecyclerViewAdapter extends RecyclerView.Adapter<NewPostsRecyclerViewAdapter.ViewHolder> implements HeaderItemDecoration.StickyHeaderInterface {

    public static int MAX_LINES;

    static ArrayList<BlogPost> mData;
    Context mContext;
    static OnItemClickListener mListener;
    OnBottomReachListener mOnBottomReachListener;
    public static ArrayList<Integer> expandedItems;

    public NewPostsRecyclerViewAdapter(Context context, final OnItemClickListener listener, final OnBottomReachListener onBottomReachListener ,final ArrayList<BlogPost> data) {

        mContext = context;
        mData = data;
        mListener = listener;
        mOnBottomReachListener = onBottomReachListener;
        MAX_LINES = PreferenceManager.getDefaultSharedPreferences(mContext).getInt(SettingFragment.PREVIEW_SIZE, 6);
        expandedItems = new ArrayList<>();

    }

    public interface OnItemClickListener {

        void onItemClick(int position, BlogPost blogPost);

        void onShareClick(int position, BlogPost blogPost);

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

        abstract public void setClickListener(final BlogPost blogPost,final int position);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
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
    public int getHeaderPositionForItem(int itemPosition) {
        int headerPosition = 0;
        do {
            if (this.isHeader(itemPosition)) {
                headerPosition = itemPosition;
                break;
            }
            itemPosition -= 1;
        } while (itemPosition >= 0);
        return headerPosition;
    }

    @Override
    public int getHeaderLayout(int headerPosition) {
        return R.layout.new_post_section;
    }

    @Override
    public void bindHeaderData(View header, int headerPosition) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, d. MMMM yyyy", Locale.GERMANY);
        ((TextView) header.findViewById(R.id.section_label)).setText(dateFormat.format(mData.get(headerPosition).getDate()));
    }

    @Override
    public boolean isHeader(int itemPosition) {
        if(mData.size() > itemPosition)
            return mData.get(itemPosition).type == BlogPost.TYPE_SECTION;

        return false;
    }

    static class DataViewHolder extends ViewHolder {
        private PreventScrollTextView mContent;
        private TextView mUpdateBanner;
        private ImageButton mExpand;
        private ImageButton mBookmark;
        private ImageButton mShare;
        private View dividerBottom;


        public DataViewHolder(View itemView, int viewType) {
            super(itemView, viewType);
            mContent = (PreventScrollTextView) itemView.findViewById(R.id.post_text);
            mExpand = (ImageButton) itemView.findViewById(R.id.expand);
            mBookmark = (ImageButton) itemView.findViewById(R.id.bookmark);
            mUpdateBanner = (TextView) itemView.findViewById(R.id.update_banner);
            mShare = (ImageButton) itemView.findViewById(R.id.share);
            dividerBottom = (View) itemView.findViewById(R.id.divider_bottom);


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
            if(expandedItems.contains(position)) expandContent();
            else closeContent();
            setBookmarkIcon(blogPost.isBookmarked());
            if(position+1 < mData.size())
                dividerBottom.setVisibility( mData.get(position+1).type == BlogPost.TYPE_SECTION ? View.GONE : View.VISIBLE );



            mContent.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {

                    int bannerMarginRight = 0;

                    if(mContent.getLineCount() <= MAX_LINES) {
                        mExpand.setVisibility(View.GONE);
                        bannerMarginRight = (int) dpToPx(32);
                    } else {
                        mExpand.setVisibility(View.VISIBLE);
                    }

                    ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) mUpdateBanner.getLayoutParams();
                    params.setMargins(0, 0, bannerMarginRight, 0);
                    mUpdateBanner.setLayoutParams(params);

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
                        expandedItems.add(position);
                    } else {
                        closeContent();
                        jumpToPosition((position == 0) ? 0 : position-1);
                        expandedItems.remove((Integer) position);
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
