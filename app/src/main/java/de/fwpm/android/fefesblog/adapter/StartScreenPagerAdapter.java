package de.fwpm.android.fefesblog.adapter;

import android.content.Context;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import de.fwpm.android.fefesblog.R;
import de.fwpm.android.fefesblog.fragments.BookmarkFragment;
import de.fwpm.android.fefesblog.fragments.NewPostsFragment;

/**
 * Created by alex on 20.01.18.
 */

public class StartScreenPagerAdapter extends FragmentStatePagerAdapter {

    private static final int PAGE_COUNT = 2;

    NewPostsFragment newPostsFragment = NewPostsFragment.getInstance();

    BookmarkFragment bookmarkFragment = BookmarkFragment.getInstance();

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

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }
}
