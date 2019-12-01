package de.fwpm.android.fefesblog.fragments;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.google.android.material.snackbar.Snackbar;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import de.fwpm.android.fefesblog.BlogPost;
import de.fwpm.android.fefesblog.BlogPostViewModel;
import de.fwpm.android.fefesblog.DetailsActivity;
import de.fwpm.android.fefesblog.R;
import de.fwpm.android.fefesblog.adapter.BookmarkRecyclerViewAdapter;
import de.fwpm.android.fefesblog.utils.CustomTextView;
import de.fwpm.android.fefesblog.utils.NetworkUtils;

import static de.fwpm.android.fefesblog.MainActivity.fab;
import static de.fwpm.android.fefesblog.utils.CustomTextView.handleClickedLink;
import static de.fwpm.android.fefesblog.utils.SharePostUtil.sharePost;

/**
 * Created by alex on 20.01.18.
 */

public class BookmarkFragment extends Fragment{

    private BlogPostViewModel viewModel;
    private RecyclerView mRecyclerView;
    private BookmarkRecyclerViewAdapter recyclerViewAdapter;

    private Context mContext;
    private static RecyclerView.LayoutManager mLayoutManager;
    private static RecyclerView.SmoothScroller smoothScroller;
    private View view;

    private static BookmarkFragment instance;

    public BookmarkFragment() {
        instance = this;
    }

    public static BookmarkFragment getInstance() {
        if(instance != null)
            return instance;
        else return new BookmarkFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view =  inflater.inflate(R.layout.fragment_bookmarks, container, false);

        mContext = getContext();

        initView();
        initViewModel();

        return view;

    }

    private void initViewModel() {

        viewModel = ViewModelProviders.of(this).get(BlogPostViewModel.class);

        viewModel.getBookmarkedPosts().observe(this, new Observer<List<BlogPost>>() {
            @Override
            public void onChanged(@Nullable List<BlogPost> blogPosts) {

                showNoBookmarkScreen(blogPosts.size() == 0);
                recyclerViewAdapter.dataChanged(blogPosts);

            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private void initView() {

        mRecyclerView = (RecyclerView) view.findViewById(R.id.bookmark_recyclerview);
        mRecyclerView.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);

        smoothScroller = new LinearSmoothScroller(mContext) {
            @Override protected int getVerticalSnapPreference() {
                return LinearSmoothScroller.SNAP_TO_START;
            }
        };

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener(){
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy){
                if (dy > 0 && fab.isShown())
                    fab.hide();
                else if(dy < 0 && !fab.isShown()) fab.show();
            }

        });

        recyclerViewAdapter = new BookmarkRecyclerViewAdapter(mContext,
                new BookmarkRecyclerViewAdapter.OnItemClickListener() {

                    @Override
                    public void onItemClick(int position, BlogPost blogPost) {

                        Intent intent;

                        if (CustomTextView.clickedLink != null) {

                            if(!handleClickedLink(getActivity(), blogPost, CustomTextView.clickedLink)) {
                                new NetworkUtils(getContext()).noNetwork((FrameLayout) view.findViewById(R.id.bookmarkFragment));
                            }

                            CustomTextView.clickedLink = null;

                        } else {
                            intent = new Intent(getActivity(), DetailsActivity.class);
                            intent.putExtra(DetailsActivity.INTENT_BLOG_POST, (Serializable) blogPost);
                            startActivity(intent);
                        }

                    }

                    @Override
                    public void onBookMarkClick(int position, BlogPost blogPost) {

                        if(blogPost.isBookmarked()) showUnbookmarkedSnackbar(blogPost);
                        blogPost.setBookmarked(!blogPost.isBookmarked());
                        viewModel.updatePost(blogPost);

                    }

                    @Override
                    public void onShareClick(int position, BlogPost blogPost) {
                        sharePost(mContext, blogPost);
                    }

                    @Override
                    public void onExpandListener(int scrollTo) {
                        jumpToPosition(scrollTo);
                    }


                },new ArrayList<BlogPost>());

        mRecyclerView.setAdapter(recyclerViewAdapter);
    }

    public void jumpToPosition(int position) {

        if(position >= 0 && ((LinearLayoutManager) mRecyclerView.getLayoutManager()).findFirstVisibleItemPosition() > position) {
            smoothScroller.setTargetPosition(position);
            mLayoutManager.startSmoothScroll(smoothScroller);
        }

    }

    private void showNoBookmarkScreen(boolean show) {
        view.findViewById(R.id.noBookmarkScreen).setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void showUnbookmarkedSnackbar(final BlogPost blogPost) {

        Snackbar.make(view, "Lesezeichen entfernt", Snackbar.LENGTH_LONG)
                .setAction("RÜCKGÄNGIG", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        blogPost.setBookmarked(true);
                        viewModel.updatePost(blogPost);

                    }
                }).show();

    }


}
