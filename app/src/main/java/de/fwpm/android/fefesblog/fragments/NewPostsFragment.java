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
import java.util.Calendar;
import java.util.Date;

import de.fwpm.android.fefesblog.BlogPost;
import de.fwpm.android.fefesblog.DetailsActivity;
import de.fwpm.android.fefesblog.WebActivity;
import de.fwpm.android.fefesblog.data.DataFetcher;
import de.fwpm.android.fefesblog.utils.CustomTextView;
import de.fwpm.android.fefesblog.utils.NetworkUtils;
import de.fwpm.android.fefesblog.utils.PinnedHeaderItemDecoration;
import de.fwpm.android.fefesblog.R;
import de.fwpm.android.fefesblog.adapter.NewPostsRecyclerViewAdapter;
import de.fwpm.android.fefesblog.database.AppDatabase;

import static de.fwpm.android.fefesblog.DetailsActivity.INTENT_URL;
import static de.fwpm.android.fefesblog.MainActivity.FIRST_START;
import static de.fwpm.android.fefesblog.MainActivity.fab;
import static de.fwpm.android.fefesblog.utils.SharePostUtil.sharePost;

/**
 * Created by alex on 20.01.18.
 */

public class NewPostsFragment extends Fragment implements FragmentLifecycle {

    private static final String TAG = "NewsPostFragment";
    private static final long FIVE_MINUTES = 5 * 60 * 1000;

    private RecyclerView mRecyclerView;
    private NewPostsRecyclerViewAdapter recyclerViewAdapter;
    private SwipeRefreshLayout mNewPostSwipeRefresh;
    private static RecyclerView.LayoutManager mLayoutManager;
    private static RecyclerView.SmoothScroller smoothScroller;

    private ArrayList<BlogPost> mListWithHeaders;
    private Handler mHandler;

    private Context context;
    private NetworkUtils networkUtils;

    long lastSyncTimestamp;

    View view;

    public NewPostsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_newposts, container, false);

        mHandler = new Handler();
        mListWithHeaders = new ArrayList<>();
        context = getContext();
        networkUtils = new NetworkUtils(context);

        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        boolean firstStart = mPrefs.getBoolean(FIRST_START, true);

        initView();

        if (firstStart) mPrefs.edit().putBoolean(FIRST_START, false).commit();
        else this.getData();

        return view;

    }

    @Override
    public void onResume() {

        super.onResume();
        if (lastSyncTimestamp == 0 || Math.abs(System.currentTimeMillis() - lastSyncTimestamp) > FIVE_MINUTES)
            startSync();
        else this.getData();

    }

    @Override
    public void onPauseFragment() {
    }

    @Override
    public void onResumeFragment() {
        this.getData();
    }

    private void startSync() {

        lastSyncTimestamp = System.currentTimeMillis();

        if (networkUtils.isConnectingToInternet()) {
            new DataFetcher(this).execute();
            setRefresh(true);
        } else {

            networkUtils.noNetwork(mNewPostSwipeRefresh);
            setRefresh(false);
        }

    }

    private void getData() {

        new Thread(new Runnable() {
            @Override
            public void run() {

                ArrayList<BlogPost> data = (ArrayList<BlogPost>) AppDatabase.getInstance(context).blogPostDao().getAllPosts();

                if (mListWithHeaders == null) mListWithHeaders = new ArrayList<>();
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

        recyclerViewAdapter
                = new NewPostsRecyclerViewAdapter(getContext(),
                new NewPostsRecyclerViewAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(int position, final BlogPost blogPost) {
                        if (blogPost.isUpdate() || !blogPost.isHasBeenRead()) {

                            blogPost.setUpdate(false);
                            blogPost.setHasBeenRead(true);
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    AppDatabase.getInstance(context).blogPostDao().updateBlogPost(blogPost);
                                }
                            }).start();

                        }

                        Intent intent;

                        if (CustomTextView.clickedLink != null) {
                            intent = new Intent(getActivity(), WebActivity.class);
                            intent.putExtra(INTENT_URL, CustomTextView.clickedLink);
                            CustomTextView.clickedLink = null;
                            if (networkUtils.isConnectingToInternet()) startActivity(intent);
                            else networkUtils.noNetwork(mNewPostSwipeRefresh);
                        } else {
                            intent = new Intent(getActivity(), DetailsActivity.class);
                            intent.putExtra(DetailsActivity.INTENT_BLOG_POST, (Serializable) blogPost);
                            startActivity(intent);
                        }

                    }

                    @Override
                    public void onShareClick(int position, BlogPost blogPost) {

                        sharePost(context, blogPost);

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

        smoothScroller = new LinearSmoothScroller(context) {
            @Override
            protected int getVerticalSnapPreference() {
                return LinearSmoothScroller.SNAP_TO_START;
            }
        };

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0 && fab.isShown())
                    fab.hide();
                else if (dy < 0 && !fab.isShown()) fab.show();
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

                recyclerViewAdapter.notifyDataSetChanged();
                setRefresh(false);
            }
        });
    }

    private void loadMoreData() {

        String nextUrl = new String();
        int counter = mListWithHeaders.size() - 1;
        while (nextUrl.equals("")) {

            String nextMonthurl = mListWithHeaders.get(counter).getNextUrl();

            try {
                if (mListWithHeaders.get(counter).getNextUrl() != null) {

                    SimpleDateFormat fmt = new SimpleDateFormat("yyyyMM");

                    Date before = fmt.parse(nextMonthurl.substring(nextMonthurl.length() - 6));

                    Date lastdate = mListWithHeaders.get(counter).getDate();

                    if (before.after((lastdate))) {

                        Calendar nextMonth = Calendar.getInstance();
                        nextMonth.setTimeInMillis(before.getTime());
                        nextMonth.add(Calendar.MONTH, -1);
                        nextUrl = "https://blog.fefe.de//?mon=" + fmt.format(nextMonth.getTime());

                    } else nextUrl = mListWithHeaders.get(counter).getNextUrl();

                }
            } catch (Exception e) {
                Toast.makeText(context, "Fehler 101", Toast.LENGTH_SHORT).show();
            }
            counter--;

        }

        if (networkUtils.isConnectingToInternet()) {
            new DataFetcher(this).execute(nextUrl);
            setRefresh(true);
        } else {
            networkUtils.noNetwork(mNewPostSwipeRefresh);
            setRefresh(false);
        }

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
