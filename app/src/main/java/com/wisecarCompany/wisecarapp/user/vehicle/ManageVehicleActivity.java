package com.wisecarCompany.wisecarapp.user.vehicle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
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
import com.wisecarCompany.wisecarapp.viewElement.CircleImageView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

public class ManageVehicleActivity extends AppCompatActivity {    //edit a special vehicle, or select functions of this vehicle to go

    private final static String TAG = "Manage Vehicle";
    private final String IP_HOST = "http://54.206.19.123:3000";
    private final String GET_SERVICE = "/api/v1/services/";

    private String vehicleID;
    private Vehicle vehicle;

    private ImageButton backImageButton;

    private CircleImageView vehicleImageView;
    private TextView makeRegistrationNoTextView;
    private TextView vinTextView;
    private TextView registrationTextView;
    private TextView serviceTextView;

    private LinearLayout servicesLayout;

    private ImageButton shareImageButton;


    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_vehicle);

        vehicleID = (String) this.getIntent().getStringExtra("vehicleID");
        assert vehicleID != null;
        if (vehicleID.equals("a")) {
            //id = "a" means the vehicle is newly added and it is a fake id, and synchronizing process is not finished
            //jump back to VehicleActivity to wait for synchronizing
            for (String newID : UserInfo.getVehicles().keySet()) {
                UserInfo.setVehicles(null);
                Toast.makeText(ManageVehicleActivity.this, "Please wait for system to finish adding vehicle", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(ManageVehicleActivity.this, VehicleActivity.class));
                return;
            }
        }
        Log.d(TAG, "vehicleID: " + vehicleID);

        vehicle = UserInfo.getVehicles().get(vehicleID);
        Log.d(TAG, "vehicle: " + vehicle);

        vehicleImageView = $(R.id.vehicleImageView);
        if(vehicle.getImage()==null) vehicleImageView.setImageDrawable(getResources().getDrawable(R.drawable.profile0empty_image));
        else vehicleImageView.setImageBitmap(vehicle.getImage());

        makeRegistrationNoTextView = $(R.id.makeRegistrationNoTextView);
        vinTextView = $(R.id.vinTextView);
        registrationTextView = $(R.id.registrationCheckBox);
        serviceTextView = $(R.id.serviceTextView);

        makeRegistrationNoTextView.setText(vehicle.getMake_name() + " - " + vehicle.getRegistration_no());

        loadServices(vehicleID, new servicesCallbacks() {
            @Override
            public void onSuccess(@NonNull List<Integer> services) {
                services = new ArrayList<>(new TreeSet<>(services));
                Log.e("services", String.valueOf(services));
                vehicle.setServices(services);
                UserInfo.getVehicles().get(vehicleID).setServices(services);    //don't know why... it cannot sync
                //for(int i: services) vehicle.getServices().add(i);
                Log.d(TAG, "services: " + vehicle.getServices());
                Log.d(TAG, "this vehicle: " + vehicle);
                Log.d(TAG, "services in UserInfo: " + UserInfo.getVehicles().get(vehicleID).getServices());
                Log.d(TAG, "this vehicle in UserInfo: " + UserInfo.getVehicles().get(vehicleID));

                servicesLayout = $(R.id.servicesLayout);
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
                                imageViews[j].setOnClickListener(v -> startServiceRecords(vehicleID));
                                break;
                            case 2:
                                imageViews[j].setImageDrawable(getResources().getDrawable(R.drawable.edit_vehicle0driver_button));
                                imageViews[j].setOnClickListener(v -> startDriverlog(vehicleID));
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
                            //case 6:
                                //imageViews[j].setImageDrawable(getResources().getDrawable(R.drawable.edit_vehicle0toll_button));
                                //break;
                            case 6:
                                imageViews[j].setImageDrawable(getResources().getDrawable(R.drawable.edit_vehicle0fuel_button));
                                imageViews[j].setOnClickListener(v -> startFuelReceipts(vehicleID));
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

                shareImageButton = $(R.id.shareImageButton);
                shareImageButton.setOnClickListener(v -> shareVehicle(vehicleID));

            }

            @Override
            public void onError(@NonNull String errorMessage) {
                Log.e("No service", errorMessage);
            }
        });

        backImageButton = $(R.id.backImageButton);
        backImageButton.setOnClickListener(v -> startActivity(new Intent(ManageVehicleActivity.this, VehicleActivity.class)));

    }

    private void loadServices(String vehicle_id, @Nullable final servicesCallbacks callbacks) {

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

        Volley.newRequestQueue(ManageVehicleActivity.this).add(objectRequest);
    }


    public interface servicesCallbacks {
        void onSuccess(@NonNull List<Integer> value);

        void onError(@NonNull String errorMessage);
    }

    private void startServiceRecords(String vehicleID) {
        Log.d(TAG, "ServiceRecordsVehicleID: " + vehicleID);
        startActivity(new Intent(ManageVehicleActivity.this, ServiceRecordsActivity.class).putExtra("vehicleID", vehicleID));
    }

    private void startDriverlog(String vehicleID) {
        Log.d(TAG, "DriverLogVehicleID: " + vehicleID);
        if(UserInfo.getCurrLog()==null || UserInfo.getCurrLog().getVehicleID().equals(vehicleID)) {
            startActivity(new Intent(ManageVehicleActivity.this, DriverLogActivity.class).putExtra("vehicleID", vehicleID));
        } else {
            Toast.makeText(this, "Driver Log for Vehicle "
                    + UserInfo.getVehicles().get(UserInfo.getCurrLog().getVehicleID()).getRegistration_no()
                    + " is still in process. Please stop it first before starting a new vehicle driver log. ", Toast.LENGTH_LONG).show();
        }
    }

    private void startRegistrationReminder(String vehicleID) {
        Log.d(TAG, "RegistrationReminderVehicleID: " + vehicleID);
        startActivity(new Intent(ManageVehicleActivity.this, RegistrationReminderActivity.class).putExtra("vehicleID", vehicleID));
    }

    private void startParkingReceipts(String vehicleID) {
        Log.d(TAG, "ParkingReceiptsVehicleID: " + vehicleID);
        startActivity(new Intent(ManageVehicleActivity.this, ParkingReceiptActivity.class).putExtra("vehicleID", vehicleID));
    }

    private void startInsuranceRecord(String vehicleID) {
        Log.d(TAG, "InsuranceRecordVehicleID: " + vehicleID);
        startActivity(new Intent(ManageVehicleActivity.this, InsuranceRecordActivity.class).putExtra("vehicleID", vehicleID));
    }

    private void startFuelReceipts(String vehicleID) {
        Log.d(TAG, "FuelReceiptsVehicleID: " + vehicleID);
        startActivity(new Intent(ManageVehicleActivity.this, FuelReceiptActivity.class).putExtra("vehicleID", vehicleID));
    }

    private void shareVehicle(String vehicleID) {
        Log.d(TAG, "shared vehicle ID: " + vehicleID);
        Log.d(TAG, "shared vehicle services: " + UserInfo.getVehicles().get(vehicleID).getServices());
        startActivity(new Intent(ManageVehicleActivity.this, ShareVehicleListActivity.class).putExtra("vehicleID", vehicleID));
    }

    private <T extends View> T $(int id){
        return (T) findViewById(id);
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
