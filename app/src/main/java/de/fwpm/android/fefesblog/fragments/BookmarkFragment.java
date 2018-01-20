package de.fwpm.android.fefesblog.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import de.fwpm.android.fefesblog.R;

/**
 * Created by alex on 20.01.18.
 */

public class BookmarkFragment extends Fragment {

    public static final String ARG_ITEM_ID = "item_id";

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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view =  inflater.inflate(R.layout.fragment_bookmarks, container, false);

//        mContext = getContext();
//        mHandler = new Handler();
//        mProgressBar = (ProgressBar) view.findViewById(R.id.progess_bar);
//
//        List mockData = createMockData();
//
//        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerview);
//        mRecyclerView.setHasFixedSize(true);
//
//        mLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
//        mRecyclerView.setLayoutManager(mLayoutManager);
//
//        mLecturerViewAdapter = new LecturerViewAdapter(mContext, new LecturerViewAdapter.OnItemClickListener() {
//            @Override
//            public void onItemClick(final int position) {
//                Log.d(TAG, "onItemClick: "+position);
//            }
//        }, mockData);
//        mRecyclerView.setAdapter(mLecturerViewAdapter);

        return view;

    }

}
