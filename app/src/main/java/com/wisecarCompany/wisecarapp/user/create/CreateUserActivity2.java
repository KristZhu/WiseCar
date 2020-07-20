package com.wisecarCompany.wisecarapp.user.create;

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
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.wisecarCompany.wisecarapp.R;
import com.wisecarCompany.wisecarapp.user.login.LoginActivity;

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
import java.util.Arrays;
import java.util.Calendar;
import java.util.Locale;


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

        firstNameEditText = $(R.id.userFNameEditText);
        lastNameEditText = $(R.id.userLNameEditText);
        dobEditText = $(R.id.dobEditText);
        dobEditText.setInputType(InputType.TYPE_NULL);
        licenceEditText = $(R.id.licenceEditText);
        address1EditText = $(R.id.address1EditText);
        address2EditText = $(R.id.address2EditText);
        countryEditText = $(R.id.countryEditText);
        stateEditText = $(R.id.stateEditText);
        postCodeEditText = $(R.id.postCodeEditText);
        createImageButton = $(R.id.createImageButton);

        dobEditText.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new DatePickerDialog(CreateUserActivity2.this, (view, year, monthOfYear, dayOfMonth) -> {
                dob = intToDate(year, monthOfYear, dayOfMonth);
                SimpleDateFormat format = new SimpleDateFormat("ddMMM yyyy", Locale.getDefault());
                String str = format.format(dob);
                dobEditText.setText(str);
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
        });
        dobEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                Calendar c = Calendar.getInstance();
                new DatePickerDialog(CreateUserActivity2.this, (view, year, monthOfYear, dayOfMonth) -> {
                    dob = intToDate(year, monthOfYear, dayOfMonth);
                    SimpleDateFormat format = new SimpleDateFormat("ddMMM yyyy", Locale.getDefault());
                    String str = format.format(dob);
                    dobEditText.setText(str);
                }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();

            }
        });


        createImageButton.setOnClickListener(v -> {
            firstName = firstNameEditText.getText().toString();
            lastName = lastNameEditText.getText().toString();
            licence = licenceEditText.getText().toString();
            address1 = address1EditText.getText().toString();
            address2 = address2EditText.getText().toString();
            country = countryEditText.getText().toString();
            state = stateEditText.getText().toString();
            postCode = postCodeEditText.getText().toString();

            Log.d(TAG, "--------------------Create User------------------");
            Log.d(TAG, "userImg: " + Arrays.toString(userImg));
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
        if (v instanceof EditText) {
            int[] l = {0, 0};
            v.getLocationInWindow(l);
            int left = l[0],
                    top = l[1],
                    bottom = top + v.getHeight(),
                    right = left + v.getWidth();
            return !(event.getX() > left && event.getX() < right
                    && event.getY() > top && event.getY() < bottom);
        }
        return false;
    }

    private void hideSoftInput(IBinder token) {
        if (token != null) {
            InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            assert manager != null;
            manager.hideSoftInputFromWindow(token, InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    private static java.util.Date strToDate(String str) {
        if (str == null || str.length() == 0) return null;
        SimpleDateFormat format = new SimpleDateFormat("ddMMM yyyy", Locale.getDefault());
        java.util.Date date = null;
        try {
            date = format.parse(str);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    private static java.util.Date intToDate(int year, int month, int day) {
        StringBuilder sb = new StringBuilder();
        if (day < 10) sb.append("0").append(day);
        else sb.append(day);
        sb.append("/");
        month++;
        if (month < 10) sb.append("0").append(month);
        else sb.append(month);
        sb.append("/").append(year);
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        java.util.Date date = null;
        try {
            date = format.parse(sb.toString());
        } catch (ParseException e) {
            return null;
        }
        return date;
    }

    private void uploadByHttpClient() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                HttpClient httpClient = new DefaultHttpClient();
                HttpPost postRequest = new HttpPost(IP_HOST + CREATE_USER);

                MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
                DateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
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
                    if (s.toString().contains("success")) {
                        runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(getApplicationContext(), "success", Toast.LENGTH_SHORT).show();
                            }
                        });
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

                postRequest.abort();
                httpClient.getConnectionManager().shutdown();

            }

        });
        thread.start();
    }

    private <T extends View> T $(int id){
        return (T) findViewById(id);
    }

}
