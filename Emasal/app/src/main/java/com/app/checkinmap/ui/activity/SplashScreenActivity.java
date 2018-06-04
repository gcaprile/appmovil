package com.app.checkinmap.ui.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.app.checkinmap.R;
import com.app.checkinmap.util.ApiManager;
import com.app.checkinmap.util.AppStatus;
import com.app.checkinmap.util.NetworkUtilsTask;
import com.app.checkinmap.util.OnNetworkListener;
import com.app.checkinmap.util.PreferenceManager;
import com.app.checkinmap.util.Utility;
import com.salesforce.androidsdk.rest.RestClient;
import com.salesforce.androidsdk.ui.SalesforceActivity;

import org.json.JSONException;
import org.json.JSONObject;

public class SplashScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /**
         * Here we check if is the first time
         * in the app
         */
        if(PreferenceManager.getInstance(this).isFirstTime()){
            PreferenceManager.getInstance(this).setLastCleanDate(Utility.getCurrentDate());
            PreferenceManager.getInstance(this).setFirstTime(false);
        }
        new NetworkUtilsTask(this, new OnNetworkListener() {
            @Override
            public void onNetwork(boolean success, String message) {
                if(success){
                    startActivity(SalesForceLoginActivity.getIntent(getApplicationContext()));
                }else{
                    startActivity(NoInternetActivity.getIntent(getApplicationContext()));
                }
                finish();
            }
        }).execute();
    }

}
