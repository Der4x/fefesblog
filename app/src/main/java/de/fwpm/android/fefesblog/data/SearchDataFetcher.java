package de.fwpm.android.fefesblog.data;

import android.os.AsyncTask;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.ArrayList;

import de.fwpm.android.fefesblog.BlogPost;
import de.fwpm.android.fefesblog.BlogPostViewModel;
import de.fwpm.android.fefesblog.SearchActivity;

import static de.fwpm.android.fefesblog.data.HtmlParser.parseHtml;

/**
 * Created by alex on 22.01.18.
 */

public class SearchDataFetcher extends AsyncTask<String, Void, ArrayList<BlogPost>> {

    private static final String SEARCH_URL = "https://blog.fefe.de/?q=";

    private Document html;
    private BlogPostViewModel viewModel;

    public SearchDataFetcher(BlogPostViewModel viewModel) {

        this.viewModel = viewModel;

    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected ArrayList<BlogPost> doInBackground(String... params) {

        try {

            html = Jsoup.connect(SEARCH_URL + params[0]).timeout(1000 * 15).get();

            ArrayList<BlogPost> allPosts = parseHtml(html, true);

            return allPosts;

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(ArrayList<BlogPost> allPosts) {

        super.onPostExecute(allPosts);
        viewModel.searchList.setValue(allPosts);

    }

}
