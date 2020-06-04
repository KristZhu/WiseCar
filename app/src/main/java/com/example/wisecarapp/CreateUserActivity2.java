package com.example.wisecarapp;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
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
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;


public class CreateUserActivity2 extends AppCompatActivity {

    private final static String TAG = "CreateUser2";

    private byte[] userImg;
    private String username;
    private String userEmail;
    private String password;
    private String firstName;
    private String lastName;
    private java.util.Date dob;
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

    private final String IP_HOST = "http://54.206.19.123:3000";
    private final String CREATE_USER = "/api/v1/users/register";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_user2);

        userImg = (byte[]) this.getIntent().getSerializableExtra("userImg");
        username = this.getIntent().getStringExtra("username");
        userEmail = this.getIntent().getStringExtra("userEmail");
        password = this.getIntent().getStringExtra("password");

        firstNameEditText = (EditText) findViewById(R.id.userFNameEditText);
        lastNameEditText = (EditText) findViewById(R.id.userLNameEditText);
        dobEditText = (EditText) findViewById(R.id.dobEditText);
        dobEditText.setInputType(InputType.TYPE_NULL);
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
                Calendar c = Calendar.getInstance();
                new DatePickerDialog(CreateUserActivity2.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        dob = intToDate(year, monthOfYear, dayOfMonth);
                        dobEditText.setText(dateToStr(dob));
                    }
                }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
        dobEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    Calendar c = Calendar.getInstance();
                    new DatePickerDialog(CreateUserActivity2.this, new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                            dob = intToDate(year, monthOfYear, dayOfMonth);
                            dobEditText.setText(dateToStr(dob));
                        }
                    }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();

                }
            }
        });


        createImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firstName = firstNameEditText.getText().toString();
                lastName = lastNameEditText.getText().toString();
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


                if (!username.equals("")
                        && !userEmail.equals("")
                        && !password.equals("")
                        && !firstName.equals("")
                        && !lastName.equals("")
                        && dob != null && dob.before(new java.util.Date())
                        && !licence.equals("")
                        && !address1.equals("")
                        && !country.equals("")
                        && !state.equals("") && !postCode.equals("")
                ) {

                    uploadByHttpClient();

                } else {
                    if (firstName.equals(""))
                        Toast.makeText(getApplicationContext(), "Please enter first name", Toast.LENGTH_LONG).show();
                    else if (lastName.equals(""))
                        Toast.makeText(getApplicationContext(), "Please enter last name", Toast.LENGTH_LONG).show();
                    else if (dob == null || dob.compareTo(new java.util.Date()) > 0)
                        Toast.makeText(getApplicationContext(), "Please select date of birth correctly", Toast.LENGTH_LONG).show();
                    else if (licence.equals(""))
                        Toast.makeText(getApplicationContext(), "Please enter driver licence", Toast.LENGTH_LONG).show();
                    else if (address1.equals(""))
                        Toast.makeText(getApplicationContext(), "Please enter address", Toast.LENGTH_LONG).show();
                    else if (country.equals(""))
                        Toast.makeText(getApplicationContext(), "Please enter country", Toast.LENGTH_LONG).show();
                    else if (state.equals(""))
                        Toast.makeText(getApplicationContext(), "Please enter state", Toast.LENGTH_LONG).show();
                    else if (postCode.equals(""))
                        Toast.makeText(getApplicationContext(), "Please enter post code", Toast.LENGTH_LONG).show();
                    else
                        Toast.makeText(getApplicationContext(), "Creation fail", Toast.LENGTH_LONG).show();
                }

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

    private static java.util.Date strToDate(String str) {
        if (str == null || str.length() == 0) return null;
        SimpleDateFormat format = new SimpleDateFormat("ddMMM yyyy");
        java.util.Date date = null;
        try {
            date = format.parse(str);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    private static java.util.Date intToDate(int year, int month, int day) {
        StringBuffer sb = new StringBuffer();
        if (day < 10) sb.append("0" + day);
        else sb.append(day);
        sb.append("/");
        month++;
        if (month < 10) sb.append("0" + month);
        else sb.append(month);
        sb.append("/" + year);
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
        java.util.Date date = null;
        try {
            date = format.parse(sb.toString());
        } catch (ParseException e) {
            return null;
        }
        return date;
    }

    private static String dateToStr(java.util.Date date) {
        SimpleDateFormat format = new SimpleDateFormat("ddMMM yyyy");
        String str = format.format(date);
        return str;
    }

    private static java.sql.Date utilDateToSqlDate(java.util.Date date) {
        return new java.sql.Date(date.getTime());
    }

    private static java.util.Date sqlDateToUtilDate(java.sql.Date date) {
        return new java.util.Date(date.getTime());
    }

    private void uploadByHttpClient() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                HttpClient httpClient = new DefaultHttpClient();
                HttpPost postRequest = new HttpPost(IP_HOST + CREATE_USER);

                MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
                DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                try {
                    reqEntity.addPart("user_name", new StringBody(username));
                    reqEntity.addPart("first_name", new StringBody(firstName));
                    reqEntity.addPart("last_name", new StringBody(lastName));
                    reqEntity.addPart("date_of_birth", new StringBody(format.format(dob)));
                    reqEntity.addPart("driver_license", new StringBody(licence));
                    reqEntity.addPart("address_line1", new StringBody(address1));
                    reqEntity.addPart("address_line2", new StringBody(address2));
                    reqEntity.addPart("postcode", new StringBody(postCode));
                    reqEntity.addPart("state", new StringBody(state));
                    reqEntity.addPart("country", new StringBody(country));
                    reqEntity.addPart("email", new StringBody(userEmail));
                    reqEntity.addPart("password", new StringBody(password));

                    ByteArrayBody userImgBody = new ByteArrayBody(userImg, ContentType.IMAGE_PNG, "logo.png");
                    reqEntity.addPart("logo", userImgBody);

                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    try {
                        reqEntity.addPart("logo", new StringBody("image error"));
                    } catch (UnsupportedEncodingException ex) {
                        ex.printStackTrace();
                    }
                }

                postRequest.setEntity(reqEntity);
                HttpResponse response = null;
                StringBuilder s = new StringBuilder();
                try {
                    response = httpClient.execute(postRequest);
                    BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
                    String sResponse;
                    while ((sResponse = reader.readLine()) != null) {
                        s = s.append(sResponse);
                    }
                    if(s.toString().contains("success")){
                        Intent intent = new Intent(CreateUserActivity2.this, LoginActivity.class);
                        int position = s.indexOf("user_id");
                        Log.e("user_id test: ", "\"" + s.substring(position + 9, s.length() - 1) + "\"");
                        intent.putExtra("user_id", s.substring(position + 9, s.length() - 1));
                        startActivity(intent);
                    }
                    Log.e("response", s.toString());
                } catch (IOException e) {
                    e.printStackTrace();
                }

//                StringBuilder finalS = s;
//                runOnUiThread(new Runnable() {
//                    public void run() {
//                        Toast.makeText(CreateUserActivity2.this, finalS.toString(), Toast.LENGTH_LONG).show();
//                    }
//                });

                postRequest.abort();

            }

        });
        thread.start();
    }

}
