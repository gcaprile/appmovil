package com.app.checkinmap.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;

import com.app.checkinmap.R;
import com.app.checkinmap.db.DatabaseManager;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;


/**
 * This class help us to check if we have
 * internet connection.
 */

public class NetworkUtilsTask extends AsyncTask<Void,Void,Void> {
    private final static String TAG = NetworkUtilsTask.class.getName();
    private Context mContext;
    private boolean mIsSuccess=false;
    private String  mMessage="";
    private OnNetworkListener mListener;

    public NetworkUtilsTask(Context context, OnNetworkListener listener){
        mContext= context;
        mListener= listener;
    }
    @Override
    protected Void doInBackground(Void... params) {
        if (isNetworkAvailable()) {
            try {
                HttpURLConnection urlc = (HttpURLConnection) new URL("http://clients3.google.com/generate_204").openConnection();
                urlc.setRequestProperty("User-Agent", "Android");
                urlc.setRequestProperty("Connection", "close");
                urlc.setConnectTimeout(1500);
                urlc.connect();
                if(urlc.getResponseCode() == 204 && urlc.getContentLength() == 0){
                    mIsSuccess=true;
                }else{
                    mMessage=  mContext.getString(R.string.text_no_internet_connection);
                }
            } catch (IOException e) {
                Log.e(TAG, "Error checking internet connection", e);
                mMessage = mContext.getString(R.string.text_error_checking_internet_connection);
            }
        } else {
            Log.d(TAG, "No network available!");
            mMessage = mContext.getString(R.string.text_no_network_available);
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        if(mIsSuccess){
            PreferenceManager.getInstance(mContext).setInternetConnection("Si");
            mListener.onNetwork(true,"");
        }else {
            PreferenceManager.getInstance(mContext).setInternetConnection("No");
            DatabaseManager.getInstance().saveUserAction(mContext,mMessage);
            mListener.onNetwork(false,mMessage);
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnected();
    }
}
