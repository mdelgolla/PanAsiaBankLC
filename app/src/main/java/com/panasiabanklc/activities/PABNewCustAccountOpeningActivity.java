package com.panasiabanklc.activities;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.basgeekball.awesomevalidation.AwesomeValidation;
import com.basgeekball.awesomevalidation.ValidationStyle;
import com.basgeekball.awesomevalidation.utility.RegexTemplate;
import com.panasiabanklc.PABUserLoginActivity;
import com.panasiabanklc.R;
import com.panasiabanklc.services.PABServiceConstant;
import com.panasiabanklc.utility.PABMultipartEntity;
import com.panasiabanklc.utility.PABPhoneNumberTextWatcher;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by user1 on 8/6/2017.
 */

public class PABNewCustAccountOpeningActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 100;
    public static final int MEDIA_TYPE_IMAGE = 1;
    // directory name to store captured images and videos
    private static final String IMAGE_DIRECTORY_NAME = "Android File Upload";
    private Uri fileUri; // file url to store image/video
    private Spinner accounttype,spin_modeof_contact;
    private ImageView header_left,btnmenu,imgPreview;
    private ProgressDialog mProgressDialog;
    private String progressTitle = "",progressMessage = "";
    private EditText et_full_name, et_address, et_nic_number,et_mobile,et_home_tel,et_office_tel,et_email_add;
    private Button btn_new_account,btnCapturePicture,btnUpload;
    private TextView tv_loan_calculator,txtPercentage;
    private RadioGroup rg_get_title;
    private String radiovalue, account, username,contact_mode,filePath = null;
    private RadioButton rb_rev,rb_mr,rb_mrs,rb_miss;
    private AwesomeValidation awesomeValidation;
    private ProgressBar progressBar;
    private long totalSize = 0;

    static PABNewCustAccountOpeningActivity activity = null;

    protected void onCreate(Bundle savedInstantState) {
        super.onCreate(savedInstantState);

        awesomeValidation = new AwesomeValidation(ValidationStyle.BASIC);

        activity = this;

        setContentView(R.layout.pab_new_cust_account);
        initViews();
    }

    public void initViews() {
        imgPreview = (ImageView) findViewById(R.id.imgPreview);
        header_left = (ImageView) findViewById(R.id.btn_header_left);
        header_left.setOnClickListener(this);
        btnmenu=(ImageView)findViewById(R.id.btnmenu);
        btnmenu.setOnClickListener(this);
        et_full_name = (EditText) findViewById(R.id.et_full_name);
        et_address = (EditText) findViewById(R.id.et_address);
        et_nic_number = (EditText) findViewById(R.id.et_nic_number);
        txtPercentage=(TextView) findViewById(R.id.txtPercentage);
        et_mobile=(EditText)findViewById(R.id.et_mobile);

        et_mobile.addTextChangedListener(new TextWatcher() {
            int mPreviousLen;
            boolean keyDel;
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                mPreviousLen = s.length();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                keyDel = mPreviousLen > s.length();
                et_mobile.setError(null);
                if(!keyDel){
                    if (!(et_mobile.getText().toString().startsWith("0")) ){
                        et_mobile.setText("");
                        et_mobile.setError("Start Mobile Number With '0'");
                    }
                }

            }
        });
        et_home_tel=(EditText)findViewById(R.id.et_home_tel);
        et_home_tel.addTextChangedListener(new PABPhoneNumberTextWatcher(et_home_tel));
        et_office_tel=(EditText)findViewById(R.id.et_office_tel);
        et_office_tel.addTextChangedListener(new PABPhoneNumberTextWatcher(et_office_tel));
        et_email_add=(EditText)findViewById(R.id.et_email_add) ;
        btn_new_account = (Button) findViewById(R.id.btn_new_account);
        btn_new_account.setOnClickListener(this);
        btnCapturePicture=(Button) findViewById(R.id.btnCapturePicture);
        btnCapturePicture.setOnClickListener(this);
        btnUpload = (Button) findViewById(R.id.btnUpload);
        btnUpload.setOnClickListener(this);
        tv_loan_calculator=(TextView)findViewById(R.id.tv_loan_calculator) ;
        tv_loan_calculator.setText("ACCOUNT OPENING");
        rg_get_title=(RadioGroup) findViewById(R.id.rg_get_title);
        Log.i("Radiovalue",":"+radiovalue);
        accounttype=(Spinner) findViewById(R.id.sp_accounttype);
        spin_modeof_contact=(Spinner) findViewById(R.id.spin_modeof_contact);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        awesomeValidation.addValidation(this, R.id.et_full_name, "^[A-Za-z\\s]{1,}[\\.]{0,1}[A-Za-z\\s]{0,}$", R.string.enter_name);
        awesomeValidation.addValidation(this, R.id.et_address, RegexTemplate.NOT_EMPTY, R.string.enter_addres);
        awesomeValidation.addValidation(this, R.id.et_nic_number, "^[0-9]{9}[V,X,v,x]", R.string.enter_nic);
        awesomeValidation.addValidation(this, R.id.et_mobile, "^[+]?[0-9]{10}$", R.string.enter_mobile);
        awesomeValidation.addValidation(this, R.id.et_home_tel, "^[+]?[0-9]{10}$", R.string.enter_hometel);
        awesomeValidation.addValidation(this, R.id.et_office_tel, "^[+]?[0-9]{10}$", R.string.enter_officetel);
        awesomeValidation.addValidation(this, R.id.et_email_add, "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$", R.string.enter_email);


        SharedPreferences preferences = getApplicationContext().getSharedPreferences("pabloancalculatordefaults", PABNewCustAccountOpeningActivity.this.MODE_PRIVATE);
        username=preferences.getString("Username","");
        Log.i("username",":"+username);

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setCancelable(false);

    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnCapturePicture:
                captureImage();
                // Checking camera availability
                if (!isDeviceSupportCamera()) {
                    Toast.makeText(getApplicationContext(),
                            "Sorry! Your device doesn't support camera",
                            Toast.LENGTH_LONG).show();
                    // will close the app if the device does't have camera
                    finish();
                }
                break;
            case R.id.btnUpload:
                new UploadFileToServer().execute();
                break;
            case R.id.btn_new_account:
                try {

                    radiovalue = ((RadioButton) this.findViewById(rg_get_title.getCheckedRadioButtonId())).getText().toString();
                    account = accounttype.getSelectedItem().toString();
                    contact_mode = spin_modeof_contact.getSelectedItem().toString();
                    if (awesomeValidation.validate()) {
                        newAccountRegister();
                        Toast.makeText(this, "Validation Successfull", Toast.LENGTH_LONG).show();
                    }
                }catch (Exception e){
                    Log.i("RegisterUser",":" + e);
                }
//                Log.i("RegisterUser"," : Clicked");
//                try{
//                    int validateCode = validateInputData(202);
//                    radiovalue=((RadioButton)this.findViewById(rg_get_title.getCheckedRadioButtonId())).getText().toString();
//                    account=accounttype.getSelectedItem().toString();
//                    contact_mode=spin_modeof_contact.getSelectedItem().toString();
//
//                    Log.i("RegisterUser"," : " + validateCode);
//                    if (validateCode == 202) {
//                        Log.i("RegisterUser"," Submit 1: " + validateCode);
//                        newAccountRegister();
//                    }else{
//                        Log.i("RegisterUser",":" + validateCode);
//                    }
//                }catch (Exception e){
//                    Log.i("RegisterUser",":" + e);
//                    // Tracking exception
//                    //LECApplicationActivity.getInstance().trackException(e);
//                }

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
                        SharedPreferences preferences = getApplicationContext().getSharedPreferences("pabloancalculatordefaults", PABNewCustAccountOpeningActivity.this.MODE_PRIVATE);
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
    public void newAccountRegister() {
        try {
            int validationCode = validateInputData(202);
            Toast.makeText(getApplicationContext(), "validateCode 1 :" + validationCode, Toast.LENGTH_SHORT).show();
            if (validationCode == 202) {
//                if (PABCheckNetworkConnection.isConnectionAvailable(getApplicationContext())) {
                    Toast.makeText(getApplicationContext(), "getAllAccountDetail 2:" + validationCode, Toast.LENGTH_SHORT).show();
                    new AccountOpeningApplication().execute(radiovalue,et_full_name.getText().toString().trim(),et_address.getText().toString().trim(),et_nic_number.getText().toString().trim(),account,et_mobile.getText().toString().trim(),et_home_tel.getText().toString().trim(),et_office_tel.getText().toString().trim(),et_email_add.getText().toString().trim(),
                            contact_mode,username);
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

    class AccountOpeningApplication extends AsyncTask<String, String, String>{
        @Override
        protected void onPreExecute() {
            Log.i("onpreexecute",":");
            super.onPreExecute();
            progressTitle = getString(R.string.loading_message);
            progressMessage = getString(R.string.loan_submit);
            viewProgressDialog(progressTitle, progressMessage);
        }

        @Override
        protected String doInBackground(String... params) {
          Log.i("result_1",":");
          String accountSubmitResult = "";

            try{
                DefaultHttpClient httpClient = new DefaultHttpClient();
                // HttpGet httpGet = new HttpGet(url);
                HttpPost httpPost = new HttpPost(PABServiceConstant.NEW_CUST_REGISTER);
                List<NameValuePair> nameValuePairs = new
                        ArrayList<NameValuePair>(11);
                Log.i("AccountRegister",":"+params[0]+"|"+params[1]+"|"+params[2]+"|"+params[3]+"|"+params[4]+"|"+params[5]);
                nameValuePairs.add(new BasicNameValuePair("title",params[0]));
                nameValuePairs.add(new BasicNameValuePair("name",params[1]));
                nameValuePairs.add(new BasicNameValuePair("address",params[2]));
                nameValuePairs.add(new BasicNameValuePair("nic",params[3]));
                nameValuePairs.add(new BasicNameValuePair("type",params[4]));
                nameValuePairs.add(new BasicNameValuePair("mobile",params[5]));
                nameValuePairs.add(new BasicNameValuePair("home",params[6]));
                nameValuePairs.add(new BasicNameValuePair("office",params[7]));
                nameValuePairs.add(new BasicNameValuePair("email",params[8]));
                nameValuePairs.add(new BasicNameValuePair("mode",params[9]));
                nameValuePairs.add(new BasicNameValuePair("user",params[10]));

                httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                //username,email,firstname,lastname,password,profileimg,userroleuid,creationdate,modifiedby,isactive
                try {
                    HttpResponse response= httpClient.execute(httpPost); // some response object
                    BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
                    accountSubmitResult = reader.readLine();
                    Log.i("shiv", "valeLogin:" + accountSubmitResult);
                }catch (Exception e){
                    Log.i("reg_fail",":"+e);
                }

            }catch (Exception e){
                Log.i("reg_fail_1",":"+e);
            }

           return accountSubmitResult;
        }

        @Override
        protected void onPostExecute(String result) {
            Log.i("onpostexecute",":");
            dissmissProgressDialog();
            Log.i("LoanApplicationRegistry", ":" + result);

            boolean response = false;
            String message ="";
            try{
                JSONObject jsonResponse = new JSONObject(result);
                if (!jsonResponse.isNull("response")) {
                    response = jsonResponse.getBoolean("response");
                }
                if(response == true) {
                    alertDialog();
                }else{
                    Toast.makeText(getApplicationContext(), "Loan Registration Failed. Please try again.", Toast.LENGTH_SHORT).show();
                }
            }catch (Exception e){
                Toast.makeText(getApplicationContext(), "Loan Registration Fail :" + e, Toast.LENGTH_SHORT).show();
            }

        }

    }
    public void alertDialog(){
        AlertDialog.Builder alertDialog=new AlertDialog.Builder(PABNewCustAccountOpeningActivity.this);
        alertDialog.setTitle("New Account Register");
        alertDialog.setMessage("Do you want to save a new customer?");
        alertDialog.setPositiveButton("YES",new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int which){
                SharedPreferences preferences = getApplicationContext().getSharedPreferences("pabnewcutaccountdefault", PABNewCustAccountOpeningActivity.this.MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("NICNumber", et_nic_number.getText().toString().trim());
                editor.commit();
                Intent submit = new Intent(getApplicationContext(), PABExistingCustAccountActivity.class);
                startActivity(submit);
                finish();

            }
        });
        alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int which){
                Intent submit = new Intent(getApplicationContext(), PABNewCustAccountOpeningActivity.class);
                startActivity(submit);
                finish();

            }
        });
        alertDialog.setNeutralButton("CANCEL",new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int which){
                Intent submit = new Intent(getApplicationContext(), PABNewCustAccountOpeningActivity.class);
                startActivity(submit);
                finish();

            }
        });
        alertDialog.show();

    }

    public int validateInputData(int validationCode) {
//        if (et_full_name.getText().toString().trim().length()<3){
//            validationCode = 404;
//            viewErrorValidation(getString(R.string.account_register), getString(R.string.enter_name), 404);
//        }
//        else if (et_address.getText().toString().trim().length()<3){
//            validationCode = 404;
//            viewErrorValidation(getString(R.string.account_register), getString(R.string.enter_addres), 404);
//        }
//        else if (et_nic_number.getText().toString().trim().length()<13){
//            validationCode = 404;
//            viewErrorValidation(getString(R.string.account_register), getString(R.string.enter_nic), 404);
//        }
//        if (et_mobile.getText().toString().trim().length()!=10){
//            validationCode = 404;
//            viewErrorValidation(getString(R.string.account_register), getString(R.string.enter_mobile), 404);
//        }
//        else if (et_home_tel.getText().toString().trim().length()!=10){
//            validationCode = 404;
//            viewErrorValidation(getString(R.string.account_register), getString(R.string.enter_hometel), 404);
//        }
//        else if (et_office_tel.getText().toString().trim().length()!=10){
//            validationCode = 404;
//            viewErrorValidation(getString(R.string.account_register), getString(R.string.enter_officetel), 404);
//        }
//        else if (et_email_add.getText().toString().trim().length()<3){
//            validationCode = 404;
//            viewErrorValidation(getString(R.string.account_register), getString(R.string.enter_email), 404);
//        }
//
    return validationCode;
    }
    /**
     * Checking device has camera hardware or not
     */
    private boolean isDeviceSupportCamera() {
        if (getApplicationContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }

    /**
     * Capturing Camera Image will lauch camera app requrest image capture
     */
    private void captureImage() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
        // start the image capture Intent
        startActivityForResult(intent, CAMERA_CAPTURE_IMAGE_REQUEST_CODE);
    }

    /**
     * Here we store the file url as it will be null after returning from camera
     * app
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // save file url in bundle as it will be null on scren orientation
        // changes
        outState.putParcelable("file_uri", fileUri);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        // get the file url
        fileUri = savedInstanceState.getParcelable("file_uri");
    }

    /**
     * Receiving activity result method will be called after closing the camera
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // if the result is capturing Image
        if (requestCode == CAMERA_CAPTURE_IMAGE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // successfully captured the image
                // display it in image view
                previewCapturedImage();
            } else if (resultCode == RESULT_CANCELED) {
                // user cancelled Image capture
                Toast.makeText(getApplicationContext(),
                        "User cancelled image capture", Toast.LENGTH_SHORT)
                        .show();
            } else {
                // failed to capture image
                Toast.makeText(getApplicationContext(),
                        "Sorry! Failed to capture image", Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }

    /**
     * Display image from a path to ImageView
     */
    private void previewCapturedImage() {
        try {
            imgPreview.setVisibility(View.VISIBLE);
            // bimatp factory
            BitmapFactory.Options options = new BitmapFactory.Options();
            // downsizing image as it throws OutOfMemory Exception for larger
            // images
            options.inSampleSize = 8;

            final Bitmap bitmap = BitmapFactory.decodeFile(fileUri.getPath(), options);

            imgPreview.setImageBitmap(bitmap);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }
    /**
     * ------------ Helper Methods ----------------------
     * */

    /**
     * Creating file uri to store image/video
     */
    public Uri getOutputMediaFileUri(int type) {

        return Uri.fromFile(getOutputMediaFile(type));
    }

    /**
     * returning image / video
     */
    private static File getOutputMediaFile(int type) {
        // External sdcard location
        File mediaStorageDir = new File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                IMAGE_DIRECTORY_NAME);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d(IMAGE_DIRECTORY_NAME, "Oops! Failed create "
                        + IMAGE_DIRECTORY_NAME + " directory");
                return null;
            }
        }
        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
                Locale.getDefault()).format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator
                    + "IMG_" + timeStamp + ".jpg");
        } else {
            return null;
        }

        return mediaFile;
    }


    private class UploadFileToServer extends AsyncTask<Void, Integer, String> {
        @Override
        protected void onPreExecute() {
            progressBar.setProgress(0);
            // setting progress bar to zero
            super.onPreExecute();
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            // Making progress bar visible
            progressBar.setVisibility(View.VISIBLE);

            // updating progress bar value
            progressBar.setProgress(progress[0]);

            // updating percentage value
            txtPercentage.setText(String.valueOf(progress[0]) + "%");
        }


        @Override
        protected String doInBackground(Void... params) {

            return uploadFile();
        }

        @SuppressWarnings("deprecation")
        private String uploadFile() {
            Log.i("upload", ":");
            String responseString = null;

            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(PABServiceConstant.FILE_UPLOAD_URL);

            try {
                PABMultipartEntity entity = new PABMultipartEntity(
                        new PABMultipartEntity.ProgressListener() {

                            @Override
                            public void transferred(long num) {
                                publishProgress((int) ((num / (float) totalSize) * 100));
                            }
                        });
                filePath = fileUri.getPath();
                Log.i("filePath", ":" + filePath);

                File sourceFile = new File(filePath);

                // Adding file data to http body
                entity.addPart("image", new FileBody(sourceFile));

                totalSize = entity.getContentLength();
                httppost.setEntity(entity);

                // Making server call
                HttpResponse response = httpclient.execute(httppost);
                HttpEntity r_entity = response.getEntity();

                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode == 200) {
                    // Server response
                    responseString = EntityUtils.toString(r_entity);
                } else {
                    responseString = "Error occurred! Http Status Code: "
                            + statusCode;
                }

            } catch (ClientProtocolException e) {
                responseString = e.toString();
                Log.i("responce", ":" + responseString);
            } catch (IOException e) {
                responseString = e.toString();
                Log.i("responce:1", ":" + responseString);
            }

            return responseString;

        }

        @Override
        protected void onPostExecute(String result) {
            viewErrorValidation("Upload Success", "Image Uploading Success", 202);
            super.onPostExecute(result);
        }
    }

    public  void viewErrorValidation(String msgTitle, String msgBody,final int validationCode ){
        AlertDialog.Builder builder=new AlertDialog.Builder(PABNewCustAccountOpeningActivity.this);
        builder.setTitle(msgTitle);
        builder.setMessage(msgBody);
        builder.setPositiveButton("OK",new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }

                });
        builder.show();

    }



    public void onBackPressed() {
        Intent intent = new Intent(activity, PABAccountOpeningActivity.class);
        activity.finish();
        startActivity(intent);
        overridePendingTransition(R.anim.abc_fade_in, R.anim.abc_fade_out);
    }

    public void viewProgressDialog(String progressTitleStr,String progressMessageStr){
        Log.i("onpreexecute_1",":");
        if(!mProgressDialog.isShowing()){
            mProgressDialog = new ProgressDialog(PABNewCustAccountOpeningActivity.this);
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

}
