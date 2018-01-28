package de.fwpm.android.fefesblog.utils;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by alex on 28.01.18.
 */

public class PreventScrollTextView extends TextView {
    public PreventScrollTextView(Context context) {
        super(context);
    }

    public PreventScrollTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PreventScrollTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public PreventScrollTextView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public void scrollTo(int x, int y) {
//do nothing
    }
}
