package com.example.wisecarapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.text.Layout;
import android.util.Log;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class RecordLogActivity extends AppCompatActivity {

    private final static String TAG = "RecordLogActivity";

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


    @SuppressLint({"SetTextI18n", "Assert", "ResourceType"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_log);

        logs = new HashSet<>();
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
            companyTextView.setTextColor(0xdb0a00);
            companyTextView.setText("Not shared with any companies");
        } else {
            companyTextView.setTextColor(0x6f0a00);
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
            UserInfo.getCurrLog().setRecording(1);
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
            recording();
        });

        pauseResumeImageButton.setOnClickListener(v -> {
            if(UserInfo.getCurrLog()==null || UserInfo.getCurrLog().getRecording()==0) {
                Toast.makeText(getApplicationContext(), "Please start the record first", Toast.LENGTH_LONG).show();
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
                Toast.makeText(getApplicationContext(), "Please start the record first", Toast.LENGTH_LONG).show();
            }
            UserInfo.getCurrLog().setRecording(0);
            ending();
            finishRecord();

        });

        logsDiv = $(R.id.logsDiv);
        for(RecordLog log: logs) {

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
            dateImageView.setBackground(getResources().getDrawable(R.drawable.record_log0date));
            set.connect(dateImageView.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP);
            set.connect(dateImageView.getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM);
            set.connect(dateImageView.getId(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START, 32);
            set.connect(dateImageView.getId(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END);
            set.constrainPercentWidth(dateImageView.getId(), 0.18f);
            set.setDimensionRatio(dateImageView.getId(), "1:1");
            set.setHorizontalBias(dateImageView.getId(), 0.0f);
            logLineLayout.addView(dateImageView);



            set.applyTo(logLineLayout);
            logsDiv.addView(logLineLayout);

        }

    }

    private void recording() {   //1
        pauseResumeImageButton.setImageDrawable(getResources().getDrawable(R.drawable.record_log0pause));
        timeDistanceTextView.setTextColor(0x007ba4);

    }

    private void pausing() {    //2
        pauseResumeImageButton.setImageDrawable(getResources().getDrawable(R.drawable.record_log0resume));
        timeDistanceTextView.setTextColor(0xa5a6a3);

    }

    private void ending() { //0
        timeDistanceTextView.setText("");
        pauseResumeImageButton.setImageDrawable(getResources().getDrawable(R.drawable.record_log0pause));

    }

    private void finishRecord() {   //write to log

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
