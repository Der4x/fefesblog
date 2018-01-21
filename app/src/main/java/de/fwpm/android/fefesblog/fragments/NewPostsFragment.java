package de.fwpm.android.fefesblog.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Date;

import de.fwpm.android.fefesblog.BlogPost;
import de.fwpm.android.fefesblog.DataFetcher;
import de.fwpm.android.fefesblog.NetworkUtils;
import de.fwpm.android.fefesblog.PinnedHeaderItemDecoration;
import de.fwpm.android.fefesblog.R;
import de.fwpm.android.fefesblog.adapter.NewPostsRecyclerViewAdapter;
import de.fwpm.android.fefesblog.database.AppDatabase;

/**
 * Created by alex on 20.01.18.
 */

public class NewPostsFragment extends Fragment {

    public static final String ARG_ITEM_ID = "item_id";
    private static final String TAG = "NewsPostFragment";

    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private NewPostsRecyclerViewAdapter recyclerViewAdapter;
    private SwipeRefreshLayout mNewPostSwipeRefresh;

    private ArrayList<BlogPost> mData;
    private ArrayList<BlogPost> mListWithHeaders;
    private AppDatabase appDatabase;
    private Handler mHandler;

    View view;

    public NewPostsFragment() {
        // Required empty public constructor
    }

    public static NewPostsFragment newInstance(int position) {
        Bundle args = new Bundle();
        args.putInt(ARG_ITEM_ID, position);
        NewPostsFragment fragment = new NewPostsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_newposts, container, false);

        mHandler = new Handler();
        appDatabase = AppDatabase.getInstance(getContext());

        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        boolean firstStart = mPrefs.getBoolean("firstStart", true);

        initView();

        if(firstStart) {
            mPrefs.edit().putBoolean("firstStart", false).commit();
            Log.d(TAG, "firstStart");
        }
        else getData();

        startSync();



        return view;

    }

    private void startSync() {

        if(new NetworkUtils(getContext()).isConnectingToInternet()) {
            new DataFetcher(this).execute();
            setRefresh(true);
        }
        else {
            Toast.makeText(getContext(), "Kein Internet!", Toast.LENGTH_LONG).show();
            setRefresh(false);
        }

    }

    private void getData() {

        new Thread(new Runnable() {
            @Override
            public void run() {

                mData = (ArrayList<BlogPost>) appDatabase.blogPostDao().getAllPosts();

                mListWithHeaders = new ArrayList<>();
                Date firstDate = mData.get(0).getDate();
                addHeader(mListWithHeaders, firstDate);

                for (BlogPost blogPost : mData) {

                    if (blogPost.getDate().equals(firstDate)) {

                        mListWithHeaders.add(blogPost);

                    } else {

                        firstDate = blogPost.getDate();
                        addHeader(mListWithHeaders, firstDate);
                        mListWithHeaders.add(blogPost);

                    }

                }

                updateUI();

            }
        }).start();

    }

    private void initView() {
        mNewPostSwipeRefresh = (SwipeRefreshLayout) view.findViewById(R.id.new_posts_swipe_refresh);
        mNewPostSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                startSync();
            }
        });

        mRecyclerView = (RecyclerView) view.findViewById(R.id.newpost_recyclerview);
        mRecyclerView.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);
    }

    private void setRefresh(boolean bool) {
        mNewPostSwipeRefresh.setRefreshing(bool);
    }

    public void populateResult() {
        getData();
    }

    private void updateUI() {

        mHandler.post(new Runnable() {
            @Override
            public void run() {

                if(recyclerViewAdapter == null) {

                    recyclerViewAdapter
                            = new NewPostsRecyclerViewAdapter(getContext(),
                            new NewPostsRecyclerViewAdapter.OnItemClickListener() {
                                @Override
                                public void onItemClick(int position) {
                                    Log.d(TAG, "onItemClick" + position);
                                }
                            },
                            new NewPostsRecyclerViewAdapter.OnBottomReachListener() {
                                @Override
                                public void onBottom(int position) {
                                    Log.d(TAG, "onBottom" + position);
                                }
                            },
                            mListWithHeaders);

                    mRecyclerView.addItemDecoration(new PinnedHeaderItemDecoration());
                    mRecyclerView.setAdapter(recyclerViewAdapter);

                } else {

                    recyclerViewAdapter.notifyDataSetChanged();

                }

                setRefresh(false);

            }
        });

    }

    private void addHeader(ArrayList<BlogPost> listWithHeaders, Date firstDate) {
        BlogPost headerBlogPost = new BlogPost(firstDate, BlogPost.TYPE_SECTION);
        listWithHeaders.add(headerBlogPost);
    }


}
