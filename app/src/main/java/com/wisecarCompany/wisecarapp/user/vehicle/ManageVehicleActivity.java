package com.wisecarCompany.wisecarapp.user.vehicle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import android.annotation.SuppressLint;
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
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.wisecarCompany.wisecarapp.R;
import com.wisecarCompany.wisecarapp.function.driverLog.DriverLogActivity;
import com.wisecarCompany.wisecarapp.function.fuelReceipt.FuelReceiptActivity;
import com.wisecarCompany.wisecarapp.function.insuranceRecord.InsuranceRecordActivity;
import com.wisecarCompany.wisecarapp.function.parkingReceipt.ParkingReceiptActivity;
import com.wisecarCompany.wisecarapp.function.registrationReminder.RegistrationReminderActivity;
import com.wisecarCompany.wisecarapp.function.serviceRecords.ServiceRecordsActivity;
import com.wisecarCompany.wisecarapp.function.shareVehicle.ShareVehicleListActivity;
import com.wisecarCompany.wisecarapp.user.UserInfo;
import com.wisecarCompany.wisecarapp.element.CircleImageView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

public class ManageVehicleActivity extends AppCompatActivity {    //edit a special vehicle, or select functions of this vehicle to go

    private final static String TAG = "Manage Vehicle";
    private final String IP_HOST = "http://7ce7ccc8008dec603016594c02f76d60-1846191374.ap-southeast-2.elb.amazonaws.com";
    private final String GET_SERVICE = "/api/v1/services/";
    private final String GET_VEHICLE_LIST = "/api/v1/vehicles/user/";

    private SharedPreferences sp;
    private String userID;

    private ImageButton backImageButton;

    private CircleImageView vehicleImageView;
    //private TextView makeRegistrationNoTextView;
    //private TextView vinTextView;
    //private TextView registrationTextView;
    //private TextView serviceTextView;
    private TextView regTextView;

    private LinearLayout servicesLayout;

    private ImageButton shareImageButton;


    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_vehicle);

        sp = this.getSharedPreferences("userInfo", MODE_PRIVATE);
        userID = sp.getString("USER_ID", "");
        Log.d(TAG, "userID: " + userID);

        if (UserInfo.getNewVehicle()!=null || UserInfo.getCurrVehicle().getVehicle_id()==null) {
            //synchronizing process is not finished
            //call returnNewVehicle again to syc
            Toast.makeText(ManageVehicleActivity.this, "Please wait for system to finish adding vehicle", Toast.LENGTH_SHORT).show();
            returnNewVehicle(new newVehicleCallbacks() {
                @Override
                public void onSuccess(Vehicle value) {
                    Log.d(TAG, "return new vehicle: " + value);
                    if(value==null) {
                        Toast.makeText(ManageVehicleActivity.this, "Adding vehicle process has not finished. Please try later. ", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(ManageVehicleActivity.this, VehicleActivity.class));
                    } else {
                        UserInfo.setNewVehicle(null);
                        UserInfo.setCurrVehicle(value);
                        loadPage();
                    }
                }
                @Override
                public void onError(@NonNull String errorMessage) {
                    Toast.makeText(ManageVehicleActivity.this, "Load Vehicle Fail. " + errorMessage, Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(ManageVehicleActivity.this, VehicleActivity.class));
                }
            });

        } else {
            loadPage();
        }

    }

    private void loadPage() {
        assert UserInfo.getNewVehicle()==null;
        assert UserInfo.getCurrVehicle()!=null;

        backImageButton = $(R.id.backImageButton);
        backImageButton.setOnClickListener(v -> {
            UserInfo.setCurrVehicle(null);
            startActivity(new Intent(ManageVehicleActivity.this, VehicleActivity.class));
        });

        vehicleImageView = $(R.id.vehicleImageView);
        if(UserInfo.getCurrVehicle().getImage()==null) vehicleImageView.setImageDrawable(getResources().getDrawable(R.drawable.profile0empty_image));
        else vehicleImageView.setImageBitmap(UserInfo.getCurrVehicle().getImage());

        //makeRegistrationNoTextView = $(R.id.makeRegistrationNoTextView);
        //vinTextView = $(R.id.vinTextView);
        //registrationTextView = $(R.id.registrationCheckBox);
        //serviceTextView = $(R.id.serviceTextView);
        //makeRegistrationNoTextView.setText(vehicle.getMake_name() + " - " + vehicle.getRegistration_no());
        regTextView = $(R.id.regTextView);
        regTextView.setText(UserInfo.getCurrVehicle().getRegistration_no());

        servicesLayout = $(R.id.servicesLayout);

        shareImageButton = $(R.id.shareImageButton);

        loadServices(new servicesCallbacks() {
            @Override
            public void onSuccess(@NonNull List<Integer> services) {
                services = new ArrayList<>(new TreeSet<>(services));
                Log.e("services", String.valueOf(services));
                UserInfo.getCurrVehicle().setServices(services);
                //int column = 3;
                int column = 2;
                for (int i = 0; i < services.size(); i += column) {
                    ConstraintLayout servicesLineLayout = new ConstraintLayout(ManageVehicleActivity.this);
                    ConstraintSet set = new ConstraintSet();
                    ImageView[] imageViews = new ImageView[Math.min(services.size() - i, column)];
                    for (int j = 0; j < imageViews.length; j++) {
                        imageViews[j] = new ImageView(ManageVehicleActivity.this);
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
                            //    imageViews[j].setImageDrawable(getResources().getDrawable(R.drawable.edit_vehicle0toll_button));
                            //    break;
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
                        //set.constrainPercentWidth(imageViews[j].getId(), 0.3f);
                        set.setDimensionRatio(imageViews[j].getId(), "1:1");
                        set.setHorizontalBias(imageViews[j].getId(), (float) (1.0 * j));
                        //set.setHorizontalBias(imageViews[j].getId(), (float) (0.5 * j));
                        servicesLineLayout.addView(imageViews[j]);
                    }
                    set.applyTo(servicesLineLayout);
                    servicesLayout.addView(servicesLineLayout);
                }

                shareImageButton.setOnClickListener(v -> shareVehicle());

            }

            @Override
            public void onError(@NonNull String errorMessage) {
                Log.e("No service", errorMessage);
            }
        });
    }


    private void returnNewVehicle(@Nullable final newVehicleCallbacks callbacks) {

        String URL = IP_HOST + GET_VEHICLE_LIST + userID;

        JsonObjectRequest objectRequest = new JsonObjectRequest(Request.Method.GET, URL, null, response -> {
            JSONArray jsonArray;
            JSONObject jsonObject;

            Vehicle vehicle = null;

            try {
                jsonArray = response.getJSONArray("vehicle_list");
                for (int i = 0; i < jsonArray.length(); i++) {
                    jsonObject = jsonArray.getJSONObject(i);
                    Log.e("returnNewV array: ", jsonObject.toString());

                    String regNo = jsonObject.optString("registration_no").replaceAll("\r\n|\r|\n", "");
                    if(UserInfo.getNewVehicle().getRegistration_no().equals(regNo)) {

                        byte[] logoBase64 = Base64.decode(jsonObject.optString("image"), Base64.DEFAULT);
                        Bitmap imgBitmap = BitmapFactory.decodeByteArray(logoBase64, 0, logoBase64.length);

                        vehicle = new Vehicle(
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
                        if (callbacks != null)
                            callbacks.onSuccess(vehicle);
                        break;

                    }

                }
                if (callbacks != null)
                    callbacks.onSuccess(vehicle);
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

        Volley.newRequestQueue(this).add(objectRequest);
    }

    public interface newVehicleCallbacks {
        void onSuccess(Vehicle value);

        void onError(@NonNull String errorMessage);
    }


    private void loadServices(@Nullable final servicesCallbacks callbacks) {

        String URL = IP_HOST + GET_SERVICE + UserInfo.getCurrVehicle().getVehicle_id();

        JsonObjectRequest objectRequest = new JsonObjectRequest(Request.Method.GET, URL, null, response -> {
            Log.e("Response: ", response.toString());
            JSONArray jsonArray;
            JSONObject jsonObject;
            try {
                List<Integer> services = new ArrayList<>();
                jsonArray = response.getJSONArray("service_list");
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
                Log.e("No service", message);
                if (callbacks != null)
                    callbacks.onError(message);
            }

        });

        Volley.newRequestQueue(ManageVehicleActivity.this).add(objectRequest);
    }

    public interface servicesCallbacks {
        void onSuccess(@NonNull List<Integer> value);

        void onError(@NonNull String errorMessage);
    }


    private void startServiceRecords() {
        Log.d(TAG, "ServiceRecords");
        startActivity(new Intent(ManageVehicleActivity.this, ServiceRecordsActivity.class));
    }

    private void startDriverlog() {
        Log.d(TAG, "DriverLog");
        if(UserInfo.getCurrLog()==null || UserInfo.getCurrLog().getVehicle().getVehicle_id().equals(UserInfo.getCurrVehicle().getVehicle_id())) {
            startActivity(new Intent(ManageVehicleActivity.this, DriverLogActivity.class));
        } else {
            Toast.makeText(this, "Driver Log for Vehicle "
                    + UserInfo.getCurrLog().getVehicle().getRegistration_no()
                    + " is still in process. Please stop it first before starting a new vehicle driver log. ", Toast.LENGTH_SHORT).show();
        }
    }

    private void startRegistrationReminder() {
        Log.d(TAG, "RegistrationReminder");
        startActivity(new Intent(ManageVehicleActivity.this, RegistrationReminderActivity.class));
    }

    private void startParkingReceipts() {
        Log.d(TAG, "ParkingReceipts");
        startActivity(new Intent(ManageVehicleActivity.this, ParkingReceiptActivity.class));
    }

    private void startInsuranceRecord() {
        Log.d(TAG, "InsuranceRecord");
        startActivity(new Intent(ManageVehicleActivity.this, InsuranceRecordActivity.class));
    }

    private void startFuelReceipts() {
        Log.d(TAG, "FuelReceipts");
        startActivity(new Intent(ManageVehicleActivity.this, FuelReceiptActivity.class));
    }

    private void shareVehicle() {
        Log.d(TAG, "shared vehicle");
        Log.d(TAG, "shared vehicle services: " + UserInfo.getCurrVehicle().getServices());
        startActivity(new Intent(ManageVehicleActivity.this, ShareVehicleListActivity.class));
    }

    private <T extends View> T $(int id){
        return (T) findViewById(id);
    }

    @Override
    public void onBackPressed() {
        UserInfo.setCurrVehicle(null);
        startActivity(new Intent(this, VehicleActivity.class));
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK) {
            UserInfo.setCurrVehicle(null);
            startActivity(new Intent(this, VehicleActivity.class));
            return true;    //stop calling super method
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }

}
