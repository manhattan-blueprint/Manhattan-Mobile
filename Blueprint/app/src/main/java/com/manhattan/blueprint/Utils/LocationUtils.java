package com.manhattan.blueprint.Utils;

import android.content.Context;
import android.location.LocationManager;
import android.os.Build;
import android.provider.Settings;

import com.google.android.gms.maps.model.LatLng;

public class LocationUtils {
    // Formula from https://stackoverflow.com/a/11172685/5310315
    public static double distanceBetween(LatLng a, LatLng b) {
        double earthRadius = 6378.137;
        double dLat = b.latitude * Math.PI / 180 - a.latitude * Math.PI / 180;
        double dLong = b.longitude * Math.PI / 180 - a.longitude * Math.PI / 180;

        double alpha = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(a.latitude * Math.PI / 180) *
                        Math.cos(b.latitude * Math.PI / 180) *
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
}
