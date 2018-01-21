package de.fwpm.android.fefesblog;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

/**
 * Created by alex on 19.01.18.
 */

@Entity
public class BlogPost {

    public final static int TYPE_SECTION = 0;
    public final static int TYPE_DATA = 1;

    @PrimaryKey
    @NonNull
    private String url;

    @ColumnInfo(name = "type")
    public int type;

    @ColumnInfo(name = "date")
    private Date date;

    @ColumnInfo(name = "text")
    private String text;

    @ColumnInfo(name = "html_ext")
    private String htmlText;

    @ColumnInfo(name = "links")
    private HashMap<String, String> links;

    @ColumnInfo(name = "has_been_read")
    private boolean hasBeenRead;

    @ColumnInfo(name = "bookmarked")
    private boolean bookmarked;

    @ColumnInfo(name= "update")
    private boolean update;

    public BlogPost() {

    }

    public BlogPost(Date date, int type) {

        this.date = date;
        this.type = type;

    }

    public Date getDate() {
        return date;
    }

    public void setDate(String dateAsString) {

        try {

            Date date = new SimpleDateFormat("EEE MMM d yyyy", Locale.ENGLISH).parse(dateAsString);
            date.setHours(new Date().getHours());
            date.setMinutes(new Date().getMinutes());
            this.date = date;
        } catch (ParseException e) {
            e.printStackTrace();
            this.date = null;
        }

    }

    public void setDate(Date date) {
        this.date = date;
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

    public boolean isUpdate() {
        return update;
    }

    public void setUpdate(boolean update) {
        this.update = update;
    }
}
