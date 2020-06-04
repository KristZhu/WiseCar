package com.example.wisecarapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.text.InputType;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

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
    private String refNo;
    private String notes;
    private java.util.Date nextDate;
    private String nextDistance;
    private boolean isOil;
    private boolean isBrake;
    private boolean isBattery;
    private boolean isCooling;
    private boolean isLights;

    private ImageButton saveImageButton;

    private final String IP_HOST = "http://54.206.19.123:3000";
    private final String ADD_SERVICE_RECORD = "/api/v1/servicerecords/";
    private final String GET_RECORD_IDENTIFIER = "/api/v1/servicerecords/identifier/";
    private String vehicle_id;
    private String servicesOptions = "";
    String currentDate = "";
    private String identifier;
    private String record_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_records);

        DateFormat format = new SimpleDateFormat("dd-MMM-yyyy");
        currentDate = format.format(Calendar.getInstance().getTime());



/*
        String vehicleID = (String) this.getIntent().getStringExtra("vehicleID");
        Log.d(TAG, "vehicleID: " + vehicleID);
        vehicle = UserInfo.getVehicles().get(vehicleID);
        Log.d(TAG, "vehicle: " + vehicle);
 */

        vehicle_id = (String) this.getIntent().getStringExtra("vehicle_id");
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

        dateEditText.setInputType(InputType.TYPE_NULL);
        dateEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dateEditText.setText("");
                date = null;
                Calendar c = Calendar.getInstance();
                new TimePickerDialog(ServiceRecordsActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hour, int minute) {
                        if (date == null)
                            ; //user should see DatePickerDialog first, and set date first. If user click back / cancel and skip date, time set should not be allowed.
                        else {
                            StringBuffer time = new StringBuffer();
                            time.append(", ");
                            time.append(hour >= 10 ? hour : "0" + hour);
                            time.append(":");
                            time.append(minute >= 10 ? minute : "0" + minute);
                            time.append("  ");
                            date = new java.util.Date(date.getTime() + (hour * 60 + minute) * 60 * 1000);
                            dateEditText.append(time);
                        }
                    }
                }, 0, 0, true).show();
                new DatePickerDialog(ServiceRecordsActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        date = intToDate(year, monthOfYear, dayOfMonth);
                        dateEditText.append(dateToStr(date));
                    }
                }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
        dateEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    dateEditText.setText("");
                    Calendar c = Calendar.getInstance();
                    new TimePickerDialog(ServiceRecordsActivity.this, new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePicker view, int hour, int minute) {
                            if (date == null)
                                return; //user should see DatePickerDialog first, and set date first. If user click back / cancel and skip date, time set should not be allowed.
                            else {
                                StringBuffer time = new StringBuffer();
                                time.append(hour >= 10 ? hour : "0" + hour);
                                time.append(":");
                                time.append(minute >= 10 ? minute : "0" + minute);
                                time.append("  ");
                                date = new Date(date.getTime() + (hour * 60 + minute) * 60 * 1000);
                                dateEditText.append(time);
                                Log.d(TAG, "date: " + date);
                            }
                        }
                    }, 0, 0, true).show();
                    new DatePickerDialog(ServiceRecordsActivity.this, new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                            date = intToDate(year, monthOfYear, dayOfMonth);
                            dateEditText.append(dateToStr(date) + ", ");
                        }
                    }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
                }
            }
        });

        nextDateEditText.setInputType(InputType.TYPE_NULL);
        nextDateEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar c = Calendar.getInstance();
                new DatePickerDialog(ServiceRecordsActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        nextDate = intToDate(year, monthOfYear, dayOfMonth);
                        nextDateEditText.setText(dateToStr(nextDate));
                    }
                }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
        nextDateEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    Calendar c = Calendar.getInstance();
                    new DatePickerDialog(ServiceRecordsActivity.this, new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                            nextDate = intToDate(year, monthOfYear, dayOfMonth);
                            nextDateEditText.setText(dateToStr(nextDate));
                        }
                    }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
                }
            }
        });


        saveImageButton = (ImageButton) findViewById(R.id.saveImageButton);
        saveImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Log.d(TAG, "userID" + UserInfo.getUserID());
                //Log.d(TAG, "vehicle" + vehicle);
                Log.d(TAG, "date: " + date);
                Log.d(TAG, "centre: " + centre);
                Log.d(TAG, "refNo: " + refNo);
                Log.d(TAG, "isOil: " + isOil);
                Log.d(TAG, "isBrake：" + isBrake);
                Log.d(TAG, "isBattery: " + isBattery);
                Log.d(TAG, "isCooling: " + isCooling);
                Log.d(TAG, "isLights:" + isLights);
                Log.d(TAG, "notes: " + notes);
                Log.d(TAG, "nextDate: " + nextDate);
                Log.d(TAG, "nextDistance: " + nextDistance);

                try {
                    if (Double.parseDouble(nextDistance) <= 0) throw new Exception();
                    if (nextDate.after(new java.util.Date())) {

                        // Write INSERTIONG here

                    } else {
                        Toast.makeText(getApplicationContext(), "Please enter correct next service date", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), "Please enter correct next service distance", Toast.LENGTH_SHORT).show();
                }

            }
        });

    }

    public boolean dispatchTouchEvent(MotionEvent ev) {
        View v = getCurrentFocus();
        if (isShouldHideInput(v, ev)) {
            hideSoftInput(v.getWindowToken());
            centre = centreEditText.getText().toString();
            refNo = refNoEditText.getText().toString();
            notes = notesEditText.getText().toString();
            nextDistance = nextDistanceEditText.getText().toString();
            if (date != null && centre != null && refNo != null && notes != null && nextDate != null && nextDistance != null
                    && centre.length() > 0 && refNo.length() > 0 && notes.length() > 0 && nextDistance.length() > 0
            ) {
                isOil = oilCheckBox.isChecked();
                isBrake = brakeCheckBox.isChecked();
                isBattery = batteryCheckBox.isChecked();
                isCooling = coolingCheckBox.isChecked();
                isLights = lightsCheckBox.isChecked();
                saveImageButton.setAlpha(1.0f);
                saveImageButton.setClickable(true);
            } else {
                saveImageButton.setAlpha(0.5f);
                saveImageButton.setClickable(false);
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    private boolean isShouldHideInput(View v, MotionEvent event) {
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

    private void uploadServiceRecord() {

        if (isOil) {
            servicesOptions += "1";
        }
        if (isBrake) {
            servicesOptions += "2";
        }
        if (isBattery) {
            servicesOptions += "3";
        }
        if (isCooling) {
            servicesOptions += "4";
        }
        if (isLights) {
            servicesOptions += "5";
        }

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                HttpClient httpClient = new DefaultHttpClient();
                HttpPost postRequest = new HttpPost(IP_HOST + ADD_SERVICE_RECORD);

                MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

                DateFormat format = new SimpleDateFormat("yyyy-MM-dd");

                try {
//                    reqEntity.addPart("record_id", new StringBody());
                    reqEntity.addPart("vehicle_id", new StringBody(vehicle_id));
                    reqEntity.addPart("service_date", new StringBody(format.format(date)));
                    reqEntity.addPart("service_center", new StringBody(centre));
                    reqEntity.addPart("service_ref", new StringBody(refNo));
                    reqEntity.addPart("service_option_ids", new StringBody(servicesOptions));
                    reqEntity.addPart("service_notes", new StringBody(notes));
                    reqEntity.addPart("next_service_date", new StringBody(format.format(nextDate)));
                    reqEntity.addPart("next_service_odometer", new StringBody(nextDistance));
//                    reqEntity.addPart("document", new StringBody());
//                    reqEntity.addPart("service_record_identifier", new StringBody());


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
                        Toast.makeText(ServiceRecordsActivity.this, "success", Toast.LENGTH_LONG).show();
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

    private void returnVehicles(String user_id, @Nullable final recordIdentifierCallback callbacks) {

        // "ISUZU-TFS195" is a dummy data and needs to be modified.
        String URL = IP_HOST + GET_RECORD_IDENTIFIER + "ISUZU-TFS195" + currentDate;

        JsonObjectRequest objectRequest = new JsonObjectRequest(Request.Method.GET, URL, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.e("Response: ", response.toString());
                JSONObject jsonObject = new JSONObject();

                identifier = jsonObject.optString("identifier");
                record_id = jsonObject.optString("record_id");

                if (callbacks != null)
                    callbacks.onSuccess(identifier, record_id);

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

//                Log.e("ERROR!!!", error.toString());
//                Log.e("ERROR!!!", String.valueOf(error.networkResponse));

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
                    Log.e("Error", message);
//                    if (callbacks != null)
//                        callbacks.onError(message);
                }

            }
        });

        Volley.newRequestQueue(ServiceRecordsActivity.this).add(objectRequest);
    }

    public interface recordIdentifierCallback {
        void onSuccess(@NonNull String identifier, String record_id);

//        void onError(@NonNull String errorMessage);
    }

}
