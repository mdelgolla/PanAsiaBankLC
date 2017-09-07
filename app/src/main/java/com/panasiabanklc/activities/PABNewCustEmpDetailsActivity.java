package com.panasiabanklc.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.basgeekball.awesomevalidation.AwesomeValidation;
import com.basgeekball.awesomevalidation.ValidationStyle;
import com.panasiabanklc.PABUserLoginActivity;
import com.panasiabanklc.R;

import org.json.JSONObject;

/**
 * Created by user1 on 8/6/2017.
 */

public class PABNewCustEmpDetailsActivity extends AppCompatActivity implements View.OnClickListener{
    private Button btn_next_sal;
    private String customerdetails;
    private ImageView btn_header_left,btnmenu;
    private EditText et_com_employed,et_designation,et_work_exp;
    private TextView tv_loan_calculator;
    private AwesomeValidation awesomeValidation;
    static PABNewCustEmpDetailsActivity activity =null;


    protected void onCreate(Bundle savedInstantState) {
        super.onCreate(savedInstantState);
        setContentView(R.layout.pab_new_cust_emp_details);
        awesomeValidation = new AwesomeValidation(ValidationStyle.BASIC);

        activity = this;

        initViews();

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            try {
                customerdetails = bundle.getString("customerdetails");
                Log.i("AssetListChk", " 200 :" + customerdetails);
            } catch (Exception e) {
                Log.i("AssetListChk", " 200 :" + e);
            }
        }
    }
    public void initViews(){
        btn_next_sal=(Button)findViewById(R.id.btn_next_sal);
        btn_next_sal.setOnClickListener(this);
        btn_header_left=(ImageView) findViewById(R.id.btn_header_left);
        btn_header_left.setOnClickListener(this);
        btnmenu=(ImageView)findViewById(R.id.btnmenu);
        btnmenu.setOnClickListener(this);
        et_com_employed=(EditText)findViewById(R.id.et_com_employed);
        et_designation=(EditText)findViewById(R.id.et_designation);
        et_work_exp=(EditText)findViewById(R.id.et_work_exp);
        tv_loan_calculator=(TextView)findViewById(R.id.tv_loan_calculator);
        tv_loan_calculator.setText("EMPLOYEMENT DETAILS");

        awesomeValidation.addValidation(this, R.id.et_com_employed, "^[A-Za-z\\s]{1,}[\\.]{0,1}[A-Za-z\\s]{0,}$", R.string.enter_compay);
        awesomeValidation.addValidation(this, R.id.et_designation, "^[A-Za-z\\s]{1,}[\\.]{0,1}[A-Za-z\\s]{0,}$", R.string.enter_designation);



    }
    public void onClick(View v){
        switch (v.getId()){
            case R.id.btn_next_sal:
                try {
                    int validateCode=validateInputData(202);
                    if (validateCode==202){
                        createJsonObjects();
                    }
                    else {
                        Log.i("EmpDetail",":"+validateCode);
                    }
                }catch (Exception e){
                    Log.i("EmpDetail",":"+e);
                }
                validateInputData(202);
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
                        SharedPreferences preferences = getApplicationContext().getSharedPreferences("pabloancalculatordefaults", PABNewCustEmpDetailsActivity.this.MODE_PRIVATE);
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
    public int validateInputData(int validationCode) {
        if (et_com_employed.getText().toString().trim().length() < 3) {
            validationCode = 404;
            viewErrorValidation(getString(R.string.register_fail), getString(R.string.enter_compay), 404);
        } else if (et_designation.getText().toString().trim().length() < 3) {
            validationCode = 404;
            viewErrorValidation(getString(R.string.register_fail), getString(R.string.enter_designation), 404);
        } else if (et_work_exp.getText().toString().trim().length() ==0) {
            validationCode = 404;
            viewErrorValidation(getString(R.string.register_fail), getString(R.string.enter_workexperience), 404);

        }return validationCode;
    }
    public void viewErrorValidation(String msgTitle, String msgBody,final int validationCode) {
        AlertDialog.Builder builder=new AlertDialog.Builder(PABNewCustEmpDetailsActivity.this);
        builder.setTitle(msgTitle);
        builder.setMessage(msgBody);
        builder.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (validationCode==202){

                        }
                    }

                });
        builder.show();


    }
    public void createJsonObjects(){
        JSONObject empDetails=null;
        try{
            empDetails = new JSONObject();
            empDetails.put("company",et_com_employed.getText().toString().trim());
            empDetails.put("design",et_designation.getText().toString().trim());
            empDetails.put("work",et_work_exp.getText().toString().trim());

        }
        catch (Exception e){
            Log.i("exception",e.toString());
        }
       Intent intent = new Intent(activity, PABNewCustSalDetailsActivity.class);
        intent.putExtra("customerdetails",customerdetails);
        intent.putExtra("employementdetails",empDetails.toString());
        activity.startActivity(intent);
        activity.overridePendingTransition(R.anim.slide_in_bottom, R.anim.slide_in_up);
    }
    public void onBackPressed() {
        Intent intent = new Intent(activity, PABNewCustLoanActivity.class);
        activity.finish();
        startActivity(intent);
        overridePendingTransition(R.anim.abc_fade_in, R.anim.abc_fade_out);
    }
}
