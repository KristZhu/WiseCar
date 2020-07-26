package com.wisecarCompany.wisecarapp.function.serviceRecords;

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

public class ServiceRecordsDashboardActivity extends AppCompatActivity {

    private final static String TAG = "ServiceRecordsDashboard";

    private String IP_HOST = "http://54.206.19.123:3000";
    private String GET_SERVICE_REFCORDS = "/api/v1/servicerecords/getallrecordbyuser";
    private String GET_RECORDS_BY_REG_NO = "/api/v1/servicerecords//getrecordbyuserregisno";

    private ImageButton backImageButton;

    private LinearLayout mainDiv;
    private List<ServiceRecord> allRecords;

    private AutoCompleteTextView searchEditText;
    private ImageButton cancelImageButton;


    @SuppressLint("ResourceType")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_records_dashboard);

        backImageButton = $(R.id.backImageButton);
        backImageButton.setOnClickListener(v -> startActivity(new Intent(ServiceRecordsDashboardActivity.this, DashboardActivity.class)));

        mainDiv = $(R.id.mainDiv);

        searchEditText = $(R.id.searchEditText);
        cancelImageButton = $(R.id.cancelImageButton);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new ArrayList<>());

        // CALL GET SERVICE RECORDS METHOD
        // ATTENTION: returnServiceRecordByRegNo METHOD USES THE SAME CALLBACK, PASTE THIS IN THE onClick METHOD OF SEARCH BUTTON
        getServiceRecords(new serviceRecordsCallbacks() {
            @Override
            public void onSuccess(@NonNull List<ServiceRecord> records) {
                Log.e("Records: ", String.valueOf(records));
                allRecords = records;
                Set<String> regNos = new HashSet<>();
                mainDiv.removeAllViews();
                for(ServiceRecord record: records) {
                    regNos.add(record.getRegistrationNo());
                    showServiceRecord(record);
                }
                adapter.addAll(regNos);
                searchEditText.setAdapter(adapter);
                searchEditText.setOnItemClickListener((parent, view, position, id) -> {
                    cancelImageButton.setVisibility(View.VISIBLE);
                    String regNo = searchEditText.getText().toString();
                    returnServiceRecordByRegNo(regNo, new serviceRecordsCallbacks() {
                        @Override
                        public void onSuccess(@NonNull List<ServiceRecord> records) {
                            mainDiv.removeAllViews();
                            for(ServiceRecord record: records) showServiceRecord(record);
                        }
                    });
                });
            }
        });

        cancelImageButton.setOnClickListener(v -> {
            searchEditText.setText("");
            cancelImageButton.setVisibility(View.INVISIBLE);
            mainDiv.removeAllViews();
            for(ServiceRecord record: allRecords) showServiceRecord(record);
        });

    }


    @SuppressLint("ResourceType")
    private void showServiceRecord(ServiceRecord record) {
        ConstraintLayout lineLayout = new ConstraintLayout(this);
        ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 16, 0, 16);
        lineLayout.setLayoutParams(params);

        ConstraintSet set = new ConstraintSet();

        ImageView lightImageView = new ImageView(this);
        lightImageView.setId(0);
        if(record.getNextDate().before(new Date())) lightImageView.setImageDrawable(getResources().getDrawable(R.drawable.dashboard0light_red_line));
        else lightImageView.setImageDrawable(getResources().getDrawable(R.drawable.dashboard0light_line));
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
        if(record.getNextDate().before(new Date())) darkImageView.setImageDrawable(getResources().getDrawable(R.drawable.dashboard0dark_red_line));
        else darkImageView.setImageDrawable(getResources().getDrawable(R.drawable.dashboard0dark_line));
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) registrationNoTextView.setAutoSizeTextTypeUniformWithConfiguration(10, 30, 1, TypedValue.COMPLEX_UNIT_SP);
        registrationNoTextView.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        registrationNoTextView.setTextColor(0xff007ba4);
        registrationNoTextView.setText(record.getRegistrationNo());
        lineLayout.addView(registrationNoTextView);

        TextView dateTextView = new TextView(this);
        dateTextView.setId(11);
        set.connect(dateTextView.getId(), ConstraintSet.TOP, registrationNoTextView.getId(), ConstraintSet.BOTTOM);
        set.connect(dateTextView.getId(), ConstraintSet.BOTTOM, lightImageView.getId(), ConstraintSet.BOTTOM);
        set.connect(dateTextView.getId(), ConstraintSet.START, lightImageView.getId(), ConstraintSet.START, 32);
        set.connect(dateTextView.getId(), ConstraintSet.END, lightImageView.getId(), ConstraintSet.END);
        set.constrainPercentHeight(dateTextView.getId(), 0.2f);
        set.setVerticalBias(dateTextView.getId(), 0.0f);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) dateTextView.setAutoSizeTextTypeUniformWithConfiguration(10, 30, 1, TypedValue.COMPLEX_UNIT_SP);
        dateTextView.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        dateTextView.setTextColor(0xff000000);
        dateTextView.setText("Date: " + new SimpleDateFormat("ddMMM yyyy", Locale.getDefault()).format(record.getDate()));
        lineLayout.addView(dateTextView);

        TextView refNoTextView = new TextView(this);
        refNoTextView.setId(12);
        set.connect(refNoTextView.getId(), ConstraintSet.TOP, dateTextView.getId(), ConstraintSet.BOTTOM);
        set.connect(refNoTextView.getId(), ConstraintSet.BOTTOM, lightImageView.getId(), ConstraintSet.BOTTOM);
        set.connect(refNoTextView.getId(), ConstraintSet.START, lightImageView.getId(), ConstraintSet.START, 32);
        set.connect(refNoTextView.getId(), ConstraintSet.END, lightImageView.getId(), ConstraintSet.END);
        set.constrainPercentHeight(refNoTextView.getId(), 0.2f);
        set.setVerticalBias(refNoTextView.getId(), 0.0f);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) refNoTextView.setAutoSizeTextTypeUniformWithConfiguration(10, 30, 1, TypedValue.COMPLEX_UNIT_SP);
        refNoTextView.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        refNoTextView.setTextColor(0xff000000);
        refNoTextView.setText("Ref: " + record.getRefNo());
        lineLayout.addView(refNoTextView);

        TextView nextTextView = new TextView(this);
        nextTextView.setId(13);
        set.connect(nextTextView.getId(), ConstraintSet.TOP, darkImageView.getId(), ConstraintSet.TOP);
        set.connect(nextTextView.getId(), ConstraintSet.BOTTOM, darkImageView.getId(), ConstraintSet.BOTTOM);
        set.connect(nextTextView.getId(), ConstraintSet.START, darkImageView.getId(), ConstraintSet.START, 32);
        set.connect(nextTextView.getId(), ConstraintSet.END, darkImageView.getId(), ConstraintSet.END);
        set.constrainPercentHeight(nextTextView.getId(), 0.2f);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) nextTextView.setAutoSizeTextTypeUniformWithConfiguration(10, 30, 1, TypedValue.COMPLEX_UNIT_SP);
        nextTextView.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        nextTextView.setTextColor(0xff000000);
        nextTextView.setText("Next Service: " + new SimpleDateFormat("ddMMM yyyy", Locale.getDefault()).format(record.getNextDate()) + " / " + record.getNextDistance() + "km");
        lineLayout.addView(nextTextView);

        ImageView sentImageView = new ImageView(this);
        sentImageView.setId(20);
        set.connect(sentImageView.getId(), ConstraintSet.TOP, registrationNoTextView.getId(), ConstraintSet.TOP);
        set.connect(sentImageView.getId(), ConstraintSet.BOTTOM, refNoTextView.getId(), ConstraintSet.BOTTOM);  //do not know why... if constraint to background, there are bugs
        set.connect(sentImageView.getId(), ConstraintSet.START, lightImageView.getId(), ConstraintSet.START);
        set.connect(sentImageView.getId(), ConstraintSet.END, lightImageView.getId(), ConstraintSet.END, 32);
        set.setDimensionRatio(sentImageView.getId(), "1:1");
        set.constrainPercentWidth(sentImageView.getId(), 0.04f);
        set.setHorizontalBias(sentImageView.getId(), 1.0f);
        sentImageView.setScaleType(ImageView.ScaleType.FIT_XY);
        if(record.isSentBefore()) sentImageView.setImageDrawable(getResources().getDrawable(R.drawable.dashboard0sent));
        else sentImageView.setImageDrawable(getResources().getDrawable(R.drawable.dashboard0unsent));
        lineLayout.addView(sentImageView);

        ImageView sendImageView = new ImageView(this);
        sendImageView.setId(21);
        set.connect(sendImageView.getId(), ConstraintSet.TOP, registrationNoTextView.getId(), ConstraintSet.TOP);
        set.connect(sendImageView.getId(), ConstraintSet.BOTTOM, refNoTextView.getId(), ConstraintSet.BOTTOM);  //do not know y 2...
        set.connect(sendImageView.getId(), ConstraintSet.START, lightImageView.getId(), ConstraintSet.START);
        set.connect(sendImageView.getId(), ConstraintSet.END, sentImageView.getId(), ConstraintSet.START, 32);
        set.setDimensionRatio(sendImageView.getId(), "1:1");
        set.constrainPercentWidth(sendImageView.getId(), 0.1f);
        set.setHorizontalBias(sendImageView.getId(), 1.0f);
        sendImageView.setScaleType(ImageView.ScaleType.FIT_XY);
        sendImageView.setImageDrawable(getResources().getDrawable(R.drawable.dashboard0send));
        sendImageView.setOnClickListener(v -> {
            Log.d(TAG, "send servicerecord registrationNO: " + record.getRegistrationNo());
            Log.d(TAG, "send servicerecord ID: " + record.getId());
            startActivity(new Intent(this, ServiceRecordsSendActivity.class).putExtra("recordID", record.getId()));
        });
        lineLayout.addView(sendImageView);

        if(record.getNextDate().before(new Date())) {
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

    private void getServiceRecords(@Nullable final serviceRecordsCallbacks callbacks) {

        String URL = IP_HOST + GET_SERVICE_REFCORDS;

        final JSONObject jsonParam = new JSONObject();
        try {
            jsonParam.put("user_id", UserInfo.getUserID());

        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest objectRequest = new JsonObjectRequest(Request.Method.POST, URL, jsonParam, response -> {
            Log.e("Records Response", response.toString());
            JSONObject jsonObject;
            DateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            List<ServiceRecord> records = new ArrayList();

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
        Volley.newRequestQueue(ServiceRecordsDashboardActivity.this).add(objectRequest);

    }

    public interface serviceRecordsCallbacks {
        void onSuccess(@NonNull List<ServiceRecord> value);

//        void onError(@NonNull List<ServiceRecord> value);
    }



    private void returnServiceRecordByRegNo(String regNo, @Nullable final serviceRecordsCallbacks callbacks) {

        String URL = IP_HOST + GET_RECORDS_BY_REG_NO;

        List<ServiceRecord> records = new ArrayList();

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

        Volley.newRequestQueue(ServiceRecordsDashboardActivity.this).add(objectRequest);
    }

}
