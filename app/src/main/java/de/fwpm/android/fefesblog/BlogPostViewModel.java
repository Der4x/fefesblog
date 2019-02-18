package de.fwpm.android.fefesblog;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.support.annotation.NonNull;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.fwpm.android.fefesblog.data.DataFetcher;
import de.fwpm.android.fefesblog.data.SearchDataFetcher;
import de.fwpm.android.fefesblog.database.AppDatabase;
import de.fwpm.android.fefesblog.fragments.NewPostsFragment;

public class BlogPostViewModel extends AndroidViewModel {

    private AppDatabase appDatabase;
    private LiveData<List<BlogPost>> newPostList;
    private LiveData<List<BlogPost>> bookmarkList;
    public MutableLiveData<List<BlogPost>> searchList;

    public BlogPostViewModel(@NonNull Application application) {
        super(application);

        appDatabase = AppDatabase.getInstance(this.getApplication());
        newPostList = appDatabase.blogPostDao().getAllPosts();
        bookmarkList = appDatabase.blogPostDao().getAllBookmarkedPosts();
        searchList = new MutableLiveData<>();

    }

    public LiveData<List<BlogPost>> getAllPosts() {
        return newPostList;
    }

    public LiveData<List<BlogPost>> getBookmarkedPosts() {
        return bookmarkList;
    }

    public LiveData<List<BlogPost>> getSearchList() {
        return searchList;
    }

    public void updatePost(final BlogPost post) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                appDatabase.blogPostDao().updateBlogPost(post);
            }
        }).start();

    }

    public void insertPost(final BlogPost blogPost) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                appDatabase.blogPostDao().insertBlogPost(blogPost);
            }
        }).start();

    }

    public void syncPosts(NewPostsFragment container) {
        new DataFetcher(container).execute();
    }

    public void loadOlderPosts(NewPostsFragment container) {

        List<BlogPost> data = newPostList.getValue();
        String nextUrl = "";
        int counter;

        if (data != null && data.size() > 1) {

            counter = data.size() - 1;

            while (nextUrl.equals("")) {

                try {

                    String nextMonthurl = data.get(counter).getNextUrl();

                    if (data.get(counter).getNextUrl() != null) {

                        SimpleDateFormat fmt = new SimpleDateFormat("yyyyMM", Locale.GERMANY);

                        Date before = fmt.parse(nextMonthurl.substring(nextMonthurl.length() - 6));

                        Date lastdate = data.get(counter).getDate();

                        if (before.after((lastdate))) {

                            Calendar nextMonth = Calendar.getInstance();
                            nextMonth.setTimeInMillis(before.getTime());
                            nextMonth.add(Calendar.MONTH, -1);
                            nextUrl = "https://blog.fefe.de//?mon=" + fmt.format(nextMonth.getTime());

                        } else nextUrl = data.get(counter).getNextUrl();

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                counter--;

            }

        }

        new DataFetcher(container).execute(nextUrl);

    }

    public void searchPosts(String query) {

        new SearchDataFetcher(this).execute(query);

    }

    public void searchPostsInDatabase(final String query) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                searchList.postValue(appDatabase.blogPostDao().searchPosts("%" + query + "%"));
            }
        }).start();

    }


}
