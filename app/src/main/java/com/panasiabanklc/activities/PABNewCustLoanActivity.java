package com.panasiabanklc.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Patterns;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.basgeekball.awesomevalidation.AwesomeValidation;
import com.basgeekball.awesomevalidation.ValidationStyle;
import com.basgeekball.awesomevalidation.utility.RegexTemplate;
import com.panasiabanklc.PABUserLoginActivity;
import com.panasiabanklc.R;
import com.panasiabanklc.utility.PABPhoneNumberTextWatcher;

import org.json.JSONObject;

/**
 * Created by user1 on 8/6/2017.
 */

public class PABNewCustLoanActivity extends AppCompatActivity implements View.OnClickListener{
    private EditText full_name, address, nic_number,et_office_tel,et_home_tel,et_mobile,et_email_add;
    private TextView loan_calc;
    private Button btn_submit;
    private RadioGroup rb_title,rg_married_stastus;
    private ImageView header_left,btnmenu;
    private Spinner spin_dependencies,spin_loan_type,spin_modeof_contact;
    private String radio_val,radio_married_status,dependencies,loantype,modeof_contact;
    private AwesomeValidation awesomeValidation;
    static PABNewCustLoanActivity activity = null;


    protected void onCreate(Bundle savedInstantState) {
        super.onCreate(savedInstantState);

        awesomeValidation = new AwesomeValidation(ValidationStyle.BASIC);
        activity = this;

        setContentView(R.layout.pab_new_cust_loan);
        initViews();
    }
    public void initViews(){
        full_name = (EditText) findViewById(R.id.et_full_name);
        address = (EditText) findViewById(R.id.et_address);
        nic_number = (EditText) findViewById(R.id.et_nic_number);
        et_office_tel=(EditText) findViewById(R.id.et_office_tel);
        et_office_tel.addTextChangedListener(new PABPhoneNumberTextWatcher(et_office_tel));
        et_home_tel=(EditText)findViewById(R.id.et_home_tel);
        et_home_tel.addTextChangedListener(new PABPhoneNumberTextWatcher(et_home_tel));
        et_mobile=(EditText) findViewById(R.id.et_mobile);
        et_mobile.addTextChangedListener(new PABPhoneNumberTextWatcher(et_mobile));
        et_email_add=(EditText) findViewById(R.id.et_email_add);
        loan_calc = (TextView) findViewById(R.id.tv_loan_calculator);
        btn_submit = (Button) findViewById(R.id.btn_submit);
        rb_title=(RadioGroup) findViewById(R.id.rg_title);
        rg_married_stastus=(RadioGroup) findViewById(R.id.rg_married_stastus);
        spin_dependencies=(Spinner) findViewById(R.id.spin_dependencies);
        spin_loan_type=(Spinner) findViewById(R.id.spin_loan_type);
        spin_modeof_contact=(Spinner) findViewById(R.id.spin_modeof_contact);
        btn_submit.setOnClickListener(this);
        header_left=(ImageView) findViewById(R.id.btn_header_left);
        header_left.setOnClickListener(this);
        btnmenu=(ImageView)findViewById(R.id.btnmenu);
        btnmenu.setOnClickListener(this);
        loan_calc.setText("PERSONAL DETAILS");

        awesomeValidation.addValidation(this, R.id.et_full_name, "^[A-Za-z\\s]{1,}[\\.]{0,1}[A-Za-z\\s]{0,}$", R.string.enter_name);
        awesomeValidation.addValidation(this, R.id.et_address, RegexTemplate.NOT_EMPTY, R.string.enter_addres);
        awesomeValidation.addValidation(this, R.id.et_nic_number, "(^[0-9]{9}[V,X,v,x])|(^[0-9]{12})", R.string.enter_nic);
        awesomeValidation.addValidation(this, R.id.et_mobile, "^[+]?[0-9]{10}$", R.string.enter_mobile);
        awesomeValidation.addValidation(this, R.id.et_home_tel, "^[+]?[0-9]{10}$", R.string.enter_hometel);
        awesomeValidation.addValidation(this, R.id.et_office_tel, "^[+]?[0-9]{10}$", R.string.enter_officetel);
        awesomeValidation.addValidation(this, R.id.et_email_add, Patterns.EMAIL_ADDRESS, R.string.enter_email);

    }
    public void onClick(View v){
        switch (v.getId()){
            case R.id.btn_submit:
                try{
//                    int validateCode=validateInputData(202);
                    radio_val=((RadioButton)this.findViewById(rb_title.getCheckedRadioButtonId())).getText().toString();
                    radio_married_status=((RadioButton)this.findViewById(rg_married_stastus.getCheckedRadioButtonId())).getText().toString();
                    dependencies=spin_dependencies.getSelectedItem().toString();
                    loantype=spin_loan_type.getSelectedItem().toString();
                    modeof_contact=spin_modeof_contact.getSelectedItem().toString();
                    if (awesomeValidation.validate()) {
                        createJasonObjects();
                    }
//                    if (validateCode==202){
//                        createJasonObjects();
//                    }else {
//                        Log.i("LoanRegister",":"+validateCode);
//                    }
                }catch (Exception e){
                    Log.i("LoanRegiter",":"+e);
                }
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
                        SharedPreferences preferences = getApplicationContext().getSharedPreferences("pabloancalculatordefaults", PABNewCustLoanActivity.this.MODE_PRIVATE);
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

    private int validateInputData(int validationCode) {
        if(full_name.getText().toString().trim().length()<3){
            validationCode=404;
            viewErrorValidation(getString(R.string.register_fail),getString(R.string.enter_name),404);
        }else if (address.getText().toString().trim().length()<3){
            validationCode=404;
            viewErrorValidation(getString(R.string.register_fail),getString(R.string.enter_addres),404);
        }else if (nic_number.getText().toString().trim().length()<3){
            validationCode=404;
            viewErrorValidation(getString(R.string.register_fail),getString(R.string.enter_nic),404);
        }else if (et_office_tel.getText().toString().trim().length()!=10){
            validationCode=404;
            viewErrorValidation(getString(R.string.register_fail),getString(R.string.enter_officetel),404);
        }else if (et_home_tel.getText().toString().trim().length()!=10){
            validationCode=404;
            viewErrorValidation(getString(R.string.register_fail),getString(R.string.enter_hometel),404);
        }else if (et_mobile.getText().toString().trim().length()!=10){
            validationCode=404;
            viewErrorValidation(getString(R.string.register_fail),getString(R.string.enter_mobile),404);
        }else if (et_email_add.getText().toString().trim().length()<3){

            validationCode=404;
            viewErrorValidation(getString(R.string.register_fail),getString(R.string.enter_email),404);
        }
        return validationCode;


    }
    public void viewErrorValidation(String msgTitle, String msgBody,final int validationCode){
        AlertDialog.Builder builder=new AlertDialog.Builder(PABNewCustLoanActivity.this);
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

    public void createJasonObjects() {
        JSONObject custDetails = null;
        try{
            custDetails = new JSONObject();
            custDetails.put("title",radio_val);
            custDetails.put("name",full_name.getText().toString().trim());
            custDetails.put("address",address.getText().toString().trim());
            custDetails.put("nic",nic_number.getText().toString().trim());
            custDetails.put("status",radio_married_status);
            custDetails.put("depend",dependencies);
            custDetails.put("office",et_office_tel.getText().toString().trim());
            custDetails.put("home",et_home_tel.getText().toString().trim());
            custDetails.put("mob",et_home_tel.getText().toString().trim());
            custDetails.put("type",loantype);
            custDetails.put("email",et_email_add.getText().toString().trim());
            custDetails.put("mode",modeof_contact);

        }catch (Exception e){
            Log.i("","");
        }


        SharedPreferences preferences = getApplicationContext().getSharedPreferences("pabnewcustloandefault",PABNewCustLoanActivity.this.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("NICNumber", nic_number.getText().toString().trim());
        editor.commit();
        Intent intent = new Intent(activity, PABNewCustEmpDetailsActivity.class);
        intent.putExtra("customerdetails", custDetails.toString());
        activity.startActivity(intent);
        activity.overridePendingTransition(R.anim.abc_fade_in, R.anim.abc_fade_out);
    }
    public void onBackPressed(){
        Intent intent=new Intent(activity,PABLoanApplicationActivity.class);
        activity.finish();
        startActivity(intent);
        overridePendingTransition(R.anim.abc_fade_in, R.anim.abc_fade_out);
    }
}
