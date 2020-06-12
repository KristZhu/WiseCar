package com.example.wisecarapp;

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

import java.util.LinkedList;
import java.util.List;

public class SharedVehiclesActivity extends AppCompatActivity {

    private final static String TAG = "SharedVehicles";

    private String vehicleID;
    private Vehicle vehicle;

    private ImageButton backImageButton;
    private TextView headerTextView;
    private ImageView vehicleImageView;

    private LinearLayout shareLayout;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @SuppressLint("ResourceType")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shared_vehicles);

        //vehicleID = (String) this.getIntent().getStringExtra("vehicleID");
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
        List<Integer> shares = new LinkedList<>();
        shares.add(1); shares.add(2);
        for(int share: shares) {
            ConstraintLayout shareLineLayout = new ConstraintLayout(SharedVehiclesActivity.this);
            ConstraintSet set = new ConstraintSet();

            ImageView bgImageView = new ImageView(SharedVehiclesActivity.this);
            bgImageView.setId(0);
            bgImageView.setBackgroundColor(0xFFFFFFFF);
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
            editImageButton.setOnClickListener(v -> {

            });
            set.connect(editImageButton.getId(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END, 32);
            set.connect(editImageButton.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP);
            set.connect(editImageButton.getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM);
            set.setDimensionRatio(editImageButton.getId(), "1:1");
            set.constrainPercentHeight(editImageButton.getId(), 0.33f);
            shareLineLayout.addView(editImageButton);

            String companyName = "abc";//share.getCompanyName;
            String companyID = "123";//share.getCompanyID;
            TextView companyTextView = new TextView(SharedVehiclesActivity.this);
            companyTextView.setId(2);
            companyTextView.setAutoSizeTextTypeUniformWithConfiguration(14, 30, 1, TypedValue.COMPLEX_UNIT_SP);
            String temp = companyName + " - <font color='#00FFFF'>" + companyID + "</font>";
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
            if(true) {
                TextView startTextView = new TextView(SharedVehiclesActivity.this);
                startTextView.setId(3);
                startTextView.setAutoSizeTextTypeUniformWithConfiguration(10, 30, 1, TypedValue.COMPLEX_UNIT_SP);
                //companyTextView.setText("Start " + share.getStart());
                startTextView.setText("Start 3:00");
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
                //companyTextView.setText("End " + share.getEnd());
                endTextView.setText("End   3:00");
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
                //companyTextView.setText("Recurring " + share.get...());
                recurringTextView.setText("Recurring ...");
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
                //companyTextView.setText("Recurring " + share.get...());
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

    private <T extends View> T $(int id) {
        return (T) findViewById(id);
    }
}
