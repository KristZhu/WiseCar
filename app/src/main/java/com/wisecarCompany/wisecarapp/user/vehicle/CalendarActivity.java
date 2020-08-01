package com.wisecarCompany.wisecarapp.user.vehicle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.wisecarCompany.wisecarapp.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;


public class CalendarActivity extends AppCompatActivity {

    private final static String TAG = "Calendar";

    private final String IP_HOST = "http://54.206.19.123:3000";
    private final String GET_CALENDAR_NOTIFICATION = "/api/v1/notification/calendar";

    private SharedPreferences sp;
    private String userID;

    private ImageButton backImageButton;

    private ConstraintLayout backMonthButtonDiv;
    private ConstraintLayout forthMonthButtonDiv;
    private TextView backMonthTextView;
    private TextView forthMonthTextView;
    private TextView currMonthYearTextView;

    //private ConstraintLayout[] dateLineDiv;
    private TextView[][] dateTextView;

    private LinearLayout noticeDiv;

    private Calendar cal;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        sp = this.getSharedPreferences("userInfo", MODE_PRIVATE);
        userID = sp.getString("USER_ID", "");
        Log.d(TAG, "userID: " + userID);

        backImageButton = $(R.id.backImageButton);
        backImageButton.setOnClickListener(v -> startActivity(new Intent(this, VehicleActivity.class)));

        cal = Calendar.getInstance();
        cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH),0, 0, 0);   //still contains millisec, need /1000*1000 to get only date

        backMonthTextView = $(R.id.backMonthTextView);
        forthMonthTextView = $(R.id.forthMonthTextView);
        currMonthYearTextView = $(R.id.currMonthYearTextView);

        //dateLineDiv = new ConstraintLayout[] {$(R.id.dayLine0Div), $(R.id.dayLine1Div), $(R.id.dayLine2Div), $(R.id.dayLine3Div), $(R.id.dayLine4Div), $(R.id.dayLine5Div)};
        dateTextView = new TextView[][] {
                {$(R.id.textView00), $(R.id.textView01), $(R.id.textView02), $(R.id.textView03), $(R.id.textView04), $(R.id.textView05), $(R.id.textView06)},
                {$(R.id.textView10), $(R.id.textView11), $(R.id.textView12), $(R.id.textView13), $(R.id.textView14), $(R.id.textView15), $(R.id.textView16)},
                {$(R.id.textView20), $(R.id.textView21), $(R.id.textView22), $(R.id.textView23), $(R.id.textView24), $(R.id.textView25), $(R.id.textView26)},
                {$(R.id.textView30), $(R.id.textView31), $(R.id.textView32), $(R.id.textView33), $(R.id.textView34), $(R.id.textView35), $(R.id.textView36)},
                {$(R.id.textView40), $(R.id.textView41), $(R.id.textView42), $(R.id.textView43), $(R.id.textView44), $(R.id.textView45), $(R.id.textView46)},
                {$(R.id.textView50), $(R.id.textView51), $(R.id.textView52), $(R.id.textView53), $(R.id.textView54), $(R.id.textView55), $(R.id.textView56)}
        };
        noticeDiv = $(R.id.noticeDiv);
        backMonthButtonDiv = $(R.id.backMonthButtonDiv);
        forthMonthButtonDiv = $(R.id.forthMonthButtonDiv);

        getAllNotifications(new calendarCallbacks() {
            @Override
            public void onSuccess(@NonNull Map<Date, List<String[]>> value) {

                setCalendar(value);
                setNotices(value);

                backMonthButtonDiv.setOnClickListener(v -> {
                    cal.add(Calendar.MONTH, -1);
                    setCalendar(value);
                    noticeDiv.removeAllViews();
                    setNotices(value);
                });
                forthMonthButtonDiv.setOnClickListener(v -> {
                    cal.add(Calendar.MONTH, 1);
                    setCalendar(value);
                    noticeDiv.removeAllViews();
                    setNotices(value);
                });

            }

            @Override
            public void onError(@NonNull String errorMessage) {
                Log.e(TAG, "getAllNotifications onError: " + errorMessage);
            }
        });

    }

    @SuppressLint("SetTextI18n")
    private void setCalendar(Map<Date, List<String[]>> notices) {
        assert notices != null;

        Log.d(TAG, "setCalendar: cal: " + cal.getTime());
        cal.set(Calendar.DAY_OF_MONTH, 1);  //first day of this month
        int week = 0;
        int weekIndex = cal.get(Calendar.DAY_OF_WEEK) - 1;  //0:Sun, 6:Sat
        int daysOfMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        for(int i=0; i<weekIndex; i++) {  //other days not in this month but may in previous/next month and set content before, clear weekdays before 1st day of this month
            dateTextView[0][i].setText("");
            dateTextView[0][i].setOnClickListener(v -> setNotices(notices));
        }

        for(int i=1; i<=daysOfMonth; i++) {
            Date thisDate = new Date(cal.getTime().getTime()/1000*1000);
            dateTextView[week][weekIndex].setText(i+"");
            if(notices.containsKey(thisDate)) {  //only get date not time, delete millisecond

                dateTextView[week][weekIndex].setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
                boolean isEmergent;
                try{
                    isEmergent = Integer.parseInt(notices.get(thisDate).get(0)[2])<=7;  //date_diff<=7
                } catch (Exception e) {
                    Calendar currDateCal = Calendar.getInstance();
                    currDateCal.set(currDateCal.get(Calendar.YEAR), currDateCal.get(Calendar.MONTH), currDateCal.get(Calendar.DAY_OF_MONTH),0, 0, 0);
                    isEmergent = Math.abs(currDateCal.getTime().getTime() - thisDate.getTime()) <= 7*24*60*60*1000;
                }
                if(isEmergent) dateTextView[week][weekIndex].setTextColor(0xffff0000); //red
                else dateTextView[week][weekIndex].setTextColor(0xff71C984); //green

                boolean finalIsEmergent = isEmergent;   //IDE requires...
                dateTextView[week][weekIndex].setOnClickListener(v -> {
                    noticeDiv.removeAllViews();
                    int seq = 1;
                    for(String[] contents: notices.get(thisDate)) setNotice(thisDate, contents, finalIsEmergent, seq++);
                });

            } else {
                dateTextView[week][weekIndex].setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
                dateTextView[week][weekIndex].setOnClickListener(v -> setNotices(notices));
                if(weekIndex==0) dateTextView[week][weekIndex].setTextColor(0xffffc909);    //Sun
                else dateTextView[week][weekIndex].setTextColor(0xff9fa0a4);
            }

            cal.add(Calendar.DATE, 1);
            weekIndex++;
            if(weekIndex>=7) {
                weekIndex = 0;
                week++;
            }
        }

        while(weekIndex++<7) {  //clear the rest weekdays of this week
            dateTextView[week][weekIndex-1].setText("");
            dateTextView[week][weekIndex-1].setOnClickListener(v -> setNotices(notices));
        }
        if(week==4) {   //clear line5
            for(int i=0; i<7; i++) {
                dateTextView[5][i].setText("");
                dateTextView[5][i].setOnClickListener(v -> setNotices(notices));
            }
        }
        if(week==3) {   //clear line4
            for(int i=0; i<7; i++) {
                dateTextView[4][i].setText("");
                dateTextView[4][i].setOnClickListener(v -> setNotices(notices));
            }
        }

        forthMonthTextView.setText(new SimpleDateFormat("MMM", Locale.getDefault()).format(cal.getTime()));
        cal.add(Calendar.MONTH, -2);    //after the loop, cal is the first day of next month. Go to previous month first, then go to curr month
        backMonthTextView.setText(new SimpleDateFormat("MMM", Locale.getDefault()).format(cal.getTime()));
        cal.add(Calendar.MONTH, 1);
        currMonthYearTextView.setText(new SimpleDateFormat("MMM - yyyy", Locale.getDefault()).format(cal.getTime()));

    }

    private void setNotices(Map<Date, List<String[]>> notices) {
        assert notices != null;

        noticeDiv.removeAllViews();
        int seq = 1;
        for(Date date: notices.keySet()) {
            Calendar cal2 = Calendar.getInstance();
            cal2.setTime(date);
            if(cal.get(Calendar.YEAR) != cal2.get(Calendar.YEAR)) continue;
            if(cal.get(Calendar.MONTH) != cal2.get(Calendar.MONTH)) continue;   //not same month
            for(String[] contents: notices.get(date)){
                boolean isEmergent;
                try{
                    isEmergent = Integer.parseInt(contents[2])<=7;
                } catch (NumberFormatException e) {
                    Calendar currDateCal = Calendar.getInstance();
                    currDateCal.set(currDateCal.get(Calendar.YEAR), currDateCal.get(Calendar.MONTH), currDateCal.get(Calendar.DAY_OF_MONTH),0, 0, 0);
                    isEmergent = Math.abs(currDateCal.getTime().getTime() - date.getTime()) <= 7*24*60*60*1000;
                }
                setNotice(date, contents, isEmergent, seq++);
            }
        }
    }

    @SuppressLint({"ResourceType", "SetTextI18n"})
    private void setNotice(Date date, String[] contents, boolean isEmergency, int seq) {
        Log.d(TAG, "setNotice: date" + date);
        Log.d(TAG, "setNotice: content: " + contents);

        ConstraintLayout noticeLineLayout = new ConstraintLayout(this);
        noticeLineLayout.setBackgroundColor(0xffeeeeee);
        ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        params.setMargins(0, 0, 16, 16);
        noticeLineLayout.setLayoutParams(params);

        ConstraintSet set = new ConstraintSet();

        TextView seqTextView = new TextView(this);
        seqTextView.setId(1);
        seqTextView.setText(seq+"");
        seqTextView.setTextColor(0xff888888);
        seqTextView.setGravity(Gravity.CENTER);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) seqTextView.setAutoSizeTextTypeUniformWithConfiguration(10, 30, 1, TypedValue.COMPLEX_UNIT_SP);
        set.connect(seqTextView.getId(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START);
        set.connect(seqTextView.getId(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END);
        set.connect(seqTextView.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP);
        set.connect(seqTextView.getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM);
        set.setHorizontalBias(seqTextView.getId(), 0.0f);
        set.constrainPercentWidth(seqTextView.getId(), 0.1f);
        set.constrainPercentHeight(seqTextView.getId(), 0.5f);
        noticeLineLayout.addView(seqTextView);

        TextView noticeTextView = new TextView(this);
        noticeTextView.setId(2);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) noticeTextView.setAutoSizeTextTypeUniformWithConfiguration(14, 30, 1, TypedValue.COMPLEX_UNIT_SP);
        noticeTextView.setTextColor(isEmergency ? 0xffff0000 : 0xff007ba4);
        noticeTextView.setText(contents[0] + ", " + contents[1]);
        if (contents.length!=3) Log.e(TAG, "setNotice: notice value String[] length != 3 ERR0R!!");
        noticeTextView.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        set.connect(noticeTextView.getId(), ConstraintSet.START, seqTextView.getId(), ConstraintSet.END, 8);
        set.connect(noticeTextView.getId(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END, 8);
        set.connect(noticeTextView.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, 8);
        set.connect(noticeTextView.getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM);
        set.setVerticalBias(noticeTextView.getId(), 0.0f);
        set.constrainPercentHeight(noticeTextView.getId(), 0.45f);
        set.setDimensionRatio(noticeTextView.getId(), "15:1");
        noticeLineLayout.addView(noticeTextView);

        TextView dateTextView = new TextView(this);
        dateTextView.setId(3);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) dateTextView.setAutoSizeTextTypeUniformWithConfiguration(14, 30, 1, TypedValue.COMPLEX_UNIT_SP);
        dateTextView.setTextColor(isEmergency ? 0xffff0000 : 0xff007ba4);
        dateTextView.setText(new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(date));
        dateTextView.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        set.connect(dateTextView.getId(), ConstraintSet.START, seqTextView.getId(), ConstraintSet.END, 8);
        set.connect(dateTextView.getId(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END, 8);
        set.connect(dateTextView.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP);
        set.connect(dateTextView.getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, 8);
        set.setVerticalBias(dateTextView.getId(), 1.0f);
        set.constrainPercentHeight(dateTextView.getId(), 0.45f);
        set.setDimensionRatio(dateTextView.getId(), "15:1");
        noticeLineLayout.addView(dateTextView);

        set.applyTo(noticeLineLayout);
        noticeDiv.addView(noticeLineLayout);

    }


    private void getAllNotifications(@Nullable final calendarCallbacks callbacks) {

        String URL = IP_HOST + GET_CALENDAR_NOTIFICATION;
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        final JSONObject jsonParam = new JSONObject();
        try {
            jsonParam.put("user_id", userID);
            jsonParam.put("current_date", format.format(new Date()));

        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest objectRequest = new JsonObjectRequest(Request.Method.POST, URL, jsonParam, response -> {
            Log.e("Notification response: ", response.toString());
            JSONArray jsonArray;
            JSONObject jsonObject;

            Calendar currDateCal = Calendar.getInstance();
            currDateCal.set(currDateCal.get(Calendar.YEAR), currDateCal.get(Calendar.MONTH), currDateCal.get(Calendar.DAY_OF_MONTH),0, 0, 0);
            Map<Date, List<String[]>> notifications = new TreeMap<>((o1, o2) -> { //sort by the time distance from today
                return Long.compare(Math.abs(currDateCal.getTime().getTime() - o1.getTime()), Math.abs(currDateCal.getTime().getTime() - o2.getTime()));
            });

            try {
                jsonArray = response.getJSONArray("results");
                for (int i = 0; i < jsonArray.length(); i++) {
                    jsonObject = jsonArray.getJSONObject(i);

                    Date date = null;

                    try {
                        date = format.parse(jsonObject.optString("expiry_date"));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    String[] notiInfo = new String[3];
                    notiInfo[0] = (jsonObject.optString("registration_no"));
                    notiInfo[1] = (jsonObject.optString("type"));
                    notiInfo[2] = (jsonObject.optString("date_diff"));

                    List<String[]> contents = notifications.get(date);
                    if(contents==null) contents = new ArrayList<>();
                    contents.add(notiInfo);
                    notifications.put(date, contents);
                }
                if (callbacks != null)
                    callbacks.onSuccess(notifications);
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
                Log.e("No notification: ", message);
                if (callbacks != null)
                    callbacks.onError(message);
            }
        });

        Volley.newRequestQueue(this).add(objectRequest);
    }

    public interface calendarCallbacks {
        void onSuccess(@NonNull Map<Date, List<String[]>> value);

        void onError(@NonNull String errorMessage);
    }


    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, VehicleActivity.class));
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK) {
            startActivity(new Intent(this, VehicleActivity.class));
            return true;    //stop calling super method
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }


    private <T extends View> T $(int id) {
        return (T) findViewById(id);
    }

}
