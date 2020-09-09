package de.fwpm.android.fefesblog;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.OnApplyWindowInsetsListener;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.google.android.material.appbar.AppBarLayout;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import de.fwpm.android.fefesblog.adapter.SearchRecyclerViewAdapter;
import de.fwpm.android.fefesblog.utils.CustomTextView;
import de.fwpm.android.fefesblog.utils.NetworkUtils;

import static de.fwpm.android.fefesblog.utils.CustomTextView.handleClickedLink;
import static de.fwpm.android.fefesblog.utils.SharePostUtil.sharePost;

public class SearchActivity extends AppCompatActivity implements SearchView.OnQueryTextListener, SearchView.OnCloseListener {

    private static final String TAG = "SearchActivity";

    private Context mContext;
    private BlogPostViewModel viewModel;
    private Handler mHandler;
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private SearchRecyclerViewAdapter recyclerViewAdapter;
    private NetworkUtils networkUtils;

    private SearchView mSearchView;
    private ProgressBar mProgressBar;
    private LinearLayout noResultLayout;
    private CoordinatorLayout mContainer;
    private String mQueryString;
    private boolean darkTheme;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (App.getInstance().isNightModeEnabled()) {
            darkTheme = true;
            if(App.getInstance().isAmoledModeEnabled())
                setTheme(R.style.MainActivityThemeAmoledDark);
            else
                setTheme(R.style.MainActivityThemeDark);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        super.onCreate(savedInstanceState);
        setContentView(darkTheme ? R.layout.activity_search_dark : R.layout.activity_search);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        AppBarLayout appBarLayout = (AppBarLayout) findViewById(R.id.toolbar_layout);
        if(darkTheme) appBarLayout.getContext().setTheme(R.style.AppTheme_AppBarOverlay_Dark);

        mContext = this;
        mHandler = new Handler();
        networkUtils = new NetworkUtils(this);

        initView();

        viewModel = ViewModelProviders.of(this).get(BlogPostViewModel.class);
        viewModel.getSearchList().observe(this, new Observer<List<BlogPost>>() {
            @Override
            public void onChanged(@Nullable List<BlogPost> blogPosts) {

                if(blogPosts != null && blogPosts.size() > 0) {

                    recyclerViewAdapter.dataChanged(blogPosts);
                    mRecyclerView.scrollToPosition(0);

                } else showNoResultScreen(true);

                showProgressBar(false);

            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

            ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), new OnApplyWindowInsetsListener() {
                @Override
                public WindowInsetsCompat onApplyWindowInsets(View v, WindowInsetsCompat insets) {

                    AppBarLayout.LayoutParams params = (AppBarLayout.LayoutParams) toolbar.getLayoutParams();

                    int margin = insets.getSystemWindowInsetTop();

                    if(margin > 0) {

                        params.topMargin = margin;
                        toolbar.setLayoutParams(params);

                    }

                    return insets.consumeSystemWindowInsets();
                }
            });
        }

    }

    private void initView() {

        setTitle(getString(R.string.search));
        mProgressBar = (ProgressBar) findViewById(R.id.progess_bar);
        mContainer = (CoordinatorLayout) findViewById(R.id.container);
        noResultLayout = (LinearLayout) findViewById(R.id.noResultScreen);
        mRecyclerView = (RecyclerView) findViewById(R.id.search_recyclerview);
        mRecyclerView.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        recyclerViewAdapter
                = new SearchRecyclerViewAdapter(mContext,
                new SearchRecyclerViewAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(int position, BlogPost blogPost) {
                        Intent intent;

                        if (CustomTextView.clickedLink != null) {

                            if(!handleClickedLink(SearchActivity.this, blogPost, CustomTextView.clickedLink)) {
                                networkUtils.noNetwork(mContainer);
                            }

                            CustomTextView.clickedLink = null;

                        } else {
                            intent = new Intent(SearchActivity.this, DetailsActivity.class);
                            intent.putExtra(DetailsActivity.INTENT_BLOG_POST, (Serializable) blogPost);
                            startActivity(intent);
                        }
                    }

                    @Override
                    public void onItemShare(int position, BlogPost blogPost) {
                        sharePost(mContext, blogPost);
                    }

                    @Override
                    public void onBookmarkClick(int position, BlogPost blogPost) {
                        blogPost.setBookmarked(!blogPost.isBookmarked());
                        viewModel.insertPost(blogPost);
                    }
                },
                new ArrayList<BlogPost>());

        mRecyclerView.setAdapter(recyclerViewAdapter);


    }

    private void search(final String query) {

        showNoResultScreen(false);

        if(networkUtils.isConnectingToInternet()) {

            viewModel.searchPosts(query);
            showProgressBar(true);

        } else {

            networkUtils.noNetwork(mContainer);
            viewModel.searchPostsInDatabase(query);

        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.search_menu, menu);

        MenuItem searchItem = menu.findItem(R.id.search);
        searchItem.setVisible(true);
        searchItem.setEnabled(true);
        searchItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem menuItem) {
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem menuItem) {

                finish();
                return true;

            }
        });

        mSearchView = (SearchView) searchItem.getActionView();
        mSearchView.setOnQueryTextListener(this);
        mSearchView.setOnCloseListener(this);
        mSearchView.setVisibility(View.VISIBLE);
        mSearchView.setQueryHint("Suchbegriff");
        mSearchView.setEnabled(true);

        View v = mSearchView.findViewById(androidx.appcompat.R.id.search_plate);
        v.setBackgroundColor(Color.parseColor("#00ffffff"));

        // expand searchview on create
        searchItem.expandActionView();

        searchItem.getIcon().setColorFilter(getResources().getColor(darkTheme ? R.color.primaryTextColorDark : R.color.secondaryTextColorLight), PorterDuff.Mode.SRC_IN);

        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {

        if (query.length() > 2) search(query);
        return false;

    }

    @Override
    public boolean onQueryTextChange(String newText) {

        mQueryString = newText;

        //wait for stop typing
        mHandler.removeCallbacksAndMessages(null);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {

                if (mQueryString.length() > 2) search(mQueryString);

            }
        }, 500);

        return false;
    }

    @Override
    public boolean onClose() {

        return false;
    }

    private void showNoResultScreen(boolean show) {

        noResultLayout.setVisibility(show ? View.VISIBLE : View.GONE);

    }

    private void showProgressBar(final boolean show) {

        mProgressBar.setVisibility(show ? View.VISIBLE : View.INVISIBLE);

    }

    public void jumpToPosition(int position) {

        if (position >= 0 && ((LinearLayoutManager) mRecyclerView.getLayoutManager()).findFirstVisibleItemPosition() > position) {

            LinearSmoothScroller smoothScroller = new LinearSmoothScroller(this) {
                @Override
                protected int getVerticalSnapPreference() {
                    return LinearSmoothScroller.SNAP_TO_START;
                }
            };

            smoothScroller.setTargetPosition(position);
            mLayoutManager.startSmoothScroll(smoothScroller);

        }

    }

}
