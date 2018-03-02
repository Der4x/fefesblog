package de.fwpm.android.fefesblog.data;

import android.os.AsyncTask;
import android.util.Log;

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

public class ALDataFetcher extends AsyncTask<String, Void, Void> {

    private static final String TAG = "ALDATAFETCHER";
    private static final String BASIC_URL = "https://alternativlos.org/";

    private Document html;
    private AlternativlosActivity container;
    private ArrayList<Episode> allEpisodes;

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
            
            int sizeOldList = appDatabase.episodeDao().getAllEpisodes().size();

            html = Jsoup.connect(BASIC_URL).get();

            allEpisodes = parseALHtml(html, sizeOldList);

            if(allEpisodes != null) appDatabase.episodeDao().insertEpisodeLisr(allEpisodes);
            else Log.d(TAG, "doInBackground: no new Episodes");
            
            return null;

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void vVoid) {

        super.onPostExecute(vVoid);

        if(container!=null && allEpisodes != null) {
            container.getData();
            this.container = null;
        }

    }

}

