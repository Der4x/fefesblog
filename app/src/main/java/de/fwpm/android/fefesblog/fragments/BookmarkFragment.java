package de.fwpm.android.fefesblog.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
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
import de.fwpm.android.fefesblog.adapter.BookmarkRecyclerViewAdapter;
import de.fwpm.android.fefesblog.database.AppDatabase;
import de.fwpm.android.fefesblog.utils.CustomTextView;

import static de.fwpm.android.fefesblog.MainActivity.fab;

/**
 * Created by alex on 20.01.18.
 */

public class BookmarkFragment extends Fragment implements FragmentLifecycle{

    public static final String ARG_ITEM_ID = "item_id";
    private RecyclerView mRecyclerView;
    private BookmarkRecyclerViewAdapter recyclerViewAdapter;
    private ArrayList<BlogPost> mList;
    private static String TAG = "BookmarkFragment";
    private Context mContext;
    private Handler mHandler;
    private RecyclerView.LayoutManager mLayoutManager;
    View view;


    public BookmarkFragment() {
        // Required empty public constructor
    }

    public static BookmarkFragment newInstance(int position) {
        Bundle args = new Bundle();
        args.putInt(ARG_ITEM_ID, position);
        BookmarkFragment fragment = new BookmarkFragment();
        fragment.setArguments(args);
        return fragment;
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

            }
        });

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view =  inflater.inflate(R.layout.fragment_bookmarks, container, false);

        mHandler = new Handler();

        mContext = getContext();

        mRecyclerView = (RecyclerView) view.findViewById(R.id.bookmark_recyclerview);
        mRecyclerView.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);

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
                        Log.d(TAG, "onItemClick" + position);
                        mList.get(position).setUpdate(false);
                        recyclerViewAdapter.notifyDataSetChanged();
                        //go to detail view
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
                        share.putExtra(Intent.EXTRA_TEXT, preView + "...\n\n" + blogPost.getUrl());
                        share.setType("text/plain");
                        startActivity(Intent.createChooser(share, getResources().getText(R.string.share_to)));
                    }
                },mList);

        mRecyclerView.setAdapter(recyclerViewAdapter);

        getData();


        return view;

    }

    @Override
    public void onPauseFragment() {

    }

    @Override
    public void onResumeFragment() {
        Log.d(TAG, "onResumeFragment: " + this);
        this.getData();
    }
}
