package com.app.checkinmap.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.app.checkinmap.R;
import com.app.checkinmap.util.AppStatus;

public class NoInternetActivity extends AppCompatActivity {

  /**
   * This method help us to get a single intent
   * in order to show a internet message connection.
   * @param context application context
   */
  public static Intent getIntent(Context context){
    Intent intent = new Intent(context, NoInternetActivity.class);
    return intent;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_no_internet);
  }

  @Override
  protected void onResume() {
    super.onResume();
    //isOnline();
  }

  public void isOnline() {
    if (AppStatus.getInstance(getApplicationContext()).isOnline()) {
      // Internet
      Intent intent = new Intent(this, SplashScreenActivity.class);
      startActivity(intent);
      finish();
    }
  }

}
