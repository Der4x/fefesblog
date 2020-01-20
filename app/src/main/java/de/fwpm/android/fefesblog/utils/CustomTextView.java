package de.fwpm.android.fefesblog.utils;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;

import androidx.browser.customtabs.CustomTabsIntent;
import androidx.core.content.ContextCompat;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.view.View;
import android.widget.TextView;

import java.io.Serializable;

import de.fwpm.android.fefesblog.App;
import de.fwpm.android.fefesblog.BlogPost;
import de.fwpm.android.fefesblog.DetailsActivity;
import de.fwpm.android.fefesblog.R;
import de.fwpm.android.fefesblog.WebActivity;

import static de.fwpm.android.fefesblog.DetailsActivity.INTENT_URL;
import static de.fwpm.android.fefesblog.utils.CustomQuoteSpan.replaceQuoteSpans;

/**
 * Created by alex on 25.01.18.
 */

public class CustomTextView {

    public static final String YOUTUBE_PACKAGE_NAME = "com.google.android.youtube";
    public static final String TWITTER_PACKAGE_NAME = "com.twitter.android";

    private static final String TAG = "CustomTextView";
    public static String clickedLink;

    protected static void makeLinkClickable(SpannableStringBuilder strBuilder, final URLSpan span) {
        int start = strBuilder.getSpanStart(span);
        int end = strBuilder.getSpanEnd(span);
        int flags = strBuilder.getSpanFlags(span);
        final ClickableSpan clickable = new ClickableSpan() {
            public void onClick(View view) {

                clickedLink = span.getURL();

            }
        };
        strBuilder.setSpan(clickable, start, end, flags);
        strBuilder.removeSpan(span);
    }

    public static void setTextViewHTML(TextView text, String html) {

        CharSequence sequence = Html.fromHtml(html);
        SpannableStringBuilder strBuilder = new SpannableStringBuilder(sequence);
        URLSpan[] urls = strBuilder.getSpans(0, sequence.length(), URLSpan.class);
        for (URLSpan span : urls) {
            makeLinkClickable(strBuilder, span);
        }
        replaceQuoteSpans(strBuilder);
        text.setText(trimTrailingWhitespace(strBuilder));
        text.setMovementMethod(LinkMovementMethod.getInstance());

    }

    public static CharSequence trimTrailingWhitespace(CharSequence source) {

        if (source == null)
            return "";

        int i = source.length();

        // loop back to the first non-whitespace character
        while (--i >= 0 && Character.isWhitespace(source.charAt(i))) {
        }

        return source.subSequence(0, i + 1);
    }

    public static boolean handleClickedLink(Activity activity, BlogPost blogPost, String url) {

        if (url.startsWith("/?ts=") || url.startsWith("https://blog.fefe.de/?ts=")) {

            Intent intent = new Intent(activity, DetailsActivity.class);
            intent.putExtra(INTENT_URL, url);
            intent.putExtra(DetailsActivity.INTENT_BLOG_POST, (Serializable) blogPost);
            activity.startActivity(intent);
            return true;

        } else if (new NetworkUtils(activity).isConnectingToInternet()) {

            if (url.startsWith("//ptrace.fefe.de")) url = "https:" + url;

            try {
                startCustomTab(activity, url);
            } catch (Exception e) {

                e.printStackTrace();
                startWebActivity(activity, url);

            }

            return true;
        }

        return false;


    }

    private static void startWebActivity(Activity activity, String url) {

        if ((url.contains("youtu.be") || url.contains("www.youtube")) && isAppInstalled(YOUTUBE_PACKAGE_NAME, activity)) {

            openInApp(url, YOUTUBE_PACKAGE_NAME, activity);


        } else if(url.contains("twitter.com") && isAppInstalled(TWITTER_PACKAGE_NAME, activity)) {

            openInApp(url, TWITTER_PACKAGE_NAME, activity);


        } else {

            Intent intent = new Intent(activity, WebActivity.class);
            intent.putExtra(INTENT_URL, url);
            activity.startActivity(intent);

        }
    }

    private static void startCustomTab(Activity activity, String url) {

        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
        builder.addDefaultShareMenuItem();

        builder.setToolbarColor(ContextCompat.getColor(activity, App.getInstance().isNightModeEnabled()
                ? (App.getInstance().isAmoledModeEnabled() ? R.color.colorPrimaryDark : R.color.backgroundDark)
                : R.color.backgroundLight));
        builder.setShowTitle(true);

        CustomTabsIntent customTabsIntent = builder.build();
        customTabsIntent.launchUrl(activity, Uri.parse(url));
    }

    private static void openInApp(String url, String packageName, Activity activity) {

        Intent viewIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        viewIntent.setPackage(packageName);
        try {
            activity.startActivity(viewIntent);
        } catch (android.content.ActivityNotFoundException ex) {
            ex.printStackTrace();
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            activity.startActivity(intent);
        }

    }

    private static boolean isAppInstalled(String packageName, Activity activity) {
        PackageManager packageManager = activity.getPackageManager();
        try {
            packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {

        }
        return false;
    }

}
