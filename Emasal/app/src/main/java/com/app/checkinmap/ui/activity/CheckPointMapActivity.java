package com.app.checkinmap.ui.activity;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Chronometer;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.app.checkinmap.R;
import com.app.checkinmap.bus.BusProvider;
import com.app.checkinmap.bus.NewLocationEvent;
import com.app.checkinmap.db.DatabaseManager;
import com.app.checkinmap.model.CheckPointData;
import com.app.checkinmap.model.CheckPointLocation;
import com.app.checkinmap.model.Contact;
import com.app.checkinmap.service.LocationService;
import com.app.checkinmap.util.ApiManager;
import com.app.checkinmap.util.NetworkUtilsTask;
import com.app.checkinmap.util.OnNetworkListener;
import com.app.checkinmap.util.PreferenceManager;
import com.app.checkinmap.util.Utility;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.otto.Subscribe;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.Realm;

import static com.app.checkinmap.ui.activity.SignatureActivity.ARG_SING_FILE_PATH;
import static com.app.checkinmap.ui.activity.SignatureActivity.RESULT_NO_SIGNATURE_FILE;
import static com.app.checkinmap.ui.activity.SignatureActivity.WHO_SIGNS;
import static com.app.checkinmap.ui.activity.SignatureJustificationActivity.JUSTIFICATION;

public class CheckPointMapActivity extends AppCompatActivity implements OnMapReadyCallback, ActivityCompat.OnRequestPermissionsResultCallback{

    public static final int    REQUEST_CHECK_IN =             79;
    public static final int    PERMISSION_LOCATION_REQUEST =  12;
    public static final int    SIGNATURE_REQUEST =            15;
    public static final int    SIGNATURE_JUSTIFICATION =      16;
    public static String       ARG_CHECK_POINT_DATA=          "check_point_data";
    public static final int    CHECK_DISTANCE =               Utility.getRadioCheckIn();
    public static final String ARG_CHECK_POINT_LOCATION_ID=   "check_point_location_id";

    @BindView(R.id.linear_layout_check_progress)
    LinearLayout mLnlCheckProgress;

    /*@BindView(R.id.chronometer_view)
    Chronometer mChronometer;*/

    @BindView(R.id.button_check)
    TextView mBtnCheck;

    @BindView(R.id.linear_layout_progress)
    LinearLayout mMapProgress;

    @BindView(R.id.map_progress_message)
    TextView mMapProgressMessage;

    @BindView(R.id.text_view_start_date)
    TextView mTxvStartDate;

    private GoogleMap                   mMap;
    private boolean                     mIsChecking=false;
    private boolean                     mLocationPermissionGranted=false;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private CheckPointLocation          mCheckPointLocation;
    private LocationManager             mLocationManager;
    private boolean                     mLocationSettingCalled=false;
    private Circle                      mCircle;
    private CheckPointData              mCheckPointData;
    private boolean                     mFirstLoadRequest=true;
    private boolean                     mNoAddressLocation=false;
    private boolean                     mRestoredCheckInProgress = false;
    private int                         mWorkOrderStatus = 0;
    /**
     * This method help us to get a single
     * map instance
     */
    public static Intent getIntent(Context context, CheckPointData checkPointData){
        Intent intent = new Intent(context,CheckPointMapActivity.class);
        intent.putExtra(ARG_CHECK_POINT_DATA,checkPointData);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_point_map);

        ButterKnife.bind(this);

        /*We check if the app come from background*/
        if(PreferenceManager.getInstance(this).isDoingCheckIn()){
            mCheckPointData = new Gson().fromJson(PreferenceManager.getInstance(this).getCheckPointData(),CheckPointData.class);
            mCheckPointLocation = new Gson().fromJson(PreferenceManager.getInstance(this).getCheckPointLocation(),CheckPointLocation.class);
            /*Here we track the user action*/
            DatabaseManager.getInstance().saveUserAction(this,getString(R.string.restored_check_in));
        }else{
            /*Here we get the account address */
            mCheckPointData = getIntent().getExtras().getParcelable(ARG_CHECK_POINT_DATA);
        }

        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(mCheckPointData.getName());
        }

        //Here we get the location manager
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // Construct a FusedLocationProviderClient.
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

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


    /**
     * This method help us to check if
     * we the permissions
     */
    private  boolean checkAndRequestPermissions() {
        int locationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        List<String> listPermissionsNeeded = new ArrayList<>();
        if (locationPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }

        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]),PERMISSION_LOCATION_REQUEST);
            mLocationPermissionGranted=false;
            return false;
        }
        mLocationPermissionGranted=true;
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case PERMISSION_LOCATION_REQUEST: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    mLocationPermissionGranted=true;
                    getUserLocation();
                } else {
                    showRationale();
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case SIGNATURE_REQUEST:
               if(resultCode==RESULT_OK){
                    String whoSigns = data.getExtras().getString(WHO_SIGNS);
                    mCheckPointLocation.setSignatureFilePath(data.getExtras().getString(ARG_SING_FILE_PATH));
                    updateWorkOrderSignature(whoSigns, "SI", "");
                }else if(resultCode == RESULT_NO_SIGNATURE_FILE){
                   showMessage(R.string.signature_error_dialog_message);
               }
                break;
            case SIGNATURE_JUSTIFICATION:
                if(resultCode==RESULT_OK) {
                    String justification = data.getExtras().getString(JUSTIFICATION);
                    updateWorkOrderSignature("", "NO", justification);
                }
                break;
        }
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
     * This method help us to get the
     * flg to indicate in the location
     * services is running or not
     */
    private boolean isLocationServiceEnabled(){
        return PreferenceManager.getInstance(this).isServiceEnabled();
    }

    @OnClick(R.id.button_check)
    public void checkUserLocation(){

        if(mIsChecking){
            showCheckInFinalizeMessage();
        }else{
            if(mCheckPointData.getCheckPointType()==3){
                if(mCheckPointData.isIsMainTechnical()){
                    mBtnCheck.setVisibility(View.GONE);
                    mMapProgressMessage.setText(R.string.text_work_order_updating_status);
                    mMapProgress.setVisibility(View.VISIBLE);
                    updateWorkOrderStatus(1);
                }else{
                    startCheckInFlow();
                }
            }else{
                startCheckInFlow();
            }
        }
    }

    /**
     * This method help us to start the check in flow
     */
    public void startCheckInFlow(){
        mIsChecking = true;
        mBtnCheck.setVisibility(View.GONE);
        mMapProgressMessage.setText(R.string.getting_your_location);
        mMapProgress.setVisibility(View.VISIBLE);
        getUserLocation();
        // Stop location service after Check In
        stopLocationService();
    }

    /**
     * Here we update the work order status
     */
      public void updateWorkOrderStatus(final int status){

          mBtnCheck.setVisibility(View.GONE);
          mMapProgressMessage.setText(R.string.updating_work_order_state);
          mMapProgress.setVisibility(View.VISIBLE);

          String statusName = "";
            if (status == 0) {
                statusName = "In Process"; // Open
            }else if(status == 1){
                statusName = "In Process";
            }else{
                statusName = "Finalizada Tecnico";
            }

          DatabaseManager.getInstance().saveUserAction(this,getString(R.string.work_order_status_change_send)
                  +" "+mCheckPointData.getId()+" "+statusName);
            /*Here we update the work order status*/
            ApiManager.getInstance().updateWorkOrderStatus(this, mCheckPointData.getId(), statusName, new ApiManager.OnObjectListener() {
                @Override
                public void onObject(boolean success, JSONObject jsonObject, String errorMessage) {
                    if(success){
                        //Utility.logLargeString(jsonObject.toString());
                        if(status == 1){

                            /*Here we track the user action*/
                            DatabaseManager.getInstance().saveUserAction(getApplicationContext(),getString(R.string.work_order_status_change_success)
                                    +" "+mCheckPointData.getId());
                            startCheckInFlow();
                        }else{
                            finishCheckIn();
                        }
                    }else{
                        showMessage(R.string.text_work_order_status_no_updated);
                        mMapProgress.setVisibility(View.GONE);
                        mBtnCheck.setVisibility(View.VISIBLE);
                    }
                }
            });
      }

    /**
     * Here we update the work order signature option
     */
      public void updateWorkOrderSignature(String whoSigns, String hasSignature, String justification) {

          mBtnCheck.setVisibility(View.GONE);
          mMapProgressMessage.setText(R.string.text_work_order_updating_status);
          mMapProgress.setVisibility(View.VISIBLE);


          ApiManager.getInstance().updateWorkOrderSignature(this, mCheckPointData.getId(), whoSigns, hasSignature, justification, new ApiManager.OnObjectListener() {
              @Override
              public void onObject(boolean success, JSONObject jsonObject, String errorMessage) {
                  if(success){
                      System.out.println("Success! WorkOrderSignature");
                      getUserLocationToFinalize();
                  }else{
                      showMessage(errorMessage);
                      mBtnCheck.setVisibility(View.VISIBLE);
                      mMapProgress.setVisibility(View.GONE);
                  }
              }
          });
      }

      public void updateWorkOrderStartTime(String startTime) {
          ApiManager.getInstance().updateWorkOrderStartTime(this, mCheckPointData.getId(), startTime, new ApiManager.OnObjectListener() {
              @Override
              public void onObject(boolean success, JSONObject jsonObject, String errorMessage) {
                  if(success){
                      System.out.println("Success! WorkOrderStartTime");
                  }
              }
          });
      }

      public void updateWorkOrderFinalHour(String finalHour) {
          ApiManager.getInstance().updateWorkOrderFinalHour(this, mCheckPointData.getId(), finalHour, new ApiManager.OnObjectListener() {
              @Override
              public void onObject(boolean success, JSONObject jsonObject, String errorMessage) {
                  if(success){
                      System.out.println("Success! WorkOrderFinalHour");
                  }
              }
          });
      }

    @Override
    protected void onStart() {
        super.onStart();
        BusProvider.getInstance().register(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Utility.checkSFSession(getApplicationContext());
        if(mLocationSettingCalled){
            mLocationSettingCalled=false;
            if(isGpsEnable() && mIsChecking == false){ // NUEVO! No probado!
                startLocationService();
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

    @Subscribe
    public void getNewLocation(NewLocationEvent newLocationEvent){
        Log.d("LOCATION LAT:" , String.valueOf(newLocationEvent.getLat()));
        Log.d("LOCATION LON:" , String.valueOf(newLocationEvent.getLon()));

        /*Here we sav the user location to future use*/
        mCheckPointData.setUserLatitude(newLocationEvent.getLat());
        mCheckPointData.setUserLongitude(newLocationEvent.getLon());

        /*Here we draw the location in the map*/
        drawUserAndAccountLocation(newLocationEvent.getLat(),newLocationEvent.getLon());
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
                                if(mFirstLoadRequest){

                                    /*This is the first load in the activity and we need to show only the user location
                                     *and account address location
                                     */
                                    mFirstLoadRequest= false;
                                    drawUserAndAccountLocation(location.getLatitude(),location.getLongitude());
                                }else{
                                   /*Ths location was requested by the user to make the check in for the account address
                                   */
                                    saveCheckData(location.getLatitude(),location.getLongitude());
                                }
                            }else{
                               showMessage(R.string.no_user_location_available);
                                restartCheckInUi();
                            }
                        } else {
                            showMessage(R.string.no_user_location_available);
                            restartCheckInUi();
                        }
                    }
                });
            }
        } catch(SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    /**
     * This method help us to restart the
     * check in UI
     */
    public void restartCheckInUi(){
        mIsChecking=false;
        mBtnCheck.setText(R.string.check_in);
        //mChronometer.stop();
        //mChronometer.setVisibility(View.INVISIBLE);
        mLnlCheckProgress.setVisibility(View.GONE);
        mMapProgress.setVisibility(View.GONE);
        mBtnCheck.setVisibility(View.VISIBLE);
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
        new AlertDialog.Builder(this)
                .setTitle(R.string.app_name)
                .setMessage(R.string.gps_disable_message)
                .setPositiveButton(R.string.go_to_settings, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mLocationSettingCalled=true;
                        openGpsSettings();
                        dialogInterface.dismiss();
                    }
                }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        }).setCancelable(false).show();
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
     * This method help us to show a message
     * to indicate the check in is going to
     * finalize
     */
    public void showCheckInFinalizeMessage(){

        new MaterialDialog.Builder(this)
                .title(R.string.app_name)
                .content(R.string.check_in_finalize)
                .positiveColorRes(R.color.colorPrimary)
                .positiveText(R.string.accept)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                        if(mCheckPointData.getCheckPointType() ==3){
                            if(mCheckPointData.isIsMainTechnical()){
                                showWorkOrderStatusMessage();
                            } else {
                                checkSingActivity(); // Digital Signature
                            }

                        }else{
                            showCommentDialog();
                        }
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
     * This method help us to show a message
     * to indicate the check in is going to
     * finalize
     */
    public void showWorkOrderStatusMessage(){
        new MaterialDialog.Builder(this)
            .title(R.string.app_name)
            .content(R.string.work_order_status)
            .positiveColorRes(R.color.colorPrimary)
            .positiveText(R.string.yes)
            .onPositive(new MaterialDialog.SingleButtonCallback() {
                @Override
                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                    //WorkOrder  Close
                    mWorkOrderStatus = 2;
                    DatabaseManager.getInstance().saveUserAction(getCurrentContext(),getString(R.string.work_order_finalized));
                    checkSingActivity(); // Digital Signature

                }
            })
            .negativeColorRes(R.color.colorPrimary)
            .negativeText(R.string.no)
            .onNegative(new MaterialDialog.SingleButtonCallback() {
                @Override
                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                    // WorkOrder Open
                    mWorkOrderStatus = 0;
                    DatabaseManager.getInstance().saveUserAction(getCurrentContext(),getString(R.string.work_order_no_finalized));
                    getUserLocationToFinalize();

                }
            })
            .cancelable(false)
            .show();
    }

    /**
     * This method show a message explaining
     * that the selected account doesnt have
     * a location in the data base
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


    /**
     * This method show a message explaining
     * that the selected account doesnt have
     * a location in the data base
     */
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
     * This method show a message explaining
     * that the selected account doesnt have
     * a location in the data base
     */
    public void showMessageWithCancel(int message){

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
            .negativeText(R.string.cancel)
            .negativeColorRes(R.color.colorPrimary)
            .onNegative(new MaterialDialog.SingleButtonCallback() {
                @Override
                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                    dialog.cancel();
                    finish();

                }
            })
            .show();
    }

    /**
     * This method show a dialog with all the
     * visit type for the check in
     */
    public void showVisitTypeMessage(){
        new MaterialDialog.Builder(this)
                .title(R.string.select_visit_type)
                .items(R.array.visit_type)
                .itemsCallbackSingleChoice(0, new MaterialDialog.ListCallbackSingleChoice() {
                    @Override
                    public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                        /*Here we save the visit type*/
                        mCheckPointLocation.setVisitType(text.toString());

                        /*Here we show the map progress barr*/
                        mMapProgressMessage.setText(R.string.getting_contact_list);
                        mMapProgress.setVisibility(View.VISIBLE);
                        getContactList();

                        return true;
                    }
                })
                .widgetColorRes(R.color.colorPrimary)
                .positiveText(R.string.accept)
                .positiveColorRes(R.color.colorPrimary)
                .cancelable(false)
                .negativeText(R.string.cancel)
                .negativeColorRes(R.color.colorPrimary)
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.cancel();
                       restartCheckInUi();

                    }
                })
                .show();
    }

    /**
     * This method show a message
     * asking if digital signature
     * is required
     */
    public void showMessageDigitalSignature(){

        new MaterialDialog.Builder(this)
            .title(R.string.app_name)
            .content(R.string.digital_signature_available)
            .positiveColorRes(R.color.colorPrimary)
            .positiveText(R.string.yes)
            .onPositive(new MaterialDialog.SingleButtonCallback() {
                @Override
                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                    DatabaseManager.getInstance().saveUserAction(getCurrentContext(),getString(R.string.signature_available));
                    dialog.dismiss();
                    startActivityForResult(SignatureActivity.getIntent(getApplicationContext(),mCheckPointData.getName(),mCheckPointData.getWorkOrderNumber()),SIGNATURE_REQUEST);
                }
            })
            .cancelable(false)
            .negativeText(R.string.no)
            .negativeColorRes(R.color.colorPrimary)
            .onNegative(new MaterialDialog.SingleButtonCallback() {
                @Override
                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                    DatabaseManager.getInstance().saveUserAction(getCurrentContext(),getString(R.string.signature_no_available));
                    dialog.dismiss();
                    startActivityForResult(SignatureJustificationActivity.getIntent(getApplicationContext(), "Name"), SIGNATURE_JUSTIFICATION);
                }
            })
            .show();
    }

    /**
     * This method help us to save the user
     * check in data
     */
    public void saveCheckData(double latitude, double longitude){

        if (Utility.getRestClient() != null) {
            if (mIsChecking) {
                if (isInRadius(latitude, longitude)) {
                    mCheckPointLocation = new CheckPointLocation();
                    mCheckPointLocation.setId(System.currentTimeMillis());
                    mCheckPointLocation.setCheckInLatitude(latitude);
                    mCheckPointLocation.setCheckInLongitude(longitude);
                    mCheckPointLocation.setCheckInDate(Utility.getCurrentDate());
                    mCheckPointLocation.setAddressId(mCheckPointData.getAddressId());
                    if (mCheckPointData.isUpdateAddress()) {
                        mCheckPointLocation.setLongitude(mCheckPointData.getLongitude());
                        mCheckPointLocation.setLatitude(mCheckPointData.getLatitude());
                        mCheckPointLocation.setUpdateAddress(true);
                    } else {
                        mCheckPointLocation.setUpdateAddress(false);
                    }

                /*Here we hide the progress*/
                    mMapProgress.setVisibility(View.GONE);

                /*Here we ask the visit type*/
                    switch (mCheckPointData.getCheckPointType()) {
                        case 1:
                            mCheckPointLocation.setAddressId(mCheckPointData.getAddressId());
                            mCheckPointLocation.setRecordType("0126A000000l3CuQAI");
                            mCheckPointLocation.setMainTechnical(false);
                            showVisitTypeMessage();
                            break;
                        case 2:
                            mCheckPointLocation.setVisitType("Prospecto");
                            mCheckPointLocation.setLeadId(mCheckPointData.getId());
                            mCheckPointLocation.setRecordType("0126A000000l3CzQAI");
                            mCheckPointLocation.setAccountContactName(mCheckPointData.getName());
                            mCheckPointLocation.setMainTechnical(false);
                           // startCheck();
                            showOdometerDialog();
                            break;
                        case 3:
                            mCheckPointLocation.setWorkOderXTechnicalId(mCheckPointData.getWorkOderXTechnicalId());
                            mCheckPointLocation.setWorkOrderId(mCheckPointData.getId());
                            mCheckPointLocation.setWorkOrderContactId(mCheckPointData.getContactId());
                            mCheckPointLocation.setTechnicalId(Utility.getRestClient().getClientInfo().userId);
                            mCheckPointLocation.setRecordType("0126A000000l3D4QAI");
                            mCheckPointLocation.setAccountContactName(mCheckPointData.getContactName());
                            if (mCheckPointData.isIsMainTechnical()) {
                                mCheckPointLocation.setMainTechnical(true);
                                // Update Work Order start time
                                System.out.println(mCheckPointData.getmWorkOrderStatus());
                                if (mCheckPointData.getmWorkOrderStatus().equals("Open") || mCheckPointData.getmWorkOrderStatus().equals("Abierto")) {
                                    updateWorkOrderStartTime(mCheckPointLocation.getCheckInDateSalesForceDate());
                                }
                            } else {
                                mCheckPointLocation.setMainTechnical(false);
                            }
                           // startCheck();
                            showOdometerDialog();
                            break;
                    }
                } else {
                    showMessage(R.string.no_check_in_available);
                    restartCheckInUi();
                }
            } else {

                //Here we update the progress message
                mBtnCheck.setVisibility(View.GONE);
                mMapProgressMessage.setText(R.string.saving_data);
                mMapProgress.setVisibility(View.VISIBLE);

                mCheckPointLocation.setCheckOutLatitude(latitude);
                mCheckPointLocation.setCheckOutLongitude(longitude);
                mCheckPointLocation.setCheckOutDate(Utility.getCurrentDate());


                String visitTypeName = "";
                switch (mCheckPointData.getCheckPointType()) {
                    case 1:
                        visitTypeName = "C"; // Contacto
                        break;
                    case 2:
                        visitTypeName = "L"; // Lead
                        break;
                    case 3:
                        visitTypeName = "T"; // Tecnica
                        break;
                }

                // Update work order final hour if it's closed
                if (mCheckPointData.isIsMainTechnical() && mWorkOrderStatus == 2) {
                    updateWorkOrderFinalHour(mCheckPointLocation.getCheckOutDateSalesForceDate());
                }

                /*Here we create the name*/
                String name = "";
                if (mCheckPointData.getName() != null) {
                    String sDate  = Utility.getDateForNameSimple();
                    String sName  = Utility.capitalize(mCheckPointData.getName()).replace(" ", "");
                    String sDName = Utility.capitalize(Utility.getRestClient().getClientInfo().displayName).replace(" ", "");
                    String sRoute = String.valueOf(DatabaseManager.getInstance().getCorrelativeCheckPoint(PreferenceManager.getInstance(this).getRouteId()));
                    name = sDate + "-" + sName + "-" + sDName + "-" + visitTypeName + "-" + sRoute;
                    if (name.length() > 80) {
                        sName = Utility.truncate(sName, 30);
                        sDName = Utility.truncate(sDName, 30);
                        name = sDate + "-" + sName + "-" + sDName + "-" + visitTypeName + "-" + sRoute;
                    }
                } else {
                    String sDate  = Utility.getDateForNameSimple();
                    String sUName = Utility.capitalize(Utility.getRestClient().getClientInfo().username).replace(" ", "");
                    String sRoute = String.valueOf(DatabaseManager.getInstance().getCorrelativeCheckPoint(PreferenceManager.getInstance(this).getRouteId()));
                    name = sDate + "-" + sUName + "-" + visitTypeName + "-" + sRoute;
                    if (name.length() > 80) {
                        sUName = Utility.truncate(sUName, 60);
                        name = sDate + "-" + sUName + "-" + visitTypeName + "-" + sRoute;
                    }
                }
                Log.d("checkP Name", name);
                mCheckPointLocation.setName(name);
                mCheckPointLocation.setRouteId(PreferenceManager.getInstance(this).getRouteId());
                mCheckPointLocation.setVisitTime(Utility.getDurationInHours(mCheckPointLocation.getCheckInDate(), mCheckPointLocation.getCheckOutDate()));
                mCheckPointLocation.setVisitTimeNumber(Utility.getDurationInHoursNumber(mCheckPointLocation.getCheckInDate(), mCheckPointLocation.getCheckOutDate()));
                String travelStartDate = DatabaseManager.getInstance().getTravelStartDate(PreferenceManager.getInstance(this).getRouteId());
                mCheckPointLocation.setTravelTime(Utility.getDurationInHours(travelStartDate, mCheckPointLocation.getCheckInDate()));
                mCheckPointLocation.setTravelTimeNumber(Utility.getDurationInHoursNumber(travelStartDate, mCheckPointLocation.getCheckInDate()));
                mCheckPointLocation.setAddress(mCheckPointData.getAddress());
                mCheckPointLocation.setSync("0");

                /*Here we save the data in Realm*/
                saveCheckPointLocation();
            }
        } else {
            // Some error
            showMessage(R.string.salesforce_error);
            //restartCheckInUi(); Commented 25/01/18
        }
    }


    /**
     * This method help us to  save the check data
     * in the local storage
     */
    private void saveCheckPointLocation(){
        Realm realm = null;
        try{
            realm = Realm.getDefaultInstance();
            realm.executeTransaction(new Realm.Transaction(){
                @Override
                public void execute(Realm realm) {
                    realm.copyToRealmOrUpdate(mCheckPointLocation);
                    Log.d("REALM CHECK POINT", "SUCCESS CHECK");
                }
            });
        }catch(Exception e){
            Log.d("REALM CHECK POINT ERROR", e.toString());
        }finally {
            if(realm != null){
                realm.close();
                if(mCheckPointData.getCheckPointType()==3){
                    if(mCheckPointData.isIsMainTechnical()){
                        updateWorkOrderStatus(mWorkOrderStatus);
                    }else{
                        finishCheckIn();
                    }
                }else{
                    finishCheckIn();
                }
            }
        }
    }

    public void finishCheckIn(){
         /*Here we return to route mode*/
        PreferenceManager.getInstance(this).setIsDoingCheckIn(false);
        PreferenceManager.getInstance(this).setCheckPointLocation("");
        PreferenceManager.getInstance(this).setCheckPointData("");

        Intent dataIntent = new Intent();
        dataIntent.putExtra(ARG_CHECK_POINT_LOCATION_ID,mCheckPointLocation.getId());

        /*Here we track the user action*/
        DatabaseManager.getInstance().saveUserAction(this,getString(R.string.check_in_end));

        /*Here we finalize the activity for result*/
        setResult(RESULT_OK,dataIntent);
        finish();
    }

    /**
     * This method help us to draw in the map the
     * user location and the account/work order
     * location
     */
    public void drawUserAndAccountLocation(double userLatitude, double userLongitude){
        if (mMap != null) {
            /*Here we clean the map*/
            mMap.clear();

            /*Here we remove the accuracy circle*/
            if(mCircle!=null){
                mCircle.remove();
            }

            /*here we add the accuracy circle with new user location*/
            CircleOptions circleOptions = new CircleOptions();
            circleOptions.center(new LatLng(userLatitude,userLongitude));
            circleOptions.radius(CHECK_DISTANCE);
            circleOptions.fillColor(R.color.colorBlackTransparent);
            circleOptions.strokeWidth(0.1f);
            mCircle = mMap.addCircle(circleOptions);

            /*Here we update the user location in the map*/
            mMap.addMarker(
                    new MarkerOptions()
                            .position(new LatLng(userLatitude, userLongitude))
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));



            /*Here we update the address location*/
            if(mCheckPointData.getLatitude()==0 && mCheckPointData.getLongitude()==0){

                mCheckPointData.setLatitude(userLatitude);
                mCheckPointData.setLongitude(userLongitude);
                mCheckPointData.setUpdateAddress(true);

                mNoAddressLocation=true;

                    /*Here we show an explanation*/
                if(mCheckPointData.getCheckPointType()==2){
                    showMessageWithCancel(R.string.no_address_description_leads);
                }else{
                    showMessageWithCancel(R.string.no_address_description);
                }
            }else{
                mCheckPointData.setUpdateAddress(false);
            }

            if(mNoAddressLocation){

                /*Here we update the account location*/
                mMap.addMarker(
                        new MarkerOptions()
                                .position(new LatLng(mCheckPointData.getLatitude(), mCheckPointData.getLongitude()))
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA)));

                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                        new LatLng(mCheckPointData.getLatitude(),
                                mCheckPointData.getLongitude()), 15));

            }else{

                /*Here we update the account location*/
                mMap.addMarker(
                        new MarkerOptions()
                                .position(new LatLng(mCheckPointData.getLatitude(), mCheckPointData.getLongitude()))
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA)));

                /*Here we decide the zoom level in the map*/
                if(isInRadius(userLatitude,userLongitude)){
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                            new LatLng(mCheckPointData.getLatitude(),
                                    mCheckPointData.getLongitude()), 15));
                }else{
                    LatLngBounds.Builder builder = new LatLngBounds.Builder();
                    builder.include(new LatLng(userLatitude,userLongitude));
                    builder.include(new LatLng(mCheckPointData.getLatitude(),mCheckPointData.getLongitude()));
                    LatLngBounds bounds = builder.build();
                    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 60);
                    mMap.animateCamera(cameraUpdate);
                }
            }

            /*Here we update the button */
            if(PreferenceManager.getInstance(this).isDoingCheckIn()){
                if(!mRestoredCheckInProgress){
                    mRestoredCheckInProgress = true;
                    restartCheckInProgressUi();
                }
            }else{
                if(!mBtnCheck.isShown()){
                    if(!mIsChecking){
                        mBtnCheck.setVisibility(View.VISIBLE);
                    }
                }

                if(mMapProgress.isShown()){
                    mMapProgress.setVisibility(View.GONE);
                }
            }
        }
    }

    /**
     * This method help us to set all the check in
     * progress ui
     */
    public void restartCheckInProgressUi(){
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            Date saveDate =    sdf.parse(mCheckPointLocation.getCheckInDate());
            Date currentDate = sdf.parse(Utility.getCurrentDate());

            /*long baseTime = SystemClock.elapsedRealtime()-(currentDate.getTime() - saveDate.getTime());
            mChronometer.setBase(baseTime);
            mChronometer.start();*/
            mTxvStartDate.setText(mCheckPointLocation.getCheckInDate());
            mMapProgress.setVisibility(View.GONE);
            mBtnCheck.setText(R.string.finalize);
            mBtnCheck.setVisibility(View.VISIBLE);
            mLnlCheckProgress.setVisibility(View.VISIBLE);
            mIsChecking = true;
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
    /**
     * This method help us to check if the user location
     * and account address location are in the correct
     * radius
     */
    public boolean isInRadius(double userLatitude, double userLongitude){
        boolean flag = false;
        /*Here we define the objects*/
        Location locationUser = new Location("");
        Location locationAccount = new Location("");

        /*Hre we set the location data*/
        locationUser.setLatitude(userLatitude);
        locationUser.setLongitude(userLongitude);
        locationAccount.setLatitude(mCheckPointData.getLatitude());
        locationAccount.setLongitude(mCheckPointData.getLongitude());

        /*Here we get the distance between locations*/
        float distance = locationUser.distanceTo(locationAccount);

        if(distance<= CHECK_DISTANCE){
            flag = true;
        }
        return flag;
    }

    /**
     * This method help us to get the contact list
     * from sales force
     */
    public void getContactList(){
        new NetworkUtilsTask(this, new OnNetworkListener() {
            @Override
            public void onNetwork(boolean success, String message) {
                if(success){
                    String osql="SELECT Id, Name, Phone, MobilePhone, Email, Department, AccountId, " +
                            "Tipo_de_contacto__c FROM Contact WHERE AccountId = '"+mCheckPointData.getId()+"' ORDER BY Id";

                    ApiManager.getInstance().getJSONObject(getCurrentContext(), osql, new ApiManager.OnObjectListener() {
                        @Override
                        public void onObject(boolean success, JSONObject jsonObject, String errorMessage) {
                            mMapProgress.setVisibility(View.GONE);
                            if(success){
                                try {
                                    Utility.logLargeString("Account id: "+mCheckPointData.getId());
                                    Utility.logLargeString("Contactos: "+jsonObject.toString());
                                    Type listType = new TypeToken<List<Contact>>() {}.getType();
                                    List<Contact> contactList = new Gson().fromJson(jsonObject.getJSONArray("records").toString(), listType);
                                    if(contactList.size()>0){
                                        showContactListMessage(contactList);
                                    }else{
                                        restartCheckInUi();
                                        showMessage(getString(R.string.no_contacts));
                                        DatabaseManager.getInstance().saveUserAction(getApplicationContext(),getString(R.string.no_contacts));
                                    }
                                } catch (JSONException e) {
                                    restartCheckInUi();
                                    showMessage(getString(R.string.contact_list_no_got));
                                }
                            }else{
                                restartCheckInUi();
                                showMessage(errorMessage);
                                DatabaseManager.getInstance().saveUserAction(getApplicationContext(),errorMessage);
                            }
                        }
                    });
                }else{
                    restartCheckInUi();
                    showMessage(message);
                }
            }
        }).execute();
    }

    public Context getCurrentContext(){
        return this;
    }

    /**
     * This method show a dialog with the
     * contact list for the check in
     */
    public void showContactListMessage(final List<Contact> contactList){
        if(contactList.size()>0){
            ArrayList<String> contacts = new ArrayList<>();

            for(Contact contact: contactList){
                contacts.add(contact.getName());
            }

            new MaterialDialog.Builder(this)
                    .title(R.string.select_contact)
                    .items(contacts)
                    .itemsCallbackSingleChoice(0, new MaterialDialog.ListCallbackSingleChoice() {
                        @Override
                        public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                           /*Here we save the visit type*/
                            mCheckPointLocation.setAccountContactId(contactList.get(which).getId());
                            mCheckPointLocation.setAccountContactName(text.toString());

                            /*Here we track the user action*/
                            DatabaseManager.getInstance().saveUserAction(getApplicationContext(),getString(R.string.contact_selected));

                            /*Here we start the check flow*/
                            //startCheck();
                            showOdometerDialog();
                            return true;
                        }
                    })
                    .widgetColorRes(R.color.colorPrimary)
                    .positiveText(R.string.accept)
                    .positiveColorRes(R.color.colorPrimary)
                    .negativeText(R.string.cancel)
                    .negativeColorRes(R.color.colorPrimary)
                    .onNegative(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            dialog.dismiss();
                            restartCheckInUi();
                        }
                    })
                    .cancelable(false)
                    .show();
        }else{
            showMessage(R.string.no_contacts_available);
            restartCheckInUi();
        }
    }

    /**
     * This method help us to start the check
     * flow
     */
    public void startCheck(){
        /*Here we start the check in*/
        //mChronometer.setBase(SystemClock.elapsedRealtime());

        /*Here we start the checking time*/
        //mChronometer.start();
        mTxvStartDate.setText(mCheckPointLocation.getCheckInDate());
        mLnlCheckProgress.setVisibility(View.VISIBLE);
        mBtnCheck.setText(R.string.finalize);
        mBtnCheck.setVisibility(View.VISIBLE);
        PreferenceManager.getInstance(this).setIsDoingCheckIn(true);
        PreferenceManager.getInstance(this).setCheckPointData(new Gson().toJson(mCheckPointData));
        PreferenceManager.getInstance(this).setCheckPointLocation(new Gson().toJson(mCheckPointLocation));
        /*Here we track the user action*/
        DatabaseManager.getInstance().saveUserAction(getApplicationContext(),getString(R.string.check_in_start));
    }


    /**
     * This dialog help su to get the comment or
     * description about the visit before save
     * the check point data
     */
    public void showCommentDialog(){
        new MaterialDialog.Builder(this)
                .title(R.string.visit_description)
                .contentColorRes(R.color.colorPrimaryDark)
                .content(R.string.visit_description_message)
                .positiveColorRes(R.color.colorPrimary)
                .positiveText(R.string.accept)
                .input(R.string.text_description, 0, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(MaterialDialog dialog, CharSequence input) {

                       if(input.toString().compareTo("")!=0){
                           /*Here we set the description*/
                           mCheckPointLocation.setDescription(input.toString());
                           /*Here we track the user action*/
                           DatabaseManager.getInstance().saveUserAction(getApplicationContext(),getString(R.string.visit_description_action));
                           getUserLocationToFinalize();
                       }else{
                         showMessage(R.string.no_description_typed);
                       }
                    }
                })
                .widgetColorRes(R.color.colorPrimary)
                .show();
    }

    /**
     * This method help us to check if we need
     * show the sign activity
     */
    public void checkSingActivity(){
         /*Here we check if we have to get the signature*/
        if(mCheckPointData.isIsMainTechnical()){
            showMessageDigitalSignature();
        }else{
          getUserLocationToFinalize();
        }
    }


    /**
     * This method help us to get the user
     * location to finalize the check out
     */
    public void getUserLocationToFinalize(){

        mBtnCheck.setVisibility(View.GONE);
        mMapProgressMessage.setText(R.string.getting_your_location);
        mMapProgress.setVisibility(View.VISIBLE);

        /*Here we request the user location to finalize the check*/
        mIsChecking=false;
        // Start location service after Check Out
        startLocationService();
        getUserLocation();
    }

    @Override
    public void onBackPressed() {
        if(PreferenceManager.getInstance(this).isDoingCheckIn()){
            checkUserLocation();
        }else{
            super.onBackPressed();
        }
    }


    /**
     * This dialog help us to set the current
     * odometer data for the current visit
     */
    public void showOdometerDialog(){
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
                        int odometer = Integer.parseInt(input.toString());
                        if(odometer >=PreferenceManager.getInstance(getApplicationContext()).getStartOdometer()){
                            //Here we save the odometer value
                            mCheckPointLocation.setOdometer(odometer);
                            //here we start the check
                            startCheck();
                        }else{
                            Toast.makeText(getApplicationContext(),R.string.odometer_grater_than_start_odometer,Toast.LENGTH_LONG).show();
                            showOdometerDialog();
                        }
                    }
                })
                .widgetColorRes(R.color.colorPrimary)
                .cancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        restartCheckInUi();
                    }
                })
                .show();
    }

}
