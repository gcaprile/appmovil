package com.app.checkinmap.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.widget.EditText;

import com.app.checkinmap.R;
import com.app.checkinmap.db.DatabaseManager;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SignatureJustificationActivity extends AppCompatActivity {

  public static final String JUSTIFICATION = "justification";

  @BindView(R.id.etJustification)
  EditText mEtJustification;

  @BindView(R.id.button_check)
  AppCompatButton mBtnCheckOut;

  /**
   * This method help us to get a single signature activity
   * intent
   */
  public static Intent getIntent(Context context, String name){
    Intent intent = new Intent(context,SignatureJustificationActivity.class);
    //intent.putExtra(ARG_NAME,name);
    //intent.putExtra(ARG_WORK_ORDER_ID,workOrderId);
    return intent;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_signature_justification);
    ButterKnife.bind(this);
  }

  @OnClick(R.id.button_check)
  public void checkOut(){
      /*Here we track the user action*/
      DatabaseManager.getInstance().saveUserAction(this,getString(R.string.signature_justification_message));

      Intent intent = new Intent();
      intent.putExtra(JUSTIFICATION, mEtJustification.getText().toString());
      setResult(RESULT_OK,intent);
      finish();
  }

}
