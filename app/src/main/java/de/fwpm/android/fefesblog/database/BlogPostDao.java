package de.fwpm.android.fefesblog.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.ArrayList;
import java.util.List;

import de.fwpm.android.fefesblog.BlogPost;

/**
 * Created by alex on 21.01.18.
 */

@Dao
public interface BlogPostDao {

    @Query("SELECT * FROM blogpost ORDER BY date DESC")
    LiveData<List<BlogPost>> getAllPosts();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertList(ArrayList<BlogPost> listOfPosts);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertBlogPost(BlogPost blogPost);

    @Update
    void updateBlogPost(BlogPost blogPost);

    @Query("SELECT * FROM blogpost WHERE url LIKE :url")
    BlogPost getPostByUrl(String url);

    @Query("SELECT * FROM blogpost WHERE text LIKE :query")
    List<BlogPost> searchPosts(String query);

    @Query("SELECT * FROM blogpost WHERE bookmarked = 1 ORDER BY date DESC")
    LiveData<List<BlogPost>> getAllBookmarkedPosts();

}
