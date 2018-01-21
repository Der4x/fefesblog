package de.fwpm.android.fefesblog.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import de.fwpm.android.fefesblog.BlogPost;
import de.fwpm.android.fefesblog.DataFetcher;
import de.fwpm.android.fefesblog.PinnedHeaderItemDecoration;
import de.fwpm.android.fefesblog.R;
import de.fwpm.android.fefesblog.adapter.NewPostsRecyclerViewAdapter;

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

        view =  inflater.inflate(R.layout.fragment_newposts, container, false);

        mNewPostSwipeRefresh = (SwipeRefreshLayout) view.findViewById(R.id.new_posts_swipe_refresh);
        mNewPostSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                setRefresh(false);
            }
        });

        new DataFetcher(this).execute();
        setRefresh(true);

        return view;

    }

    private void setRefresh(boolean bool) {
        mNewPostSwipeRefresh.setRefreshing(bool);
    }

    public void populateResult(ArrayList<BlogPost> allPosts) {

        if(recyclerViewAdapter == null) {

            initRecyclerView(allPosts);

        }

    }

    private void initRecyclerView(ArrayList<BlogPost> allPosts) {

        mRecyclerView = (RecyclerView) view.findViewById(R.id.newpost_recyclerview);
        mRecyclerView.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.addItemDecoration(new PinnedHeaderItemDecoration());

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
                allPosts);
        mRecyclerView.setAdapter(recyclerViewAdapter);
        setRefresh(false);

    }



}
