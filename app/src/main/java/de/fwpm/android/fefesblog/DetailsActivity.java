package de.fwpm.android.fefesblog;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.Layout;
import android.text.Selection;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.method.Touch;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.WebBackForwardList;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

import de.fwpm.android.fefesblog.data.SingleDataFetcher;
import de.fwpm.android.fefesblog.database.AppDatabase;
import de.fwpm.android.fefesblog.utils.CustomMovementMethod;
import de.fwpm.android.fefesblog.utils.NetworkUtils;

import static de.fwpm.android.fefesblog.utils.CustomQuoteSpan.replaceQuoteSpans;
import static de.fwpm.android.fefesblog.utils.CustomTextView.handleClickedLink;
import static de.fwpm.android.fefesblog.utils.SharePostUtil.shareLink;
import static de.fwpm.android.fefesblog.utils.SharePostUtil.sharePost;

public class DetailsActivity extends AppCompatActivity {

    private static final String TAG = "DetailActivity";
    public static final String INTENT_BLOG_POST = "blogPost";
    public static final String INTENT_URL = "CLICKED_LINK";

    private BlogPost blogPost;
    private String clickedUrl;
    private TextView postContent;
    private MenuItem bookmark_item;
    private MenuItem share_item;

    private CoordinatorLayout mContainer;
    private ProgressBar mProgressBar;
    private boolean newPostLoaded;
    private boolean darkTheme;

    private ArrayList<BlogPost> historyList;
    private NetworkUtils networkUtils;

    private Context context;

    Animation animFadein;
    Animation animFadeout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (App.getInstance().isNightModeEnabled()) {
            setTheme(R.style.MainActivityThemeDark);
            darkTheme = true;

        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        context = this;
        newPostLoaded = false;
        historyList = new ArrayList<>();
        networkUtils = new NetworkUtils(this);

        initToolbar();

        initView();

        final Intent intent = getIntent();


        if(intent.getData() != null) {
            //Deep Link
            clickedUrl = intent.getDataString();
            loadPostUrl(clickedUrl);
        }
        else {

            clickedUrl = intent.getStringExtra(INTENT_URL);
            Serializable extra = intent.getSerializableExtra(INTENT_BLOG_POST);
            if (extra instanceof BlogPost) {

                blogPost = (BlogPost) extra;
                if(clickedUrl == null) setContent();
                else loadPostUrl(clickedUrl);

            }
        }
    }

    @Override
    public void onBackPressed() {

        if (historyList.size() > 0) {

            changeBlogPost(historyList.get(historyList.size() - 1));
            historyList.remove(historyList.size() - 1);
            historyList.remove(historyList.size() - 1);

        } else super.onBackPressed();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:

                this.finish();
                break;

            case R.id.menu_bookmark:
                blogPost.setBookmarked(!blogPost.isBookmarked());
                setBookmarkIcon(blogPost.isBookmarked());

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (newPostLoaded)
                            AppDatabase.getInstance(getBaseContext()).blogPostDao().insertBlogPost(blogPost);
                        else
                            AppDatabase.getInstance(getBaseContext()).blogPostDao().updateBlogPost(blogPost);
                    }
                }).start();
                break;
            case R.id.menu_share:
                sharePost(context, blogPost);
                break;
        }
        return true;

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.detail_menu, menu);

        bookmark_item = menu.findItem(R.id.menu_bookmark);
        bookmark_item.setIcon(blogPost.isBookmarked() ? R.drawable.ic_stat_bookmark : R.drawable.ic_stat_bookmark_border);

        bookmark_item.getIcon().setColorFilter(getResources().getColor(darkTheme ? R.color.primaryTextColorDark : R.color.secondaryTextColorLight), PorterDuff.Mode.SRC_IN);

        share_item = menu.findItem(R.id.menu_share);
        share_item.getIcon().setColorFilter(getResources().getColor(darkTheme ? R.color.primaryTextColorDark : R.color.secondaryTextColorLight), PorterDuff.Mode.SRC_IN);
//        share_item.setIcon(R.drawable.ic_share_white_24dp);

        return true;

    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.getNavigationIcon().setColorFilter(getResources().getColor(darkTheme ? R.color.primaryTextColorDark : R.color.secondaryTextColorLight), PorterDuff.Mode.SRC_ATOP);

    }

    private void initView() {

        mContainer = (CoordinatorLayout) findViewById(R.id.container);
        mProgressBar = (ProgressBar) findViewById(R.id.progess_bar);
        postContent = (TextView) findViewById(R.id.blogPostText);

        Animation.AnimationListener animationListener = new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

                if (animation == animFadeout) {

                    if (postContent.getVisibility() == View.INVISIBLE) {
                        setContent();
                        postContent.setVisibility(View.VISIBLE);
                        postContent.startAnimation(animFadein);
                    }

                }

            }

            @Override
            public void onAnimationEnd(Animation animation) {

                if (animation == animFadein)
                    ((ScrollView) findViewById(R.id.scrollView)).scrollTo(0,0);

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        };

        animFadein = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in);
        animFadein.setAnimationListener(animationListener);
        animFadeout = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_out);
        animFadeout.setAnimationListener(animationListener);

    }

    private void setContent() {

        getSupportActionBar().setTitle(new SimpleDateFormat("d. MMMM yyyy", Locale.GERMANY).format(blogPost.getDate()));
        getSupportActionBar().setSubtitle("");
        setTextViewHTML(postContent, blogPost.getHtmlText().split("</a>", 2)[1]);

    }

    public void loadPostUrl(final String url) {

        if (url.startsWith("/?ts=")) {

            getPostFromUrl(getString(R.string.basic_url) + url);

        } else if (url.startsWith("https://blog.fefe.de/?ts=")) {

            getPostFromUrl(url);

        } else {

            if(!handleClickedLink(this, blogPost, url))
                networkUtils.noNetwork(mContainer);

        }
    }

    private void getPostFromUrl(final String url) {

        new Thread(new Runnable() {
            @Override
            public void run() {

                BlogPost linkPost = AppDatabase.getInstance(getBaseContext()).blogPostDao().getPostByUrl(url);

                if (linkPost != null) changeBlogPost(linkPost);
                else if (networkUtils.isConnectingToInternet()) {

                    new SingleDataFetcher(DetailsActivity.this).execute(url);
                    newPostLoaded = true;
                    showProgressBar(true);

                } else networkUtils.noNetwork(mContainer);

            }
        }).start();
    }

    public void changeBlogPost(final BlogPost _blogPost) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                postContent.setVisibility(View.INVISIBLE);
                postContent.startAnimation(animFadeout);

                if (clickedUrl == null) historyList.add(blogPost);
                else clickedUrl = null;

                blogPost = _blogPost;
                setBookmarkIcon(blogPost.isBookmarked());
                showProgressBar(false);

            }
        });
    }

    protected void makeLinkClickable(SpannableStringBuilder strBuilder, final URLSpan span) {

        int start = strBuilder.getSpanStart(span);
        int end = strBuilder.getSpanEnd(span);
        int flags = strBuilder.getSpanFlags(span);
        ClickableSpan clickable = new ClickableSpan() {
            public void onClick(View view) {
                loadPostUrl(span.getURL());
            }
        };
        strBuilder.setSpan(clickable, start, end, flags);
        strBuilder.removeSpan(span);

    }

    public void setTextViewHTML(TextView text, String html) {

        CharSequence sequence = Html.fromHtml(html);
        SpannableStringBuilder strBuilder = new SpannableStringBuilder(sequence);
        URLSpan[] urls = strBuilder.getSpans(0, sequence.length(), URLSpan.class);
        for (URLSpan span : urls) {
            makeLinkClickable(strBuilder, span);
        }
        replaceQuoteSpans(strBuilder);
        text.setText(strBuilder);
        text.setMovementMethod(new CustomMovementMethod());

    }

    private void setBookmarkIcon(boolean isBookmarked) {

        if (bookmark_item != null) {
            if (isBookmarked) bookmark_item.setIcon(R.drawable.ic_bookmark_white_24dp);
            else bookmark_item.setIcon(R.drawable.ic_bookmark_border_white_24dp);

        }

    }

    private void showProgressBar(final boolean show) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mProgressBar.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });

    }

}
