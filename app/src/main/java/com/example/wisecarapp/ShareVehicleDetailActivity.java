package com.example.wisecarapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

public class ShareVehicleDetailActivity extends AppCompatActivity {

    private static final String TAG = "ShareVehicleDetail";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_vehicle_detail);

        SwitchButton switchButton = (SwitchButton) findViewById(R.id.shareSwitchButton);
        //switchButton.setToggleOn(true);
        switchButton.setOnToggleChanged(new SwitchButton.OnToggleChanged(){
            @Override
            public void onToggle(boolean isOn) {
                Log.d(TAG, "isOn: " + isOn);
            }
        });

    }
}
