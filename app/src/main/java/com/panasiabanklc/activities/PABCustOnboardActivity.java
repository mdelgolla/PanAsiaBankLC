package com.panasiabanklc.activities;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
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
import android.widget.TextView;
import android.widget.Toast;

import com.basgeekball.awesomevalidation.AwesomeValidation;
import com.basgeekball.awesomevalidation.ValidationStyle;
import com.basgeekball.awesomevalidation.utility.RegexTemplate;
import com.panasiabanklc.PABUserLoginActivity;
import com.panasiabanklc.R;
import com.panasiabanklc.services.PABServiceConstant;
import com.panasiabanklc.utility.PABPhoneNumberTextWatcher;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by user1 on 8/6/2017.
 */

public class PABCustOnboardActivity extends AppCompatActivity implements View.OnClickListener {
    private TextView tv_cust_onboard;
    private EditText et_full_name, et_address, et_nic_number, et_home_tel, et_mobilenum, et_email_add;
    private RadioGroup rg_title;
    private ImageView header_left,btnmenu;
    private Button btn_submit;
    private ProgressDialog mProgressDialog;
    private String progressTitle = "", progressMessage = "";
    private String radiotitle,username;
    private AwesomeValidation awesomeValidation;
    static PABCustOnboardActivity activity = null;

    protected void onCreate(Bundle savedInstantState) {
        super.onCreate(savedInstantState);
        setContentView(R.layout.pab_cust_onboard);
        awesomeValidation = new AwesomeValidation(ValidationStyle.BASIC);
        activity = this;
        initViews();

    }

    public void initViews() {
        tv_cust_onboard = (TextView) findViewById(R.id.tv_loan_calculator);
        tv_cust_onboard.setText("CUSTOMER ONBOARD");
        rg_title=(RadioGroup) findViewById(R.id.rg_title);
        et_full_name = (EditText) findViewById(R.id.et_full_name);
        et_address = (EditText) findViewById(R.id.et_address);
        et_nic_number = (EditText) findViewById(R.id.et_nic_number);
        et_home_tel = (EditText) findViewById(R.id.et_home_tel);
        et_home_tel.addTextChangedListener(new PABPhoneNumberTextWatcher(et_home_tel));
        et_mobilenum = (EditText) findViewById(R.id.et_mobilenum);
        et_mobilenum.addTextChangedListener(new PABPhoneNumberTextWatcher(et_mobilenum));
        et_email_add = (EditText) findViewById(R.id.et_email_add);
        header_left = (ImageView) findViewById(R.id.btn_header_left);
        header_left.setOnClickListener(this);
        btnmenu=(ImageView) findViewById(R.id.btnmenu);
        btnmenu.setOnClickListener(this);
        btn_submit = (Button) findViewById(R.id.btn_submit);
        btn_submit.setOnClickListener(this);

        SharedPreferences preferences = getApplicationContext().getSharedPreferences("pabloancalculatordefaults", PABCustOnboardActivity.this.MODE_PRIVATE);
        username=preferences.getString("Username","");
        Log.i("username",":"+username);

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setCancelable(false);

        awesomeValidation.addValidation(this, R.id.et_full_name, "^[A-Za-z\\s]{1,}[\\.]{0,1}[A-Za-z\\s]{0,}$", R.string.enter_name);
        awesomeValidation.addValidation(this, R.id.et_address, RegexTemplate.NOT_EMPTY, R.string.enter_addres);
        awesomeValidation.addValidation(this, R.id.et_nic_number, "^[0-9]{9,12}[V,X]?$", R.string.enter_nic);
        awesomeValidation.addValidation(this, R.id.et_mobile, "^[+]?[0-9]{10}$", R.string.enter_mobile);
        awesomeValidation.addValidation(this, R.id.et_home_tel, "^[+]?[0-9]{10}$", R.string.enter_hometel);
        awesomeValidation.addValidation(this, R.id.et_email_add, Patterns.EMAIL_ADDRESS, R.string.enter_email);

    }


    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_header_left:
                onBackPressed();
                break;
            case R.id.btn_submit:
                try {
                    //int validateCode = validateInputData(202);
                    radiotitle=((RadioButton)this.findViewById(rg_title.getCheckedRadioButtonId())).getText().toString();
                    if (awesomeValidation.validate()) {
                        custonboardAccount();
                        Toast.makeText(this, "Validation Successfull", Toast.LENGTH_LONG).show();
                    }
//                    Log.i("CustOnBoard", " : " + validateCode);
//                    if (validateCode == 202) {
//                        Log.i("CustOnBoard", " Submit 1: " + validateCode);
//                        custonboardAccount();
//                    } else {
//                        Log.i("CustOnBoard", ":" + validateCode);
//                    }
                } catch (Exception e) {

                }

                break;
            case R.id.btnmenu:
                PopupMenu popup = new PopupMenu(activity, btnmenu);
                //Inflating the Popup using xml file
                popup.getMenuInflater().inflate(R.menu.pab_popup_menu, popup.getMenu());

                //registering popup with OnMenuItemClickListener
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        SharedPreferences preferences = getApplicationContext().getSharedPreferences("pabloancalculatordefaults", PABCustOnboardActivity.this.MODE_PRIVATE);
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

    public void custonboardAccount() {
        try {
            int validationCode = validateInputData(202);
            Toast.makeText(getApplicationContext(), "validateCode 1 :" + validationCode, Toast.LENGTH_SHORT).show();
            if (validationCode == 202) {
//                if (PABCheckNetworkConnection.isConnectionAvailable(getApplicationContext())) {
                    Toast.makeText(getApplicationContext(), "getAllLoanDetail 2:" + validationCode, Toast.LENGTH_SHORT).show();
                    new AccountOnboardApplication().execute(radiotitle,et_full_name.getText().toString().trim(),et_address.getText().toString().trim(), et_nic_number.getText().toString().trim(), et_mobilenum.getText().toString().trim(),et_home_tel.getText().toString().trim(),
                            et_email_add.getText().toString().trim(),username);
                    //registerUser(et_username.getText().toString(),et_email.getText().toString(),et_firstname.getText().toString(),et_lastname.getText().toString(),et_password.getText().toString(),"","1",getCurrentDate(),getCurrentDate(),"1");
//                } else {
//                    //displayValidation("Please check the network connection");
//                    Toast.makeText(getApplicationContext(), "Please check the network connection", Toast.LENGTH_SHORT).show();
//                }
            }
        } catch (Exception e) {
            Log.i("Account Submit", ":" + e);

        }
    }

    class AccountOnboardApplication extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            Log.i("onpreexecute", ":");
            super.onPreExecute();
            progressTitle = getString(R.string.account_submit);
            progressMessage = getString(R.string.account_submit_msg);
            viewProgressDialog(progressTitle, progressMessage);
        }

        @Override
        protected String doInBackground(String... params) {
            String custonboardResult = "";
            try {
                DefaultHttpClient httpClient = new DefaultHttpClient();
                // HttpGet httpGet = new HttpGet(url);
                HttpPost httpPost = new HttpPost(PABServiceConstant.URL_CUST_ONBOARD);
                List<NameValuePair> nameValuePairs = new
                        ArrayList<NameValuePair>(8);
                nameValuePairs.add(new BasicNameValuePair("title", params[0]));
                nameValuePairs.add(new BasicNameValuePair("name", params[1]));
                nameValuePairs.add(new BasicNameValuePair("address", params[2]));
                nameValuePairs.add(new BasicNameValuePair("nic", params[3]));
                nameValuePairs.add(new BasicNameValuePair("mobile", params[4]));
                nameValuePairs.add(new BasicNameValuePair("home", params[5]));
                nameValuePairs.add(new BasicNameValuePair("email", params[6]));
                nameValuePairs.add(new BasicNameValuePair("user", params[7]));
                httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                //username,email,firstname,lastname,password,profileimg,userroleuid,creationdate,modifiedby,isactive
                try {
                    HttpResponse response = httpClient.execute(httpPost); // some response object
                    BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
                    custonboardResult = reader.readLine();
                    Log.i("shiv", "valeLogin:" + custonboardResult);
                } catch (Exception e) {
                    Log.i("reg_fail", ":" + e);
                }

            } catch (Exception e) {
                Log.i("reg_fail_1", ":" + e);
            }
            return custonboardResult;
        }

        @Override
        protected void onPostExecute(String result) {
            Log.i("onpostexecute", ":");
            dissmissProgressDialog();
            Log.i("LoanApplicationRegistry", ":" + result);

            boolean response = false;
            String message = "";
            try {
                JSONObject jsonResponse = new JSONObject(result);
                if (!jsonResponse.isNull("response")) {
                    response = jsonResponse.getBoolean("response");
                }
                if (response == true) {
                    alertDialog();
                } else {
                    Toast.makeText(getApplicationContext(), "Loan Registration Failed. Please try again.", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), "Loan Registration Fail :" + e, Toast.LENGTH_SHORT).show();
            }

        }
    }

    public void alertDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(PABCustOnboardActivity.this);
        alertDialog.setTitle("New Account Register");
        alertDialog.setMessage("Do you want to save a new customer?");
        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent submit = new Intent(getApplicationContext(), PABMainMenuActivity.class);
                startActivity(submit);
                finish();

            }
        });
        alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        alertDialog.setNeutralButton("CANCEL", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        alertDialog.show();

    }

    public int validateInputData(int validationCode) {
        if (et_full_name.getText().toString().trim().length() < 3) {
            validationCode = 404;
            viewErrorValidation(getString(R.string.account_register), getString(R.string.enter_name), 404);
        } else if (et_address.getText().toString().trim().length() < 3) {
            validationCode = 404;
            viewErrorValidation(getString(R.string.account_register), getString(R.string.enter_addres), 404);
        } else if (et_nic_number.getText().toString().trim().length() != 10) {
            validationCode = 404;
            viewErrorValidation(getString(R.string.account_register), getString(R.string.enter_nic), 404);
        } else if (et_home_tel.getText().toString().trim().length() != 10) {
            validationCode = 404;
            viewErrorValidation(getString(R.string.account_register), getString(R.string.enter_hometel), 404);
        } else if (et_mobilenum.getText().toString().trim().length() != 10) {
            validationCode = 404;
            viewErrorValidation(getString(R.string.account_register), getString(R.string.enter_mobile), 404);
        } else if (et_email_add.getText().toString().trim().length() < 3) {
            validationCode = 404;
            viewErrorValidation(getString(R.string.account_register), getString(R.string.enter_email), 404);
        }

        return validationCode;
    }

    public void viewErrorValidation(String msgTitle, String msgBody, final int validationCode) {
        AlertDialog.Builder builder = new AlertDialog.Builder(PABCustOnboardActivity.this);
        builder.setTitle(msgTitle);
        builder.setMessage(msgBody);
        builder.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }

                });
        builder.show();

    }

    public void viewProgressDialog(String progressTitleStr, String progressMessageStr) {
        Log.i("onpreexecute_1", ":");
        if (!mProgressDialog.isShowing()) {
            mProgressDialog = new ProgressDialog(PABCustOnboardActivity.this);
            mProgressDialog.setMax(100);
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mProgressDialog.setCancelable(false);
            // Set progressdialog title
            mProgressDialog.setTitle(progressTitleStr);
            // Set progressdialog message
            mProgressDialog.setMessage(progressMessageStr);
            mProgressDialog.setIndeterminate(false);
            // Show progressdialog
            mProgressDialog.show();
        }
    }

    public void dissmissProgressDialog() {
        if (mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    public void onBackPressed() {
        Intent intent = new Intent(activity, PABMainMenuActivity.class);
        activity.finish();
        startActivity(intent);
        overridePendingTransition(R.anim.abc_fade_in, R.anim.abc_fade_out);
    }
}
