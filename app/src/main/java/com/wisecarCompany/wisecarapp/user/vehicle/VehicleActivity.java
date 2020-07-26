package com.wisecarCompany.wisecarapp.user.vehicle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

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
import android.widget.Button;
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
import com.wisecarCompany.wisecarapp.user.login.LoginActivity;
import com.wisecarCompany.wisecarapp.viewElement.CircleImageView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

public class VehicleActivity extends AppCompatActivity {

    private static final String TAG = "Vehicle";

    private final String IP_HOST = "http://54.206.19.123:3000";
    private final String GET_IMG_EMAIL = "/api/v1/users/";
    private final String GET_VEHICLE_LIST = "/api/v1/vehicles/user/";

    private SharedPreferences sp;

    private TextView usernameTextView;
    private TextView userEmailTextView;
    private ImageView userImgImageView;

    private ImageButton backImageButton;
    private ImageButton settingImageButton;
    private ImageButton editImageButton;
    private ImageButton licenceImageButton;

    private ImageButton dashboardImageButton;
    private Button dashboardButton;
    private ImageButton calendarImageButton;
    private Button calendarButton;

    private TextView notifyTextView;
    Date registrationDue;
    Date nextService;

    private Button selectedVehicleTextView; //use button type only to make the format same as others. it is not clickable
    private ImageView selectedVehicleImageView;
    private LinearLayout vehicleLayout;
    private ImageButton editVehicleImageButton;
    private ImageButton addImageButton;
    private ImageButton manageImageButton;
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
                    userImgImageView.setImageBitmap(ImgBitmap);
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
            userImgImageView.setImageDrawable(new BitmapDrawable(getResources(), ImgBitmap));
        }

        backImageButton = $(R.id.backImageButton);
        backImageButton.setOnClickListener(v -> {
/*
            final String[] ways = new String[]{"Yes", "No"};
            AlertDialog alertDialog = new AlertDialog.Builder(VehicleActivity.this)
                    .setTitle("Are you sure you want to log out? ")
                    .setIcon(R.mipmap.ic_launcher)
                    .setItems(ways, (dialogInterface, i) -> {
                        Log.d(TAG, "onClick: " + ways[i]);
                        if (i == 0) {  //log out
                            UserInfo.clear();
                            startActivity(new Intent(VehicleActivity.this, LoginActivity.class));
                        } else {
                            //cancel
                        }
                    }).create();
            alertDialog.show();
*/
            if(UserInfo.getCurrLog()==null) {
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
                        .setNegativeButton("Cancel", (dialog, which) -> {

                        }).create();
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
        licenceImageButton.setOnClickListener(v -> startActivity(new Intent(VehicleActivity.this, LicenceActivity.class)));

        dashboardImageButton = $(R.id.dashboardImageButton);
        dashboardButton = $(R.id.dashboardButton);
        dashboardImageButton.setOnClickListener(v -> startDashboard());
        dashboardButton.setOnClickListener(v -> startDashboard());

        calendarImageButton = $(R.id.calendarImageButton);
        calendarButton = $(R.id.calendarButton);
        calendarImageButton.setOnClickListener(v -> startCalendar());
        calendarButton.setOnClickListener(v -> startCalendar());

        notifyTextView = $(R.id.notifyTextView);


        registrationDue = new Date();   //get from DB
        nextService = new Date();    //get from DB


        //SimpleDateFormat format = new SimpleDateFormat("ddMMM yyyy", Locale.getDefault());
        String temp = "<font color='#612a00'>Registration Due - " + displayDateFormat.format(registrationDue) + "</font><br/>"
                + "<font color='#003c00'>Next Service - " + displayDateFormat.format(nextService) + "</font>";
        notifyTextView.setText(Html.fromHtml(temp));
        Log.d(TAG, "notify: " + notifyTextView.getText());

        selectedVehicleTextView = $(R.id.selectedVehicleTextView);
        selectedVehicleImageView = $(R.id.selectedVehicleImageView);
        vehicleLayout = $(R.id.vehicleLayout);
        editVehicleImageButton = $(R.id.editVehicleImageButton);
        addImageButton = $(R.id.addImageButton);
        manageImageButton = $(R.id.manageImageButton);

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


        addImageButton.setOnClickListener(v -> addVehicle());

        manageImageButton.setOnClickListener(v -> {
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
        if(UserInfo.getCurrLog()==null) {
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
        if(keyCode == KeyEvent.KEYCODE_BACK) {
            if(UserInfo.getCurrLog()==null) {
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

    private void startDashboard() {
        startActivity(new Intent(this, DashboardActivity.class));
    }

    private void startCalendar() {

    }

    private void showVehicles(Map<String, Vehicle> vehicles) {
        assert vehicles.size() > 0;

        //vehicleImageViews = new HashMap<>();

        //default show the first vehicle (latest added, sorted by TreeMap)
        for (String vehicleID : vehicles.keySet()) {
            selectedVehicleTextView.setText(vehicles.get(vehicleID).getMake_name() + " - " + vehicles.get(vehicleID).getRegistration_no());
            selectedVehicleImageView.setImageBitmap(vehicles.get(vehicleID).getImage());
            editVehicleImageButton.setOnClickListener(v -> editVehicle(vehicleID));
            break;
        }

        for (String vehicleID : vehicles.keySet()) {
            Vehicle vehicle = vehicles.get(vehicleID);
            CircleImageView imageView = new CircleImageView(VehicleActivity.this);
            imageView.setImageBitmap(vehicle.getImage());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            params.setMargins(0, 0, 16, 0);
            imageView.setLayoutParams(params);
            vehicleLayout.addView(imageView);
            imageView.setOnClickListener(v -> {
                Log.d(TAG, "onClickVehicle: " + vehicle);
                selectedVehicleTextView.setText(vehicles.get(vehicleID).getMake_name() + " - " + vehicles.get(vehicleID).getRegistration_no());
                selectedVehicleImageView.setImageBitmap(vehicle.getImage());
                editVehicleImageButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        editVehicle(vehicleID);
                    }
                });
            });
            //vehicleImageViews.put(vehicles.get(vehicleID).getRegistration_no(), imageView);
        }
    }

    private void editVehicle(String vehicleID) {
        Log.d(TAG, "editVehicleID: " + vehicleID);
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
}
