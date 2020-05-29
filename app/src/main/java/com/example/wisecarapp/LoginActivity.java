package com.example.wisecarapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
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

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "login";

    private EditText usernameEditText;
    private EditText passwordEditText;
    private ImageButton signInImageButton;
    private ImageButton createUserImageButton;
    String username;
    String password;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        usernameEditText = (EditText) findViewById(R.id.usernameEditText);
        passwordEditText = (EditText) findViewById(R.id.passwordEditText);
        signInImageButton = (ImageButton) findViewById(R.id.signInImageButton);
        createUserImageButton = (ImageButton) findViewById(R.id.createUserImageButton);

        signInImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                username = usernameEditText.getText().toString();
                password = passwordEditText.getText().toString();
                //isValidLogIn(username, password);
                if (!username.equals("") && !password.equals("")) {
                    String URL = "http://54.206.19.123:3000/api/v1/users/login";
                    final JSONObject jsonParam = new JSONObject();
                    try {
                        jsonParam.put("user_name", username);
                        jsonParam.put("password", password);
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
                                UserInfo.setUsername(username);
                                Intent intent = new Intent(LoginActivity.this, VehicleActivity.class);
                                intent.putExtra("user_id", response.optString("user_id"));
                                startActivity(intent);
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
            }
        });

        createUserImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, CreateUserActivity.class));
            }
        });
    }

}
