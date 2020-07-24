package com.wisecarCompany.wisecarapp.user.login;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
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

import java.util.List;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "login";

    private final String IP_HOST = "http://54.206.19.123:3000";
    private final String LOGIN = "/api/v1/users/login";

    private SharedPreferences sp;

    private EditText usernameEditText;
    private EditText passwordEditText;
    private CheckBox rememberCheckBox;
    private CheckBox autoLoginCheckBox;

    private ImageButton signInImageButton;
    private ImageButton createUserImageButton;

    private String username;
    private String password;
    private boolean remember;
    private boolean autoLogin;
    private boolean passwordChanged;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        sp = this.getSharedPreferences("userInfo", MODE_PRIVATE);

        usernameEditText = $(R.id.usernameEditText);
        passwordEditText = $(R.id.passwordEditText);
        rememberCheckBox = $(R.id.rememberCheckBox);
        autoLoginCheckBox = $(R.id.autoLoginCheckBox);

        //both are always true for now
        remember = sp.getBoolean("REMEMBER_PASSWORD", true);
        autoLogin = sp.getBoolean("AUTO_LOGIN", true);
        rememberCheckBox.setChecked(remember);
        autoLoginCheckBox.setChecked(autoLogin);

        if(autoLogin) {
            login(sp.getString("USERNAME", ""), sp.getString("PASSWORD", ""));
        } else if(remember) {
            usernameEditText.setText(sp.getString("USERNAME", ""));
            passwordEditText.setText(sp.getString("PASSWORD", ""));
            autoLoginCheckBox.setClickable(true);
            autoLoginCheckBox.setAlpha(1.0f);
        }

        passwordChanged = false;    //only when password is remembered and not changed will it remain false
        passwordEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if(!passwordChanged) {
                passwordChanged = true;
                passwordEditText.setText("");
            }
        });
        passwordEditText.setOnClickListener(v -> {
            if(!passwordChanged) {
                passwordChanged = true;
                passwordEditText.setText("");
            }
        });

        rememberCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(isChecked) {
                autoLoginCheckBox.setClickable(true);
                autoLoginCheckBox.setAlpha(1.0f);
            } else {
                autoLoginCheckBox.setChecked(false);
                autoLoginCheckBox.setClickable(false);
                autoLoginCheckBox.setAlpha(0.5f);
            }
        });

        signInImageButton = $(R.id.signInImageButton);
        createUserImageButton = $(R.id.createUserImageButton);

        signInImageButton.setOnClickListener(v -> {
            if (usernameEditText.getText().toString().length()>0 && passwordEditText.getText().toString().length()>0) {
                username = usernameEditText.getText().toString();
                if(passwordChanged) password = org.apache.commons.codec.digest.DigestUtils.sha256Hex(passwordEditText.getText().toString());
                else password = passwordEditText.getText().toString();
                remember = rememberCheckBox.isChecked();
                autoLogin = autoLoginCheckBox.isChecked();
                Log.d(TAG, "username: " + username);
                Log.d(TAG, "password: " + password);
                Log.d(TAG, "password changed: " + passwordChanged);
                login(username, password);
            } else {
                Toast.makeText(getApplicationContext(), "Please enter username and password", Toast.LENGTH_SHORT).show();
            }
        });

        createUserImageButton.setOnClickListener((v) -> startActivity(new Intent(LoginActivity.this, CreateUserActivity.class)));

    }

    private void login(String username, String password) {
        String URL = IP_HOST + LOGIN;
        final JSONObject jsonParam = new JSONObject();
        try {
            jsonParam.put("user_name", username);
            jsonParam.put("password", password);    //already hashed
//                    jsonParam.put("password", org.apache.commons.codec.digest.DigestUtils.sha256Hex(password));

        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest objectRequest = new JsonObjectRequest(Request.Method.POST, URL, jsonParam, response -> {
            Log.e("Response", response.toString());
            if (response.optString("message").equals("success")) {
                Toast.makeText(getApplicationContext(), "Login successfully", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "onResponse: Success");
                // Login successfully
                SharedPreferences.Editor editor = sp.edit()
                        .putString("USERNAME", username)
                        .putString("PASSWORD", password)
                        //.putBoolean("REMEMBER_PASSWORD", remember)
                        //.putBoolean("AUTO_LOGIN", autoLogin);
                        .putBoolean("REMEMBER_PASSWORD", true)
                        .putBoolean("AUTO_LOGIN", true);
                editor.commit();

                UserInfo.clear();
                UserInfo.setUsername(username);
                UserInfo.setUserID(response.optString("user_id"));
                startActivity(new Intent(LoginActivity.this, VehicleActivity.class));
            } else {
                Toast.makeText(this, "Please check your username or password", Toast.LENGTH_SHORT).show();
            }
        }, error -> {
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
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                if (!message.equals("The password does not match")) {
                    usernameEditText.setText("");
                }
                passwordEditText.setText("");
            }

        });

        Volley.newRequestQueue(LoginActivity.this).add(objectRequest);
    }

    private <T extends View> T $(int id) {
        return (T) findViewById(id);
    }

    public boolean dispatchTouchEvent(MotionEvent ev) {
        View v = getCurrentFocus();
        if (isShouldHideInput(v, ev)) {
            assert v != null;
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
                    Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                }

            }
        });
        Volley.newRequestQueue(LoginActivity.this).add(objectRequest);
    }

    @Override
    public void onBackPressed() {
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle("Are you sure you want to exit? ")
                .setPositiveButton("OK", (dialog, which) -> {
                    exitAPP();
                }).setNegativeButton("Cancel", (dialog, which) -> {

                }).create();
        alertDialog.show();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            AlertDialog alertDialog = new AlertDialog.Builder(this)
                    .setTitle("Are you sure you want to exit? ")
                    .setPositiveButton("OK", (dialog, which) -> {
                        exitAPP();
                    }).setNegativeButton("Cancel", (dialog, which) -> {

                    }).create();
            alertDialog.show();
            return true;    //stop calling super method
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void exitAPP() {
        ActivityManager activityManager = (ActivityManager) getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.AppTask> appTaskList = activityManager.getAppTasks();
        for (ActivityManager.AppTask appTask : appTaskList) {
            appTask.finishAndRemoveTask();
        }
//        appTaskList.get(0).finishAndRemoveTask();
        System.exit(0);
    }

}
