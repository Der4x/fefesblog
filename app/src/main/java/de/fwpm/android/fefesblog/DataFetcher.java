package de.fwpm.android.fefesblog;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.ArrayList;

import de.fwpm.android.fefesblog.fragments.NewPostsFragment;

import static de.fwpm.android.fefesblog.HtmlParser.parseHtml;

/**
 * Created by alex on 19.01.18.
 */

public class DataFetcher extends AsyncTask<String, Void, ArrayList<BlogPost>> {

    private static final String TAG = "DATAFETCHER";
    private static final String BASIC_URL = "https://blog.fefe.de/";

    private Document html;
    private NewPostsFragment container;


    public DataFetcher(NewPostsFragment fragment) {

            this.container = fragment;

    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected ArrayList<BlogPost> doInBackground(String... params) {

        try {

            html = Jsoup.connect(BASIC_URL).get();
            return parseHtml(html);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(ArrayList<BlogPost> allPosts) {

        super.onPostExecute(allPosts);
        if(container!=null && container.getActivity()!=null) {
            container.populateResult(allPosts);
//            container.hideProgressBar();
            this.container = null;
        }

    }


}

