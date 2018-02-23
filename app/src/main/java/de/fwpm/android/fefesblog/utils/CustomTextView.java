package de.fwpm.android.fefesblog.utils;

import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import static android.text.Html.FROM_HTML_OPTION_USE_CSS_COLORS;

/**
 * Created by alex on 25.01.18.
 */

public class CustomTextView {

    private static final String TAG = "CustomTextView";
    public static String clickedLink;

    protected static void makeLinkClickable(SpannableStringBuilder strBuilder, final URLSpan span)
    {
        int start = strBuilder.getSpanStart(span);
        int end = strBuilder.getSpanEnd(span);
        int flags = strBuilder.getSpanFlags(span);
        final ClickableSpan clickable = new ClickableSpan() {
            public void onClick(View view) {

                Log.d(TAG, "onClick: " + span.getURL());
                clickedLink = span.getURL();

            }
        };
        strBuilder.setSpan(clickable, start, end, flags);
        strBuilder.removeSpan(span);
    }

    public static void setTextViewHTML(TextView text, String html)
    {
//        String fontcolor = "<p style=\"color:red;\">";
//        String styled = html.replace("<a ", "<a style=\"color:red\" ");


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
