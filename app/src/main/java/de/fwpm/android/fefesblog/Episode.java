package de.fwpm.android.fefesblog;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by alex on 28.02.18.
 */

public class Episode {

    private int nr;
    private String url;
    private Date date;
    private String titel;
    private String file_mp3;
    private String file_ogg;

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
}
