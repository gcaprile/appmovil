package com.app.checkinmap.service;


import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.GeomagneticField;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import com.app.checkinmap.R;
import com.app.checkinmap.bus.BusProvider;
import com.app.checkinmap.bus.NewLocationEvent;
import com.app.checkinmap.db.DatabaseManager;
import com.app.checkinmap.model.UserLocation;
import com.app.checkinmap.util.PreferenceManager;
import com.app.checkinmap.util.Utility;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import io.realm.Realm;
import io.realm.RealmResults;

public class LocationService extends Service{

    private NewLocationEvent            mNewLocationEventBus;
    private LocationRequest             mLocationRequest;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationCallback            mLocationCallback;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        //Here we create the variables
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mNewLocationEventBus = new NewLocationEvent();

        //Here we create the location request settings
        createLocationCallback();
        createLocationRequest();

        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * This method help us to create the location result
     * callback
     */
    private void createLocationCallback(){
        mLocationCallback = new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }

                //Here we update the location
               if(locationResult.getLastLocation()!=null){

                   //Here we notify to the system the new location
                   mNewLocationEventBus.setLat(locationResult.getLastLocation().getLatitude());
                   mNewLocationEventBus.setLon(locationResult.getLastLocation().getLongitude());
                   mNewLocationEventBus.setBearing(locationResult.getLastLocation().getBearing());
                   BusProvider.getInstance().post(mNewLocationEventBus);

                   //Here we check if we have to save the current location in the database
                   if(PreferenceManager.getInstance(getApplicationContext()).isInRoute()){
                       if(!PreferenceManager.getInstance(getApplicationContext()).isDoingCheckIn()){
                           saveLocation(locationResult.getLastLocation());
                       }
                   }
               }
            }
        };
    }

    /**
     * This method create the location request
     * settings
     */
    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(PreferenceManager.getInstance(this).getLocationIntervalUpdate());
        mLocationRequest.setFastestInterval(PreferenceManager.getInstance(this).getLocationIntervalUpdate()/2);
        //mLocationRequest.setSmallestDisplacement(5);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        
        //Here we start the location updated
        startLocationUpdates();
    }

    /**
     * This method help us to start the location updated
     */
    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                mLocationCallback,
                null /* Looper */);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
    }


    /**
     * This method help us to save the current location
     * in the database
     */
    private void saveLocation(final Location location){
        Realm realm = null;

        try{

            realm = Realm.getDefaultInstance();
            realm.executeTransaction(new Realm.Transaction(){
                @Override
                public void execute(Realm realm) {
                    //Here we set the flag to save or not the current route
                    boolean saveInDataBase=true;


                    //Here we define the last user location object
                    double distanceA = 0.00;
                    double distanceB = 0.00;
                    double distanceC = 0.00;

                    //Here we get the last user location
                    RealmResults<UserLocation> allUserLocations = realm.where(UserLocation.class).
                            equalTo("routeId",PreferenceManager.getInstance(getApplicationContext()).getRouteId()).findAll();

                    if(allUserLocations!=null){

                        if(allUserLocations.size()>0){
                            //Here we get the last user location
                            UserLocation userLocation = allUserLocations.last();

                            //Here we get the short distance with the new location
                            Location lastLocation = new Location("LastPoint");
                            lastLocation.setLatitude(userLocation.getLatitude());
                            lastLocation.setLongitude(userLocation.getLongitude());

                            distanceA = userLocation.getDistance() + (lastLocation.distanceTo(location)/1000.00);

                            //Here we calculate the distance B
                            float[] values = new float[3];
                            Location.distanceBetween(lastLocation.getLatitude(),lastLocation.getLongitude(),
                                    location.getLatitude(),location.getLongitude(),values);

                            distanceB = userLocation.getDistanceB() + (values[0]/1000.00);

                            //Here we calculate the distance c
                            distanceC = userLocation.getDistanceC() + Utility.distanceInKilometers(lastLocation.getLatitude(),lastLocation.getLongitude(),
                                    location.getLatitude(),location.getLongitude());

                            try {
                                //Here we define the format date
                                DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.ENGLISH);

                                //Here we check if we have to save the current locations
                                Date lastLocationDate = dateFormat.parse(userLocation.getDate());
                                Date currentDate =  dateFormat.parse(Utility.getCurrentDate());

                                if(currentDate.compareTo(lastLocationDate)==0){
                                    saveInDataBase=false;
                                    Utility.logLargeString("Location date equal: no save");
                                }else{
                                    Utility.logLargeString("Location date ar not equal: save");
                                }

                                //Here we refresh the token if apply
                                Date refresTokenDate =  dateFormat.parse(getRefreshTokenDate());
                                long diffInMillies = Math.abs(currentDate.getTime()- refresTokenDate.getTime());
                                long diff = TimeUnit.MINUTES.convert(diffInMillies, TimeUnit.MILLISECONDS);

                                if(diff>=15){
                                    refreshToken();
                                    refreshTokenFlag(true);
                                }else{
                                    refreshTokenFlag(false);
                                }

                            } catch (ParseException e) {
                                e.printStackTrace();
                                saveInDataBase=true;
                            }
                        }else{
                            setRefreshTokenDate();
                        }
                    }else{
                        setRefreshTokenDate();
                    }

                    if(saveInDataBase){
                        UserLocation userLocation = new UserLocation();
                        userLocation.setId(System.currentTimeMillis());
                        userLocation.setLatitude(location.getLatitude());
                        userLocation.setLongitude(location.getLongitude());
                        userLocation.setAccuracy(location.getAccuracy());
                        userLocation.setDate(Utility.getCurrentDate());
                        userLocation.setRouteId(PreferenceManager.getInstance(getApplicationContext()).getRouteId());
                        userLocation.setDistance(distanceA);
                        userLocation.setDistanceB(distanceB);
                        userLocation.setDistanceC(distanceC);

                        realm.copyToRealmOrUpdate(userLocation);
                        Log.d("REALM", "SUCCESS");
                    }
                }
            });
        }catch(Exception e){
            Log.d("REALM ERROR", e.toString());
        }finally {
            if(realm != null){
                realm.close();
                refreshTokenLog();
            }
        }
    }

    /**
     * This method help us to save the
     * refresh date token
     */
    private void setRefreshTokenDate(){
        PreferenceManager.getInstance(this).setRefreshDate(Utility.getCurrentDate());
    }

    /**
     * This method help us to get the refresh token date
     */
    private String getRefreshTokenDate(){
        return PreferenceManager.getInstance(this).getRefreshDate();
    }

    /**
     * This method help us to refresh the toke if
     * as session exist
     */
    private void refreshToken(){
        Utility.checkSFSession(this);
        setRefreshTokenDate();
    }

    /**
     * This method help us to set the refresh token flag
     */
    private void refreshTokenFlag(boolean state){
        PreferenceManager.getInstance(this).setRefreshToken(state);
    }

    /**
     * This method help us to save the refresh token action
     */
    private void refreshTokenLog(){
        if(PreferenceManager.getInstance(this).isRefreshToken()){
            DatabaseManager.getInstance().saveUserAction(this,getString(R.string.user_token_updated));
            refreshTokenFlag(false);
        }
    }
}
