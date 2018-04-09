package de.fwpm.android.fefesblog.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.io.Serializable;
import java.io.Serializable;
import java.util.ArrayList;

import de.fwpm.android.fefesblog.BlogPost;
import de.fwpm.android.fefesblog.DetailsActivity;
import de.fwpm.android.fefesblog.DetailsActivity;
import de.fwpm.android.fefesblog.R;
import de.fwpm.android.fefesblog.WebActivity;
import de.fwpm.android.fefesblog.adapter.BookmarkRecyclerViewAdapter;
import de.fwpm.android.fefesblog.database.AppDatabase;
import de.fwpm.android.fefesblog.utils.CustomTextView;

import static de.fwpm.android.fefesblog.DetailsActivity.INTENT_URL;
import static de.fwpm.android.fefesblog.MainActivity.fab;
import static de.fwpm.android.fefesblog.utils.SharePostUtil.sharePost;

/**
 * Created by alex on 20.01.18.
 */

public class BookmarkFragment extends Fragment implements FragmentLifecycle{

    private static String TAG = "BookmarkFragment";

    private RecyclerView mRecyclerView;
    private BookmarkRecyclerViewAdapter recyclerViewAdapter;
    private ArrayList<BlogPost> mList;

    private Context mContext;
    private Handler mHandler;
    private static RecyclerView.LayoutManager mLayoutManager;
    private static RecyclerView.SmoothScroller smoothScroller;
    private View view;

    public BookmarkFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view =  inflater.inflate(R.layout.fragment_bookmarks, container, false);

        mHandler = new Handler();
        mContext = getContext();

        initView();
        getData();

        return view;

    }

    @Override
    public void onResume() {

        super.onResume();
        this.getData();

    }

    @Override
    public void onPauseFragment() {

    }

    @Override
    public void onResumeFragment() {
        this.getData();
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

        mList = new ArrayList<>();

        recyclerViewAdapter = new BookmarkRecyclerViewAdapter(mContext,
                new BookmarkRecyclerViewAdapter.OnItemClickListener() {

                    @Override
                    public void onItemClick(int position, BlogPost blogPost) {

                        Intent intent;

                        if (CustomTextView.clickedLink != null) {
                            intent = new Intent(getActivity(), WebActivity.class);
                            intent.putExtra(INTENT_URL, CustomTextView.clickedLink);
                            CustomTextView.clickedLink = null;
                        } else {
                            intent = new Intent(getActivity(), DetailsActivity.class);
                            intent.putExtra(DetailsActivity.INTENT_BLOG_POST, (Serializable) blogPost);
                        }

                        startActivity(intent);

                    }

                    @Override
                    public void onShareClick(int position, BlogPost blogPost) {
                        sharePost(mContext, blogPost);
                    }
                },mList);

        mRecyclerView.setAdapter(recyclerViewAdapter);
    }

    private void getData() {

        new Thread(new Runnable() {
            @Override
            public void run() {

                if(mList==null){
                    mList = new ArrayList<>();
                }else mList.clear();

                mList.addAll((ArrayList<BlogPost>) AppDatabase.getInstance(getContext()).blogPostDao().getAllBookmarkedPosts());

                updateUI();

            }
        }).start();

    }

    private void updateUI() {

        mHandler.post(new Runnable() {
            @Override
            public void run() {

                recyclerViewAdapter.notifyDataSetChanged();
                if(mList.isEmpty()) showNoBookmarkScreen(true);
                else showNoBookmarkScreen(false);

            }
        });

    }

    public static void jump_To_Position(int position) {

        smoothScroller.setTargetPosition(position);
        mLayoutManager.startSmoothScroll(smoothScroller);

    }

    private void showNoBookmarkScreen(boolean show) {

        ((LinearLayout) view.findViewById(R.id.noBookmarkScreen)).setVisibility(show ? View.VISIBLE : View.GONE);

    }


}
