package com.panasiabanklc.activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.panasiabanklc.PABUserLoginActivity;
import com.panasiabanklc.R;
import com.panasiabanklc.services.PABServiceConstant;
import com.panasiabanklc.utility.PABNumberTextWatcher;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;

/**
 * Created by user1 on 8/6/2017.
 */

public class PABLoanCalculatorActivity extends AppCompatActivity implements View.OnClickListener {
    private ProgressDialog mProgressDialog;
    private SeekBar simpleSeekBar, sb_monthlypayment;
    private EditText et_loanamount, et_interestrate, et_basicsalary;
    private TextView tv_loan_period, tv_monthlyinstalment;
    private Button btn_email_result;
    private ImageView btn_header_left, btnmenu;
    private int progress, t = 0;
    private double principalamount, interestrate;
    private String progressTitle = "", progressMessage = "";
    private String customerdetails, salarydetails, username, basicsalary;
    private String employementdetails, loan_instalment, loan_period, data_string;
    private String filePath = null;
    long totalSize = 0;
    private int monthlyPayment, max_val, instalment, instalment_val, max_monthly_instalment, basic_sal = 0;
    static PABLoanCalculatorActivity activity = null;
    DecimalFormat formatter = new DecimalFormat("#,###,###");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = this;
        setContentView(R.layout.pab_loan_calculator);
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setCancelable(false);

        initViews();
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            try {
                customerdetails = bundle.getString("customerdetails");
                employementdetails = bundle.getString("employementdetails");
                salarydetails = bundle.getString("salarydetails");
                filePath = bundle.getString("filePath");
                Log.i("prinrtDetails: ", "100:" + customerdetails);
                Log.i("prinrtDetails: ", "200:" + employementdetails);
                Log.i("prinrtDetails: ", "300:" + salarydetails);
                //Log.i("prinrtDetails","301"+filePath);

            } catch (Exception e) {
                Log.i("EmpDetailList", "200:" + e);
            }
        }
        // seekbarScale();

        tv_loan_period.setText(simpleSeekBar.getProgress() + "/" + simpleSeekBar.getMax() + ":Years");

        simpleSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            //int progress =0;
            @Override
            public void onProgressChanged(SeekBar seekBar, int progressValue, boolean fromUser) {
                progress = progressValue;
                Log.i("progress", ":" + instalment);

                Toast.makeText(getApplicationContext(), "Changing Seekbar's progress", Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                Toast.makeText(getApplicationContext(), "Start tracking seekbar", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
//                sb_monthlypayment.setMax(max_monthly_instalment);
//                sb_monthlypayment.setProgress(instalment);

                Log.i("progress_1", ":" + instalment);
                calculateLoan();


                Toast.makeText(getApplicationContext(), "Stop tracking seekbar", Toast.LENGTH_SHORT).show();

            }
        });

        sb_monthlypayment.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress_val, boolean fromUser) {
                instalment_val = progress_val;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
//                seekBar.setProgress(Math.round((seekBar.getProgress()+(smoothnessFactor/2))/smoothnessFactor)*smoothnessFactor);
                loanPeriod();
            }
        });

    }

    public void initViews() {
        simpleSeekBar = (SeekBar) findViewById(R.id.simpleSeekBar);// initiate the progress bar
        sb_monthlypayment = (SeekBar) findViewById(R.id.sb_monthlypayment);
        tv_loan_period = (TextView) findViewById(R.id.tv_loan_preriod);
        et_loanamount = (EditText) findViewById(R.id.et_loanamount);
        et_loanamount.addTextChangedListener(new PABNumberTextWatcher(et_loanamount));

        et_interestrate = (EditText) findViewById(R.id.et_interestrate);

        et_basicsalary = (EditText) findViewById(R.id.et_basicsalary);
        et_basicsalary.addTextChangedListener(new PABNumberTextWatcher(et_basicsalary));

        btn_header_left = (ImageView) findViewById(R.id.btn_header_left);
        btn_header_left.setOnClickListener(this);
        btnmenu = (ImageView) findViewById(R.id.btnmenu);
        btnmenu.setOnClickListener(this);
        tv_monthlyinstalment = (TextView) findViewById(R.id.tv_monthlyinstalment);
        btn_email_result = (Button) findViewById(R.id.btn_email_result);
        btn_email_result.setOnClickListener(this);
        SharedPreferences preferences = getApplicationContext().getSharedPreferences("pabloancalculatordefaults", PABLoanCalculatorActivity.this.MODE_PRIVATE);
        username = preferences.getString("Username", "");
        Log.i("username", ":" + username);

        SharedPreferences pref = getApplicationContext().getSharedPreferences("pabcustsalrdefaults", PABLoanCalculatorActivity.this.MODE_PRIVATE);
        basicsalary = pref.getString("BasicSalary", "");
        et_basicsalary.setText(basicsalary);

        simpleSeekBar.setMax(5); // 5 maximum value for the Seek bar
        simpleSeekBar.setProgress(0); // 1 default progress value


//        Drawable draw = getResources().getDrawable(R.drawable.customprogressbar);
//        progressBar.setProgressDrawable(draw);
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_email_result:
                loan_instalment = Integer.toString(instalment);
                loan_period = Integer.toString(progress);
                try {
                    int validateCode = validateInputData(202);
                    if (validateCode == 202) {
                        createJsonObjects();
                    } else {
                        Log.i("EmpDetail", ":" + validateCode);
                    }
                } catch (Exception e) {
                    Log.i("EmpDetail", ":" + e);
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
                        SharedPreferences preferences = getApplicationContext().getSharedPreferences("pabloancalculatordefaults", PABLoanCalculatorActivity.this.MODE_PRIVATE);
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
            default:
                break;
        }

    }

    public int validateInputData(int validationCode) {

        return validationCode;
    }

    public void createJsonObjects() {
        JSONObject loanCalcDetails = null;
        try {
            loanCalcDetails = new JSONObject();
            String title = null, name = null, address = null, nic = null, status = null, depend = null, office = null, home = null, mob = null, type = null, email = null, mode = null, company = null, design = null, work = null, confirm = null, proof = null, basic_sal = null, fixed_allow = null, sal_deduction = null,
                    net_sal = null, filePath = null;
            //customerdetails,
            //"title":"Mr","name":"asdfhj","address":"adfbmk","nic":"256?9223578"
            JSONObject jsonResponseCusDet = new JSONObject(customerdetails);
            if (!jsonResponseCusDet.isNull("title")) {
                title = jsonResponseCusDet.getString("title");
            }
            if (!jsonResponseCusDet.isNull("name")) {
                name = jsonResponseCusDet.getString("name");
            }
            if (!jsonResponseCusDet.isNull("address")) {
                address = jsonResponseCusDet.getString("address");
            }
            if (!jsonResponseCusDet.isNull("nic")) {
                nic = jsonResponseCusDet.getString("nic");
            }
            if (!jsonResponseCusDet.isNull("status")) {
                status = jsonResponseCusDet.getString("status");
            }
            if (!jsonResponseCusDet.isNull("depend")) {
                depend = jsonResponseCusDet.getString("depend");
            }
            if (!jsonResponseCusDet.isNull("office")) {
                office = jsonResponseCusDet.getString("office");
            }
            if (!jsonResponseCusDet.isNull("home")) {
                home = jsonResponseCusDet.getString("home");
            }
            if (!jsonResponseCusDet.isNull("mob")) {
                mob = jsonResponseCusDet.getString("mob");
            }
            if (!jsonResponseCusDet.isNull("type")) {
                type = jsonResponseCusDet.getString("type");
            }
            if (!jsonResponseCusDet.isNull("email")) {
                email = jsonResponseCusDet.getString("email");
            }
            if (!jsonResponseCusDet.isNull("mode")) {
                mode = jsonResponseCusDet.getString("mode");
            }


            JSONObject jsonResponseEmpDet = new JSONObject(employementdetails);
            if (!jsonResponseEmpDet.isNull("company")) {
                company = jsonResponseEmpDet.getString("company");
            }
            if (!jsonResponseEmpDet.isNull("design")) {
                design = jsonResponseEmpDet.getString("design");
            }
            if (!jsonResponseEmpDet.isNull("work")) {
                work = jsonResponseEmpDet.getString("work");
            }

            JSONObject jsonResponseSalDet = new JSONObject(salarydetails);
            if (!jsonResponseSalDet.isNull("basic_sal")) {
                basic_sal = jsonResponseSalDet.getString("basic_sal");
            }
            if (!jsonResponseSalDet.isNull("fixed_allow")) {
                fixed_allow = jsonResponseSalDet.getString("fixed_allow");
            }
            if (!jsonResponseSalDet.isNull("sal_deduction")) {
                sal_deduction = jsonResponseSalDet.getString("sal_deduction");
            }
            if (!jsonResponseSalDet.isNull("net_sal")) {
                net_sal = jsonResponseSalDet.getString("net_sal");
            }
            if (!jsonResponseSalDet.isNull("filePath")) {
                filePath = jsonResponseSalDet.getString("filePath");
            }

            loanCalcDetails.put("title", title);
            loanCalcDetails.put("name", name);
            loanCalcDetails.put("address", address);
            loanCalcDetails.put("nic", nic);
            loanCalcDetails.put("status", status);
            loanCalcDetails.put("depend", depend);
            loanCalcDetails.put("office", office);
            loanCalcDetails.put("home", home);
            loanCalcDetails.put("mob", mob);
            loanCalcDetails.put("type", type);
            loanCalcDetails.put("email", email);
            loanCalcDetails.put("mode", mode);
            loanCalcDetails.put("company", company);
            loanCalcDetails.put("design", design);
            loanCalcDetails.put("work", work);
            loanCalcDetails.put("basic_sal", basic_sal);
            loanCalcDetails.put("fixed_allow", fixed_allow);
            loanCalcDetails.put("sal_deduction", sal_deduction);
            loanCalcDetails.put("net_sal", net_sal);
            loanCalcDetails.put("image_name", filePath);


//            Uri filePathUri = Uri.parse(filePath); // bimatp factory
//            ContentResolver cr = getContentResolver();
//            InputStream is = cr.openInputStream(filePathUri);
//            File sourceFile = new File(filePathUri.getPath());
//            byte[] data = getBytesFromFile(sourceFile);
//            byte[] encoded_data = Base64.encodeBase64(data);
//             data_string = new String(encoded_data);


//            BitmapFactory.Options options = new BitmapFactory.Options();
//            // downsizing image as it throws OutOfMemory Exception for larger
//            // images
//            options.inSampleSize = 8;
////File sourceFile = new File(filePathUri.getPath());
//            final Bitmap thumbnail = BitmapFactory.decodeFile(filePathUri.getPath(),options);

//            Uri filePathUri=Uri.parse(filePath);
//            File sourceFile = new File(filePathUri.getPath());
            File sourceFile = new File(filePath);
            loanCalcDetails.put("amount", et_loanamount.getText().toString().trim());
            loanCalcDetails.put("period", loan_period);
            loanCalcDetails.put("instalment", loan_instalment);
            loanCalcDetails.put("user", username);
            loanCalcDetails.put("file", new FileBody(sourceFile));

//           loanCalcDetails.put("file",sourceFile);

//            if (PABCheckNetworkConnection.isConnectionAvailable(getApplicationContext())) {
            //email,password
            new PABLoanCalculatorActivity.submitLoanApplication().execute(loanCalcDetails.toString());
                /*new PABNewCustSalDetailsActivity.submitLoanApplication().execute(title,name,address,nic,status,depend,mob,
                        type,email,company,design,work,confirm,et_basic_sal.getText().toString().trim());*/
//            } else {
//
//                //displayValidation("Please check the network connection");
//                Toast.makeText(getApplicationContext(), "Please check the network connection", Toast.LENGTH_SHORT).show();
//            }

        } catch (Exception e) {

        }
        Log.i("prinrtDetails ", "500" + loanCalcDetails);

    }

    class submitLoanApplication extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            progressTitle = getString(R.string.loading_message);
            progressMessage = getString(R.string.loan_submit);
            viewProgressDialog(progressTitle, progressMessage);
        }

        @Override
        protected String doInBackground(String... params) {
            String registerResults = null;

            try {
                HttpParams myParams = new BasicHttpParams();
                HttpConnectionParams.setConnectionTimeout(myParams, 10000);
                HttpConnectionParams.setSoTimeout(myParams, 10000);
                HttpClient httpclient = new DefaultHttpClient(myParams);
                String json = params[0];


                try {

                    HttpPost httppost = new HttpPost(PABServiceConstant.URL_LOAN_APPLICATION);
                    httppost.setHeader("Content-type", "application/json");

                    StringEntity se = new StringEntity(json);
                    se.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
                    httppost.setEntity(se);

                    HttpResponse response = httpclient.execute(httppost);
                    registerResults = EntityUtils.toString(response.getEntity());
                    Log.i("tag", registerResults);

//                    PABMultipartEntity entity=new PABMultipartEntity(
//                            new PABMultipartEntity.ProgressListener() {
//                                @Override
//                                public void transferred(long num) {
//                                    publishProgress((int)((num/(float)totalSize)*100));
//                                }
//                            }
//                    );


                } catch (ClientProtocolException e) {

                } catch (IOException e) {
                }
            } catch (Exception e) {

            }

            return registerResults;
        }

        @Override
        protected void onPostExecute(String result) {
            dissmissProgressDialog();
            Log.i("LoanApplicationRegistry", ":" + result);


            boolean response = false;
            try {
                JSONObject jsonResponse = new JSONObject(result);
                if (!jsonResponse.isNull("response")) {
                    response = jsonResponse.getBoolean("response");
                }
                if (response == true) {
                    viewErrorValidation(getString(R.string.form_submit), getString(R.string.form_sumbit_msg), 202);
//                   Intent submit = new Intent(getApplicationContext(), PABMainMenuActivity.class);
//                   startActivity(submit);
//                   finish();
                } else {
                    Toast.makeText(getApplicationContext(), "Loan Registration Failed. Please try again.", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), "Loan Registration Fail :" + e, Toast.LENGTH_SHORT).show();
            }

        }
    }

    public void viewErrorValidation(String msgTitle, String msgBody, final int validationCode) {
        AlertDialog.Builder builder = new AlertDialog.Builder(
                PABLoanCalculatorActivity.this);
        builder.setTitle(msgTitle);
        builder.setMessage(msgBody);
        builder.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,
                                        int which) {
                        if (validationCode == 202) {
                            Intent login = new Intent(getApplicationContext(), PABExistingCustLoanActivity.class);
                            startActivity(login);
                            finish();
                        }
                    }
                });
        builder.show();

    }


    public void viewProgressDialog(String progressTitleStr, String progressMessageStr) {
        if (!mProgressDialog.isShowing()) {
            mProgressDialog = new ProgressDialog(PABLoanCalculatorActivity.this);
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

    public void calculateLoan() {

        Log.i("loancalulator", ":");

        try {
            principalamount = Double.parseDouble(et_loanamount.getText().toString().replaceAll(",", ""));
            interestrate = Double.parseDouble(et_interestrate.getText().toString());
            basic_sal = Integer.parseInt(et_basicsalary.getText().toString().replaceAll(",", ""));
            max_monthly_instalment = ((Double) Math.ceil(basic_sal * 0.6)).intValue();

            //double loanperiod = Double.parseDouble(et_loanperiod.getText().toString());
            double r = interestrate / 100;
            double n = Math.pow(r + 1, progress * 1);
            double totalPayment = (principalamount * n);
            max_val = ((Double) Math.ceil(principalamount * n)).intValue();
            double monthlyPayment = (totalPayment / (progress * 12));
            instalment = ((Double) Math.ceil(monthlyPayment)).intValue();
            if (instalment < max_monthly_instalment) {
                sb_monthlypayment.setMax(max_monthly_instalment);
                sb_monthlypayment.setProgress(instalment);
                tv_monthlyinstalment.setText(formatter.format(instalment) + "/" + formatter.format(max_monthly_instalment) + ":LKR");
                tv_loan_period.setText(progress + "/" + simpleSeekBar.getMax() + ":Years");
            } else {
                viewErrorValidation("Warning", "Yo Are Exeeding The Maximum Monthly Instalment", 404);

            }
        } catch (Exception e) {
            Log.i("NumberFormat", "" + e);

        }
    }

    public void loanPeriod() {
        try {
            sb_monthlypayment.setMax(5);
            principalamount = Double.parseDouble(et_loanamount.getText().toString().replaceAll(",", ""));
            interestrate = Double.parseDouble(et_interestrate.getText().toString());
            basic_sal = Integer.parseInt(et_basicsalary.getText().toString().replaceAll(",", ""));
            max_monthly_instalment = ((Double) Math.ceil(basic_sal * 0.6)).intValue();

            //double loanperiod = Double.parseDouble(et_loanperiod.getText().toString());
            double r = interestrate / 100;
            double n = Math.pow(r + 1, (6 - instalment_val) * 1);
            double totalPayment = (principalamount * n);
            max_val = ((Double) Math.ceil(principalamount * n)).intValue();
            double monthlyPayment = (totalPayment / ((6 - instalment_val) * 12));
            instalment = ((Double) Math.ceil(monthlyPayment)).intValue();
            sb_monthlypayment.setMax(5);
            sb_monthlypayment.setMinimumWidth(1);
            if (instalment < max_monthly_instalment) {
                if (instalment_val == 1) {
                    simpleSeekBar.setProgress(5);
                    tv_monthlyinstalment.setText(formatter.format(instalment) + "/" + formatter.format(max_monthly_instalment) + ":LKR");
                    tv_loan_period.setText(simpleSeekBar.getProgress() + "/" + simpleSeekBar.getMax() + ":Years");
                }
                if (instalment_val == 2) {
                    simpleSeekBar.setProgress(4);
                    tv_monthlyinstalment.setText(formatter.format(instalment) + "/" + formatter.format(max_monthly_instalment) + ":LKR");
                    tv_loan_period.setText(simpleSeekBar.getProgress() + "/" + simpleSeekBar.getMax() + ":Years");
                }
                if (instalment_val == 3) {
                    simpleSeekBar.setProgress(3);
                    tv_monthlyinstalment.setText(formatter.format(instalment) + "/" + formatter.format(max_monthly_instalment) + ":LKR");
                    tv_loan_period.setText(simpleSeekBar.getProgress() + "/" + simpleSeekBar.getMax() + ":Years");
                }
                if (instalment_val == 4) {
                    simpleSeekBar.setProgress(2);
                    tv_monthlyinstalment.setText(formatter.format(instalment) + "/" + formatter.format(max_monthly_instalment) + ":LKR");
                    tv_loan_period.setText(simpleSeekBar.getProgress() + "/" + simpleSeekBar.getMax() + ":Years");
                }
                if (instalment_val == 5) {
                    simpleSeekBar.setProgress(1);
                    tv_monthlyinstalment.setText(formatter.format(instalment) + "/" + formatter.format(max_monthly_instalment) + ":LKR");
                    tv_loan_period.setText(simpleSeekBar.getProgress() + "/" + simpleSeekBar.getMax() + ":Years");
                }
            } else {
                viewErrorValidation("Warning", "Yo Are Exeeding The Maximum Monthly Instalment", 404);
            }
        } catch (Exception e) {

        }
//            try{
//                principalamount = Double.parseDouble(et_loanamount.getText().toString().replaceAll(",","")  );
//                t = ((Double) Math.ceil(principalamount / (instalment_val * 12))).intValue();
//                simpleSeekBar.setProgress(t);
//                tv_monthlyinstalment.setText(formatter.format(instalment_val) + "/" + formatter.format(max_monthly_instalment) + ":LKR");
//                tv_loan_period.setText(simpleSeekBar.getProgress() + "/" + simpleSeekBar.getMax() + ":Years");
//
//            }catch (Exception e){
//                Log.i("loanAmount",":"+e);
//            }

    }

    public void onBackPressed() {
        Intent intent = new Intent(activity, PABNewCustSalDetailsActivity.class);
        activity.finish();
        startActivity(intent);
        overridePendingTransition(R.anim.abc_fade_in, R.anim.abc_fade_out);

    }


    public static byte[] getBytesFromFile(File file) throws IOException {
        InputStream is = new FileInputStream(file);

        // Get the size of the file
        long length = file.length();

        // You cannot create an array using a long type.
        // It needs to be an int type.
        // Before converting to an int type, check
        // to ensure that file is not larger than Integer.MAX_VALUE.
        if (length > Integer.MAX_VALUE) {
            // File is too large
        }

        // Create the byte array to hold the data
        byte[] bytes = new byte[(int) length];

        // Read in the bytes
        int offset = 0;
        int numRead = 0;
        while (offset < bytes.length
                && (numRead = is.read(bytes, offset, Math.min(bytes.length - offset, 512*1024))) >= 0) {
            offset += numRead;
        }

        // Ensure all the bytes have been read in
        if (offset < bytes.length) {
            throw new IOException("Could not completely read file "+file.getName());
        }

        // Close the input stream and return bytes
        is.close();
        return bytes;
    }
}


