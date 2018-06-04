package com.app.checkinmap.util;

import android.app.ActivityManager;
import android.content.Context;
import android.hardware.GeomagneticField;
import android.location.Location;
import android.os.Environment;
import android.os.StatFs;
import android.util.FloatMath;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.salesforce.androidsdk.app.SalesforceSDKManager;
import com.salesforce.androidsdk.rest.RestClient;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.content.Context.ACTIVITY_SERVICE;


/**
 * This class help us encapsulate
 * all the common method in the application
 */

public class Utility {
    public static final String      BACKUP_DIRECTORY="EmasalApp"+File.separator+"Backup";
    public static final String TAG="EMASAL";
    private static RestClient  mRestClient;
    private static String      mUserProfileId;
    private static Roles       mUserRole;
    private static String      mUserProfileName;
    private static String      mUserCountry;
    private static int         mRadioCheckIn;
    private static int         mIntervalSeconds;

    public enum Roles{
        SELLER,
        MANAGER,
        TECHNICAL,
        CUSTOMER_SERVICE,
        TECHNICAL_COORDINATOR,
        OTHER
    }

    /**
     * This method help us to get the current
     * date with hours
     */
    public static String getCurrentDate(){
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Date date = new Date();
        return dateFormat.format(date);
    }

    public static String getCurrentSalesforceDate(){
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.000Z");
        Date date = new Date();
        return dateFormat.format(date);
    }

    /**
     * This method set the Rest client instance from the login
     */
    public static void setRestClient(RestClient restClient){
        mRestClient = restClient;
    }

    /**
     * This method set the Rest client instance from the login
     */
    public static RestClient getRestClient(){
        return mRestClient;
    }


    /**
     * This method set the user profile id like a global variable
     */
    public static void setUserProfileId(String userProfileId){
        mUserProfileId = userProfileId;
        /*Here we check if the user is a seller*/
        switch (userProfileId){
            case "00e6A000000IRoOQAW":
                setUserRole(Roles.SELLER);
                break;
            case "00e6A000000IRoEQAW":
                setUserRole(Roles.TECHNICAL);
                break;
            case "00e6A000000IRoTQAW":
                setUserRole(Roles.CUSTOMER_SERVICE);
                break;
            case "00e6A000000IRoJQAW":
                setUserRole(Roles.TECHNICAL_COORDINATOR);
                break;
            case "00e6A000000IRnzQAG":
                setUserRole(Roles.MANAGER);
                break;
            default:
                setUserRole(Roles.OTHER);
                break;
        }
    }

    /**
     * This method get the user profile id like
     */
    public static String getUserProfileId(){
        return mUserProfileId ;
    }

    /**
     * This method set the user type*/
    public static void setUserRole(Roles userRole){
        mUserRole = userRole;
    }

    /**
     * This method get the user type
     */
    public static Roles getUserRole(){
        return mUserRole ;
    }


    /**
     * This method get the user profile name
     */
    public static String getUserProfileName(){
        return mUserProfileName ;
    }

    /**
     * This method set the user profile name*/
    public static void setUserProfileName(String userProfileName){
        mUserProfileName = userProfileName;
    }


    /**
     * This method help us to
     * show the large JSON
     */
    public static void logLargeString(String str) {
        if(str.length() > 3000) {
            Log.i(TAG, str.substring(0, 3000));
            logLargeString(str.substring(3000));
        } else {
            Log.i(TAG, str); // continuation
        }
    }

    /**
     * This method get the user country
     */
    public static String getUserCountry(){
        return mUserCountry ;
    }

    /**
     * This method set the user country*/
    public static void setUserCountry(String userCountry){
        mUserCountry = userCountry;
    }

    /**
     * Get radio
     * @return
     */
    public static int getRadioCheckIn() {
        return  mRadioCheckIn;
    }

    /**
     * Set radio
     * @param radioCheckIn
     */
    public static void setRadioCheckIn(int radioCheckIn){
        mRadioCheckIn = radioCheckIn;
    }

    /**
     * Get refresh location interval in seconds
     * @return
     */
    public  static int getIntervalSeconds(){
        return mIntervalSeconds;
    }

    /**
     * Set refresh location interval in seconds
     * @param intervalSeconds
     */
    public static void setIntervalSeconds(int intervalSeconds){
        mIntervalSeconds = intervalSeconds;
    }

    /**
     * This method help us to get the current
     * date with hours for route name
     */
    public static String getDateForName(){
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        Date date = new Date();
        return dateFormat.format(date);
    }

    /**
     * This method help us to get the current
     * date with hours for route name
     */
    public static String getDateForNameSimple(){
        DateFormat dateFormat = new SimpleDateFormat("ddMMyy");
        Date date = new Date();
        return dateFormat.format(date);
    }

    /**
     * This method help us to get the current
     * date with hours for route search
     */
    public static String getDateForSearch(){
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        Date date = new Date();
        return dateFormat.format(date);
    }

    /**
     * This method help us to get the time in hour
     * for the visit
     */
    public static String getDurationInHours(String dateStart,String dateFinish){
        String time ="0 horas 0 min";

        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

        Date d1;
        Date d2;

        try {
            d1 = format.parse(dateStart);
            d2 = format.parse(dateFinish);

            //in milliseconds
            long diff = d2.getTime() - d1.getTime();

           // long diffSeconds = diff / 1000 % 60;
            long diffMinutes = diff / (60 * 1000) % 60;
            long diffHours = diff / (60 * 60 * 1000) % 24;
           // long diffDays = diff / (24 * 60 * 60 * 1000);

            time = "";
            time = diffHours+" horas "+diffMinutes+" min";

        } catch (Exception e) {
            e.printStackTrace();
        }

        return time;
    }


    /**
     * This method help us to get the time in hour
     * for the visit
     */
    public static double getDurationInHoursNumber(String dateStart,String dateFinish){
        double totalTime= 0.000;

        //HH converts hour in 24 hours format (0-23), day calculation
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

        Date d1;
        Date d2;

        try {
            d1 = format.parse(dateStart);
            d2 = format.parse(dateFinish);

            //in milliseconds
            long diff = d2.getTime() - d1.getTime();

            // long diffSeconds = diff / 1000 % 60;
            long diffMinutes = diff / (60 * 1000) % 60;
            long diffHours = diff / (60 * 60 * 1000) % 24;
            // long diffDays = diff / (24 * 60 * 60 * 1000);

            totalTime = diffHours;

            totalTime = totalTime + (diffMinutes/60.0);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return totalTime;
    }


    public static String capitalize(String capString) {
        StringBuffer capBuffer = new StringBuffer();
        Matcher capMatcher = Pattern.compile("([a-z-áéíóú])([a-z-áéíóú]*)", Pattern.CASE_INSENSITIVE).matcher(capString);
        while (capMatcher.find()){
            capMatcher.appendReplacement(capBuffer, capMatcher.group(1).toUpperCase() + capMatcher.group(2).toLowerCase());
        }

        return capMatcher.appendTail(capBuffer).toString();
    }

    public static String truncate(String value, int length) {
        // Ensure String length is longer than requested size.
        if (value.length() > length) {
            return value.substring(0, length);
        } else {
            return value;
        }
    }

    public static void checkSFSession(Context context) {
        if (SalesforceSDKManager.getInstance().getUserAccountManager().getCurrentUser() != null) {
            PreferenceManager.getInstance(context).setPreviousSession(true);
            Log.d(TAG, "Refresh Token");
            SalesforceSDKManager.getInstance().getUserAccountManager().getCurrentUser().getRefreshToken();
        }else{
            Log.d(TAG, "No user session");
            PreferenceManager.getInstance(context).setPreviousSession(false);
        }
    }

    /**
     * This method help us to create a txt file in order
     * to prepare the data to make a backup by user in Sales
     * Force
     */
    public static File createBackUpFile(String type) throws IOException {
        //Here we define the backup file
        File backupFile=null;

        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String fileName = "backup_"+type+"_"+Utility.getRestClient().getClientInfo().username+"_"+timeStamp+".csv";

        //create a file to write bitmap data
        File directory= new File(Environment.getExternalStorageDirectory(),BACKUP_DIRECTORY);

        //Here we verify if exist the directory
        if(!directory.exists()){
            directory.mkdirs();
        }

        //create a file to write bitmap data
        backupFile= new File(directory.getAbsolutePath(), fileName);
        if(!backupFile.exists()){
            backupFile.createNewFile();
        }

        return backupFile;
    }

    /**
     * This method help us to encode our file before
     * to make the salesforce request
     */
   public static String encodeFileToBase64(File file){
       byte[] encoded = new byte[0];
       try {
           encoded = Base64.encodeBase64(FileUtils.readFileToByteArray(file));
       } catch (IOException e) {
           e.printStackTrace();
       }
       return new String(encoded, StandardCharsets.UTF_8);
    }

    /**
     * This method help us to get the current
     * date with milliseconds
     */
    public static String getCurrentDateWithTime(){
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Date date = new Date();
        return dateFormat.format(date);
    }

    /**
     * Method for finding bearing between two locations
     * @param begin start location
     * @param end   destination location
     */
    public static float getBearing(LatLng begin, LatLng end) {
        Location beginL= convertLatLngToLocation(begin);
        Location endL= convertLatLngToLocation(end);
        return beginL.bearingTo(endL);
    }

    /**
     * Method to transform  the LatLng variable to
     * Location object
     */
    private static Location convertLatLngToLocation(LatLng latLng) {
        Location loc = new Location("someLoc");
        loc.setLatitude(latLng.latitude);
        loc.setLongitude(latLng.longitude);
        return loc;
    }

    /**
     * This method help us to get the free memory RAM
     * amount
     * @param context the application context
     */
    public static long freeRamMemorySize(Context context) {
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        ActivityManager activityManager = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
        assert activityManager != null;
        activityManager.getMemoryInfo(mi);
        return mi.availMem;
    }

    /**
     * This method help us to get the total memory RAM
     * amount
     * @param context the application context
     */
    public static long totalRamMemorySize(Context context) {
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        ActivityManager activityManager = (ActivityManager)context.getSystemService(ACTIVITY_SERVICE);
        assert activityManager != null;
        activityManager.getMemoryInfo(mi);
        return mi.totalMem;
    }

    /**
     * This method help us to check if the external memory
     * is available
     */
    private static boolean externalMemoryAvailable() {
        return android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
    }

    /**
     * This method help us to get the available internal memory
     * size
     */
    public static long getAvailableInternalMemorySize() {
        StatFs stat = new StatFs(Environment.getRootDirectory().getAbsolutePath());
        long blockSize = stat.getBlockSize();
        long availableBlocks = stat.getAvailableBlocks();
        return availableBlocks * blockSize;
    }

    /**
     * This method help us to get the total
     * internal memory size
     */
    public static long getTotalInternalMemorySize() {
        StatFs stat = new StatFs(Environment.getRootDirectory().getAbsolutePath());
        long blockSize = stat.getBlockSize();
        long totalBlocks = stat.getBlockCount();
        return totalBlocks * blockSize;
    }

    /**
     * This method help us to get the available external memory
     * size
     */
    public static long getAvailableExternalMemorySize() {
        if (externalMemoryAvailable()) {
            StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getAbsolutePath());
            long blockSize = stat.getBlockSize();
            long availableBlocks = stat.getAvailableBlocks();
            return availableBlocks * blockSize;
        } else {
            return 0;
        }
    }

    /**
     * This method help us to get the total
     * external memory size
     */
    public static long getTotalExternalMemorySize() {
        if (externalMemoryAvailable()) {
            StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getAbsolutePath());
            long blockSize = stat.getBlockSize();
            long totalBlocks = stat.getBlockCount();
            return totalBlocks * blockSize;
        } else {
            return 0;
        }
    }

    /**
     * This method help us to set right scale
     * to the memory size
     */
    public static String formatSize(long memory) {
        //Here we define the output
        DecimalFormat twoDecimalForm = new DecimalFormat("#.##",new DecimalFormatSymbols(Locale.ENGLISH));

        //Here we initialize the variable
        String memoryString;

        //Here we check the amount
        double kb = memory / 1024.0;
        double mb = memory / 1048576.0;
        double gb = memory / 1073741824.0;

        if (gb > 1) {
            memoryString = twoDecimalForm.format(gb).concat(" GB");
        } else if (mb > 1) {
            memoryString = twoDecimalForm.format(mb).concat(" MB");
        } else if (kb > 1) {
            memoryString = twoDecimalForm.format(kb).concat(" KB");
        } else {
            memoryString = twoDecimalForm.format(memory).concat(" bytes");
        }

        return memoryString;
    }


    /**
     * This method help us to set all the phone data
     * in a simple string
     */
    public static String getPhoneStatusData(Context context){
        StringBuilder dataBuilder = new StringBuilder();
        dataBuilder.append("Estado conexión: ");
        dataBuilder.append(PreferenceManager.getInstance(context).getNetworkState()+"; ");
        dataBuilder.append("Conexión a Internet: ");
        dataBuilder.append(PreferenceManager.getInstance(context).getInternetConnection()+"; ");
        dataBuilder.append("GPS: ");
        dataBuilder.append(PreferenceManager.getInstance(context).getGpsState()+"; ");
        dataBuilder.append("RAM usada: ");
        dataBuilder.append(formatSize(totalRamMemorySize(context)-freeRamMemorySize(context)).replace(",",".")+"; ");
        dataBuilder.append("RAM libre: ");
        dataBuilder.append(formatSize(freeRamMemorySize(context)).replace(",",".")+"; ");
        dataBuilder.append("RAM total: ");
        dataBuilder.append(formatSize(totalRamMemorySize(context)).replace(",",".")+"; ");
        dataBuilder.append("Mem.interna usada: ");
        dataBuilder.append(formatSize(getTotalInternalMemorySize()-getAvailableInternalMemorySize()).replace(",",".")+"; ");
        dataBuilder.append("Mem.interna libre: ");
        dataBuilder.append(formatSize(getAvailableInternalMemorySize()).replace(",",".")+"; ");
        dataBuilder.append("Mem. interna total: ");
        dataBuilder.append(formatSize(getTotalInternalMemorySize()).replace(",",".")+"; ");
        dataBuilder.append("Mem.externa usada: ");
        dataBuilder.append(formatSize(getTotalExternalMemorySize()-getAvailableExternalMemorySize()).replace(",",".")+"; ");
        dataBuilder.append("Mem. externa libre: ");
        dataBuilder.append(formatSize(getAvailableExternalMemorySize()).replace(",",".")+"; ");
        dataBuilder.append("Mem. externa total: ");
        dataBuilder.append(formatSize(getTotalExternalMemorySize()).replace(",","."));
        return dataBuilder.toString();
    }


    /**
     * This method help us to calculate the distances
     * in kilometers
     */
    public static double distanceInKilometers(double latitudeStart, double longitudeStar, double latitudeEnd, double longitudeEnd) {
        double theta = longitudeStar - longitudeEnd;
        double dist = Math.sin(deg2rad(latitudeStart))
                * Math.sin(deg2rad(latitudeEnd))
                + Math.cos(deg2rad(latitudeStart))
                * Math.cos(deg2rad(latitudeEnd))
                * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        return (dist);
    }

    public static double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    public static double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }
}
