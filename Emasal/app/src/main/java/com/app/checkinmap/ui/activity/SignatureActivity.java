package com.app.checkinmap.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.app.checkinmap.R;
import com.app.checkinmap.db.DatabaseManager;
import com.app.checkinmap.util.ImageHelper;
import com.simplify.ink.InkView;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SignatureActivity extends AppCompatActivity {

    public static final int    RESULT_NO_SIGNATURE_FILE=44;
    public static final String ARG_SING_FILE_PATH ="file_path";
    public static final String WHO_SIGNS = "";

    public static String   ARG_NAME="name";
    public static String   ARG_WORK_ORDER_ID="work_order_id";

    @BindView(R.id.button_check)
    AppCompatButton mBtnCheckOut;

    @BindView(R.id.etWhoSigns)
    EditText etWhoSigns;

    @BindView(R.id.ink)
    InkView mInkView;

    private boolean mSignatureIsEmpty=true;

    /**
     * This method help us to get a single signature activity
     * intent
     */
    public static Intent getIntent(Context context,String name,String workOrderId){
        Intent intent = new Intent(context,SignatureActivity.class);
        intent.putExtra(ARG_NAME,name);
        intent.putExtra(ARG_WORK_ORDER_ID,workOrderId);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signature);

        ButterKnife.bind(this);

        if(getSupportActionBar()!=null){
            getSupportActionBar().setHomeButtonEnabled(false);
            getSupportActionBar().setTitle(getIntent().getExtras().getString(ARG_NAME));
        }

        mInkView.setColor(getResources().getColor(android.R.color.black));
        mInkView.setMinStrokeWidth(1.5f);
        mInkView.setMaxStrokeWidth(6f);
        mInkView.addInkListener(new InkView.InkListener() {


            @Override
            public void onInkClear() {
                mSignatureIsEmpty=true;
            }

            @Override
            public void onInkDraw() {
                mSignatureIsEmpty=false;
            }
        });

        /*Here we track the user action*/
        DatabaseManager.getInstance().saveUserAction(this,getString(R.string.signature_request)+" "+getIntent().getExtras().getString(ARG_WORK_ORDER_ID));
    }

    @OnClick(R.id.button_check)
    public void checkOut(){
        String whoSigns = etWhoSigns.getText().toString();

        if (!whoSigns.isEmpty()) {

            /*Here we save the current sing*/
            Bitmap drawing = mInkView.getBitmap(getResources().getColor(R.color.colorWhite));

            if(!mSignatureIsEmpty){

                File file = ImageHelper.saveBitMap(this,drawing, getIntent().getExtras().getString(ARG_WORK_ORDER_ID));

                if (file != null) {
                    Intent intent = new Intent();
                    intent.putExtra(WHO_SIGNS, whoSigns);
                    intent.putExtra(ARG_SING_FILE_PATH, file.getPath());
                    setResult(RESULT_OK, intent);

                 /*Here we track the user action*/
                    DatabaseManager.getInstance().saveUserAction(this,getString(R.string.signature_saved)+" "+getIntent().getExtras().getString(ARG_WORK_ORDER_ID));

                } else {
                    setResult(RESULT_NO_SIGNATURE_FILE);
                   /*Here we track the user action*/
                    DatabaseManager.getInstance().saveUserAction(this,getString(R.string.signature_saved_error)+" "+getIntent().getExtras().getString(ARG_WORK_ORDER_ID));
                }

                //Here we finish the activity
                finish();
            }else{
                Toast.makeText(this, R.string.signature_empty, Toast.LENGTH_LONG).show();
            }
        }else{
            Toast.makeText(this, R.string.who_sign_signature_empty, Toast.LENGTH_LONG).show();
        }
    }

    @OnClick(R.id.text_view_clear)
    public void clearInkView(){
        mInkView.clear();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if ( v instanceof EditText) {
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int)ev.getRawX(), (int)ev.getRawY())) {
                    v.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        }
        return super.dispatchTouchEvent(ev);
    }
}
