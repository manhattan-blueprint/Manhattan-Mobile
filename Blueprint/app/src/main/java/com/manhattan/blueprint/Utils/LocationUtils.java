package com.manhattan.blueprint.Utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Build;
import android.provider.Settings;

import com.manhattan.blueprint.R;
import com.mapbox.mapboxsdk.geometry.LatLng;


public class LocationUtils {
    // Formula from https://stackoverflow.com/a/11172685/5310315
    public static double distanceBetween(LatLng a, LatLng b) {
        double earthRadius = 6378.137;
        double dLat = b.getLatitude() * Math.PI / 180 - a.getLatitude() * Math.PI / 180;
        double dLong = b.getLongitude() * Math.PI / 180 - a.getLongitude() * Math.PI / 180;

        double alpha = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(a.getLatitude() * Math.PI / 180) *
                        Math.cos(b.getLatitude() * Math.PI / 180) *
                        Math.sin(dLong/2) * Math.sin(dLong/2);

        double c = 2 * Math.atan2(Math.sqrt(alpha), Math.sqrt(1-alpha));
        double d = earthRadius * c;
        return d * 1000;
    }

    public static boolean isLocationEnabled(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            return lm.isLocationEnabled();
        } else {
            int mode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE,
                    Settings.Secure.LOCATION_MODE_OFF);
            return (mode != Settings.Secure.LOCATION_MODE_OFF);
        }
    }

    public static void displayLocationServicesRequest(Activity activity) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(activity);
        alertDialog.setTitle(activity.getString(R.string.enable_location_title));
        alertDialog.setMessage(activity.getString(R.string.enable_location_description));
        alertDialog.setPositiveButton(activity.getString(R.string.enable_location_positive_response), (dialog, which) -> {
            activity.startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
        });
        alertDialog.setNegativeButton(activity.getString(R.string.negative_response), (dialog, which) -> dialog.cancel());
        alertDialog.show();
    }
}
