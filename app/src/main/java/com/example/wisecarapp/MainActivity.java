package com.example.wisecarapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.PersistableBundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private EditText usernameEditText;
    private EditText passwordEditText;
    private ImageButton signInImageButton;
    private ImageButton createUserImageButton;

    // CHANGE TO GLOBAL VARIABLE
    private String username;
    private String password;

    public boolean dispatchTouchEvent(MotionEvent ev) {
        View v = getCurrentFocus();
        if (isShouldHideInput(v, ev)) {
            hideSoftInput(v.getWindowToken());
            if (usernameEditText.getText().toString().equals("")) {
                usernameEditText.setText("USER NAME");
            }
            if (passwordEditText.getText().toString().equals("")) {
                passwordEditText.setInputType(97);  //plain
                passwordEditText.setText("PASSWORD");
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    public boolean isShouldHideInput(View v, MotionEvent event) {
        if (v != null && (v instanceof EditText)) {
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
            manager.hideSoftInputFromWindow(token, InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        usernameEditText = (EditText) findViewById(R.id.usernameEditText);
        passwordEditText = (EditText) findViewById(R.id.passwordEditText);
        signInImageButton = (ImageButton) findViewById(R.id.signInImageButton);
        createUserImageButton = (ImageButton) findViewById(R.id.createUserImageButton);

        usernameEditText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (usernameEditText.getText().toString().equals("USER NAME")) {
                    usernameEditText.setText("");
                }
                return false;
            }
        });
        usernameEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (usernameEditText.getText().toString().equals("USER NAME")) {
                    usernameEditText.setText("");
                }
            }
        });

        passwordEditText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (passwordEditText.getText().toString().equals("PASSWORD")) {
                    passwordEditText.setInputType(129); //cyber
                    passwordEditText.setText("");
                }
                return false;
            }
        });
        passwordEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (passwordEditText.getText().toString().equals("PASSWORD")) {
                    passwordEditText.setInputType(129); //cyber
                    passwordEditText.setText("");
                }
            }
        });

        signInImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                username = usernameEditText.getText().toString();
                password = passwordEditText.getText().toString();

                // THIS LINE BELOW TO TEST THE THREAD METHOD. IT DID NOT WORK CORRECTLY.
//                sendPost();

                String URL = "http://54.206.19.123:3000/api/v1/users/login";
                JSONObject jsonParam = new JSONObject();
                try {
                    jsonParam.put("user_name", username);
                    jsonParam.put("password", password);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                JsonObjectRequest objectRequest = new JsonObjectRequest(Request.Method.POST, URL, jsonParam, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.e("Rest Response", response.toString());
                        Toast.makeText(getApplicationContext(), response.optString("message"), Toast.LENGTH_LONG).show();
                        if (!response.optString("status").equals("201")) {
                            if (!response.optString("message").equals("The password does not match")) {
                                usernameEditText.setText("");
                            }
                            passwordEditText.setText("");
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("Rest Response", error.toString());
                    }
                });
                Volley.newRequestQueue(MainActivity.this).add(objectRequest);

            }
        });

        createUserImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }


    // METHOD USING THREAD. (SOMETHING IS WRONG. CANNOT GET CORRECT RESULT.
//    private void sendPost() {
//        Thread thread = new Thread(new Runnable() {
//            @Override
//            public void run() {
//                JSONObject jsonParam = new JSONObject();
//                try {
//                    URL url = new URL("http://54.206.19.123:3000/api/v1/users/login");
//                    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
//                    httpURLConnection.setRequestMethod("POST");
//                    // NOT SURE HOW THIS BELOW PART WORKS
//                    httpURLConnection.setRequestProperty("Content-Type", "application/json");
//                    httpURLConnection.setRequestProperty("Accept", "application/json");
//                    httpURLConnection.setDoOutput(true);
//                    httpURLConnection.setDoInput(true);
//                    httpURLConnection.connect();
//
//                    jsonParam.put("user_name", username);
//                    jsonParam.put("password", password);
//
//                    Log.e("JSONParam", jsonParam.toString());
//
//                    DataOutputStream dataOutputStream = new DataOutputStream(httpURLConnection.getOutputStream());
//                    dataOutputStream.writeBytes(jsonParam.toString());
//
//                    dataOutputStream.flush();
//                    dataOutputStream.close();
//
//                    Log.e("stats", String.valueOf(httpURLConnection.getResponseCode()));
//                    Log.e("message", httpURLConnection.getResponseMessage());
//
//                    httpURLConnection.disconnect();
//
//                } catch (IOException e) {
//                    e.printStackTrace();
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//            }
//
//        });
//        thread.start();
//    }
}
