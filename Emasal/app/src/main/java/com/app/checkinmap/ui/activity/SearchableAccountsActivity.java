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
import com.app.checkinmap.model.Account;
import com.app.checkinmap.ui.adapter.AccountAdapterList;
import com.app.checkinmap.util.ApiManager;
import com.app.checkinmap.util.NetworkUtilsTask;
import com.app.checkinmap.util.OnNetworkListener;
import com.app.checkinmap.util.Utility;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SearchableAccountsActivity extends AppCompatActivity implements AccountAdapterList.OnItemClickListener {
    public static final String ACTION_SEARCH_RESULT_ACCOUNT= "com.app.checkinmap.SEARCH_RESULT_ACCOUNT";

    @BindView(R.id.rcv_accounts)
    RecyclerView mRv;

    @BindView(R.id.progress_bar)
    ProgressBar mPgBar;

    @BindView(R.id.my_accounts_subtitle)
    TextView mTxvSubtitle;

    @BindView(R.id.text_view_message)
    TextView mTxvMessage;

    private AccountAdapterList mAdapter;
    private String mQuery;


    /**
     * This method help us to get a single
     * intent in order to get a my account activity
     * instance
     */
    public static Intent getIntent(Context context){
        Intent intent = new Intent(context,SearchableAccountsActivity.class);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_accounts);

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

            System.out.println(mQuery);

            mRv.setHasFixedSize(true);
            LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
            mRv.setLayoutManager(layoutManager);

            /*Here we track the user action*/
            DatabaseManager.getInstance().saveUserAction(this,getString(R.string.account_search)+" "+mQuery);

            /*Here we get the accounts from the sales force*/
            getAccountFromSalesForce(mQuery);

        }else{
            mPgBar.setVisibility(View.GONE);
            mTxvMessage.setText(R.string.text_no_result);
            mTxvMessage.setVisibility(View.VISIBLE);

            /*Here we track the user action*/
            DatabaseManager.getInstance().saveUserAction(this,getString(R.string.text_no_result));
        }
    }


    /**
     * This method help us to get all the accounts from
     * sales force
     */
    public void getAccountFromSalesForce(final String search){
        new NetworkUtilsTask(this, new OnNetworkListener() {
            @Override
            public void onNetwork(boolean success, String message) {
                if(success){
                    String osql ="SELECT Id, Name, Phone, BillingStreet, BillingCity, BillingState, BillingPostalCode, BillingCountry, Description, Numero_Contactos__c " +
                            "FROM Account WHERE Name LIKE '%" + search + "%' ORDER BY Name ASC LIMIT 100";

                    ApiManager.getInstance().getJSONObject(getCurrentContext(), osql, new ApiManager.OnObjectListener() {
                        @Override
                        public void onObject(boolean success, JSONObject jsonObject, String errorMessage) {
                        /*Here we hide the progress bar*/
                            mPgBar.setVisibility(View.GONE);
                            mTxvSubtitle.setVisibility(View.VISIBLE);
                            if(success){
                                DatabaseManager.getInstance().saveUserAction(getApplicationContext(),getString(R.string.search_success));
                                Utility.logLargeString(jsonObject.toString());
                                try {
                                    Type listType = new TypeToken<List<Account>>() {}.getType();
                                    List<Account> accountList = new Gson().fromJson(jsonObject.getJSONArray("records").toString(), listType);
                                    loadListData(accountList);
                                    showInfo(accountList.size());
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
                }else {
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

        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                break;
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
    public void loadListData(List<Account> accountList){
        if(accountList.size()>0){
            mAdapter = new AccountAdapterList(getApplicationContext(),accountList);
            mAdapter.setOnItemClickListener(this);
            mRv.setAdapter(mAdapter);
            mRv.setVisibility(View.VISIBLE);
            DatabaseManager.getInstance().saveUserAction(this,getString(R.string.account_found)+" "+accountList.size());
        }else{
            mTxvMessage.setText(R.string.no_contacts_to_show);
            mTxvMessage.setVisibility(View.VISIBLE);
            DatabaseManager.getInstance().saveUserAction(this,getString(R.string.no_contacts_to_show));
        }
    }

    @Override
    public void onItemClick(Account account) {
        if(account.getNumberContacts()>0){
             /*Here we notify the result*/
            Intent intent= new Intent();
            intent.setAction(ACTION_SEARCH_RESULT_ACCOUNT);
            intent.putExtra(MyAccountsActivity.ARG_ACCOUNT_DATA,account);
            sendBroadcast(intent);
            finish();
        }else{
            showMessage(R.string.no_contacts);
            DatabaseManager.getInstance().saveUserAction(this,getString(R.string.no_contacts_in_account));
        }
    }

    /**
     * This method help us to show a single
     * message
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

    public Context getCurrentContext(){
        return this;
    }
}
