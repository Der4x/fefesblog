package de.fwpm.android.fefesblog.database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.ArrayList;
import java.util.List;

import de.fwpm.android.fefesblog.BlogPost;
import de.fwpm.android.fefesblog.Episode;

/**
 * Created by alex on 01.03.18.
 */

@Dao
public interface EpisodeDao {

    @Query("SELECT * FROM episode ORDER BY nr DESC")
    List<Episode> getAllEpisodes();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertEpisodeLisr(ArrayList<Episode> listOfPosts);

    @Query("SELECT * FROM episode WHERE nr LIKE :nr")
    Episode getEpisodeByNr(int nr);


}
