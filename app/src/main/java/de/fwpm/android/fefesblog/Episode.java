package de.fwpm.android.fefesblog;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * Created by alex on 28.02.18.
 */
@Entity
public class Episode {

    @PrimaryKey
    @NonNull
    private int nr;

    @ColumnInfo(name = "url")
    private String url;

    @ColumnInfo(name = "date")
    private Date date;

    @ColumnInfo(name = "titel")
    private String titel;

    @ColumnInfo(name = "file_mp3")
    private String file_mp3;

    @ColumnInfo(name = "file_ogg")
    private String file_ogg;

    @ColumnInfo(name = "topic")
    private String topic;

    @ColumnInfo(name = "linkList")
    private ArrayList<String> linkList;

    @ColumnInfo(name = "bookList")
    private ArrayList<String> bookList;

    public Episode() {

    }

    public int getNr() {
        return nr;
    }

    public void setNr(int nr) {
        this.nr = nr;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTitel() {
        return titel;
    }

    public void setTitel(String titel) {
        this.titel = titel;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(String dateAsString) {

        try {

            Date date = new SimpleDateFormat("dd.MM.yyyy", Locale.ENGLISH).parse(dateAsString);
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

    public String getFile_mp3() {
        return file_mp3;
    }

    public void setFile_mp3(String file_mp3) {
        this.file_mp3 = file_mp3;
    }

    public String getFile_ogg() {
        return file_ogg;
    }

    public void setFile_ogg(String file_ogg) {
        this.file_ogg = file_ogg;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public ArrayList<String> getLinkList() {
        return linkList;
    }

    public void setLinkList(ArrayList<String> linkList) {
        this.linkList = linkList;
    }

    public ArrayList<String> getBookList() {
        return bookList;
    }

    public void setBookList(ArrayList<String> bookList) {
        this.bookList = bookList;
    }

}
