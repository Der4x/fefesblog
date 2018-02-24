package de.fwpm.android.fefesblog.utils;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.Layout;
import android.text.Spannable;
import android.text.style.ForegroundColorSpan;
import android.text.style.LeadingMarginSpan;
import android.text.style.LineBackgroundSpan;
import android.text.style.QuoteSpan;

/**
 * Created by alex on 24.02.18.
 */

public class CustomQuoteSpan implements LeadingMarginSpan, LineBackgroundSpan {

    private static final int MY_BACKGROUND_COLOR = Color.parseColor("#00ffffff");
    private static final int MY_STRIPE_COLOR = Color.parseColor("#cccccc");
    private static final float MY_STRIPE_WIDTH = 4;
    private static final float MY_GAP_WIDTH = 30;

    private final int backgroundColor;
    private final int stripeColor;
    private final float stripeWidth;
    private final float gap;

    public CustomQuoteSpan(int backgroundColor, int stripeColor, float stripeWidth, float gap) {
        this.backgroundColor = backgroundColor;
        this.stripeColor = stripeColor;
        this.stripeWidth = stripeWidth;
        this.gap = gap;
    }

    @Override
    public int getLeadingMargin(boolean first) {
        return (int) (stripeWidth + gap);
    }

    @Override
    public void drawLeadingMargin(Canvas c, Paint p, int x, int dir, int top, int baseline, int bottom,
                                  CharSequence text, int start, int end, boolean first, Layout layout) {
        Paint.Style style = p.getStyle();
        int paintColor = p.getColor();

        p.setStyle(Paint.Style.FILL);
        p.setColor(stripeColor);

        c.drawRect(x, top, x + dir * stripeWidth, bottom, p);

        p.setStyle(style);
        p.setColor(paintColor);
    }

    @Override
    public void drawBackground(Canvas c, Paint p, int left, int right, int top, int baseline, int bottom, CharSequence text, int start, int end, int lnum) {
        int paintColor = p.getColor();
        p.setColor(backgroundColor);
        c.drawRect(left, top, right, bottom, p);
        p.setColor(paintColor);
    }

    public static void replaceQuoteSpans(Spannable spannable) {
        QuoteSpan[] quoteSpans = spannable.getSpans(0, spannable.length(), QuoteSpan.class);
        for (QuoteSpan quoteSpan : quoteSpans) {
            int start = spannable.getSpanStart(quoteSpan);
            int end = spannable.getSpanEnd(quoteSpan);
            int flags = spannable.getSpanFlags(quoteSpan);
            spannable.removeSpan(quoteSpan);
            spannable.setSpan(new CustomQuoteSpan(
                            MY_BACKGROUND_COLOR,
                            MY_STRIPE_COLOR,
                            MY_STRIPE_WIDTH,
                            MY_GAP_WIDTH),
                    start,
                    end,
                    flags);
            spannable.setSpan(new ForegroundColorSpan(0xFF5C5C5C), start, end, flags);
        }
    }


}
