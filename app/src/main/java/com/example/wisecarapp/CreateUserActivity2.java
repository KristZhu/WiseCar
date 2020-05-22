package com.example.wisecarapp;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.IBinder;
import android.text.InputType;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TimePicker;

import java.util.Calendar;

public class CreateUserActivity2 extends AppCompatActivity {

    private final static String TAG = "CreateUser2";

    private String userImg;
    private String username;
    private String userEmail;
    private String password;
    private String firstName;
    private String lastName;
    private String dob;
    private String licence;
    private String address1;
    private String address2;
    private String country;
    private String state;
    private String postCode;

    private EditText firstNameEditText;
    private EditText lastNameEditText;
    private EditText dobEditText;
    private EditText licenceEditText;
    private EditText address1EditText;
    private EditText address2EditText;
    private EditText countryEditText;
    private EditText stateEditText;
    private EditText postCodeEditText;
    private ImageButton createImageButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_user2);

        userImg = this.getIntent().getStringExtra("userImg");
        username = this.getIntent().getStringExtra("username");
        userEmail = this.getIntent().getStringExtra("userEmail");
        password = this.getIntent().getStringExtra("password");

        firstNameEditText = (EditText) findViewById(R.id.userFNameEditText);
        lastNameEditText = (EditText) findViewById(R.id.userLNameEditText);
        dobEditText = (EditText) findViewById(R.id.dobEditText);
        licenceEditText = (EditText) findViewById(R.id.licenceEditText);
        address1EditText = (EditText) findViewById(R.id.address1EditText);
        address2EditText = (EditText) findViewById(R.id.address2EditText);
        countryEditText = (EditText) findViewById(R.id.countryEditText);
        stateEditText = (EditText) findViewById(R.id.stateEditText);
        postCodeEditText = (EditText) findViewById(R.id.postCodeEditText);
        createImageButton = (ImageButton) findViewById(R.id.createImageButton);

        dobEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
/*
                AlertDialog.Builder builder = new AlertDialog.Builder(CreateUserActivity2.this);
                View view = View.inflate(CreateUserActivity2.this, R.layout.activity_create_user2, null);
                final DatePicker datePicker = (DatePicker) view.findViewById(R.id.date_picker);
                builder.setView(view);
                Calendar cal = Calendar.getInstance();
                cal.setTimeInMillis(System.currentTimeMillis());
                datePicker.init(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), null);

                final int inType = dobEditText.getInputType();
                dobEditText.setSelection(dobEditText.getText().length());
                builder.setTitle("选取起始时间");
                builder.setPositiveButton("确  定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        StringBuffer sb = new StringBuffer();
                        sb.append(String.format("%d-%02d-%02d",
                                datePicker.getYear(),
                                datePicker.getMonth() + 1,
                                datePicker.getDayOfMonth()));
                        dobEditText.setText(sb);
                        dialog.cancel();
                    }
                });

 */

            }
        });


        createImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firstName = firstNameEditText.getText().toString();
                lastName = lastNameEditText.getText().toString();
                dob = dobEditText.getText().toString();
                licence = licenceEditText.getText().toString();
                address1 = address1EditText.getText().toString();
                address2 = address2EditText.getText().toString();
                country = countryEditText.getText().toString();
                state = stateEditText.getText().toString();
                postCode = postCodeEditText.getText().toString();

                Log.d(TAG, "--------------------Create User------------------");
                Log.d(TAG, "userImg: " + userImg);
                Log.d(TAG, "username: " + username);
                Log.d(TAG, "userEmail: " + userEmail);
                Log.d(TAG, "password: " + password);
                Log.d(TAG, "FName: " + firstName);
                Log.d(TAG, "LName: " + lastName);
                Log.d(TAG, "dob: " + dob);
                Log.d(TAG, "licence: " + licence);
                Log.d(TAG, "address1: " + address1);
                Log.d(TAG, "address2: " + address2);
                Log.d(TAG, "country: " + country);
                Log.d(TAG, "state: " + state);
                Log.d(TAG, "postCode: " + postCode);

                // Write database connection here



            }
        });

    }

    public boolean dispatchTouchEvent(MotionEvent ev) {
        View v = getCurrentFocus();
        if(HideKeyBoard.isShouldHideInput(v, ev)) {
            hideSoftInput(v.getWindowToken());
        }
        return super.dispatchTouchEvent(ev);
    }
    private void hideSoftInput(IBinder token) {
        if (token != null) {
            InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            manager.hideSoftInputFromWindow(token, InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

}
