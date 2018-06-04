package com.app.checkinmap.util;

import android.os.AsyncTask;
import android.util.Log;

import com.app.checkinmap.R;
import com.app.checkinmap.model.CheckPointLocation;
import com.app.checkinmap.model.Route;
import com.app.checkinmap.model.UserLocation;
import com.app.checkinmap.model.UserLog;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * This class help us to clean the database using
 * the custom settings value.
 */

public class CleanDatabaseAsyncTask extends AsyncTask<Void,Void,Void> {
    private  OnDatabaseCleanListener mListener;
    private  int mErasedItems=0;
    private  int mMessageResource= R.string.data_base_cleaned;

    public CleanDatabaseAsyncTask(OnDatabaseCleanListener listener){
        mListener = listener;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        Realm realm = Realm.getDefaultInstance();
        try {
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {

                    //Here we get the CheckPointLocations
                    RealmResults<CheckPointLocation> resultVisit = realm.where(CheckPointLocation.class).findAll();
                    if(resultVisit!=null){
                        if(resultVisit.size()>0){
                            for (CheckPointLocation checkpointlocation: resultVisit) {
                                checkpointlocation.deleteFromRealm();
                                mErasedItems++;
                            }
                        }
                    }

                    //UserLocation
                    RealmResults<UserLocation> resultLocation = realm.where(UserLocation.class).findAll();
                    if(resultLocation!=null){
                        if(resultLocation.size()>0){
                            for (UserLocation userLocation: resultLocation) {
                                userLocation.deleteFromRealm();
                                mErasedItems++;
                            }
                        }
                    }

                    //Route
                    RealmResults<Route> resultRoute = realm.where(Route.class).findAll();
                    if(resultRoute!=null){
                        if(resultRoute.size()>0){
                            for (Route route: resultRoute) {
                                route.deleteFromRealm();
                                mErasedItems++;
                            }
                        }
                    }

                    //UserLog
                    RealmResults<UserLog> resultLog = realm.where(UserLog.class).findAll();
                    if(resultLog!=null){
                        if(resultLog.size()>0){
                            for (UserLog userLog: resultLog) {
                                userLog.deleteFromRealm();
                                mErasedItems++;
                            }
                        }
                    }
                }
            });
        }catch (Exception e){
            mMessageResource= R.string.error_cleaning_data_base;
        }finally {
            if(realm!=null){
                realm.close();
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        mListener.onDatabaseClean(mMessageResource,mErasedItems);
    }

    /**
     * This method help us to determinate if we have
     * to delete the current row
     */
    /*private boolean toDelete(String date){
        boolean flag = false;

        //Here we check if we have to clean the data base
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

        try {
            Date registerDate = sdf.parse(date);
            Date currentDate = sdf.parse(Utility.getCurrentDate());

            //Here we calculate the day between days
            long daysPassed =  Math.abs((registerDate.getTime()-currentDate.getTime())/86400000);

            if(daysPassed >= mHistoricalDays){
                flag = true;
            }
        } catch (ParseException e) {
           flag = false;
        }
        return flag;
    }*/
}
