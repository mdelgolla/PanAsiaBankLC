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
import com.panasiabanklc.services.PABJSONParser;
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
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by user1 on 8/6/2017.
 */

public class PABExistingCustLoanActivity extends AppCompatActivity implements View.OnClickListener {
    static PABExistingCustLoanActivity activity = null;
    private ProgressDialog mProgressDialog;
    private String progressTitle = "",progressMessage = "";
    private String nic_number;
    private ListView list_loan;
    private boolean errorChecked;

    private EditText loanid;
    private Button btn_loandetails;
    private ListView list;
    private ImageView header_left,btnmenu;
    private TextView tv_loan_calculator;
    ArrayList<HashMap<String, String>> custLoanList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pab_existing_cust_loan);
        activity=this;

        initViews();

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setCancelable(false);

    }

    public void initViews() {
        loanid = (EditText) findViewById(R.id.et_loan_id);
        btn_loandetails = (Button) findViewById(R.id.btn_loan_details);
        btn_loandetails.setOnClickListener(this);
        header_left=(ImageView) findViewById(R.id.btn_header_left);
        header_left.setOnClickListener(this);
        list_loan=(ListView) findViewById(R.id.list_loan);
        tv_loan_calculator=(TextView)findViewById(R.id.tv_loan_calculator);
        tv_loan_calculator.setText("LOAN DETAILS");
        btnmenu=(ImageView) findViewById(R.id.btnmenu);
        btnmenu.setOnClickListener(this);
        custLoanList = new ArrayList<>();

        SharedPreferences preferences = getApplicationContext().getSharedPreferences("pabnewcustloandefault", PABExistingCustLoanActivity.this.MODE_PRIVATE);
        nic_number=preferences.getString("NICNumber","");
        loanid.setText(nic_number);
        Log.i("nicnumber",":"+nic_number);

    }
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_loan_details:
                try {
                    int validateCode = getLoginValidation(202);
                    Toast.makeText(getApplicationContext(), "validateCode 1 :" + validateCode, Toast.LENGTH_SHORT).show();
                    if (validateCode == 202) {
                        Toast.makeText(getApplicationContext(), "validateCode 2 :" + validateCode, Toast.LENGTH_SHORT).show();
//                        if (PABCheckNetworkConnection.isConnectionAvailable(getApplicationContext())) {
                            //email,password
                            Toast.makeText(getApplicationContext(), "getAllLoanDetail 3:" + validateCode, Toast.LENGTH_SHORT).show();
                            new getAllLoanDetail().execute(loanid.getText().toString().trim());
//                        } else {
//
//                            //displayValidation("Please check the network connection");
//                            Toast.makeText(getApplicationContext(), "Please check the network connection", Toast.LENGTH_SHORT).show();
//                        }
                    }

                }catch (Exception e){
                    Log.i("Get_loan_Detail",":");

                }

                break;
            case R.id.btn_header_left:
                onBackPressed();
                break;
            case R.id.btnmenu:
                PopupMenu popup = new PopupMenu(PABExistingCustLoanActivity.this, btnmenu);
                //Inflating the Popup using xml file
                popup.getMenuInflater().inflate(R.menu.pab_popup_menu, popup.getMenu());

                //registering popup with OnMenuItemClickListener
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        SharedPreferences preferences = getApplicationContext().getSharedPreferences("pabloancalculatordefaults", PABExistingCustLoanActivity
                                .this.MODE_PRIVATE);
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.clear();
                        editor.commit();
                        Intent intent=new Intent(PABExistingCustLoanActivity.this, PABUserLoginActivity.class);
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
    public  int getLoginValidation(int validationCode){
        if (loanid.getText().toString().trim().length()==0){
            validationCode = 404;
            viewErrorValidation(getString(R.string.no_result), getString(R.string.try_again), 404);
        }

        return validationCode;
    }

    public  void viewErrorValidation(String msgTitle, String msgBody,final int validationCode ){
        AlertDialog.Builder builder=new AlertDialog.Builder(PABExistingCustLoanActivity.this);
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


    class getAllLoanDetail extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressTitle = getString(R.string.loading_message);
            progressMessage = getString(R.string.loan_detail);
            viewProgressDialog(progressTitle,progressMessage);
        }
        @Override
        protected String doInBackground(String... params) {
            String responseResult = "";
            try {
                DefaultHttpClient httpClient = new DefaultHttpClient();
                // HttpGet httpGet = new HttpGet(url);
                HttpPost httpPost = new HttpPost(PABServiceConstant.URL_LOAN_DETAIL_BYID + "nic");
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
                Log.i("responceResult",":" + params[0] );
                nameValuePairs.add(new BasicNameValuePair("nic", params[0]));
                httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                //username,email,firstname,lastname,password,profileimg,userroleuid,creationdate,modifiedby,isactive
                HttpGet httpGetCall = new HttpGet(PABServiceConstant.URL_LOAN_DETAIL_BYID +"?"+ URLEncodedUtils.format(nameValuePairs, "utf-8"));
                try {
                    HttpResponse response= httpClient.execute(httpGetCall); // some response object
                    BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
                    responseResult = reader.readLine();
                } catch (Exception e) {
                    viewErrorValidation(getString(R.string.no_result), getString(R.string.try_again), 404);
                }

            }catch (Exception e){
                viewErrorValidation(getString(R.string.no_result), getString(R.string.try_again), 404);

            }

            return responseResult;
        }
        @Override
        protected void onPostExecute(String result) {
            dissmissProgressDialog();
            Log.i("GetLoanDetailByID",":"+ result);
            boolean response = false;

            try{
                JSONObject jsonResponse = new JSONObject(result);
                Log.i("RespnceData",":" );
                if (!jsonResponse.isNull("response")) {
                    response = jsonResponse.getBoolean("response");
                }
                if (response == true) {
                    JSONArray jsonarray = jsonResponse.getJSONArray("loan");
                    for (int i = 0; i < jsonarray.length(); i++) {
                        JSONObject jsonLoanDetail = jsonarray.getJSONObject(i);
                        Log.i("RespnceData_1", ":" + jsonLoanDetail);
                        String title = "", name = "", address = "", nic = "", married_status = "", depend = "", type = "", home_tel = "", mob_no = "",office_tel="",modeof_contact="",
                                email = "", company = "", designation = "", work = "", confirm = "", basic_sal = "", fixed_allow = "", sal_deduction = "", net_sal = "", other_loan = "",
                        loan_amount="",loan_period="",monthly_instalment="",authorized_user="",created_at="",updated_at="";
                        if (!jsonLoanDetail.isNull("title")) {
                            title = jsonLoanDetail.getString("title");
                        }
                        if (!jsonLoanDetail.isNull("name")) {
                            name = jsonLoanDetail.getString("name");
                        }
                        if (!jsonLoanDetail.isNull("address")) {
                            address = jsonLoanDetail.getString("address");
                        }
                        if (!jsonLoanDetail.isNull("nic")) {
                            nic = jsonLoanDetail.getString("nic");
                        }
                        if (!jsonLoanDetail.isNull("married_status")) {
                            married_status = jsonLoanDetail.getString("married_status");
                        }
                        if (!jsonLoanDetail.isNull("depend")) {
                            depend = jsonLoanDetail.getString("depend");
                        }
                        if (!jsonLoanDetail.isNull("type")) {
                            type = jsonLoanDetail.getString("type");
                        }
                        if (!jsonLoanDetail.isNull("home_tel")) {
                            home_tel = jsonLoanDetail.getString("home_tel");
                        }
                        if (!jsonLoanDetail.isNull("mob_no")) {
                            mob_no = jsonLoanDetail.getString("mob_no");
                        }
                        if (!jsonLoanDetail.isNull("office_tel")) {
                            office_tel = jsonLoanDetail.getString("office_tel");
                        }
                        if (!jsonLoanDetail.isNull("modeof_contact")) {
                            modeof_contact = jsonLoanDetail.getString("modeof_contact");
                        }
                        if (!jsonLoanDetail.isNull("email")) {
                            email = jsonLoanDetail.getString("email");
                        }
                        if (!jsonLoanDetail.isNull("company")) {
                            company = jsonLoanDetail.getString("company");
                        }
                        if (!jsonLoanDetail.isNull("designation")) {
                            designation = jsonLoanDetail.getString("designation");
                        }
                        if (!jsonLoanDetail.isNull("work")) {
                            work = jsonLoanDetail.getString("work");
                        }
                        if (!jsonLoanDetail.isNull("confirm")) {
                            confirm = jsonLoanDetail.getString("confirm");
                        }
                        if (!jsonLoanDetail.isNull("basic_sal")) {
                            basic_sal = jsonLoanDetail.getString("basic_sal");
                        }
                        if (!jsonLoanDetail.isNull("fixed_allow")) {
                            fixed_allow = jsonLoanDetail.getString("fixed_allow");
                        }
                        if (!jsonLoanDetail.isNull("sal_deduction")) {
                            sal_deduction = jsonLoanDetail.getString("sal_deduction");
                        }
                        if (!jsonLoanDetail.isNull("net_sal")) {
                            net_sal = jsonLoanDetail.getString("net_sal");
                        }
                        if (!jsonLoanDetail.isNull("other_loan")) {
                            other_loan = jsonLoanDetail.getString("other_loan");
                        }
                        if (!jsonLoanDetail.isNull("loan_amount")) {
                            loan_amount = jsonLoanDetail.getString("loan_amount");
                        }
                        if (!jsonLoanDetail.isNull("loan_period")) {
                            loan_period = jsonLoanDetail.getString("loan_period");
                        }
                        if (!jsonLoanDetail.isNull("monthly_instalment")) {
                            monthly_instalment = jsonLoanDetail.getString("monthly_instalment");
                        }
                        if (!jsonLoanDetail.isNull("authorized_user")) {
                            authorized_user = jsonLoanDetail.getString("authorized_user");
                        }
                        if (!jsonLoanDetail.isNull("created_at")) {
                            created_at = jsonLoanDetail.getString("created_at");
                        }
                        if (!jsonLoanDetail.isNull("updated_at")) {
                            updated_at = jsonLoanDetail.getString("updated_at");
                        }

                        HashMap<String,String>custLoan=new HashMap<>();
                        custLoan.put("title",title);
                        custLoan.put("name",name);
                        custLoan.put("address",address);
                        custLoan.put("nic",nic);
                        custLoan.put("married_status",married_status);
                        custLoan.put("type",type);
                        custLoan.put("email",email);
                        custLoan.put("mob_no",mob_no);
                        custLoan.put("home_tel",home_tel);
                        custLoan.put("office_tel",office_tel);
                        custLoan.put("modeof_contact",modeof_contact);
                        custLoan.put("company",company);
                        custLoan.put("designation",designation);
                        custLoan.put("basic_sal",basic_sal);
                        custLoan.put("other_loan",other_loan);
                        custLoan.put("loan_amount",loan_amount);
                        custLoan.put("loan_period",loan_period);
                        custLoan.put("monthly_instalment",monthly_instalment);
                        custLoan.put("authorized_user",authorized_user);
                        custLoan.put("created_at",created_at);
                        custLoan.put("updated_at",updated_at);
                        custLoanList.add(custLoan);
                        Log.i("custAccountList",":"+custLoanList);

                        ListAdapter adapter = new SimpleAdapter(activity,custLoanList,R.layout.pab_existing_cust_loan_list, new String[]{"title","name","address","nic","married_status","type",
                        "mob_no","home_tel","office_tel","modeof_contact","company","designation","basic_sal","other_loan","loan_amount","loan_period","monthly_instalment","authorized_user","created_at","updated_at"},
                                new int[]{R.id.tv_title,R.id.tv_fullname,R.id.tv_address,R.id.tv_nic,R.id.tv_married_status,R.id.tv_loan_type,R.id.tv_contact,R.id.tv_contact_home,R.id.tv_contact_office,R.id.tv_contact_mode,
                               R.id.tv_company,R.id.tv_designation,R.id.tv_basic_sal,R.id.tv_otherloan,R.id.tv_loanamount ,R.id.tv_loan_period,R.id.tv_monthly_payment,R.id.tv_registeredby,R.id.tv_create_date,R.id.tv_updatedate});
                        list_loan.setAdapter(adapter);

                    }
                }
                else{
                    viewErrorValidation(getString(R.string.no_result), getString(R.string.account_no), 404);
                }

            }catch (Exception e){
                viewErrorValidation("Loan detail view failed", getString(R.string.please_try_again), 404);
            }

        }

        }

    public void viewProgressDialog(String progressTitleStr,String progressMessageStr){
        if(!mProgressDialog.isShowing()){
            mProgressDialog = new ProgressDialog(PABExistingCustLoanActivity.this);
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
        Intent intent = new Intent(PABExistingCustLoanActivity.this, PABLoanApplicationActivity.class);
        PABExistingCustLoanActivity.this.finish();
        startActivity(intent);
        overridePendingTransition(R.anim.abc_fade_in, R.anim.abc_fade_out);
    }
    }

