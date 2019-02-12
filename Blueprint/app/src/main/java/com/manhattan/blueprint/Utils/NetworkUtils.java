package com.manhattan.blueprint.Utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;

import com.manhattan.blueprint.Controller.MapViewActivity;
import com.manhattan.blueprint.Model.DAO.Consumer;
import com.manhattan.blueprint.R;

import java.util.concurrent.Callable;

public class NetworkUtils {
    private static boolean isNetworkConnected(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(context.CONNECTIVITY_SERVICE);

        NetworkInfo.State mobileState = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState();
        NetworkInfo.State wifiState = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();

        return mobileState == NetworkInfo.State.CONNECTED ||
                mobileState == NetworkInfo.State.CONNECTING ||
                wifiState == NetworkInfo.State.CONNECTED ||
                wifiState == NetworkInfo.State.CONNECTING;
    }

    public static class CheckNetworkConnectionThread extends Thread {
        private boolean insideDialog;
        private Context context;
        private Consumer<Void> consumer;

        public CheckNetworkConnectionThread(Context context) {
            this.context = context;
            this.insideDialog = false;
        }

        public void setCallback(Consumer<Void> consumer){
            this.consumer = consumer;
        }

        public void canContinue(boolean canContinue) {
            insideDialog = !canContinue;
        }

        @Override
        public void run() {
            if (!isNetworkConnected(context) && !insideDialog) {
                insideDialog = true;
                consumer.consume(null);
            }
        }
    }
}
