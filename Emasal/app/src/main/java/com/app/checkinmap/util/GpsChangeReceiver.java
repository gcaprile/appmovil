package com.app.checkinmap.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.util.Log;

import com.app.checkinmap.R;
import com.app.checkinmap.db.DatabaseManager;

import static android.content.Context.LOCATION_SERVICE;

/**
 * This method help us to check when the
 * GPS is disable
 */

public class GpsChangeReceiver extends BroadcastReceiver {
    public static final String TAG=GpsChangeReceiver.class.getName();
    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent.getAction().matches("android.location.PROVIDERS_CHANGED")) {
            LocationManager locationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);
            /*Here we check if the GPS is disable*/
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                Log.i(TAG, "GPS fue activado");
                /*Here we track the user action*/
                DatabaseManager.getInstance().saveUserAction(context,context.getString(R.string.gps_enable));
                PreferenceManager.getInstance(context).setGpsState("Activado");
            } else {
                Log.i(TAG, "GPS fue desactivado");
                /*Here we track the user action*/
                DatabaseManager.getInstance().saveUserAction(context,context.getString(R.string.gps_disable));
                PreferenceManager.getInstance(context).setGpsState("Desactivado");
            }
        }
    }
}
