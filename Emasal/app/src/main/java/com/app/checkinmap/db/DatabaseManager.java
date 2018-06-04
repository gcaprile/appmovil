package com.app.checkinmap.db;

import android.content.Context;
import android.util.Log;

import com.app.checkinmap.R;
import com.app.checkinmap.model.CheckPointData;
import com.app.checkinmap.model.CheckPointLocation;
import com.app.checkinmap.model.Route;
import com.app.checkinmap.model.UserLocation;
import com.app.checkinmap.model.UserLog;
import com.app.checkinmap.util.PreferenceManager;
import com.app.checkinmap.util.Utility;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;

public class DatabaseManager {

    private static DatabaseManager mInstance;

    public static DatabaseManager getInstance(){
        if(mInstance == null){
            mInstance = new DatabaseManager();
        }

        return mInstance;
    }

    /**
     * This method get all the user location saved
     * in local storage by route
     */
    public List<UserLocation> getUserLocationList(long routeId){
        Realm realm = Realm.getDefaultInstance();
        RealmResults<UserLocation> query = realm.where(UserLocation.class).equalTo("routeId",routeId).findAll();
        List<UserLocation> userLocationList = null;
        if(query != null){
            userLocationList = realm.copyFromRealm(query);
        }
        realm.close();
        return userLocationList;
    }


    /**
     * This method get all the user location saved
     * in local storage
     */
    public List<UserLocation> getUserLocationList(){
        Realm realm = Realm.getDefaultInstance();
        RealmResults<UserLocation> query = realm.where(UserLocation.class).findAll();
        List<UserLocation> userLocationList = null;
        if(query != null){
            userLocationList = realm.copyFromRealm(query);
        }
        realm.close();
        return userLocationList;
    }

    /**
     * This method help us to get all the check point
     * from data base from a specific route
     */
    public List<CheckPointLocation> getCheckPointLocationList(long routeId){
        Realm realm = Realm.getDefaultInstance();
        RealmResults<CheckPointLocation> query = realm.where(CheckPointLocation.class).equalTo("routeId",routeId ).findAll();
        List<CheckPointLocation> checkPointLocationList = null;
        if(query != null){
            checkPointLocationList = realm.copyFromRealm(query);
        }
        realm.close();
        return checkPointLocationList;
    }


    /**
     * This method help us to get all the check point
     * from data base
     */
    public List<CheckPointLocation> getAllCheckPointLocation(){
        Realm realm = Realm.getDefaultInstance();
        RealmResults<CheckPointLocation> query = realm.where(CheckPointLocation.class).findAll();
        List<CheckPointLocation> checkPointLocationList = null;
        if(query != null){
            checkPointLocationList = realm.copyFromRealm(query);
        }
        realm.close();
        return checkPointLocationList;
    }


    /**
     * This method help us to get all the user action
     * from data base
     */
    public List<UserLog> getAllUserActions(){
        Realm realm = Realm.getDefaultInstance();
        RealmResults<UserLog> query = realm.where(UserLog.class).findAll();
        List<UserLog> userLogList = null;
        if(query != null){
            userLogList = realm.copyFromRealm(query);
        }
        realm.close();
        return userLogList;
    }

    /**
     * This method help us to get all the routes
     * from data base
     */
    public List<Route> getAllRoutes(){
        Realm realm = Realm.getDefaultInstance();
        RealmResults<Route> query = realm.where(Route.class).findAll();
        List<Route> routesList = null;
        if(query != null){
            routesList = realm.copyFromRealm(query);
        }
        realm.close();
        return routesList;
    }


    /**
     * This method help us to get the correlative
     * to create the route name
     */
    public int getCorrelativeRoute(String date){
        int correlative = 1;

        Realm realm = Realm.getDefaultInstance();
        RealmResults<Route> query = realm.where(Route.class).contains("startDate",date).findAll();
        List<Route> routeList = null;
        if(query != null){
            routeList = realm.copyFromRealm(query);
            if(routeList.size()>0){
                correlative = routeList.size() +1;
                Log.d("Correlativo creado",String.valueOf(correlative));
            }
        }
        realm.close();

        return correlative;
    }

    /**
     * This method help us to get the correlative
     * to create the check point name
     */
    public int getCorrelativeCheckPoint(long routeId){
        int correlative = 1;

        Realm realm = Realm.getDefaultInstance();
        RealmResults<CheckPointLocation> query = realm.where(CheckPointLocation.class).equalTo("routeId",routeId).findAll();
        List<CheckPointLocation> checkPointList = null;
        if(query != null){
            checkPointList = realm.copyFromRealm(query);
            if(checkPointList.size()>0){
                correlative = checkPointList.size() +1;
                Log.d("Check point c ",String.valueOf(correlative));
            }
        }
        realm.close();

        return correlative;
    }


    /**
     * This method help us to get the start date for the
     * travel in a visit
     */
    public String getTravelStartDate(long routeId){
        String startDate = "";

        Realm realm = Realm.getDefaultInstance();
        RealmResults<CheckPointLocation> query = realm.where(CheckPointLocation.class).equalTo("routeId",routeId).findAll();
        List<CheckPointLocation> checkPointList = null;
        if(query != null){
            checkPointList = realm.copyFromRealm(query);
            if(checkPointList.size()>0){
                startDate = checkPointList.get(checkPointList.size()-1).getCheckOutDate();
            }else{
                Route route = realm.where(Route.class).equalTo("id",routeId).findFirst();
                if(route!=null){
                    startDate = route.getStartDate();
                }
            }
        }
        realm.close();

        return startDate;
    }


    /**
     * This method help us to get all the check point
     * from data base from a specific route
     */
    public CheckPointLocation getCheckPointLocation(long routeId){
        CheckPointLocation checkPointLocation = new CheckPointLocation();
        Realm realm = Realm.getDefaultInstance();
        CheckPointLocation checkPointLocationQuery = realm.where(CheckPointLocation.class).equalTo("id",routeId ).findFirst();
        if(checkPointLocationQuery!=null){
            checkPointLocation = realm.copyFromRealm(checkPointLocationQuery);
        }
        realm.close();
        return checkPointLocation;
    }

    /**
     * This method help us to check if we have a
     * current route in data base
     */
    public Route getStartedRoute(){
        Route route = null;
        Realm realm = Realm.getDefaultInstance();
        Route routeQuery = realm.where(Route.class).equalTo("status","Iniciada" ).findFirst();
        if(routeQuery!=null){
            route = realm.copyFromRealm(routeQuery);
        }
        realm.close();
        return route;
    }

    /**
     * This method help us to get all the check point
     * from data base from a specific route
     */
    public String getRouteName(long id){
        Route route = new Route();
        Realm realm = Realm.getDefaultInstance();
        Route routeQuery = realm.where(Route.class).equalTo("id",id ).findFirst();
        if(routeQuery!=null){
            route = realm.copyFromRealm(routeQuery);
        }
        realm.close();
        return route.getName();
    }


    /**
     * This method help us to get the rout start date
     */
    public String getRouteStartDate(long id){
        Route route = new Route();
        Realm realm = Realm.getDefaultInstance();
        Route routeQuery = realm.where(Route.class).equalTo("id",id ).findFirst();
        if(routeQuery!=null){
            route = realm.copyFromRealm(routeQuery);
        }
        realm.close();
        return route.getStartDate();
    }


    /**
     * This method help us to get the route end date
     */
    public String getRouteEndDate(long id){
        Route route = new Route();
        Realm realm = Realm.getDefaultInstance();
        Route routeQuery = realm.where(Route.class).equalTo("id",id ).findFirst();
        if(routeQuery!=null){
            route = realm.copyFromRealm(routeQuery);
        }
        realm.close();
        return route.getEndDate();
    }

    /**
     * This method help us to delete all the data about
     * current route
     */
    public void deleteCurrentRouteData(final Context context){
        Realm realm = Realm.getDefaultInstance();
        try {
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {

                    /*Here we delete all the registered visits */
                    RealmResults<CheckPointLocation> checkPointLocations = realm.where(CheckPointLocation.class).equalTo("routeId",PreferenceManager.getInstance(context).getRouteId()).findAll();
                    checkPointLocations.deleteAllFromRealm();

                    /*Here we delete all the registered user location */
                    RealmResults<UserLocation> userLocations = realm.where(UserLocation.class).equalTo("routeId",PreferenceManager.getInstance(context).getRouteId()).findAll();
                    userLocations.deleteAllFromRealm();

                    /*Here we delete all the registered routes */
                    RealmResults<Route> routes = realm.where(Route.class).equalTo("id",PreferenceManager.getInstance(context).getRouteId()).findAll();
                    routes.deleteAllFromRealm();
                    Log.d("REALM"," limpieza rutas no finalizadas");
                }
            });
        }catch (Exception e){
           Log.d("Error Realm Emasal",e.getMessage());
        }finally {
            if(realm!=null){
                realm.close();
            }
        }
    }

    /**
     * This method help us to save the user
     * action in the app
     */
    public void saveUserAction(final Context context, final String userAction){
        Realm realm = Realm.getDefaultInstance();
        try {
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    UserLog userLog = new UserLog();
                    userLog.setId(System.currentTimeMillis());
                    userLog.setUserAction(userAction + "; "+Utility.getPhoneStatusData(context));
                    userLog.setDate(Utility.getCurrentDateWithTime());
                    userLog.setUserId(Utility.getRestClient().getClientInfo().userId);
                    userLog.setRouteId(PreferenceManager.getInstance(context).getRouteId());

                    realm.copyToRealmOrUpdate(userLog);

                    Log.d("REALM"," User action saved");
                }
            });
        }catch (Exception e){
            Log.d("Error Realm Emasal",e.getMessage());
        }finally {
            if(realm!=null){
                realm.close();
            }
        }
    }

    /**
     * This method help us to check if exist a current
     * active route
     */
    public boolean checkStartedRoute(){
       boolean exist=false;
        Realm realm = Realm.getDefaultInstance();
        Route routeQuery = realm.where(Route.class).equalTo("status","Iniciada" ).findFirst();
        if(routeQuery!=null){
           exist=true;
        }
        realm.close();
        return exist;
    }

    /**
     * This method help us to clean the database
     */
    public void cleanDataBase(){
        Realm realm = Realm.getDefaultInstance();
        try {
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {

                    //CheckPointLocation
                    RealmResults<CheckPointLocation> resultVisit = realm.where(CheckPointLocation.class).findAll();
                    resultVisit.deleteAllFromRealm();

                    //UserLocation
                    RealmResults<UserLocation> resultLocation = realm.where(UserLocation.class).findAll();
                    resultLocation.deleteAllFromRealm();

                    //Route
                    RealmResults<Route> resultRoute = realm.where(Route.class).findAll();
                    resultRoute.deleteAllFromRealm();

                    //UserLog
                    RealmResults<UserLog> resultLog = realm.where(UserLog.class).findAll();
                    resultLog.deleteAllFromRealm();

                    Log.d("REALM","Data base cleaned");
                }
            });
        }catch (Exception e){
            Log.d("Error Realm Emasal",e.getMessage());
        }finally {
            if(realm!=null){
                realm.close();
            }
        }
    }
}
