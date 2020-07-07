package com.example.wisecarapp;

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
import android.text.Html;
import android.text.Layout;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class RecordLogActivity extends AppCompatActivity {

    private final static String TAG = "RecordLogActivity";

    private final String IP_HOST = "http://54.206.19.123:3000";
    private final String GET_SHARED_LIST = "/api/v1/sharevehicle/sharedcompanylist/";

    private String vehicleID;
    private Vehicle vehicle;

    private Set<Share> shares;  //从数据库返回
    private Share currShare;
    private Set<RecordLog> logs;    //从数据库返回

    private ImageButton backImageButton;

    private AutoCompleteTextView searchEditText;

    private TextView companyTextView;

    private ImageButton startImageButton;
    private ImageButton pauseResumeImageButton;
    private ImageButton endImageButton;
    private TextView timeDistanceTextView;

    private LinearLayout logsDiv;


    @RequiresApi(api = Build.VERSION_CODES.O)
    @SuppressLint({"SetTextI18n", "Assert", "ResourceType"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_log);

        returnSharedList(vehicleID, new sharedCallbacks() {
            @Override
            public void onSuccess(@NonNull Map<String, Share> shares) {
                Log.e("map", String.valueOf(shares.size()));

            }

            @Override
            public void onError(@NonNull String errorMessage) {

            }
        });

        logs = new HashSet<>();
        logs.add(new RecordLog());
        logs.add(new RecordLog());
        logs.add(new RecordLog());

        vehicleID = (String) this.getIntent().getStringExtra("vehicleID");
        //vehicleID = "303";
        Log.d(TAG, "vehicleID: " + vehicleID);
        vehicle = UserInfo.getVehicles().get(vehicleID);
        Log.d(TAG, "vehicle: " + vehicle);

        backImageButton = $(R.id.backImageButton);
        backImageButton.setOnClickListener(v -> startActivity(new Intent(RecordLogActivity.this, VehicleActivity.class)));

        searchEditText = $(R.id.searchEditText);
        //......

        companyTextView = $(R.id.companyTextView);
        if(shares!=null) {
            for(Share share: shares) {
                if(share.isShare()) {
                    SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
                    if(share.isRecurring()) {
                        Calendar c = Calendar.getInstance();
                        c.setTime(new Date());
                        if(share.getDate().before(new Date())
                                && new Date((share.getDate().getTime() + 24*60*60*1000-1)).after(new Date())
                                && share.getRecurring_days()[c.get(Calendar.DAY_OF_WEEK)-1]) {
                            currShare = share;
                            break;
                        }
                    } else {
                        if(fmt.format(share.getDate()).equals(fmt.format(new Date()))) {
                            currShare = share;
                            break;
                        }
                    }
                }
            }
        }
        if(currShare==null) {
            companyTextView.setTextColor(0xffdb0a00);
            companyTextView.setText("Not shared with any companies");
        } else {
            companyTextView.setTextColor(0xff6f0a00);
            companyTextView.setText("Currently shared with " + currShare.getCompany_name());
        }

        startImageButton = $(R.id.startImageButton);
        pauseResumeImageButton = $(R.id.pauseResumeImageButton);
        endImageButton = $(R.id.endImageButton);
        timeDistanceTextView = $(R.id.timeDistanceTextView);

        if(UserInfo.getCurrLog()==null || UserInfo.getCurrLog().getRecording()==0) {
            ending();
        } else if(UserInfo.getCurrLog().getRecording()==1) {
            recording();
        } else if(UserInfo.getCurrLog().getRecording()==2) {
            pausing();
        } else {
            Log.d(TAG, "recording state error");
            assert false;
        }

        startImageButton.setOnClickListener(v -> {
            if(UserInfo.getCurrLog()==null) {
                Calendar c = Calendar.getInstance();
                c.setTime(new Date());
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd", Locale.getDefault());
                SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
                try {
                    Date date = dateFormat.parse(dateFormat.format(new Date()));
                    Date startTime = timeFormat.parse(timeFormat.format(new Date()));
                    UserInfo.setCurrLog(new RecordLog(date, startTime));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
            UserInfo.getCurrLog().setRecording(1);
            recording();
        });

        pauseResumeImageButton.setOnClickListener(v -> {
            if(UserInfo.getCurrLog()==null || UserInfo.getCurrLog().getRecording()==0) {
                Toast.makeText(getApplicationContext(), "Please start a record first", Toast.LENGTH_LONG).show();
                return;
            }
            if(UserInfo.getCurrLog().getRecording()==1) {    //pause
                UserInfo.getCurrLog().setRecording(2);
                pausing();
            } else if(UserInfo.getCurrLog().getRecording()==2) { //resume
                UserInfo.getCurrLog().setRecording(1);
                recording();
            }
        });

        endImageButton.setOnClickListener(v -> {
            if(UserInfo.getCurrLog()==null || UserInfo.getCurrLog().getRecording()==0) {
                Toast.makeText(getApplicationContext(), "Please start a record first", Toast.LENGTH_LONG).show();
                return;
            }
            UserInfo.getCurrLog().setRecording(0);
            ending();
            finishRecord();

        });

        logsDiv = $(R.id.logsDiv);
        for(RecordLog log: logs) {
            Log.d(TAG, "log: " + log);

            ConstraintLayout logLineLayout = new ConstraintLayout(RecordLogActivity.this);
            ConstraintSet set = new ConstraintSet();

            ImageView bgImageView = new ImageView(RecordLogActivity.this);
            bgImageView.setId(0);
            bgImageView.setBackground(getResources().getDrawable(R.drawable.record_log0line));
            set.connect(bgImageView.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, 16);
            set.connect(bgImageView.getId(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START);
            set.connect(bgImageView.getId(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END);
            set.setDimensionRatio(bgImageView.getId(), "4.3:1");
            logLineLayout.addView(bgImageView);

            ImageView dateImageView = new ImageView(RecordLogActivity.this);
            dateImageView.setId(1);
            dateImageView.setImageDrawable(getResources().getDrawable(R.drawable.record_log0date));
            set.connect(dateImageView.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP);
            set.connect(dateImageView.getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM);
            set.connect(dateImageView.getId(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START, 32);
            set.connect(dateImageView.getId(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END);
            set.constrainPercentWidth(dateImageView.getId(), 0.18f);
            set.setDimensionRatio(dateImageView.getId(), "1:1");
            set.setHorizontalBias(dateImageView.getId(), 0.0f);
            logLineLayout.addView(dateImageView);

            TextView dateTextView = new TextView(RecordLogActivity.this);
            dateTextView.setId(2);
            dateTextView.setText(new SimpleDateFormat("dd MMM", Locale.getDefault()).format(log.getDate()));
            set.connect(dateTextView.getId(), ConstraintSet.TOP, dateImageView.getId(), ConstraintSet.TOP);
            set.connect(dateTextView.getId(), ConstraintSet.BOTTOM, dateImageView.getId(), ConstraintSet.BOTTOM);
            set.connect(dateTextView.getId(), ConstraintSet.START, dateImageView.getId(), ConstraintSet.START);
            set.connect(dateTextView.getId(), ConstraintSet.END, dateImageView.getId(), ConstraintSet.END);
            set.constrainPercentHeight(dateTextView.getId(), 0.3f);
            dateTextView.setAutoSizeTextTypeUniformWithConfiguration(10, 30, 1, TypedValue.COMPLEX_UNIT_SP);
            dateTextView.setTextColor(0xffffffff);
            dateTextView.setGravity(Gravity.CENTER);
            logLineLayout.addView(dateTextView);

            TextView timeDistanceTextView = new TextView(RecordLogActivity.this);
            timeDistanceTextView.setId(3);
            timeDistanceTextView.setText(log.getMins() + "Mins, " + log.getKm() + "KM");
            set.connect(timeDistanceTextView.getId(), ConstraintSet.TOP, dateImageView.getId(), ConstraintSet.TOP);
            set.connect(timeDistanceTextView.getId(), ConstraintSet.BOTTOM, dateImageView.getId(), ConstraintSet.BOTTOM);
            set.connect(timeDistanceTextView.getId(), ConstraintSet.START, dateImageView.getId(), ConstraintSet.END, 16);
            set.connect(timeDistanceTextView.getId(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END);
            set.constrainPercentHeight(timeDistanceTextView.getId(), 0.28f);
            set.setVerticalBias(timeDistanceTextView.getId(), 0.0f);
            timeDistanceTextView.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
            timeDistanceTextView.setAutoSizeTextTypeUniformWithConfiguration(10, 30, 1, TypedValue.COMPLEX_UNIT_SP);
            timeDistanceTextView.setTextColor(0xff000000);
            logLineLayout.addView(timeDistanceTextView);

            TextView logInfoTextView = new TextView(RecordLogActivity.this);
            logInfoTextView.setId(4);
            StringBuilder sb = new StringBuilder();
            SimpleDateFormat fmt = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
            sb.append("Start Time: ").append(fmt.format(log.getStartTime())).append("<br/>");
            sb.append("End Time: ").append(fmt.format(log.getEndTime())).append("<br/>");
            sb.append("Paused: ").append(log.getCountPause());
            logInfoTextView.setText(Html.fromHtml(sb.toString()));
            set.connect(logInfoTextView.getId(), ConstraintSet.TOP, timeDistanceTextView.getId(), ConstraintSet.BOTTOM);
            set.connect(logInfoTextView.getId(), ConstraintSet.BOTTOM, dateImageView.getId(), ConstraintSet.BOTTOM);
            set.connect(logInfoTextView.getId(), ConstraintSet.START, dateImageView.getId(), ConstraintSet.END, 32);
            set.connect(logInfoTextView.getId(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END);
            set.setVerticalBias(logInfoTextView.getId(), 0.0f);
            logInfoTextView.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
            logInfoTextView.setTextColor(0xff47b5be);
            logInfoTextView.setAutoSizeTextTypeUniformWithConfiguration(10, 30, 1, TypedValue.COMPLEX_UNIT_SP);
            logLineLayout.addView(logInfoTextView);

            ImageView companyLogoImageView = new ImageView(RecordLogActivity.this);
            companyLogoImageView.setId(5);
            if(log.getCustID()!=null) companyLogoImageView.setImageBitmap(log.getCompanyLogo());
            set.connect(companyLogoImageView.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP);
            set.connect(companyLogoImageView.getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM);
            set.connect(companyLogoImageView.getId(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START);
            set.connect(companyLogoImageView.getId(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END, 16);
            set.constrainPercentWidth(companyLogoImageView.getId(), 0.18f);
            set.setDimensionRatio(companyLogoImageView.getId(), "1:1");
            set.setHorizontalBias(companyLogoImageView.getId(), 1.0f);
            logLineLayout.addView(companyLogoImageView);

            set.applyTo(logLineLayout);
            logsDiv.addView(logLineLayout);

        }

    }

    private void recording() {   //1
        Log.d(TAG, "recording: ");
        pauseResumeImageButton.setImageDrawable(getResources().getDrawable(R.drawable.record_log0pause));
        timeDistanceTextView.setTextColor(0xff007ba4);

    }

    private void pausing() {    //2
        Log.d(TAG, "pausing: ");
        pauseResumeImageButton.setImageDrawable(getResources().getDrawable(R.drawable.record_log0resume));
        timeDistanceTextView.setTextColor(0xffa5a6a3);

    }

    private void ending() { //0
        Log.d(TAG, "ending: ");
        timeDistanceTextView.setText("");
        pauseResumeImageButton.setImageDrawable(getResources().getDrawable(R.drawable.record_log0pause));

    }

    private void finishRecord() {   //write to log
        Log.d(TAG, "finishRecord: ");
        
    }

    private void returnSharedList(String vehicleID, @Nullable final sharedCallbacks callbacks) {

        String URL = IP_HOST + GET_SHARED_LIST + vehicleID;

        JsonObjectRequest objectRequest = new JsonObjectRequest(Request.Method.GET, URL, null, response -> {
            Log.e("Response", response.toString());
            JSONArray jsonArray;
            JSONObject jsonObject;

            Map<String, Share> shares = new HashMap<>();  //key: id
            try {
                jsonArray = response.getJSONArray("result");

                for (int i = 0; i < jsonArray.length(); i++) {
                    jsonObject = jsonArray.getJSONObject(i);

                    Share share = new Share();

                    try {
                        share.setShare_id(jsonObject.optString("share_id"));
                        String recurring_flag = jsonObject.optString("recurring_flag");
                        if (recurring_flag.equals("1")) {
                            share.setRecurring(true);
                            share.setRecurring_end_date(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(jsonObject.optString("recurring_end_date")));
                            String recurringDaysStr = jsonObject.optString("recurring_days");
                            boolean[] recurringDays = new boolean[] {false, false, false, false, false, false, false};
                            for (char c : recurringDaysStr.toCharArray()) recurringDays[c-'0'] = true;
                            share.setRecurring_days(recurringDays);
                        } else {
                            share.setRecurring(false);
                            share.setDate(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(jsonObject.optString("date")));
                        }
                        share.setCust_id(jsonObject.optString("cust_id"));
                        share.setCompany_name(jsonObject.optString("company_name"));
                        share.setStart_time(new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).parse(jsonObject.optString("start_time")));
                        share.setEnd_time(new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).parse(jsonObject.optString("end_time")));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    shares.put(share.getShare_id(), share);

                }
                if (callbacks != null)
                    callbacks.onSuccess(shares);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, error -> {

//                Log.e("ERROR!!!", error.toString());
//                Log.e("ERROR!!!", String.valueOf(error.networkResponse));

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
                if (callbacks != null)
                    callbacks.onError(message);
            }

        });

        Volley.newRequestQueue(RecordLogActivity.this).add(objectRequest);
    }
    public interface sharedCallbacks {
        void onSuccess(@NonNull Map<String, Share> value);

        void onError(@NonNull String errorMessage);
    }

    public boolean dispatchTouchEvent(MotionEvent ev) {
        View v = getCurrentFocus();
        if (isShouldHideInput(v, ev)) {
            hideSoftInput(v.getWindowToken());
        }
        return super.dispatchTouchEvent(ev);
    }

    private boolean isShouldHideInput(View v, MotionEvent event) {
        if (v != null && (v instanceof EditText)) {
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
            manager.hideSoftInputFromWindow(token, InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    private <T extends View> T $(int id){
        return (T) findViewById(id);
    }
}
