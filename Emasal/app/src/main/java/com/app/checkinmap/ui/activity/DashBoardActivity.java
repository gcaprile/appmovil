package com.app.checkinmap.ui.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.app.checkinmap.BuildConfig;
import com.app.checkinmap.R;
import com.app.checkinmap.bus.BusProvider;
import com.app.checkinmap.bus.NewLocationEvent;
import com.app.checkinmap.db.DatabaseManager;
import com.app.checkinmap.model.CheckPointLocation;
import com.app.checkinmap.model.Route;
import com.app.checkinmap.model.UserLocation;
import com.app.checkinmap.service.LocationService;
import com.app.checkinmap.util.ApiManager;
import com.app.checkinmap.util.BackupAsyncTask;
import com.app.checkinmap.util.CleanDatabaseAsyncTask;
import com.app.checkinmap.util.Foreground;
import com.app.checkinmap.util.NetworkUtilsTask;
import com.app.checkinmap.util.OnBackUpListener;
import com.app.checkinmap.util.OnDatabaseCleanListener;
import com.app.checkinmap.util.OnNetworkListener;
import com.app.checkinmap.util.PreferenceManager;
import com.app.checkinmap.util.Utility;
import com.crashlytics.android.Crashlytics;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.salesforce.androidsdk.app.SalesforceSDKManager;
import com.salesforce.androidsdk.rest.RestClient;
import com.squareup.otto.Subscribe;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.Realm;

import static com.app.checkinmap.ui.activity.CheckPointMapActivity.ARG_CHECK_POINT_LOCATION_ID;
import static com.app.checkinmap.ui.activity.CheckPointMapActivity.PERMISSION_LOCATION_REQUEST;
import static com.app.checkinmap.ui.activity.CheckPointMapActivity.REQUEST_CHECK_IN;
import static com.app.checkinmap.ui.activity.MyAccountsActivity.REQUEST_ACCOUNT_SELECTION;
import static com.app.checkinmap.ui.activity.MyLeadsActivity.REQUEST_LEAD_SELECTION;
import static com.app.checkinmap.ui.activity.MyWorkOrdersActivity.REQUEST_WORK_ORDER_SELECTION;

public class DashBoardActivity extends AppCompatActivity
        implements OnMapReadyCallback,NavigationView.OnNavigationItemSelectedListener, Foreground.Listener {
    @BindView(R.id.toolbar)
    Toolbar mToolBar;

    @BindView(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;

    @BindView(R.id.nav_view)
    NavigationView mNavigationView;

    @BindView(R.id.app_version)
    TextView mAppVersion;

    @BindView(R.id.button_start_rout)
    TextView mTxvRouteButton;

    @BindView(R.id.button_work_order)
    TextView mTxvWorkOrder;

    @BindView(R.id.button_accounts)
    TextView mTxvAccounts;

    @BindView(R.id.button_leads)
    TextView mTxvLeads;

    @BindView(R.id.linear_layout_progress)
    LinearLayout mLnlProgress;

    @BindView(R.id.image_view_my_location)
    ImageView mImgMyLocation;



    private boolean                     mLocationSettingCalled=false;
    private LocationManager             mLocationManager;
    private GoogleMap                   mMap;
    private boolean                     mLocationPermissionGranted=false;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private MaterialDialog              mMaterialProgressDialog;
    private int                         mMapReadyCall=0;
    private boolean                     mPermissionDialogCall = false;
    private double                      mLatitude  = 0;
    private double                      mLongitude = 0;
    private boolean                     mRouteCreatedSuccess=false;
    private Marker                      mMarker;
    private boolean                     mFirstMapAnimation=true;


    /**
     * This method help us to get a single intent
     * from Main activity
     */
    public static Intent getIntent(Context context){
        Intent intent = new Intent(context,DashBoardActivity.class);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dash_board);

        ButterKnife.bind(this);

        /*Here we get initialize the map*/
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
            .findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);

        //Here we get the location manager
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // Construct a FusedLocationProviderClient.
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        //Here we initialize the toolbar an main menu
        initToolbarAndMenu();

        //Her we update the user data
        setUserDataInMenu();

        // Set version name to menu
        mAppVersion.setText(getString(R.string.app_name_version, BuildConfig.VERSION_NAME));

        //Set the listener of the background and foreground state
        Foreground.get().addListener(this);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if(mMapReadyCall==0){
            mMapReadyCall++;
             /*Here we check the permission*/
            if(checkAndRequestPermissions()){
                if(isGpsEnable()){
                /*Here we request a explicit user location request*/
                getUserLocation();
              }else{
                showGpsDisableMessage();
              }
            }
        }
    }

    /**
     * This method initialize the main menu and
     * the tool bar
     */
    public void initToolbarAndMenu(){
        mToolBar.setTitle(R.string.emasal);
        setSupportActionBar(mToolBar);


        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawerLayout, mToolBar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawerLayout.setDrawerListener(toggle);
        toggle.syncState();

        mNavigationView.setNavigationItemSelectedListener(this);
    }


    /**
     * This method help us to load the user data in the
     * lateral main menu
     */
    public void setUserDataInMenu(){
        /*Here we set the user data*/
        TextView mTxvAccountName = mNavigationView.getHeaderView(0).findViewById(R.id.text_view_account_name) ;

        TextView mTxvUserName=  mNavigationView.getHeaderView(0).findViewById(R.id.text_view_user_name) ;

        TextView mTxvProfileName= mNavigationView.getHeaderView(0).findViewById(R.id.text_view_profile_name) ;

        if (Utility.getRestClient() != null) {

            RestClient.ClientInfo ci = Utility.getRestClient().getClientInfo();
            mTxvAccountName.setText(ci.displayName);
            mTxvUserName.setText(ci.username);
            mTxvProfileName.setText(Utility.getUserProfileName());

            /*Here we show or hide the menu options*/
            switch (Utility.getUserRole()) {
                case SELLER:
                    mNavigationView.getMenu().findItem(R.id.nav_my_accounts).setVisible(true);
                    mNavigationView.getMenu().findItem(R.id.nav_candidates).setVisible(true);
                    mNavigationView.getMenu().findItem(R.id.nav_sync).setVisible(true);
                    mNavigationView.getMenu().findItem(R.id.nav_delete).setVisible(true);
                    mTxvLeads.setVisibility(View.VISIBLE);
                    mTxvAccounts.setVisibility(View.VISIBLE);
                    break;
                case MANAGER:
                    mNavigationView.getMenu().findItem(R.id.nav_my_accounts).setVisible(true);
                    mNavigationView.getMenu().findItem(R.id.nav_candidates).setVisible(true);
                    mNavigationView.getMenu().findItem(R.id.nav_sync).setVisible(true);
                    mNavigationView.getMenu().findItem(R.id.nav_delete).setVisible(true);
                    mTxvLeads.setVisibility(View.VISIBLE);
                    mTxvAccounts.setVisibility(View.VISIBLE);
                    break;
                case TECHNICAL:
                    mNavigationView.getMenu().findItem(R.id.nav_my_orders).setVisible(true);
                    mNavigationView.getMenu().findItem(R.id.nav_sync).setVisible(true);
                    mNavigationView.getMenu().findItem(R.id.nav_delete).setVisible(true);
                    mTxvWorkOrder.setVisibility(View.VISIBLE);
                    break;
                case CUSTOMER_SERVICE:
                    mNavigationView.getMenu().findItem(R.id.nav_my_accounts).setVisible(true);
                    mNavigationView.getMenu().findItem(R.id.nav_candidates).setVisible(true);
                    mNavigationView.getMenu().findItem(R.id.nav_sync).setVisible(true);
                    mNavigationView.getMenu().findItem(R.id.nav_delete).setVisible(true);
                    mTxvLeads.setVisibility(View.VISIBLE);
                    mTxvAccounts.setVisibility(View.VISIBLE);
                    break;
                case TECHNICAL_COORDINATOR:
                    mNavigationView.getMenu().findItem(R.id.nav_my_orders).setVisible(true);
                    mNavigationView.getMenu().findItem(R.id.nav_sync).setVisible(true);
                    mNavigationView.getMenu().findItem(R.id.nav_delete).setVisible(true);
                    mTxvWorkOrder.setVisibility(View.VISIBLE);
                    break;
                default:
                    mTxvRouteButton.setVisibility(View.GONE);
                    break;
            }
        } else {
            closeSalesForce();
        }
        //
    }

    @Override
    protected void onStart() {
        super.onStart();
        BusProvider.getInstance().register(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Utility.checkSFSession(this);
        if(mLocationSettingCalled){
            mLocationSettingCalled=false;
            if(isGpsEnable()){
                getUserLocation();
            }else{
                showGpsDisableMessage();
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        BusProvider.getInstance().unregister(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(!isInRoute()){
            stopLocationService();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case PERMISSION_LOCATION_REQUEST: {
                if (grantResults.length > 0 ) {
                    if(grantResults.length ==2){
                        if (grantResults[0] == PackageManager.PERMISSION_GRANTED
                                && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                            mLocationPermissionGranted=true;
                            mPermissionDialogCall = false;
                            getUserLocation();
                        } else {
                            showRationale();
                        }
                    }else{
                        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                            mLocationPermissionGranted=true;
                            mPermissionDialogCall = false;
                            getUserLocation();
                        } else {
                            showRationale();
                        }
                    }
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case REQUEST_ACCOUNT_SELECTION:
            case REQUEST_LEAD_SELECTION:
            case REQUEST_WORK_ORDER_SELECTION:
            case REQUEST_CHECK_IN:
                if(resultCode == RESULT_OK){
                    long checkPointLocationId = data.getExtras().getLong(ARG_CHECK_POINT_LOCATION_ID);
                    PreferenceManager.getInstance(this).setIsDoingCheckIn(false);
                    PreferenceManager.getInstance(this).setCheckPointLocation("");
                    PreferenceManager.getInstance(this).setCheckPointData("");
                    showSummaryVisitDialog(checkPointLocationId);
                }
                break;
        }
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }



    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_my_information) {

            startActivity(MyInformationActivity.getIntent(this));

        } else if (id == R.id.nav_my_accounts) {

            openAccountMenu();

        } else if (id == R.id.nav_candidates) {

           openLeadMenu();

        }else if (id == R.id.nav_my_orders) {

            openWorkOrderMenu();

        }  else if (id == R.id.nav_sync) {

           showDataBaseBackUpQuestion();

        }  else if (id == R.id.nav_delete) {

            showDataBaseCleanQuestion();

        }else if (id == R.id.nav_exit) {

            /*Here we close the session*/
            closeSession();
        }

        mDrawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @OnClick(R.id.button_leads)
    public void openLeadMenu(){
        startActivityForResult(MyLeadsActivity.getIntent(this), REQUEST_LEAD_SELECTION);
    }

    @OnClick(R.id.button_accounts)
    public void openAccountMenu(){
        startActivityForResult(MyAccountsActivity.getIntent(this), REQUEST_ACCOUNT_SELECTION);
    }

    @OnClick(R.id.button_work_order)
    public void openWorkOrderMenu(){
        startActivityForResult(MyWorkOrdersActivity.getIntent(this),REQUEST_WORK_ORDER_SELECTION);
    }

    @OnClick(R.id.button_start_rout)
    public void startRoute(){
       if(isInRoute()){
           confirmRouteCompletion();
       }else{
           if(checkAndRequestPermissions()){
               if(isGpsEnable()){
                   confirmStartOfRoute();
               }else{
                   showGpsDisableMessage();
               }
           }
       }
    }

    public void confirmStartOfRoute() {

        new MaterialDialog.Builder(this)
            .title(R.string.app_name)
            .content(R.string.start_route_message)
            .positiveColorRes(R.color.colorPrimary)
            .positiveText(R.string.start)
            .onPositive(new MaterialDialog.SingleButtonCallback() {
                @Override
                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                    createRoute();
                }
            })
            .negativeColorRes(R.color.colorPrimary)
            .negativeText(R.string.cancel)
            .onNegative(new MaterialDialog.SingleButtonCallback() {
                @Override
                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                    dialog.dismiss();
                }
            })
            .cancelable(false)
            .show();
    }

    /**
     * This method help us to show a confirmation message
     * before finalize the route
     */
    public void confirmRouteCompletion() {

        new MaterialDialog.Builder(this)
            .title(R.string.app_name)
            .content(R.string.end_route_message)
            .positiveColorRes(R.color.colorPrimary)
            .positiveText(R.string.end)
            .onPositive(new MaterialDialog.SingleButtonCallback() {
                @Override
                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                    showEndOdometerDialog();
                }
            })
            .negativeColorRes(R.color.colorPrimary)
            .negativeText(R.string.cancel)
            .onNegative(new MaterialDialog.SingleButtonCallback() {
                @Override
                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                    dialog.dismiss();
                }
            })
            .cancelable(false)
            .show();
    }

    /**
     * This method help us to create the route instance
     */
    public void createRoute(){

        /*Here we update the ui*/
        mTxvRouteButton.setVisibility(View.GONE);
        mLnlProgress.setVisibility(View.VISIBLE);

        new NetworkUtilsTask(this, new OnNetworkListener() {

            @Override
            public void onNetwork(boolean success, String message) {
                if(success){
                    String osql = "SELECT Radio_Check_In__c FROM Aplicacion_Movil_EMASAL__c WHERE name = 'config'";

                    ApiManager.getInstance().getJSONObject(getCurrentContext(), osql, new ApiManager.OnObjectListener() {
                        @Override
                        public void onObject(boolean success, JSONObject jsonObject, String errorMessage) {
                            mLnlProgress.setVisibility(View.GONE);
                            if(success){
                                /*Here we check if exist an previous active route */
                                if(!DatabaseManager.getInstance().checkStartedRoute()){
                                    try {
                                        final int radius = jsonObject.getJSONArray("records").getJSONObject(0).getInt("Radio_Check_In__c");

                                        //Here we ask for odometer data
                                        showStartOdometerDialog(radius);

                                    } catch (JSONException e) {
                                        mTxvRouteButton.setVisibility(View.VISIBLE);
                                        updateButtonUi();
                                        showMessage(R.string.text_no_radius);
                                    }
                                }else{
                                    mLnlProgress.setVisibility(View.GONE);
                                    mTxvRouteButton.setVisibility(View.VISIBLE);
                                    updateButtonUi();
                                    showMessage(R.string.text_an_active_route_exist);
                                }
                            }else{
                                mTxvRouteButton.setVisibility(View.VISIBLE);
                                updateButtonUi();
                                showMessage(R.string.text_no_radius);
                            }
                        }
                    });
                }else{
                    mTxvRouteButton.setVisibility(View.VISIBLE);
                    updateButtonUi();
                    showMessage(message);
                }
            }
        }).execute();
    }

    /**
     * This method help use to show a message
     */
    public void showMessage(int message){
        new MaterialDialog.Builder(this)
                .title(R.string.app_name)
                .content(message)
                .positiveColorRes(R.color.colorPrimary)
                .positiveText(R.string.accept)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                    }
                })
                .cancelable(false)
                .show();
    }

    public void showMessage(String message){
        new MaterialDialog.Builder(this)
            .title(R.string.app_name)
            .content(message)
            .positiveColorRes(R.color.colorPrimary)
            .positiveText(R.string.accept)
            .onPositive(new MaterialDialog.SingleButtonCallback() {
                @Override
                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                    dialog.dismiss();
                }
            })
            .cancelable(false)
            .show();
    }

    /**
     * This method check if the app has all the
     * permissions needed
     */
    private  boolean checkAndRequestPermissions() {

        int locationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        int storagePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        List<String> listPermissionsNeeded = new ArrayList<>();

        if (locationPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }

        if (storagePermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        if (!listPermissionsNeeded.isEmpty()) {
            if(!mPermissionDialogCall){
                mPermissionDialogCall=true;
                mLocationPermissionGranted=false;
                ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]),PERMISSION_LOCATION_REQUEST);
            }
            return false;
        }
        mLocationPermissionGranted=true;
        return true;
    }

    /**
     * This method help us to show the rationale
     * for the location permission
     */
    public void showRationale(){
        new MaterialDialog.Builder(this)
                .title(R.string.app_name)
                .content(R.string.location_permission_rationale)
                .positiveColorRes(R.color.colorPrimary)
                .positiveText(R.string.accept)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        checkAndRequestPermissions();
                    }
                })
                .cancelable(false)
                .show();
    }

    /**
     * This method help us to start the location
     * service in the app
     */
    private void startLocationService(){
        startService(new Intent(this, LocationService.class));
    }

    /**
     * This method help us to stop the location
     * service in the app
     */
    private void stopLocationService(){
        stopService(new Intent(this, LocationService.class));
    }


    /**
     * This method check if the user is in route
     * mode
     */
    private boolean isInRoute(){
        boolean flag;
        if(PreferenceManager.getInstance(this).isInRoute()){
            flag= true;
        }else{
            Route route = DatabaseManager.getInstance().getStartedRoute();
            if(route!=null){
                PreferenceManager.getInstance(this).setIsInRoute(true);
                PreferenceManager.getInstance(this).setRouteId(route.getId());
                 /*Here we track the user action*/
                DatabaseManager.getInstance().saveUserAction(this,getString(R.string.route_from_local_storage)+" "+
                       route.getId());
                flag=true;
            }else{
                flag=false;
            }
        }
        return flag;
    }


    /**
     * This method check if the GPS is enable
     */
    private boolean isGpsEnable(){
        return mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    /**
     * This method help us to ope the settings in order to enable the
     * GPS in the device
     */
    public void openGpsSettings(){
        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivity(intent);
    }


    /**
     * This method help us to show a message
     * to indicate the user have to enable the GPS
     */
    public void showGpsDisableMessage(){

        new MaterialDialog.Builder(this)
                .title(R.string.app_name)
                .content(R.string.gps_disable_message)
                .positiveColorRes(R.color.colorPrimary)
                .positiveText(R.string.text_enable)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        mLocationSettingCalled=true;
                        openGpsSettings();
                        dialog.dismiss();
                    }
                })
                .cancelable(false)
                .show();
    }


    @Subscribe
    public void getNewLocation(NewLocationEvent newLocationEvent){
        Log.d("LOCATION LAT:" , String.valueOf(newLocationEvent.getLat()));
        Log.d("LOCATION LON:" , String.valueOf(newLocationEvent.getLon()));
        Log.d("LOCATION BEARING:" , String.valueOf(newLocationEvent.getBearing()));

        //Save the the user location
        mLatitude = newLocationEvent.getLat();
        mLongitude = newLocationEvent.getLon();

        //Here we update the user location in the map
        updateUserLocation(newLocationEvent.getLat(),newLocationEvent.getLon(),
                newLocationEvent.getBearing());
    }

    /**
     * This method make the location request
     * in order to get the las device location
     */
    public void getUserLocation(){
/*
     * Get the best and most recent location of the device, which may be null in rare
     * cases when a location is not available.
     */
        try {
            if (mLocationPermissionGranted) {
                Task locationResult = mFusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            // Set the map's camera position to the current location of the device.
                            Location location = (Location) task.getResult();
                            if(location!=null){
                                setUserLocationOnMap(location.getLatitude(),location.getLongitude());
                            }else{
                                startLocationService();
                                updateButtonUi();
                                mImgMyLocation.setVisibility(View.VISIBLE);
                            }
                        }

                        /*Here we update the button ui*/
                        updateButtonUi();

                        /*Here we check a previous route*/
                        if(isInRoute()){
                             /*Here we track the user action*/
                            DatabaseManager.getInstance().saveUserAction(getApplicationContext(),getString(R.string.restored_route));
                        }

                         /*Here we restart the ui if we were doing check in*/
                       checkPreviousCheckIn();
                    }
                });
            }
        } catch(SecurityException e)  {
            DatabaseManager.getInstance().saveUserAction(getApplicationContext(),e.getMessage().replace(",",""));
        }
    }

    /**
     * This method help us to check if we were in a
     * previous check in
     */
    private void checkPreviousCheckIn(){
        if(PreferenceManager.getInstance(this).isInRoute()){
            if(PreferenceManager.getInstance(this).isDoingCheckIn()){
                if(PreferenceManager.getInstance(this).getCheckPointData().compareTo("")!=0 &&
                        PreferenceManager.getInstance(this).getCheckPointLocation().compareTo("")!=0){
                    startActivityForResult(CheckPointMapActivity.getIntent(this,null),
                            REQUEST_CHECK_IN);
                }
            }
        }
    }

    /**
     * This method help us to set the user location
     * in the map
     */
    public void setUserLocationOnMap(double latitude, double longitude){

           /*Here we add the new one*/
            if(mMarker == null){
                mMarker = mMap.addMarker(
                        new MarkerOptions()
                                .position(new LatLng(latitude, longitude))
                                .anchor(0.5f,0.5f)
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
            }else{
                mMarker.setPosition(new LatLng(latitude,longitude));
            }

        //Here we update the camera position
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(new LatLng(latitude, longitude))
                .tilt(90)
                .zoom(mMap.getCameraPosition().zoom >=17f ? mMap.getCameraPosition().zoom : 20f)
                .build();


        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), 1500, new GoogleMap.CancelableCallback() {
            @Override
            public void onFinish() {
                /*Here we start the location service*/
                startLocationService();
                mImgMyLocation.setVisibility(View.VISIBLE);
            }

            @Override
            public void onCancel() {

            }
        });

        if(mLnlProgress.isShown()){
            mLnlProgress.setVisibility(View.GONE);
        }

        if(!mTxvRouteButton.isShown()){
            mTxvRouteButton.setVisibility(View.VISIBLE);
        }

        mLatitude = latitude;
        mLongitude = longitude;
    }
    /**
     * This method help us to update the user location
     */
      public void updateUserLocation(final double newLatitude, final double newLongitude, final float bearing){
          if(mMarker==null){
              getUserLocation();
          }else{
              final LatLng beginLatLng = mMarker.getPosition();
              final LatLng endLatLng = new LatLng(newLatitude,newLongitude);
              final Handler handler = new Handler();
              final long start = SystemClock.uptimeMillis();
              final Interpolator interpolator = new LinearInterpolator();


              handler.post(new Runnable() {

                  @Override
                  public void run() {
                      long elapsed = SystemClock.uptimeMillis() - start;
                      double t = interpolator.getInterpolation((float)elapsed/1500);

                      double lat = t * endLatLng.latitude + (1-t) * beginLatLng.latitude;
                      double lng = t * endLatLng.longitude + (1-t) * beginLatLng.longitude;


                      LatLng newPosition = new LatLng(lat, lng);
                      mMarker.setPosition(newPosition);
                      if (t< 1) {
                          handler.postDelayed(this, 16);
                      } else {
                          float bearingL = Utility.getBearing(beginLatLng, endLatLng);

                          //Here we prepare the new camera position
                          CameraPosition cameraPosition = new CameraPosition.Builder()
                                  .target(endLatLng) // changed this...
                                  .bearing(bearingL + 20f)
                                  .tilt(90)
                                  .zoom(mMap.getCameraPosition().zoom)
                                  .build();

                          mMap.animateCamera(
                                  CameraUpdateFactory.newCameraPosition(cameraPosition),
                                  1500,
                                  null
                          );
                      }
                  }
              });
          }
      }



      /**
       * This method help us to update the
       * UI main button
       */
      public void updateButtonUi(){
          /*Here we check if we are in route*/
          if(isInRoute()){
              mTxvRouteButton.setText(R.string.finalize_route);
              mTxvRouteButton.setBackgroundColor(getResources().getColor(R.color.colorRed));
          }else{
              mTxvRouteButton.setText(R.string.start_route);
              mTxvRouteButton.setBackgroundColor(getResources().getColor(R.color.colorBlue));
          }
      }

       /**
        * This method help us to show a message
        * with an overview about the last visit
       */
       public void  showSummaryVisitDialog(long checkPointLocationId){
           CheckPointLocation checkPointLocation = DatabaseManager.getInstance().getCheckPointLocation(checkPointLocationId);
           TextView  visitTime;
           TextView  visitTypeLabel;
           TextView  visitType;
           TextView  visitTypeDescriptionLabel;
           TextView  visitTypeDescription;
           TextView  contactName;

           MaterialDialog dialog = new MaterialDialog.Builder(this)
                   .titleColorRes(R.color.colorPrimary)
                   .title(R.string.text_visit_overview)
                   .customView(R.layout.dialog_visit_summary,true)
                   .positiveColorRes(R.color.colorPrimary)
                   .positiveText(R.string.accept)
                   .onPositive(new MaterialDialog.SingleButtonCallback() {
                       @Override
                       public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                           dialog.dismiss();
                       }
                   }).cancelable(false)
                   .build();

           /*Here we get reference to the layout widgets*/
           View dialogView = dialog.getCustomView();
           visitTime= dialogView.findViewById(R.id.text_view_visit_time);
           visitTypeLabel= dialogView.findViewById(R.id.text_view_visit_type_label);
           visitType = dialogView.findViewById(R.id.text_view_visit_type);
           visitTypeDescriptionLabel = dialogView.findViewById(R.id.text_view_visit_description_label);
           visitTypeDescription = dialogView.findViewById(R.id.text_view_visit_description);
           contactName = dialogView.findViewById(R.id.text_view_contact);

           switch (checkPointLocation.getRecordType()){
               case "0126A000000l3CuQAI":
                   visitTime.setText(checkPointLocation.getVisitTime());
                   visitType.setText(checkPointLocation.getVisitType());
                   visitTypeDescription.setText(checkPointLocation.getDescription());
                   contactName.setText(checkPointLocation.getAccountContactName());
                   break;
               case "0126A000000l3CzQAI":
                   visitTime.setText(checkPointLocation.getVisitTime());
                   visitType.setText(checkPointLocation.getVisitType());
                   visitTypeDescription.setText(checkPointLocation.getDescription());
                   contactName.setText(checkPointLocation.getAccountContactName());
                   break;
               case "0126A000000l3D4QAI":
                   visitTime.setText(checkPointLocation.getVisitTime());
                   visitType.setVisibility(View.GONE);
                   visitTypeLabel.setVisibility(View.GONE);
                   visitTypeDescriptionLabel.setVisibility(View.GONE);
                   visitTypeDescription.setVisibility(View.GONE);
                   contactName.setText(checkPointLocation.getAccountContactName());
                   break;
           }

           /*Here we clean variables*/
           PreferenceManager.getInstance(this).setIsDoingCheckIn(false);
           PreferenceManager.getInstance(this).setCheckPointData("");
           PreferenceManager.getInstance(this).setCheckPointLocation("");

           dialog.show();

            /*Here we track the user action*/
           DatabaseManager.getInstance().saveUserAction(getCurrentContext(),getString(R.string.check_in_success)+" "+checkPointLocation.getName());
       }

    /**
     * This method help us to update the  finalize
     * route date and close the route
     */
    public void updateRouteDate(final int odometer) {
        showProgressDialog(R.string.text_sending_route_data);
        new NetworkUtilsTask(this, new OnNetworkListener() {

            @Override
            public void onNetwork(boolean success, String message) {

                if(success){
                    Realm realm = Realm.getDefaultInstance();
                    try {
                        realm.executeTransaction(new Realm.Transaction() {
                            @Override
                            public void execute(Realm realm) {

                                Route routeCopy;
                                Route toEdit = realm.where(Route.class)
                                        .equalTo("id", PreferenceManager.getInstance(getApplicationContext()).getRouteId()).findFirst();
                                toEdit.setEndDate(Utility.getCurrentDate());
                                toEdit.setMileage(getRoutDistanceFirstWay());
                                toEdit.setMileageB(getRoutDistanceSecondWay());
                                toEdit.setMileageC(getRoutDistanceThirdWay());
                                toEdit.setEndLatitude(mLatitude);
                                toEdit.setEndLongitude(mLongitude);
                                toEdit.setStatus("Finalizada");
                                toEdit.setEndOdometer(odometer);

                                //Here we copy data to database
                                realm.copyToRealmOrUpdate(toEdit);

                               /*Here we send the data to sales force*/
                                routeCopy = realm.copyFromRealm(toEdit);
                                sendToSalesForce(routeCopy);
                                Log.d("REALM"," actualizada");
                            }
                        });
                    }catch (Exception e){
                        e.printStackTrace();
                        hideProgressDialog();
                        showMessage(R.string.text_no_route_finalize);
                        Crashlytics.log(Log.ERROR, "Close Route Exception", e.getMessage());
                    }finally {
                        if(realm!=null){
                            realm.close();
                        }
                    }
                }else{
                    hideProgressDialog();
                    showMessage(message);
                }
            }
        }).execute();
    }

    /**
     * This method help us to send the data to sales force
     */
    public void sendToSalesForce(final Route route){
        ApiManager.getInstance().makeRouteUpsert(this, route, new ApiManager.OnObjectListener() {
            @Override
            public void onObject(boolean success, JSONObject jsonObject, String errorMessage) {
                if(success){
                    try {
                        if(jsonObject.getBoolean("success")){

                            /*Here we get the sales force route id*/
                            String routeId = jsonObject.getString("id");

                            /*Here we get the visits from the database*/
                            List<CheckPointLocation> checkPointLocations = DatabaseManager.getInstance().getCheckPointLocationList(route.getId());

                            /*Here we update the route sync stated*/
                            updateRouteSyncState();

                            /*Here we send the visit to salesforce*/
                            sendVisitToSalesForce(checkPointLocations,routeId,0);

                        }else{
                            showMessage(getString(R.string.text_data_no_saved_info, "ruta", "Error al obtener la respuesta desde SF."));
                            hideProgressDialog();
                            Crashlytics.log(Log.ERROR, "RouteNoSavedJson", "jsonObject has not success message");
                            Crashlytics.log(Log.ERROR, "RouteNoSavedJson", errorMessage.toString());
                            Crashlytics.log(Log.ERROR, "RouteNoSavedJson", jsonObject.toString());
                        }
                    } catch (JSONException e) {
                        showMessage(getString(R.string.text_data_no_saved_info, "ruta", "Excepción: " + e.toString()));
                        hideProgressDialog();
                        Crashlytics.log(Log.ERROR, "RouteNoSavedException", e.toString());
                        Crashlytics.logException(e);
                    }
                }else{
                    showMessage(getString(R.string.text_data_no_saved_info, "ruta", "Error: " + errorMessage.toString()));
                    hideProgressDialog();
                }
            }
        });
    }


    /**
     * This method help us to modify the
     * route sync state after saved in
     * salesforce
     */
    public void updateRouteSyncState() {
        Realm realm = Realm.getDefaultInstance();
        try {
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {

                    Route toEdit = realm.where(Route.class)
                            .equalTo("id", PreferenceManager.getInstance(getApplicationContext()).getRouteId()).findFirst();

                    toEdit.setSync("1");
                    realm.copyToRealmOrUpdate(toEdit);

                    Log.d("REALM","Route sync state updated");
                }
            });
        }catch (Exception e){
            Crashlytics.log(Log.ERROR, "Route sync state Exception", e.toString());
        }finally {
            if(realm!=null){
                realm.close();
            }
        }
    }

    /**
     * This method help us to modify the
     * route sync state after saved in
     * salesforce
     */
    public void updateVisitSyncState(final CheckPointLocation visit) {
        Realm realm = Realm.getDefaultInstance();
        try {
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {

                    CheckPointLocation toEdit = realm.where(CheckPointLocation.class)
                            .equalTo("id", visit.getId()).findFirst();

                    toEdit.setSync("1");
                    realm.copyToRealmOrUpdate(toEdit);

                    Log.d("REALM"," Visit sync state updated");
                }
            });
        }catch (Exception e){
            Crashlytics.log(Log.ERROR, "Visit sync state Exception", e.toString());
        }finally {
            if(realm!=null){
                realm.close();
            }
        }
    }


    /**
     * Here we show all the data about
     * the route
     */
    public void callHistoryActivity(long routeId){
        startActivity(HistoryActivity.getIntent(this,routeId));
    }

    /**
     * This method help us to close
     * the current session
     */
    public void closeSession(){
        if(PreferenceManager.getInstance(this).isInRoute()){
            new MaterialDialog.Builder(this)
                    .title(R.string.app_name)
                    .content(R.string.text_close_session_b)
                    .positiveColorRes(R.color.colorPrimary)
                    .positiveText(R.string.accept)
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            /*Here we clean the current route data*/
                            DatabaseManager.getInstance().deleteCurrentRouteData(getApplicationContext());

                            /*Here we close the current session*/
                            closeSalesForce();
                            dialog.dismiss();
                        }
                    })
                    .negativeColorRes(R.color.colorPrimary)
                    .negativeText(R.string.cancel)
                    .onNegative(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            dialog.dismiss();
                        }
                    })
                    .cancelable(false)
                    .show();
        }else{
            new MaterialDialog.Builder(this)
                    .title(R.string.app_name)
                    .content(R.string.text_close_session)
                    .positiveColorRes(R.color.colorPrimary)
                    .positiveText(R.string.accept)
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                            closeSalesForce();
                            dialog.dismiss();
                        }
                    })
                    .negativeColorRes(R.color.colorPrimary)
                    .negativeText(R.string.cancel)
                    .onNegative(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            dialog.dismiss();
                        }
                    })
                    .cancelable(false)
                    .show();
        }
    }

    /**
     * This method close the sales force
     * session
     */
    public void closeSalesForce(){
         /*Here we clean variables*/
        PreferenceManager.getInstance(this).setIsInRoute(false);
        PreferenceManager.getInstance(this).setRouteId(0);
        PreferenceManager.getInstance(this).setIsDoingCheckIn(false);
        PreferenceManager.getInstance(this).setCheckPointData("");
        PreferenceManager.getInstance(this).setCheckPointLocation("");
        PreferenceManager.getInstance(this).setPreviousSession(false);

        /*Here we logout from Salesforce session*/
        SalesforceSDKManager.getInstance().logout(this);

        /*Here we track the user action*/
        DatabaseManager.getInstance().saveUserAction(getCurrentContext(),getString(R.string.logout_success));

        /*Here we close the activity*/
        finish();
    }

    /**
     * This method help us to show a single progress dialog
     */
    public void showProgressDialog(int message){
        mMaterialProgressDialog = new MaterialDialog.Builder(this)
                .title(R.string.app_name)
                .content(message)
                .progress(true,0)
                .widgetColor(getResources().getColor(R.color.colorPrimary))
                .cancelable(false)
                .show();

    }

    /**
     * This method help us to hide the progress
     * dialog
     */
    public void hideProgressDialog(){
        mMaterialProgressDialog.dismiss();
    }

    /**
     * This method help us to send all the visit
     * to sales force account
     */
    public void sendVisitToSalesForce(final List<CheckPointLocation> visits, final String routeId, final int position){
        if(position<visits.size()){
            ApiManager.getInstance().makeVisitUpsert(this, routeId, visits.get(position), new ApiManager.OnObjectListener() {
                @Override
                public void onObject(boolean success, JSONObject jsonObject, String errorMessage) {
                    if(success){
                        try {
                            if(jsonObject.getBoolean("success")){

                                if(visits.get(position).isUpdateAddress()){

                                    updateAddressLocation(visits,routeId,position);

                                }else{
                                    if(visits.get(position).getRecordType().compareTo("0126A000000l3D4QAI")==0){
                                        if(visits.get(position).isMainTechnical()){
                                            sendSingImage(visits.get(position).getWorkOrderId(),visits,routeId,position);
                                        }else{
                                            updateWorkOrderXTechnical(visits,routeId,position);
                                        }
                                    }else{
                                        sendVisitToSalesForce(visits,routeId,position+1);
                                    }
                                }

                                /*Here we update the visit sync state*/
                                updateVisitSyncState(visits.get(position));

                            }else{
                                showMessage(getString(R.string.text_data_no_saved_info, "visita", "Error al obtener la respuesta desde SF."));
                                hideProgressDialog();
                                Crashlytics.log(Log.ERROR, "VisitNoSavedJson", "jsonObject has not success message");
                                Crashlytics.log(Log.ERROR, "VisitNoSavedJson", errorMessage.toString());
                                Crashlytics.log(Log.ERROR, "VisitNoSavedJson", jsonObject.toString());
                            }
                        } catch (JSONException e) {
                            showMessage(getString(R.string.text_data_no_saved_info, "visita", "Excepción: " + e.toString()));
                            hideProgressDialog();
                            Crashlytics.log(Log.ERROR, "VisitNoSavedException", e.toString());
                            Crashlytics.logException(e);
                        }
                    }else{
                        showMessage(getString(R.string.text_data_no_saved_info, "visita", "Error: " + errorMessage.toString()));
                        hideProgressDialog();
                        Crashlytics.log(Log.ERROR, "VisitNoSavedSFError", errorMessage.toString());
                        Crashlytics.logException(new Exception(errorMessage.toString()));
                    }
                }
            });
        }else{
            /*Here we track the user action*/
            DatabaseManager.getInstance().saveUserAction(this,getString(R.string.route_visits_register_success)+" "+routeId);
            PreferenceManager.getInstance(this).setIsInRoute(false);
            PreferenceManager.getInstance(this).setIsDoingCheckIn(false);
            PreferenceManager.getInstance(this).setCheckPointLocation("");
            PreferenceManager.getInstance(this).setCheckPointData("");

            //Here we save the current route id to show the results
            long currentRouteId = PreferenceManager.getInstance(this).getRouteId();

            //Here we clean the route id from the preferences
            PreferenceManager.getInstance(this).setRouteId(0);
            updateButtonUi();
            callHistoryActivity(currentRouteId);
            hideProgressDialog();
        }
    }

    /**
     * This method help us to update the address object
     * in sales force
     */
    public void updateAddressLocation(final List<CheckPointLocation> visits, final String routeId, final int position){

        String objectId;
        String objectName;
        HashMap<String,Object> dataSend = new HashMap<>();
        double latitude = visits.get(position).getLatitude();
        double longitude = visits.get(position).getLongitude();

        if(visits.get(position).getRecordType().compareTo("0126A000000l3CzQAI")==0){
            objectId= visits.get(position).getLeadId();
            objectName = "Lead";
        }else{
            objectId = visits.get(position).getAddressId();
            objectName = "Direcciones__c";
        }
        dataSend.put("Coordenadas__Latitude__s",latitude);
        dataSend.put("Coordenadas__Longitude__s",longitude);

        ApiManager.getInstance().updateAddressCoordinates(this, objectId,objectName,dataSend, new ApiManager.OnObjectListener() {
            @Override
            public void onObject(boolean success, JSONObject jsonObject, String errorMessage) {
                if(success){

                    if(visits.get(position).getRecordType().compareTo("0126A000000l3D4QAI")==0){
                        if(visits.get(position).isMainTechnical()){
                            sendSingImage(visits.get(position).getWorkOrderId(),visits,routeId,position);
                        }else{
                            updateWorkOrderXTechnical(visits,routeId,position);
                        }
                    }else{
                        sendVisitToSalesForce(visits,routeId,position+1);
                    }
                }else{
                    showMessage(getString(R.string.text_data_no_saved_info, "dirección", "Error: " + errorMessage));
                    hideProgressDialog();
                    Crashlytics.log(Log.ERROR, "AddressNoSavedSFError", errorMessage);
                    Crashlytics.logException(new Exception(errorMessage));
                }
            }
        });
    }

    /**
     * This method send the data to update
     * work order x technical
     */
    public void updateWorkOrderXTechnical(final List<CheckPointLocation> visits, final String routeId, final int position){
        ApiManager.getInstance().updateThecnicalWorkOrderData(this, visits.get(position), new ApiManager.OnObjectListener() {
            @Override
            public void onObject(boolean success, JSONObject jsonObject, String errorMessage) {
                if(success){
                    sendVisitToSalesForce(visits,routeId,position+1);
                }else{
                    showMessage(getString(R.string.text_data_no_saved_info, "orden de trabajo", "Error: " + errorMessage.toString()));
                    hideProgressDialog();
                    Crashlytics.log(Log.ERROR, "WorkOrderNoSavedSFError", errorMessage.toString());
                    Crashlytics.logException(new Exception(errorMessage.toString()));
                }
            }
        });

    }

    /**
     * This method help us to update the address object
     * in sales force
     */
    public void sendSingImage(String workOrderId,final List<CheckPointLocation> visits, final String routeId, final int position){
        try {
            File file = new File(visits.get(position).getSignatureFilePath());

            ApiManager.getInstance().sendSingToSalesForce(this, workOrderId, file.getPath(), file.getName(), new ApiManager.OnObjectListener() {
                @Override
                public void onObject(boolean success, JSONObject jsonObject, String errorMessage) {
                    if (success) {
                        Utility.logLargeString("json imagen " + jsonObject.toString());
                        updateWorkOrderXTechnical(visits, routeId, position);
                    } else {
                        Utility.logLargeString("Error guardando imagen");
                        showMessage(getString(R.string.text_data_no_saved_info, "firma electrónica", "Error: " + errorMessage.toString()));
                        hideProgressDialog();
                        Crashlytics.log(Log.ERROR, "SignImagetNoSavedSFError", errorMessage.toString());
                        Crashlytics.logException(new Exception(errorMessage.toString()));
                    }
                }
            });
        } catch (Exception e) {
            // Just update WorkOrder
            updateWorkOrderXTechnical(visits, routeId, position);
        }
    }


    /**
     * This method help su to calculate
     * the route distance
     */
    public double getRoutDistanceFirstWay(){

        double distance=0.00;
        List<UserLocation> userLocations = DatabaseManager.getInstance().getUserLocationList(PreferenceManager.getInstance(this).getRouteId());

        if(userLocations.size()>0){

            for(int i=0;i<userLocations.size();i++){
                if((i+1)<userLocations.size()){
                    UserLocation userLocationA = userLocations.get(i);
                    UserLocation userLocationB = userLocations.get(i+1);

                    Location locationA = new Location("");
                    Location locationB = new Location("");

                    locationA.setLongitude(userLocationA.getLongitude());
                    locationA.setLatitude(userLocationA.getLatitude());

                    locationB.setLongitude(userLocationB.getLongitude());
                    locationB.setLatitude(userLocationB.getLatitude());

                    distance = distance + locationA.distanceTo(locationB);
                }
            }

        }
        return (distance/1000.00);
    }


    /**
     * This method help su to calculate
     * the route distance
     */
    public double getRoutDistanceSecondWay(){

        double distance=0.00;
        List<UserLocation> userLocations = DatabaseManager.getInstance().getUserLocationList(PreferenceManager.getInstance(this).getRouteId());

        if(userLocations.size()>0){

            for(int i=0;i<userLocations.size();i++){
                if((i+1)<userLocations.size()){
                    UserLocation userLocationA = userLocations.get(i);
                    UserLocation userLocationB = userLocations.get(i+1);

                    float[] values = new float[3];
                    Location.distanceBetween(userLocationA.getLatitude(),userLocationA.getLongitude(),
                            userLocationB.getLatitude(),userLocationB.getLongitude(),values);

                    distance = distance + (values[0]);
                }
            }

        }
        return (distance/1000.00);
    }


    /**
     * This method help su to calculate
     * the route distance
     */
    public double getRoutDistanceThirdWay(){

        double distance=0.00;
        List<UserLocation> userLocations = DatabaseManager.getInstance().getUserLocationList(PreferenceManager.getInstance(this).getRouteId());

        if(userLocations.size()>0){

            for(int i=0;i<userLocations.size();i++){
                if((i+1)<userLocations.size()){
                    UserLocation userLocationA = userLocations.get(i);
                    UserLocation userLocationB = userLocations.get(i+1);

                    distance = distance + + Utility.distanceInKilometers(userLocationA.getLatitude(),userLocationA.getLongitude(),
                            userLocationB.getLatitude(),userLocationB.getLongitude());
                }
            }

        }
        return distance;
    }

    /**
     * This method show a question before to send
     * an error report
     */
    public void showDataBaseBackUpQuestion(){
        new MaterialDialog.Builder(this)
                .title(R.string.app_name)
                .content(R.string.back_up_question)
                .positiveColorRes(R.color.colorPrimary)
                .positiveText(R.string.send_report)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        sendDataBaseBackUp();
                    }
                })
                .negativeColorRes(R.color.colorPrimary)
                .negativeText(R.string.cancel)
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                    }
                })
                .cancelable(false)
                .show();
    }


    /**
     * This method show a question before to clean
     * the database
     */
    public void showDataBaseCleanQuestion(){
        new MaterialDialog.Builder(this)
                .title(R.string.app_name)
                .content(R.string.clean_database_question)
                .positiveColorRes(R.color.colorPrimary)
                .positiveText(R.string.clean_data_base)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                      cleanAllDataBase();
                    }
                })
                .negativeColorRes(R.color.colorPrimary)
                .negativeText(R.string.cancel)
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                    }
                })
                .cancelable(false)
                .show();
    }

    /**
     * This method help us to clean all the data base
     */
    private void cleanAllDataBase(){
        new CleanDatabaseAsyncTask( new OnDatabaseCleanListener() {
            @Override
            public void onDatabaseClean(int messageResource, int totalItems) {

                /*Here we track the user action*/
                DatabaseManager.getInstance().saveUserAction(
                        getApplicationContext(),getString(messageResource)+" "+totalItems);

                /*Here we show success message*/
                showMessage(getString(messageResource)+" "+totalItems);
            }
        }).execute();
    }

    /**
     * This method help us to prepare and send
     * a data base backup to sales force
     */
    public void sendDataBaseBackUp(){
        /*Here we track the user action*/
        DatabaseManager.getInstance().saveUserAction(this,getString(R.string.back_up_start));

        showProgressDialog(R.string.text_getting_data_base_file);

        new NetworkUtilsTask(this, new OnNetworkListener() {
            @Override
            public void onNetwork(boolean success, String message) {
                if(success){
                    new BackupAsyncTask(getCurrentContext(), new OnBackUpListener() {
                        @Override
                        public void onBackUp(boolean success, String message, final List<File> dataBaseBuckUp) {
                            if(success){
                                ApiManager.getInstance().makeBackUpUpsert(getCurrentContext(), new ApiManager.OnObjectListener() {

                                    @Override
                                    public void onObject(boolean success, JSONObject jsonObject, String errorMessage) {
                                        if(success){
                                            try {
                                                if(jsonObject.getBoolean("success")){

                                                    /*Here we get the sales force route id*/
                                                    String backUpId = jsonObject.getString("id");

                                                    /*Here we send data to the sales force*/
                                                    uploadDataBaseFile(backUpId,dataBaseBuckUp,0);
                                                }
                                            } catch (JSONException e) {
                                                hideProgressDialog();
                                                showMessage(R.string.text_error_sending_data_base_file);
                                                DatabaseManager.getInstance().saveUserAction(getCurrentContext(),getString(R.string.text_error_sending_data_base_file)+" "+
                                                e.getMessage().replace(",",""));
                                            }
                                        }else{
                                            hideProgressDialog();
                                            showMessage(R.string.text_error_sending_data_base_file);
                                            DatabaseManager.getInstance().saveUserAction(getCurrentContext(),getString(R.string.text_error_sending_data_base_file)+" "+
                                            errorMessage.replace(",",""));
                                        }
                                    }
                                });
                            }else{
                                hideProgressDialog();
                                showMessage(message);
                            }
                        }
                    }).execute();
                }else{
                    hideProgressDialog();
                    showMessage(message);
                }
            }
        }).execute();
    }

    /**
     * This method send the csv file to the backup record
     */
    public void uploadDataBaseFile(final String backupId, final List<File> backupFiles, final int position){
        if(position < backupFiles.size()){
            ApiManager.getInstance().sendBackUpFile(getCurrentContext(), backupId,
                    backupFiles.get(position), new ApiManager.OnObjectListener() {
                        @Override
                        public void onObject(boolean success, JSONObject jsonObject, String errorMessage) {
                            if(success){
                                //Utility.logLargeString("json backup " + jsonObject.toString());
                                uploadDataBaseFile(backupId,backupFiles,position+1);
                            }else{
                                /*Here we track the user action*/
                                DatabaseManager.getInstance().saveUserAction(getApplicationContext(),getString(R.string.text_error_sending_data_base_file)
                                +" "+errorMessage.replace(",",""));
                                hideProgressDialog();
                                showMessage(R.string.text_error_sending_data_base_file);
                            }
                        }
                    });
        }else{
            /*Here we track the user action*/
            DatabaseManager.getInstance().saveUserAction(this,getString(R.string.back_up_finish_success));
            hideProgressDialog();
            showMessage(R.string.text_data_base_file_sent);
        }
    }

    /**
     * This method get the current context
     */
    public Activity getCurrentContext(){
        return this;
    }

    @Override
    public void onBecameForeground() {
        if(!PreferenceManager.getInstance(this).isDoingCheckIn()){
            //Here we update the user location
            getUserLocation();
        }
    }

    @Override
    public void onBecameBackground() {

    }

    /**
     * This method help us to center the current location
     * in the map
     */
    @OnClick(R.id.image_view_my_location)
    public void centerCurrentLocation(){
        /*Here we add the new one*/
        if(mMarker!= null){

            LatLng currentPosition = mMarker.getPosition();

            //Here we update the camera position
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(currentPosition)
                    .tilt(90)
                    .zoom(mMap.getCameraPosition().zoom >=17f ? mMap.getCameraPosition().zoom : 20f)
                    .build();


            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), 1500, null);

        }
    }

    /**
     * This dialog help us to set the current
     * odometer data for the route
     */
    public void showStartOdometerDialog(final int radius){
        new MaterialDialog.Builder(this)
                .title(R.string.current_odometer_data)
                .contentColorRes(R.color.colorPrimaryDark)
                .content(R.string.current_odometer_data_message)
                .positiveColorRes(R.color.colorPrimary)
                .positiveText(R.string.accept)
                .negativeColorRes(R.color.colorPrimary)
                .negativeText(R.string.cancel)
                .inputType(InputType.TYPE_CLASS_NUMBER)
                .inputRangeRes(1, 6, R.color.colorRed)
                .input(R.string.current_odometer, 0, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(MaterialDialog dialog, CharSequence input) {
                        saveRouteInDataBase(radius,Integer.parseInt(input.toString()));
                    }
                })
                .cancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        mTxvRouteButton.setVisibility(View.VISIBLE);
                    }
                })
                .widgetColorRes(R.color.colorPrimary)
                .show();
    }

    /**
     * This dialog help us to set the current
     * odometer data for the route
     */
    public void showEndOdometerDialog(){
        new MaterialDialog.Builder(this)
                .title(R.string.current_odometer_data)
                .contentColorRes(R.color.colorPrimaryDark)
                .content(R.string.current_odometer_data_message)
                .positiveColorRes(R.color.colorPrimary)
                .positiveText(R.string.accept)
                .negativeColorRes(R.color.colorPrimary)
                .negativeText(R.string.cancel)
                .inputType(InputType.TYPE_CLASS_NUMBER)
                .inputRangeRes(1, 6, R.color.colorRed)
                .input(R.string.current_odometer, 0, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(MaterialDialog dialog, CharSequence input) {
                        //Here we get the end odometer value
                        int endOdometer = Integer.parseInt(input.toString());
                        if(endOdometer >PreferenceManager.getInstance(getCurrentContext()).getStartOdometer()){
                            updateRouteDate(endOdometer);
                        }else{
                            Toast.makeText(getCurrentContext(),R.string.end_odometer_grater_than_start_odometer,Toast.LENGTH_LONG).show();
                            showEndOdometerDialog();
                        }
                    }
                })
                .widgetColorRes(R.color.colorPrimary)
                .show();
    }

    /**
     * This method help us to save and created the
     * route in the database
     */
    public void saveRouteInDataBase(final int radius, final int odometer){
        Realm realm = null;
        try{
            realm = Realm.getDefaultInstance();
            realm.executeTransaction(new Realm.Transaction(){
                @Override
                public void execute(Realm realm) {

                    Route route = new Route();
                    route.setId(System.currentTimeMillis());

                    /**Here we create the route name*/
                    String routeTypeString ="";
                    if(Utility.getUserRole() == Utility.Roles.SELLER){
                        routeTypeString = "Venta";
                    }else{
                        routeTypeString = "Tecnica";
                    }
                    String sDate  = Utility.getDateForNameSimple();
                    String sDName = Utility.capitalize(Utility.getRestClient().getClientInfo().displayName).replace(" ", "");
                    String name = sDate+"-"+sDName+"-"+routeTypeString+
                            "-"+ DatabaseManager.getInstance().getCorrelativeRoute(Utility.getDateForSearch());
                    route.setName(name);
                    route.setStartDate(Utility.getCurrentDate());
                    route.setUserId(Utility.getRestClient().getClientInfo().userId);
                    route.setTypeId(Utility.getUserProfileId());
                    route.setStartLatitude(mLatitude);
                    route.setStartLongitude(mLongitude);
                    route.setStatus("Iniciada");
                    route.setSync("0");
                    route.setStartOdometer(odometer);
                    /*Here we sav rhe route in the local data base*/
                    realm.copyToRealmOrUpdate(route);
                    PreferenceManager.getInstance(getApplicationContext()).setIsInRoute(true);
                    PreferenceManager.getInstance(getApplicationContext()).setRadius(radius);
                    PreferenceManager.getInstance(getApplicationContext()).setRouteId(route.getId());
                    PreferenceManager.getInstance(getApplicationContext()).setStartOdometer(odometer);

                    mTxvRouteButton.setVisibility(View.VISIBLE);
                    updateButtonUi();
                    Log.d("REALM", "ROUTE SUCCESS");
                    Log.d("ROUTE NAME", name);
                    mRouteCreatedSuccess=true;
                }
            });
        }catch(Exception e){
            Log.d("REALM ERROR", e.toString());
            mTxvRouteButton.setVisibility(View.VISIBLE);
            updateButtonUi();
            showMessage(R.string.text_no_route_created);
            PreferenceManager.getInstance(getApplicationContext()).setIsInRoute(false);
            PreferenceManager.getInstance(getApplicationContext()).setRouteId(0);
        }finally {
            if(realm != null){
                realm.close();
                if(mRouteCreatedSuccess){
                                    /*Here we track the user action*/
                    DatabaseManager.getInstance().saveUserAction(this,getString(R.string.route_created_local)+" "+
                            PreferenceManager.getInstance(getCurrentContext()).getRouteId());
                    mRouteCreatedSuccess=false;
                }
            }
        }
    }
}
