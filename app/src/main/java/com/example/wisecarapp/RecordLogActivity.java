package com.example.wisecarapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
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
import java.time.Period;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

public class RecordLogActivity extends AppCompatActivity {

    private final static String TAG = "RecordLogActivity";

    private final String IP_HOST = "http://54.206.19.123:3000";
    private final String GET_SHARED_LIST = "/api/v1/sharevehicle/sharedcompanylist/";

    private String vehicleID;
    private Vehicle vehicle;

    private LocationManager locationManager;
    private double latitude = 0.0;
    private double longitude = 0.0;
    private long duration = 0;  //last duration before pausing

    private Set<Share> shares;  //从数据库返回
    private Share currShare;

    private ImageButton backImageButton;

    private AutoCompleteTextView searchEditText;
    private ImageButton fliterImageButton;

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

        vehicleID = (String) this.getIntent().getStringExtra("vehicleID");
        //vehicleID = "303";
        Log.d(TAG, "vehicleID: " + vehicleID);
        vehicle = UserInfo.getVehicles().get(vehicleID);
        Log.d(TAG, "vehicle: " + vehicle);

        //List<RecordLog> logs = DB.get...
        List<RecordLog> logs = new ArrayList<>();
        //vehicle.setLogs(logs);

        backImageButton = $(R.id.backImageButton);
        backImageButton.setOnClickListener(v -> startActivity(new Intent(RecordLogActivity.this, VehicleActivity.class)));

        logsDiv = $(R.id.logsDiv);
        for(RecordLog log: logs) addRecentLog(log);

        searchEditText = $(R.id.searchEditText);
        //......

        fliterImageButton = $(R.id.fliterImageButton);
        fliterImageButton.setOnClickListener(v -> {

            ConstraintLayout layout = new ConstraintLayout(this);
            layout.setMinHeight(200);
            ConstraintSet set = new ConstraintSet();
            EditText startDuration = new EditText(this);
            startDuration.setId(1);
            set.connect(startDuration.getId(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START);
            set.connect(startDuration.getId(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END);
            set.connect(startDuration.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP);
            set.connect(startDuration.getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM);
            set.setHorizontalBias(startDuration.getId(), 0.0f);
            set.constrainPercentWidth(startDuration.getId(), 0.5f);
            layout.addView(startDuration);
            set.applyTo(layout);

            new AlertDialog.Builder(this).setTitle("Fliter")
                    .setIcon(android.R.drawable.ic_dialog_info)
                    .setView(layout)
                    .setPositiveButton("Confirm", (dialog, which) -> {
                        String input = startDuration.getText().toString();
                        if (input.equals("")) {

                        } else {
                            Log.d(TAG, input);
                        }
                    }).setNegativeButton("Cancel", null).show();


            logsDiv.removeAllViews();
            //re add
        });

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

        if(UserInfo.getCurrLog()==null) {
            ending();
        } else if(UserInfo.getCurrLog().isPausing()) {
            pausing();
        } else {
            recording();
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
                    Log.d(TAG, "new currLog: " + UserInfo.getCurrLog());
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                int permissionCheckFineLocation = ContextCompat.checkSelfPermission(RecordLogActivity.this, Manifest.permission.ACCESS_FINE_LOCATION);
                int permissionCheckCoarseLocation = ContextCompat.checkSelfPermission(RecordLogActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION);
                Log.d(TAG, "onClickPermissionCheckFineLocation: " + permissionCheckFineLocation);
                Log.d(TAG, "onClickPermissionCheckCoarseLocation: " + permissionCheckCoarseLocation);

                if(permissionCheckFineLocation == PackageManager.PERMISSION_DENIED || permissionCheckCoarseLocation == PackageManager.PERMISSION_DENIED) {  //first time using this function, or denied before
                    ActivityCompat.requestPermissions(
                            RecordLogActivity.this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                            0
                    );
                } else {
                    Log.d(TAG, "Permitted");
                    recording();
                }

            } else {
                Toast.makeText(getApplicationContext(), "You already start a record", Toast.LENGTH_LONG).show();
            }
        });

        pauseResumeImageButton.setOnClickListener(v -> {
            if(UserInfo.getCurrLog()==null) {
                Toast.makeText(getApplicationContext(), "Please start a record first", Toast.LENGTH_LONG).show();
                return;
            }
            if(UserInfo.getCurrLog().isPausing()) {    //resume
                UserInfo.getCurrLog().setPausing(false);
                recording();
            } else { //pause
                UserInfo.getCurrLog().setPausing(true);
                UserInfo.getCurrLog().setCountPause(UserInfo.getCurrLog().getCountPause()+1);
                pausing();
            }
        });

        endImageButton.setOnClickListener(v -> {
            if(UserInfo.getCurrLog()==null) {
                Toast.makeText(getApplicationContext(), "Please start a record first", Toast.LENGTH_LONG).show();
                return;
            }
            ending();
            finishRecord();
            UserInfo.setCurrLog(null);
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult: Length: " + grantResults.length);
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 0:
                Log.d(TAG, "onRequestPermissionsResult?");
                if(grantResults[0] == 0) {
                    Log.d(TAG, "Permit");
                    recording();
                } else {
                    Toast.makeText(getApplicationContext(), "You cannot record without authorization", Toast.LENGTH_SHORT).show();
                    UserInfo.setCurrLog(null);
                }
                break;
        }
    }

    private void recording() {
        Log.d(TAG, "recording: ");
        pauseResumeImageButton.setImageDrawable(getResources().getDrawable(R.drawable.record_log0pause));
        timeDistanceTextView.setTextColor(0xff007ba4);

        startLocation();

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                if(UserInfo.getCurrLog()==null) {
                    Log.d(TAG, "timer: end");
                    this.cancel();
                } else if(UserInfo.getCurrLog().isPausing()) {
                    Log.d(TAG, "timer: pause");
                    this.cancel();
                } else {
                    Log.d(TAG, "timer: currLog: " + UserInfo.getCurrLog());
                    duration += 1000;
                    String minDuration = duration/(60 * 1000)>=10 ? ""+ duration/(60 * 1000) : "0"+ duration/(60 * 1000);
                    String secDuration = duration/1000>=10 ? ""+ duration/1000 : "0"+ duration/1000;
                    timeDistanceTextView.setText(minDuration + ":" + secDuration + ", " + (int)(UserInfo.getCurrLog().getKm()*10)/10.0 + "km");
                }
            }
        }, 1000, 1000);
    }

    private void pausing() {
        Log.d(TAG, "pausing: ");
        pauseResumeImageButton.setImageDrawable(getResources().getDrawable(R.drawable.record_log0resume));
        timeDistanceTextView.setTextColor(0xffa5a6a3);
        timeDistanceTextView.setText(timeDistanceTextView.getText().toString() + " (paused)");

        //Settings.Secure.setLocationProviderEnabled(getContentResolver(), LocationManager.GPS_PROVIDER, false);
        if(locationManager!=null) locationManager.removeUpdates(locationListener);
    }

    private void ending() {
        Log.d(TAG, "ending: ");
        timeDistanceTextView.setText("");
        pauseResumeImageButton.setImageDrawable(getResources().getDrawable(R.drawable.record_log0pause));

        //Settings.Secure.setLocationProviderEnabled(getContentResolver(), LocationManager.GPS_PROVIDER, false);
        if(locationManager!=null) locationManager.removeUpdates(locationListener);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void finishRecord() {   //write to log
        Log.d(TAG, "finishRecord: ");
        UserInfo.getCurrLog().setMins((int)duration/(60 * 1000));
        duration = 0;

        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        try {
            UserInfo.getCurrLog().setEndTime(timeFormat.parse(timeFormat.format(new Date())));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "finishRecord: currLog: " + UserInfo.getCurrLog());
        addRecentLog(UserInfo.getCurrLog());
        //vehicle.getLogs().add(UserInfo.getCurrLog());
    }

    @SuppressLint("HandlerLeak")
    private void startLocation() {
        //Settings.Secure.setLocationProviderEnabled(getContentResolver(), LocationManager.GPS_PROVIDER, true);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        assert locationManager != null;
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Log.d(TAG, "gps is turned on");
            getLocation();
        } else {
            Log.d(TAG, "gps is turned off");
            toggleGPS();
            new Handler() {
            }.postDelayed((Runnable) this::getLocation, 2000);
        }
    }

    @SuppressLint("MissingPermission")  //only when permitted will this function be called
    private void toggleGPS() {  //This method is never tested!!
        Log.d(TAG, "toggleGPS: ");
        Intent gpsIntent = new Intent();
        gpsIntent.setClassName("com.android.settings", "com.android.settings.widget.SettingsAppWidgetProvider");
        gpsIntent.addCategory("android.intent.category.ALTERNATIVE");
        gpsIntent.setData(Uri.parse("custom:3"));
        try {
            PendingIntent.getBroadcast(this, 0, gpsIntent, 0).send();
        } catch (PendingIntent.CanceledException e) {
            e.printStackTrace();
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0, locationListener);
            Location location1 = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if (location1 != null) {
                latitude = location1.getLatitude();
                longitude = location1.getLongitude();
            }
        }
    }

    @SuppressLint("MissingPermission")  //only when permitted will this function be called
    private void getLocation() {
        if(UserInfo.getCurrLog()!=null && !UserInfo.getCurrLog().isPausing()) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, locationListener);
            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            Log.d(TAG, "location: " + location);
        } else {
            locationManager.removeUpdates(locationListener);
        }
        /*
        if (location != null) {
            latitude = location.getLatitude();
            longitude = location.getLongitude();
        } else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, locationListener);
        }
         */
    }

    LocationListener locationListener = new LocationListener() {
        // Provider的状态在可用、暂时不可用和无服务三个状态直接切换时触发此函数
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }
        // Provider被enable时触发此函数，比如GPS被打开
        @Override
        public void onProviderEnabled(String provider) {
            Log.e(TAG, provider);
        }
        // Provider被disable时触发此函数，比如GPS被关闭
        @Override
        public void onProviderDisabled(String provider) {
            Log.e(TAG, provider);
        }
        // 当坐标改变时触发此函数，如果Provider传进相同的坐标，它就不会被触发
        @Override
        public void onLocationChanged(Location location) {
            if (location != null) {
                Log.e("Map", "Location changed : Lat: " + location.getLatitude() + " Lng: " + location.getLongitude());
                double distance = getDistance(longitude, latitude, location.getLongitude(), location.getLatitude());
                Log.d(TAG, "distance(m): " + distance);
                UserInfo.getCurrLog().setKm(UserInfo.getCurrLog().getKm() + distance/1000.0);
                Log.d(TAG, "km: " + UserInfo.getCurrLog().getKm());
                latitude = location.getLatitude();
                longitude = location.getLongitude();
            }
        }
    };

    private double getDistance(double lon1, double lat1, double lon2, double lat2) {
        double radLat1 = rad(lat1);
        double radLat2 = rad(lat2);
        double a = radLat1 - radLat2;
        double b = rad(lon1) - rad(lon2);
        double s = 2 *Math.asin(Math.sqrt(Math.pow(Math.sin(a/2),2)+Math.cos(radLat1)*Math.cos(radLat2)*Math.pow(Math.sin(b/2),2)));
        double EARTH_RADIUS = 6378137;
        s = s * EARTH_RADIUS;
        return s;   //in meter not km
    }
    private double rad(double d){
        return d * Math.PI / 180.0;
    }

    @SuppressLint("ResourceType")
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void addRecentLog(RecordLog log) {
        Log.d(TAG, "add recent log: " + log);

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
        timeDistanceTextView.setText(log.getMins() + "Mins, " + (int)(log.getKm()*10)/10.0 + "KM");
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
