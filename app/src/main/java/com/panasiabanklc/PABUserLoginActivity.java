package com.panasiabanklc;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.panasiabanklc.activities.PABLoanCalculatorActivity;
import com.panasiabanklc.activities.PABMainMenuActivity;
import com.panasiabanklc.services.PABGPSTracker;
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
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class PABUserLoginActivity extends AppCompatActivity implements View.OnClickListener {

    static PABUserLoginActivity activity = null;
    public  static final int PERMISSIONS_MULTIPLE_REQUEST = 123;

    private EditText ed_username,ed_password;
    private Button btn_login;
    private TextView tv_forgetpw;

    private ProgressDialog mProgressDialog;
    private String progressTitle = "",progressMessage = "";
    private static final String TAG_USERS = "user";
    private static final String TAG_PID = "id";
    PABGPSTracker pabgpsTracker;
    PABJSONParser jsonParser = new PABJSONParser();
    JSONObject json;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pab_user_login_activity);
        activity = this;

        // Progress dialog
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setCancelable(false);

        initViews();
        //Check for externl user permission
        checkAndroidVersion();
    }

    public void initViews(){
        ed_username  = (EditText) findViewById(R.id.ed_username);
        ed_password = (EditText) findViewById(R.id.ed_password);
        btn_login = (Button) findViewById(R.id.btn_login);
        btn_login.setOnClickListener(this);

        tv_forgetpw = (TextView) findViewById(R.id.tv_forgetpw);
    }

    private void checkAndroidVersion() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkPermission();
        } else {
            // write your logic here
        }
    }

    // Implement the OnClickListener callback
    public void onClick(View v) {
        // do something when the button is clicked
        switch (v.getId() /*to get clicked view id**/) {
            case R.id.btn_login:
                userlogging();

                break;
            default:
                break;
        }
    }
    public void userlogging(){
        try{
            int validateCode = getLoginValidation(202);
            Toast.makeText(getApplicationContext(), "validateCode 1 :" + validateCode, Toast.LENGTH_SHORT).show();
            if (validateCode == 202) {
                Toast.makeText(getApplicationContext(), "validateCode 2 :" + validateCode, Toast.LENGTH_SHORT).show();
//                if (PABCheckNetworkConnection.isConnectionAvailable(getApplicationContext())) {
                    //email,password
                    Toast.makeText(getApplicationContext(), "submitUserLogin 3:" + validateCode, Toast.LENGTH_SHORT).show();
                    new submitUserLogin().execute(ed_username.getText().toString().trim(),ed_password.getText().toString().trim());
//                } else {
//
//                    //displayValidation("Please check the network connection");
//                    Toast.makeText(getApplicationContext(), "Please check the network connection", Toast.LENGTH_SHORT).show();
//                }
            }
        }catch (Exception e){
            Log.i("Login"," : " + e);
        }

    }

    public  int getLoginValidation(int validationCode){
        Log.i("Login"," : " + validationCode);
        if (ed_username.getText().toString().trim().length()==0){
            validationCode = 404;
            viewErrorValidation(getString(R.string.login_fail), getString(R.string.username), 404);
        }
        else if(ed_password.getText().toString().trim().length() == 0){
            validationCode = 404;
            viewErrorValidation(getString(R.string.login_fail), getString(R.string.enter_password), 404);
        }

        return validationCode;
    }

    class submitUserLogin extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            progressTitle = getString(R.string.login_user);
            progressMessage = getString(R.string.login_user_msg);
            viewProgressDialog(progressTitle,progressMessage);
        }

        @Override
        protected String doInBackground(String... params) {
            String registerResults = null;

           // Toast.makeText(getApplicationContext(), "Exception 1:" + params[0], Toast.LENGTH_SHORT).show();
            try{
                DefaultHttpClient httpClient = new DefaultHttpClient();
                //HttpGet httpGetCall = new HttpGet(PABServiceConstant.URL_LOGIN);
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
                Log.i("RegisterActivity",":" + params[0] + "|" + params[1]);
                nameValuePairs.add(new BasicNameValuePair("id",params[0]));
                nameValuePairs.add(new BasicNameValuePair("key",params[1]));
                //httpGetCall.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                HttpGet httpGetCall = new HttpGet(PABServiceConstant.URL_LOGIN +"?"+ URLEncodedUtils.format(nameValuePairs, "utf-8"));
                try {
                    HttpResponse response= httpClient.execute(httpGetCall); // some response object
                    BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
                    registerResults = reader.readLine();

                }catch (Exception e){
                    //Toast.makeText(getApplicationContext(), "Exception 1:" + e.toString(), Toast.LENGTH_SHORT).show();
                    viewErrorValidation(getString(R.string.login_fail), getString(R.string.please_try_again), 404);
                }

            }catch (Exception e){
                //Toast.makeText(getApplicationContext(), "Exception 2:" + e.toString(), Toast.LENGTH_SHORT).show();
                viewErrorValidation(getString(R.string.login_fail), getString(R.string.please_try_again), 404);
            }

            return registerResults;
        }
        @Override
        protected void onPostExecute(String result) {
           dissmissProgressDialog();
            Log.i("RegisterActivity_1",":" + result);
            boolean user = false;
            String message="";
            try{
                Log.i("RegisterActivity_1",":" + result);
                JSONObject jsonResponse = new JSONObject(result);
                if (!jsonResponse.isNull("user")) {
                    user = jsonResponse.getBoolean("user");
                }
                if(user == true) {
                    locationFinder();

//                    Intent login = new Intent(getApplicationContext(), PABMainMenuActivity.class);
//                    startActivity(login);
//                    finish();
                }else{
                    Toast.makeText(getApplicationContext(), "Login Fail. Please try again.", Toast.LENGTH_SHORT).show();
                }
            }
            catch (Exception e){
                Toast.makeText(getApplicationContext(), "Login Fail :" + e, Toast.LENGTH_SHORT).show();
            }


        }
    }
    public void locationFinder(){
        pabgpsTracker=new PABGPSTracker(PABUserLoginActivity.this);
        if (pabgpsTracker.canGetLocation()){
            double latitude=pabgpsTracker.getLatitude();
            String getlatitude=Double.toString(latitude);
            double longitude=pabgpsTracker.getLongitude();
            String getlongitude=Double.toString(longitude);
            Toast.makeText(getApplicationContext(), "Your Location is - \nLat: " + latitude + "\nLong: " + longitude, Toast.LENGTH_LONG).show();
            new submitUserCurrentLocation().execute(ed_username.getText().toString().trim(),getlatitude,getlongitude);
        }else {
            pabgpsTracker.showSettingsAlert();
        }

    }
    class submitUserCurrentLocation extends AsyncTask<String, String, String>{

        @Override
        protected void onPreExecute() {
            progressTitle = getString(R.string.location_acquiring);
            progressMessage = getString(R.string.location_msg);
            viewProgressDialog(progressTitle,progressMessage);
        }

        @Override
        protected String doInBackground (String...params){
            String locationResult = null;

            // Toast.makeText(getApplicationContext(), "Exception 1:" + params[0], Toast.LENGTH_SHORT).show();
            try{
                DefaultHttpClient httpClient = new DefaultHttpClient();
                //HttpGet httpGetCall = new HttpGet(PABServiceConstant.URL_LOGIN);
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(3);
                Log.i("FindLocation",":" + params[0] + "|" + params[1]+"|" +params[2]);
                nameValuePairs.add(new BasicNameValuePair("user",params[0]));
                nameValuePairs.add(new BasicNameValuePair("lat",params[1]));
                nameValuePairs.add(new BasicNameValuePair("long",params[2]));
                //httpGetCall.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                HttpGet httpGetCall = new HttpGet(PABServiceConstant.URL_LOCATION +"?"+ URLEncodedUtils.format(nameValuePairs, "utf-8"));
                try {
                    HttpResponse response= httpClient.execute(httpGetCall); // some response object
                    BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
                    locationResult = reader.readLine();

                }catch (Exception e){
                    //Toast.makeText(getApplicationContext(), "Exception 1:" + e.toString(), Toast.LENGTH_SHORT).show();
                    viewErrorValidation("Location Error", getString(R.string.please_try_again), 404);
                }

            }catch (Exception e){
                //Toast.makeText(getApplicationContext(), "Exception 2:" + e.toString(), Toast.LENGTH_SHORT).show();
                viewErrorValidation("Location Error", getString(R.string.please_try_again), 404);
            }

            return locationResult;

        }
        @Override
        protected void onPostExecute(String result) {
            dissmissProgressDialog();
            Log.i("RegisterActivity_1",":" + result);
            boolean response = false;
            String message="";
            try{
                Log.i("Location_Result",":" + result);
                JSONObject jsonResponse = new JSONObject(result);
                if (!jsonResponse.isNull("response")) {
                    response = jsonResponse.getBoolean("response");
                }
                if(response == true) {
                    SharedPreferences preferences = getApplicationContext().getSharedPreferences("pabloancalculatordefaults", PABUserLoginActivity.this.MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("Username", ed_username.getText().toString().trim());
                    editor.putString("UserPassword", ed_password.getText().toString().trim());
                    editor.commit();

                    viewErrorValidation(getString(R.string.logging_success),getString(R.string.logging_messge),202);

                }else{
                    Toast.makeText(getApplicationContext(), "Location Find Fail.", Toast.LENGTH_SHORT).show();
                }
            }
            catch (Exception e){
                Toast.makeText(getApplicationContext(), "Location Find Fail._1" + e, Toast.LENGTH_SHORT).show();
            }


        }
    }

    public void viewProgressDialog(String progressTitleStr,String progressMessageStr){
        if(!mProgressDialog.isShowing()){
            mProgressDialog = new ProgressDialog(
                    PABUserLoginActivity.this);
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


    public void viewErrorValidation(String msgTitle, String msgBody,final int validationCode) {
        AlertDialog.Builder builder = new AlertDialog.Builder(
                PABUserLoginActivity.this);
        builder.setTitle(msgTitle);
        builder.setMessage(msgBody);
        builder.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,
                                        int which) {
                        if(validationCode == 202){
                            Intent login = new Intent(getApplicationContext(), PABMainMenuActivity.class);
                            startActivity(login);
                            finish();
                        }
                    }
                });
        builder.show();

    }


    private void checkPermission() {
        if (ContextCompat.checkSelfPermission(activity,Manifest.permission.READ_EXTERNAL_STORAGE)
                + ContextCompat.checkSelfPermission(activity,Manifest.permission.ACCESS_COARSE_LOCATION)
                + ContextCompat.checkSelfPermission(activity,Manifest.permission.ACCESS_FINE_LOCATION)
                + ContextCompat.checkSelfPermission(activity,Manifest.permission.WRITE_EXTERNAL_STORAGE)
                + ContextCompat.checkSelfPermission(activity,Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.READ_EXTERNAL_STORAGE) ||
                    ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.ACCESS_COARSE_LOCATION) ||
                    ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.ACCESS_FINE_LOCATION) ||
                    ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) ||
                    ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.CAMERA) ) {

                Snackbar.make(activity.findViewById(android.R.id.content),
                        "Please Grant Permissions to upload profile photo",
                        Snackbar.LENGTH_INDEFINITE).setAction("ENABLE",
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    requestPermissions(
                                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                                                    Manifest.permission.ACCESS_COARSE_LOCATION,
                                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                                    Manifest.permission.CAMERA},
                                            PERMISSIONS_MULTIPLE_REQUEST);
                                }

                            }
                        }).show();
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                                    Manifest.permission.ACCESS_COARSE_LOCATION,
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                    Manifest.permission.CAMERA},
                            PERMISSIONS_MULTIPLE_REQUEST);
                }

            }
        } else {
            // write your logic code if permission already granted
        }
    }

}
