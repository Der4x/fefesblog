package de.fwpm.android.fefesblog.database;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import android.content.Context;

import de.fwpm.android.fefesblog.BlogPost;

/**
 * Created by alex on 21.01.18.
 */

@Database(entities = {BlogPost.class}, version = 1, exportSchema = false)
@TypeConverters({Converter.class})
public abstract class AppDatabase extends RoomDatabase {

    private static AppDatabase instance;

    public static AppDatabase getInstance(Context context) {

        if(instance == null) {

            instance = Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, "blogpost-db").build();

        }
        return instance;

    }

    public abstract BlogPostDao blogPostDao();

}
