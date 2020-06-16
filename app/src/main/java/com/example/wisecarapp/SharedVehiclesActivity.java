package com.example.wisecarapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.util.TypedValue;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class SharedVehiclesActivity extends AppCompatActivity {

    private final static String TAG = "SharedVehicles";

    private final String IP_HOST = "http://54.206.19.123:3000";
    private final String GET_SHARED_LIST = "/api/v1/sharevehicle/sharedcompanylist/";

    private final String GET_HISTORY = "/api/v1/sharevehicle/sharedetailapp/";

    private String vehicleID;
    private Vehicle vehicle;

    private ImageButton backImageButton;
    private TextView headerTextView;
    private ImageView vehicleImageView;

    private LinearLayout shareLayout;

    private ImageButton shareImageButton;

    private static Map<String, Share> shares = new HashMap<>(); //key: id

    @RequiresApi(api = Build.VERSION_CODES.O)
    @SuppressLint("ResourceType")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shared_vehicles);

        //vehicleID = (String) this.getIntent().getStringExtra("vehicleID");
        vehicleID = "303";
        Log.d(TAG, "vehicleID: " + vehicleID);
        //vehicle = UserInfo.getVehicles().get(vehicleID);
        Log.d(TAG, "vehicle: " + vehicle);

        backImageButton = $(R.id.backImageButton);
        backImageButton.setOnClickListener(v -> {
            Intent intent = new Intent(SharedVehiclesActivity.this, EditVehicleActivity.class);
            intent.putExtra("vehicleID", vehicleID);
            startActivity(intent);
        });

        headerTextView = $(R.id.headerTextView);
        //headerTextView.setText(vehicle.getMake_name() + " - " + vehicle.getRegistration_no());

        vehicleImageView = $(R.id.vehicleImageView);
        //vehicleImageView.setImageBitmap(vehicle.getImage());


        shareLayout = $(R.id.sharesLayout);
        returnSharedList(vehicleID, new sharedCallbacks() {
            @Override
            public void onSuccess(@NonNull Map<String, Share> value) {
                Log.e("map", String.valueOf(shares.size()));

                for (String shareID : shares.keySet()) {
                    ConstraintLayout shareLineLayout = new ConstraintLayout(SharedVehiclesActivity.this);
                    ConstraintSet set = new ConstraintSet();

                    ImageView bgImageView = new ImageView(SharedVehiclesActivity.this);
                    bgImageView.setId(0);
                    bgImageView.setBackgroundColor(0xFF89D0D5);
                    set.connect(bgImageView.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, 16);
                    set.connect(bgImageView.getId(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START);
                    set.connect(bgImageView.getId(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END);
                    set.setDimensionRatio(bgImageView.getId(), "4.3:1");
                    shareLineLayout.addView(bgImageView);

                    ImageButton editImageButton = new ImageButton(SharedVehiclesActivity.this);
                    editImageButton.setId(1);
                    editImageButton.setImageDrawable(getResources().getDrawable(R.drawable.share_vehicle0edit));
                    editImageButton.setPadding(0, 0, 0, 0);
                    editImageButton.setScaleType(ImageView.ScaleType.FIT_XY);
                    editImageButton.setBackground(null);
                    editImageButton.setOnClickListener(v -> editShare(shareID));
                    set.connect(editImageButton.getId(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END, 32);
                    set.connect(editImageButton.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP);
                    set.connect(editImageButton.getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM);
                    set.setDimensionRatio(editImageButton.getId(), "1:1");
                    set.constrainPercentHeight(editImageButton.getId(), 0.33f);
                    shareLineLayout.addView(editImageButton);

                    String companyName = shares.get(shareID).getCompany_name();
                    String custID = shares.get(shareID).getCust_id();
                    TextView companyTextView = new TextView(SharedVehiclesActivity.this);
                    companyTextView.setId(2);
                    companyTextView.setAutoSizeTextTypeUniformWithConfiguration(14, 30, 1, TypedValue.COMPLEX_UNIT_SP);
                    String temp = companyName + " - <font color='#00FFFF'>" + custID + "</font>";
                    companyTextView.setText(Html.fromHtml(temp));
                    companyTextView.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
                    set.connect(companyTextView.getId(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START, 8);
                    set.connect(companyTextView.getId(), ConstraintSet.END, editImageButton.getId(), ConstraintSet.START, 8);
                    set.connect(companyTextView.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, 8);
                    set.connect(companyTextView.getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM);
                    set.setVerticalBias(companyTextView.getId(), 0.0f);
                    set.constrainPercentHeight(companyTextView.getId(), 0.3f);
                    shareLineLayout.addView(companyTextView);

                    //if(share.isShare) {
                    if (true) {
                        SimpleDateFormat formatDate = new SimpleDateFormat("ddMMM yyyy");
                        SimpleDateFormat formatTime = new SimpleDateFormat("HH:mm");

                        TextView startTextView = new TextView(SharedVehiclesActivity.this);
                        startTextView.setId(3);
                        startTextView.setAutoSizeTextTypeUniformWithConfiguration(10, 30, 1, TypedValue.COMPLEX_UNIT_SP);
                        startTextView.setText("Start " + formatTime.format(shares.get(shareID).getStart_time()));
                        set.connect(startTextView.getId(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START, 16);
                        set.connect(startTextView.getId(), ConstraintSet.END, editImageButton.getId(), ConstraintSet.START, 8);
                        set.connect(startTextView.getId(), ConstraintSet.TOP, companyTextView.getId(), ConstraintSet.BOTTOM);
                        set.connect(startTextView.getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM);
                        set.setVerticalBias(startTextView.getId(), 0.0f);
                        set.constrainPercentHeight(startTextView.getId(), 0.2f);
                        shareLineLayout.addView(startTextView);

                        TextView endTextView = new TextView(SharedVehiclesActivity.this);
                        endTextView.setId(4);
                        endTextView.setAutoSizeTextTypeUniformWithConfiguration(10, 30, 1, TypedValue.COMPLEX_UNIT_SP);
                        endTextView.setText("End   " + formatTime.format(shares.get(shareID).getEnd_time()));
                        set.connect(endTextView.getId(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START, 16);
                        set.connect(endTextView.getId(), ConstraintSet.END, editImageButton.getId(), ConstraintSet.START, 8);
                        set.connect(endTextView.getId(), ConstraintSet.TOP, startTextView.getId(), ConstraintSet.BOTTOM);
                        set.connect(endTextView.getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM);
                        set.setVerticalBias(endTextView.getId(), 0.0f);
                        set.constrainPercentHeight(endTextView.getId(), 0.2f);
                        shareLineLayout.addView(endTextView);

                        TextView recurringTextView = new TextView(SharedVehiclesActivity.this);
                        recurringTextView.setId(5);
                        recurringTextView.setAutoSizeTextTypeUniformWithConfiguration(10, 30, 1, TypedValue.COMPLEX_UNIT_SP);
                        StringBuilder sb = new StringBuilder();
                        if (shares.get(shareID).isRecurring()) {
                            sb.append("Recurring: ");
                            for (int i = 0; i < 7; i++) {
                                if (shares.get(shareID).getRecurring_days()[i]) {
                                    switch (i) {
                                        case 0:
                                            sb.append("Sun, ");
                                            break;
                                        case 1:
                                            sb.append("Mon, ");
                                            break;
                                        case 2:
                                            sb.append("Tue, ");
                                            break;
                                        case 3:
                                            sb.append("Wed, ");
                                            break;
                                        case 4:
                                            sb.append("Thu, ");
                                            break;
                                        case 5:
                                            sb.append("Fri, ");
                                            break;
                                        case 6:
                                            sb.append("Sat, ");
                                            break;
                                    }
                                }
                            }
                            sb.append("till " + formatDate.format(shares.get(shareID).getRecurring_end_date()));
                        } else {

                        }
                        recurringTextView.setText(sb);
                        set.connect(recurringTextView.getId(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START, 16);
                        set.connect(recurringTextView.getId(), ConstraintSet.END, editImageButton.getId(), ConstraintSet.START, 8);
                        set.connect(recurringTextView.getId(), ConstraintSet.TOP, endTextView.getId(), ConstraintSet.BOTTOM);
                        set.connect(recurringTextView.getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM);
                        set.setVerticalBias(recurringTextView.getId(), 0.0f);
                        set.constrainPercentHeight(recurringTextView.getId(), 0.2f);
                        shareLineLayout.addView(recurringTextView);

                    } else {
                        TextView offTextView = new TextView(SharedVehiclesActivity.this);
                        offTextView.setId(6);
                        offTextView.setAutoSizeTextTypeUniformWithConfiguration(10, 30, 1, TypedValue.COMPLEX_UNIT_SP);
                        offTextView.setText("OFF");
                        offTextView.setTextColor(0xFF444444);
                        set.connect(offTextView.getId(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START, 16);
                        set.connect(offTextView.getId(), ConstraintSet.END, editImageButton.getId(), ConstraintSet.START);
                        set.connect(offTextView.getId(), ConstraintSet.TOP, companyTextView.getId(), ConstraintSet.BOTTOM);
                        set.connect(offTextView.getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM);
                        set.constrainPercentHeight(offTextView.getId(), 0.3f);
                        shareLineLayout.addView(offTextView);
                    }

                    set.applyTo(shareLineLayout);
                    shareLayout.addView(shareLineLayout);

                }

            }

            @Override
            public void onError(@NonNull String errorMessage) {

            }
        });

        shareImageButton = $(R.id.shareImageButton);
        shareImageButton.setOnClickListener(v -> {
            addShare();
        });

    }

    private void addShare() {
        Log.d(TAG, "addShare: " + vehicleID);
        Intent intent = new Intent(SharedVehiclesActivity.this, ShareVehicleDetailActivity.class);
        intent.putExtra("vehicleID", vehicleID);
        intent.putExtra("NEW", true);
        startActivity(intent);
    }

    private void editShare(String shareID) {
        Log.d(TAG, "editShare: " + vehicleID);
        Log.d(TAG, "editShare: shareID: " + shareID);
        Intent intent = new Intent(SharedVehiclesActivity.this, ShareVehicleDetailActivity.class);
        intent.putExtra("vehicleID", vehicleID);
        intent.putExtra("shareID", shareID);
        intent.putExtra("NEW", false);
        startActivity(intent);
    }

    private <T extends View> T $(int id) {
        return (T) findViewById(id);
    }

    private void returnSharedList(String vehicleID, @Nullable final sharedCallbacks callbacks) {

        String URL = IP_HOST + GET_SHARED_LIST + vehicleID;

        JsonObjectRequest objectRequest = new JsonObjectRequest(Request.Method.GET, URL, null, response -> {
            Log.e("Response", response.toString());
            JSONArray jsonArray;
            JSONObject jsonObject;
            try {
                jsonArray = response.getJSONArray("result");
                for (int i = 0; i < jsonArray.length(); i++) {
                    jsonObject = jsonArray.getJSONObject(i);

                    Share share = new Share();

                    try {
                        share.setShare_id(jsonObject.optString("share_id"));
                        String recurring_flag = jsonObject.optString("recurring_flag");
                        if (recurring_flag.equals("1")) {
                            share.setRecurring(true);
                            share.setRecurring_end_date(new SimpleDateFormat("yyyy-MM-dd").parse(jsonObject.optString("recurring_end_date")));
                            String recurringDaysStr = jsonObject.optString("recurring_days");
                            boolean[] recurringDays = new boolean[] {false, false, false, false, false, false, false};
                            for (char c : recurringDaysStr.toCharArray()) recurringDays[c-'0'] = true;
                            share.setRecurring_days(recurringDays);
                        } else {
                            share.setRecurring(false);
                            share.setDate(new SimpleDateFormat("yyyy-MM-dd").parse(jsonObject.optString("date")));
                        }
                        share.setCust_id(jsonObject.optString("cust_id"));
                        share.setCompany_name(jsonObject.optString("company_name"));
                        share.setStart_time(new SimpleDateFormat("HH:mm:ss").parse(jsonObject.optString("start_time")));
                        share.setEnd_time(new SimpleDateFormat("HH:mm:ss").parse(jsonObject.optString("end_time")));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    shares.put(share.getShare_id(), share);

                }
                if (callbacks != null)
                    callbacks.onSuccess(shares);
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
                if (callbacks != null)
                    callbacks.onError(message);
            }

        });

        Volley.newRequestQueue(SharedVehiclesActivity.this).add(objectRequest);
    }

    public interface sharedCallbacks {
        void onSuccess(@NonNull Map<String, Share> value);

        void onError(@NonNull String errorMessage);
    }

}
