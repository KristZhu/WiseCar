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
import android.graphics.drawable.BitmapDrawable;
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
import com.wisecarCompany.wisecarapp.user.profile.AboutActivity;
import com.wisecarCompany.wisecarapp.user.profile.LoginActivity;
import com.wisecarCompany.wisecarapp.user.profile.UpdateProfileActivity;
import com.wisecarCompany.wisecarapp.viewElement.CircleImageView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

public class VehicleActivity extends AppCompatActivity {

    private static final String TAG = "Vehicle";

    private final String IP_HOST = "http://54.206.19.123:3000";
    private final String GET_IMG_EMAIL = "/api/v1/users/";
    private final String GET_VEHICLE_LIST = "/api/v1/vehicles/user/";
    private final String GET_CLOSEST_NOTIFICATIONS = "/api/v1/notification/gettwoclosest";

    private SharedPreferences sp;

    private TextView usernameTextView;
    private TextView userEmailTextView;
    private ImageView userImgImageView;

    private ConstraintLayout imageDiv;
    private ConstraintLayout mainDiv;
    private ImageButton menuImageButton;
    private LinearLayout menuDiv;
    private ConstraintLayout profileDiv;
    private ConstraintLayout devicesDiv;
    private ConstraintLayout aboutDiv;
    private ConstraintLayout logoutDiv;

    private ImageButton settingImageButton;
    private ImageButton editImageButton;
    private ImageButton licenceImageButton;

    private ConstraintLayout dashboardDiv;
    private ConstraintLayout calendarDiv;

    private TextView[] notifyTextView;
    private ImageView notificationImageView;

    private TextView selectedVehicleTextView;
    private ImageView selectedVehicleImageView;
    private LinearLayout vehicleLayout;
    private ImageButton manageVehicleImageButton;   //go to manage vehicle activity to manage THIS vehicle
    private ImageButton addImageButton;
    private ImageButton editVehiclesImageButton;    //pencil, edit all vehicles (delete...) not implemented for now
    //private Map<String, ImageView> vehicleImageViews; //key: registrationNo, value: CircleImageView

    private String user_id;
    private String email_address;
    private String user_name;
    private Bitmap ImgBitmap;

    private Map<String, Vehicle> vehiclesDB;   //vehicle data from db, should update to Userinfo.vehicles

    private SimpleDateFormat displayDateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vehicle);

        usernameTextView = $(R.id.usernameTextView);
        userEmailTextView = $(R.id.userEmailTextView);
        userImgImageView = $(R.id.userImgImageView);

        user_id = UserInfo.getUserID();
        //user_id = "179";
        user_name = UserInfo.getUsername();
        usernameTextView.setText(user_name);

        email_address = UserInfo.getUserEmail();
        ImgBitmap = UserInfo.getUserImg();
        if (email_address == null || ImgBitmap == null) {
            loadUserEmailImg(user_id, new userImageCallback() {
                @Override
                public void onSuccess(@NonNull Bitmap value) {
                    Log.e("image bitmap callback", ImgBitmap.toString());
//                    userImgImageView.setImageDrawable(new BitmapDrawable(getResources(), ImgBitmap));
                    if (ImgBitmap == null)
                        userImgImageView.setImageDrawable(getResources().getDrawable(R.drawable.vehicle0empty_user));
                    else userImgImageView.setImageBitmap(ImgBitmap);
                    UserInfo.setUserImg(ImgBitmap);
                }
            }, new userEmailCallback() {
                @Override
                public void onSuccess(@NonNull String value) {
                    Log.e("email: ", email_address);
                    userEmailTextView.setText(email_address);
                    UserInfo.setUserEmail(email_address);
                }
            });
        } else {
            Log.e("stored image bitmap: ", ImgBitmap.toString());
            Log.e("stored email: ", email_address);
            userEmailTextView.setText(email_address);
            if (UserInfo.getUserImg() == null)
                userImgImageView.setImageDrawable(getResources().getDrawable(R.drawable.vehicle0empty_user));
            else userImgImageView.setImageBitmap(UserInfo.getUserImg());
        }

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
                            sp = this.getSharedPreferences("userInfo", MODE_PRIVATE);
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
                        + UserInfo.getVehicles().get(UserInfo.getCurrLog().getVehicleID()).getRegistration_no()
                        + " is still in process. Please stop it first. ", Toast.LENGTH_LONG).show();
            }
        });

        settingImageButton = $(R.id.settingImageButton);
        editImageButton = $(R.id.editImageButton);
        settingImageButton.setOnClickListener(v -> {
            //to be implemented
        });
        editImageButton.setOnClickListener(v -> {
            //to be implemented
        });

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
            public void onSuccess(@NonNull Map<Date, String[]> value) {

                UserInfo.setEmerNotices(value);

                if (UserInfo.getEmerNotices().size() >= 2) {
                    notificationImageView.setImageDrawable(getResources().getDrawable(R.drawable.vehicle0notification_red));
                    int i = 0;
                    for (Map.Entry<Date, String[]> entry : UserInfo.getEmerNotices().entrySet()) {
                        String temp = "<font color='#ff0000'>" + entry.getValue()[0] + "<br>" + entry.getValue()[1] + " - " + displayDateFormat.format(entry.getKey()) + "</font>";
                        notifyTextView[i++].setText(Html.fromHtml(temp));
                        if (i >= 2) break;
                    }
                } else if (UserInfo.getEmerNotices().size() == 1) {
                    notificationImageView.setImageDrawable(getResources().getDrawable(R.drawable.vehicle0notification_red));
                    for (Map.Entry<Date, String[]> entry : UserInfo.getEmerNotices().entrySet()) {
                        String temp = "<font color='#ff0000'>" + entry.getValue()[0] + "<br>" + entry.getValue()[1] + " - " + displayDateFormat.format(entry.getKey()) + "</font>";
                        notifyTextView[0].setText(Html.fromHtml(temp));
                        break;
                    }
                    for (Map.Entry<Date, String[]> entry : UserInfo.getNotices().entrySet()) {
                        String temp = "<font color='#0c450c'>" + entry.getValue()[0] + "<br>" + entry.getValue()[1] + " - " + displayDateFormat.format(entry.getKey()) + "</font>";
                        notifyTextView[1].setText(Html.fromHtml(temp));
                        break;
                    }
                } else {
                    notificationImageView.setImageDrawable(getResources().getDrawable(R.drawable.vehicle0notification));
                    int i = 0;
                    for (Map.Entry<Date, String[]> entry : UserInfo.getNotices().entrySet()) {
                        String temp = "<font color='#0c450c'>" + entry.getValue()[0] + "<br>" + entry.getValue()[1] + " - " + displayDateFormat.format(entry.getKey()) + "</font>";
                        notifyTextView[i++].setText(Html.fromHtml(temp));
                        if (i >= 2) break;
                    }
                }
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
        editVehiclesImageButton = $(R.id.editVehiclesImageButton);

        vehiclesDB = new TreeMap<>((o1, o2) -> o2.compareTo(o1));

        //get vehicle data from db and store locally only when there is no vehicle locally
        //it is because this user either just log in or really has no vehicles
        //every time the user adds a new vehicle, it will add to local, and upload to db. so no need to get data from db later.
        if (UserInfo.getVehicles() == null || UserInfo.getVehicles().size() == 0) {
            returnVehicles(user_id, new vehicleMapCallbacks() {
                @SuppressLint("SetTextI18n")
                @Override
                public void onSuccess(@NonNull Map<String, Vehicle> value) {
                    UserInfo.setVehicles(vehiclesDB);
                    Log.d(TAG, "vehicle DB: " + vehiclesDB);

                    if (vehiclesDB.size() == 0) {
                        selectedVehicleTextView.setText("No Vehicle");
                        selectedVehicleImageView.setImageDrawable(getResources().getDrawable(R.drawable.vehicle0empty_vehicle));
                    } else {
                        showVehicles(vehiclesDB);
                    }
                }

                @Override
                public void onError(@NonNull String errorMessage) {
                    Log.e("return vehicles error: ", errorMessage);
                }

            });
        } else {
            Log.d(TAG, "vehicle local: " + UserInfo.getVehicles());
            showVehicles(UserInfo.getVehicles());
        }


        addImageButton.setOnClickListener(v -> {
            if (menuDiv.getVisibility() == View.VISIBLE) menuDiv.setVisibility(View.GONE);
            else addVehicle();
        });

        editVehiclesImageButton.setOnClickListener(v -> {
            if (menuDiv.getVisibility() == View.VISIBLE) menuDiv.setVisibility(View.GONE);
            else ;
            //to be implementer
        });

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
                    + UserInfo.getVehicles().get(UserInfo.getCurrLog().getVehicleID()).getRegistration_no()
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
                        + UserInfo.getVehicles().get(UserInfo.getCurrLog().getVehicleID()).getRegistration_no()
                        + " is still in process. Please stop it first. ", Toast.LENGTH_LONG).show();
            }
            return true;    //stop calling super method
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }

    private void showVehicles(Map<String, Vehicle> vehicles) {
        assert vehicles.size() > 0;

        //vehicleImageViews = new HashMap<>();

        //default show the first vehicle (latest added, sorted by TreeMap)
        for (String vehicleID : vehicles.keySet()) {
            selectedVehicleTextView.setText(vehicles.get(vehicleID).getMake_name() + " - " + vehicles.get(vehicleID).getRegistration_no());
            if (vehicles.get(vehicleID).getImage() == null)
                selectedVehicleImageView.setImageDrawable(getResources().getDrawable(R.drawable.wc0blank_white_circle));
            else selectedVehicleImageView.setImageBitmap(vehicles.get(vehicleID).getImage());
            manageVehicleImageButton.setOnClickListener(v -> manageVehicle(vehicleID));
            break;
        }

        for (String vehicleID : vehicles.keySet()) {
            Vehicle vehicle = vehicles.get(vehicleID);
            assert vehicle != null;
            CircleImageView imageView = new CircleImageView(VehicleActivity.this);
            if (vehicle.getImage() == null)
                imageView.setImageDrawable(getResources().getDrawable(R.drawable.wc0blank_white_circle));
            else imageView.setImageBitmap(vehicle.getImage());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            params.setMargins(0, 0, 16, 0);
            imageView.setLayoutParams(params);
            vehicleLayout.addView(imageView);
            imageView.setOnClickListener(v -> {
                Log.d(TAG, "onClickVehicle: " + vehicle);
                selectedVehicleTextView.setText(vehicles.get(vehicleID).getMake_name() + " - " + vehicles.get(vehicleID).getRegistration_no());
                if (vehicle.getImage() == null)
                    selectedVehicleImageView.setImageDrawable(getResources().getDrawable(R.drawable.wc0blank_white_circle));
                else selectedVehicleImageView.setImageBitmap(vehicle.getImage());
                manageVehicleImageButton.setOnClickListener(v1 -> {
                    if (menuDiv.getVisibility() == View.VISIBLE) menuDiv.setVisibility(View.GONE);
                    else manageVehicle(vehicleID);
                });
            });
            //vehicleImageViews.put(vehicles.get(vehicleID).getRegistration_no(), imageView);
        }
    }

    private void manageVehicle(String vehicleID) {
        Log.d(TAG, "manageVehicleID: " + vehicleID);
        //the new added vehicle does not have ID locally. so its id = "a" (temp)
        //if the user wants to edit, the id must be synchronized with db
        returnVehicles(user_id, new vehicleMapCallbacks() {
            @Override
            public void onSuccess(@NonNull Map<String, Vehicle> value) {
                UserInfo.setVehicles(vehiclesDB);
                Log.d(TAG, "editVehicle, vehicle DB: " + vehiclesDB);

            }

            @Override
            public void onError(@NonNull String errorMessage) {
                Log.e("return vehicles error: ", errorMessage);
            }

        });

        Log.d(TAG, "editVehicle, finalVehicleID: " + vehicleID);
        Intent intent = new Intent(VehicleActivity.this, ManageVehicleActivity.class);
        intent.putExtra("vehicleID", vehicleID);
        startActivity(intent);
    }

    private void addVehicle() {
        Log.d(TAG, "addVehicle: ");
        returnVehicles(user_id, new vehicleMapCallbacks() {
            @Override
            public void onSuccess(@NonNull Map<String, Vehicle> value) {
                UserInfo.setVehicles(vehiclesDB);
                Log.d(TAG, "addVehicle, vehicle DB: " + vehiclesDB);
            }

            @Override
            public void onError(@NonNull String errorMessage) {
                Log.e("return vehicles error: ", errorMessage);
            }

        });
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

    private void startInbox() {

    }

    private void loadUserEmailImg(String user_id, @Nullable final userImageCallback imageCallback, @Nullable final userEmailCallback emailCallback) {
        String URL = IP_HOST + GET_IMG_EMAIL + user_id;

        JsonObjectRequest objectRequest = new JsonObjectRequest(Request.Method.GET, URL, null, response -> {
            Log.e("Response", response.toString());
            byte[] logoBase64 = Base64.decode(response.optString("logo"), Base64.DEFAULT);
            ImgBitmap = BitmapFactory.decodeByteArray(logoBase64, 0, logoBase64.length);
            Log.e("image bitmap method: ", ImgBitmap == null ? "null img" : ImgBitmap.toString());
            email_address = response.optString("email_address");
            if (ImgBitmap == null) {
                Log.e("No image: ", "this user has no image");
            }
            if (ImgBitmap != null)
                imageCallback.onSuccess(ImgBitmap);
            if (emailCallback != null)
                emailCallback.onSuccess(email_address);
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

        Volley.newRequestQueue(VehicleActivity.this).add(objectRequest);
    }

    public interface userImageCallback {
        void onSuccess(@NonNull Bitmap value);

//        void onError(@NonNull String error);
    }


    public interface userEmailCallback {
        void onSuccess(@NonNull String value);

//        void onError(@NonNull Throwable throwable);
    }

    private void returnVehicles(String user_id, @Nullable final vehicleMapCallbacks callbacks) {

        String URL = IP_HOST + GET_VEHICLE_LIST + user_id;

        JsonObjectRequest objectRequest = new JsonObjectRequest(Request.Method.GET, URL, null, response -> {
            Log.e("Response: ", response.toString());
            JSONArray jsonArray;
            JSONObject jsonObject;
            try {
                jsonArray = response.getJSONArray("vehicle_list");
                for (int i = 0; i < jsonArray.length(); i++) {
                    jsonObject = jsonArray.getJSONObject(i);

                    Vehicle vehicle = new Vehicle();

                    vehicle.setRegistration_no(jsonObject.optString("registration_no"));
                    vehicle.setMake_name(jsonObject.optString("make_name"));
                    vehicle.setModel_name(jsonObject.optString("model_name"));
                    vehicle.setMake_year(jsonObject.optString("make_year"));
                    vehicle.setDescription(jsonObject.optString("description"));
                    vehicle.setUser_id(jsonObject.optInt("user_id"));
                    vehicle.setUser_name(jsonObject.optString("user_name"));
                    vehicle.setImage(jsonObject.optString("image"));
                    vehicle.setState_name(jsonObject.optString("state_name"));
                    vehicle.setVehicle_id(jsonObject.optString("vehicle_id"));

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

    private <T extends View> T $(int id) {
        return (T) findViewById(id);
    }

    private void getTwoClosestNotifications(@Nullable final notificationsCallbacks callbacks) {

        String URL = IP_HOST + GET_CLOSEST_NOTIFICATIONS;
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

        Volley.newRequestQueue(VehicleActivity.this).add(objectRequest);
    }

    public interface notificationsCallbacks {
        void onSuccess(@NonNull Map<Date, String[]> value);

        void onError(@NonNull String errorMessage);
    }
}
