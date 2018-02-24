package de.fwpm.android.fefesblog;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
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
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import java.io.Serializable;
import java.util.ArrayList;

import de.fwpm.android.fefesblog.adapter.SearchRecyclerViewAdapter;
import de.fwpm.android.fefesblog.data.SearchDataFetcher;
import de.fwpm.android.fefesblog.utils.CustomTextView;

public class SearchActivity extends AppCompatActivity implements SearchView.OnQueryTextListener, SearchView.OnCloseListener {

    private static final String TAG = "SearchActivity";

    private Context mContext;
    private ArrayList<BlogPost> mListOfPosts;

    private Handler mHandler;
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private SearchRecyclerViewAdapter recyclerViewAdapter;

    private SearchView mSearchView;
    private ProgressBar mProgressBar;
    private LinearLayout noResultLayout;
    private String mQueryString;
    private SearchActivity activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle(getString(R.string.search));

        mContext = this;
        activity = this;
        mHandler = new Handler();
        mProgressBar = (ProgressBar) findViewById(R.id.progess_bar);
        noResultLayout = (LinearLayout) findViewById(R.id.noResultScreen);

        mListOfPosts = new ArrayList<>();

        mRecyclerView = (RecyclerView) findViewById(R.id.search_recyclerview);
        mRecyclerView.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        recyclerViewAdapter
                = new SearchRecyclerViewAdapter(mContext,
                new SearchRecyclerViewAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(int position, BlogPost blogPost) {
                        Log.d(TAG, "onItemClick" + position);
                        Intent intent = new Intent(SearchActivity.this, DetailsActivity.class);
                        intent.putExtra(DetailsActivity.INTENT_BLOG_POST, (Serializable) blogPost);
                        if (CustomTextView.clickedLink != null) {
                            intent.putExtra("CLICKED_LINK", CustomTextView.clickedLink);
                            CustomTextView.clickedLink = null;
                        }
                        startActivity(intent);
                    }
                },
                mListOfPosts);

        mRecyclerView.setAdapter(recyclerViewAdapter);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.search_menu, menu);
        MenuItem searchItem = menu.findItem(R.id.search);
        searchItem.setVisible(true);
        searchItem.setEnabled(true);

        mSearchView = (SearchView) searchItem.getActionView();
        mSearchView.setOnQueryTextListener(this);
        mSearchView.setOnCloseListener(this);
        mSearchView.setVisibility(View.VISIBLE);
        mSearchView.setEnabled(true);

        // expand searchview on create
        searchItem.expandActionView();

        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {

        if (query.length() > 2) {
            new SearchDataFetcher(this).execute(query);
            showProgressBar(true);
            showNoResultScreen(false);
        }

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

                if (mQueryString.length() > 2) {
                    new SearchDataFetcher(activity).execute(mQueryString);
                    showProgressBar(true);
                    showNoResultScreen(false);
                }

            }
        }, 500);


        return false;
    }

    @Override
    public boolean onClose() {

        return false;
    }

    public void populateResult(ArrayList<BlogPost> allPosts) {

        mListOfPosts.clear();
        mListOfPosts.addAll(allPosts);
        recyclerViewAdapter.notifyDataSetChanged();
        mRecyclerView.scrollToPosition(0);
        showProgressBar(false);

        if (allPosts.size() == 0) showNoResultScreen(true);

    }

    private void showNoResultScreen(boolean show) {

        if (show) noResultLayout.setVisibility(View.VISIBLE);
        else noResultLayout.setVisibility(View.GONE);

    }

    private void showProgressBar(final boolean show) {

        mProgressBar.setVisibility(show ? View.VISIBLE : View.INVISIBLE);

    }

}
