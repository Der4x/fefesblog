package de.fwpm.android.fefesblog;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.DownloadListener;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

import de.fwpm.android.fefesblog.data.SingleDataFetcher;
import de.fwpm.android.fefesblog.database.AppDatabase;
import de.fwpm.android.fefesblog.utils.NetworkUtils;

import static de.fwpm.android.fefesblog.utils.CustomQuoteSpan.replaceQuoteSpans;
import static de.fwpm.android.fefesblog.utils.SharePostUtil.sharePost;

public class DetailsActivity extends AppCompatActivity implements Animation.AnimationListener {

    private static final String TAG = "DetailActivity";
    public static final String INTENT_BLOG_POST = "blogPost";
    public static final String INTENT_URL = "CLICKED_LINK";
    private BlogPost blogPost;
    private TextView postContent;
    private MenuItem bookmark_item;
    private MenuItem share_item;

    private FrameLayout mWebContainer;
    private FrameLayout mContainer;
    private WebView mWebView;
    private ProgressBar mProgressBar;
    private boolean newPostLoaded;
    private boolean direktClick;
    private ArrayList<BlogPost> historyList;
    private NetworkUtils networkUtils;
    private String mCurrentUrl;

    Animation animFadein;
    Animation animFadeout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        newPostLoaded = false;
        historyList = new ArrayList<>();
        networkUtils = new NetworkUtils(this);

        initView();

        initWebView();


        final Intent intent = getIntent();

        Serializable extra = intent.getSerializableExtra(INTENT_BLOG_POST);
        if (extra instanceof BlogPost) {

            blogPost = (BlogPost) extra;

            setContent();

            if(intent.hasExtra(INTENT_URL)) {
                direktClick = true;
                loadPostUrl(intent.getStringExtra(INTENT_URL));
            }

        }
    }

    @Override
    public void onBackPressed() {

        if(direktClick && !mWebView.canGoBack()) {

            finish();

        } else if(mWebContainer.getVisibility() == View.VISIBLE) {

            if(mWebView.canGoBack()) mWebView.goBack();
            else hideWebView();

        } else if(historyList.size() > 0) {

            changeBlogPost(historyList.get(historyList.size() - 1));
            historyList.remove(historyList.size() - 1);
            historyList.remove(historyList.size() - 1);

        } else super.onBackPressed();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:

                if(direktClick && !mWebView.canGoBack()) {

                    finish();

                } else if(mWebContainer.getVisibility() == View.VISIBLE) {

                    if(mWebView.canGoBack()) mWebView.goBack();
                    else hideWebView();

                } else if(historyList.size() > 0) {

                    changeBlogPost(historyList.get(historyList.size() - 1));
                    historyList.remove(historyList.size() - 1);
                    historyList.remove(historyList.size() - 1);

                } else this.finish();
                break;

            case R.id.menu_bookmark:
                blogPost.setBookmarked(!blogPost.isBookmarked());
                setBookmarkIcon(blogPost.isBookmarked());

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if(newPostLoaded) AppDatabase.getInstance(getBaseContext()).blogPostDao().insertBlogPost(blogPost);
                        else AppDatabase.getInstance(getBaseContext()).blogPostDao().updateBlogPost(blogPost);
                    }
                }).start();
                break;
            case R.id.menu_share:
                sharePost(getApplicationContext(), blogPost);
                break;
        }
        return true;

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

    private void initView() {

        mWebContainer = (FrameLayout) findViewById(R.id.web_container);
        mContainer = (FrameLayout) findViewById(R.id.container);
        mProgressBar = (ProgressBar) findViewById(R.id.progess_bar);
        postContent = (TextView) findViewById(R.id.blogPostText);

        animFadein = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in);
        animFadein.setAnimationListener(this);
        animFadeout = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_out);
        animFadeout.setAnimationListener(this);

    }

    private void initWebView() {

        mWebView = (WebView) findViewById(R.id.webview);
        mWebView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setLoadWithOverviewMode(true);
        mWebView.getSettings().setUseWideViewPort(true);
        mWebView.getSettings().setSupportZoom(true);
        mWebView.getSettings().setBuiltInZoomControls(true);
        mWebView.getSettings().setDisplayZoomControls(false);
        mWebView.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        mWebView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        mWebView.getSettings().setDomStorageEnabled(true);
        mWebView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        mWebView.setScrollbarFadingEnabled(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            mWebView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        } else {
            mWebView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }

        mWebView.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent,
                                        String contentDisposition, String mimetype,
                                        long contentLength) {

                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
            }
        });


        mWebView.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {

                if(mCurrentUrl != null && url != null && url.equals(mCurrentUrl)) {

                    onBackPressed();

                } else {
                    view.loadUrl(url);
                    mCurrentUrl = url;
                    mProgressBar.setVisibility(View.VISIBLE);
                }


//                view.loadUrl(url);
                return true;

            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {

            }


            @Override
            public void onPageFinished(WebView view, String url) {

                mProgressBar.setVisibility(View.INVISIBLE);

            }
        });

    }

    private void showWebView() {

        mWebContainer.setVisibility(View.VISIBLE);
        mWebContainer.animate()
                .alpha(1)
                .setDuration(500);
    }

    private void hideWebView() {

        mWebContainer.setVisibility(View.INVISIBLE);
        mWebContainer.animate()
                .alpha(0)
                .setDuration(500);

        mWebView.loadUrl("");

    }

    private void setContent() {

        setTitle(new SimpleDateFormat("d. MMMM yyyy", Locale.GERMANY).format(blogPost.getDate()));
        setTextViewHTML(postContent,blogPost.getHtmlText().split("</a>", 2)[1]);

    }

    public void loadPostUrl(final String url) {

        if(url.startsWith("/?ts=")) {

            new Thread(new Runnable() {
                @Override
                public void run() {

                    BlogPost linkPost = AppDatabase.getInstance(getBaseContext()).blogPostDao().getPostByUrl(getString(R.string.basic_url) + url);

                    if(linkPost != null) changeBlogPost(linkPost);
                    else if(networkUtils.isConnectingToInternet()) {

                        new SingleDataFetcher(DetailsActivity.this).execute(url);
                        newPostLoaded = true;
                        mProgressBar.setVisibility(View.VISIBLE);

                    } else networkUtils.noNetwork(mContainer);

                }
            }).start();

        } else {

            if(networkUtils.isConnectingToInternet()) {
                mWebView.loadUrl(url);
                mProgressBar.setVisibility(View.VISIBLE);
                showWebView();
            }
            else networkUtils.noNetwork(mContainer);

        }
    }

    public void changeBlogPost(final BlogPost _blogPost) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                postContent.setVisibility(View.INVISIBLE);
                postContent.startAnimation(animFadeout);

                historyList.add(blogPost);
                blogPost = _blogPost;
                setBookmarkIcon(blogPost.isBookmarked());
                mProgressBar.setVisibility(View.INVISIBLE);

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
        for(URLSpan span : urls) {
            makeLinkClickable(strBuilder, span);
        }
        replaceQuoteSpans(strBuilder);
        text.setText(strBuilder);
        text.setMovementMethod(LinkMovementMethod.getInstance());

    }

    private void setBookmarkIcon(boolean isBookmarked) {

        if(bookmark_item != null) {
            if (isBookmarked) bookmark_item.setIcon(R.drawable.ic_bookmark_white_24dp);
            else bookmark_item.setIcon(R.drawable.ic_bookmark_border_white_24dp);

        }

    }

    @Override
    public void onAnimationEnd(Animation animation) {

        if (animation == animFadein) {

        } else if (animation == animFadeout) {

            if(postContent.getVisibility() == View.INVISIBLE) {
                setContent();
                postContent.setVisibility(View.VISIBLE);
                postContent.startAnimation(animFadein);
            }

        }

    }

    @Override
    public void onAnimationRepeat(Animation animation) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onAnimationStart(Animation animation) {
        // TODO Auto-generated method stub

    }

}
