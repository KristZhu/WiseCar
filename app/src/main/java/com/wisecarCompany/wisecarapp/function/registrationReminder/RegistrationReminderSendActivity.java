package com.wisecarCompany.wisecarapp.function.registrationReminder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
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
import com.wisecarCompany.wisecarapp.function.serviceRecords.ServiceRecord;
import com.wisecarCompany.wisecarapp.function.serviceRecords.ServiceRecordsDashboardActivity;
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

public class RegistrationReminderSendActivity extends AppCompatActivity {

    private final static String TAG = "Reg Reminder Send";

    private String IP_HOST = "http://54.206.19.123:3000";
    private String GET_SERVICE_REFCORD_INFO = "/api/v1/servicerecords/getrecordbyid";
    private String SEND_EMAIL = "/api/v1/servicerecords/sendemail";

    private String reminderID;

    private ImageButton backImageButton;

    private TextView headerTextView;
    private TextView dateTextView;
    private TextView payRefTextView;
    private TextView expirydateTextView;
    private TextView documentLinkTextView;

    private EditText emailEditText;
    private Button sendButton;
    private String email;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration_reminder_send);


        reminderID = (String) this.getIntent().getStringExtra("reminderID");
        Log.d(TAG, "reminderID: " + reminderID);

        backImageButton = $(R.id.backImageButton);
        backImageButton.setOnClickListener(v -> startActivity(new Intent(this, RegistrationReminderDashboardActivity.class)));

        headerTextView = $(R.id.headerTextView);
        dateTextView = $(R.id.dateTextView);
        payRefTextView = $(R.id.payRefTextView);
        expirydateTextView = $(R.id.expiryDateTextView);
        documentLinkTextView = $(R.id.documentLinkTextView);

        emailEditText = $(R.id.emailEditText);
        sendButton = $(R.id.sendButton);


        // SOME INFO YOU CAN GET IN HERE
        getRegReminderInfo(new regReminderSendCallbacks() {
            @Override
            public void onSuccess(@NonNull RegistrationReminder reminder) {

                SimpleDateFormat format = new SimpleDateFormat("ddMMM yyyy", Locale.getDefault());
                headerTextView.setText("Registration Payment Ref: " + reminder.getPayRef());
                dateTextView.setText(format.format(reminder.getDate()));
                payRefTextView.setText(reminder.getPayRef());
                expirydateTextView.setText(format.format(reminder.getExpireDate()));

                documentLinkTextView.setOnClickListener(v -> {
                    Log.d(TAG, "document link url: " + reminder.getDocumentLink());
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(reminder.getDocumentLink())));
                });

                sendButton.setOnClickListener(v -> {
                    email = emailEditText.getText().toString();
                    boolean isEmail = false;
                    try{
                        String check = "^([a-z0-9A-Z]+[-|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";
                        Pattern regex = Pattern.compile(check);
                        Matcher matcher = regex.matcher(email);
                        isEmail = matcher.matches();
                    } catch(Exception e ){
                        isEmail = false;
                    }
                    if(isEmail) {

                        reminder.setEmailAddress(email);
                        sendEmail(reminder);

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
        if(v instanceof EditText) {
            int[] l = {0, 0};
            v.getLocationInWindow(l);
            int left = l[0],
                    top = l[1],
                    bottom = top + v.getHeight(),
                    right = left + v.getWidth();
            return !(event.getX()>left && event.getX()<right
                    && event.getY()>top && event.getY()<bottom);
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

    private <T extends View> T $(int id){
        return (T) findViewById(id);
    }

    private void getRegReminderInfo(@Nullable final regReminderSendCallbacks callbacks) {

        String URL = IP_HOST + GET_SERVICE_REFCORD_INFO;

        final JSONObject jsonParam = new JSONObject();
        try {
            jsonParam.put("record_id", reminderID);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest objectRequest = new JsonObjectRequest(Request.Method.POST, URL, jsonParam, response -> {
            Log.e("Records Response", response.toString());
            JSONObject jsonObject = response;
            DateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            List<String> options = new ArrayList<>();
            ServiceRecord serviceRecord;
            try {
                JSONArray jsonArray = response.getJSONArray("service_options");
                for (int i = 0; i < jsonArray.length(); i++) {
                    jsonObject = jsonArray.getJSONObject(i);
                    options.add(jsonObject.optString("service_option"));
                }

                serviceRecord = new ServiceRecord(
                        format.parse(response.optString("service_date")),
                        response.optString("service_center"),
                        response.optString("service_ref_no"),
                        options,
                        response.optString("notes"),
                        format.parse(response.optString("next_service_date")),
                        response.optDouble("next_service_odometer"),
                        response.optString("file_url")
                );

                if (callbacks != null)
                    callbacks.onSuccess(serviceRecord);
            } catch (JSONException e) {
                e.printStackTrace();
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

    public interface regReminderSendCallbacks {
        void onSuccess(@NonNull RegistrationReminder value);

//        void onError(@NonNull List<ServiceRecord> value);
    }

    private void sendEmail(RegistrationReminder reminder) {

        String URL = IP_HOST + SEND_EMAIL;
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        final JSONObject jsonParam = new JSONObject();
        try {
            jsonParam.put("service_id", record.getId());
            jsonParam.put("email_to_address", record.getEmailAddress());
            jsonParam.put("submit_date_time", format.format(new Date()));
            jsonParam.put("user_id", UserInfo.getUserID());
            jsonParam.put("service_date", dateFormat.format(record.getDate()));
            jsonParam.put("service_center", record.getCentre());
            jsonParam.put("service_options", record.getOptions());
            jsonParam.put("service_ref_no", record.getRefNo());
            jsonParam.put("notes", record.getNotes());
            jsonParam.put("next_service_date", dateFormat.format(record.getNextDate()));
            jsonParam.put("next_service_odometer", (int)record.getNextDistance());

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
        Volley.newRequestQueue(this).add(objectRequest);

    }
}
