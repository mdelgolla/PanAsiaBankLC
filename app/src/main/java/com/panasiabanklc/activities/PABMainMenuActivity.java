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
import android.widget.Toast;

import com.panasiabanklc.PABUserLoginActivity;
import com.panasiabanklc.R;

import org.w3c.dom.Text;

/**
 * Created by user1 on 8/6/2017.
 */

public class PABMainMenuActivity extends AppCompatActivity implements View.OnClickListener {
    private Button btn_cust_onboard;
    private Button btn_account_opening;
    private Button btn_loan_application;
    private ImageView header_left, btnmenu;
    private TextView title_bar;

    protected void onCreate (Bundle savedInstantState){
        super.onCreate(savedInstantState);
        setContentView(R.layout.pab_main_menu);

        initViews();

    }
    public void initViews(){

        btn_cust_onboard=(Button)findViewById(R.id.btn_cust_onboard);
        btn_cust_onboard.setOnClickListener(this);
        btn_account_opening=(Button)findViewById(R.id.btn_account_opening);
        btn_account_opening.setOnClickListener(this);
        btn_loan_application=(Button)findViewById(R.id.btn_loan_application);
        btn_loan_application.setOnClickListener(this);
        header_left=(ImageView)findViewById(R.id.btn_header_left);
        header_left.setOnClickListener(this);
        title_bar=(TextView)findViewById(R.id.tv_loan_calculator);
        title_bar.setText("MAIN MENU");
        btnmenu=(ImageView)findViewById(R.id.btnmenu);
        btnmenu.setOnClickListener(this);

    }
    public void onClick(View v){
        switch (v.getId()){
            case R.id.btn_cust_onboard:
                startActivity(new Intent(PABMainMenuActivity.this, PABCustOnboardActivity.class));
                overridePendingTransition(R.anim.slide_in_bottom,
                        R.anim.slide_in_up);
                break;
            case R.id.btn_account_opening:
                startActivity(new Intent(PABMainMenuActivity.this, PABAccountOpeningActivity.class));
                overridePendingTransition(R.anim.slide_in_bottom,
                        R.anim.slide_in_up);
                break;
            case R.id.btn_loan_application:
                startActivity(new Intent(PABMainMenuActivity.this, PABLoanApplicationActivity.class));
                overridePendingTransition(R.anim.slide_in_bottom,
                        R.anim.slide_in_up);
                break;
            case R.id.btnmenu:
                PopupMenu popup = new PopupMenu(PABMainMenuActivity.this, btnmenu);
                //Inflating the Popup using xml file
                popup.getMenuInflater().inflate(R.menu.pab_popup_menu, popup.getMenu());

                //registering popup with OnMenuItemClickListener
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        SharedPreferences preferences = getApplicationContext().getSharedPreferences("pabloancalculatordefaults", PABMainMenuActivity.this.MODE_PRIVATE);
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.clear();
                        editor.commit();
                        Intent intent=new Intent(PABMainMenuActivity.this, PABUserLoginActivity.class);
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

}
