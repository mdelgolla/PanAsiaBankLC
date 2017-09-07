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
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.panasiabanklc.PABUserLoginActivity;
import com.panasiabanklc.R;
import com.panasiabanklc.services.PABServiceConstant;
import com.panasiabanklc.utility.PABCheckNetworkConnection;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by user1 on 8/6/2017.
 */

public class PABExistingCustAccountActivity extends AppCompatActivity implements View.OnClickListener {
    static PABExistingCustAccountActivity activity = null;
    private ProgressDialog mProgressDialog;
    private boolean errorChecked;
    private String progressTitle = "", progressMessage = "";
    private String nic_number;
    private ListView list_account;
    private EditText et_account_no;
    private Button btn_account_details;
    private ImageView btn_header_left,btnmenu;
    private TextView tv_loan_calculator;
    ArrayList<HashMap<String, String>> custAccountList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pab_existing_cust_account);
        activity = this;

        initViews();
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setCancelable(false);
    }

    public void initViews() {
        btn_account_details = (Button) findViewById(R.id.btn_account_details);
        btn_account_details.setOnClickListener(this);
        btn_header_left=(ImageView) findViewById(R.id.btn_header_left);
        btn_header_left.setOnClickListener(this);
        et_account_no = (EditText) findViewById(R.id.et_account_no);
        tv_loan_calculator=(TextView)findViewById(R.id.tv_loan_calculator);
        tv_loan_calculator.setText("ACCOUNT DETAILS");
        btnmenu=(ImageView) findViewById(R.id.btnmenu);
        btnmenu.setOnClickListener(this);
        list_account=(ListView) findViewById(R.id.list_account);
        custAccountList = new ArrayList<>();
        SharedPreferences preferences = getApplicationContext().getSharedPreferences("pabnewcutaccountdefault", PABExistingCustAccountActivity.this.MODE_PRIVATE);
        nic_number=preferences.getString("NICNumber","");
        et_account_no.setText(nic_number);
        Log.i("nicnumber",":"+nic_number);

    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_account_details:
                try {
                    int validateCode = getAccountValidation(202);
                    Toast.makeText(getApplicationContext(), "validateCode 1 :" + validateCode, Toast.LENGTH_SHORT).show();
                    if (validateCode == 202) {
                        Toast.makeText(getApplicationContext(), "validateCode 2 :" + validateCode, Toast.LENGTH_SHORT).show();
//                        if (PABCheckNetworkConnection.isConnectionAvailable(getApplicationContext())) {
                            //email,password
                            Toast.makeText(getApplicationContext(), "getAllLoanDetail 3:" + validateCode, Toast.LENGTH_SHORT).show();
                            new PABExistingCustAccountActivity.getAllAccountDetail().execute(et_account_no.getText().toString().trim());
//                        } else {
//
//                            //displayValidation("Please check the network connection");
//                            Toast.makeText(getApplicationContext(), "Please check the network connection", Toast.LENGTH_SHORT).show();
//                        }
                    }

                } catch (Exception e) {
                    Log.i("GetAllAccount",":");

                }

                break;
            case R.id.btn_header_left:
                onBackPressed();
                break ;
            case R.id.btnmenu:
                PopupMenu popup = new PopupMenu(activity, btnmenu);
                //Inflating the Popup using xml file
                popup.getMenuInflater().inflate(R.menu.pab_popup_menu, popup.getMenu());

                //registering popup with OnMenuItemClickListener
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        SharedPreferences preferences = getApplicationContext().getSharedPreferences("pabloancalculatordefaults", PABExistingCustAccountActivity.this.MODE_PRIVATE);
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.clear();
                        editor.commit();
                        Intent intent=new Intent(PABExistingCustAccountActivity.this, PABUserLoginActivity.class);
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

    public int getAccountValidation(int validationCode) {
        if (et_account_no.getText().toString().trim().length() == 0) {
            validationCode = 404;
            viewErrorValidation(getString(R.string.no_result), getString(R.string.account_no), 404);
        }

        return validationCode;
    }

    public void viewErrorValidation(String msgTitle, String msgBody, final int validationCode) {
        AlertDialog.Builder builder=new AlertDialog.Builder(PABExistingCustAccountActivity.this);
        builder.setTitle(msgTitle);
        builder.setMessage(msgBody);
        builder.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        errorChecked=false;

                    }
                });
        builder.show();

    }

    class getAllAccountDetail extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressTitle = getString(R.string.loading_message);
            progressMessage = getString(R.string.loan_detail);
            viewProgressDialog(progressTitle, progressMessage);
        }

        @Override
        protected String doInBackground(String... params) {
            String responseResult = "";
            try {
                DefaultHttpClient httpClient = new DefaultHttpClient();
                // HttpGet httpGet = new HttpGet(url);
                HttpPost httpPost = new HttpPost(PABServiceConstant.ACCOUNT_DETAIL_BYID + "nic");
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
                Log.i("responceResult", ":" + params[0]);
                nameValuePairs.add(new BasicNameValuePair("nic", params[0]));
                httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                //username,email,firstname,lastname,password,profileimg,userroleuid,creationdate,modifiedby,isactive
                HttpGet httpGetCall = new HttpGet(PABServiceConstant.ACCOUNT_DETAIL_BYID + "?" + URLEncodedUtils.format(nameValuePairs, "utf-8"));
                try {
                    HttpResponse response = httpClient.execute(httpGetCall); // some response object
                    BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
                    responseResult = reader.readLine();
                } catch (Exception e) {
                    viewErrorValidation(getString(R.string.no_result), getString(R.string.try_again), 404);
                }

            } catch (Exception e) {
                viewErrorValidation(getString(R.string.no_result), getString(R.string.try_again), 404);

            }

            return responseResult;
        }

        @Override
        protected void onPostExecute(String result) {
            dissmissProgressDialog();
            Log.i("GetAccountDetailByID", ":" + result);
            boolean response = false;
                try {
                    Log.i("RespnceData", ":");
                    JSONObject jsonResponse = new JSONObject(result);
                    if (!jsonResponse.isNull("response")) {
                        response = jsonResponse.getBoolean("response");
                    }
                    if (response == true) {

                        JSONArray jsonarray = jsonResponse.getJSONArray("account");
                        for (int i = 0; i < jsonarray.length(); i++) {
                            JSONObject jsonAccount = jsonarray.getJSONObject(i);
                            Log.i("RespnceData", ":" + jsonAccount);
                            String title = "", name = "", address = "", nic = "", acc_no = "", type = "", created_at = "", updated_at = "",mobile="",home_tel="",office_tel="",email="",contact_mode="";
                            if (!jsonAccount.isNull("title")) {
                                title = jsonAccount.getString("title");
                            }
                            if (!jsonAccount.isNull("name")) {
                                name = jsonAccount.getString("name");
                            }
                            if (!jsonAccount.isNull("address")) {
                                address = jsonAccount.getString("address");
                            }
                            if (!jsonAccount.isNull("nic")) {
                                nic = jsonAccount.getString("nic");
                            }
                            if (!jsonAccount.isNull("acc_no")) {
                                acc_no = jsonAccount.getString("acc_no");
                            }
                            if (!jsonAccount.isNull("type")) {
                                type = jsonAccount.getString("type");
                            }
                            if (!jsonAccount.isNull("created_at")) {
                                created_at = jsonAccount.getString("created_at");
                            }
                            if (!jsonAccount.isNull("updated_at")) {
                                updated_at = jsonAccount.getString("updated_at");
                            }
                            if (!jsonAccount.isNull("mobile")) {
                                mobile = jsonAccount.getString("mobile");
                            }
                            if (!jsonAccount.isNull("home_tel")) {
                                home_tel = jsonAccount.getString("home_tel");
                            }
                            if (!jsonAccount.isNull("office_tel")) {
                                office_tel = jsonAccount.getString("office_tel");
                            }
                            if (!jsonAccount.isNull("email")) {
                                email = jsonAccount.getString("email");
                            }
                            if (!jsonAccount.isNull("contact_mode")) {
                                contact_mode = jsonAccount.getString("contact_mode");
                            }

                            HashMap<String,String>custAccount=new HashMap<>();
                            custAccount.put("title",title);
                            custAccount.put("name",name);
                            custAccount.put("address",address);
                            custAccount.put("nic",nic);
                            custAccount.put("acc_no",acc_no);
                            custAccount.put("type",type);
                            custAccount.put("created_at",created_at);
                            custAccount.put("updated_at",updated_at);
                            custAccount.put("mobile",mobile);
                            custAccount.put("home_tel",home_tel);
                            custAccount.put("office_tel",office_tel);
                            custAccount.put("email",email);
                            custAccount.put("contact_mode",contact_mode);

                            custAccountList.add(custAccount);
                            Log.i("custAccountList",":"+custAccountList);
                            ListAdapter adapter = new SimpleAdapter(activity,custAccountList,
                                    R.layout.pab_existing_cust_account_list, new String[]{"title","name","address","nic","type","created_at","updated_at",
                            "mobile","home_tel","office_tel","email","contact_mode"},new int[]{
                                    R.id.tv_title,R.id.tv_accfullname,R.id.tv_accaddress,R.id.tv_accnicno,R.id.tv_acctype,R.id.tv_acc_createdat,R.id.tv_acc_updatedat,
                            R.id.tv_acc_mobile,R.id.tv_acc_hometel,R.id.tv_acc_officetel,R.id.tv_acc_email,R.id.tv_acc_modeofcontact});
                            list_account.setAdapter(adapter);

                        }
                    }
                    else{
                        viewErrorValidation(getString(R.string.no_result), getString(R.string.account_no), 404);
                    }

                    } catch(Exception e){
                        viewErrorValidation("Loan detail view failed", getString(R.string.please_try_again), 404);
                    }
                }

        }



    public void viewProgressDialog(String progressTitleStr,String progressMessageStr){
        if(!mProgressDialog.isShowing()){
            mProgressDialog = new ProgressDialog(PABExistingCustAccountActivity.this);
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
    public void dissmissProgressDialog(){
        if(mProgressDialog.isShowing()){
            mProgressDialog.dismiss();
        }
    }
    @Override
    public void onBackPressed(){
        Intent intent = new Intent(PABExistingCustAccountActivity.this, PABAccountOpeningActivity.class);
        PABExistingCustAccountActivity.this.finish();
        startActivity(intent);
        overridePendingTransition(R.anim.abc_fade_in, R.anim.abc_fade_out);
    }
}
