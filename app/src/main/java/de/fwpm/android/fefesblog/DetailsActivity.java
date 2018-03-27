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
import android.webkit.DownloadListener;
import android.webkit.WebBackForwardList;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
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
import static de.fwpm.android.fefesblog.utils.SharePostUtil.shareLink;
import static de.fwpm.android.fefesblog.utils.SharePostUtil.sharePost;

public class DetailsActivity extends AppCompatActivity {

    private static final String TAG = "DetailActivity";
    public static final String INTENT_BLOG_POST = "blogPost";
    public static final String INTENT_URL = "CLICKED_LINK";
    private BlogPost blogPost;
    private TextView postContent;
    private MenuItem bookmark_item;
    private MenuItem share_item;

    private FrameLayout mWebContainer;
    private CoordinatorLayout mContainer;
    private WebView mWebView;
    private ProgressBar mProgressBar;
    private boolean newPostLoaded;
    private boolean direktClick;
    private boolean clearWebViewHistory;
    private boolean titleClickable;
    private ArrayList<BlogPost> historyList;
    private NetworkUtils networkUtils;
    private String mCurrentUrl;
    private Context context;
    private String downloadUrl;
    private DownloadManager dm;
    private long enq;

    Animation animFadein;
    Animation animFadeout;

    Animation animSlideDown;
    Animation animSlideUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        context = this;
        newPostLoaded = false;
        historyList = new ArrayList<>();
        networkUtils = new NetworkUtils(this);

        initToolbar();

        initView();

        initWebView();


        final Intent intent = getIntent();

        Serializable extra = intent.getSerializableExtra(INTENT_BLOG_POST);
        if (extra instanceof BlogPost) {

            blogPost = (BlogPost) extra;

            setContent();

            if (intent.hasExtra(INTENT_URL)) {
                direktClick = true;
                loadPostUrl(intent.getStringExtra(INTENT_URL));
            }

        }
    }


    @Override
    public void onBackPressed() {

        if (direktClick && !mWebView.canGoBack() && historyList.size() <= 1) {

            finish();

        } else if (mWebContainer.getVisibility() == View.VISIBLE) {

            if (mWebView.canGoBack()) goBackInWebView();
            else hideWebView();

        } else if (historyList.size() > 0) {

            changeBlogPost(historyList.get(historyList.size() - 1));
            historyList.remove(historyList.size() - 1);
            historyList.remove(historyList.size() - 1);

        } else super.onBackPressed();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:

                if (direktClick && !mWebView.canGoBack() && historyList.size() <= 1) {

                    finish();

                } else if (mWebContainer.getVisibility() == View.VISIBLE) {

                    if (mWebView.canGoBack()) goBackInWebView();
                    else hideWebView();

                } else if (historyList.size() > 0) {

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
                        if (newPostLoaded)
                            AppDatabase.getInstance(getBaseContext()).blogPostDao().insertBlogPost(blogPost);
                        else
                            AppDatabase.getInstance(getBaseContext()).blogPostDao().updateBlogPost(blogPost);
                    }
                }).start();
                break;
            case R.id.menu_share:
                if (mWebContainer.getVisibility() == View.VISIBLE)
                    shareLink(context, mWebView.getUrl(), mWebView.getTitle());
                else
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
        bookmark_item.setIcon(blogPost.isBookmarked() ? R.drawable.ic_bookmark_white_24dp : R.drawable.ic_bookmark_border_white_24dp);

        share_item = menu.findItem(R.id.menu_share);
        share_item.setIcon(R.drawable.ic_share_white_24dp);

        if(mWebContainer != null && mWebContainer.getVisibility() == View.VISIBLE) bookmark_item.setVisible(false);

        return true;

    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (titleClickable) {

                    ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("link", mWebView.getUrl());
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(context, "URL in Zwischenablage gespeichert", Toast.LENGTH_LONG).show();

                }
            }
        });
    }

    private void initView() {

        mWebContainer = (FrameLayout) findViewById(R.id.web_container);
        mContainer = (CoordinatorLayout) findViewById(R.id.container);
        mProgressBar = (ProgressBar) findViewById(R.id.progess_bar);
        postContent = (TextView) findViewById(R.id.blogPostText);

        Animation.AnimationListener animationListener = new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

                if (animation == animFadein) {

                } else if (animation == animFadeout) {

                    if (postContent.getVisibility() == View.INVISIBLE) {
                        setContent();
                        postContent.setVisibility(View.VISIBLE);
                        postContent.startAnimation(animFadein);
                    }

                }

            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        };

        animFadein = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in);
        animFadein.setAnimationListener(animationListener);
        animFadeout = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_out);
        animFadeout.setAnimationListener(animationListener);


        animSlideDown = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_down);
        animSlideDown.setAnimationListener(animationListener);
        animSlideUp = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_up);
        animSlideUp.setAnimationListener(animationListener);

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

//                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));

                if(haveStoragePermission()) {
                    downloadContent(url);
                } else {
                    downloadUrl = url;
                    requestForStoragePermission();
                }

            }
        });


        mWebView.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {

                if (mCurrentUrl != null && url.equals(mCurrentUrl)) {

                    onBackPressed();

                } else {
                    view.loadUrl(url);
                    mCurrentUrl = url;
                    showProgressBar(true);

                }

                return true;

            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {

                if (!url.equals("about:blank")) {
                    getSupportActionBar().setTitle("Laden...");
                    getSupportActionBar().setSubtitle(view.getUrl());
                    titleClickable = true;
                }

            }


            @Override
            public void onPageFinished(WebView view, String url) {

                showProgressBar(false);
                if (!url.equals("about:blank") && mWebContainer.getVisibility() == View.VISIBLE) {
                    getSupportActionBar().setTitle(view.getTitle());
                    getSupportActionBar().setSubtitle(view.getUrl());
                }
                if (clearWebViewHistory) {
                    mWebView.clearHistory();
                    clearWebViewHistory = false;
                }
            }
        });

    }

    private void downloadContent(String url) {

        String[] splitUrl = url.split("/");
        String filename = splitUrl[splitUrl.length-1];

        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.allowScanningByMediaScanner();
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename);
        dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        registerReceiver(receiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        enq = dm.enqueue(request);
        Toast.makeText(getApplicationContext(), "Downloading File", Toast.LENGTH_LONG).show();
    }

    private void showWebView() {

        mWebContainer.setVisibility(View.VISIBLE);
        if(!direktClick) mWebContainer.startAnimation(animSlideUp);
//        mWebContainer.animate()
//                .alpha(1)
//                .setDuration(500);
        clearWebViewHistory = true;
        if(bookmark_item != null) bookmark_item.setVisible(false);

    }

    private void hideWebView() {

        if(bookmark_item != null) bookmark_item.setVisible(true);
        setContent();
        mWebContainer.setVisibility(View.INVISIBLE);
        mWebContainer.startAnimation(animSlideDown);
//        mWebContainer.animate()
//                .alpha(0)
//                .setDuration(500);

        clearWebViewHistory = true;
        mWebView.loadUrl("");
        mCurrentUrl = "";

    }

    private void setContent() {

        getSupportActionBar().setTitle(new SimpleDateFormat("d. MMMM yyyy", Locale.GERMANY).format(blogPost.getDate()));
        getSupportActionBar().setSubtitle("");
        setTextViewHTML(postContent, blogPost.getHtmlText().split("</a>", 2)[1]);
        titleClickable = false;

    }

    public void loadPostUrl(final String url) {

        if (url.startsWith("/?ts=")) {

            new Thread(new Runnable() {
                @Override
                public void run() {

                    BlogPost linkPost = AppDatabase.getInstance(getBaseContext()).blogPostDao().getPostByUrl(getString(R.string.basic_url) + url);

                    if (linkPost != null) changeBlogPost(linkPost);
                    else if (networkUtils.isConnectingToInternet()) {

                        new SingleDataFetcher(DetailsActivity.this).execute(url);
                        newPostLoaded = true;
                        showProgressBar(true);

                    } else networkUtils.noNetwork(mContainer);

                }
            }).start();

        } else {

            if (networkUtils.isConnectingToInternet()) {
                mWebView.loadUrl(url);
                showProgressBar(true);
                showWebView();
            } else networkUtils.noNetwork(mContainer);

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
                mProgressBar.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
            }
        });

    }

    public void goBackInWebView() {
        WebBackForwardList history = mWebView.copyBackForwardList();
        int index = -1;
        String url = null;

        while (mWebView.canGoBackOrForward(index)) {
            if (!history.getItemAtIndex(history.getCurrentIndex() + index).getUrl().equals("about:blank")) {
                mWebView.goBackOrForward(index);
                url = history.getItemAtIndex(-index).getUrl();
                Log.e("tag", "first non empty" + url);
                break;
            }
            index--;

        }
        // no history found that is not empty
        if (url == null) {
            Log.d(TAG, "goBackInWebView: no history found that is not empty");
            hideWebView();
        }
    }

    private boolean haveStoragePermission() {

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    private void requestForStoragePermission() {
        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);

    }

    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {

        switch (requestCode) {
            case 1: {

                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    downloadContent(downloadUrl);

                } else {

                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(downloadUrl)));

                }
                return;
            }
        }
    }

    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
                long downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0);
                DownloadManager.Query query = new DownloadManager.Query();
                query.setFilterById(enq);
                Cursor c = dm.query(query);
                if (c.moveToFirst()) {
                    int columnIndex = c.getColumnIndex(DownloadManager.COLUMN_STATUS);
                    if (DownloadManager.STATUS_SUCCESSFUL == c.getInt(columnIndex)) {
                        String uriString = c.getString(c.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));

                        if (uriString.substring(0, 7).matches("file://")) {
                            uriString =  uriString.substring(7);
                        }

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            File file=new File(uriString);
                            Uri uri = FileProvider.getUriForFile(getApplicationContext(), getPackageName() + ".provider", file);
                            intent = new Intent(Intent.ACTION_VIEW);
                            intent.setData(uri);
                            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            startActivity(intent);
                        } else {
                            intent = new Intent(Intent.ACTION_VIEW);
                            intent.setDataAndType(Uri.parse(uriString), "application/pdf");
                            intent = Intent.createChooser(intent, "Open File");
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        }

                        onBackPressed();

                    }
                }
            }
        }
    };
}
