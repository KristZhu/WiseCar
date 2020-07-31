package com.wisecarCompany.wisecarapp.function.driverLog;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.app.NotificationCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.text.Html;
import android.text.InputType;
import android.util.Base64;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
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
import com.wisecarCompany.wisecarapp.user.vehicle.ManageVehicleActivity;
import com.wisecarCompany.wisecarapp.R;
import com.wisecarCompany.wisecarapp.user.UserInfo;
import com.wisecarCompany.wisecarapp.user.vehicle.Vehicle;
import com.wisecarCompany.wisecarapp.user.vehicle.VehicleActivity;
import com.wisecarCompany.wisecarapp.viewElement.ScreenListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeSet;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;


public class DriverLogActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {

    private final static String TAG = "DriverLogActivity";

    private final String IP_HOST = "http://54.206.19.123:3000";
    private final String GET_CURRENT_SHARE = "/api/v1/drivelog/currentsharedetail";
    private final String GET_LOG_BY_VID = "/api/v1/drivelog/recentlogbyvid";
    private final String GET_LOG_BY_COMPANY = "/api/v1/drivelog/recentlogbycompany";
    private final String SAVE_LOG = "/api/v1/drivelog/savedrivelog";
    private final String GET_COMPANY_LIST = "/api/v1/customers/customer/list";

    private String vehicleID;
    private Vehicle vehicle;

    private LocationManager locationManager;
    //private CurrDriverLog currLog;    //saver to use UserInfo.getCurrLog

    private static final int REQUEST_CODE_PERMISSION_LOCATION = 1;
    private static final int REQUEST_CODE_LOCATION = 1;

    //private final int REQUEST_CODE_PERMISSION_LOCATION = 0;

    private ImageButton backImageButton;

    private AutoCompleteTextView searchEditText;
    private ImageButton cancelImageButton;

    //the fliter is depreciated. but mini/max are still in showLog. can be deleted in the future.
    private ImageButton fliterImageButton;
    private Date miniDate;
    private Date maxDate;
    private int miniMin;
    private int maxMin;
    private double miniDistance;
    private double maxDistance;
    //end of fliter fields

    private TextView companyTextView;
    private String currCustID;
    private String currShareID;
    private double currClaimRate;
    private String currCompanyName;
    private Bitmap currCompanyLogo;

    private ImageButton startImageButton;
    private ImageButton pauseResumeImageButton;
    private ImageButton endImageButton;
    private TextView timeDistanceTextView;

    private LinearLayout logsDiv;

    PowerManager.WakeLock wakeLock;
    private ScreenListener screenListener;
    MediaPlayer mediaPlayer;
    NotificationManager manager;


    @SuppressLint({"SetTextI18n", "Assert", "ResourceType"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_log);

        vehicleID = (String) this.getIntent().getStringExtra("vehicleID");
        Log.d(TAG, "vehicleID: " + vehicleID);
        vehicle = UserInfo.getVehicles().get(vehicleID);
        Log.d(TAG, "vehicle: " + vehicle);

        backImageButton = $(R.id.backImageButton);
        backImageButton.setOnClickListener(v -> startActivity(new Intent(DriverLogActivity.this, ManageVehicleActivity.class).putExtra("vehicleID", vehicleID)));

        searchEditText = $(R.id.searchEditText);
        cancelImageButton = $(R.id.cancelImageButton);
        logsDiv = $(R.id.logsDiv);
        miniDistance = -1;
        maxDistance = -1;
        miniMin = -1;
        maxMin = -1;

        queryRecordLogsByVehicleID(vehicleID, new logsCallbacks() {
            @Override
            public void onSuccess(@NonNull Set<DriverLog> logs) {
                Log.d(TAG, "logs: " + logs);
                vehicle.setLogs(logs);
                logsDiv.removeAllViews();
                for (DriverLog log : logs) showRecordLog(log);
            }

            @Override
            public void onError(@NonNull Set<DriverLog> logs) {
                // Here is when there is no log, an empty List is returned.
                Log.d(TAG, "no logs");
            }

        });

        final ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new ArrayList<>());
        returnCompanies(new companiesCallbacks() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onSuccess(@NonNull Map<String, String> companies) {
                Log.d(TAG, "companies: " + companies);
                for (String name : companies.keySet()) {
                    adapter.add(name);
                }
                searchEditText.setAdapter(adapter);
                searchEditText.setOnItemClickListener((parent, view, position, id) -> {
                    cancelImageButton.setVisibility(View.VISIBLE);
                    String companyName = searchEditText.getText().toString();
                    String customer_id = companies.get(companyName);
                    queryRecordLogsByCompany(customer_id, new logsCallbacks() {
                        @Override
                        public void onSuccess(@NonNull Set<DriverLog> logs) {
                            logsDiv.removeAllViews();
                            for (DriverLog log : logs) showRecordLog(log);
                        }
                        @Override
                        public void onError(@NonNull Set<DriverLog> value) {
                            //no logs for this company
                            logsDiv.removeAllViews();
                        }
                    });
                });
            }

            @Override
            public void onError(@NonNull String errorMessage) {

            }
        });

        cancelImageButton.setOnClickListener(v -> {
            searchEditText.setText("");
            cancelImageButton.setVisibility(View.GONE);
            logsDiv.removeAllViews();
            for (DriverLog log : vehicle.getLogs()) showRecordLog(log);
        });


        //depreciated function
        fliterImageButton = $(R.id.fliterImageButton);
        fliterImageButton.setOnClickListener(v -> { //currently both company and fliterAlertDialog fliter is not supposed. Only one.
            searchEditText.setText("");

            LayoutInflater factory = LayoutInflater.from(this);
            @SuppressLint("InflateParams") View view = factory.inflate(R.layout.driver_log_fliter_alert, null);
            EditText miniDateEditText = (EditText) view.findViewById(R.id.miniDate);
            EditText maxDateEditText = (EditText) view.findViewById(R.id.maxDate);
            EditText miniMinEditText = (EditText) view.findViewById(R.id.miniMin);
            EditText maxMinEditText = (EditText) view.findViewById(R.id.maxMin);
            EditText miniDistanceEditText = (EditText) view.findViewById(R.id.miniDistance);
            EditText maxDistanceEditText = (EditText) view.findViewById(R.id.maxDistance);

            SimpleDateFormat format = new SimpleDateFormat("ddMMM yyyy", Locale.getDefault());
            if(miniDate != null) miniDateEditText.setText(format.format(miniDate));
            if(maxDate != null) maxDateEditText.setText(format.format(maxDate));
            if(miniDistance >= 0) miniDistanceEditText.setText(((int)miniDistance*10)/10.0 + "");
            if(maxDistance >= 0) maxDistanceEditText.setText(((int)maxDistance*10)/10.0 + "");
            if(miniMin >= 0) miniMinEditText.setText(miniMin);
            if(maxMin >= 0) maxMinEditText.setText(maxMin);

            miniDateEditText.setInputType(InputType.TYPE_NULL);
            miniDateEditText.setOnClickListener(v1 -> {
                Calendar c = Calendar.getInstance();
                new DatePickerDialog(DriverLogActivity.this, (view1, year, monthOfYear, dayOfMonth) -> {
                    miniDate = intToDate(year, monthOfYear, dayOfMonth);
                    String str = format.format(miniDate);
                    miniDateEditText.setText(str);
                }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
            });
            miniDateEditText.setOnFocusChangeListener((v1, hasFocus) -> {
                if (hasFocus) {
                    Calendar c = Calendar.getInstance();
                    new DatePickerDialog(DriverLogActivity.this, (view1, year, monthOfYear, dayOfMonth) -> {
                        miniDate = intToDate(year, monthOfYear, dayOfMonth);
                        String str = format.format(miniDate);
                        miniDateEditText.setText(str);
                    }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();

                }
            });

            maxDateEditText.setInputType(InputType.TYPE_NULL);
            maxDateEditText.setOnClickListener(v1 -> {
                Calendar c = Calendar.getInstance();
                new DatePickerDialog(DriverLogActivity.this, (view1, year, monthOfYear, dayOfMonth) -> {
                    maxDate = intToDate(year, monthOfYear, dayOfMonth);
                    String str = format.format(maxDate);
                    maxDateEditText.setText(str);
                }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
            });
            maxDateEditText.setOnFocusChangeListener((v1, hasFocus) -> {
                if (hasFocus) {
                    Calendar c = Calendar.getInstance();
                    new DatePickerDialog(DriverLogActivity.this, (view1, year, monthOfYear, dayOfMonth) -> {
                        maxDate = intToDate(year, monthOfYear, dayOfMonth);
                        String str = format.format(maxDate);
                        maxDateEditText.setText(str);
                    }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();

                }
            });

            new AlertDialog.Builder(this)
                    .setIcon(android.R.drawable.ic_dialog_info)
                    .setView(view)
                    .setPositiveButton("Confirm", (dialog, which) -> {
                        try {
                            miniMin = miniMinEditText.getText().length() == 0 ? -1 : Integer.parseInt(miniMinEditText.getText().toString());
                            maxMin = maxMinEditText.getText().length() == 0 ? -1 : Integer.parseInt(maxMinEditText.getText().toString());
                            miniDistance = miniDistanceEditText.getText().length() == 0 ? -1 : Double.parseDouble(miniDistanceEditText.getText().toString());
                            maxDistance = maxDistanceEditText.getText().length() == 0 ? -1 : Double.parseDouble(maxDistanceEditText.getText().toString());
                            Log.d(TAG, "alert: ");
                            Log.d(TAG, "miniDate: " + miniDate);
                            Log.d(TAG, "maxDate: " + maxDate);
                            Log.d(TAG, "miniMin: " + miniMin);
                            Log.d(TAG, "maxMin: " + maxMin);
                            Log.d(TAG, "minDistance: " + miniDistance);
                            Log.d(TAG, "maxDistance: " + maxDistance);
                            if(miniDate!=null && maxDate!=null && miniDate.after(maxDate)) throw new Exception();
                            if(miniDistance>=0 && maxDistance>=0 && miniDistance > maxDistance) throw new Exception();
                            if(miniMin>=0 && maxMin>=0 && miniMin > maxMin) throw new Exception();
                            logsDiv.removeAllViews();
                            for (DriverLog log : vehicle.getLogs()) showRecordLog(log);
                        } catch (Exception e) {
                            Toast.makeText(getApplicationContext(), "Please enter correct info", Toast.LENGTH_LONG).show();
                            e.printStackTrace();
                        }
                    }).setNegativeButton("Cancel", null)
                    .show();

        });

        //depreciated function end


        companyTextView = $(R.id.companyTextView);
        getShareByTime(vehicleID, new Date(), new shareCallbacks() {
            @Override
            public void onSuccess(String custID, String companyName, double claimRate, String shareID, Date startTime, Date endTime, Bitmap companyLogo) {
                Log.d(TAG, "getShareByTime onSuccess");
                Log.d(TAG, "custID: " + custID);
                Log.d(TAG, "companyName: " + companyName);
                Log.d(TAG, "claimRate: " + claimRate);
                Log.d(TAG, "shareID: " + shareID);
                Log.d(TAG, "startTime: " + startTime);
                Log.d(TAG, "endTime: " + endTime);
                currCustID = custID;
                currShareID = shareID;
                currClaimRate = claimRate;
                currCompanyLogo = companyLogo;
                currCompanyName = companyName;

                if (custID == null) {
                    companyTextView.setTextColor(0xffdb0a00);
                    companyTextView.setText("Not shared with any companies");
                } else {
                    companyTextView.setTextColor(0xff6f0a00);
                    companyTextView.setText("Currently shared with " + companyName);
                }
            }

            @Override
            public void onError(@NonNull String errorMessage) {

            }
        });


        screenListener = new ScreenListener(this);
        screenListener.register(new ScreenListener.ScreenStateListener() {
            String TAG = "screenListener";
            @Override
            public void onScreenOn() {
                stopVoice();
            }
            @Override
            public void onScreenOff() {
                if(UserInfo.getCurrLog()!=null && !UserInfo.getCurrLog().isPausing()) startTurnOnScreenVoice();
            }
            @Override
            public void onUserPresent() {
                stopVoice();
            }
        });

        startImageButton = $(R.id.startImageButton);
        pauseResumeImageButton = $(R.id.pauseResumeImageButton);
        endImageButton = $(R.id.endImageButton);
        timeDistanceTextView = $(R.id.timeDistanceTextView);

        if (UserInfo.getCurrLog() == null) {
            ending();
        } else if (UserInfo.getCurrLog().isPausing()) {
            pausing();
        } else {
            startRecording();
        }

        startImageButton.setOnClickListener(v -> {
            if (UserInfo.getCurrLog() == null) {
                startRecording();
            } else {
                Toast.makeText(getApplicationContext(), "You already start a record", Toast.LENGTH_SHORT).show();
            }
        });

        pauseResumeImageButton.setOnClickListener(v -> {
            if (UserInfo.getCurrLog() == null) {
                Toast.makeText(getApplicationContext(), "Please start a record first", Toast.LENGTH_SHORT).show();
                return;
            }
            if (UserInfo.getCurrLog().isPausing()) {    //resume
                UserInfo.getCurrLog().setPausing(false);
                startRecording();
            } else { //pause
                UserInfo.getCurrLog().setPausing(true);
                UserInfo.getCurrLog().setCountPause(UserInfo.getCurrLog().getCountPause() + 1);
                pausing();
            }
        });

        endImageButton.setOnClickListener(v -> {
            if (UserInfo.getCurrLog() == null) {
                Toast.makeText(getApplicationContext(), "Please start a record first", Toast.LENGTH_SHORT).show();
                return;
            }
            ending();
            finishRecord();
            UserInfo.setCurrLog(null);
        });

    }

    @Override
    protected void onDestroy() {
        if (screenListener != null) {
            screenListener.unregister();
        }
        super.onDestroy();
    }

    public void startTurnOnScreenVoice() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {return;}
        mediaPlayer = MediaPlayer.create(this, R.raw.turn_on_screen);
        mediaPlayer.start();
        mediaPlayer.setOnCompletionListener(mp -> {
            if (mediaPlayer == null) {return;}
            mediaPlayer.start();
            mediaPlayer.setLooping(true);
        });
    }

    public void stopVoice() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    private void showTaskBarLogNotification(String time, String distance) {

        manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        assert manager != null;

        //8.0 and above need channelId
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            String channelId = "default";
            String channelName = "default notice";
            manager.createNotificationChannel(new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH));
        }

        //TaskStackBuilder
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(DriverLogActivity.class);
        stackBuilder.addNextIntent(new Intent(this, DriverLogActivity.class).putExtra("vehicleID", vehicleID));

        PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification taskBarLogNotification = new NotificationCompat.Builder(this, "default")
                .setSmallIcon(R.drawable.wisecar_logo)
                .setContentTitle(time)
                .setContentText(distance)
                .setAutoCancel(false)
                .setOngoing(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setContentIntent(pendingIntent)
                .build();

        manager.notify(0, taskBarLogNotification);

    }

    private void stopTaskBarLogNotification() {
        if(manager!=null) manager.cancel(0);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        UserInfo.setCurrLog(null);
    }

    @AfterPermissionGranted(REQUEST_CODE_PERMISSION_LOCATION)
    private void startRecording() {
        String[] perms;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            perms = new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_BACKGROUND_LOCATION};
        } else {
            perms = new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
        }
        if (EasyPermissions.hasPermissions(this, perms)) {
            recording();
        } else {
            EasyPermissions.requestPermissions(this, "Please enable location permissions to use the App", REQUEST_CODE_PERMISSION_LOCATION, perms);
        }
    }

    @SuppressLint("SetTextI18n")
    private void recording() {
        if(UserInfo.getCurrLog()==null) {
            UserInfo.setCurrLog(CurrDriverLog.getInstance(vehicleID, currCustID, new Date(), currClaimRate, currShareID, currCompanyName, currCompanyLogo));
        }
        Log.d(TAG, "currLog in UserInfo: " + UserInfo.getCurrLog());
        Log.d(TAG, "recording...");
        startImageButton.setAlpha(0.5f);
        pauseResumeImageButton.setAlpha(1.0f);
        endImageButton.setAlpha(1.0f);
        pauseResumeImageButton.setImageDrawable(getResources().getDrawable(R.drawable.record_log0pause));
        timeDistanceTextView.setTextColor(0xff007ba4);
        long minD = UserInfo.getCurrLog().getDuration() / 60;
        long secD = UserInfo.getCurrLog().getDuration() % 60;
        String minDuration = minD >= 10 ? "" + minD : "0" + minD;
        String secDuration = secD >= 10 ? "" + secD : "0" + secD;
        timeDistanceTextView.setText(minDuration + ":" + secDuration + ", " + (int) (UserInfo.getCurrLog().getKm() * 1000) / 1000.0 + "km");

        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                        | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                        | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        startLocation();

        if (!UserInfo.getCurrLog().isTimerRunning()) {
            //both start when clicking start/resume, and END when clicking PAUSE/end, every interval is a different timer
            //the timers here only add second, and display data. Job of getting lng/lat every second is done in getLocation method
            new Timer().schedule(new TimerTask() {  //this timer adds 1 to currLog.duration every sec and save log every 30sec
                @SuppressLint("SetTextI18n")
                @Override
                public void run() {
                    if (UserInfo.getCurrLog() == null) {
                        Log.d(TAG, "timer: end");
                        this.cancel();
                    } else if (UserInfo.getCurrLog().isPausing()) {
                        Log.d(TAG, "timer: pause");
                        this.cancel();
                        UserInfo.getCurrLog().setTimerRunning(false);
                    } else {
                        Log.d(TAG, "timer: currLog: " + UserInfo.getCurrLog());
                        UserInfo.getCurrLog().setDuration(UserInfo.getCurrLog().getDuration() + 1);

                        if (UserInfo.getCurrLog().getDuration() % 30 == 1) { //save log every 30s
                            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                            String time = format.format(new Date());
                            Log.d(TAG, "send log every 30s: ");
                            Log.d(TAG, "time: " + time);
                            Log.d(TAG, "lat: " + UserInfo.getCurrLog().getLatitude());
                            Log.d(TAG, "lng: " + UserInfo.getCurrLog().getLongitude());

                            UserInfo.getCurrLog().getLocations().put(new Date(), new double[]{UserInfo.getCurrLog().getLatitude(), UserInfo.getCurrLog().getLongitude()});
                        }
                    }
                }
            }, 500, 1000);
            UserInfo.getCurrLog().setTimerRunning(true);
        }
        new Timer().schedule(new TimerTask() {  //this timer sets timerDistanceTextView
            @Override
            public void run() {
                if (UserInfo.getCurrLog() == null) {
                    Log.d(TAG, "timer: end");
                    this.cancel();
                } else if (UserInfo.getCurrLog().isPausing()) {
                    Log.d(TAG, "timer: pause");
                    this.cancel();
                    UserInfo.getCurrLog().setTimerRunning(false);
                } else {
                    long minD = UserInfo.getCurrLog().getDuration() / 60;
                    long secD = UserInfo.getCurrLog().getDuration() % 60;
                    String minDuration = minD >= 10 ? "" + minD : "0" + minD;
                    String secDuration = secD >= 10 ? "" + secD : "0" + secD;

                    timeDistanceTextView.setText(minDuration + ":" + secDuration + ", " + (int) (UserInfo.getCurrLog().getKm() * 1000) / 1000.0 + "km");
                    showTaskBarLogNotification("Driver Log", timeDistanceTextView.getText().toString());

                    TextView testLng = $(R.id.testLng);
                    testLng.setText("" + UserInfo.getCurrLog().getLongitude());
                    TextView testLat = $(R.id.testLat);
                    testLat.setText("" + UserInfo.getCurrLog().getLatitude());
                }
            }
        }, 1000, 1000);

    }

    @SuppressLint("SetTextI18n")
    private void pausing() {
        Log.d(TAG, "pausing...");
        startImageButton.setAlpha(0.5f);
        pauseResumeImageButton.setAlpha(1.0f);
        endImageButton.setAlpha(1.0f);
        pauseResumeImageButton.setImageDrawable(getResources().getDrawable(R.drawable.record_log0resume));
        timeDistanceTextView.setTextColor(0xffa5a6a3);
        long minD = UserInfo.getCurrLog().getDuration() / 60;
        long secD = UserInfo.getCurrLog().getDuration() % 60;
        String minDuration = minD >= 10 ? "" + minD : "0" + minD;
        String secDuration = secD >= 10 ? "" + secD : "0" + secD;
        timeDistanceTextView.setText(minDuration + ":" + secDuration + ", "
                + (int) (UserInfo.getCurrLog().getKm() * 10) / 10.0 + "km");
        timeDistanceTextView.setText(timeDistanceTextView.getText().toString() + " (paused)");

        getWindow().clearFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                        | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                        | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        //Settings.Secure.setLocationProviderEnabled(getContentResolver(), LocationManager.GPS_PROVIDER, false);
        if (locationManager != null) locationManager.removeUpdates(locationListener);
    }

    private void ending() {
        Log.d(TAG, "ending...");
        startImageButton.setAlpha(1.0f);
        pauseResumeImageButton.setAlpha(0.5f);
        endImageButton.setAlpha(0.5f);
        timeDistanceTextView.setText("");
        pauseResumeImageButton.setImageDrawable(getResources().getDrawable(R.drawable.record_log0pause));

        getWindow().clearFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                        | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                        | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        stopTaskBarLogNotification();

        //Settings.Secure.setLocationProviderEnabled(getContentResolver(), LocationManager.GPS_PROVIDER, false);
        if (locationManager != null) locationManager.removeUpdates(locationListener);
    }

    private void finishRecord() {   //write to log
/*
        Log.d(TAG, "finishRecord: ");
        UserInfo.getCurrLog().setMins((int) duration / 60 );
        duration = 0;

        UserInfo.getCurrLog().setEndTime(new Date());
        UserInfo.getCurrLog().setTimestamp(new Date().getTime() + "");

        Log.d(TAG, "finishRecord: locations: " + locations);
        //convert the locations into UserInfo.getCurrLog.logJSON in proper way
        DateFormat formatDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String StringJSON = "[";
        for (Date date : locations.keySet()) {
            StringJSON += "{\"" + formatDate.format(date) + "\":\"" + locations.get(date)[0] + "," + locations.get(date)[1] + "\"},";
        }
        StringJSON = StringJSON.substring(0, StringJSON.length() - 1);
        StringJSON += "]";
        Log.e(TAG, StringJSON);
        UserInfo.getCurrLog().setLogJSON(StringJSON);
*/

        Log.d(TAG, "finishRecord: currLog: " + UserInfo.getCurrLog());
        DriverLog newLog = new DriverLog(UserInfo.getCurrLog());

        if(vehicle.getLogs()==null) vehicle.setLogs(new TreeSet<>());
        //vehicle.getLogs().add(UserInfo.getCurrLog());
        vehicle.getLogs().add(newLog);
        CurrDriverLog.clearInstance();
        UserInfo.setCurrLog(null);

        logsDiv.removeAllViews();
        for (DriverLog log : vehicle.getLogs()) showRecordLog(log);

        //add newLog to DB

        String URL = IP_HOST + SAVE_LOG;

        final JSONObject jsonParam = new JSONObject();
        DateFormat formatDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        try {
            jsonParam.put("vehicle_id", vehicleID);
            jsonParam.put("log_start_time", formatDate.format(newLog.getStartTime()));
            jsonParam.put("log_end_time", formatDate.format(newLog.getEndTime()));
            jsonParam.put("km_travelled", newLog.getKm());
            jsonParam.put("paused_time", newLog.getCountPause());
            jsonParam.put("total_travel_time", newLog.getMins());
            jsonParam.put("location_logs", newLog.getLogJSON());

            if (newLog.getCustID() != null) {
                jsonParam.put("customer_id", newLog.getCustID());
                jsonParam.put("claim_rate", newLog.getClaimRate());
                jsonParam.put("share_id", newLog.getShareID());
            } else {
                jsonParam.put("customer_id", "");
                jsonParam.put("claim_rate", "");
                jsonParam.put("share_id", "");
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest objectRequest = new JsonObjectRequest(Request.Method.POST, URL, jsonParam, response -> {
            Log.e("Save response", response.toString());
            if (response.optString("message").equals("success")) {
                Toast.makeText(getApplicationContext(), "Log successfully saved", Toast.LENGTH_LONG).show();
                Log.e("Log id", response.optString("log_id"));
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
        Volley.newRequestQueue(DriverLogActivity.this).add(objectRequest);

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
                UserInfo.getCurrLog().setLatitude(location1.getLatitude());
                UserInfo.getCurrLog().setLongitude(location1.getLongitude());
            }
        }
    }

    @SuppressLint("MissingPermission")  //only when permitted will this function be called
    private void getLocation() {
        if (UserInfo.getCurrLog() != null && !UserInfo.getCurrLog().isPausing()) {
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
            if(UserInfo.getCurrLog()==null) return;
            if (location != null) {
                Log.e("Map", "Location changed : Lat: " + location.getLatitude() + " Lng: " + location.getLongitude());
                //currLog.getLng/Lat() is the data of last sec (or default 0, 0 at beginning); location.getLng/Lat() is the curr data
                double distanceSinceLastSec = getDistance(UserInfo.getCurrLog().getLongitude(), UserInfo.getCurrLog().getLatitude(), location.getLongitude(), location.getLatitude());
                Log.d(TAG, "distance(m): " + distanceSinceLastSec);
                if(Math.abs(UserInfo.getCurrLog().getLongitude())<0.000001 && Math.abs(UserInfo.getCurrLog().getLatitude())<0.000001)
                    Log.d(TAG, "The first sec of this log");   //lng&lat == 0, which is the defalut value in construction
                else
                    UserInfo.getCurrLog().setKm(UserInfo.getCurrLog().getKm() + distanceSinceLastSec / 1000.0);
                Log.d(TAG, "km: " + UserInfo.getCurrLog().getKm());
                UserInfo.getCurrLog().setLatitude(location.getLatitude());
                UserInfo.getCurrLog().setLongitude(location.getLongitude());
            }
        }
    };

    private double getDistance(double lon1, double lat1, double lon2, double lat2) {
        double radLat1 = rad(lat1);
        double radLat2 = rad(lat2);
        double a = radLat1 - radLat2;
        double b = rad(lon1) - rad(lon2);
        double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2) + Math.cos(radLat1) * Math.cos(radLat2) * Math.pow(Math.sin(b / 2), 2)));
        double EARTH_RADIUS = 6378137;
        s = s * EARTH_RADIUS;
        return s;   //in meter not km
    }

    private double rad(double d) {
        return d * Math.PI / 180.0;
    }

    @SuppressLint("ResourceType")
    private void showRecordLog(DriverLog log) {
        Log.d(TAG, "show record log: " + log);

        if (miniDate != null && log.getStartTime().before(miniDate)) return;
        if (maxDate != null && log.getEndTime().after(new Date(maxDate.getTime() + 24 * 60 * 60 * 1000 - 1))) return;
        if (miniMin >= 0 && log.getMins() < miniMin) return;
        if (maxMin >= 0 && log.getMins() > maxMin) return;
        if (miniDistance >= 0 && log.getKm() < miniDistance) return;
        if (maxDistance >= 0 && log.getKm() > maxDistance) return;

        Log.d(TAG, "showRecordLog: the log fulfills fliter: " + log);

        ConstraintLayout logLineLayout = new ConstraintLayout(DriverLogActivity.this);
        ConstraintSet set = new ConstraintSet();

        ImageView bgImageView = new ImageView(DriverLogActivity.this);
        bgImageView.setId(0);
        bgImageView.setBackground(getResources().getDrawable(R.drawable.record_log0line));
        set.connect(bgImageView.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, 16);
        set.connect(bgImageView.getId(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START);
        set.connect(bgImageView.getId(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END);
        set.setDimensionRatio(bgImageView.getId(), "4.3:1");
        logLineLayout.addView(bgImageView);

        ImageView dateImageView = new ImageView(DriverLogActivity.this);
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

        TextView dateTextView = new TextView(DriverLogActivity.this);
        dateTextView.setId(2);
        dateTextView.setText(new SimpleDateFormat("dd MMM", Locale.getDefault()).format(log.getStartTime()));
        set.connect(dateTextView.getId(), ConstraintSet.TOP, dateImageView.getId(), ConstraintSet.TOP);
        set.connect(dateTextView.getId(), ConstraintSet.BOTTOM, dateImageView.getId(), ConstraintSet.BOTTOM);
        set.connect(dateTextView.getId(), ConstraintSet.START, dateImageView.getId(), ConstraintSet.START);
        set.connect(dateTextView.getId(), ConstraintSet.END, dateImageView.getId(), ConstraintSet.END);
        set.constrainPercentHeight(dateTextView.getId(), 0.3f);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) dateTextView.setAutoSizeTextTypeUniformWithConfiguration(10, 30, 1, TypedValue.COMPLEX_UNIT_SP);
        dateTextView.setTextColor(0xffffffff);
        dateTextView.setGravity(Gravity.CENTER);
        logLineLayout.addView(dateTextView);

        TextView timeDistanceTextView = new TextView(DriverLogActivity.this);
        timeDistanceTextView.setId(3);
        timeDistanceTextView.setText(log.getMins() + "Mins, " + (int) (log.getKm() * 10) / 10.0 + "KM");
        set.connect(timeDistanceTextView.getId(), ConstraintSet.TOP, dateImageView.getId(), ConstraintSet.TOP);
        set.connect(timeDistanceTextView.getId(), ConstraintSet.BOTTOM, dateImageView.getId(), ConstraintSet.BOTTOM);
        set.connect(timeDistanceTextView.getId(), ConstraintSet.START, dateImageView.getId(), ConstraintSet.END, 16);
        set.connect(timeDistanceTextView.getId(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END);
        set.constrainPercentHeight(timeDistanceTextView.getId(), 0.28f);
        set.setVerticalBias(timeDistanceTextView.getId(), 0.0f);
        timeDistanceTextView.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) timeDistanceTextView.setAutoSizeTextTypeUniformWithConfiguration(10, 30, 1, TypedValue.COMPLEX_UNIT_SP);
        timeDistanceTextView.setTextColor(0xff000000);
        logLineLayout.addView(timeDistanceTextView);

        TextView logInfoTextView = new TextView(DriverLogActivity.this);
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) logInfoTextView.setAutoSizeTextTypeUniformWithConfiguration(10, 30, 1, TypedValue.COMPLEX_UNIT_SP);
        logLineLayout.addView(logInfoTextView);

        if (log.getCustID() != null) {
            //CircleImageView companyLogoImageView = new CircleImageView(DriverLogActivity.this);
            ImageView companyLogoImageView = new ImageView(DriverLogActivity.this);
            companyLogoImageView.setId(5);
            companyLogoImageView.setImageBitmap(log.getCompanyLogo());
            set.connect(companyLogoImageView.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP);
            set.connect(companyLogoImageView.getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM);
            set.connect(companyLogoImageView.getId(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START);
            set.connect(companyLogoImageView.getId(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END, 16);
            set.constrainPercentWidth(companyLogoImageView.getId(), 0.18f);
            set.setDimensionRatio(companyLogoImageView.getId(), "1:1");
            set.setHorizontalBias(companyLogoImageView.getId(), 1.0f);
            logLineLayout.addView(companyLogoImageView);
        }

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

    private <T extends View> T $(int id) {
        return (T) findViewById(id);
    }

    private void getShareByTime(String vehicleID, Date date, @NonNull final shareCallbacks callbacks) {

        String URL = IP_HOST + GET_CURRENT_SHARE;
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

        final JSONObject jsonParam = new JSONObject();
        try {
            jsonParam.put("vehicle_id", vehicleID);
            jsonParam.put("current_date_time", format.format(date));

        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest objectRequest = new JsonObjectRequest(Request.Method.POST, URL, jsonParam, response -> {
            Log.e("getShareByTime Response", response.toString());

            String current_share_cust_id;
            String current_share_company_name;
            double current_share_claim_rate;
            String current_share_share_id;
            Date current_share_start_time = null;
            Date current_share_end_time = null;
            Bitmap current_share_company_logo;

            if (!response.toString().contains("The vehicle has no current share at the moment.")) {
                current_share_cust_id = response.optString("cust_id");
                current_share_company_name = response.optString("company_name");
                current_share_claim_rate = response.optDouble("claim_rate");
                current_share_share_id = response.optString("share_id");
                try {
                    current_share_start_time = format.parse(response.optString("start_time"));
                    current_share_end_time = format.parse(response.optString("end_time"));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                byte[] logoBase64 = Base64.decode(response.optString("company_logo"), Base64.DEFAULT);
                current_share_company_logo = BitmapFactory.decodeByteArray(logoBase64, 0, logoBase64.length);

                if (callbacks != null) {
                    callbacks.onSuccess(current_share_cust_id, current_share_company_name, current_share_claim_rate, current_share_share_id, current_share_start_time, current_share_end_time, current_share_company_logo);
                }

            } else {
                callbacks.onSuccess(null, null, 0, null, null, null, null);
            }
        }, error -> {
            Log.e("ERROR!!!", error.toString());
            Log.e("ERROR!!!", String.valueOf(error.networkResponse));

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
                Log.e("JSON ERROR MESSAGE!!!", message);
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
            }

        });
        Volley.newRequestQueue(DriverLogActivity.this).add(objectRequest);
    }

    public interface shareCallbacks {
        void onSuccess(@NonNull String custID, String companyName, double claimRate, String shareID, Date startTime, Date endTime, Bitmap companyLogo);

        void onError(@NonNull String errorMessage);
    }

    private void queryRecordLogsByVehicleID(String vehicleID, @Nullable final logsCallbacks callbacks) {

        String URL = IP_HOST + GET_LOG_BY_VID;
        Set<DriverLog> logs = new TreeSet<>();

        final JSONObject jsonParam = new JSONObject();
        try {
            jsonParam.put("vehicle_id", vehicleID);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest objectRequest = new JsonObjectRequest(Request.Method.POST, URL, jsonParam, response -> {
            Log.e("queryLogsByVID Response", response.toString());
            JSONObject jsonObject;
            DateFormat formatTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

            try {
                JSONArray jsonArray = response.getJSONArray("recent_logs");

                for (int i = 0; i < jsonArray.length(); i++) {
                    jsonObject = jsonArray.getJSONObject(i);
                    DriverLog log;
                    if (jsonObject.toString().contains("customer_id")) {

                        byte[] logoBase64 = Base64.decode(response.optString("company_logo"), Base64.DEFAULT);

                        log = new DriverLog(
                                vehicleID,
                                formatTime.parse(jsonObject.optString("log_start_date_time")),
                                formatTime.parse(jsonObject.optString("log_stop_date_time")),
                                jsonObject.optInt("paused_times"),
                                jsonObject.optInt("total_travel_times"),
                                jsonObject.optDouble("km_travel"),
                                jsonObject.optDouble("claim_rate"),
                                jsonObject.optString("share_id"),
                                jsonObject.optString("customer_id"),
                                jsonObject.optString("company_name"),
                                BitmapFactory.decodeByteArray(logoBase64, 0, logoBase64.length),
                                jsonObject.optString("location_log"),
                                jsonObject.optString("timestamp")
                        );
                    } else {
                        log = new DriverLog(
                                vehicleID,
                                formatTime.parse(jsonObject.optString("log_start_date_time")),
                                formatTime.parse(jsonObject.optString("log_stop_date_time")),
                                jsonObject.optInt("paused_times"),
                                jsonObject.optInt("total_travel_times"),
                                jsonObject.optDouble("km_travel"),
                                jsonObject.optString("location_log"),
                                jsonObject.optString("timestamp")
                        );
                    }
                    logs.add(log);
                }
                if (callbacks != null)
                    callbacks.onSuccess(logs);
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
                if (callbacks != null && message.equals("The vehicle has no driver logs"))
                    callbacks.onError(logs);
                else
                    Toast.makeText(getApplicationContext(), "Network error!", Toast.LENGTH_LONG).show();
            }

        });
        Volley.newRequestQueue(DriverLogActivity.this).add(objectRequest);

    }

    private void queryRecordLogsByCompany(String customer_id, @Nullable final logsCallbacks callbacks) {

        String URL = IP_HOST + GET_LOG_BY_COMPANY;
        Set<DriverLog> logs = new TreeSet<>();

        final JSONObject jsonParam = new JSONObject();
        try {
            jsonParam.put("vehicle_id", vehicleID);
            jsonParam.put("customer_id", customer_id);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest objectRequest = new JsonObjectRequest(Request.Method.POST, URL, jsonParam, response -> {
            Log.e("queryLogsByCom Response", response.toString());
            JSONObject jsonObject;
            DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

            String company_name = response.optString("company_name");
            String returned_customer_id = response.optString("customer_id");
            byte[] logoBase64 = Base64.decode(response.optString("image"), Base64.DEFAULT);
            String claim_rate = response.optString("claim_rate");

            try {
                JSONArray jsonArray = response.getJSONArray("recent_logs");

                for (int i = 0; i < jsonArray.length(); i++) {
                    jsonObject = jsonArray.getJSONObject(i);
                    DriverLog log;

                    log = new DriverLog(
                            vehicleID,
                            format.parse(jsonObject.optString("log_start_date_time")),
                            format.parse(jsonObject.optString("log_stop_date_time")),
                            jsonObject.optInt("paused_times"),
                            jsonObject.optInt("total_travel_times"),
                            jsonObject.optDouble("km_travel"),
                            jsonObject.optDouble(claim_rate),
                            jsonObject.optString("share_id"),
                            jsonObject.optString(returned_customer_id),
                            jsonObject.optString(company_name),
                            BitmapFactory.decodeByteArray(logoBase64, 0, logoBase64.length),
                            jsonObject.optString("location_log"),
                            jsonObject.optString("timestamp")
                    );

                    logs.add(log);
                }
                if (callbacks != null)
                    callbacks.onSuccess(logs);
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
                if (callbacks != null && message.equals("The vehicle has no drive log for the input company"))
                    callbacks.onError(logs);
                else
                    Toast.makeText(getApplicationContext(), "Network error!", Toast.LENGTH_LONG).show();
            }

        });
        Volley.newRequestQueue(DriverLogActivity.this).add(objectRequest);

    }

    public interface logsCallbacks {
        void onSuccess(@NonNull Set<DriverLog> value);

        void onError(@NonNull Set<DriverLog> value);
    }

    private void returnCompanies(@Nullable final companiesCallbacks callbacks) {

        String URL = IP_HOST + GET_COMPANY_LIST;

        JsonObjectRequest objectRequest = new JsonObjectRequest(Request.Method.GET, URL, null, response -> {
            Log.e("Response: ", response.toString());
            JSONArray jsonArray;
            JSONObject jsonObject;
            Map<String, String> companies = new HashMap<>(); //<ID, Name>
            try {
                jsonArray = response.getJSONArray("company_list");
                for (int i = 0; i < jsonArray.length(); i++) {
                    jsonObject = jsonArray.getJSONObject(i);
                    companies.put(jsonObject.optString("company_name"), jsonObject.optString("cust_id"));
                }
                if (callbacks != null)
                    callbacks.onSuccess(companies);
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
                Log.e("No vehicle: ", message);
                if (callbacks != null)
                    callbacks.onError(message);
            }

        });

        Volley.newRequestQueue(DriverLogActivity.this).add(objectRequest);
    }

    public interface companiesCallbacks {
        void onSuccess(@NonNull Map<String, String> value);

        void onError(@NonNull String errorMessage);
    }

    private static java.util.Date intToDate(int year, int month, int day) {
        StringBuilder sb = new StringBuilder();
        if (day < 10) sb.append("0").append(day);
        else sb.append(day);
        sb.append("/");
        month++;
        if (month < 10) sb.append("0").append(month);
        else sb.append(month);
        sb.append("/").append(year);
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        java.util.Date date = null;
        try {
            date = format.parse(sb.toString());
        } catch (ParseException e) {
            return null;
        }
        return date;
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, ManageVehicleActivity.class).putExtra("vehicleID", vehicleID));
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK) {
            startActivity(new Intent(this, ManageVehicleActivity.class).putExtra("vehicleID", vehicleID));
            return true;    //stop calling super method
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }

}
