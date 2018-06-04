package com.app.checkinmap.util;

import android.content.Context;
import android.os.AsyncTask;

import com.app.checkinmap.BuildConfig;
import com.app.checkinmap.R;
import com.app.checkinmap.db.DatabaseManager;
import com.app.checkinmap.model.CheckPointLocation;
import com.app.checkinmap.model.Route;
import com.app.checkinmap.model.UserLocation;
import com.app.checkinmap.model.UserLog;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ASUS-PC on 17/02/2018.
 */

public class BackupAsyncTask extends AsyncTask<Void,Void,Void> {
    private OnBackUpListener mListener;
    private Context mContext;
    private boolean mSuccess=false;
    private List<File> mBackUpFiles;
    private String mAditionalMessage="";

    public BackupAsyncTask(Context context,OnBackUpListener listener){
        mContext= context;
        mListener = listener;
        mBackUpFiles= new ArrayList<>();
    }

    @Override
    protected Void doInBackground(Void... voids) {
        try {
            //We prepare the file with all the routes in the device
            File routesFile= Utility.createBackUpFile("routs");
            FileOutputStream fileStreamRoutes = new FileOutputStream(routesFile);
            OutputStreamWriter  routeFileWriter = new OutputStreamWriter(fileStreamRoutes,"UTF-8");

            //Here we write the column names
            routeFileWriter.append("Id");
            routeFileWriter.append(",");
            routeFileWriter.append("Name");
            routeFileWriter.append(",");
            routeFileWriter.append("Start Date");
            routeFileWriter.append(",");
            routeFileWriter.append("End Date");
            routeFileWriter.append(",");
            routeFileWriter.append("Mileage");
            routeFileWriter.append(",");
            routeFileWriter.append("User Id");
            routeFileWriter.append(",");
            routeFileWriter.append("Type Id");
            routeFileWriter.append(",");
            routeFileWriter.append("Start Latitude");
            routeFileWriter.append(",");
            routeFileWriter.append("Start Longitude");
            routeFileWriter.append(",");
            routeFileWriter.append("End Latitude");
            routeFileWriter.append(",");
            routeFileWriter.append("End Longitude");
            routeFileWriter.append(",");
            routeFileWriter.append("Status");
            routeFileWriter.append(",");
            routeFileWriter.append("Sync");
            routeFileWriter.append(",");
            routeFileWriter.append("Version");
            routeFileWriter.append("\n");

            /*Here we get the data from the data base*/
            List<Route> routes = DatabaseManager.getInstance().getAllRoutes();

            for(Route route: routes){
                routeFileWriter.append(String.valueOf(route.getId()));
                routeFileWriter.append(",");
                routeFileWriter.append(route.getName());
                routeFileWriter.append(",");
                routeFileWriter.append(route.getStartDate());
                routeFileWriter.append(",");
                routeFileWriter.append(route.getEndDate());
                routeFileWriter.append(",");
                routeFileWriter.append(String.valueOf(route.getMileage()));
                routeFileWriter.append(",");
                routeFileWriter.append(route.getUserId());
                routeFileWriter.append(",");
                routeFileWriter.append(route.getTypeId());
                routeFileWriter.append(",");
                routeFileWriter.append(String.valueOf(route.getStartLatitude()));
                routeFileWriter.append(",");
                routeFileWriter.append(String.valueOf(route.getStartLongitude()));
                routeFileWriter.append(",");
                routeFileWriter.append(String.valueOf(route.getEndLatitude()));
                routeFileWriter.append(",");
                routeFileWriter.append(String.valueOf(route.getEndLongitude()));
                routeFileWriter.append(",");
                routeFileWriter.append(route.getStatus());
                routeFileWriter.append(",");
                routeFileWriter.append(route.getSync());
                routeFileWriter.append(",");
                routeFileWriter.append( BuildConfig.VERSION_NAME);
                routeFileWriter.append("\n");

            }
            /*Here we close the current route file*/
            routeFileWriter.close();

            /*Here we add the routes file*/
            mBackUpFiles.add(routesFile);

            //We prepare the file with all the visits in the device
            File visitsFile= Utility.createBackUpFile("visits");
            FileOutputStream fileStreamVisits = new FileOutputStream(visitsFile);
            OutputStreamWriter  visitFileWriter = new OutputStreamWriter(fileStreamVisits,"UTF-8");

            //Here we write the column names
            visitFileWriter.append("Id");
            visitFileWriter.append(",");
            visitFileWriter.append("CheckIn Latitude");
            visitFileWriter.append(",");
            visitFileWriter.append("CheckIn Longitude");
            visitFileWriter.append(",");
            visitFileWriter.append("CheckOut Latitude");
            visitFileWriter.append(",");
            visitFileWriter.append("CheckOut Longitude");
            visitFileWriter.append(",");
            visitFileWriter.append("Latitude");
            visitFileWriter.append(",");
            visitFileWriter.append("Longitude");
            visitFileWriter.append(",");
            visitFileWriter.append("Lead Id");
            visitFileWriter.append(",");
            visitFileWriter.append("Work Order Contact Id");
            visitFileWriter.append(",");
            visitFileWriter.append("Account Contact Id");
            visitFileWriter.append(",");
            visitFileWriter.append("Address Id");
            visitFileWriter.append(",");
            visitFileWriter.append("CheckIn Date");
            visitFileWriter.append(",");
            visitFileWriter.append("CheckOut Date");
            visitFileWriter.append(",");
            visitFileWriter.append("Travel Time");
            visitFileWriter.append(",");
            visitFileWriter.append("Work order Id");
            visitFileWriter.append(",");
            visitFileWriter.append("Visit Type");
            visitFileWriter.append(",");
            visitFileWriter.append("Description");
            visitFileWriter.append(",");
            visitFileWriter.append("Route Id");
            visitFileWriter.append(",");
            visitFileWriter.append("Name");
            visitFileWriter.append(",");
            visitFileWriter.append("Technical Id");
            visitFileWriter.append(",");
            visitFileWriter.append("Record Type");
            visitFileWriter.append(",");
            visitFileWriter.append("Account contact name");
            visitFileWriter.append(",");
            visitFileWriter.append("Address");
            visitFileWriter.append(",");
            visitFileWriter.append("Visit Time Number");
            visitFileWriter.append(",");
            visitFileWriter.append("Travel Time Number");
            visitFileWriter.append(",");
            visitFileWriter.append("Update Address");
            visitFileWriter.append(",");
            visitFileWriter.append("Is Main Technical");
            visitFileWriter.append(",");
            visitFileWriter.append("Signature File Path");
            visitFileWriter.append(",");
            visitFileWriter.append("Work order x Technical id");
            visitFileWriter.append(",");
            visitFileWriter.append("Sync");
            visitFileWriter.append(",");
            visitFileWriter.append("Version");
            visitFileWriter.append("\n");

            /*Here we get the data from the data base*/
            List<CheckPointLocation> visits = DatabaseManager.getInstance().getAllCheckPointLocation();

            for(CheckPointLocation visit: visits){
                visitFileWriter.append(String.valueOf(visit.getId()));
                visitFileWriter.append(",");
                visitFileWriter.append(String.valueOf(visit.getCheckInLatitude()));
                visitFileWriter.append(",");
                visitFileWriter.append(String.valueOf(visit.getCheckInLongitude()));
                visitFileWriter.append(",");
                visitFileWriter.append(String.valueOf(visit.getCheckOutLatitude()));
                visitFileWriter.append(",");
                visitFileWriter.append(String.valueOf(visit.getCheckOutLongitude()));
                visitFileWriter.append(",");
                visitFileWriter.append(String.valueOf(visit.getLatitude()));
                visitFileWriter.append(",");
                visitFileWriter.append(String.valueOf(visit.getLongitude()));
                visitFileWriter.append(",");
                visitFileWriter.append(visit.getLeadId());
                visitFileWriter.append(",");
                visitFileWriter.append(visit.getWorkOrderContactId());
                visitFileWriter.append(",");
                visitFileWriter.append(visit.getAccountContactId());
                visitFileWriter.append(",");
                visitFileWriter.append(visit.getAddressId());
                visitFileWriter.append(",");
                visitFileWriter.append(visit.getCheckInDate());
                visitFileWriter.append(",");
                visitFileWriter.append(visit.getCheckOutDate());
                visitFileWriter.append(",");
                visitFileWriter.append(visit.getTravelTime());
                visitFileWriter.append(",");
                visitFileWriter.append(visit.getWorkOrderId());
                visitFileWriter.append(",");
                visitFileWriter.append(visit.getVisitType());
                visitFileWriter.append(",");
                if(visit.getDescription()!=null){
                    visitFileWriter.append(visit.getDescription().replace(",",";"));
                }else{
                    visitFileWriter.append(visit.getDescription());
                }
                visitFileWriter.append(",");
                visitFileWriter.append(String.valueOf(visit.getRouteId()));
                visitFileWriter.append(",");
                visitFileWriter.append(visit.getName());
                visitFileWriter.append(",");
                visitFileWriter.append(visit.getTechnicalId());
                visitFileWriter.append(",");
                visitFileWriter.append(visit.getRecordType());
                visitFileWriter.append(",");
                visitFileWriter.append(visit.getAccountContactName());
                visitFileWriter.append(",");
                if(visit.getAddress()!=null){
                    visitFileWriter.append(visit.getAddress().replace(",",";"));
                }else{
                    visitFileWriter.append(visit.getAddress());
                }
                visitFileWriter.append(",");
                visitFileWriter.append(String.valueOf(visit.getVisitTimeNumber()));
                visitFileWriter.append(",");
                visitFileWriter.append(String.valueOf(visit.getTravelTimeNumber()));
                visitFileWriter.append(",");
                visitFileWriter.append(String.valueOf(visit.isUpdateAddress()));
                visitFileWriter.append(",");
                visitFileWriter.append(String.valueOf(visit.isMainTechnical()));
                visitFileWriter.append(",");
                visitFileWriter.append(visit.getSignatureFilePath());
                visitFileWriter.append(",");
                visitFileWriter.append(visit.getWorkOderXTechnicalId());
                visitFileWriter.append(",");
                visitFileWriter.append(visit.getSync());
                visitFileWriter.append(",");
                visitFileWriter.append( BuildConfig.VERSION_NAME);
                visitFileWriter.append("\n");
            }

             /*Here we close the current route file*/
            visitFileWriter.close();

            /*Here we add the routes file*/
            mBackUpFiles.add(visitsFile);


            //We prepare the file with the user action log
            File userActionLogFile= Utility.createBackUpFile("user_log");
            FileOutputStream fileStreamUserLog = new FileOutputStream(userActionLogFile);
            OutputStreamWriter  userActionLogFileWriter = new OutputStreamWriter(fileStreamUserLog,"UTF-8");


            userActionLogFileWriter.append("Id");
            userActionLogFileWriter.append(",");
            userActionLogFileWriter.append("Accion");
            userActionLogFileWriter.append(",");
            userActionLogFileWriter.append("Fecha");
            userActionLogFileWriter.append(",");
            userActionLogFileWriter.append("Id Usuario");
            userActionLogFileWriter.append(",");
            userActionLogFileWriter.append("Id Ruta");
            userActionLogFileWriter.append(",");
            userActionLogFileWriter.append("Version");
            userActionLogFileWriter.append("\n");

            List<UserLog> userLogs = DatabaseManager.getInstance().getAllUserActions();

            for(UserLog userLog: userLogs){
                userActionLogFileWriter.append(String.valueOf(userLog.getId()));
                userActionLogFileWriter.append(",");
                userActionLogFileWriter.append(userLog.getUserAction());
                userActionLogFileWriter.append(",");
                userActionLogFileWriter.append(userLog.getDate());
                userActionLogFileWriter.append(",");
                userActionLogFileWriter.append(userLog.getUserId());
                userActionLogFileWriter.append(",");
                userActionLogFileWriter.append(String.valueOf(userLog.getRouteId()));
                userActionLogFileWriter.append(",");
                userActionLogFileWriter.append( BuildConfig.VERSION_NAME);
                userActionLogFileWriter.append("\n");
            }

            /*Here we close the current user log file*/
            userActionLogFileWriter.close();

            /*Here we add the user log file*/
            mBackUpFiles.add(userActionLogFile);

            //We prepare the file with the user location in te routes
            File userLocationFile= Utility.createBackUpFile("user_location");
            FileOutputStream fileStreamUserLocation = new FileOutputStream(userLocationFile);
            OutputStreamWriter  userLocationFileWriter = new OutputStreamWriter(fileStreamUserLocation,"UTF-8");


            userLocationFileWriter.append("Id");
            userLocationFileWriter.append(",");
            userLocationFileWriter.append("Latitude");
            userLocationFileWriter.append(",");
            userLocationFileWriter.append("Longitude");
            userLocationFileWriter.append(",");
            userLocationFileWriter.append("Exactitud");
            userLocationFileWriter.append(",");
            userLocationFileWriter.append("Fecha");
            userLocationFileWriter.append(",");
            userLocationFileWriter.append("Id de ruta");
            userLocationFileWriter.append(",");
            userLocationFileWriter.append("Distancia acumulada(km)");
            userLocationFileWriter.append(",");
            userLocationFileWriter.append("Distancia acumulada - B (km)");
            userLocationFileWriter.append(",");
            userLocationFileWriter.append("Distancia acumulada - C (km)");
            userLocationFileWriter.append(",");
            userLocationFileWriter.append("Version");
            userLocationFileWriter.append("\n");

            //Here we get the user location list from database
            List<UserLocation> userLocationList = DatabaseManager.getInstance().getUserLocationList();

            for(UserLocation userLocation: userLocationList){
                userLocationFileWriter.append(String.valueOf(userLocation.getId()));
                userLocationFileWriter.append(",");
                userLocationFileWriter.append(String.valueOf(userLocation.getLatitude()));
                userLocationFileWriter.append(",");
                userLocationFileWriter.append(String.valueOf(userLocation.getLongitude()));
                userLocationFileWriter.append(",");
                userLocationFileWriter.append(String.valueOf(userLocation.getAccuracy()));
                userLocationFileWriter.append(",");
                userLocationFileWriter.append(userLocation.getDate());
                userLocationFileWriter.append(",");
                userLocationFileWriter.append(String.valueOf(userLocation.getRouteId()));
                userLocationFileWriter.append(",");
                userLocationFileWriter.append(String.valueOf(userLocation.getDistance()));
                userLocationFileWriter.append(",");
                userLocationFileWriter.append(String.valueOf(userLocation.getDistanceB()));
                userLocationFileWriter.append(",");
                userLocationFileWriter.append(String.valueOf(userLocation.getDistanceC()));
                userLocationFileWriter.append(",");
                userLocationFileWriter.append(BuildConfig.VERSION_NAME);
                userLocationFileWriter.append("\n");
            }

            /*Here we close the current user log file*/
            userLocationFileWriter.close();

            /*Here we add the user log file*/
            mBackUpFiles.add(userLocationFile);

            mSuccess=true;

        } catch (IOException e) {
            e.printStackTrace();
            mSuccess=false;
            mAditionalMessage = e.getMessage().replace(",","");
            return null;
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        if(mSuccess){
            mListener.onBackUp(true,null,mBackUpFiles);
        }else{
            mListener.onBackUp(false,mContext.getString(R.string.text_error_getting_data_base_file)+" "+mAditionalMessage,null);
        }
    }
}
