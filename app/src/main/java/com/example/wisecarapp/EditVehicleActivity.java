package com.example.wisecarapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
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

    private ImageButton backImageButton;

    private TextView registrationTextView;
    private TextView serviceTextView;

    private LinearLayout servicesLayout;

    private final String IP_HOST = "http://54.206.19.123:3000";
    private final String GET_SERVICE = "/api/v1/services/";

    private static List<Integer> service_list = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_vehicle);

        loadServices("89", new servicesListCallbacks() {

            @Override
            public void onSuccess(@NonNull List<Integer> value) {
                Log.e("service list size", String.valueOf(service_list.size()));
            }

            @Override
            public void onError(@NonNull String errorMessage) {
                Log.e("No service", String.valueOf(service_list.size()));
            }

        });

        backImageButton = (ImageButton) findViewById(R.id.backImageButton);
        backImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        registrationTextView = (TextView) findViewById(R.id.registrationCheckBox);
        serviceTextView = (TextView) findViewById(R.id.serviceTextView);

        servicesLayout = (LinearLayout) findViewById(R.id.servicesLayout);

        List<Integer> services = new LinkedList<>();
        services.add(1);
        services.add(2);
        services.add(3);
        services.add(6);

        if (services.size() == 0) {

        } else if (services.size() % 2 == 0) {
            while (services.size() > 0) {
                int left = services.get(0);
                services.remove(0);
                int right = services.get(0);
                services.remove(0);
                LinearLayout servicesLineLayout = new LinearLayout(EditVehicleActivity.this);
                ImageView leftImageView = new ImageView(EditVehicleActivity.this);
                switch (left) {
                    case 0:
                        leftImageView.setImageDrawable(getResources().getDrawable(R.drawable.edit_vehicle0service_button));
                        break;
                    case 1:
                        leftImageView.setImageDrawable(getResources().getDrawable(R.drawable.edit_vehicle0driver_button));
                        break;
                    case 2:
                        leftImageView.setImageDrawable(getResources().getDrawable(R.drawable.edit_vehicle0registration_button));
                        break;
                    case 3:
                        leftImageView.setImageDrawable(getResources().getDrawable(R.drawable.edit_vehicle0parking_button));
                        break;
                    case 4:
                        leftImageView.setImageDrawable(getResources().getDrawable(R.drawable.edit_vehicle0insurance_button));
                        break;
                    case 5:
                        leftImageView.setImageDrawable(getResources().getDrawable(R.drawable.edit_vehicle0toll_button));
                        break;
                    case 6:
                        leftImageView.setImageDrawable(getResources().getDrawable(R.drawable.edit_vehicle0fuel_button));
                        break;
                }
                LinearLayout.LayoutParams leftParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                leftParams.setMargins(0, 0, 16, 0);
                leftImageView.setLayoutParams(leftParams);
                servicesLineLayout.addView(leftImageView);
                ImageView rightImageView = new ImageView(EditVehicleActivity.this);

                switch (left) {
                    case 0:
                        rightImageView.setImageDrawable(getResources().getDrawable(R.drawable.edit_vehicle0service_button));
                        break;
                    case 1:
                        rightImageView.setImageDrawable(getResources().getDrawable(R.drawable.edit_vehicle0driver_button));
                        break;
                    case 2:
                        rightImageView.setImageDrawable(getResources().getDrawable(R.drawable.edit_vehicle0registration_button));
                        break;
                    case 3:
                        rightImageView.setImageDrawable(getResources().getDrawable(R.drawable.edit_vehicle0parking_button));
                        break;
                    case 4:
                        rightImageView.setImageDrawable(getResources().getDrawable(R.drawable.edit_vehicle0insurance_button));
                        break;
                    case 5:
                        rightImageView.setImageDrawable(getResources().getDrawable(R.drawable.edit_vehicle0toll_button));
                        break;
                    case 6:
                        rightImageView.setImageDrawable(getResources().getDrawable(R.drawable.edit_vehicle0fuel_button));
                        break;
                }
                LinearLayout.LayoutParams rightParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                rightParams.setMargins(0, 0, 16, 0);
                rightImageView.setLayoutParams(rightParams);
                servicesLineLayout.addView(rightImageView);
                servicesLayout.addView(servicesLineLayout);

            }
        } else {
            while (services.size() > 0) {
                int left = services.get(0);
                services.remove(0);
                int right = services.get(0);
                services.remove(0);

            }
            int last = services.get(0);
        }

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

                        service_list.add(jsonObject.optInt("service_id"));

//                        Log.e("service id", String.valueOf(jsonObject.optInt("service_id")));
                    }

                    if (callbacks != null)
                        callbacks.onSuccess(service_list);
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
