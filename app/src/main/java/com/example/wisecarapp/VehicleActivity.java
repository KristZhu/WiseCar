package com.example.wisecarapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.Layout;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class VehicleActivity extends AppCompatActivity {

    private static final String TAG = "Vehicle";

    private String username;
    private TextView usernameTextView;
    private String userEmail;
    private TextView userEmailTextView;
    private ImageView userImgImageView;

    private ImageButton settingImageButton;
    private ImageButton editImageButton;
    private ImageButton dashboardImageButton;
    private Button dashboardButton;
    private ImageButton calendarImageButton;
    private Button calendarButton;
    private ImageButton inboxImageButton;
    private Button inboxButton;

    private LinearLayout vehicleLayout;
    private ImageButton editVehicleImageButton;
    private ImageButton addImageButton;
    private ImageButton manageImageButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vehicle);

        //username = UserInfo.getUsername();
        usernameTextView = (TextView) findViewById(R.id.usernameTextView);
        usernameTextView.setText(username);
        userEmailTextView = (TextView) findViewById(R.id.userEmailTextView);
        userEmailTextView.setText(userEmail);
        userImgImageView = (ImageView) findViewById(R.id.userImgImageView);
        //userImgImageView.setImageDrawable();

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


        vehicleLayout = (LinearLayout) findViewById(R.id.vehicleLayout);
        editVehicleImageButton = (ImageButton) findViewById(R.id.editVehicleImageButton);
        addImageButton = (ImageButton) findViewById(R.id.addImageButton);
        manageImageButton = (ImageButton) findViewById(R.id.manageImageButton);

        List<String> strings = new ArrayList<>();
        strings.add("test0");
        strings.add("test1");
        strings.add("test2");
        strings.add("test3");
        strings.add("test4");
        strings.add("test5");

        if(strings.size()==0) {
            Log.d(TAG, "onCreate: no vehicles");
        } else {
            for(String s: strings) {
                CircleImageView imageView = new CircleImageView(VehicleActivity.this);
                imageView.setImageDrawable(userImgImageView.getDrawable());
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                params.setMargins(0, 0, 16, 0);
                imageView.setLayoutParams(params);
                vehicleLayout.addView(imageView);
            }
        }

    }

    private void startDashboard() {

    }

    private void startCalendar() {

    }

    private void startInbox() {

    }
}
