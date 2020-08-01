package com.wisecarCompany.wisecarapp.user.vehicle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.wisecarCompany.wisecarapp.R;
import com.wisecarCompany.wisecarapp.function.driverLog.DriverLogDashboardActivity;
import com.wisecarCompany.wisecarapp.function.fuelReceipt.FuelReceiptDashboardActivity;
import com.wisecarCompany.wisecarapp.function.insuranceRecord.InsuranceRecordDashboardActivity;
import com.wisecarCompany.wisecarapp.function.parkingReceipt.ParkingReceiptDashboardActivity;
import com.wisecarCompany.wisecarapp.function.registrationReminder.RegistrationReminderDashboardActivity;
import com.wisecarCompany.wisecarapp.function.serviceRecords.ServiceRecordsDashboardActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.TreeSet;

public class DashboardActivity extends AppCompatActivity {

    private final String TAG = "dashboard";

    private final String IP_HOST = "http://54.206.19.123:3000";
    private final String GET_SERVICES = "/api/v1/services/getservicebyuid";
    private final String GET_IMG = "/api/v1/users/";

    private SharedPreferences sp;
    private String userID;
    private String username;

    private ImageButton backImageButton;

    private ImageView userImgImageView;
    private TextView usernameTextView;

    private LinearLayout servicesLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        sp = this.getSharedPreferences("userInfo", MODE_PRIVATE);
        userID = sp.getString("USER_ID", "");
        username = sp.getString("USERNAME", "");
        Log.d(TAG, "userID: " + userID);
        Log.d(TAG, "username: " + username);

        backImageButton = $(R.id.backImageButton);
        backImageButton.setOnClickListener(v -> startActivity(new Intent(DashboardActivity.this, VehicleActivity.class)));

        userImgImageView = $(R.id.userImgImageView);
        usernameTextView = $(R.id.usernameTextView);
        usernameTextView.setText(username);
        loadUserImg(new userImageCallback() {
            @Override
            public void onSuccess(@NonNull Bitmap value) {
                userImgImageView.setImageBitmap(value);
            }
        });

        getServices(new serviceCallbacks() {
            @Override
            public void onSuccess(@NonNull List<Integer> serviceList) {
                List<Integer> services = new ArrayList<>(new TreeSet<>(serviceList));
                Log.e("service list", String.valueOf(services));

                servicesLayout = $(R.id.servicesLayout);
                //int column = 3;
                int column = 2;
                for (int i = 0; i < services.size(); i += column) {
                    ConstraintLayout servicesLineLayout = new ConstraintLayout(DashboardActivity.this);
                    ConstraintSet set = new ConstraintSet();
                    ImageView[] imageViews = new ImageView[Math.min(services.size() - i, column)];
                    for (int j = 0; j < imageViews.length; j++) {
                        imageViews[j] = new ImageView(DashboardActivity.this);
                        imageViews[j].setId(j);
                        switch (services.get(i + j)) {
                            case 1:
                                imageViews[j].setImageDrawable(getResources().getDrawable(R.drawable.edit_vehicle0service_button));
                                imageViews[j].setOnClickListener(v -> startServiceRecords());
                                break;
                            case 2:
                                imageViews[j].setImageDrawable(getResources().getDrawable(R.drawable.edit_vehicle0driver_button));
                                imageViews[j].setOnClickListener(v -> startDriverlog());
                                break;
                            case 3:
                                imageViews[j].setImageDrawable(getResources().getDrawable(R.drawable.edit_vehicle0registration_button));
                                imageViews[j].setOnClickListener(v -> startRegistrationReminder());
                                break;
                            case 4:
                                imageViews[j].setImageDrawable(getResources().getDrawable(R.drawable.edit_vehicle0parking_button));
                                imageViews[j].setOnClickListener(v -> startParkingReceipts());
                                break;
                            case 5:
                                imageViews[j].setImageDrawable(getResources().getDrawable(R.drawable.edit_vehicle0insurance_button));
                                imageViews[j].setOnClickListener(v -> startInsuranceRecord());
                                break;
                            //case 6:
                                //imageViews[j].setImageDrawable(getResources().getDrawable(R.drawable.edit_vehicle0toll_button));
                                //break;
                            case 6:
                                imageViews[j].setImageDrawable(getResources().getDrawable(R.drawable.edit_vehicle0fuel_button));
                                imageViews[j].setOnClickListener(v -> startFuelReceipts());
                                break;
                        }
                        set.connect(imageViews[j].getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, 16);
                        set.connect(imageViews[j].getId(), ConstraintSet.LEFT, ConstraintSet.PARENT_ID, ConstraintSet.LEFT);
                        set.connect(imageViews[j].getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, 16);
                        set.connect(imageViews[j].getId(), ConstraintSet.RIGHT, ConstraintSet.PARENT_ID, ConstraintSet.RIGHT);
                        set.constrainPercentWidth(imageViews[j].getId(), 0.45f);
                        set.setDimensionRatio(imageViews[j].getId(), "1:1");
                        set.setHorizontalBias(imageViews[j].getId(), (float) (1.0 * j));
                        servicesLineLayout.addView(imageViews[j]);
                    }
                    set.applyTo(servicesLineLayout);
                    servicesLayout.addView(servicesLineLayout);
                }
            }

            @Override
            public void onError(@NonNull String errorMessage) {
                Log.e("No service", errorMessage);
            }
        });

    }

    private void startServiceRecords() {
        startActivity(new Intent(DashboardActivity.this, ServiceRecordsDashboardActivity.class));
    }

    private void startDriverlog() {
        startActivity(new Intent(DashboardActivity.this, DriverLogDashboardActivity.class));
    }

    private void startRegistrationReminder() {
        startActivity(new Intent(DashboardActivity.this, RegistrationReminderDashboardActivity.class));
    }

    private void startParkingReceipts() {
        startActivity(new Intent(DashboardActivity.this, ParkingReceiptDashboardActivity.class));
    }

    private void startInsuranceRecord() {
        startActivity(new Intent(DashboardActivity.this, InsuranceRecordDashboardActivity.class));
    }

    private void startFuelReceipts() {
        startActivity(new Intent(DashboardActivity.this, FuelReceiptDashboardActivity.class));
    }

    private <T extends View> T $(int id){
        return (T) findViewById(id);
    }

    private void getServices(@Nullable final serviceCallbacks callbacks) {

        String URL = IP_HOST + GET_SERVICES;
        List<Integer> services = new ArrayList();

        final JSONObject jsonParam = new JSONObject();
        try {
            jsonParam.put("user_id", userID);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest objectRequest = new JsonObjectRequest(Request.Method.POST, URL, jsonParam, response -> {
            Log.e("Records Response", response.toString());
            JSONObject jsonObject;

            try {
                JSONArray jsonArray = response.getJSONArray("service_list");

                for (int i = 0; i < jsonArray.length(); i++) {
                    jsonObject = jsonArray.getJSONObject(i);

                    services.add(jsonObject.optInt("service_id"));

                }
                if (callbacks != null)
                    callbacks.onSuccess(services);
            } catch (JSONException e) {
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
            }

        });
        Volley.newRequestQueue(DashboardActivity.this).add(objectRequest);

    }

    public interface serviceCallbacks {
        void onSuccess(@NonNull List<Integer> value);

        void onError(@NonNull String errorMsg);
    }

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

}
