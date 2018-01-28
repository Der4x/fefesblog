package de.fwpm.android.fefesblog.data;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.ArrayList;

import de.fwpm.android.fefesblog.BlogPost;
import de.fwpm.android.fefesblog.R;
import de.fwpm.android.fefesblog.SyncReceiver;
import de.fwpm.android.fefesblog.database.AppDatabase;

import static de.fwpm.android.fefesblog.data.HtmlParser.parseHtml;
import static de.fwpm.android.fefesblog.fragments.SettingFragment.NOTIFICATION_DEFAULT;
import static de.fwpm.android.fefesblog.fragments.SettingFragment.NOTIFICATION_ENABLED;

/**
 * Created by alex on 19.01.18.
 */

public class BackgroundDataFetcher extends AsyncTask<String, Void, Boolean> {

    public static boolean areNotificationsAllowed = true;

    private static final String TAG = "SYNC";
    private static final String BASIC_URL = "https://blog.fefe.de/";

    private Document html;
    private Context mContext;

    private AppDatabase appDatabase;
    private int postsCounter;
    private int updateCounter;
    private StringBuilder newPosts;


    public BackgroundDataFetcher(Context context) {

        postsCounter = 0;
        updateCounter = 0;
        newPosts = new StringBuilder();
        mContext = context;

    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Boolean doInBackground(String... params) {

        try {

            appDatabase = AppDatabase.getInstance(mContext);
            html = Jsoup.connect(BASIC_URL).get();

            ArrayList<BlogPost> allPosts = parseHtml(html, false);

            for (BlogPost post : allPosts) {

                BlogPost oldEntry = appDatabase.blogPostDao().getPostByUrl(post.getUrl());

                if (oldEntry != null) {

                    post.setDate(oldEntry.getDate());
                    post.setBookmarked(oldEntry.isBookmarked());
                    post.setHasBeenRead(oldEntry.isHasBeenRead());

                    if (!oldEntry.getText().equals(post.getText())) {

                        post.setUpdate(true);
                        updateCounter++;
                    } else post.setUpdate(oldEntry.isUpdate());

                } else {
                    postsCounter++;
                    newPosts.append(post.getText().length() > 99 ? post.getText().substring(4, 100) : post.getText().substring(4));
                    newPosts.append("/;/");

                }

            }

            appDatabase.blogPostDao().insertList(allPosts);

        } catch (IOException e) {
            e.printStackTrace();
        }

        if (postsCounter != 0 || updateCounter != 0) {

            if (PreferenceManager.getDefaultSharedPreferences(mContext).getBoolean(NOTIFICATION_ENABLED, NOTIFICATION_DEFAULT)) {

                Intent intent = new Intent(mContext, SyncReceiver.class);
                intent.putExtra("Update", updateCounter);
                intent.putExtra("NewPosts", newPosts.toString());
                mContext.sendBroadcast(intent);
            } else {
                Log.d(TAG, "Notifications are not allowed");

            }

        }


        Log.d(TAG, "doInBackground: " + postsCounter + ", " + updateCounter);
        return true;
    }

    @Override
    protected void onPostExecute(Boolean success) {

        Log.d(TAG, "onPostExecute: ");
        super.onPostExecute(success);

    }

}

