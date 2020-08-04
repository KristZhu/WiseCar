package com.wisecarCompany.wisecarapp.user.vehicle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.wisecarCompany.wisecarapp.user.UserInfo;
import com.wisecarCompany.wisecarapp.user.licence.LicenceActivity;
import com.wisecarCompany.wisecarapp.R;
import com.wisecarCompany.wisecarapp.user.introduction.AboutActivity;
import com.wisecarCompany.wisecarapp.user.profile.LoginActivity;
import com.wisecarCompany.wisecarapp.user.profile.UpdateProfileActivity;
import com.wisecarCompany.wisecarapp.element.CircleImageView;

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

public class VehicleActivity extends AppCompatActivity {

    private static final String TAG = "Vehicle";

    private final String IP_HOST = "http://54.206.19.123:3000";
    private final String GET_IMG = "/api/v1/users/";
    private final String GET_VEHICLE_LIST = "/api/v1/vehicles/user/";
    private final String GET_CLOSEST_NOTIFICATIONS = "/api/v1/notification/gettwoclosest";

    private SharedPreferences sp;
    private String userID;
    private String username;

    private ImageButton menuImageButton;
    private LinearLayout menuDiv;
    private ConstraintLayout profileDiv;
    private ConstraintLayout devicesDiv;
    private ConstraintLayout aboutDiv;
    private ConstraintLayout logoutDiv;

    private TextView usernameTextView;
    //private TextView userEmailTextView;  //depreciated
    private ImageView userImgImageView;

    //private ImageButton settingImageButton;   //depreciated
    //private ImageButton editImageButton;      //depreciated
    private ImageButton licenceImageButton;

    private ConstraintLayout imageDiv;
    private ConstraintLayout mainDiv;

    private ConstraintLayout dashboardDiv;
    private ConstraintLayout calendarDiv;

    private TextView[] notifyTextView;
    private ImageView notificationImageView;

    private TextView selectedVehicleTextView;
    private ImageView selectedVehicleImageView;
    private LinearLayout vehicleLayout;
    private ImageButton manageVehicleImageButton;   //go to manage vehicle activity to manage THIS vehicle
    private ImageButton addImageButton;
    //private ImageButton editVehiclesImageButton;    //pencil, edit all vehicles (delete...) not implemented for now
    //private Map<String, ImageView> vehicleImageViews; //key: registrationNo, value: CircleImageView

    private final SimpleDateFormat displayDateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());


    @SuppressLint("ShowToast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vehicle);

        sp = this.getSharedPreferences("userInfo", MODE_PRIVATE);
        userID = sp.getString("USER_ID", "");
        username = sp.getString("USERNAME", "");
        Log.d(TAG, "userID: " + userID);
        Log.d(TAG, "username: " + username);

        userImgImageView = $(R.id.userImgImageView);
        usernameTextView = $(R.id.usernameTextView);
        //userEmailTextView = $(R.id.userEmailTextView);

        usernameTextView.setText(username);
        loadUserImg(new userImageCallback() {
            @Override
            public void onSuccess(@NonNull Bitmap value) {  //if no user image, it will not return onSuccess
                userImgImageView.setImageBitmap(value);
            }
        });

        menuImageButton = $(R.id.menuImageButton);
        menuDiv = $(R.id.menuDiv);
        profileDiv = $(R.id.profileDiv);
        devicesDiv = $(R.id.devicesDiv);
        aboutDiv = $(R.id.aboutDiv);
        logoutDiv = $(R.id.logoutDiv);

        imageDiv = $(R.id.imageDiv);
        mainDiv = $(R.id.mainDiv);
        menuImageButton.setOnClickListener(v -> {
            if (menuDiv.getVisibility() == View.VISIBLE) {
                menuDiv.setVisibility(View.GONE);
            } else {
                menuDiv.setVisibility(View.VISIBLE);
                imageDiv.setOnClickListener(v1 -> menuDiv.setVisibility(View.GONE));
                mainDiv.setOnClickListener(v1 -> menuDiv.setVisibility(View.GONE));
            }
        });

        profileDiv.setOnClickListener(v -> startActivity(new Intent(this, UpdateProfileActivity.class)));
        devicesDiv.setOnClickListener(v -> Toast.makeText(this, "This function is not available now", Toast.LENGTH_SHORT));
        aboutDiv.setOnClickListener(v -> startActivity(new Intent(this, AboutActivity.class)));

        logoutDiv.setOnClickListener(v -> {
            if (UserInfo.getCurrLog() == null) {
                AlertDialog alertDialog = new AlertDialog.Builder(this)
                        .setTitle("Are you sure you want to log out? ")
                        .setPositiveButton("OK", (dialog, which) -> {
                            UserInfo.clear();
                            SharedPreferences.Editor editor = sp.edit()
                                    .putBoolean("AUTO_LOGIN", false);
                            editor.commit();
                            startActivity(new Intent(VehicleActivity.this, LoginActivity.class));
                        })
                        .setNegativeButton("Cancel", null)
                        .create();
                alertDialog.show();
            } else {
                Toast.makeText(this, "Driver Log for Vehicle "
                        + UserInfo.getCurrLog().getVehicle().getRegistration_no()
                        + " is still in process. Please end it first. ", Toast.LENGTH_SHORT).show();
            }
        });

/*
        settingImageButton = $(R.id.settingImageButton);
        editImageButton = $(R.id.editImageButton);
        settingImageButton.setOnClickListener(v -> {

        });
        editImageButton.setOnClickListener(v -> {

        });
*/

        licenceImageButton = $(R.id.licenceImageButton);
        licenceImageButton.setOnClickListener(v -> {
            if (menuDiv.getVisibility() == View.VISIBLE) menuDiv.setVisibility(View.GONE);
            else startActivity(new Intent(VehicleActivity.this, LicenceActivity.class));
        });

        dashboardDiv = $(R.id.dashboardDiv);
        dashboardDiv.setOnClickListener(v -> {
            if (menuDiv.getVisibility() == View.VISIBLE) menuDiv.setVisibility(View.GONE);
            else startActivity(new Intent(this, DashboardActivity.class));
        });

        calendarDiv = $(R.id.calendarDiv);
        calendarDiv.setOnClickListener(v -> {
            if (menuDiv.getVisibility() == View.VISIBLE) menuDiv.setVisibility(View.GONE);
            else startActivity(new Intent(this, CalendarActivity.class));
        });

        notifyTextView = new TextView[]{$(R.id.notifyTextView0), $(R.id.notifyTextView1)};
        notificationImageView = $(R.id.notificationImageView);

        getTwoClosestNotifications(new notificationsCallbacks() {
            @Override
            public void onSuccess(@NonNull Map<Date, List<String[]>> value) {

                boolean containEmergency = false;
                int i = 0;
                for(Date date: value.keySet()) {
                    for(String[] contents: value.get(date)) {
                        boolean isEmergent;
                        try{
                            isEmergent = Integer.parseInt(contents[2])<=7;
                        } catch (NumberFormatException e) {
                            Calendar currDateCal = Calendar.getInstance();
                            currDateCal.set(currDateCal.get(Calendar.YEAR), currDateCal.get(Calendar.MONTH), currDateCal.get(Calendar.DAY_OF_MONTH),0, 0, 0);
                            isEmergent = Math.abs(currDateCal.getTime().getTime() - date.getTime()) <= 7*24*60*60*1000;
                        }
                        String temp;
                        if(isEmergent) {
                            containEmergency = true;
                            temp = "<font color='#ff0000'>" + contents[0] + "<br>" + contents[1] + " - " + displayDateFormat.format(date) + "</font>";
                        } else {
                            temp = "<font color='#0c450c'>" + contents[0] + "<br>" + contents[1] + " - " + displayDateFormat.format(date) + "</font>";
                        }
                        notifyTextView[i++].setText(Html.fromHtml(temp));
                    }
                }
                if(containEmergency) notificationImageView.setImageDrawable(getResources().getDrawable(R.drawable.vehicle0notification_red));
                else notificationImageView.setImageDrawable(getResources().getDrawable(R.drawable.vehicle0notification));

            }

            @Override
            public void onError(@NonNull String errorMessage) {

            }
        });

        selectedVehicleTextView = $(R.id.selectedVehicleTextView);
        selectedVehicleImageView = $(R.id.selectedVehicleImageView);
        vehicleLayout = $(R.id.vehicleLayout);
        manageVehicleImageButton = $(R.id.manageVehicleImageButton);
        addImageButton = $(R.id.addImageButton);
        //editVehiclesImageButton = $(R.id.editVehiclesImageButton);

        returnVehicles(new vehicleMapCallbacks() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onSuccess(@NonNull Map<String, Vehicle> vehiclesDB) {
                //UserInfo.setVehicles(vehiclesDB);
                Log.d(TAG, "vehicle DB: " + vehiclesDB);

                showVehicles(vehiclesDB);
            }

            @Override
            public void onError(@NonNull String errorMessage) {
                Log.e("return vehicles error: ", errorMessage);
            }

        });


        addImageButton.setOnClickListener(v -> {
            if (menuDiv.getVisibility() == View.VISIBLE) menuDiv.setVisibility(View.GONE);
            else addVehicle();
        });

/*
        editVehiclesImageButton.setOnClickListener(v -> {
            if (menuDiv.getVisibility() == View.VISIBLE) menuDiv.setVisibility(View.GONE);
            else ;
            //to be implementer
        });
*/

    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void exitAPP() {
        ActivityManager activityManager = (ActivityManager) getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.AppTask> appTaskList = activityManager.getAppTasks();
        for (ActivityManager.AppTask appTask : appTaskList) {
            appTask.finishAndRemoveTask();
        }
//        appTaskList.get(0).finishAndRemoveTask();
        System.exit(0);
    }


    @Override
    public void onBackPressed() {
        if (UserInfo.getCurrLog() == null) {
            AlertDialog alertDialog = new AlertDialog.Builder(this)
                    .setTitle("Are you sure you want to exit? ")
                    .setPositiveButton("OK", (dialog, which) -> {
                        UserInfo.clear();
                        exitAPP();
                    }).setNegativeButton("Cancel", (dialog, which) -> {

                    }).create();
            alertDialog.show();
        } else {
            Toast.makeText(this, "Driver Log for Vehicle "
                    + UserInfo.getCurrLog().getVehicle().getRegistration_no()
                    + " is still in process. Please stop it first. ", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (UserInfo.getCurrLog() == null) {
                AlertDialog alertDialog = new AlertDialog.Builder(this)
                        .setTitle("Are you sure you want to exit? ")
                        .setPositiveButton("OK", (dialog, which) -> {
                            UserInfo.clear();
                            exitAPP();
                        }).setNegativeButton("Cancel", (dialog, which) -> {

                        }).create();
                alertDialog.show();
            } else {
                Toast.makeText(this, "Driver Log for Vehicle "
                        + UserInfo.getCurrLog().getVehicle().getRegistration_no()
                        + " is still in process. Please stop it first. ", Toast.LENGTH_LONG).show();
            }
            return true;    //stop calling super method
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }

    @SuppressLint("SetTextI18n")
    private void showVehicles(Map<String, Vehicle> vehicles) {
        //vehicleImageViews = new HashMap<>();
        if(vehicles.size()==0 && UserInfo.getNewVehicle()==null) {  //totally new user
            selectedVehicleTextView.setText("No Vehicle");
            selectedVehicleImageView.setImageDrawable(getResources().getDrawable(R.drawable.vehicle0empty_vehicle));
        } else if(vehicles.size()==0) { //just finish adding the first vehicle
            showSelectedVehicle(UserInfo.getNewVehicle());  //show the newly added vehicle as selected
            showVehicleInScrollView(UserInfo.getNewVehicle(), null);
            //vehicleImageViews.put(vehicles.get(vehicleID), imageView);
        } else if(UserInfo.getNewVehicle()==null) { //has vehicles, return from other activities, not add
            for (Map.Entry<String, Vehicle> vehicleEntry: vehicles.entrySet()) {    //show the first vehicle in vehicles map as selected
                showSelectedVehicle(vehicleEntry.getValue());
                break;
            }
            for (Map.Entry<String, Vehicle> vehicleEntry: vehicles.entrySet()) {    //show all vehicles
                showVehicleInScrollView(vehicleEntry.getValue(), null);
                //vehicleImageViews.put(vehicles.get(vehicleID), imageView);
            }
        } else {    //has vehicle, and just added a new one
            showSelectedVehicle(UserInfo.getNewVehicle());  //show the newly added vehicle as selected
            showVehicleInScrollView(UserInfo.getNewVehicle(), null);
            //vehicleImageViews.put(UserInfo.getNewVehicle.get(vehicleID), imageView);
            for (Map.Entry<String, Vehicle> vehicleEntry: vehicles.entrySet()) {    //show all vehicles
                showVehicleInScrollView(vehicleEntry.getValue(), UserInfo.getNewVehicle().getRegistration_no());
                //vehicleImageViews.put(vehicles.get(vehicleID), imageView);
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private void showSelectedVehicle(Vehicle vehicle) {
        selectedVehicleTextView.setText(vehicle.getMake_name() + " - " + vehicle.getRegistration_no());
        if (vehicle.getImage() == null) selectedVehicleImageView.setImageDrawable(getResources().getDrawable(R.drawable.blank_white_circle));
        else selectedVehicleImageView.setImageBitmap(vehicle.getImage());
        manageVehicleImageButton.setOnClickListener(v -> {
            if (menuDiv.getVisibility() == View.VISIBLE) menuDiv.setVisibility(View.GONE);
            else manageVehicle(vehicle);
        });
    }

    private void showVehicleInScrollView(Vehicle vehicle, String alreadyShownNewAddedVehicleRegNo) {
        if(vehicle.getRegistration_no().equals(alreadyShownNewAddedVehicleRegNo)) return;

        CircleImageView imageView = new CircleImageView(VehicleActivity.this);
        if (vehicle.getImage() == null) imageView.setImageDrawable(getResources().getDrawable(R.drawable.blank_white_circle));
        else imageView.setImageBitmap(vehicle.getImage());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        params.setMargins(0, 0, 16, 0);
        imageView.setLayoutParams(params);
        vehicleLayout.addView(imageView);
        imageView.setOnClickListener(v -> {
            Log.d(TAG, "onClickVehicleID: " + vehicle.getVehicle_id());
            showSelectedVehicle(vehicle);
        });
    }

    private void manageVehicle(Vehicle vehicle) {
        Log.d(TAG, "manageVehicle: " + vehicle);
        UserInfo.setCurrVehicle(vehicle);
        startActivity(new Intent(VehicleActivity.this, ManageVehicleActivity.class));
    }

    private void addVehicle() {
        Log.d(TAG, "addVehicle");
        startActivity(new Intent(VehicleActivity.this, AddVehicleActivity.class));
    }

/*
    private void manageVehicle() {
        Log.d(TAG, "manageVehicle: " + vehicleImageViews);
        if (vehicleImageViews.size() > 0) {

        } else {

        }
    }
 */

    private void loadUserImg(@Nullable final userImageCallback imageCallback) {
        String URL = IP_HOST + GET_IMG + userID;

        JsonObjectRequest objectRequest = new JsonObjectRequest(Request.Method.GET, URL, null, response -> {
            Log.e("Response", response.toString());
            byte[] logoBase64 = Base64.decode(response.optString("logo"), Base64.DEFAULT);
            Bitmap ImgBitmap = BitmapFactory.decodeByteArray(logoBase64, 0, logoBase64.length);
            Log.e("image bitmap method: ", ImgBitmap == null ? "null img" : ImgBitmap.toString());
            if (ImgBitmap == null) {
                Log.e("No image: ", "this user has no image");
            }
            if (ImgBitmap != null)
                imageCallback.onSuccess(ImgBitmap);
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
            }
        });

        Volley.newRequestQueue(this).add(objectRequest);
    }

    public interface userImageCallback {
        void onSuccess(@NonNull Bitmap value);

//        void onError(@NonNull String error);
    }

    private void returnVehicles(@Nullable final vehicleMapCallbacks callbacks) {

        String URL = IP_HOST + GET_VEHICLE_LIST + userID;

        JsonObjectRequest objectRequest = new JsonObjectRequest(Request.Method.GET, URL, null, response -> {
            Log.e("Response: ", response.toString());
            JSONArray jsonArray;
            JSONObject jsonObject;

            Map<String, Vehicle> vehiclesDB = new TreeMap<>((o1, o2) -> o2.compareTo(o1));;   //vehicle data from db, should update to Userinfo.vehicles

            try {
                jsonArray = response.getJSONArray("vehicle_list");
                for (int i = 0; i < jsonArray.length(); i++) {
                    jsonObject = jsonArray.getJSONObject(i);

                    byte[] logoBase64 = Base64.decode(jsonObject.optString("image"), Base64.DEFAULT);
                    Bitmap imgBitmap = BitmapFactory.decodeByteArray(logoBase64, 0, logoBase64.length);

                    Vehicle vehicle = new Vehicle(
                            jsonObject.optString("vehicle_id"),
                            jsonObject.optString("registration_no"),
                            jsonObject.optString("make_name"),
                            jsonObject.optString("model_name"),
                            jsonObject.optString("make_year"),
                            jsonObject.optString("description"),
                            jsonObject.optString("user_id"),
                            jsonObject.optString("user_name"),
                            imgBitmap,
                            jsonObject.optString("state_name")
                    );
/*
                    vehicle.setRegistration_no(jsonObject.optString("registration_no"));
                    vehicle.setMake_name(jsonObject.optString("make_name"));
                    vehicle.setModel_name(jsonObject.optString("model_name"));
                    vehicle.setMake_year(jsonObject.optString("make_year"));
                    vehicle.setDescription(jsonObject.optString("description"));
                    vehicle.setUser_id(jsonObject.optString("user_id"));
                    vehicle.setUser_name(jsonObject.optString("user_name"));
                    //vehicle.setImage(jsonObject.optString("image"));
                    vehicle.setImage(imgBitmap);
                    vehicle.setState_name(jsonObject.optString("state_name"));
                    vehicle.setVehicle_id(jsonObject.optString("vehicle_id"));
*/

                    vehiclesDB.put(vehicle.getVehicle_id(), vehicle);

                }
                if (callbacks != null)
                    callbacks.onSuccess(vehiclesDB);
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

        Volley.newRequestQueue(VehicleActivity.this).add(objectRequest);
    }

    public interface vehicleMapCallbacks {
        void onSuccess(@NonNull Map<String, Vehicle> value);

        void onError(@NonNull String errorMessage);
    }

    private void getTwoClosestNotifications(@Nullable final notificationsCallbacks callbacks) {

        String URL = IP_HOST + GET_CLOSEST_NOTIFICATIONS;
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

        Volley.newRequestQueue(VehicleActivity.this).add(objectRequest);
    }

    public interface notificationsCallbacks {
        void onSuccess(@NonNull Map<Date, List<String[]>> value);

        void onError(@NonNull String errorMessage);
    }


    private <T extends View> T $(int id) {
        return (T) findViewById(id);
    }
}
