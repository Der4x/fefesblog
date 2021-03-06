package de.fwpm.android.fefesblog;

import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.OnApplyWindowInsetsListener;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.AppBarLayout;

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
    private Toolbar toolbar;
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
            darkTheme = true;
            if(App.getInstance().isAmoledModeEnabled())
                setTheme(R.style.MainActivityThemeAmoledDark);
            else
                setTheme(R.style.MainActivityThemeDark);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

            ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), new OnApplyWindowInsetsListener() {
                @Override
                public WindowInsetsCompat onApplyWindowInsets(View v, WindowInsetsCompat insets) {

                    AppBarLayout.LayoutParams params = (AppBarLayout.LayoutParams) toolbar.getLayoutParams();

                    int margin = insets.getSystemWindowInsetTop();

                    if(margin > 0) {

                        params.topMargin = margin;
                        toolbar.setLayoutParams(params);

                    }

                    int paddingBottom = insets.getSystemWindowInsetBottom();
                    if(paddingBottom > 0) {
                        ((ScrollView) findViewById(R.id.scrollView)).setPadding(0,0,0,paddingBottom);
                    }

                    return insets.consumeSystemWindowInsets();
                }
            });
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
        if(blogPost != null)
            bookmark_item.setIcon(blogPost.isBookmarked() ? R.drawable.ic_stat_bookmark : R.drawable.ic_stat_bookmark_border);

        bookmark_item.getIcon().setColorFilter(getResources().getColor(darkTheme ? R.color.primaryTextColorDark : R.color.secondaryTextColorLight), PorterDuff.Mode.SRC_IN);

        share_item = menu.findItem(R.id.menu_share);
        share_item.getIcon().setColorFilter(getResources().getColor(darkTheme ? R.color.primaryTextColorDark : R.color.secondaryTextColorLight), PorterDuff.Mode.SRC_IN);

        return true;

    }

    private void initToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
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
            if (isBookmarked) bookmark_item.setIcon(R.drawable.ic_stat_bookmark);
            else bookmark_item.setIcon(R.drawable.ic_stat_bookmark_border);

            bookmark_item.getIcon().setColorFilter(getResources().getColor(darkTheme ? R.color.primaryTextColorDark : R.color.secondaryTextColorLight), PorterDuff.Mode.SRC_IN);

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
