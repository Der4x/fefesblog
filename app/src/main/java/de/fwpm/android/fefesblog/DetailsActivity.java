package de.fwpm.android.fefesblog;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Locale;

import de.fwpm.android.fefesblog.database.AppDatabase;

public class DetailsActivity extends AppCompatActivity {

    public static final String INTENT_BLOG_POST = "blogPost";
    BlogPost blogPost;
    private TextView postContent;
    private MenuItem bookmark_item;
    private MenuItem share_item;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        // Get BlogPost
        final Intent intent = getIntent();
        Serializable extra = intent.getSerializableExtra(INTENT_BLOG_POST);
        if (extra instanceof BlogPost){
            blogPost = (BlogPost) extra;

//            set detail title
            SimpleDateFormat dateFormat = new SimpleDateFormat("d. MMMM yyyy", Locale.GERMANY);
            setTitle(dateFormat.format(blogPost.getDate()));

        postContent = findViewById(R.id.blogPostText);
        postContent.setText(Html.fromHtml(blogPost.getHtmlText().split("</a>", 2)[1]));
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.menu_bookmark) {

            blogPost.setBookmarked(!blogPost.isBookmarked());
            setBookmarkIcon(blogPost.isBookmarked());

            new Thread(new Runnable() {
                @Override
                public void run() {
                    AppDatabase.getInstance(getBaseContext()).blogPostDao().updateBlogPost(blogPost);
                }
            }).start();
        return true;
        }else if (itemId == R.id.menu_share){
            String postText = blogPost.getText();
            String preView = postText.length() > 99 ?  postText.substring(4,100) : postText.substring(4);
            Intent share = new Intent();
            share.setAction(Intent.ACTION_SEND);
            share.putExtra(Intent.EXTRA_TEXT, preView + "...\n\n" +blogPost.getUrl());
            share.setType("text/plain");
            startActivity(Intent.createChooser(share, getResources().getText(R.string.share_to)));
            return true;
        }

            return super.onOptionsItemSelected(item);

    }

    private void setBookmarkIcon(boolean isBookmarked) {

        if(isBookmarked) bookmark_item.setIcon(R.drawable.ic_bookmark_white_24dp);
        else bookmark_item.setIcon(R.drawable.ic_bookmark_border_white_24dp);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.detail_menu, menu);

        bookmark_item = menu.findItem(R.id.menu_bookmark);
        bookmark_item.setIcon(blogPost.isBookmarked() ? R.drawable.ic_bookmark_white_24dp : R.drawable.ic_bookmark_border_white_24dp);

        share_item = menu.findItem(R.id.menu_share);
        share_item.setIcon(R.drawable.ic_share_white_24dp);

        return true;

    }
}
