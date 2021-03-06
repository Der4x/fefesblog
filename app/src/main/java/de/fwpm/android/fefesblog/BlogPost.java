package de.fwpm.android.fefesblog;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

/**
 * Created by alex on 19.01.18.
 */

@Entity
public class BlogPost implements Serializable {

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

    @ColumnInfo(name= "nexturl")
    private String nextUrl;

    public BlogPost() {

    }

    @Ignore
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

            Calendar now = Calendar.getInstance();
            Calendar postDate = Calendar.getInstance();
            postDate.setTime(date);
            if(postDate.get(Calendar.DAY_OF_MONTH) != now.get(Calendar.DAY_OF_MONTH)) {

                postDate.set(Calendar.HOUR_OF_DAY, 23);
                postDate.set(Calendar.MINUTE, 59);

            } else {

                postDate.set(Calendar.HOUR_OF_DAY, now.get(Calendar.HOUR_OF_DAY));
                postDate.set(Calendar.MINUTE, now.get(Calendar.MINUTE));

            }

            this.date = new Date(postDate.getTimeInMillis());

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

    public String getNextUrl() {
        return nextUrl;
    }

    public void setNextUrl(String nextUrl) {
        this.nextUrl = nextUrl;
    }
}
