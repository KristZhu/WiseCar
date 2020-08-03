package com.wisecarCompany.wisecarapp.function.insuranceRecord;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.wisecarCompany.wisecarapp.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InsuranceRecordSendActivity extends AppCompatActivity {

    private final static String TAG = "Insurance Record Send";

    private String IP_HOST = "http://54.206.19.123:3000";
    private String GET_INSURANCE_REFCORD_INFO = "/api/v1/insurancerecords/getrecordbyid";
    private String SEND_EMAIL = "/api/v1/insurancerecords/sendemail";

    private SharedPreferences sp;
    private String userID;

    private String recordID;

    private ImageButton backImageButton;

    private TextView headerTextView;
    private TextView policyNoTextView;
    private TextView insurerTextView;
    private TextView startDateTextView;
    private TextView endDateTextView;
    private TextView typeTextView;
    private TextView documentLinkTextView;

    private EditText emailEditText;
    private Button sendButton;
    private String email;

    private SimpleDateFormat displayDateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_insurance_record_send);

        sp = this.getSharedPreferences("userInfo", MODE_PRIVATE);
        userID = sp.getString("USER_ID", "");
        Log.d(TAG, "userID: " + userID);

        recordID = (String) this.getIntent().getStringExtra("recordID");
        Log.d(TAG, "recordID: " + recordID);

        backImageButton = $(R.id.backImageButton);
        backImageButton.setOnClickListener(v -> startActivity(new Intent(this, InsuranceRecordDashboardActivity.class)));

        headerTextView = $(R.id.headerTextView);
        policyNoTextView = $(R.id.policyNoTextView);
        insurerTextView = $(R.id.insurerTextView);
        startDateTextView = $(R.id.startDateTextView);
        endDateTextView = $(R.id.endDateTextView);
        typeTextView = $(R.id.typeTextView);
        documentLinkTextView = $(R.id.documentLinkTextView);

        emailEditText = $(R.id.emailEditText);
        sendButton = $(R.id.sendButton);


        // SOME INFO YOU CAN GET IN HERE
        getInsuranceRecordInfo(new insuranceRecordSendCallbacks() {
            @Override
            public void onSuccess(@NonNull InsuranceRecord record) {

                //SimpleDateFormat format = new SimpleDateFormat("ddMMM yyyy", Locale.getDefault());
                headerTextView.setText("Police Number: " + record.getPolicyNo());
                policyNoTextView.setText(record.getPolicyNo());
                insurerTextView.setText(record.getInsurer());
                startDateTextView.setText(displayDateFormat.format(record.getStartDate()));
                endDateTextView.setText(displayDateFormat.format(record.getEndDate()));
                typeTextView.setText(record.getType());

                documentLinkTextView.setOnClickListener(v -> {
                    Log.d(TAG, "document link url: " + record.getDocumentLink());
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(record.getDocumentLink())));
                });

                sendButton.setOnClickListener(v -> {
                    email = emailEditText.getText().toString();
                    boolean isEmail = false;
                    try {
                        String check = "^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$";
                        Pattern regex = Pattern.compile(check);
                        Matcher matcher = regex.matcher(email);
                        isEmail = matcher.matches();
                    } catch (Exception e) {
                        isEmail = false;
                    }
                    if (isEmail) {

                        record.setEmailAddress(email);
                        sendEmail(record);

                    } else {
                        Toast.makeText(getApplicationContext(), "Please enter correct email address", Toast.LENGTH_SHORT).show();
                    }
                });

            }
        });

    }

    public boolean dispatchTouchEvent(MotionEvent ev) {
        View v = getCurrentFocus();
        if (isShouldHideInput(v, ev)) {
            assert v != null;
            hideSoftInput(v.getWindowToken());
        }
        return super.dispatchTouchEvent(ev);
    }

    private boolean isShouldHideInput(View v, MotionEvent event) {
        if (v instanceof EditText) {
            int[] l = {0, 0};
            v.getLocationInWindow(l);
            int left = l[0],
                    top = l[1],
                    bottom = top + v.getHeight(),
                    right = left + v.getWidth();
            return !(event.getX() > left && event.getX() < right
                    && event.getY() > top && event.getY() < bottom);
        }
        return false;
    }

    private void hideSoftInput(IBinder token) {
        if (token != null) {
            InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            assert manager != null;
            manager.hideSoftInputFromWindow(token, InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    private <T extends View> T $(int id) {
        return (T) findViewById(id);
    }

    private void getInsuranceRecordInfo(@Nullable final insuranceRecordSendCallbacks callbacks) {

        String URL = IP_HOST + GET_INSURANCE_REFCORD_INFO;

        final JSONObject jsonParam = new JSONObject();
        try {
            jsonParam.put("record_id", recordID);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest objectRequest = new JsonObjectRequest(Request.Method.POST, URL, jsonParam, response -> {
            Log.e("Records Response", response.toString());
            JSONObject jsonObject = response;
            DateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            InsuranceRecord insuranceRecord;
            try {

                insuranceRecord = new InsuranceRecord(
                        response.optString("service_id"),
                        response.optString("policy_number"),
                        response.optString("insurer"),
                        format.parse(response.optString("cover_start_date")),
                        format.parse(response.optString("cover_end_date")),
                        response.optString("cover_type"),
                        response.optString("file_url")
                );

                if (callbacks != null)
                    callbacks.onSuccess(insuranceRecord);
            } catch (ParseException e) {
                e.printStackTrace();
            }

        }, error -> {
            Log.e("ERROR", String.valueOf(error.networkResponse));

            NetworkResponse networkResponse = error.networkResponse;
            if (networkResponse != null && networkResponse.data != null) {
                String JSONError = new String(networkResponse.data);
                JSONObject messageJO;
                String message = "";
                try {
                    messageJO = new JSONObject(JSONError);
                    message = messageJO.optString("message");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Log.e("JSON ERROR MESSAGE", message);
            }

        });
        Volley.newRequestQueue(this).add(objectRequest);

    }

    public interface insuranceRecordSendCallbacks {
        void onSuccess(@NonNull InsuranceRecord value);

//        void onError(@NonNull List<ServiceRecord> value);
    }

    private void sendEmail(InsuranceRecord record) {

        String URL = IP_HOST + SEND_EMAIL;
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        final JSONObject jsonParam = new JSONObject();
        try {
            jsonParam.put("service_id", "5");
            jsonParam.put("email_to_address", record.getEmailAddress());
            jsonParam.put("submit_date_time", format.format(new Date()));
            jsonParam.put("user_id", userID);
            jsonParam.put("policy_number", record.getPolicyNo());
            jsonParam.put("insurer", record.getInsurer());
            jsonParam.put("start_of_cover", dateFormat.format(record.getStartDate()));
            jsonParam.put("end_of_cover", dateFormat.format(record.getEndDate()));
            jsonParam.put("cover_type", record.getType());
            jsonParam.put("record_id", recordID);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest objectRequest = new JsonObjectRequest(Request.Method.POST, URL, jsonParam, response -> {
            Log.e("Records Response", response.toString());
            if (response.optString("message").equals("success")) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(getApplicationContext(), "success", Toast.LENGTH_SHORT).show();
                    }
                });
            }

        }, error -> {
            Log.e("ERROR", String.valueOf(error.networkResponse));

            NetworkResponse networkResponse = error.networkResponse;
            if (networkResponse != null && networkResponse.data != null) {
                String JSONError = new String(networkResponse.data);
                JSONObject messageJO;
                String message = "";
                try {
                    messageJO = new JSONObject(JSONError);
                    message = messageJO.optString("message");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Log.e("JSON ERROR MESSAGE", message);
                runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Failed. Please check if the email address is validated.", Toast.LENGTH_LONG).show();
                    }
                });
            }

        });
        Volley.newRequestQueue(this).add(objectRequest);

    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, InsuranceRecordDashboardActivity.class));
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK) {
            startActivity(new Intent(this, InsuranceRecordDashboardActivity.class));
            return true;    //stop calling super method
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }
}
