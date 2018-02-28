package de.fwpm.android.fefesblog.data;

import android.content.SharedPreferences;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import de.fwpm.android.fefesblog.BlogPost;
import de.fwpm.android.fefesblog.Episode;

/**
 * Created by alex on 19.01.18.
 */

public class ALHtmlParser {

    private static final String TAG = "ALHTMLPARSER";

    private SharedPreferences mPrefs;
    private SharedPreferences.Editor editor;

    public static ArrayList<Episode> parseALHtml(Document doc) {

        ArrayList<Episode> allEpisodes = new ArrayList<>();

        Elements listOfEpisodes = doc.select("body > ul").first().children();

        for (Element episode : listOfEpisodes) {

            Episode newEpisode = new Episode();

            Element link = episode.select("a[href]").first();

            if(link != null) {

                try {
                    newEpisode.setNr(Integer.parseInt(link.text().split(" vom ")[0].split(" ")[1]));
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }

                newEpisode.setDate(link.text().split(" vom ")[1]);
                newEpisode.setUrl(link.attr("abs:href"));
                newEpisode.setTitel(episode.text());

                try {

                    Document episodeDetails = Jsoup.connect(newEpisode.getUrl()).get();

                    Elements files = episodeDetails.select("source");

                    for(Element file : files) {

                        switch (file.attr("type")) {
                            case "audio/mp3":
                                newEpisode.setFile_mp3(!file.attr("src").contains("http") ? "http:" + file.attr("src") : file.attr("src"));
                                break;
                            case "audio/ogg":
                                newEpisode.setFile_ogg(!file.attr("src").contains("http") ? "http:" + file.attr("src") : file.attr("src"));
                        }
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }

                allEpisodes.add(newEpisode);

            }

        }

        return allEpisodes;
    }
}
