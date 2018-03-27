package de.fwpm.android.fefesblog.utils;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.Html;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.LeadingMarginSpan;
import android.text.style.LineBackgroundSpan;
import android.text.style.QuoteSpan;
import android.text.style.URLSpan;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import static android.text.Html.FROM_HTML_OPTION_USE_CSS_COLORS;
import static de.fwpm.android.fefesblog.utils.CustomQuoteSpan.replaceQuoteSpans;

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
        for(URLSpan span : urls) {
            makeLinkClickable(strBuilder, span);
        }
        replaceQuoteSpans(strBuilder);
        text.setText(trimTrailingWhitespace(strBuilder));
//        text.setMovementMethod(LinkMovementMethod.getInstance());
        text.setMovementMethod(LinkMovementMethod.getInstance());

    }

    public static CharSequence trimTrailingWhitespace(CharSequence source) {

        if(source == null)
            return "";

        int i = source.length();

        // loop back to the first non-whitespace character
        while(--i >= 0 && Character.isWhitespace(source.charAt(i))) {
        }

        return source.subSequence(0, i+1);
    }

}
