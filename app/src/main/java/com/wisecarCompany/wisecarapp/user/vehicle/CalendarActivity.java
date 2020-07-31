package com.wisecarCompany.wisecarapp.user.vehicle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
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
import com.wisecarCompany.wisecarapp.user.UserInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


public class CalendarActivity extends AppCompatActivity {

    private final static String TAG = "Calendar";

    private ImageButton backImageButton;

    private ConstraintLayout backMonthButtonDiv;
    private ConstraintLayout forthMonthButtonDiv;
    private TextView backMonthTextView;
    private TextView forthMonthTextView;
    private TextView currMonthYearTextView;

    //private ConstraintLayout[] dateLineDiv;
    private TextView[][] dateTextView;

    private LinearLayout noticeDiv;

    private final String IP_HOST = "http://54.206.19.123:3000";
    private final String GET_CALENDAR_NOTIFICATION = "/api/v1/notification/calendar";

    private Calendar cal;
//  if(Math.abs(cal.getTime().getTime() - new Date().getTime()) <= 7*24*60*60*1000) {   //within 7 days before/from now


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

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
        setCalendar();

        noticeDiv = $(R.id.noticeDiv);
        setNotices();

        backMonthButtonDiv = $(R.id.backMonthButtonDiv);
        forthMonthButtonDiv = $(R.id.forthMonthButtonDiv);
        backMonthButtonDiv.setOnClickListener(v -> {
            cal.add(Calendar.MONTH, -1);
            setCalendar();
            noticeDiv.removeAllViews();
            setNotices();
        });
        forthMonthButtonDiv.setOnClickListener(v -> {
            cal.add(Calendar.MONTH, 1);
            setCalendar();
            noticeDiv.removeAllViews();
            setNotices();
        });


        getTwoClosestNotifications(new calendarCallbacks() {
            @Override
            public void onSuccess(@NonNull Map<Date, String[]> value) {

            }

            @Override
            public void onError(@NonNull String errorMessage) {

            }
        });

    }

    private void setCalendar() {
        Log.d(TAG, "setCalendar: cal: " + cal.getTime());
        cal.set(Calendar.DAY_OF_MONTH, 1);
        int week = 0;
        int weekIndex = cal.get(Calendar.DAY_OF_WEEK) - 1;  //0:Sun, 6:Sat
        int daysOfMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        for(int i=0; i<weekIndex; i++) dateTextView[0][i].setText("");  //other days not in this month
        for(int i=1; i<=daysOfMonth; i++) {
            dateTextView[week][weekIndex].setText(i+"");
            if(UserInfo.getEmerNotices().containsKey(new Date(cal.getTime().getTime()/1000*1000))) {  //only get date not time
                dateTextView[week][weekIndex].setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
                dateTextView[week][weekIndex].setTextColor(0xffff0000); //red
            } else if(UserInfo.getNotices().containsKey(new Date(cal.getTime().getTime()/1000*1000))) {
                dateTextView[week][weekIndex].setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
                dateTextView[week][weekIndex].setTextColor(0xff71C984); //green
            } else {
                dateTextView[week][weekIndex].setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
                if(weekIndex==0) dateTextView[week][weekIndex].setTextColor(0xffffc909);
                else dateTextView[week][weekIndex].setTextColor(0xff9fa0a4);
            }
            cal.add(Calendar.DATE, 1);
            weekIndex++;
            if(weekIndex>=7) {
                weekIndex = 0;
                week++;
            }
        }
        while(weekIndex++<7) dateTextView[week][weekIndex-1].setText("");
        if(week==4) for(int i=0; i<7; i++) dateTextView[5][i].setText("");
        if(week==3) for(int i=0; i<7; i++) dateTextView[4][i].setText("");
        forthMonthTextView.setText(new SimpleDateFormat("MMM", Locale.getDefault()).format(cal.getTime()));
        cal.add(Calendar.MONTH, -2);    //after the loop, cal is the first day of next month
        backMonthTextView.setText(new SimpleDateFormat("MMM", Locale.getDefault()).format(cal.getTime()));
        cal.add(Calendar.MONTH, 1);
        currMonthYearTextView.setText(new SimpleDateFormat("MMM - yyyy", Locale.getDefault()).format(cal.getTime()));
    }

    private void setNotices() {
        int seq = 1;
        for(Map.Entry<Date, String[]> entry: UserInfo.getEmerNotices().entrySet()) {
            Calendar cal2 = Calendar.getInstance();
            cal2.setTime(entry.getKey());
            if(cal.get(Calendar.YEAR) != cal2.get(Calendar.YEAR)) continue;
            if(cal.get(Calendar.MONTH) != cal2.get(Calendar.MONTH)) continue;
            setNotice(entry, true, seq++);
        }
        for(Map.Entry<Date, String[]> entry: UserInfo.getNotices().entrySet()) {
            Calendar cal2 = Calendar.getInstance();
            cal2.setTime(entry.getKey());
            if(cal.get(Calendar.YEAR) != cal2.get(Calendar.YEAR)) continue;
            if(cal.get(Calendar.MONTH) != cal2.get(Calendar.MONTH)) continue;
            setNotice(entry, false, seq++);
        }
    }

    @SuppressLint({"ResourceType", "SetTextI18n"})
    private void setNotice(Map.Entry<Date, String[]> notice, boolean isEmergency, int seq) {
        Log.d(TAG, "setNotice: " + notice);

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
        noticeTextView.setText(notice.getValue()[0] + ", " + notice.getValue()[1]);
        if (notice.getValue().length!=2) Log.e(TAG, "setNotice: notice value String[] length != 2 ERR0R!!");
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
        dateTextView.setText(new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(notice.getKey()));
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

    private <T extends View> T $(int id) {
        return (T) findViewById(id);
    }

    private void getTwoClosestNotifications(@Nullable final calendarCallbacks callbacks) {

        String URL = IP_HOST + GET_CALENDAR_NOTIFICATION;
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        final JSONObject jsonParam = new JSONObject();
        try {
            jsonParam.put("user_id", UserInfo.getUserID());
            jsonParam.put("current_date", format.format(new Date()));

        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest objectRequest = new JsonObjectRequest(Request.Method.POST, URL, jsonParam, response -> {
            Log.e("Notification response: ", response.toString());
            JSONArray jsonArray;
            JSONObject jsonObject;
            Map<Date, String[]> notifications = new HashMap<>();
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

                    notifications.put(date, notiInfo);
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

        Volley.newRequestQueue(CalendarActivity.this).add(objectRequest);
    }

    public interface calendarCallbacks {
        void onSuccess(@NonNull Map<Date, String[]> value);

        void onError(@NonNull String errorMessage);
    }
}
