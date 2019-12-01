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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.fwpm.android.fefesblog.BlogPost;
import de.fwpm.android.fefesblog.BlogPostViewModel;
import de.fwpm.android.fefesblog.DetailsActivity;
import de.fwpm.android.fefesblog.R;
import de.fwpm.android.fefesblog.adapter.NewPostsRecyclerViewAdapter;
import de.fwpm.android.fefesblog.utils.CustomTextView;
import de.fwpm.android.fefesblog.utils.HeaderItemDecoration;
import de.fwpm.android.fefesblog.utils.NetworkUtils;

import static de.fwpm.android.fefesblog.MainActivity.fab;
import static de.fwpm.android.fefesblog.adapter.NewPostsRecyclerViewAdapter.expandedItems;
import static de.fwpm.android.fefesblog.utils.CustomTextView.handleClickedLink;
import static de.fwpm.android.fefesblog.utils.SharePostUtil.sharePost;

/**
 * Created by alex on 20.01.18.
 */

public class NewPostsFragment extends Fragment {

    private static final String TAG = "NewsPostFragment";
    private static final long FIVE_MINUTES = 5 * 60 * 1000;

    private RecyclerView mRecyclerView;
    private NewPostsRecyclerViewAdapter recyclerViewAdapter;
    private SwipeRefreshLayout mNewPostSwipeRefresh;
    private static LinearLayoutManager mLayoutManager;

    private Context context;
    private NetworkUtils networkUtils;
    private BlogPostViewModel viewModel;

    long lastSyncTimestamp;

    View view;

    private static NewPostsFragment instance;

    public NewPostsFragment() {
        instance = this;
    }

    public static NewPostsFragment getInstance() {

        if (instance != null) return instance;
        else return new NewPostsFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_newposts, container, false);

        context = getContext();
        networkUtils = new NetworkUtils(context);

        initView();
        initAdapter();
        initViewModel();

        return view;

    }

    @Override
    public void onResume() {

        super.onResume();
        if (lastSyncTimestamp == 0 || Math.abs(System.currentTimeMillis() - lastSyncTimestamp) > FIVE_MINUTES)
            startSync();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void initView() {

        mNewPostSwipeRefresh = view.findViewById(R.id.new_posts_swipe_refresh);
        mNewPostSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                startSync();
            }
        });

        mRecyclerView = view.findViewById(R.id.newpost_recyclerview);
        mRecyclerView.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);

    }

    private void initAdapter() {

        recyclerViewAdapter
                = new NewPostsRecyclerViewAdapter(getContext(),
                new NewPostsRecyclerViewAdapter.OnItemClickListener() {

                    @Override
                    public void onItemClick(int position, final BlogPost blogPost) {
                        handlePostClick(blogPost);
                    }

                    @Override
                    public void onShareClick(int position, BlogPost blogPost) {
                        sharePost(context, blogPost);
                    }

                    @Override
                    public void onBookmarkClick(int position, BlogPost blogPost) {
                        blogPost.setBookmarked(!blogPost.isBookmarked());
                        viewModel.updatePost(blogPost);
                    }

                    @Override
                    public void onStateChangedListener(int position, BlogPost blogPost) {

                        if(blogPost.isUpdate() || !blogPost.isHasBeenRead()) {

                            blogPost.setUpdate(false);
                            blogPost.setHasBeenRead(true);
                            viewModel.updatePost(blogPost);

                        }

                        jumpToPosition(position);
                    }
                },
                new NewPostsRecyclerViewAdapter.OnBottomReachListener() {
                    @Override
                    public void onBottom(int position) {
                        loadMoreData();
                    }
                },
                new ArrayList<BlogPost>());

        mRecyclerView.addItemDecoration(new HeaderItemDecoration(mRecyclerView, (HeaderItemDecoration.StickyHeaderInterface) recyclerViewAdapter));
        mRecyclerView.setAdapter(recyclerViewAdapter);

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0 && fab.isShown())
                    fab.hide();
                else if (dy < 0 && !fab.isShown()) fab.show();
            }

        });
    }

    private void initViewModel() {

        viewModel = ViewModelProviders.of(this).get(BlogPostViewModel.class);

        viewModel.getAllPosts().observe(this, new Observer<List<BlogPost>>() {
            @Override
            public void onChanged(@Nullable List<BlogPost> blogPosts) {

                if(blogPosts != null && blogPosts.size() > 0) {

                    if(haveNewPosts(blogPosts)) {

                        expandedItems.clear();
                        if(mLayoutManager.findFirstVisibleItemPosition() > 0)
                            showSnackbar();

                    }

                    recyclerViewAdapter.dataChanged(addHeaders(blogPosts));
                    setRefresh(false);

                }
            }
        });

    }

    private void startSync() {

        lastSyncTimestamp = System.currentTimeMillis();

        if (networkUtils.isConnectingToInternet()) {
            viewModel.syncPosts(this);
            setRefresh(true);
        } else {
            networkUtils.noNetwork(mNewPostSwipeRefresh);
            setRefresh(false);
        }

    }

    private void loadMoreData() {

        if (networkUtils.isConnectingToInternet()) {
            viewModel.loadOlderPosts(this);
            setRefresh(true);
        } else {
            networkUtils.noNetwork(mNewPostSwipeRefresh);
            setRefresh(false);
        }

    }

    private void handlePostClick(final BlogPost blogPost) {

        if (blogPost.isUpdate() || !blogPost.isHasBeenRead()) {

            blogPost.setUpdate(false);
            blogPost.setHasBeenRead(true);
            viewModel.updatePost(blogPost);

        }

        if (CustomTextView.clickedLink != null) {

            if (!handleClickedLink(getActivity(), blogPost, CustomTextView.clickedLink)) {
                networkUtils.noNetwork(mNewPostSwipeRefresh);
            }

            CustomTextView.clickedLink = null;

        } else {

            Intent intent;
            intent = new Intent(getActivity(), DetailsActivity.class);
            intent.putExtra(DetailsActivity.INTENT_BLOG_POST, blogPost);
            startActivity(intent);

        }
    }

    public void jumpToPosition(int position) {

        if (position >= 0 && ((LinearLayoutManager) mRecyclerView.getLayoutManager()).findFirstVisibleItemPosition() > position) {

            LinearSmoothScroller smoothScroller = new LinearSmoothScroller(context) {
                @Override
                protected int getVerticalSnapPreference() {
                    return LinearSmoothScroller.SNAP_TO_START;
                }
            };

            smoothScroller.setTargetPosition(position);
            mLayoutManager.startSmoothScroll(smoothScroller);

        }

    }

    private void setRefresh(boolean bool) {
        mNewPostSwipeRefresh.setRefreshing(bool);
    }

    private void showSnackbar() {
        Snackbar bar = Snackbar.make(mNewPostSwipeRefresh, "Neue Posts", Snackbar.LENGTH_LONG)
                .setDuration(5000)
                .setAction("ANZEIGEN", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        jumpToPosition(0);
                    }
                });

        bar.show();
    }

    private boolean haveNewPosts(List<BlogPost> newData) {
        if (recyclerViewAdapter.mData.size() > 0) return !recyclerViewAdapter.mData.get(1).getUrl().equals(newData.get(0).getUrl());
        else return false;
    }

    private List<BlogPost> addHeaders(List<BlogPost> list) {

        ArrayList<BlogPost> headerList = new ArrayList<>();

        Date firstDate = list.get(0).getDate();
        addHeader(headerList, firstDate);

        SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd", Locale.GERMANY);

        for (BlogPost blogPost : list) {

            if (fmt.format(blogPost.getDate()).equals(fmt.format(firstDate))) {

                headerList.add(blogPost);

            } else {

                firstDate = blogPost.getDate();
                addHeader(headerList, firstDate);
                headerList.add(blogPost);

            }
        }

        return headerList;

    }

    private void addHeader(ArrayList<BlogPost> listWithHeaders, Date firstDate) {
        listWithHeaders.add(new BlogPost(firstDate, BlogPost.TYPE_SECTION));
    }

    public void error(final String message) {

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setRefresh(false);
                Toast.makeText(context, "Fehler beim Laden neuer Posts: " + message, Toast.LENGTH_LONG).show();

            }
        });

    }

}
