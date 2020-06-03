package com.example.wisecarapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.concurrent.ConcurrentHashMap;

public class ServiceRecordsActivity extends AppCompatActivity {

    private static final String TAG = "Service Records";

    private Vehicle vehicle;

    private TextView serviceIDTextView;
    private ImageView qrImageView;
    private Button uploadButton;
    private ImageButton cameraImageButton;

    private EditText dateEditText;
    private EditText centreEditText;
    private EditText refNoEditText;
    private EditText notesEditText;
    private EditText nextDateEditText;
    private EditText nextDistanceEditText;
    private CheckBox oilCheckBox;
    private CheckBox brakeCheckBox;
    private CheckBox batteryCheckBox;
    private CheckBox coolingCheckBox;
    private CheckBox lightsCheckBox;

    private java.util.Date date;
    private String centre;
    private String RefNo;
    private String notes;
    private java.util.Date nextDate;
    private double nextDistance;
    private boolean isOil;
    private boolean isBrake;
    private boolean isBattery;
    private boolean isCooling;
    private boolean isLights;

    private ImageButton saveImageButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_records);

        String vehicleID = (String) this.getIntent().getStringExtra("vehicleID");
        Log.d(TAG, "vehicleID: " + vehicleID);
        vehicle = UserInfo.getVehicles().get(vehicleID);
        Log.d(TAG, "vehicle: " + vehicle);

        serviceIDTextView = (TextView) findViewById(R.id.serviceTextView);
        qrImageView = (ImageView) findViewById(R.id.qrImageView);
        uploadButton = (Button) findViewById(R.id.uploadButton);
        cameraImageButton = (ImageButton) findViewById(R.id.cameraImageButton);
        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "upload record: ");
            }
        });
        cameraImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "camera: ");
            }
        });

        dateEditText = (EditText) findViewById(R.id.dateEditText);
        centreEditText = (EditText) findViewById(R.id.centreEditText);
        refNoEditText = (EditText) findViewById(R.id.refNoEditText);
        notesEditText = (EditText) findViewById(R.id.notesEditText);
        nextDateEditText = (EditText) findViewById(R.id.nextDateEditText);
        nextDistanceEditText = (EditText) findViewById(R.id.nextDistanceEditText);
        oilCheckBox = (CheckBox) findViewById(R.id.oilCheckBox);
        brakeCheckBox = (CheckBox) findViewById(R.id.brakeCheckBox);
        batteryCheckBox = (CheckBox) findViewById(R.id.batteryCheckBox);
        coolingCheckBox = (CheckBox) findViewById(R.id.coolingCheckBox);
        lightsCheckBox = (CheckBox) findViewById(R.id.lightsCheckBox);

        saveImageButton = (ImageButton) findViewById(R.id.saveImageButton);
        saveImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "save: ");
            }
        });

    }

    public boolean dispatchTouchEvent(MotionEvent ev) {
        View v = getCurrentFocus();
        if (isShouldHideInput(v, ev)) {
            hideSoftInput(v.getWindowToken());
        }
        return super.dispatchTouchEvent(ev);
    }
    private boolean isShouldHideInput(View v, MotionEvent event) {
        if(v != null && (v instanceof EditText)) {
            int[] l = {0, 0};
            v.getLocationInWindow(l);
            int left = l[0],
                    top = l[1],
                    bottom = top + v.getHeight(),
                    right = left + v.getWidth();
            return !(event.getX()>left && event.getX()<right
                    && event.getY()>top && event.getY()<bottom);
        }
        return false;
    }
    private void hideSoftInput(IBinder token) {
        if (token != null) {
            InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            manager.hideSoftInputFromWindow(token, InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

}
