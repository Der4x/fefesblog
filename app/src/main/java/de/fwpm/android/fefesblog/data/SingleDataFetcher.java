package de.fwpm.android.fefesblog.data;

import android.app.Activity;
import android.os.AsyncTask;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.ArrayList;

import de.fwpm.android.fefesblog.BlogPost;
import de.fwpm.android.fefesblog.DetailsActivity;
import de.fwpm.android.fefesblog.database.AppDatabase;
import de.fwpm.android.fefesblog.fragments.NewPostsFragment;

import static de.fwpm.android.fefesblog.data.HtmlParser.parseHtml;

/**
 * Created by alex on 19.01.18.
 */

public class SingleDataFetcher extends AsyncTask<String, Void, Void> {

    private static final String TAG = "DATAFETCHER";
    private static final String BASIC_URL = "https://blog.fefe.de/";

    private Document html;
    private DetailsActivity activity;
    private BlogPost blogPost;


    public SingleDataFetcher(DetailsActivity activity) {

        this.activity = activity;

    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Void doInBackground(String... params) {

        try {

            html = Jsoup.connect(BASIC_URL + params[0]).get();

            ArrayList<BlogPost> allPosts = parseHtml(html, false);
            blogPost = allPosts.get(0);

            return null;

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void vVoid) {

        super.onPostExecute(vVoid);
        activity.changeBlogPost(blogPost);


    }

}

