package com.example.wisecarapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
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
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VehicleActivity extends AppCompatActivity {

    private static final String TAG = "Vehicle";

    private TextView usernameTextView;
    private TextView userEmailTextView;
    private ImageView userImgImageView;

    private ImageButton backImageButton;
    private ImageButton settingImageButton;
    private ImageButton editImageButton;
    private ImageButton dashboardImageButton;
    private Button dashboardButton;
    private ImageButton calendarImageButton;
    private Button calendarButton;
    private ImageButton inboxImageButton;
    private Button inboxButton;

    private Button selectedVehicleTextView; //use button type only to make the format same as others. it is not clickable
    private ImageView selectedVehicleImageView;
    private LinearLayout vehicleLayout;
    private ImageButton editVehicleImageButton;
    private ImageButton addImageButton;
    private ImageButton manageImageButton;
    private Map<String, ImageView> vehicleImageViews; //key: registrationNo, value: CircleImageView

    private String email_address;
    private String user_name;
    private Bitmap ImgBitmap;
    private List<Vehicle> user_Vehicles;

    private final String IP_HOST = "http://54.206.19.123:3000";
    private final String GET_IMG_EMAIL = "/api/v1/users/";
    private final String GET_VEHICLE_LIST = "/api/v1/vehicles/user/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vehicle);

        usernameTextView = (TextView) findViewById(R.id.usernameTextView);
        userEmailTextView = (TextView) findViewById(R.id.userEmailTextView);
        userImgImageView = (ImageView) findViewById(R.id.userImgImageView);

        user_name = UserInfo.getUsername();
        usernameTextView.setText(user_name);

        loadUserEmailImg("179", new userImageCallback() {

            @Override
            public void onSuccess(@NonNull Bitmap value) {
                Log.e("image bitmap: ", ImgBitmap.toString());
                userImgImageView.setImageDrawable(new BitmapDrawable(getResources(), ImgBitmap));
            }
        }, new userEmailCallback() {
            @Override
            public void onSuccess(@NonNull String value) {
                Log.e("email: ", email_address);
                userEmailTextView.setText(email_address);
            }
        });

        backImageButton = (ImageButton) findViewById(R.id.backImageButton);
        backImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        settingImageButton = (ImageButton) findViewById(R.id.settingImageButton);
        editImageButton = (ImageButton) findViewById(R.id.editImageButton);

        dashboardImageButton = (ImageButton) findViewById(R.id.dashboardImageButton);
        dashboardButton = (Button) findViewById(R.id.dashboardButton);
        dashboardImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startDashboard();
            }
        });
        dashboardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startDashboard();
            }
        });

        calendarImageButton = (ImageButton) findViewById(R.id.calendarImageButton);
        calendarButton = (Button) findViewById(R.id.calendarButton);
        calendarImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startCalendar();
            }
        });
        calendarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startCalendar();
            }
        });



        selectedVehicleTextView = (Button) findViewById(R.id.selectedVehicleTextView);
        selectedVehicleImageView = (ImageView) findViewById(R.id.selectedVehicleImageView);
        vehicleLayout = (LinearLayout) findViewById(R.id.vehicleLayout);
        editVehicleImageButton = (ImageButton) findViewById(R.id.editVehicleImageButton);
        addImageButton = (ImageButton) findViewById(R.id.addImageButton);
        manageImageButton = (ImageButton) findViewById(R.id.manageImageButton);


        returnVehicles("179", new vehicleListCallbacks() {
            @Override
            public void onSuccess(@NonNull List<Vehicle> value) {
                if(user_Vehicles.size()==0) {
                    selectedVehicleTextView.setText("No Vehicle");
                    selectedVehicleImageView.setImageDrawable(getResources().getDrawable(R.drawable.vehicle0empty_vehicle));
                    return;
                }

                Log.d(TAG, "vehicles: " + user_Vehicles);
                //default show the first vehicle
                selectedVehicleTextView.setText(user_Vehicles.get(0).getRegistration_no());
                selectedVehicleImageView.setImageBitmap(user_Vehicles.get(0).getImage());
                vehicleImageViews = new HashMap<>();

                for (Vehicle vehicle : user_Vehicles) {
                    Log.e("user Vehicles: ", vehicle.getMake_name());
                    CircleImageView imageView = new CircleImageView(VehicleActivity.this);
                    imageView.setImageBitmap(vehicle.getImage());
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                    params.setMargins(0, 0, 16, 0);
                    imageView.setLayoutParams(params);
                    vehicleLayout.addView(imageView);
                    imageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Log.d(TAG, "onClickVehicle: " + vehicle.getRegistration_no());
                            selectedVehicleTextView.setText(vehicle.getRegistration_no());
                            selectedVehicleImageView.setImageBitmap(vehicle.getImage());
                            editVehicleImageButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    editVehicle(vehicle);
                                }
                            });
                        }
                    });
                    vehicleImageViews.put(vehicle.getRegistration_no(), imageView);
                }
            }

            @Override
            public void onError(@NonNull String errorMessage) {
                Log.e("user Vehicles: ", String.valueOf(user_Vehicles.size()));
            }

        });

        addImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(VehicleActivity.this, AddVehicleActivity.class));
            }
        });



        inboxImageButton = (ImageButton) findViewById(R.id.inboxImageButton);
        inboxButton = (Button) findViewById(R.id.inboxButton);
        inboxImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startInbox();
            }
        });
        inboxButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startInbox();
            }
        });

    }

    private void startDashboard() {

    }

    private void startCalendar() {

    }

    private void editVehicle(Vehicle vehicle) {
        Log.d(TAG, "editVehicle: " + vehicle);
    }

    private void startInbox() {

    }

    private void loadUserEmailImg(String user_id, @Nullable final userImageCallback imageCallback, @Nullable final userEmailCallback emailCallback) {
        String URL = IP_HOST + GET_IMG_EMAIL + user_id;

        JsonObjectRequest objectRequest = new JsonObjectRequest(Request.Method.GET, URL, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.e("Response", response.toString());
                byte[] logoBase64 = Base64.decode(response.optString("logo"), Base64.DEFAULT);
                ImgBitmap = BitmapFactory.decodeByteArray(logoBase64, 0, logoBase64.length);
                email_address = response.optString("email_address");
                if (ImgBitmap == null) {
                    Log.e("No image: ", "this user has no image");
                }
                if (ImgBitmap != null)
                    imageCallback.onSuccess(ImgBitmap);
                if (emailCallback != null)
                    emailCallback.onSuccess(email_address);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
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

    private void returnVehicles(String user_id, @Nullable final vehicleListCallbacks callbacks) {

        String URL = IP_HOST + GET_VEHICLE_LIST + user_id;

        JsonObjectRequest objectRequest = new JsonObjectRequest(Request.Method.GET, URL, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
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

                        user_Vehicles.add(vehicle);

                        Log.e("vehicle number:", String.valueOf(user_Vehicles.size()));
                    }
                    if (callbacks != null)
                        callbacks.onSuccess(user_Vehicles);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

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

            }
        });

        user_Vehicles = new ArrayList<>();

        Volley.newRequestQueue(VehicleActivity.this).add(objectRequest);
    }

    public interface vehicleListCallbacks {
        void onSuccess(@NonNull List<Vehicle> value);

        void onError(@NonNull String errorMessage);
    }

}
