package com.app.checkinmap.ui.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.app.checkinmap.R;
import com.app.checkinmap.db.DatabaseManager;
import com.app.checkinmap.model.CheckPointData;
import com.app.checkinmap.model.WorkOrder;
import com.app.checkinmap.ui.adapter.WorkOrderAdapterList;
import com.app.checkinmap.util.ApiManager;
import com.app.checkinmap.util.PreferenceManager;
import com.app.checkinmap.util.Utility;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.app.checkinmap.ui.activity.CheckPointMapActivity.REQUEST_CHECK_IN;
import static com.app.checkinmap.ui.activity.SearchableWorkOrderActivity.ACTION_SEARCH_RESULT_WORK_ORDER;

public class MyWorkOrdersActivity extends AppCompatActivity implements WorkOrderAdapterList.OnItemClickListener{
    public static final int    REQUEST_WORK_ORDER_SELECTION = 17;
    public static final String ARG_WORK_ORDER_DATA="work_order_selected";

    @BindView(R.id.rcv_work_orders)
    RecyclerView mRv;

    @BindView(R.id.progress_bar)
    ProgressBar mPgBar;

    @BindView(R.id.text_view_message)
    TextView mTxvMessage;

    private WorkOrderAdapterList mAdapter;

    BroadcastReceiver mSearchResultReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ACTION_SEARCH_RESULT_WORK_ORDER)) {
                WorkOrder workOrderData = intent.getExtras().getParcelable(ARG_WORK_ORDER_DATA);
                if(workOrderData!=null){
                    onItemClick(workOrderData);
                }
            }

        }
    };


    /**
     * This method help us to get a single
     * intent in order to get a my order work
     * instance
     */
    public static Intent getIntent(Context context){
        Intent intent = new Intent(context,MyWorkOrdersActivity.class);
        return intent;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_order_works);

        ButterKnife.bind(this);

        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.my_work_orders);
        }

        mRv.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mRv.setLayoutManager(layoutManager);

        //Here we get the opens work orders
        /*Here we track the user action*/
        DatabaseManager.getInstance().saveUserAction(this,getString(R.string.work_order_query));
        getWorkOrdersFromSalesForce();

        registerReceiver(mSearchResultReceiver, new IntentFilter(ACTION_SEARCH_RESULT_WORK_ORDER));
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case REQUEST_CHECK_IN:
                if(resultCode == RESULT_OK){
                    setResult(RESULT_OK,data);
                    finish();
                }
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.action_search:
                onSearchRequested();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mSearchResultReceiver);
    }

    /**
     * This method help us to get all the accounts from
     * sales force
     */
    public void getWorkOrdersFromSalesForce(){

        if (Utility.getRestClient() != null) {

            String osql = "SELECT Id, Tecnico__c, Principal__c, Work_Order__c, work_order__r.WorkOrderNumber, work_order__r.Direccion_Visita__r.id, work_order__r.Cuenta_del__c, work_order__r.Contacto__c,  toLabel(work_order__r.status), work_order__r.direccion_visita__r.Direccion__c,work_order__r.direccion_visita__r.Ciudad__c, work_order__r.direccion_visita__r.estado_o_provincia__c, work_order__r.direccion_visita__r.pais__c," +
                " work_order__r.direccion_visita__r.coordenadas__c, work_order__r.AccountId, work_order__r.ContactID" +
                " FROM Tecnicos_por_Orden_de_Trabajo__c" +
                " WHERE  Tecnico__c = '" + Utility.getRestClient().getClientInfo().userId + "'" +
                " AND (work_order__r.status = 'Open' OR work_order__r.status = 'In Process') ORDER BY CreatedDate ASC";

            ApiManager.getInstance().getJSONObject(this, osql, new ApiManager.OnObjectListener() {
                @Override
                public void onObject(boolean success, JSONObject jsonObject, String errorMessage) {
                /*Here we hide the progress bar*/
                    mPgBar.setVisibility(View.GONE);
                    if (success) {

                        Utility.logLargeString("ordenes de trabajo: " + jsonObject.toString());

                        try {
                            Type listType = new TypeToken<List<WorkOrder>>() {
                            }.getType();
                            List<WorkOrder> workOrderList = new Gson().fromJson(jsonObject.getJSONArray("records").toString(), listType);
                            loadListData(workOrderList);

                        } catch (JSONException e) {
                            e.printStackTrace();
                            mTxvMessage.setText(e.getMessage());
                            mTxvMessage.setVisibility(View.VISIBLE);
                            DatabaseManager.getInstance().saveUserAction(getApplicationContext(),e.getMessage().replace(",",""));
                        }
                    } else {
                        String mMessage = getString(R.string.no_connection);
                        mTxvMessage.setText(mMessage); // errorMessage
                        mTxvMessage.setVisibility(View.VISIBLE);
                        DatabaseManager.getInstance().saveUserAction(getApplicationContext(),mMessage.replace(",",""));
                    }
                }
            });

        } else {
            mTxvMessage.setText(R.string.salesforce_error);
            mTxvMessage.setVisibility(View.VISIBLE);
        }
    }

    /**
     * This method help us to load the data in the
     * recycler view
     */
    public void loadListData(List<WorkOrder> workOrderList){


        if(workOrderList.size()>0){
            mAdapter = new WorkOrderAdapterList(workOrderList);
            mAdapter.setOnItemClickListener(this);
            mRv.setAdapter(mAdapter);
            mRv.setVisibility(View.VISIBLE);
            DatabaseManager.getInstance().saveUserAction(this,getString(R.string.open_work_orders)+" "+workOrderList.size());
        }else{
            mTxvMessage.setText(R.string.no_work_orders_to_show);
            mTxvMessage.setVisibility(View.VISIBLE);
            DatabaseManager.getInstance().saveUserAction(this,getString(R.string.no_work_orders_to_show));
        }
    }

    @Override
    public void onItemClick(WorkOrder workOrder) {
        if(PreferenceManager.getInstance(this).isInRoute()){
            CheckPointData checkPointData = new CheckPointData();
            checkPointData.setWorkOderXTechnicalId(workOrder.getId());
            checkPointData.setId(workOrder.getWorkOrderId());
            checkPointData.setContactId(workOrder.getWorkOrderDetail().getContactId());
            checkPointData.setContactName(workOrder.getWorkOrderDetail().getContactName());
            checkPointData.setIsMainTechnical(workOrder.isIsPrincipal());
            checkPointData.setMainTechnicalId(workOrder.getTechnicalId());
            checkPointData.setWorkOrderNumber(workOrder.getWorkOrderDetail().getWorkOrderNumber());
            checkPointData.setCheckPointType(3);
            checkPointData.setmWorkOrderStatus(workOrder.getWorkOrderDetail().getStatus());
            if(workOrder.getWorkOrderDetail().getWorkOrderAddress()!=null){
                checkPointData.setAddress(workOrder.getWorkOrderDetail().getWorkOrderAddress().getAddress());
                checkPointData.setAddressId(workOrder.getWorkOrderDetail().getWorkOrderAddress().getId());
                checkPointData.setName(workOrder.getWorkOrderDetail().getWorkOrderNumber()+"-"+workOrder.getWorkOrderDetail().getWorkOrderAddress().getCountry());

                if(workOrder.getWorkOrderDetail().getWorkOrderAddress().getCoordinates()!=null){
                    checkPointData.setLatitude(workOrder.getWorkOrderDetail().getWorkOrderAddress().getCoordinates().getLatitude());
                    checkPointData.setLongitude(workOrder.getWorkOrderDetail().getWorkOrderAddress().getCoordinates().getLongitude());
                }else{
                    checkPointData.setLatitude(0);
                    checkPointData.setLongitude(0);
                }
            }else{
                checkPointData.setAddress("");
                checkPointData.setName(workOrder.getWorkOrderDetail().getWorkOrderNumber());
                checkPointData.setLatitude(0);
                checkPointData.setLongitude(0);
            }


            //Here we start the check flow
            startCheckPointFlow(checkPointData);
        }else{
            showMessage(R.string.you_should_start_the_route);
            DatabaseManager.getInstance().saveUserAction(this,getString(R.string.no_rout_started_in_work_oder_selection));
        }
    }


    /**
     * This method show a single message
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
     * This method show a single message
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
     * Here we start the check point flow
     */
    public void startCheckPointFlow(CheckPointData checkPointData){

        startActivityForResult(CheckPointMapActivity.getIntent(getApplicationContext(),checkPointData),
                REQUEST_CHECK_IN);
    }
}
