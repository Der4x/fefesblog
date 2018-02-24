package de.fwpm.android.fefesblog.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import de.fwpm.android.fefesblog.BlogPost;
import de.fwpm.android.fefesblog.DetailsActivity;
import de.fwpm.android.fefesblog.data.DataFetcher;
import de.fwpm.android.fefesblog.utils.CustomTextView;
import de.fwpm.android.fefesblog.utils.NetworkUtils;
import de.fwpm.android.fefesblog.utils.PinnedHeaderItemDecoration;
import de.fwpm.android.fefesblog.R;
import de.fwpm.android.fefesblog.adapter.NewPostsRecyclerViewAdapter;
import de.fwpm.android.fefesblog.database.AppDatabase;

import static de.fwpm.android.fefesblog.MainActivity.FIRST_START;
import static de.fwpm.android.fefesblog.MainActivity.fab;

/**
 * Created by alex on 20.01.18.
 */

public class NewPostsFragment extends Fragment implements FragmentLifecycle{

    public static final String ARG_ITEM_ID = "item_id";
    private static final String TAG = "NewsPostFragment";

    private static RecyclerView mRecyclerView;
    private static RecyclerView.LayoutManager mLayoutManager;
    private NewPostsRecyclerViewAdapter recyclerViewAdapter;
    private SwipeRefreshLayout mNewPostSwipeRefresh;
    private static RecyclerView.SmoothScroller smoothScroller;

    private ArrayList<BlogPost> mListWithHeaders;
    private Handler mHandler;

    static Context context;
    static NetworkUtils networkUtils;

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

        context = getContext();
        networkUtils = new NetworkUtils(context);

        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        boolean firstStart = mPrefs.getBoolean(FIRST_START, true);

        initView();

        if(firstStart) {
            mPrefs.edit().putBoolean(FIRST_START, false).commit();
            Log.d(TAG, "firstStart");
        }
        else this.getData();

        return view;

    }

    @Override
    public void onResume() {

        super.onResume();
        startSync();

    }

    private void startSync() {

        if(networkUtils.isConnectingToInternet()) {
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

                ArrayList<BlogPost> data = (ArrayList<BlogPost>) AppDatabase.getInstance(context).blogPostDao().getAllPosts();

                if(mListWithHeaders == null)
                    mListWithHeaders = new ArrayList<>();
                else mListWithHeaders.clear();

                Date firstDate = data.get(0).getDate();
                addHeader(mListWithHeaders, firstDate);

                SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd");

                for (BlogPost blogPost : data) {

                    if (fmt.format(blogPost.getDate()).equals(fmt.format(firstDate))) {

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

        smoothScroller = new LinearSmoothScroller(context) {
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

    }

    public static void jumpToPosition(int position) {

        smoothScroller.setTargetPosition(position);
        mLayoutManager.startSmoothScroll(smoothScroller);

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
                                public void onItemClick(int position, final BlogPost blogPost) {
                                    if(blogPost.isUpdate() || !blogPost.isHasBeenRead()) {

                                        blogPost.setUpdate(false);
                                        blogPost.setHasBeenRead(true);
                                        new Thread(new Runnable() {
                                            @Override
                                            public void run() {
                                                AppDatabase.getInstance(context).blogPostDao().updateBlogPost(blogPost);
                                            }
                                        }).start();

                                    }

                                    Intent intent = new Intent(getActivity(), DetailsActivity.class);
                                    intent.putExtra(DetailsActivity.INTENT_BLOG_POST, (Serializable) blogPost);
                                    if(CustomTextView.clickedLink != null) {
                                        intent.putExtra("CLICKED_LINK", CustomTextView.clickedLink);
                                        CustomTextView.clickedLink = null;
                                    }
                                    startActivity(intent);

                                }

                                @Override
                                public void onShareClick(int position, BlogPost blogPost) {
                                    String postText = blogPost.getText();
                                    String preView = postText.length() > 99 ?  postText.substring(4,100) : postText.substring(4);
                                    Intent share = new Intent();
                                    share.setAction(Intent.ACTION_SEND);
                                    share.putExtra(Intent.EXTRA_TEXT, preView +"...\n\n" +blogPost.getUrl());
                                    share.setType("text/plain");
                                    startActivity(Intent.createChooser(share, getResources().getText(R.string.share_to)));
                                }
                            },
                            new NewPostsRecyclerViewAdapter.OnBottomReachListener() {
                                @Override
                                public void onBottom(int position) {
                                    Log.d(TAG, "onBottom" + position);
                                    loadMoreData();

                                }
                            },
                            mListWithHeaders);

                    mRecyclerView.addItemDecoration(new PinnedHeaderItemDecoration());
                    mRecyclerView.setAdapter(recyclerViewAdapter);

                } else {

                    mRecyclerView.getRecycledViewPool().clear();
                    recyclerViewAdapter.notifyDataSetChanged();

                }

                setRefresh(false);

            }
        });

    }

    private void loadMoreData() {

        String nextUrl = new String();
        int counter = mListWithHeaders.size()-1;
        while (nextUrl.equals("")) {

            if(mListWithHeaders.get(counter).getNextUrl() != null) {
                nextUrl = mListWithHeaders.get(counter).getNextUrl();
            }
            counter--;

        }

        if(networkUtils.isConnectingToInternet()) {
            new DataFetcher(this).execute(nextUrl);
            setRefresh(true);
        }
        else {
            Toast.makeText(getContext(), "Kein Internet!", Toast.LENGTH_LONG).show();
            setRefresh(false);
        }

    }

    private void addHeader(ArrayList<BlogPost> listWithHeaders, Date firstDate) {
        BlogPost headerBlogPost = new BlogPost(firstDate, BlogPost.TYPE_SECTION);
        listWithHeaders.add(headerBlogPost);
    }

    @Override
    public void onPauseFragment() {
    }

    @Override
    public void onResumeFragment() {
        this.getData();
    }

}
