package de.fwpm.android.fefesblog;

import android.os.AsyncTask;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.ArrayList;

import static de.fwpm.android.fefesblog.HtmlParser.parseHtml;

/**
 * Created by alex on 22.01.18.
 */

public class SearchDataFetcher extends AsyncTask<String, Void, ArrayList<BlogPost>> {

    private static final String TAG = "SearchDataFetcher";
    private static final String SEARCH_URL = "https://blog.fefe.de/?q=";

    private Document html;
    private SearchActivity container;

    public SearchDataFetcher(SearchActivity activity) {

        this.container = activity;

    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected ArrayList<BlogPost> doInBackground(String... params) {

        try {

            html = Jsoup.connect(SEARCH_URL + params[0]).timeout(1000 * 15).get();

            ArrayList<BlogPost> allPosts = parseHtml(html);

            return allPosts;

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(ArrayList<BlogPost> allPosts) {

        super.onPostExecute(allPosts);

        if(container!=null) {
            container.populateResult(allPosts);
            this.container = null;
        }

    }

}
