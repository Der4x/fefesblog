package de.fwpm.android.fefesblog.database;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import android.arch.persistence.room.migration.Migration;
import android.content.Context;

import de.fwpm.android.fefesblog.BlogPost;
import de.fwpm.android.fefesblog.Episode;
import de.fwpm.android.fefesblog.MainActivity;

/**
 * Created by alex on 21.01.18.
 */

@Database(entities = {BlogPost.class, Episode.class}, version = 2)
@TypeConverters({Converter.class})
public abstract class AppDatabase extends RoomDatabase {

    private static AppDatabase instance;

    public static AppDatabase getInstance(Context context) {

        if (instance == null) {

            instance = Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, "blogpost-db")
                    .addMigrations(MIGRATION_1_2)
                    .build();

        }
        return instance;

    }

    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {

            database.execSQL(
                    "CREATE TABLE episode (nr INTEGER NOT NULL,"
                            + "url TEXT,"
                            + "date INTEGER,"
                            + "titel TEXT,"
                            + "file_mp3 TEXT,"
                            + "file_ogg TEXT,"
                            + "topic TEXT,"
                            + "linkList TEXT,"
                            + "bookList TEXT,"
                            + "PRIMARY KEY(nr))");
        }
    };

    public abstract BlogPostDao blogPostDao();

    public abstract EpisodeDao episodeDao();

}
