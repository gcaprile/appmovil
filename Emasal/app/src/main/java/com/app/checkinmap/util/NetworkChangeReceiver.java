package com.app.checkinmap.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.app.checkinmap.R;
import com.app.checkinmap.db.DatabaseManager;

/**
 * This class listen the change in the network
 * connection
 */

public class NetworkChangeReceiver extends BroadcastReceiver{
    public static final String TAG= NetworkChangeReceiver.class.getName();

    @Override
    public void onReceive(final Context context, Intent intent) {

        Utility.logLargeString("Mira la acción: "+intent.getAction());

        int status = NetworkUtil.getConnectivityStatus(context);

        if (intent.getAction().matches("android.net.wifi.WIFI_STATE_CHANGED")) {
            if(status==NetworkUtil.NETWORK_STATUS_NOT_CONNECTED){
                Log.e(TAG, "Sin conexión");
                 /*Here we track the user action*/
                PreferenceManager.getInstance(context).setNetworkState("Desconectado");
                DatabaseManager.getInstance().saveUserAction(context,context.getString(R.string.device_no_connected));
            }else{
                 /*Here we track the user action*/
                PreferenceManager.getInstance(context).setNetworkState("Conectado");

                /*Here we track the user action*/
                switch (status){
                    case NetworkUtil.NETWORK_STATUS_WIFI:
                        Log.e(TAG, "Conexión establecida con wifi");
                        DatabaseManager.getInstance().saveUserAction(context,context.getString(R.string.wifi_enable));
                        break;
                    case NetworkUtil.NETWORK_STATUS_MOBILE:
                        Log.e(TAG, "Conexión establecida con datos moviles");
                        DatabaseManager.getInstance().saveUserAction(context,context.getString(R.string.mobile_data_enable));
                        break;
                }
            }
        }
    }
}
