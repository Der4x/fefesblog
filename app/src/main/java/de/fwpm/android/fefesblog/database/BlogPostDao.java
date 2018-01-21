package de.fwpm.android.fefesblog.database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.ArrayList;
import java.util.List;

import de.fwpm.android.fefesblog.BlogPost;

/**
 * Created by alex on 21.01.18.
 */

@Dao
public interface BlogPostDao {

    @Query("SELECT * FROM blogpost ORDER BY date DESC")
    List<BlogPost> getAllPosts();

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    public void insertList(ArrayList<BlogPost> listOfPosts);


}
