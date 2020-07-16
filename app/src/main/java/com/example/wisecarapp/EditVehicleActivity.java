package com.example.wisecarapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class EditVehicleActivity extends AppCompatActivity {

    private final static String TAG = "EditVehicle";
    private final String IP_HOST = "http://54.206.19.123:3000";
    private final String GET_SERVICE = "/api/v1/services/";

    private String vehicleID;
    private Vehicle vehicle;

    private ImageButton backImageButton;

    private ImageView vehicleImageView;
    private TextView makeRegistrationNoTextView;
    private TextView vinTextView;
    private TextView registrationTextView;
    private TextView serviceTextView;

    private LinearLayout servicesLayout;

    private ImageButton shareImageButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_vehicle);

//        vehicleID = (String) this.getIntent().getStringExtra("vehicleID");
//        if (vehicleID.equals("a")) {
//            //id = "a" means the vehicle is newly added and it is a fake id, and synchronizing process is not finished
//            //jump back to VehicleActivity to wait for synchronizing
//            for (String newID : UserInfo.getVehicles().keySet()) {
//                UserInfo.setVehicles(null);
//                Toast.makeText(EditVehicleActivity.this, "Please wait for system to finish adding vehicle", Toast.LENGTH_SHORT).show();
//                startActivity(new Intent(EditVehicleActivity.this, VehicleActivity.class));
//                return;
//            }
//        }
//        Log.d(TAG, "vehicleID: " + vehicleID);
//
//        vehicle = UserInfo.getVehicles().get(vehicleID);
//        Log.d(TAG, "vehicle: " + vehicle);

        vehicleImageView = $(R.id.vehicleImageView);
        vehicleImageView.setImageBitmap(vehicle.getImage());

        makeRegistrationNoTextView = $(R.id.makeRegistrationNoTextView);
        vinTextView = $(R.id.vinTextView);
        registrationTextView = $(R.id.registrationCheckBox);
        serviceTextView = $(R.id.serviceTextView);

        makeRegistrationNoTextView.setText(vehicle.getMake_name() + " - " + vehicle.getRegistration_no());

        loadServices(vehicle.getVehicle_id(), new servicesListCallbacks() {
            @Override
            public void onSuccess(@NonNull List<Integer> serviceList) {
                List<Integer> services = new ArrayList<>(serviceList);
                Log.e("service list", String.valueOf(services));
                vehicle.setServices(services);
                //for(int i: services) vehicle.getServices().add(i);
                Log.d(TAG, "services: " + UserInfo.getVehicles().get(vehicleID).getServices());

                servicesLayout = $(R.id.servicesLayout);
                //int column = 3;
                int column = 2;
                for (int i = 0; i < services.size(); i += column) {
                    ConstraintLayout servicesLineLayout = new ConstraintLayout(EditVehicleActivity.this);
                    ConstraintSet set = new ConstraintSet();
                    ImageView[] imageViews = new ImageView[Math.min(services.size() - i, column)];
                    for (int j = 0; j < imageViews.length; j++) {
                        imageViews[j] = new ImageView(EditVehicleActivity.this);
                        imageViews[j].setId(j);
                        switch (services.get(i + j)) {
                            case 1:
                                imageViews[j].setImageDrawable(getResources().getDrawable(R.drawable.edit_vehicle0service_button));
                                imageViews[j].setOnClickListener(v -> startServiceRecords(vehicleID));
                                break;
                            case 2:
                                imageViews[j].setImageDrawable(getResources().getDrawable(R.drawable.edit_vehicle0driver_button));
                                imageViews[j].setOnClickListener(v -> startRecordlog(vehicleID));
                                break;
                            case 3:
                                imageViews[j].setImageDrawable(getResources().getDrawable(R.drawable.edit_vehicle0registration_button));
                                imageViews[j].setOnClickListener(v -> startRegistrationReminder(vehicleID));
                                break;
                            case 4:
                                imageViews[j].setImageDrawable(getResources().getDrawable(R.drawable.edit_vehicle0parking_button));
                                imageViews[j].setOnClickListener(v -> startParkingReceipts(vehicleID));
                                break;
                            case 5:
                                imageViews[j].setImageDrawable(getResources().getDrawable(R.drawable.edit_vehicle0insurance_button));
                                imageViews[j].setOnClickListener(v -> startInsuranceRecord(vehicleID));
                                break;
                            case 6:
                                imageViews[j].setImageDrawable(getResources().getDrawable(R.drawable.edit_vehicle0toll_button));
                                break;
                            case 7:
                                imageViews[j].setImageDrawable(getResources().getDrawable(R.drawable.edit_vehicle0fuel_button));
                                imageViews[j].setOnClickListener(v -> startFuelReceipts(vehicleID));
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

                shareImageButton = $(R.id.shareImageButton);
                shareImageButton.setOnClickListener(v -> {
                    UserInfo.getVehicles().get(vehicleID).setServices(services);    //have no idea why I must do this. But if not, services==null. Makes no senses...
                    //Log.d(TAG, "before share user services: " + UserInfo.getVehicles().get(vehicleID).getServices());
                    //Log.d(TAG, "before share services: " + services);
                    //Log.d(TAG, "before share servicesList: " + serviceList);
                    shareVehicle(vehicleID);
                });

            }

            @Override
            public void onError(@NonNull String errorMessage) {
                Log.e("No service", errorMessage);
            }
        });

        backImageButton = $(R.id.backImageButton);
        backImageButton.setOnClickListener(v -> startActivity(new Intent(EditVehicleActivity.this, VehicleActivity.class)));

    }

    private void loadServices(String vehicle_id, @Nullable final servicesListCallbacks callbacks) {

        String URL = IP_HOST + GET_SERVICE + vehicle_id;

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

        Volley.newRequestQueue(EditVehicleActivity.this).add(objectRequest);
    }


    public interface servicesListCallbacks {
        void onSuccess(@NonNull List<Integer> value);

        void onError(@NonNull String errorMessage);
    }

    private void startServiceRecords(String vehicleID) {
        Log.d(TAG, "ServiceRecordsVehicleID: " + vehicleID);
        startActivity(new Intent(EditVehicleActivity.this, ServiceRecordsActivity.class).putExtra("vehicleID", vehicleID));
    }

    private void startRecordlog(String vehicleID) {
        Log.d(TAG, "RecordLogVehicleID: " + vehicleID);
        startActivity(new Intent(EditVehicleActivity.this, RecordLogActivity.class).putExtra("vehicleID", vehicleID));
    }

    private void startRegistrationReminder(String vehicleID) {
        Log.d(TAG, "RegistrationReminderVehicleID: " + vehicleID);
        startActivity(new Intent(EditVehicleActivity.this, RegistrationReminderActivity.class).putExtra("vehicleID", vehicleID));
    }

    private void startParkingReceipts(String vehicleID) {
        Log.d(TAG, "ParkingReceiptsVehicleID: " + vehicleID);
        startActivity(new Intent(EditVehicleActivity.this, ParkingReceiptActivity.class).putExtra("vehicleID", vehicleID));
    }

    private void startInsuranceRecord(String vehicleID) {
        Log.d(TAG, "InsuranceRecordVehicleID: " + vehicleID);
        startActivity(new Intent(EditVehicleActivity.this, InsuranceRecordActivity.class).putExtra("vehicleID", vehicleID));
    }

    private void startFuelReceipts(String vehicleID) {

    }

    private void shareVehicle(String vehicleID) {
        Log.d(TAG, "shared vehicle ID: " + vehicleID);
        Log.d(TAG, "shared vehicle services: " + UserInfo.getVehicles().get(vehicleID).getServices());
        startActivity(new Intent(EditVehicleActivity.this, ShareVehicleListActivity.class).putExtra("vehicleID", vehicleID));
    }

    private <T extends View> T $(int id){
        return (T) findViewById(id);
    }

}
