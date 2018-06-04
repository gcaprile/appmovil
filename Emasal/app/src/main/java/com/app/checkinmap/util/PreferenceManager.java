package com.app.checkinmap.util;


import android.content.Context;
import android.content.SharedPreferences;

public class PreferenceManager {

    private static final String PREFERENCES_NAME=                       "com.app.checkinmap.preferences";
    private static final String PREFERENCE_SERVICES_STARTED =           "service_started";
    private static final String PREFERENCE_IS_SELLER =                  "is_seller";
    private static final String PREFERENCE_IS_IN_ROUTE =                "is_in_route";
    private static final String PREFERENCE_IS_DOING_CHECK_IN =          "is_doing_check_in";
    private static final String PREFERENCE_ROUTE_ID =                   "route_id";
    private static final String PREFERENCE_RADIUS =                     "radius";
    private static final String PREFERENCE_CHECK_POINT_DATA =           "check_point_data";
    private static final String PREFERENCE_CHECK_POINT_LOCATION =       "check_point_location";
    private static final String PREFERENCE_LOCATION_INTERVAL_UPDATE =   "location_interval_update";
    private static final String PREFERENCE_START_ODOMETER =             "start_odometer";
    private static final String PREFERENCE_IS_FIRST_TIME =              "is_first_time";
    private static final String PREFERENCE_LAST_CLEAN_DATE =            "last_clean_date";
    private static final String PREFERENCE_NETWORK_STATE =              "network_state";
    private static final String PREFERENCE_GPS_STATE =                  "gps_state";
    private static final String PREFERENCE_INTERNET_CONNECTION =        "internet_connection";
    private static final String PREFERENCE_PREVIOUS_SESSION =           "previous_session";
    private static final String PREFERENCE_DATE_TO_REFRESH =            "date_to_refresh";
    private static final String PREFERENCE_REFRESH_TOKEN =              "refresh_token";

    private static PreferenceManager mInstance;
    private        SharedPreferences mPreferences;

    public static PreferenceManager getInstance(Context context){
        if(mInstance == null){
            mInstance = new PreferenceManager(context);
        }

        return mInstance;
    }

    private PreferenceManager(Context context){
        mPreferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
    }

    public void setIsServiceEnabled(boolean isStarted){
        mPreferences.edit().putBoolean(PREFERENCE_SERVICES_STARTED, isStarted).apply();
    }

    public boolean isServiceEnabled(){
        return mPreferences.getBoolean(PREFERENCE_SERVICES_STARTED, false);
    }

    public void setIsSeller(boolean isSeller){
        mPreferences.edit().putBoolean(PREFERENCE_IS_SELLER, isSeller).apply();
    }

    public boolean isSeller(){
        return mPreferences.getBoolean(PREFERENCE_IS_SELLER, true);
    }

    public void setIsInRoute(boolean isInRoute){
        mPreferences.edit().putBoolean(PREFERENCE_IS_IN_ROUTE, isInRoute).apply();
    }

    public boolean isInRoute(){
        return mPreferences.getBoolean(PREFERENCE_IS_IN_ROUTE, false);
    }

    public void setRouteId(long routeId){
        mPreferences.edit().putLong(PREFERENCE_ROUTE_ID, routeId).apply();
    }

    public long getRouteId(){
        return mPreferences.getLong(PREFERENCE_ROUTE_ID,0);
    }

    public void setRadius(int radius){
        mPreferences.edit().putInt(PREFERENCE_RADIUS, radius).apply();
    }

    public long getRadius(){
        return mPreferences.getInt(PREFERENCE_RADIUS,Utility.getRadioCheckIn());
    }

    public void setIsDoingCheckIn(boolean isDoingCheckIn){
        mPreferences.edit().putBoolean(PREFERENCE_IS_DOING_CHECK_IN, isDoingCheckIn).apply();
    }

    public boolean isDoingCheckIn(){
        return mPreferences.getBoolean(PREFERENCE_IS_DOING_CHECK_IN, false);
    }

    public void setCheckPointData(String checkPointData){
        mPreferences.edit().putString(PREFERENCE_CHECK_POINT_DATA, checkPointData).apply();
    }

    public String getCheckPointData(){
        return mPreferences.getString(PREFERENCE_CHECK_POINT_DATA, "");
    }

    public void setCheckPointLocation(String checkPointLocation){
        mPreferences.edit().putString(PREFERENCE_CHECK_POINT_LOCATION, checkPointLocation).apply();
    }

    public String getCheckPointLocation(){
        return mPreferences.getString(PREFERENCE_CHECK_POINT_LOCATION, "");
    }

    public void setLocationIntervalUpdate(int locationIntervalUpdate){
        mPreferences.edit().putInt(PREFERENCE_LOCATION_INTERVAL_UPDATE, locationIntervalUpdate).apply();
    }

    public int getLocationIntervalUpdate(){
        return mPreferences.getInt(PREFERENCE_LOCATION_INTERVAL_UPDATE, 10*100);
    }


    public void setStartOdometer(int startOdometer){
        mPreferences.edit().putInt(PREFERENCE_START_ODOMETER, startOdometer).apply();
    }

    public int getStartOdometer(){
        return mPreferences.getInt(PREFERENCE_START_ODOMETER, 0);
    }


    public boolean isFirstTime(){
        return mPreferences.getBoolean(PREFERENCE_IS_FIRST_TIME, true);
    }

    public void setFirstTime(boolean firstTime){
        mPreferences.edit().putBoolean(PREFERENCE_IS_FIRST_TIME, firstTime).apply();
    }

    public void setLastCleanDate(String lastCleanDate){
        mPreferences.edit().putString(PREFERENCE_LAST_CLEAN_DATE, lastCleanDate).apply();
    }

    public String getLastCleanDate(){
        return mPreferences.getString(PREFERENCE_LAST_CLEAN_DATE, "");
    }

    public void setNetworkState(String networkState){
        mPreferences.edit().putString(PREFERENCE_NETWORK_STATE, networkState).apply();
    }

    public String getNetworkState(){
        return mPreferences.getString(PREFERENCE_NETWORK_STATE, "Conectado");
    }

    public void setGpsState(String gpsState){
        mPreferences.edit().putString(PREFERENCE_GPS_STATE, gpsState).apply();
    }

    public String getGpsState(){
        return mPreferences.getString(PREFERENCE_GPS_STATE, "Activado");
    }


    public void setInternetConnection(String internetConnection){
        mPreferences.edit().putString(PREFERENCE_INTERNET_CONNECTION, internetConnection).apply();
    }

    public String getInternetConnection(){
        return mPreferences.getString(PREFERENCE_INTERNET_CONNECTION, "Si");
    }

    public boolean isPreviousSession(){
        return mPreferences.getBoolean(PREFERENCE_PREVIOUS_SESSION, false);
    }

    public void setPreviousSession(boolean previousSession){
        mPreferences.edit().putBoolean(PREFERENCE_PREVIOUS_SESSION, previousSession).apply();
    }

    public void setRefreshDate(String date){
        mPreferences.edit().putString(PREFERENCE_DATE_TO_REFRESH, date).apply();
    }

    public String getRefreshDate(){
        return mPreferences.getString(PREFERENCE_DATE_TO_REFRESH, "");
    }

    public boolean isRefreshToken(){
        return mPreferences.getBoolean(PREFERENCE_REFRESH_TOKEN, false);
    }

    public void setRefreshToken(boolean refreshToken){
        mPreferences.edit().putBoolean(PREFERENCE_REFRESH_TOKEN, refreshToken).apply();
    }
}
