package com.wisecarCompany.wisecarapp.user.login;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.wisecarCompany.wisecarapp.R;
import com.wisecarCompany.wisecarapp.user.vehicle.VehicleActivity;
import com.wisecarCompany.wisecarapp.user.UserInfo;
import com.wisecarCompany.wisecarapp.user.create.CreateUserActivity;

import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "login";

    private EditText usernameEditText;
    private EditText passwordEditText;
    private ImageButton signInImageButton;
    private ImageButton createUserImageButton;
    String username;
    String password;

    private final String IP_HOST = "http://54.206.19.123:3000";
    private final String LOGIN = "/api/v1/users/login";

    public boolean dispatchTouchEvent(MotionEvent ev) {
        View v = getCurrentFocus();
        if (isShouldHideInput(v, ev)) {
            assert v != null;
            hideSoftInput(v.getWindowToken());
        }
        return super.dispatchTouchEvent(ev);
    }
    private boolean isShouldHideInput(View v, MotionEvent event) {
        if(v instanceof EditText) {
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
            assert manager != null;
            manager.hideSoftInputFromWindow(token, InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        usernameEditText = $(R.id.usernameEditText);
        passwordEditText = $(R.id.passwordEditText);
        signInImageButton = $(R.id.signInImageButton);
        createUserImageButton = $(R.id.createUserImageButton);

        signInImageButton.setOnClickListener(v -> {
            username = usernameEditText.getText().toString();
            password = passwordEditText.getText().toString();
            //isValidLogIn(username, password);
            if (!username.equals("") && !password.equals("")) {
                String URL = IP_HOST + LOGIN;
                final JSONObject jsonParam = new JSONObject();
                try {
                    jsonParam.put("user_name", username);
//                    jsonParam.put("password", password);
                    jsonParam.put("password", org.apache.commons.codec.digest.DigestUtils.sha256Hex(password));

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                JsonObjectRequest objectRequest = new JsonObjectRequest(Request.Method.POST, URL, jsonParam, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.e("Response", response.toString());
                        Toast.makeText(getApplicationContext(), response.optString("message"), Toast.LENGTH_LONG).show();
                        if (response.optString("message").equals("success")) {
                            Log.d(TAG, "onResponse: Success");
                            // Login successfully
                            UserInfo.clear();
                            UserInfo.setUsername(username);
                            UserInfo.setUserID(response.optString("user_id"));
                            startActivity(new Intent(LoginActivity.this, VehicleActivity.class));
                        }else{
                            Toast.makeText(getApplicationContext(), "Please check your username or password", Toast.LENGTH_LONG).show();
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
                            if (!message.equals("The password does not match")) {
                                usernameEditText.setText("");
                            }
                            passwordEditText.setText("");
                        }

                    }
                });

                Volley.newRequestQueue(LoginActivity.this).add(objectRequest);

            } else {
                Toast.makeText(getApplicationContext(), "Please enter your username and password", Toast.LENGTH_LONG).show();
            }
        });

        createUserImageButton.setOnClickListener((v) -> {
            startActivity(new Intent(LoginActivity.this, CreateUserActivity.class));
        });
    }

    private <T extends View> T $(int id){
        return (T) findViewById(id);
    }

    private void blockchainTest() {
        String URL = "http://13.236.209.122:3000/api/v1/servicerecords/blockchaininvoke";
        final JSONObject jsonParam = new JSONObject();
        try {
            jsonParam.put("identifier", "10210826n");
            jsonParam.put("record_type", "service_record");
            jsonParam.put("service_center", "BMW adelaide");
            jsonParam.put("service_date", "2020-07-06");
            jsonParam.put("vehicle_registration", "1212");
            jsonParam.put("ecrypt_hash", "test");
            jsonParam.put("service_file_location", "some path");
            jsonParam.put("service_options", "1234");

        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest objectRequest = new JsonObjectRequest(Request.Method.POST, URL, jsonParam, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.e("Response", response.toString());
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
        Volley.newRequestQueue(LoginActivity.this).add(objectRequest);
    }

}
