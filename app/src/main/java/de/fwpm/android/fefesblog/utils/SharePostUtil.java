package de.fwpm.android.fefesblog.utils;

import android.content.Context;
import android.content.Intent;

import de.fwpm.android.fefesblog.BlogPost;
import de.fwpm.android.fefesblog.R;

/**
 * Created by alex on 25.02.18.
 */

public class SharePostUtil {

    private static final int TEXT_SIZE = 200;

    public static void sharePost(Context context, BlogPost blogPost) {

        String postText = blogPost.getText();
        String preView = postText.length() >= TEXT_SIZE ? postText.substring(4, TEXT_SIZE)+"..." : postText.substring(4);
        Intent share = new Intent();
        share.setAction(Intent.ACTION_SEND);
        share.putExtra(Intent.EXTRA_TEXT, preView + "\n\n" + blogPost.getUrl());
        share.setType("text/plain");
        context.startActivity(Intent.createChooser(share, context.getResources().getText(R.string.share_to)));

    }

    public static void shareLink(Context context, String url, String title) {

        Intent share = new Intent();
        share.setAction(Intent.ACTION_SEND);
        share.putExtra(Intent.EXTRA_TEXT, title + "\n\n" + url);
        share.setType("text/plain");
        context.startActivity(Intent.createChooser(share, context.getResources().getText(R.string.share_link)));

    }

}
