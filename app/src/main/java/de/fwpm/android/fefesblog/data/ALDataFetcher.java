package de.fwpm.android.fefesblog.data;

import android.os.AsyncTask;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.ArrayList;

import de.fwpm.android.fefesblog.AlternativlosActivity;
import de.fwpm.android.fefesblog.BlogPost;
import de.fwpm.android.fefesblog.Episode;
import de.fwpm.android.fefesblog.database.AppDatabase;
import de.fwpm.android.fefesblog.fragments.NewPostsFragment;

import static de.fwpm.android.fefesblog.data.ALHtmlParser.parseALHtml;
import static de.fwpm.android.fefesblog.data.HtmlParser.parseHtml;

/**
 * Created by alex on 19.01.18.
 */

public class ALDataFetcher extends AsyncTask<String, Void, Void> {

    private static final String TAG = "ALDATAFETCHER";
    private static final String BASIC_URL = "https://alternativlos.org/";

    private Document html;
    private AlternativlosActivity container;

    private AppDatabase appDatabase;

    public ALDataFetcher(AlternativlosActivity activity) {

            this.container = activity;

    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Void doInBackground(String... params) {

        try {

            appDatabase = AppDatabase.getInstance(container);

            html = Jsoup.connect(BASIC_URL).get();

            ArrayList<Episode> allEpisodes = parseALHtml(html);

            appDatabase.episodeDao().insertEpisodeLisr(allEpisodes);

            return null;

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void vVoid) {

        super.onPostExecute(vVoid);

//        if(container!=null) {
//            container.populateResult();
//            this.container = null;
//        }

    }

}

