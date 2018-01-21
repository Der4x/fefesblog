package de.fwpm.android.fefesblog;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

/**
 * Created by alex on 19.01.18.
 */

public class BlogPost {

    public final static int TYPE_SECTION = 0;
    public final static int TYPE_DATA = 1;

    public int type;

    private Date date;

    private String text;

    private String htmlText;

    private HashMap<String, String> links;

    private String url;

    private boolean hasBeenRead;

    private boolean bookmarked;

    public BlogPost() {

    }

    public Date getDate() {
        return date;
    }

    public void setDate(String dateAsString) {

        try {
            this.date = new SimpleDateFormat("EEE MMM d yyyy", Locale.ENGLISH).parse(dateAsString);
        } catch (ParseException e) {
            e.printStackTrace();
            this.date = null;
        }
        ;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean isHasBeenRead() {
        return hasBeenRead;
    }

    public void setHasBeenRead(boolean hasBeenRead) {
        this.hasBeenRead = hasBeenRead;
    }

    public boolean isBookmarked() {
        return bookmarked;
    }

    public void setBookmarked(boolean bookmarked) {
        this.bookmarked = bookmarked;
    }

    public String getHtmlText() {
        return htmlText;
    }

    public void setHtmlText(String htmlText) {
        this.htmlText = htmlText;
    }

    public HashMap<String, String> getLinks() {
        return links;
    }

    public void setLinks(HashMap<String, String> links) {
        this.links = links;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
