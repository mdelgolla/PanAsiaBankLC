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
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.panasiabanklc.PABUserLoginActivity;
import com.panasiabanklc.R;
import com.panasiabanklc.services.PABServiceConstant;
import com.panasiabanklc.utility.PABMultipartEntity;
import com.panasiabanklc.utility.PABMultipartEntity.ProgressListener;
import com.panasiabanklc.utility.PABNumberTextWatcher;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by user1 on 8/6/2017.
 */

public class PABNewCustSalDetailsActivity extends AppCompatActivity implements View.OnClickListener {
    // Activity request codes
    private static final int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 100;
    public static final int MEDIA_TYPE_IMAGE = 1;
    // directory name to store captured images and videos
    private static final String IMAGE_DIRECTORY_NAME = "Android File Upload";

    private Uri fileUri; // file url to store image/video
    private ImageView imgPreview, btn_header_left, btnmenu;
    private ProgressDialog mProgressDialog;
    private String progressTitle = "", progressMessage = "";
    private Button btnCapturePicture, btn_next, btnUpload;
    private EditText et_basic_sal, et_fixed_allowance, et_sal_deduction, et_net_sal, et_other_loan_ins;
    private TextView txtPercentage;
    private Double basic_sal, fixed_allowanc, sal_deductions, net_sal;
    private ProgressBar progressBar;
    private String customerdetails, username, net_sal_calc, filePath = null;
    private String employementdetails;
    private String custSalDetails;

    private long totalSize = 0;
    static PABNewCustSalDetailsActivity activity = null;

    protected void onCreate(Bundle savedInstantState) {
        super.onCreate(savedInstantState);
        setContentView(R.layout.pab_newcust_sal_details);

        activity = this;
        // Progress dialog
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setCancelable(false);
        initViews();
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            try {
                customerdetails = bundle.getString("customerdetails");
                employementdetails = bundle.getString("employementdetails");
                Log.i("prinrtDetails: ", "100:" + customerdetails);
                Log.i("prinrtDetails: ", "200:" + employementdetails);

            } catch (Exception e) {
                Log.i("EmpDetailList", "200:" + e);
            }
        }

    }

    public void initViews() {
        imgPreview = (ImageView) findViewById(R.id.imgPreview);
        btnCapturePicture = (Button) findViewById(R.id.btnCapturePicture);
        btnCapturePicture.setOnClickListener(this);
        btn_next = (Button) findViewById(R.id.btn_next_1);
        btn_next.setOnClickListener(this);
        btn_header_left = (ImageView) findViewById(R.id.btn_header_left);
        btn_header_left.setOnClickListener(this);
        btnmenu = (ImageView) findViewById(R.id.btnmenu);
        btnmenu.setOnClickListener(this);
        et_basic_sal = (EditText) findViewById(R.id.et_basic_sal);
        et_basic_sal.addTextChangedListener(new PABNumberTextWatcher(et_basic_sal));
        et_fixed_allowance = (EditText) findViewById(R.id.et_fixed_allowance);
        et_fixed_allowance.addTextChangedListener(new PABNumberTextWatcher(et_fixed_allowance));
        et_sal_deduction = (EditText) findViewById(R.id.et_sal_deductions);
        et_sal_deduction.addTextChangedListener(new PABNumberTextWatcher(et_sal_deduction));
        et_net_sal = (EditText) findViewById(R.id.et_net_sal);
        et_net_sal.addTextChangedListener(new PABNumberTextWatcher(et_net_sal));
        et_other_loan_ins = (EditText) findViewById(R.id.et_other_loan_ins);
        et_other_loan_ins.addTextChangedListener(new PABNumberTextWatcher(et_other_loan_ins));
        txtPercentage = (TextView) findViewById(R.id.txtPercentage);
        btnUpload = (Button) findViewById(R.id.btnUpload);
        btnUpload.setOnClickListener(this);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);


//        basic_sal=Double.parseDouble(et_basic_sal.getText().toString().replaceAll(",",""));
//        fixed_allowanc=Double.parseDouble(et_fixed_allowance.getText().toString().replaceAll(",",""));
//        sal_deductions=Double.parseDouble(et_sal_deduction.getText().toString().replaceAll(",",""));
//        net_sal=((basic_sal+fixed_allowanc)-sal_deductions);
//        net_sal_calc=String.valueOf(net_sal);
//        et_net_sal.setText(net_sal_calc);

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
            case R.id.btn_next_1:


                try {
                    int validateCode = validateInputData(202);
                    if (validateCode == 202) {
                        createJsonObject();
                    } else {
                        Log.i("EmpDetail", ":" + validateCode);
                    }
                } catch (Exception e) {
                    Log.i("EmpDetail", ":" + e);
                }
//                validateInputData(202);
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
                        SharedPreferences preferences = getApplicationContext().getSharedPreferences("pabloancalculatordefaults", PABNewCustSalDetailsActivity.this.MODE_PRIVATE);
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.clear();
                        editor.commit();
                        Intent intent = new Intent(activity, PABUserLoginActivity.class);
                        startActivity(intent);
                        finish();
                        Log.i("username", ":");
                        return true;
                    }
                });

                popup.show();//showing popup menu
                break;
            default:
                break;
        }

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
            Log.i("upload",":");
            String responseString = null;

            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(PABServiceConstant.FILE_UPLOAD_URL);

            try {
                PABMultipartEntity entity = new PABMultipartEntity(
                        new ProgressListener() {

                            @Override
                            public void transferred(long num) {
                                publishProgress((int) ((num / (float) totalSize) * 100));
                            }
                        });
                filePath= fileUri.getPath();
                Log.i("filePath",":"+filePath);

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
                Log.i("responce",":"+responseString);
            } catch (IOException e) {
                responseString = e.toString();
                Log.i("responce:1",":"+responseString);
            }

            return responseString;

        }
        @Override
        protected void onPostExecute(String result) {
            viewErrorValidation("Upload Success","Image Uploading Success",202);
            super.onPostExecute(result);
        }


    }

    public void viewProgressDialog(String progressTitleStr,String progressMessageStr){
        if(!mProgressDialog.isShowing()){
            mProgressDialog = new ProgressDialog(
                    PABNewCustSalDetailsActivity.this);
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

    public int validateInputData(int validationCode){
        if (et_basic_sal.getText().toString().trim().length() < 3) {
            validationCode = 404;
            viewErrorValidation(getString(R.string.register_fail), getString(R.string.enter_basicsalary), 404);
        } else if (et_fixed_allowance.getText().toString().trim().length() == 0) {
            validationCode = 404;
            viewErrorValidation(getString(R.string.register_fail), getString(R.string.enter_fiixedallowance), 404);
        } else if (et_sal_deduction.getText().toString().trim().length() ==0) {
            validationCode = 404;
            viewErrorValidation(getString(R.string.register_fail), getString(R.string.enter_salary_deductions), 404);

        }return validationCode;
    }
    public void viewErrorValidation(String msgTitle, String msgBody,final int validationCode) {
        AlertDialog.Builder builder=new AlertDialog.Builder(PABNewCustSalDetailsActivity.this);
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
    public void createJsonObject(){
        JSONObject custSalDetails=null;
        try{
            custSalDetails=new JSONObject();

            custSalDetails.put("basic_sal",et_basic_sal.getText().toString().trim());
            custSalDetails.put("fixed_allow",et_fixed_allowance.getText().toString().trim());
            custSalDetails.put("sal_deduction",et_sal_deduction.getText().toString().trim());
            custSalDetails.put("net_sal",et_net_sal.getText().toString().trim());
            custSalDetails.put("filePath",fileUri.getPath());

        }
        catch (Exception e){

        }
        Log.i("prinrtDetails ","400" + custSalDetails);

        SharedPreferences preferences = getApplicationContext().getSharedPreferences("pabcustsalrdefaults", PABNewCustSalDetailsActivity.this.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("BasicSalary", et_basic_sal.getText().toString().trim());
        editor.commit();

        Intent intent = new Intent(activity, PABLoanCalculatorActivity.class);
        intent.putExtra("customerdetails",customerdetails);
        intent.putExtra("employementdetails",employementdetails);
        //.putExtra("filePath", fileUri.getPath());
        intent.putExtra("salarydetails",custSalDetails.toString());
        activity.startActivity(intent);
        activity.overridePendingTransition(R.anim.slide_in_bottom, R.anim.slide_in_up);

    }

    public void onBackPressed() {
        Intent intent = new Intent(activity, PABNewCustEmpDetailsActivity.class);
        activity.finish();
        startActivity(intent);
        overridePendingTransition(R.anim.abc_fade_in, R.anim.abc_fade_out);
    }

}