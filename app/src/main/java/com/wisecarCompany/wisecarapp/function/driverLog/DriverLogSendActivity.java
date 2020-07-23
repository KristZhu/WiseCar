package com.wisecarCompany.wisecarapp.function.driverLog;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
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
import com.wisecarCompany.wisecarapp.user.UserInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DriverLogSendActivity extends AppCompatActivity {


    private final static String TAG = "Service Records Send";

    private String logID;

    private ImageButton backImageButton;

    private TextView headerTextView;
    private TextView dateTextView;
    private TextView startTextView;
    private TextView endTextView;
    private TextView timeTextView;
    private TextView distanceTextView;
    private TextView shareTextView;

    private EditText emailEditText;
    private Button sendButton;
    private String email;

    private String IP_HOST = "http://54.206.19.123:3000";
    private String GET_DRIVER_LOG_INFO = "/api/v1/drivelog/getrecordbyid";
    private String SEND_EMAIL = "/api/v1/drivelog/sendemail";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_log_send);

        logID = (String) this.getIntent().getStringExtra("logID");
        Log.d(TAG, "logID: " + logID);

        backImageButton = $(R.id.backImageButton);
        backImageButton.setOnClickListener(v -> startActivity(new Intent(this, DriverLogDashboardActivity.class)));

        headerTextView = $(R.id.headerTextView);
        dateTextView = $(R.id.dateTextView);
        startTextView = $(R.id.startTextView);
        endTextView = $(R.id.endTextView);
        timeTextView = $(R.id.timeTextView);
        distanceTextView = $(R.id.distanceTextView);
        shareTextView = $(R.id.shareTextView);

        emailEditText = $(R.id.emailEditText);
        sendButton = $(R.id.sendButton);


        // SOME INFO YOU CAN GET IN HERE
        getDriverLogInfo(new driverLogSendCallbacks() {
            @Override
            public void onSuccess(@NonNull DriverLog log) {

                SimpleDateFormat dateFormat = new SimpleDateFormat("ddMMM yyyy", Locale.getDefault());
                SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
                headerTextView.setText(log.getRegistrationNo() + ", Log: " + timeFormat.format(log.getStartTime()) + "-" + timeFormat.format(log.getEndTime()));
                dateTextView.setText(dateFormat.format(log.getStartTime()));
                startTextView.setText(timeFormat.format(log.getStartTime()));
                endTextView.setText(timeFormat.format(log.getEndTime()));
                timeTextView.setText(""+log.getMins());
                distanceTextView.setText("" + (int) (log.getKm() * 10) / 10.0);
                if (log.getCompanyName() == null || log.getCompanyName().length() == 0)
                    shareTextView.setText("Not shared");
                else shareTextView.setText(log.getCompanyName());

                sendButton.setOnClickListener(v -> {
                    email = emailEditText.getText().toString();
                    boolean isEmail = false;
                    try {
                        String check = "^([a-z0-9A-Z]+[-|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";
                        Pattern regex = Pattern.compile(check);
                        Matcher matcher = regex.matcher(email);
                        isEmail = matcher.matches();
                    } catch (Exception e) {
                        isEmail = false;
                    }
                    if (isEmail) {

                        log.setEmailAddress(email);
                        sendEmail(log);

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

    private void getDriverLogInfo(@Nullable final driverLogSendCallbacks callbacks) {

        String URL = IP_HOST + GET_DRIVER_LOG_INFO;

        final JSONObject jsonParam = new JSONObject();
        try {
            jsonParam.put("record_id", logID);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest objectRequest = new JsonObjectRequest(Request.Method.POST, URL, jsonParam, response -> {
            Log.e("Records Response", response.toString());
            JSONObject jsonObject = response;
            DateFormat format = new SimpleDateFormat("dd-MMMM-yyyy HH:mm:ss", Locale.getDefault());
            DriverLog driverLog;
            try {

                driverLog = new DriverLog(
                        jsonObject.optString("registration_no"),
                        format.parse(jsonObject.optString("date") + " " + jsonObject.optString("start_time")),
                        format.parse(jsonObject.optString("date") + " " + jsonObject.optString("end_time")),
                        jsonObject.optDouble("km_travel"),
                        jsonObject.optInt("total_time"),
                        jsonObject.optString("shared_with")
                );

                if (callbacks != null)
                    callbacks.onSuccess(driverLog);
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

    public interface driverLogSendCallbacks {
        void onSuccess(@NonNull DriverLog value);

//        void onError(@NonNull DriverLog value);
    }

    private void sendEmail(DriverLog log) {

        String URL = IP_HOST + SEND_EMAIL;
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        DateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());

        final JSONObject jsonParam = new JSONObject();
        try {
            jsonParam.put("service_id", log.getId());
            jsonParam.put("email_to_address", log.getEmailAddress());
            jsonParam.put("submit_date_time", format.format(new Date()));
            jsonParam.put("user_id", UserInfo.getUserID());
            jsonParam.put("registration_no", log.getRegistrationNo());
            jsonParam.put("date", dateFormat.format(log.getStartTime()));
            jsonParam.put("start_time", timeFormat.format(log.getStartTime()));
            jsonParam.put("end_time", timeFormat.format(log.getEndTime()));
            jsonParam.put("total_km", log.getKm());
            jsonParam.put("total_time", log.getMins());
            jsonParam.put("shared_with", log.getCompanyName());

        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest objectRequest = new JsonObjectRequest(Request.Method.POST, URL, jsonParam, response -> {
            Log.e("Records Response", response.toString());
            if(response.optString("message").equals("success")){
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
            }

        });
        Volley.newRequestQueue(DriverLogSendActivity.this).add(objectRequest);

    }
}
