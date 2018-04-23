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
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.WebBackForwardList;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.File;

import de.fwpm.android.fefesblog.database.AppDatabase;

import static de.fwpm.android.fefesblog.utils.SharePostUtil.shareLink;
import static de.fwpm.android.fefesblog.utils.SharePostUtil.sharePost;

public class WebActivity extends AppCompatActivity {

    public static final String INTENT_URL = "CLICKED_LINK";

    private MenuItem share_item;
    private WebView mWebView;
    private ProgressBar mProgressBar;
    private Context context;

    private String mCurrentUrl;
    private String downloadUrl;

    private boolean clearWebViewHistory;

    private DownloadManager dm;
    private long enq;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);

        initToolbar();
        initWebView();

        String url = getIntent().getStringExtra(INTENT_URL);

        if(url.startsWith("//ptrace.fefe.de")) url = "https:" + url;

        mWebView.loadUrl(url);

        context = this;

    }

    @Override
    public void onPause() {
        super.onPause();
        mWebView.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        mWebView.onResume();
    }

    @Override
    public void onBackPressed() {

        if (mWebView.canGoBack()) goBackInWebView();
        else super.onBackPressed();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.web_menu, menu);

        share_item = menu.findItem(R.id.menu_share);
        share_item.setIcon(R.drawable.ic_share_white_24dp);

        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:

                if (mWebView.canGoBack()) goBackInWebView();
                else this.finish();
                break;

            case R.id.menu_share:

                shareLink(context, mWebView.getUrl(), mWebView.getTitle());
                break;
        }
        return true;

    }

    private void initToolbar() {

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("link", mWebView.getUrl());
                clipboard.setPrimaryClip(clip);
                Toast.makeText(context, "URL in Zwischenablage gespeichert", Toast.LENGTH_LONG).show();

            }
        });
    }

    private void initWebView() {

        mWebView = (WebView) findViewById(R.id.webview);
        mProgressBar = (ProgressBar) findViewById(R.id.progess_bar);

        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setDomStorageEnabled(true);

        mWebView.getSettings().setSupportZoom(true);
        mWebView.getSettings().setBuiltInZoomControls(true);
        mWebView.getSettings().setDisplayZoomControls(false);

        mWebView.getSettings().setLoadWithOverviewMode(true);
        mWebView.getSettings().setUseWideViewPort(true);
        mWebView.setLayerType(View.LAYER_TYPE_HARDWARE, null);

        CookieManager.getInstance().setAcceptThirdPartyCookies(mWebView, false);

        mWebView.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent,
                                        String contentDisposition, String mimetype,
                                        long contentLength) {

                if (haveStoragePermission()) {
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

                }
                return true;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {

                if (!url.equals("about:blank")) {
                    getSupportActionBar().setTitle("Laden...");
                    getSupportActionBar().setSubtitle(view.getUrl());
                }

            }


            @Override
            public void onPageFinished(WebView view, String url) {

                mCurrentUrl = url;

                if (!url.equals("about:blank")) {
                    getSupportActionBar().setTitle(view.getTitle());
                    getSupportActionBar().setSubtitle(view.getUrl());
                }
                if (clearWebViewHistory) {
                    mWebView.clearHistory();
                    clearWebViewHistory = false;
                }
            }
        });

        mWebView.setWebChromeClient(new WebChromeClient() {

            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (newProgress <= 100 && !progressBarVisible()) {
                    showProgressBar(true);
                }
                mProgressBar.setProgress(newProgress);

                if (newProgress == 100) showProgressBar(false);
            }
        });

    }

    private boolean progressBarVisible() {
        return mProgressBar.getVisibility() == View.VISIBLE;
    }

    private void showProgressBar(final boolean show) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mProgressBar.setVisibility(show ? View.VISIBLE : View.GONE);
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
        if (url == null) {
            this.finish();
        }
    }

    private void downloadContent(String url) {

        String[] splitUrl = url.split("/");
        String filename = splitUrl[splitUrl.length - 1];

        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.allowScanningByMediaScanner();
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename);
        dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        registerReceiver(receiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        enq = dm.enqueue(request);
        Toast.makeText(getApplicationContext(), "Downloading File", Toast.LENGTH_LONG).show();
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

    private void openInApp(String url, String packageName) {

        Intent viewIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        viewIntent.setPackage(packageName);
        startActivity(viewIntent);
        this.finish();

    }

    private boolean isAppInstalled(String packageName) {
        PackageManager packageManager = getPackageManager();
        try {
            packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {

        }
        return false;
    }

    public static void clearCookies() {
        CookieManager.getInstance().removeAllCookies(null);
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
                            uriString = uriString.substring(7);
                        }

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            File file = new File(uriString);
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
