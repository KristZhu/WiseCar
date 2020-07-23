package com.wisecarCompany.wisecarapp.function.registrationReminder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.wisecarCompany.wisecarapp.R;
import com.wisecarCompany.wisecarapp.function.serviceRecords.ServiceRecord;
import com.wisecarCompany.wisecarapp.function.serviceRecords.ServiceRecordsDashboardActivity;
import com.wisecarCompany.wisecarapp.function.serviceRecords.ServiceRecordsSendActivity;
import com.wisecarCompany.wisecarapp.user.UserInfo;
import com.wisecarCompany.wisecarapp.user.vehicle.DashboardActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class RegistrationReminderDashboardActivity extends AppCompatActivity {

    private final static String TAG = "RegReminderDashboard";

    private String IP_HOST = "http://54.206.19.123:3000";
    private String GET_SERVICE_REFCORDS = "/api/v1/servicerecords/getallrecordbyuser";
    private String GET_RECORDS_BY_REG_NO = "/api/v1/servicerecords//getrecordbyuserregisno";

    private ImageButton backImageButton;

    private LinearLayout mainDiv;
    private List<RegistrationReminder> allReminders;

    private AutoCompleteTextView searchEditText;
    private ImageButton cancelImageButton;


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration_reminder_dashboard);

        backImageButton = $(R.id.backImageButton);
        backImageButton.setOnClickListener(v -> startActivity(new Intent(this, DashboardActivity.class)));

        mainDiv = $(R.id.mainDiv);

        searchEditText = $(R.id.searchEditText);
        cancelImageButton = $(R.id.cancelImageButton);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new ArrayList<>());

        getRegReminder(new regReminderCallbacks() {
            @Override
            public void onSuccess(@NonNull List<RegistrationReminder> reminders) {
                Log.e("Reminders: ", String.valueOf(reminders));
                allReminders = reminders;
                Set<String> regNos = new HashSet<>();
                for(RegistrationReminder reminder: reminders) {
                    regNos.add(reminder.getRegistrationNo());
                    showRegReminder(reminder);
                }
                adapter.addAll(regNos);
                searchEditText.setAdapter(adapter);
                searchEditText.setOnItemClickListener((parent, view, position, id) -> {
                    cancelImageButton.setVisibility(View.VISIBLE);
                    String regNo = searchEditText.getText().toString();
                    returnRegReminderByRegNo(regNo, new regReminderCallbacks() {
                        @Override
                        public void onSuccess(@NonNull List<RegistrationReminder> reminders) {
                            mainDiv.removeAllViews();
                            for(RegistrationReminder reminder: reminders) showRegReminder(reminder);
                        }
                    });
                });
            }
        });

        cancelImageButton.setOnClickListener(v -> {
            searchEditText.setText("");
            cancelImageButton.setVisibility(View.INVISIBLE);
            mainDiv.removeAllViews();
            for(RegistrationReminder reminder: allReminders) showRegReminder(reminder);
        });

    }


    @SuppressLint("ResourceType")
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void showRegReminder(RegistrationReminder reminder) {
        ConstraintLayout lineLayout = new ConstraintLayout(this);
        ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 16, 0, 16);
        lineLayout.setLayoutParams(params);

        ConstraintSet set = new ConstraintSet();

        ImageView lightImageView = new ImageView(this);
        lightImageView.setId(0);
        if(reminder.getExpireDate().before(new Date())) lightImageView.setImageDrawable(getResources().getDrawable(R.drawable.dashboard0light_line));
        else lightImageView.setImageDrawable(getResources().getDrawable(R.drawable.dashboard0light_red_line));
        set.connect(lightImageView.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP);
        set.connect(lightImageView.getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM);
        set.connect(lightImageView.getId(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START);
        set.connect(lightImageView.getId(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END);
        set.setDimensionRatio(lightImageView.getId(), "4.5:1");
        set.constrainPercentHeight(lightImageView.getId(), (float)(2.0/3));
        set.setVerticalBias(lightImageView.getId(), 0.0f);
        lightImageView.setScaleType(ImageView.ScaleType.FIT_XY);
        lineLayout.addView(lightImageView);

        ImageView darkImageView = new ImageView(this);
        darkImageView.setId(1);
        darkImageView.setScaleType(ImageView.ScaleType.FIT_XY);
        if(reminder.getExpireDate().before(new Date())) darkImageView.setImageDrawable(getResources().getDrawable(R.drawable.dashboard0dark_line));
        else darkImageView.setImageDrawable(getResources().getDrawable(R.drawable.dashboard0dark_red_line));
        set.connect(darkImageView.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP);
        set.connect(darkImageView.getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM);
        set.connect(darkImageView.getId(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START);
        set.connect(darkImageView.getId(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END);
        set.setDimensionRatio(darkImageView.getId(), "9:1");
        set.constrainPercentHeight(darkImageView.getId(), (float)(1.0/3));
        set.setVerticalBias(darkImageView.getId(), 1.0f);
        darkImageView.setScaleType(ImageView.ScaleType.FIT_XY);
        lineLayout.addView(darkImageView);

        TextView registrationNoTextView = new TextView(this);
        registrationNoTextView.setId(10);
        set.connect(registrationNoTextView.getId(), ConstraintSet.TOP, lightImageView.getId(), ConstraintSet.TOP);
        set.connect(registrationNoTextView.getId(), ConstraintSet.BOTTOM, lightImageView.getId(), ConstraintSet.BOTTOM);
        set.connect(registrationNoTextView.getId(), ConstraintSet.START, lightImageView.getId(), ConstraintSet.START, 32);
        set.connect(registrationNoTextView.getId(), ConstraintSet.END, lightImageView.getId(), ConstraintSet.END);
        set.constrainPercentHeight(registrationNoTextView.getId(), 0.25f);
        set.setVerticalBias(registrationNoTextView.getId(), 0.0f);
        registrationNoTextView.setAutoSizeTextTypeUniformWithConfiguration(10, 30, 1, TypedValue.COMPLEX_UNIT_SP);
        registrationNoTextView.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        registrationNoTextView.setTextColor(0xff007ba4);
        registrationNoTextView.setText(reminder.getRegistrationNo());
        lineLayout.addView(registrationNoTextView);

        TextView dateTextView = new TextView(this);
        dateTextView.setId(11);
        set.connect(dateTextView.getId(), ConstraintSet.TOP, registrationNoTextView.getId(), ConstraintSet.BOTTOM);
        set.connect(dateTextView.getId(), ConstraintSet.BOTTOM, lightImageView.getId(), ConstraintSet.BOTTOM);
        set.connect(dateTextView.getId(), ConstraintSet.START, lightImageView.getId(), ConstraintSet.START, 32);
        set.connect(dateTextView.getId(), ConstraintSet.END, lightImageView.getId(), ConstraintSet.END);
        set.constrainPercentHeight(dateTextView.getId(), 0.2f);
        set.setVerticalBias(dateTextView.getId(), 0.0f);
        dateTextView.setAutoSizeTextTypeUniformWithConfiguration(10, 30, 1, TypedValue.COMPLEX_UNIT_SP);
        dateTextView.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        dateTextView.setTextColor(0xff000000);
        dateTextView.setText("Date: " + new SimpleDateFormat("ddMMM yyyy", Locale.getDefault()).format(reminder.getDate()));
        lineLayout.addView(dateTextView);

        TextView payRefTextView = new TextView(this);
        payRefTextView.setId(12);
        set.connect(payRefTextView.getId(), ConstraintSet.TOP, dateTextView.getId(), ConstraintSet.BOTTOM);
        set.connect(payRefTextView.getId(), ConstraintSet.BOTTOM, lightImageView.getId(), ConstraintSet.BOTTOM);
        set.connect(payRefTextView.getId(), ConstraintSet.START, lightImageView.getId(), ConstraintSet.START, 32);
        set.connect(payRefTextView.getId(), ConstraintSet.END, lightImageView.getId(), ConstraintSet.END);
        set.constrainPercentHeight(payRefTextView.getId(), 0.2f);
        set.setVerticalBias(payRefTextView.getId(), 0.0f);
        payRefTextView.setAutoSizeTextTypeUniformWithConfiguration(10, 30, 1, TypedValue.COMPLEX_UNIT_SP);
        payRefTextView.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        payRefTextView.setTextColor(0xff000000);
        payRefTextView.setText("Payment Ref: " + reminder.getPayRef());
        lineLayout.addView(payRefTextView);

        TextView expireDateTextView = new TextView(this);
        expireDateTextView.setId(13);
        set.connect(expireDateTextView.getId(), ConstraintSet.TOP, darkImageView.getId(), ConstraintSet.TOP);
        set.connect(expireDateTextView.getId(), ConstraintSet.BOTTOM, darkImageView.getId(), ConstraintSet.BOTTOM);
        set.connect(expireDateTextView.getId(), ConstraintSet.START, darkImageView.getId(), ConstraintSet.START, 32);
        set.connect(expireDateTextView.getId(), ConstraintSet.END, darkImageView.getId(), ConstraintSet.END);
        set.constrainPercentHeight(expireDateTextView.getId(), 0.2f);
        expireDateTextView.setAutoSizeTextTypeUniformWithConfiguration(10, 30, 1, TypedValue.COMPLEX_UNIT_SP);
        expireDateTextView.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        expireDateTextView.setTextColor(0xff000000);
        expireDateTextView.setText("Expiry Date: " + new SimpleDateFormat("ddMMM yyyy", Locale.getDefault()).format(reminder.getExpireDate()));
        lineLayout.addView(expireDateTextView);

        ImageView sentImageView = new ImageView(this);
        sentImageView.setId(20);
        set.connect(sentImageView.getId(), ConstraintSet.TOP, registrationNoTextView.getId(), ConstraintSet.TOP);
        set.connect(sentImageView.getId(), ConstraintSet.BOTTOM, payRefTextView.getId(), ConstraintSet.BOTTOM);  //do not know why... if constraint to background, there are bugs
        set.connect(sentImageView.getId(), ConstraintSet.START, lightImageView.getId(), ConstraintSet.START);
        set.connect(sentImageView.getId(), ConstraintSet.END, lightImageView.getId(), ConstraintSet.END, 32);
        set.setDimensionRatio(sentImageView.getId(), "1:1");
        set.constrainPercentWidth(sentImageView.getId(), 0.04f);
        set.setHorizontalBias(sentImageView.getId(), 1.0f);
        sentImageView.setScaleType(ImageView.ScaleType.FIT_XY);
        if(reminder.isSentBefore()) sentImageView.setImageDrawable(getResources().getDrawable(R.drawable.dashboard0sent));
        else sentImageView.setImageDrawable(getResources().getDrawable(R.drawable.dashboard0unsent));
        lineLayout.addView(sentImageView);

        ImageView sendImageView = new ImageView(this);
        sendImageView.setId(21);
        set.connect(sendImageView.getId(), ConstraintSet.TOP, registrationNoTextView.getId(), ConstraintSet.TOP);
        set.connect(sendImageView.getId(), ConstraintSet.BOTTOM, payRefTextView.getId(), ConstraintSet.BOTTOM);  //do not know y 2...
        set.connect(sendImageView.getId(), ConstraintSet.START, lightImageView.getId(), ConstraintSet.START);
        set.connect(sendImageView.getId(), ConstraintSet.END, sentImageView.getId(), ConstraintSet.START, 32);
        set.setDimensionRatio(sendImageView.getId(), "1:1");
        set.constrainPercentWidth(sendImageView.getId(), 0.1f);
        set.setHorizontalBias(sendImageView.getId(), 1.0f);
        sendImageView.setScaleType(ImageView.ScaleType.FIT_XY);
        sendImageView.setImageDrawable(getResources().getDrawable(R.drawable.dashboard0send));
        sendImageView.setOnClickListener(v -> {
            Log.d(TAG, "send reminder ID: " + reminder.getId());
            startActivity(new Intent(this, RegistrationReminderSendActivity.class).putExtra("reminderID", reminder.getId()));
        });
        lineLayout.addView(sendImageView);

        if(! reminder.getExpireDate().before(new Date())) {
            ImageView notifyImageView = new ImageView(this);
            notifyImageView.setId(22);
            set.connect(notifyImageView.getId(), ConstraintSet.TOP, darkImageView.getId(), ConstraintSet.TOP);
            set.connect(notifyImageView.getId(), ConstraintSet.BOTTOM, darkImageView.getId(), ConstraintSet.BOTTOM);  //do not know y 2...
            set.connect(notifyImageView.getId(), ConstraintSet.START, darkImageView.getId(), ConstraintSet.START);
            set.connect(notifyImageView.getId(), ConstraintSet.END, darkImageView.getId(), ConstraintSet.END, 16);
            set.setDimensionRatio(notifyImageView.getId(), "1:1");
            set.constrainPercentWidth(notifyImageView.getId(), 0.1f);
            set.setHorizontalBias(notifyImageView.getId(), 1.0f);
            notifyImageView.setScaleType(ImageView.ScaleType.FIT_XY);
            notifyImageView.setImageDrawable(getResources().getDrawable(R.drawable.dashboard0notification));
            lineLayout.addView(notifyImageView);
        }

        set.applyTo(lineLayout);
        mainDiv.addView(lineLayout);
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

    private void getRegReminder(@Nullable final regReminderCallbacks callbacks) {

        String URL = IP_HOST + GET_SERVICE_REFCORDS;

        final JSONObject jsonParam = new JSONObject();
        try {
            jsonParam.put("user_id", "216");
//            jsonParam.put("user_id", UserInfo.getUserID());

        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest objectRequest = new JsonObjectRequest(Request.Method.POST, URL, jsonParam, response -> {
            Log.e("Response", response.toString());
            JSONObject jsonObject;
            DateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            List<RegistrationReminder> reminders = new ArrayList();

            try {
                JSONArray jsonArray = response.getJSONArray("record_list");

                for (int i = 0; i < jsonArray.length(); i++) {
                    jsonObject = jsonArray.getJSONObject(i);
                    ServiceRecord record;

                    record = new ServiceRecord(
                            jsonObject.optString("id"),
                            jsonObject.optString("registration_no"),
                            format.parse(jsonObject.optString("service_date")),
                            jsonObject.optString("service_ref"),
                            format.parse(jsonObject.optString("next_service_date")),
                            jsonObject.optDouble("next_service_odometer"),
                            jsonObject.optString("has_sent_before").equals("1")
                    );
                    records.add(record);
                }
                if (callbacks != null)
                    callbacks.onSuccess(records);
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

    public interface regReminderCallbacks {
        void onSuccess(@NonNull List<RegistrationReminder> value);

//        void onError(@NonNull List<ServiceRecord> value);
    }



    private void returnRegReminderByRegNo(String regNo, @Nullable final regReminderCallbacks callbacks) {

        String URL = IP_HOST + GET_RECORDS_BY_REG_NO;

        List<RegistrationReminder> reminders = new ArrayList();

        final JSONObject jsonParam = new JSONObject();
        try {
            jsonParam.put("user_id", UserInfo.getUserID());
            jsonParam.put("registration_no", regNo);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest objectRequest = new JsonObjectRequest(Request.Method.POST, URL, jsonParam, response -> {
            Log.e("Records Response", response.toString());
            JSONObject jsonObject;
            DateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

            try {
                JSONArray jsonArray = response.getJSONArray("record_list");

                for (int i = 0; i < jsonArray.length(); i++) {
                    jsonObject = jsonArray.getJSONObject(i);
                    ServiceRecord record;

                    record = new ServiceRecord(
                            jsonObject.optString("id"),
                            jsonObject.optString("registration_no"),
                            format.parse(jsonObject.optString("service_date")),
                            jsonObject.optString("service_ref"),
                            format.parse(jsonObject.optString("next_service_date")),
                            jsonObject.optDouble("next_service_odometer"),
                            jsonObject.optString("has_sent_before").equals("1")
                    );

                    records.add(record);
                }
                if (callbacks != null)
                    callbacks.onSuccess(records);
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }, error -> {

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
            }
        });

        Volley.newRequestQueue(this).add(objectRequest);
    }

}
