package com.app.checkinmap.ui.activity;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.app.checkinmap.R;
import com.app.checkinmap.db.DatabaseManager;
import com.app.checkinmap.model.Lead;
import com.app.checkinmap.ui.adapter.LeadAdapterList;
import com.app.checkinmap.util.ApiManager;
import com.app.checkinmap.util.NetworkUtilsTask;
import com.app.checkinmap.util.OnNetworkListener;
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

public class SearchableLeadsActivity extends AppCompatActivity implements LeadAdapterList.OnItemClickListener{

    public static final String ACTION_SEARCH_RESULT_LEAD= "com.app.checkinmap.SEARCH_RESULT_LEAD";

    @BindView(R.id.rcv_leads)
    RecyclerView mRv;

    @BindView(R.id.progress_bar)
    ProgressBar mPgBar;

    @BindView(R.id.my_leads_subtitle)
    TextView mTxvSubtitle;

    @BindView(R.id.text_view_message)
    TextView mTxvMessage;

    private LeadAdapterList mAdapter;
    private String mQuery;

    /**
     * This method help us to get a single
     * intent in order to get a my lead activity
     * instance
     */
    public static Intent getIntent(Context context){
        Intent intent = new Intent(context,SearchableLeadsActivity.class);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_leads);

        ButterKnife.bind(this);

        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.text_result);
        }


        // Get the intent, verify the action and get the query
        Intent intent = getIntent();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {

            // Search string
            mQuery = intent.getStringExtra(SearchManager.QUERY).toLowerCase();

            mRv.setHasFixedSize(true);
            LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
            mRv.setLayoutManager(layoutManager);

            /*Here we track the user action*/
            DatabaseManager.getInstance().saveUserAction(this,getString(R.string.lead_search)+" "+mQuery);

            /*Here we get the leads from the sales force*/
            getLeadFromSalesForce(mQuery);

        }else{
            mPgBar.setVisibility(View.GONE);
            mTxvMessage.setText(R.string.text_no_result);
            mTxvMessage.setVisibility(View.VISIBLE);
            DatabaseManager.getInstance().saveUserAction(this,getString(R.string.text_no_result));
        }

    }


    /**
     * This method help us to get all the accounts from
     * sales force
     */
    public void getLeadFromSalesForce(final String search){

        new NetworkUtilsTask(this, new OnNetworkListener() {
            @Override
            public void onNetwork(boolean success, String message) {
                if(success){

                    String osql = "SELECT Id, Name, Company, Street, City, State, PostalCode, Country, Pais__c, Coordenadas__Latitude__s, Coordenadas__Longitude__s" +
                            " FROM Lead WHERE Pais__c = '"+Utility.getUserCountry()+"' AND Name LIKE '%" + search + "%' AND Status != 'Convertido' ORDER BY LastName ASC";

                    ApiManager.getInstance().getJSONObject(getCurrentContext(), osql, new ApiManager.OnObjectListener() {
                        @Override
                        public void onObject(boolean success, JSONObject jsonObject, String errorMessage) {
                            /*Here we hide the progress bar*/
                            mPgBar.setVisibility(View.GONE);
                            mTxvSubtitle.setVisibility(View.VISIBLE);
                            if(success){
                                Utility.logLargeString(jsonObject.toString());
                                try {
                                    DatabaseManager.getInstance().saveUserAction(getApplicationContext(),getString(R.string.search_success));
                                    Type listType = new TypeToken<List<Lead>>() {}.getType();
                                    List<Lead> leadList = new Gson().fromJson(jsonObject.getJSONArray("records").toString(), listType);
                                    loadListData(leadList);
                                    showInfo(leadList.size());
                                } catch (JSONException e) {
                                    DatabaseManager.getInstance().saveUserAction(getApplicationContext(),e.getMessage().replace(",",""));
                                    mTxvMessage.setText(e.getMessage());
                                    mTxvMessage.setVisibility(View.VISIBLE);
                                }
                            }else{
                                String mMessage = getString(R.string.no_connection);
                                DatabaseManager.getInstance().saveUserAction(getApplicationContext(),mMessage.replace(",",""));
                                mTxvMessage.setText(mMessage); // errorMessage
                                mTxvMessage.setVisibility(View.VISIBLE);
                            }
                        }
                    });
                }else{
                    String mMessage = getString(R.string.no_connection);
                    DatabaseManager.getInstance().saveUserAction(getApplicationContext(),mMessage.replace(",",""));
                    mTxvMessage.setText(mMessage); // errorMessage
                    mTxvMessage.setVisibility(View.VISIBLE);
                }
            }
        }).execute();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    public void showInfo(int total) {
        switch (total) {
            case 0:
                mTxvSubtitle.setText(getString(R.string.no_search_results, mQuery.toUpperCase()));
                DatabaseManager.getInstance().saveUserAction(this,getString(R.string.no_search_results, mQuery.toUpperCase()).replace(",",""));
                break;
            case 1:
                mTxvSubtitle.setText(getString(R.string.search_result, total, mQuery.toUpperCase()));
                DatabaseManager.getInstance().saveUserAction(this,getString(R.string.search_result, total, mQuery.toUpperCase()).replace(",",""));
                break;
            default:
                mTxvSubtitle.setText(getString(R.string.search_results, total, mQuery.toUpperCase()));
                DatabaseManager.getInstance().saveUserAction(this,getString(R.string.search_results, total, mQuery.toUpperCase()).replace(",",""));
                break;
        }
    }

    /**
     * This method help us to load the data in the
     * recycler view
     */
    public void loadListData(List<Lead> leadList){

        if(leadList.size()>0){
            mAdapter = new LeadAdapterList(leadList);
            mAdapter.setOnItemClickListener(this);
            mRv.setAdapter(mAdapter);
            mRv.setVisibility(View.VISIBLE);
            DatabaseManager.getInstance().saveUserAction(this,getString(R.string.leads_found).replace(",","")
                    +" "+leadList.size());
        }else{
            mTxvMessage.setText(R.string.no_leads_to_show);
            mTxvMessage.setVisibility(View.VISIBLE);
            DatabaseManager.getInstance().saveUserAction(this,getString(R.string.no_leads_to_show).replace(",",""));
        }
    }

    @Override
    public void onItemClick(Lead lead) {
         /*Here we notify the result*/
        Intent intent= new Intent();
        intent.setAction(ACTION_SEARCH_RESULT_LEAD);
        intent.putExtra(MyLeadsActivity.ARG_LEAD_DATA,lead);
        sendBroadcast(intent);
        finish();
    }

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

    public Context getCurrentContext(){
        return this;
    }
}
