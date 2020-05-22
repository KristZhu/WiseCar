package com.example.wisecarapp;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
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
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_user2);

        userImg = (byte[])this.getIntent().getSerializableExtra("userImg");
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
                        dobEditText.setText(dateToStr(intToDate(year,monthOfYear,dayOfMonth)));
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
                            dobEditText.setText(dateToStr(intToDate(year,monthOfYear,dayOfMonth)));
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
                dob = strToDate(dobEditText.getText().toString());
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

                //userImg -> byte[]
                //jsonParam.put("image", userImg);

                if (!username.equals("")
                        && !userEmail.equals("")
                        && !password.equals("")
                        && !firstName.equals("")
                        && !lastName.equals("")
                        && dob!=null && dob.before(new java.util.Date())
                        && !licence.equals("")
                        && !address1.equals("")
                        && !country.equals("")
                        && !state.equals("") && !postCode.equals("")
                ) {
                    String URL = "http://54.206.19.123:3000/api/v1/users/register";
                    final JSONObject jsonParam = new JSONObject();
                    try {
                        jsonParam.put("user_name", username);
                        jsonParam.put("first_name", firstName);
                        jsonParam.put("last_name", lastName);
                        jsonParam.put("date_of_birth", utilDateToSqlDate(dob));
                        jsonParam.put("driver_license", licence);
                        jsonParam.put("address_line1", address1);
                        jsonParam.put("address_line2", address2);
                        jsonParam.put("postcode", postCode);
                        jsonParam.put("state", state);
                        jsonParam.put("country", country);
                        jsonParam.put("email", userEmail);
                        jsonParam.put("password", password);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    JsonObjectRequest objectRequest = new JsonObjectRequest(Request.Method.POST, URL, jsonParam, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Log.e("Response", response.toString());
                            Toast.makeText(getApplicationContext(), response.optString("message"), Toast.LENGTH_LONG).show();
                            if (!response.optString("message").equals("success")) {

                                // Register successfully

                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e("ERROR!!!", error.toString());
                            Log.e("ERROR!!!", String.valueOf(error.networkResponse));

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
                                Log.e("JSON ERROR MESSAGE!!!", message);
                                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
                            }

                        }
                    });

                    Volley.newRequestQueue(CreateUserActivity2.this).add(objectRequest);

                } else {
                    if(firstName.equals("")) Toast.makeText(getApplicationContext(), "Please enter first name", Toast.LENGTH_LONG).show();
                    else if(lastName.equals("")) Toast.makeText(getApplicationContext(), "Please enter last name", Toast.LENGTH_LONG).show();
                    else if(dob==null || dob.compareTo(new java.util.Date())>0) Toast.makeText(getApplicationContext(), "Please select date of birth correctly", Toast.LENGTH_LONG).show();
                    else if(licence.equals("")) Toast.makeText(getApplicationContext(), "Please enter driver licence", Toast.LENGTH_LONG).show();
                    else if(address1.equals("")) Toast.makeText(getApplicationContext(), "Please enter address", Toast.LENGTH_LONG).show();
                    else if(country.equals("")) Toast.makeText(getApplicationContext(), "Please enter country", Toast.LENGTH_LONG).show();
                    else if(state.equals("")) Toast.makeText(getApplicationContext(), "Please enter state", Toast.LENGTH_LONG).show();
                    else if(postCode.equals("")) Toast.makeText(getApplicationContext(), "Please enter post code", Toast.LENGTH_LONG).show();
                    else Toast.makeText(getApplicationContext(), "Creation fail", Toast.LENGTH_LONG).show();
                }

            }
        });

    }

    public boolean dispatchTouchEvent(MotionEvent ev) {
        View v = getCurrentFocus();
        if (HideKeyBoard.isShouldHideInput(v, ev)) {
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

    private static java.util.Date strToDate(String str) {
        if(str==null || str.length()==0) return null;
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
        if(day<10) sb.append("0" + day);
        else sb.append(day);
        sb.append("/");
        month++;
        if(month<10) sb.append("0" + month);
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
    private static String dateToStr (java.util.Date date) {
        SimpleDateFormat format = new SimpleDateFormat("ddMMM yyyy");
        String str = format.format(date);
        return str;
    }
    private static java.sql.Date utilDateToSqlDate (java.util.Date date) {
        return new java.sql.Date(date.getTime());
    }
    private static java.util.Date sqlDateToUtilDate (java.sql.Date date) {
        return new java.util.Date(date.getTime());
    }

}
