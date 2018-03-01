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

                    Elements elements = episodeDetails.select("h2");

                    for(Element element : elements) {

                        switch (element.text()) {

                            case "Thema":
                            case "Themen":
                                newEpisode.setTopic(element.nextElementSibling().toString());
                                break;
                            case "Linkliste":
                                Element nextElement = element.nextElementSibling();
                                Element nextElement2;

                                if(nextElement.toString().contains("<p>")) {

                                    nextElement2 = nextElement.nextElementSibling();
                                    nextElement = nextElement2;

                                }

                                Elements linkliste = nextElement.select("ul");

                                if(linkliste != null && linkliste.size() > 0) {

                                    Elements links = linkliste.get(0).children();
                                    ArrayList<String> linkList = new ArrayList<>();

                                    for (Element linkItem : links) {

                                        linkList.add(linkItem.toString().substring(4));

                                    }

                                    newEpisode.setLinkList(linkList);

                                }
                                break;
                            case "Buchtipps":
                                Elements bookElements = element.nextElementSibling().select("ul");

                                if(bookElements != null && bookElements.size() > 0) {

                                    Elements books = bookElements.get(0).children();
                                    ArrayList<String> bookList = new ArrayList<>();

                                    for (Element bookItem : books) {

                                        bookList.add(bookItem.toString().substring(4));

                                    }
                                    newEpisode.setBookList(bookList);
                                }
                                break;
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
