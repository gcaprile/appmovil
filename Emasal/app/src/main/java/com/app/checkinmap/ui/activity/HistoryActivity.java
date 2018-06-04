package com.app.checkinmap.ui.activity;


import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.widget.TextView;


import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.app.checkinmap.R;
import com.app.checkinmap.db.DatabaseManager;
import com.app.checkinmap.model.CheckPointLocation;
import com.app.checkinmap.model.Route;
import com.app.checkinmap.model.UserLocation;
import com.app.checkinmap.ui.adapter.HistoryAdapterList;
import com.app.checkinmap.util.PreferenceManager;
import com.app.checkinmap.util.Utility;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class HistoryActivity extends AppCompatActivity {
    public static final String ARG_ROUTE_ID = "route_id";

    @BindView(R.id.rvHistory)
    RecyclerView mRv;

    @BindView(R.id.text_view_title_screen)
    TextView mTxvTitle;

    @BindView(R.id.text_route_name)
    TextView mTvRouteName;

    @BindView(R.id.text_view_distance)
    TextView mTvDistance;

    @BindView(R.id.text_view_time)
    TextView mTvUsedTime;

    @BindView(R.id.text_view_visit_number)
    TextView mTvVisitNumber;

    /*The current route id*/
    private long mRouteId;


    /**
     * This method help us to get a single
     * intent in order to get a history activity
     * instance
     */
    public static Intent getIntent(Context context, long routeId){
        Intent intent = new Intent(context,HistoryActivity.class);
        intent.putExtra(ARG_ROUTE_ID,routeId);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        ButterKnife.bind(this);

        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.completed_route);
            mTxvTitle.setText(R.string.route_complete_information);
        }

        //Here we get the current rote id
        mRouteId= getIntent().getExtras().getLong(ARG_ROUTE_ID);


        /*Here we set the route data*/
        mTvRouteName.setText(DatabaseManager.getInstance().getRouteName(mRouteId));
        mTvDistance.setText(getRoutDistance());
        mTvUsedTime.setText(getRouteTime());
        mTvVisitNumber.setText(getTotalVisits());

        /*Here we create the list with all the visit*/
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mRv.setLayoutManager(layoutManager);
        HistoryAdapterList adapter = new HistoryAdapterList(mRouteId,getCheckPointLocations());
        mRv.setAdapter(adapter);

        /*Here we show a success message*/
        showMessage(R.string.text_route_data_sent);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * This method help su to calculate the route distance
     * using all the user locations in the route
     */
    public String getRoutDistance(){
        //Here we define the string to return
        String routeDistance="";

        //Here we initialize the distance
        double distance=0.00;

        //Here we get all the user locations in the route
        List<UserLocation> userLocations = DatabaseManager.getInstance().getUserLocationList(mRouteId);


        //Here we check if the route has locations
        if(userLocations.size()>0){

            //Here we sum all the distance between each point
            for(int i=0;i<userLocations.size();i++){
                if((i+1)<userLocations.size()){
                    //Here get two points from the list
                    UserLocation pointA = userLocations.get(i);
                    UserLocation pointB = userLocations.get(i+1);

                    //Here we create the location objects to make the sum
                    Location locationA = new Location("Point A");
                    Location locationB = new Location("Point B");

                    //Here we seth the data in each location object
                    locationA.setLongitude(pointA.getLongitude());
                    locationA.setLatitude(pointA.getLatitude());

                    locationB.setLongitude(pointB.getLongitude());
                    locationB.setLatitude(pointB.getLatitude());

                    //Here we accumulate the distance in meters
                    distance = distance + locationA.distanceTo(locationB);
                }
            }
        }

        //Here we transform the distance to kilometers
        routeDistance = String.format("%.2f", (distance/1000.00))+" Km";
        return routeDistance;
    }


    /**
     * This method help us to get the total time
     * used in the route
     */
    public String getRouteTime(){
        String time ="0 horas";
        String dateStart = DatabaseManager.getInstance().getRouteStartDate(mRouteId);
        String dateEnd = DatabaseManager.getInstance().getRouteEndDate(mRouteId);

        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

        Date d1;
        Date d2;

        try {
            d1 = format.parse(dateStart);
            d2 = format.parse(dateEnd);

            //diff check ins
            long totalDiff = d2.getTime() - d1.getTime();

            long diffSeconds = totalDiff / 1000 % 60;
            long diffMinutes = totalDiff / (60 * 1000) % 60;
            long diffHours = totalDiff / (60 * 60 * 1000) % 24;
            long diffDays = totalDiff / (24 * 60 * 60 * 1000);

            time = diffHours+" horas "+ diffMinutes+" minutos ";

        } catch (Exception e) {
            e.printStackTrace();
        }
        return time;
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

    /**
     * This method help us to get the visit
     * total number from data base
     */
    public String getTotalVisits(){
        return  String.valueOf(DatabaseManager.getInstance().getCheckPointLocationList(
                mRouteId
        ).size());
    }

    /**
     * This method help us to get all the visits
     * in the route
     */
    private List<CheckPointLocation> getCheckPointLocations(){
        return DatabaseManager.getInstance().getCheckPointLocationList(mRouteId);
    }
}
