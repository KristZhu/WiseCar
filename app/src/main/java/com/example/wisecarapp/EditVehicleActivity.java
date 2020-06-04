package com.example.wisecarapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

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

    private Vehicle vehicle;

    private ImageButton backImageButton;

    private ImageView vehicleImageView;
    private TextView registrationTextView;
    private TextView serviceTextView;

    private LinearLayout servicesLayout;


    private static List<Integer> services;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_vehicle);

        String vehicleID = (String) this.getIntent().getStringExtra("vehicleID");
        Log.d(TAG, "vehicleID: " + vehicleID);
        vehicle = UserInfo.getVehicles().get(vehicleID);
        Log.d(TAG, "vehicle: " + vehicle);
        services = new ArrayList<>();

        vehicleImageView = (ImageView) findViewById(R.id.vehicleImageView);
        vehicleImageView.setImageBitmap(vehicle.getImage());

        registrationTextView = (TextView) findViewById(R.id.registrationCheckBox);
        serviceTextView = (TextView) findViewById(R.id.serviceTextView);

        loadServices(vehicle.getVehicle_id(), new servicesListCallbacks() {
            @Override
            public void onSuccess(@NonNull List<Integer> value) {
                Log.e("service list", String.valueOf(services));

                servicesLayout = (LinearLayout) findViewById(R.id.servicesLayout);

                //int column = 3;
                int column = 2;
                for(int i=0; i<services.size(); i+=column) {
                    ConstraintLayout servicesLineLayout = new ConstraintLayout(EditVehicleActivity.this);
                    ConstraintSet set = new ConstraintSet();
                    ImageView[] imageViews = new ImageView[Math.min(services.size()-i, column)];
                    Log.d(TAG, "i: " + i);
                    Log.d(TAG, "imageViews.length: " + imageViews.length);
                    for(int j=0; j<imageViews.length; j++) {
                        imageViews[j] = new ImageView(EditVehicleActivity.this);
                        imageViews[j].setId(j);
                        switch (services.get(i+j)) {
                            case 1:
                                imageViews[j].setImageDrawable(getResources().getDrawable(R.drawable.edit_vehicle0service_button));
                                imageViews[j].setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Log.d(TAG, "onClickService: Start Service Records");
                                        //startServiceRecords(vehicleID);
                                    }
                                });
                                break;
                            case 2: imageViews[j].setImageDrawable(getResources().getDrawable(R.drawable.edit_vehicle0driver_button)); break;
                            case 3: imageViews[j].setImageDrawable(getResources().getDrawable(R.drawable.edit_vehicle0registration_button)); break;
                            case 4: imageViews[j].setImageDrawable(getResources().getDrawable(R.drawable.edit_vehicle0parking_button)); break;
                            case 5: imageViews[j].setImageDrawable(getResources().getDrawable(R.drawable.edit_vehicle0insurance_button)); break;
                            case 6: imageViews[j].setImageDrawable(getResources().getDrawable(R.drawable.edit_vehicle0toll_button)); break;
                            case 7: imageViews[j].setImageDrawable(getResources().getDrawable(R.drawable.edit_vehicle0fuel_button)); break;
                        }
                        set.connect(imageViews[j].getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, 16);
                        set.connect(imageViews[j].getId(), ConstraintSet.LEFT, ConstraintSet.PARENT_ID, ConstraintSet.LEFT);
                        set.connect(imageViews[j].getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, 16);
                        set.connect(imageViews[j].getId(), ConstraintSet.RIGHT, ConstraintSet.PARENT_ID, ConstraintSet.RIGHT);
                        set.constrainPercentWidth(imageViews[j].getId(), 0.3f);
                        set.setDimensionRatio(imageViews[j].getId(), "1:1");
                        set.setHorizontalBias(imageViews[j].getId(), (float)(0.5*j));
                        servicesLineLayout.addView(imageViews[j]);
                    }
                    set.applyTo(servicesLineLayout);
                    servicesLayout.addView(servicesLineLayout);
                }

            }
            @Override
            public void onError(@NonNull String errorMessage) {
                Log.e("No service", String.valueOf(services));
            }
        });


        backImageButton = (ImageButton) findViewById(R.id.backImageButton);
        backImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(EditVehicleActivity.this, VehicleActivity.class));
            }
        });

    }

    private void loadServices(String vehicle_id, @Nullable final servicesListCallbacks callbacks) {

        String URL = IP_HOST + GET_SERVICE + vehicle_id;

        JsonObjectRequest objectRequest = new JsonObjectRequest(Request.Method.GET, URL, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.e("Response: ", response.toString());
                JSONArray jsonArray;
                JSONObject jsonObject;
                try {
                    jsonArray = response.getJSONArray("service_list");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        jsonObject = jsonArray.getJSONObject(i);

                        services.add(jsonObject.optInt("service_id"));

//                        Log.e("service id", String.valueOf(jsonObject.optInt("service_id")));
                    }

                    if (callbacks != null)
                        callbacks.onSuccess(services);
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
                    Log.e("No service", message);
                    if (callbacks != null)
                        callbacks.onError(message);
                }

            }
        });

        Volley.newRequestQueue(EditVehicleActivity.this).add(objectRequest);
    }


    public interface servicesListCallbacks {
        void onSuccess(@NonNull List<Integer> value);

        void onError(@NonNull String errorMessage);
    }

}
