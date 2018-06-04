package com.app.checkinmap.ui.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.app.checkinmap.R;
import com.app.checkinmap.db.DatabaseManager;
import com.app.checkinmap.model.CustomSettingsResponse;
import com.app.checkinmap.model.UserProfileResponse;
import com.app.checkinmap.util.ApiManager;
import com.app.checkinmap.util.CleanDatabaseAsyncTask;
import com.app.checkinmap.util.NetworkUtilsTask;
import com.app.checkinmap.util.OnDatabaseCleanListener;
import com.app.checkinmap.util.OnNetworkListener;
import com.app.checkinmap.util.PreferenceManager;
import com.app.checkinmap.util.Utility;
import com.google.gson.Gson;
import com.salesforce.androidsdk.rest.RestClient;
import com.salesforce.androidsdk.ui.SalesforceActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SalesForceLoginActivity extends SalesforceActivity {
    private boolean mIsFirstTime=true;

    @BindView(R.id.root)
    LinearLayout mLnlMainView;

    @BindView(R.id.progress_bar)
    ProgressBar mProgressBar;

    @BindView(R.id.text_view_message)
    TextView     mTxvMessage;

    /**
     * This method get a single intent in order to start the
     * Sales Force login activity.
     */
    public static Intent getIntent(Context context){
        return new Intent(context,SalesForceLoginActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sales_force_login);
        ButterKnife.bind(this);

        /*Here we check if a session exist to refresh the token*/
        Utility.checkSFSession(this);
    }

    @Override
    public void onResume() {
        if( mLnlMainView.getVisibility()!=View.VISIBLE){
            mLnlMainView.setVisibility(View.INVISIBLE);
        }
        super.onResume();
    }

    @Override
    public void onResume(RestClient client) {
        if(mIsFirstTime){
            mIsFirstTime= false;
            Utility.setRestClient(client);

             /*Here we track the user action*/
            if(!PreferenceManager.getInstance(this).isPreviousSession()){
                PreferenceManager.getInstance(this).setPreviousSession(true);
                DatabaseManager.getInstance().saveUserAction(this,getString(R.string.login_success));
            }
            mLnlMainView.setVisibility(View.VISIBLE);
            mTxvMessage.setText(R.string.text_getting_user_information);
            getUserProfileData();
        }
    }

    /**
     * This method help us to get the user profile
     */
    public void getUserProfileData(){
        new NetworkUtilsTask(this, new OnNetworkListener() {
            @Override
            public void onNetwork(boolean success, String message) {
                if(success){
                    if (Utility.getRestClient() != null) {
                        RestClient.ClientInfo clientInfo = Utility.getRestClient().getClientInfo();

                        String osql = "SELECT User.id, User.Email, User.FirstName, User.LastName, User.profile.id, User.profile.name, User.Username, " +
                                "User.Country, User.IsActive FROM User, User.profile WHERE User.id = '" + clientInfo.userId + "'";

                        ApiManager.getInstance().getJSONObject(getCurrentContext(), osql, new ApiManager.OnObjectListener() {
                            @Override
                            public void onObject(boolean success, JSONObject jsonObject, String errorMessage) {
                                if (success) {
                                    /*Here we get the data from the response*/
                                    UserProfileResponse response = new Gson().fromJson(jsonObject.toString(),
                                                   UserProfileResponse.class);

                                    /*Save the user data*/
                                    Utility.setUserProfileId(response.getUsers().get(0).getProfile().getId());
                                    Utility.setUserProfileName(response.getUsers().get(0).getProfile().getName());
                                    Utility.setUserCountry(response.getUsers().get(0).getCountry());

                                    /*Here we track the user action*/
                                    DatabaseManager.getInstance().saveUserAction(getApplicationContext(),getString(R.string.user_profile_success));

                                    /*This method help us to get the custom settings*/
                                    mTxvMessage.setText( R.string.text_getting_user_costume_settings);
                                    getCustomSettings();
                                } else {
                                    mProgressBar.setVisibility(View.GONE);
                                    mTxvMessage.setText( R.string.no_user_profile);
                                    DatabaseManager.getInstance().saveUserAction(getCurrentContext(),getString( R.string.no_user_profile));
                                }
                            }
                        });
                    } else {
                        mProgressBar.setVisibility(View.GONE);
                        mTxvMessage.setText( R.string.no_user_profile);
                        DatabaseManager.getInstance().saveUserAction(getCurrentContext(),getString( R.string.no_user_profile));
                    }
                }else{
                    startActivity(NoInternetActivity.getIntent(getApplicationContext()));
                }
            }
        }).execute();
    }

    /**
     * This method help us to get the custom settings to
     * user with the current user
     */
    public void getCustomSettings(){
       new NetworkUtilsTask(this, new OnNetworkListener() {
           @Override
           public void onNetwork(boolean success, String message) {
               if(success){
                   String osql = "SELECT Id, Name, Radio_Check_In__c, Intervalo_en_segundos__c, Dias_Historico_App__c FROM Aplicacion_Movil_EMASAL__c";
                   ApiManager.getInstance().getJSONObject(getCurrentContext(), osql, new ApiManager.OnObjectListener() {
                       @Override
                       public void onObject(boolean success, JSONObject jsonObject, String errorMessage) {
                            if(success){
                                Utility.logLargeString(jsonObject.toString());
                                /*Here we get the custom settings for user configuration*/
                                final CustomSettingsResponse response = new Gson().fromJson(jsonObject.toString(),CustomSettingsResponse.class);

                                /*Here we save the custom settings*/
                                Utility.setRadioCheckIn(response.getSettings().get(0).getRadioCheckIn());
                                Utility.setIntervalSeconds(response.getSettings().get(0).getIntervalInSeconds()* 1000);
                                PreferenceManager.getInstance(getApplicationContext()).setLocationIntervalUpdate(response.getSettings().get(0).getIntervalInSeconds()* 1000);

                                 /*Here we track the user action*/
                                DatabaseManager.getInstance().saveUserAction(getApplicationContext(),getString(R.string.custom_setting_success));

                               /*Here we clean the data base if apply*/
                                startActivity(DashBoardActivity.getIntent(getCurrentContext()));
                                finish();
                            }else{
                                 /*Here we track the user action*/
                                DatabaseManager.getInstance().saveUserAction(
                                        getApplicationContext(),getString(R.string.no_custom_settings));

                                mProgressBar.setVisibility(View.GONE);
                                mTxvMessage.setText( R.string.no_custom_settings);
                            }
                       }
                   });
               }else{
                   startActivity(NoInternetActivity.getIntent(getApplicationContext()));
                   finish();
               }
           }
       }).execute();
    }

    /**
     * This method get the current context
     */
    public Activity getCurrentContext(){
        return this;
    }


    /**
     * This class help us to clean the database
     */
    public void cleanDataBase(int days){
        try {
            //Here we check if we have to clean the data base
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            Date oldCleanDate = sdf.parse(PreferenceManager.getInstance(this).getLastCleanDate());
            Date currentDate = sdf.parse(Utility.getCurrentDate());

            //Here we calculate the day between days
            long daysPassed =  Math.abs((oldCleanDate.getTime()-currentDate.getTime())/86400000);

            if(daysPassed > days){
                if(!PreferenceManager.getInstance(this).isInRoute()){
                    DatabaseManager.getInstance().cleanDataBase();
                    PreferenceManager.getInstance(this).setLastCleanDate(Utility.getCurrentDate());
                }
            }
        } catch (ParseException e) {
            DatabaseManager.getInstance().saveUserAction(getCurrentContext(),getString(R.string.error_cleaning_data_base));
        }
    }
}
