package de.fwpm.android.fefesblog;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import de.fwpm.android.fefesblog.database.AppDatabase;
import de.fwpm.android.fefesblog.fragments.NewPostsFragment;

import static de.fwpm.android.fefesblog.HtmlParser.parseHtml;

/**
 * Created by alex on 19.01.18.
 */

public class DataFetcher extends AsyncTask<String, Void, Void> {

    private static final String TAG = "DATAFETCHER";
    private static final String BASIC_URL = "https://blog.fefe.de/";

    private Document html;
    private NewPostsFragment container;

    private AppDatabase appDatabase;


    public DataFetcher(NewPostsFragment fragment) {

            this.container = fragment;

    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Void doInBackground(String... params) {

        try {

            appDatabase = AppDatabase.getInstance(container.getContext());
            html = Jsoup.connect(BASIC_URL).get();

            ArrayList<BlogPost> allPosts = parseHtml(html);

            for(BlogPost post : allPosts) {

                BlogPost oldEntry = appDatabase.blogPostDao().getPostByUrl(post.getUrl());

                if(oldEntry != null) {

                    post.setDate(oldEntry.getDate());
                    post.setBookmarked(oldEntry.isBookmarked());

                    if(!oldEntry.getText().equals(post.getText())) post.setUpdate(true);
                    else post.setUpdate(oldEntry.isUpdate());

                }

            }

            appDatabase.blogPostDao().insertList(allPosts);

            return null;

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void vVoid) {

        super.onPostExecute(vVoid);

        if(container!=null && container.getActivity()!=null) {
            container.populateResult();
            this.container = null;
        }

    }


}

