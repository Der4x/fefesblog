package de.fwpm.android.fefesblog;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
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
import java.util.Locale;

import de.fwpm.android.fefesblog.database.AppDatabase;

public class DetailsActivity extends AppCompatActivity {

    private static final String TAG = "DetailActivity";
    public static final String INTENT_BLOG_POST = "blogPost";
    BlogPost blogPost;
    private TextView postContent;
    private MenuItem bookmark_item;
    private MenuItem share_item;

    private FrameLayout mWebContainer;
    private WebView mWebView;
    private ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        mWebContainer = (FrameLayout) findViewById(R.id.web_container);
        mProgressBar = (ProgressBar) findViewById(R.id.progess_bar);

        initWebView();

        // Get BlogPost
        final Intent intent = getIntent();


        Serializable extra = intent.getSerializableExtra(INTENT_BLOG_POST);
        if (extra instanceof BlogPost) {

            blogPost = (BlogPost) extra;

//            set detail title
            SimpleDateFormat dateFormat = new SimpleDateFormat("d. MMMM yyyy", Locale.GERMANY);
            setTitle(dateFormat.format(blogPost.getDate()));

            postContent = (TextView) findViewById(R.id.blogPostText);
            setTextViewHTML(postContent,blogPost.getHtmlText().split("</a>", 2)[1]);
            if(intent.hasExtra("CLICKED_LINK")) loadPostUrl(intent.getStringExtra("CLICKED_LINK"));

        }

    }

    public void loadPostUrl(String url) {

        mWebView.loadUrl(url);
        mProgressBar.setVisibility(View.VISIBLE);

    }

    private void initWebView() {

        mWebView = (WebView) findViewById(R.id.webview);
        mWebView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setBuiltInZoomControls(true);
        mWebView.getSettings().setDomStorageEnabled(true);
        mWebView.getSettings().setLoadWithOverviewMode(true);

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

                view.loadUrl(url);
                return true;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {

            }


            @Override
            public void onPageFinished(WebView view, String url) {

                showWebView();

            }
        });
    }


    @Override
    public void onBackPressed()
    {
        if(mWebContainer.getVisibility() == View.VISIBLE) {

            hideWebView();

        } else super.onBackPressed();
    }

    private void showWebView() {
        mWebContainer.setVisibility(View.VISIBLE);
        mWebContainer.animate()
                .translationY(0)
                .setDuration(300);
        mProgressBar.setVisibility(View.INVISIBLE);
    }

    private void hideWebView() {

        mWebContainer.animate()
                .translationY(mWebContainer.getHeight())
                .setDuration(300);
        mWebContainer.setVisibility(View.INVISIBLE);
        mWebView.stopLoading();

    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        switch (item.getItemId()) {
            case android.R.id.home:
                if(mWebContainer.getVisibility() == View.VISIBLE) {
                    hideWebView();

                } else this.finish();
                break;
            case R.id.menu_bookmark:
                blogPost.setBookmarked(!blogPost.isBookmarked());
                setBookmarkIcon(blogPost.isBookmarked());

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        AppDatabase.getInstance(getBaseContext()).blogPostDao().updateBlogPost(blogPost);
                    }
                }).start();
                break;
            case R.id.menu_share:
                String postText = blogPost.getText();
                String preView = postText.length() > 99 ? postText.substring(4, 100) : postText.substring(4);
                Intent share = new Intent();
                share.setAction(Intent.ACTION_SEND);
                share.putExtra(Intent.EXTRA_TEXT, preView + "...\n\n" + blogPost.getUrl());
                share.setType("text/plain");
                startActivity(Intent.createChooser(share, getResources().getText(R.string.share_to)));
                break;
        }
        return true;

    }

    private void setBookmarkIcon(boolean isBookmarked) {

        if (isBookmarked) bookmark_item.setIcon(R.drawable.ic_bookmark_white_24dp);
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

    protected void makeLinkClickable(SpannableStringBuilder strBuilder, final URLSpan span)
    {
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

    public void setTextViewHTML(TextView text, String html)
    {
        CharSequence sequence = Html.fromHtml(html);
        SpannableStringBuilder strBuilder = new SpannableStringBuilder(sequence);
        URLSpan[] urls = strBuilder.getSpans(0, sequence.length(), URLSpan.class);
        for(URLSpan span : urls) {
            makeLinkClickable(strBuilder, span);
        }
        text.setText(strBuilder);
        text.setMovementMethod(LinkMovementMethod.getInstance());
    }

}
