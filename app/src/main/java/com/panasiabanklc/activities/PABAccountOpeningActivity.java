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

public class PABAccountOpeningActivity extends AppCompatActivity implements View.OnClickListener {
    private Button btn_new_cust;
    private Button btn_existing_cust;
    private ImageView btn_header_left,btnmenu;
    private TextView tv_loan_calculator;


    protected void onCreate(Bundle savedInstantState) {
        super.onCreate(savedInstantState);
        setContentView(R.layout.pab_account_opening);
        initViews();
    }
    public void initViews(){
        btn_new_cust = (Button)findViewById(R.id.btn_app_new_cust);
        btn_new_cust.setOnClickListener(this);
        btn_existing_cust=(Button)findViewById(R.id.btn_app_existing_cust);
        btn_existing_cust.setOnClickListener(this);
        btn_header_left=(ImageView)findViewById(R.id.btn_header_left);
        btn_header_left.setOnClickListener(this);
        tv_loan_calculator=(TextView)findViewById(R.id.tv_loan_calculator);
        tv_loan_calculator.setText("ACCOUNT OPENING");
        btnmenu=(ImageView)findViewById(R.id.btnmenu);
        btnmenu.setOnClickListener(this);

    }
    public void onClick(View v){
        switch (v.getId()){
            case R.id.btn_app_new_cust:
                startActivity(new Intent(PABAccountOpeningActivity.this, PABNewCustAccountOpeningActivity.class));
                overridePendingTransition(R.anim.slide_in_bottom,
                        R.anim.slide_in_up);
                break;
            case R.id.btn_app_existing_cust:
                startActivity(new Intent(PABAccountOpeningActivity.this, PABExistingCustAccountActivity.class));
                overridePendingTransition(R.anim.slide_in_bottom,
                        R.anim.slide_in_up);
                break;
            case R.id.btn_header_left:
                onBackPressed();
                break;
            case R.id.btnmenu:
                PopupMenu popup = new PopupMenu(PABAccountOpeningActivity.this, btnmenu);
                //Inflating the Popup using xml file
                popup.getMenuInflater().inflate(R.menu.pab_popup_menu, popup.getMenu());

                //registering popup with OnMenuItemClickListener
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        SharedPreferences preferences = getApplicationContext().getSharedPreferences("pabloancalculatordefaults", PABAccountOpeningActivity.this.MODE_PRIVATE);
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.clear();
                        editor.commit();
                        Intent intent=new Intent(PABAccountOpeningActivity.this, PABUserLoginActivity.class);
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
    public void onBackPressed(){
        Intent intent = new Intent(PABAccountOpeningActivity.this, PABMainMenuActivity.class);
        PABAccountOpeningActivity.this.finish();
        startActivity(intent);
        overridePendingTransition(R.anim.abc_fade_in, R.anim.abc_fade_out);
    }
}
