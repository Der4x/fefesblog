package de.fwpm.android.fefesblog.adapter;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import de.fwpm.android.fefesblog.R;
import de.fwpm.android.fefesblog.fragments.BookmarkFragment;
import de.fwpm.android.fefesblog.fragments.NewPostsFragment;

/**
 * Created by alex on 20.01.18.
 */

public class StartScreenPagerAdapter extends FragmentPagerAdapter {

    private static final int PAGE_COUNT = 2;

    NewPostsFragment newPostsFragment = new NewPostsFragment();

    BookmarkFragment bookmarkFragment = new BookmarkFragment();

    Context mContext;

    public StartScreenPagerAdapter(Context context, FragmentManager fm) {
        super(fm);
        mContext = context;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return newPostsFragment;
            case 1:
                return bookmarkFragment;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return PAGE_COUNT;
    }



    @Override
    public CharSequence getPageTitle(final int position) {
        switch (position) {
            case 0:
                return mContext.getString(R.string.newposts);
            case 1:
                return mContext.getString(R.string.bookmarks);
            default:
                return "";
        }
    }
}
