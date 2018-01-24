package de.fwpm.android.fefesblog.data;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.ArrayList;

import de.fwpm.android.fefesblog.BlogPost;
import de.fwpm.android.fefesblog.SyncReceiver;
import de.fwpm.android.fefesblog.database.AppDatabase;

import static de.fwpm.android.fefesblog.data.HtmlParser.parseHtml;

/**
 * Created by alex on 19.01.18.
 */

public class BackgroundDataFetcher extends AsyncTask<String, Void, Boolean> {

    private static final String TAG = "SYNC";
    private static final String BASIC_URL = "https://blog.fefe.de/";

    private Document html;
    private Context mContext;

    private AppDatabase appDatabase;
    private int postsCounter;
    private int updateCounter;


    public BackgroundDataFetcher(Context context) {

            postsCounter = 0;
            updateCounter = 0;
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

            ArrayList<BlogPost> allPosts = parseHtml(html);

            for(BlogPost post : allPosts) {

                BlogPost oldEntry = appDatabase.blogPostDao().getPostByUrl(post.getUrl());

                if(oldEntry != null) {

                    post.setDate(oldEntry.getDate());
                    post.setBookmarked(oldEntry.isBookmarked());

                    if(!oldEntry.getText().equals(post.getText())) {

                        post.setUpdate(true);
                        updateCounter++;
                    }
                    else post.setUpdate(oldEntry.isUpdate());

                } else postsCounter++;


            }

            appDatabase.blogPostDao().insertList(allPosts);

        } catch (IOException e) {
            e.printStackTrace();
        }

        if(postsCounter != 0 || updateCounter != 0)  {

            Intent intent = new Intent(mContext, SyncReceiver.class);
            intent.putExtra("Update", updateCounter);
            intent.putExtra("New",postsCounter);
            mContext.sendBroadcast(intent);

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

