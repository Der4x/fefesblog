package de.fwpm.android.fefesblog.fragments;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.util.ArrayList;

import de.fwpm.android.fefesblog.BlogPost;
import de.fwpm.android.fefesblog.R;
import de.fwpm.android.fefesblog.adapter.BookmarkRecyclerViewAdapter;
import de.fwpm.android.fefesblog.database.AppDatabase;

/**
 * Created by alex on 20.01.18.
 */

public class BookmarkFragment extends Fragment {

    public static final String ARG_ITEM_ID = "item_id";
    private LinearLayout mLinearLayout;
    private static RecyclerView mRecyclerView;
    private BookmarkRecyclerViewAdapter recyclerViewAdapter;
    private ArrayList<BlogPost> mList;
    private static String TAG = "BookmarkFragment";
    private Context mContext;
    private Handler mHandler;
    private AppDatabase appDatabase;
    private RecyclerView.LayoutManager mLayoutManager;
    View view;



//    @Override
//    public void onResume() {
//        updateUI();
//        super.onResume();
//    }

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

                if(appDatabase.blogPostDao().getAllBookmarkedPosts().size()>0){
                    mList = (ArrayList<BlogPost>) appDatabase.blogPostDao().getAllBookmarkedPosts();
                    Log.d("mList Bookmarks", mList.get(0).getText() );

                    updateUI();
                }
            }
        }).start();

    }

    private void updateUI() {

        mHandler.post(new Runnable() {
            @Override
            public void run() {

                if(recyclerViewAdapter == null) {

                    recyclerViewAdapter = new BookmarkRecyclerViewAdapter(mContext,
                            new BookmarkRecyclerViewAdapter.OnItemClickListener() {
                                @Override
                                public void onItemClick(int position) {
                                    Log.d(TAG, "onItemClick" + position);
                                    mList.get(position).setUpdate(false);
                                    recyclerViewAdapter.notifyDataSetChanged();
                                }
                            },mList);

                    mRecyclerView.setAdapter(recyclerViewAdapter);

                } else {

                    recyclerViewAdapter.notifyDataSetChanged();

                }
            }
        });

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view =  inflater.inflate(R.layout.fragment_bookmarks, container, false);

        mHandler = new Handler();
        appDatabase = AppDatabase.getInstance(getContext());

        mContext = getContext();

        mLinearLayout = (LinearLayout) view.findViewById(R.id.bookmarkFragment);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.bookmark_recyclerview);
        mRecyclerView.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);

        getData();


        return view;

    }
}
