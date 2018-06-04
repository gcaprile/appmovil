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
import com.app.checkinmap.model.Account;
import com.app.checkinmap.ui.adapter.AccountAdapterList;
import com.app.checkinmap.util.ApiManager;
import com.app.checkinmap.util.Utility;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.app.checkinmap.ui.activity.AccountDetailActivity.REQUEST_ADDRESS_SELECTION;
import static com.app.checkinmap.ui.activity.SearchableAccountsActivity.ACTION_SEARCH_RESULT_ACCOUNT;

public class MyAccountsActivity extends AppCompatActivity implements AccountAdapterList.OnItemClickListener {
    public static final int REQUEST_ACCOUNT_SELECTION = 77;
    public static final String ARG_ACCOUNT_DATA="account_selected";

    @BindView(R.id.rcv_accounts)
    RecyclerView mRv;

    @BindView(R.id.progress_bar)
    ProgressBar mPgBar;

    @BindView(R.id.my_accounts_subtitle)
    TextView mTxvSubTitle;

    @BindView(R.id.text_view_message)
    TextView mTxvMessage;

    private AccountAdapterList mAdapter;

    BroadcastReceiver mSearchResultReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ACTION_SEARCH_RESULT_ACCOUNT)) {
                Account account = intent.getExtras().getParcelable(ARG_ACCOUNT_DATA);
                if(account!=null){
                    onItemClick(account);
                }
            }

        }
    };

    /**
     * This method help us to get a single
     * intent in order to get a my account activity
     * instance
     */
    public static Intent getIntent(Context context){
        Intent intent = new Intent(context,MyAccountsActivity.class);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_accounts);

        ButterKnife.bind(this);

        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.my_accounts);
        }

        mRv.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mRv.setLayoutManager(layoutManager);

        mPgBar.setVisibility(View.GONE);
        //mTxvSubTitle.setVisibility(View.GONE);
        mTxvMessage.setVisibility(View.VISIBLE);
        mTxvMessage.setText(getString(R.string.search_data, "de la cuenta"));

        /*Here we get the accounts from the sales force*/
        //getAccountFromSalesForce();

        /*Here we register the account selection broadcast*/
        registerReceiver(mSearchResultReceiver, new IntentFilter(SearchableAccountsActivity.ACTION_SEARCH_RESULT_ACCOUNT));
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case REQUEST_ADDRESS_SELECTION:
                if(resultCode == RESULT_OK){
                    setResult(RESULT_OK,data);
                    finish();
                }
                break;
        }
    }

    /**
     * This method help us to get all the accounts from
     * sales force
     */
    public void getAccountFromSalesForce(){

        String osql ="SELECT Id, Name, Phone, BillingStreet, BillingCity, BillingState, BillingPostalCode, BillingCountry, Description, Numero_Contactos__c FROM Account ORDER BY LastViewedDate DESC LIMIT 20";

        ApiManager.getInstance().getJSONObject(this, osql, new ApiManager.OnObjectListener() {
            @Override
            public void onObject(boolean success, JSONObject jsonObject, String errorMessage) {
                /*Here we hide the progress bar*/
                mPgBar.setVisibility(View.GONE);
                if(success){
                    Utility.logLargeString("Cuentas: "+jsonObject.toString());
                    try {
                        Type listType = new TypeToken<List<Account>>() {}.getType();
                        List<Account> accountList = new Gson().fromJson(jsonObject.getJSONArray("records").toString(), listType);
                        System.out.println("ACCOUNT COUNT: " + accountList.size());

                        loadListData(accountList);

                    } catch (JSONException e) {
                        e.printStackTrace();
                        mTxvMessage.setText(e.getMessage());
                        mTxvMessage.setVisibility(View.VISIBLE);
                    }

                }else{
                    String mMessage = getString(R.string.no_connection);
                    mTxvMessage.setText(mMessage); // errorMessage
                    mTxvMessage.setVisibility(View.VISIBLE);
                }
            }
        });
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
        }else{
            mTxvMessage.setText(R.string.no_contacts_to_show);
            mTxvMessage.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onItemClick(Account account) {
        if(account.getNumberContacts()>0){
            startActivityForResult(AccountDetailActivity.getIntent(getApplicationContext(),account),REQUEST_ADDRESS_SELECTION);
        }else{
            showMessage(R.string.no_contacts);
            DatabaseManager.getInstance().saveUserAction(this,getString(R.string.no_contacts));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mSearchResultReceiver);
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
}
