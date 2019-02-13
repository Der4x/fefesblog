package de.fwpm.android.fefesblog.database;

import android.arch.persistence.room.TypeConverter;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by alex on 21.01.18.
 */


public class Converter {

    @TypeConverter
    public static Date fromTimestamp(Long value) {
        return value == null ? null : new Date(value);
    }

    @TypeConverter
    public static Long dateToTimestamp(Date date) {
        return date == null ? null : date.getTime();
    }

    @TypeConverter
    public static String fromMapToString(HashMap<String, String> map) {

        StringBuilder links = new StringBuilder();

        if(map != null) {

            for(Map.Entry<String,String> entry : map.entrySet()) {

                links.append(entry.getKey());
                links.append("/;/");
                links.append(entry.getValue());
                links.append("/;/");

            }

            return links.toString();

        }else return "";

    }

    @TypeConverter
    public static HashMap<String, String> fromStringToMap(String string) {

        String[] keysAndValues = (string != null) ? string.split("/;/") : new String[1];
        HashMap<String, String> links = new HashMap<>();

        if(keysAndValues.length > 1) {

            for (int i = 0; i < keysAndValues.length; i+=2) {

                if(keysAndValues.length > i+1)
                    links.put(keysAndValues[i], keysAndValues[i+1]);

            }

            return links;

        } else return null;

    }


}
