package de.fwpm.android.fefesblog;

import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import java.io.Serializable;
import java.util.ArrayList;

import de.fwpm.android.fefesblog.adapter.SearchRecyclerViewAdapter;
import de.fwpm.android.fefesblog.data.SearchDataFetcher;
import de.fwpm.android.fefesblog.database.AppDatabase;
import de.fwpm.android.fefesblog.utils.CustomTextView;
import de.fwpm.android.fefesblog.utils.NetworkUtils;

import static de.fwpm.android.fefesblog.DetailsActivity.INTENT_URL;
import static de.fwpm.android.fefesblog.utils.CustomTextView.handleClickedLink;

public class SearchActivity extends AppCompatActivity implements SearchView.OnQueryTextListener, SearchView.OnCloseListener {

    private static final String TAG = "SearchActivity";

    private Context mContext;
    private ArrayList<BlogPost> mListOfPosts;

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
    private SearchActivity activity;
    private boolean darkTheme;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (App.getInstance().isNightModeEnabled()) {
            setTheme(R.style.MainActivityThemeDark);
            darkTheme = true;
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.getNavigationIcon().setColorFilter(getResources().getColor(darkTheme ? R.color.primaryTextColorDark : R.color.secondaryTextColorLight), PorterDuff.Mode.SRC_ATOP);
        if(darkTheme) toolbar.getContext().setTheme(R.style.AppTheme_Toolbar_Dark);

        mContext = this;
        activity = this;
        mHandler = new Handler();
        networkUtils = new NetworkUtils(this);

        mListOfPosts = new ArrayList<>();

        initView();

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
                },
                mListOfPosts);

        mRecyclerView.setAdapter(recyclerViewAdapter);
    }

    private void search(final String query) {

        showNoResultScreen(false);

        if(networkUtils.isConnectingToInternet()) {

            new SearchDataFetcher(this).execute(query);
            showProgressBar(true);

        } else {

            networkUtils.noNetwork(mContainer);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    ArrayList<BlogPost> data = (ArrayList<BlogPost>) AppDatabase.getInstance(mContext).blogPostDao().searchPosts("%" + query + "%");
                    populateResult(data);
                }
            }).start();

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
        mSearchView.setEnabled(true);

        // expand searchview on create
        searchItem.expandActionView();

//        searchItem.getIcon().setColorFilter(getResources().getColor(darkTheme ? R.color.primaryTextColorDark : R.color.secondaryTextColorLight), PorterDuff.Mode.SRC_IN);

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

    public void populateResult(final ArrayList<BlogPost> allPosts) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mListOfPosts.clear();
                mListOfPosts.addAll(allPosts);
                recyclerViewAdapter.notifyDataSetChanged();
                mRecyclerView.scrollToPosition(0);
                showProgressBar(false);

                if (allPosts.size() == 0) showNoResultScreen(true);
            }
        });
    }

    private void showNoResultScreen(boolean show) {

        noResultLayout.setVisibility(show ? View.VISIBLE : View.GONE);

    }

    private void showProgressBar(final boolean show) {

        mProgressBar.setVisibility(show ? View.VISIBLE : View.INVISIBLE);

    }

}
