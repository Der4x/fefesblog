package de.fwpm.android.fefesblog.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import static de.fwpm.android.fefesblog.utils.SharePostUtil.shareLink;

public class CustomTabsBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        shareLink(context, intent.getDataString());

    }
}
