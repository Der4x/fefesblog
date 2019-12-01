package de.fwpm.android.fefesblog.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;
import android.view.View;

import com.google.android.material.snackbar.Snackbar;

import de.fwpm.android.fefesblog.R;

/**
 * Created by alex on 21.01.18.
 */

public class NetworkUtils {

    private Context context;
    public NetworkUtils(Context context){
        this.context = context;
    }

    public boolean isConnectingToInternet(){
        ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null)
        {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null)
                for (int i = 0; i < info.length; i++)
                    if (info[i].getState() == NetworkInfo.State.CONNECTED)
                    {
                        return true;
                    }

        }
        return false;
    }

    public void noNetwork(View view) {

        Snackbar bar = Snackbar.make(view, R.string.no_network, Snackbar.LENGTH_LONG)
                .setAction("EINSTELLUNGEN", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (context instanceof Activity)
                            ((Activity) context).startActivityForResult(new Intent(Settings.ACTION_WIFI_SETTINGS), 0);
                    }
                });

        bar.show();

    }

}
