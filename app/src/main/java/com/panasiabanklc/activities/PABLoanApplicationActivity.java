package com.panasiabanklc.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.panasiabanklc.PABUserLoginActivity;
import com.panasiabanklc.R;

/**
 * Created by user1 on 8/6/2017.
 */

public class PABLoanApplicationActivity extends AppCompatActivity implements View.OnClickListener{
    private Button btn_loan_existing_cust;
    private Button btn_loan_new_cust;
    private TextView tv_loan_calculator;
    private ImageView btn_header_left,btnmenu;
    static PABLoanApplicationActivity activity = null;

    protected void onCreate(Bundle savedInstantState) {
        super.onCreate(savedInstantState);
        setContentView(R.layout.pab_loan_application);
        activity=this;
        initViews();

    }
    public void initViews(){
        btn_loan_existing_cust = (Button)findViewById(R.id.btn_loan_existing_cust);
        btn_loan_existing_cust.setOnClickListener(this);
        btn_loan_new_cust = (Button)findViewById(R.id.btn_loan_new_cust);
        btn_loan_new_cust.setOnClickListener(this);
        tv_loan_calculator=(TextView)findViewById(R.id.tv_loan_calculator);
        tv_loan_calculator.setText("LOAN APPLICATION");
        btn_header_left=(ImageView)findViewById(R.id.btn_header_left);
        btn_header_left.setOnClickListener(this);
        btnmenu=(ImageView)findViewById(R.id.btnmenu);
        btnmenu.setOnClickListener(this);
    }
    public void onClick(View v){
        switch (v.getId()){
            case R.id.btn_loan_existing_cust:
                startActivity(new Intent(PABLoanApplicationActivity.this, PABExistingCustLoanActivity.class));
                overridePendingTransition(R.anim.fade_in,
                        R.anim.fade_out);
                break;
            case R.id.btn_loan_new_cust:
                startActivity(new Intent(PABLoanApplicationActivity.this, PABNewCustLoanActivity.class));
                overridePendingTransition(R.anim.fade_in,
                        R.anim.fade_out);
                break;
            case R.id.btn_header_left:
                onBackPressed();
                break;
            case R.id.btnmenu:
                PopupMenu popup = new PopupMenu(activity, btnmenu);
                //Inflating the Popup using xml file
                popup.getMenuInflater().inflate(R.menu.pab_popup_menu, popup.getMenu());

                //registering popup with OnMenuItemClickListener
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        SharedPreferences preferences = getApplicationContext().getSharedPreferences("pabloancalculatordefaults", PABLoanApplicationActivity.this.MODE_PRIVATE);
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.clear();
                        editor.commit();
                        Intent intent=new Intent(activity, PABUserLoginActivity.class);
                        startActivity(intent);
                        finish();
                        Log.i("username",":");
                        return true;
                    }
                });

                popup.show();//showing popup menu
                break;
            default:
                break;
        }
    }
    public void onBackPressed() {
        Intent intent = new Intent(activity, PABMainMenuActivity.class);
        activity.finish();
        startActivity(intent);
        overridePendingTransition(R.anim.abc_fade_in, R.anim.abc_fade_out);
    }
}
