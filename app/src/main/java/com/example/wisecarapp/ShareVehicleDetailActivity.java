package com.example.wisecarapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

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
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class ShareVehicleDetailActivity extends AppCompatActivity {

    private static final String TAG = "ShareVehicleDetail";

    private final String IP_HOST = "http://54.206.19.123:3000";
    private final String GET_COMPANY_LIST = "/api/v1/customers/customer/list";
    private final String SUBMIT_SHARE_VEHICLE = "/api/v1/sharevehicle/submit";
    private final String SHARE_CHECK = "/api/v1/sharevehicle/check";
    private final String EDIT_SHARE = "/api/v1/sharevehicle/update";
    private final String GET_HISTORY = "/api/v1/sharevehicle/sharedetailapp/";

    private String vehicleID;
    private Vehicle vehicle;

    private boolean NEW;  //new add or edit

    private String shareID;

    private ImageButton backImageButton;
    private ImageButton saveImageButton;

    private AutoCompleteTextView searchEditText;
    private ImageButton addImageButton;
    private TextView companyNameTextView;
    private String companyName;
    private TextView companyIDTextView;
    private String custID;
    private ImageButton cancelImageButton;
    private static Map<String, String> companies = new HashMap<>(); //<ID, Name>

    private SwitchButton shareSwitchButton;
    private boolean isShare;
    private ConstraintLayout shareDiv;
    private EditText dateEditText;
    private java.util.Date date;
    private EditText startEditText;
    private java.util.Date start;
    private EditText endEditText;
    private java.util.Date end;
    private SwitchButton recurringSwitchButton;
    private boolean isRecurring = false;

    private ConstraintLayout recurringDiv;
    private EditText endDateEditText;
    private java.util.Date endDate;
    private CheckBox[] weekdayCheckBox;
    private boolean[] isWeekday;    //0: Sun, 6: Sat

    private SwitchButton visibilitySwitchButton;
    private boolean isVisibility = false;
    private LinearLayout visibilityDiv;
    private Map<Integer, Boolean> servicesVisibility = new TreeMap<>();   //value: visibility

    private String recurringDays = "";
    private String shareChecked = "";
    private String recurringChecked = "";
    private String visibilityChecked = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_vehicle_detail);

        //vehicleID = (String) this.getIntent().getStringExtra("vehicleID");
        vehicleID = "303";
        Log.d(TAG, "vehicleID: " + vehicleID);
        vehicle = UserInfo.getVehicles().get(vehicleID);
        Log.d(TAG, "vehicle: " + vehicle);
        //NEW = (boolean) this.getIntent().getSerializableExtra("NEW");
        NEW = true;
        Log.d(TAG, "NEW: " + NEW);


        searchEditText = $(R.id.searchEditText);
        addImageButton = $(R.id.addImageButton);
        companyIDTextView = $(R.id.companyIDTextView);
        companyNameTextView = $(R.id.companyNameTextView);
        cancelImageButton = $(R.id.cancelImageButton);

        shareDiv = $(R.id.shareDiv);
        shareSwitchButton = $(R.id.shareSwitchButton);

        dateEditText = $(R.id.dateEditText);
        dateEditText.setInputType(InputType.TYPE_NULL);
        startEditText = $(R.id.startEditText);
        startEditText.setInputType(InputType.TYPE_NULL);
        endEditText = $(R.id.endEditText);
        endEditText.setInputType(InputType.TYPE_NULL);

        recurringSwitchButton = $(R.id.recurringSwitchButton);
        recurringDiv = $(R.id.recurringDiv);

        endDateEditText = $(R.id.endDateEditText);
        endDateEditText.setInputType(InputType.TYPE_NULL);
        weekdayCheckBox = new CheckBox[]{
                $(R.id.sunCheckBox),
                $(R.id.monCheckBox),
                $(R.id.tueCheckBox),
                $(R.id.wedCheckBox),
                $(R.id.thuCheckBox),
                $(R.id.friCheckBox),
                $(R.id.satCheckBox)
        };
        isWeekday = new boolean[7];

        visibilitySwitchButton = $(R.id.visibilitySwitchButton);
        visibilityDiv = $(R.id.visibilityDiv);

        saveImageButton = $(R.id.saveImageButton);


        if(NEW) {

            final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, new ArrayList<String>());
            returnCompanies(new companiesCallbacks() {
                @Override
                public void onSuccess(@NonNull Map<String, String> value) {
                    Log.d(TAG, "companies: " + companies);
                    for (String id : companies.keySet()) {
                        adapter.add(id + ": " + companies.get(id));
                    }
                    searchEditText.setAdapter(adapter);
                    searchEditText.setOnItemClickListener((parent, view, position, id) -> {
                        String temp = searchEditText.getText().toString();
                        custID = temp.substring(0, temp.indexOf(":"));
                        companyIDTextView.setText("Company ID: " + custID);
                        companyName = companies.get(custID);
                        companyNameTextView.setText(companyName);
                        cancelImageButton.setVisibility(View.VISIBLE);
                    });
                    /*
                    addImageButton.setOnClickListener(v -> {
                        String temp = searchEditText.getText().toString();
                        try {
                            if(companies.containsKey(companyID)) {
                                companyIDTextView.setText("Company ID: " + companyID);
                                companyName = companies.get(companyID);
                                companyNameTextView.setText(companyName);
                                cancelImageButton.setVisibility(View.VISIBLE);
                            } else {
                                Toast.makeText(getApplicationContext(), "Please enter correct company id or name", Toast.LENGTH_LONG).show();
                            }
                        } catch (Exception e) {
                            Toast.makeText(getApplicationContext(), "Please enter correct company id or name", Toast.LENGTH_LONG).show();
                        }
                    });
                    */
                }

                @Override
                public void onError(@NonNull String errorMessage) {

                }
            });

            cancelImageButton.setOnClickListener(v -> {
                companyName = "";
                custID = "";
                companyNameTextView.setText("");
                companyIDTextView.setText("");
                searchEditText.setText("");
                cancelImageButton.setVisibility(View.INVISIBLE);
            });

            isShare = false;
            isRecurring = false;
            isVisibility = false;

//            for(int i: vehicle.getServices()) servicesVisibility.put(i, true);

            servicesVisibility.put(1, true);
            servicesVisibility.put(2, false);

        } else {    //!NEW
            shareID = (String) this.getIntent().getStringExtra("shareID");
            returnFormerSharingDetails(shareID, new formerSharingCallbacks() {
                @Override
                public void onSuccess(Share share) {

                    companyNameTextView.setText(companyName);

                    companyIDTextView.setText(custID);
                    searchEditText.setInputType(InputType.TYPE_NULL);

                    isShare = share.isShare();
                    if(isShare) {
                        shareSwitchButton.setToggleOn(true);
                        shareDiv.setVisibility(View.VISIBLE);
                    }

                    date = share.getDate();
                    start = share.getStart_time();
                    end = share.getEnd_time();
                    SimpleDateFormat formatDate = new SimpleDateFormat("ddMMM yyyy");
                    SimpleDateFormat formatTime = new SimpleDateFormat("HH:mm");
                    dateEditText.setText(formatDate.format(date));
                    startEditText.setText(formatTime.format(start));
                    endEditText.setText(formatTime.format(end));

                    isRecurring = share.isRecurring();
                    if (isRecurring) {
                        recurringSwitchButton.setToggleOn(true);
                        recurringDiv.setVisibility(View.VISIBLE);
                    }

                    endDate = share.getRecurring_end_date();
                    SimpleDateFormat format = new SimpleDateFormat("ddMMM yyyy");
                    endDateEditText.setText(format.format(endDate));
                    isWeekday = share.getRecurring_days();
                    for (int i = 0; i < 7; i++) {
                        if (isWeekday[i]) weekdayCheckBox[i].setChecked(true);
                        else weekdayCheckBox[i].setChecked(false);
                    }

                    isVisibility = true;
                    if (isVisibility) {
                        visibilitySwitchButton.setToggleOn(true);
                        visibilityDiv.setVisibility(View.VISIBLE);
                    }

                    servicesVisibility = share.getServicesVisibility();

                    saveImageButton.setAlpha(1.0f);
                    saveImageButton.setClickable(true);
                    
                }

                @Override
                public void onError(@NonNull String errorMessage) {

                }
            });
        }


        backImageButton = $(R.id.backImageButton);
        backImageButton.setOnClickListener(v -> {
            Intent intent = new Intent(ShareVehicleDetailActivity.this, ShareVehicleListActivity.class);
            intent.putExtra("vehicleID", vehicleID);
            startActivity(intent);
        });


        shareSwitchButton.setOnToggleChanged(isOn -> {
            Log.d(TAG, "share: " + isOn);
            isShare = isOn;
            if (isOn) shareDiv.setVisibility(View.VISIBLE);
            else shareDiv.setVisibility(View.GONE);
        });


        dateEditText.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new DatePickerDialog(ShareVehicleDetailActivity.this, (view, year, monthOfYear, dayOfMonth) -> {
                date = intToDate(year, monthOfYear, dayOfMonth);
                SimpleDateFormat format = new SimpleDateFormat("ddMMM yyyy");
                String str = format.format(date);
                dateEditText.setText(str);
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
        });
        dateEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                Calendar c = Calendar.getInstance();
                new DatePickerDialog(ShareVehicleDetailActivity.this, (view, year, monthOfYear, dayOfMonth) -> {
                    date = intToDate(year, monthOfYear, dayOfMonth);
                    SimpleDateFormat format = new SimpleDateFormat("ddMMM yyyy");
                    String str = format.format(date);
                    dateEditText.setText(str);
                }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();

            }
        });

        startEditText.setOnClickListener((v) -> {
            new TimePickerDialog(ShareVehicleDetailActivity.this, (view, hour, minute) -> {
                StringBuffer time = new StringBuffer();
                time.append(hour >= 10 ? hour : "0" + hour);
                time.append(":");
                time.append(minute >= 10 ? minute : "0" + minute);
                start = new Date(intToDate(1970, 1, 1).getTime() + (hour * 60 + minute) * 60 * 1000); //otherwise there is timezone problem
                startEditText.setText(time);
            }, 0, 0, true).show();
        });
        startEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                new TimePickerDialog(ShareVehicleDetailActivity.this, (view, hour, minute) -> {
                    StringBuffer time = new StringBuffer();
                    time.append(hour >= 10 ? hour : "0" + hour);
                    time.append(":");
                    time.append(minute >= 10 ? minute : "0" + minute);
                    start = new Date(intToDate(1970, 1, 1).getTime() + (hour * 60 + minute) * 60 * 1000);
                    startEditText.setText(time);
                }, 0, 0, true).show();
            }
        });

        endEditText.setOnClickListener((v) -> {
            new TimePickerDialog(ShareVehicleDetailActivity.this, (view, hour, minute) -> {
                StringBuffer time = new StringBuffer();
                time.append(hour >= 10 ? hour : "0" + hour);
                time.append(":");
                time.append(minute >= 10 ? minute : "0" + minute);
                end = new Date(intToDate(1970, 1, 1).getTime() + (hour * 60 + minute) * 60 * 1000);
                endEditText.setText(time);
            }, 0, 0, true).show();
        });
        endEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                new TimePickerDialog(ShareVehicleDetailActivity.this, (view, hour, minute) -> {
                    StringBuffer time = new StringBuffer();
                    time.append(hour >= 10 ? hour : "0" + hour);
                    time.append(":");
                    time.append(minute >= 10 ? minute : "0" + minute);
                    end = new Date(intToDate(1970, 1, 1).getTime() + (hour * 60 + minute) * 60 * 1000);
                    endEditText.setText(time);
                }, 0, 0, true).show();
            }
        });


        recurringSwitchButton.setOnToggleChanged(isOn -> {
            Log.d(TAG, "recurring: " + isOn);
            isRecurring = isOn;
            if (isOn) recurringDiv.setVisibility(View.VISIBLE);
            else recurringDiv.setVisibility(View.GONE);
        });


        endDateEditText.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new DatePickerDialog(ShareVehicleDetailActivity.this, (view, year, monthOfYear, dayOfMonth) -> {
                endDate = intToDate(year, monthOfYear, dayOfMonth);
                SimpleDateFormat format = new SimpleDateFormat("ddMMM yyyy");
                String str = format.format(endDate);
                endDateEditText.setText(str);
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
        });
        endDateEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                Calendar c = Calendar.getInstance();
                new DatePickerDialog(ShareVehicleDetailActivity.this, (view, year, monthOfYear, dayOfMonth) -> {
                    endDate = intToDate(year, monthOfYear, dayOfMonth);
                    SimpleDateFormat format = new SimpleDateFormat("ddMMM yyyy");
                    String str = format.format(endDate);
                    endDateEditText.setText(str);
                }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();

            }
        });


        List<Map.Entry<Integer, Boolean>> servicesList = new ArrayList<>(servicesVisibility.entrySet());
        Log.d(TAG, "servicesVisibility: " + servicesVisibility);
        for(int i = 0; i < servicesList.size(); i += 2) {
            ConstraintLayout servicesLineLayout = new ConstraintLayout(ShareVehicleDetailActivity.this);
            ConstraintSet set = new ConstraintSet();
            CheckBox[] checkBoxes = new CheckBox[Math.min(servicesVisibility.size() - i, 2)];
            for(int j = 0; j < checkBoxes.length; j++) {
                checkBoxes[j] = new CheckBox(getApplicationContext());
                checkBoxes[j].setId(j);
                checkBoxes[j].setButtonDrawable(getResources().getDrawable(R.drawable.vehicle0checkbox_style_2));
                checkBoxes[j].setTextColor(0xffffffff);
                switch (servicesList.get(i + j).getKey()) {
                    case 1:
                        checkBoxes[j].setText("Service Records");
                        checkBoxes[j].setChecked(servicesVisibility.get(1));
                        checkBoxes[j].setOnCheckedChangeListener((buttonView, isChecked) -> servicesVisibility.put(1, isChecked));
                        break;
                    case 2:
                        checkBoxes[j].setText("Driver Log");
                        checkBoxes[j].setChecked(servicesVisibility.get(2));
                        checkBoxes[j].setOnCheckedChangeListener((buttonView, isChecked) -> servicesVisibility.put(2, isChecked));
                        break;
                    case 3:
                        checkBoxes[j].setText("Registration Reminder");
                        checkBoxes[j].setChecked(servicesVisibility.get(3));
                        checkBoxes[j].setOnCheckedChangeListener((buttonView, isChecked) -> servicesVisibility.put(3, isChecked));
                        break;
                    case 4:
                        checkBoxes[j].setText("Parking Receipts");
                        checkBoxes[j].setChecked(servicesVisibility.get(4));
                        checkBoxes[j].setOnCheckedChangeListener((buttonView, isChecked) -> servicesVisibility.put(4, isChecked));
                        break;
                    case 5:
                        checkBoxes[j].setText("Insurance");
                        checkBoxes[j].setChecked(servicesVisibility.get(5));
                        checkBoxes[j].setOnCheckedChangeListener((buttonView, isChecked) -> servicesVisibility.put(5, isChecked));
                        break;
                    case 6:
                        checkBoxes[j].setText("Toll");
                        checkBoxes[j].setChecked(servicesVisibility.get(6));
                        checkBoxes[j].setOnCheckedChangeListener((buttonView, isChecked) -> servicesVisibility.put(6, isChecked));
                        break;
                    case 7:
                        checkBoxes[j].setText("Fuel");
                        checkBoxes[j].setChecked(servicesVisibility.get(7));
                        checkBoxes[j].setOnCheckedChangeListener((buttonView, isChecked) -> servicesVisibility.put(7, isChecked));
                        break;
                }
                set.connect(checkBoxes[j].getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, 16);
                set.connect(checkBoxes[j].getId(), ConstraintSet.LEFT, ConstraintSet.PARENT_ID, ConstraintSet.LEFT, 16);
                set.connect(checkBoxes[j].getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM);
                set.connect(checkBoxes[j].getId(), ConstraintSet.RIGHT, ConstraintSet.PARENT_ID, ConstraintSet.RIGHT);
                set.constrainPercentWidth(checkBoxes[j].getId(), 0.5f);
                set.constrainHeight(checkBoxes[j].getId(), ConstraintSet.WRAP_CONTENT);
                set.setHorizontalBias(checkBoxes[j].getId(), (float) (1.0 * j));
                servicesLineLayout.addView(checkBoxes[j]);
            }
            set.applyTo(servicesLineLayout);
            visibilityDiv.addView(servicesLineLayout);
        }

        visibilitySwitchButton.setOnToggleChanged(isOn -> {
            Log.d(TAG, "visibility: " + isOn);
            isVisibility = isOn;
            if (isOn) visibilityDiv.setVisibility(View.VISIBLE);
            else visibilityDiv.setVisibility(View.GONE);
        });


        saveImageButton.setOnClickListener(v -> {
            SimpleDateFormat format = new SimpleDateFormat("HH:mm");

            Log.d(TAG, "companyName: " + companyName);
            Log.d(TAG, "custID: " + custID);
            Log.d(TAG, "isShare: " + isShare);

            if (isShare) {
                Log.d(TAG, "date: " + date);
                Log.d(TAG, "start: " + format.format(start));
                Log.d(TAG, "end: " + format.format(end));
                Log.d(TAG, "isRecurring: " + isRecurring);

                if (date.before(new Date())) {
                    Toast.makeText(getApplicationContext(), "Please enter correct date", Toast.LENGTH_LONG).show();
                    return;
                }
                if (start.after(end)) {
                    Toast.makeText(getApplicationContext(), "Please enter correct time", Toast.LENGTH_LONG).show();
                    return;
                }

                if (isRecurring) {
                    Log.d(TAG, "endDate: " + endDate);
                    for (int i = 0; i < 7; i++) {
                        isWeekday[i] = weekdayCheckBox[i].isChecked();
                        Log.d(TAG, "isWeekday" + i + ": " + isWeekday[i]);
                    }
                    if (endDate == null || endDate.before(date)) {
                        Toast.makeText(getApplicationContext(), "Please enter correct end date", Toast.LENGTH_LONG).show();
                        return;
                    }

                /*
                List<Date> allDays = new LinkedList<>();
                Date d = new Date(date.getTime());
                while (d.before(new Date(endDate.getTime() + 24 * 60 * 60 * 1000))) {
                    Calendar calendar = new GregorianCalendar();
                    calendar.setTime(d);
                    int weekday = calendar.get(Calendar.DAY_OF_WEEK);   //1:Sun, 7:Sat
                    switch (weekday) {
                        case 1:
                            if (isSun) allDays.add(d);
                            break;
                        case 2:
                            if (isMon) allDays.add(d);
                            break;
                        case 3:
                            if (isTue) allDays.add(d);
                            break;
                        case 4:
                            if (isWed) allDays.add(d);
                            break;
                        case 5:
                            if (isThu) allDays.add(d);
                            break;
                        case 6:
                            if (isFri) allDays.add(d);
                            break;
                        case 7:
                            if (isSat) allDays.add(d);
                            break;
                    }
                    calendar.add(calendar.DATE, 1);
                    d = calendar.getTime();
                }

                Log.d(TAG, "all shared days: " + allDays);

                 */

                }

                Log.d(TAG, "isVisibility: " + isVisibility);
                if (isVisibility) {
                    Log.d(TAG, "visibility: " + servicesVisibility);
                } else {

                }

            }

            shareVehicleCheck();

        });

    }

    private void returnCompanies(@Nullable final companiesCallbacks callbacks) {

        String URL = IP_HOST + GET_COMPANY_LIST;

        JsonObjectRequest objectRequest = new JsonObjectRequest(Request.Method.GET, URL, null, response -> {
            Log.e("Response: ", response.toString());
            JSONArray jsonArray;
            JSONObject jsonObject;
            try {
                jsonArray = response.getJSONArray("company_list");
                for (int i = 0; i < jsonArray.length(); i++) {
                    jsonObject = jsonArray.getJSONObject(i);
                    companies.put(jsonObject.optString("cust_id"), jsonObject.optString("company_name"));
                }
                if (callbacks != null)
                    callbacks.onSuccess(companies);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, error -> {

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
                Log.e("No vehicle: ", message);
                if (callbacks != null)
                    callbacks.onError(message);
            }

        });

        Volley.newRequestQueue(ShareVehicleDetailActivity.this).add(objectRequest);
    }

    public interface companiesCallbacks {
        void onSuccess(@NonNull Map<String, String> value);

        void onError(@NonNull String errorMessage);
    }

    private void shareVehicleCheck() {

        for (int i = 0; i < 7; i++) {
            if (isWeekday[i]) recurringDays += i;
        }

        if (isShare) {
            shareChecked += "1";
        } else {
            shareChecked += "0";
        }
        if (isRecurring) {
            recurringChecked += "1";
        } else {
            recurringChecked += "0";
        }
        if (isVisibility) {
            visibilityChecked += "1";
        } else {
            visibilityChecked += "0";
        }

        MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        DateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");

        try {
            reqEntity.addPart("cust_id", new StringBody(custID));
            reqEntity.addPart("vehicle_id", new StringBody(vehicleID));
            reqEntity.addPart("share", new StringBody(shareChecked));
            reqEntity.addPart("date", new StringBody(dateFormat.format(date)));
            reqEntity.addPart("recurring", new StringBody(recurringChecked));
            if (recurringChecked.equals("1")) {
                reqEntity.addPart("recurring_end_date", new StringBody(dateFormat.format(endDate)));
                reqEntity.addPart("recurring_days", new StringBody(recurringDays));
            }
            reqEntity.addPart("service_visibility", new StringBody(visibilityChecked));
            if (visibilityChecked.equals("1")) {
//                StringBuilder servicesSB = new StringBuilder();
//                for (int i : vehicle.getServices()) servicesSB.append(i);
//                reqEntity.addPart("visible_service_ids", new StringBody(servicesSB.toString()));
                reqEntity.addPart("visible_service_ids", new StringBody("12"));
            }
            reqEntity.addPart("start_time", new StringBody(timeFormat.format(start)));
            reqEntity.addPart("end_time", new StringBody(timeFormat.format(end)));

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Thread submitingThread = new Thread(() -> {
            HttpClient httpClient = new DefaultHttpClient();
            HttpPost postRequest = new HttpPost(IP_HOST + SUBMIT_SHARE_VEHICLE);
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
                    Intent intent = new Intent(ShareVehicleDetailActivity.this, ShareVehicleListActivity.class);
                    startActivity(intent);
                }
                Log.e("response", s.toString());
                Log.e("share_id", s.toString().substring(s.indexOf("id") + 3));
            } catch (IOException e) {
                e.printStackTrace();
            }

            postRequest.abort();
            httpClient.getConnectionManager().shutdown();

        });

        Thread checkingThread = new Thread(() -> {
            HttpClient httpClient = new DefaultHttpClient();
            HttpPost postRequest = new HttpPost(IP_HOST + SHARE_CHECK);
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
                if (s.toString().contains("validated")) {
                    submitingThread.start();
                }
                Log.e("response", s.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }

            postRequest.abort();
            httpClient.getConnectionManager().shutdown();

        });
        checkingThread.start();
    }


    private void editShare() {

        MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        DateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");

        try {
            reqEntity.addPart("cust_id", new StringBody(custID));
            reqEntity.addPart("vehicle_id", new StringBody(vehicleID));
            reqEntity.addPart("share", new StringBody(shareChecked));
            reqEntity.addPart("date", new StringBody(dateFormat.format(date)));
            reqEntity.addPart("recurring", new StringBody(recurringChecked));
            if (recurringChecked.equals("1")) {
                reqEntity.addPart("recurring_end_date", new StringBody(dateFormat.format(endDate)));
                reqEntity.addPart("recurring_days", new StringBody(recurringDays));
            }
            reqEntity.addPart("service_visibility", new StringBody(visibilityChecked));
            if (visibilityChecked.equals("1")) {
//                StringBuilder servicesSB = new StringBuilder();
//                for (int i : vehicle.getServices()) servicesSB.append(i);
//                reqEntity.addPart("visible_service_ids", new StringBody(servicesSB.toString()));
                reqEntity.addPart("visible_service_ids", new StringBody("12"));
            }
            reqEntity.addPart("start_time", new StringBody(timeFormat.format(start)));
            reqEntity.addPart("end_time", new StringBody(timeFormat.format(end)));
            if (!isShare) {
                reqEntity.addPart("mode", new StringBody("0"));
            } else {
                reqEntity.addPart("mode", new StringBody("1"));
            }
            reqEntity.addPart("share_id", new StringBody(shareID));

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Thread Thread = new Thread(() -> {
            HttpClient httpClient = new DefaultHttpClient();
            HttpPost postRequest = new HttpPost(IP_HOST + EDIT_SHARE);
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
//                    Intent intent = new Intent(ShareVehicleDetailActivity.this, ShareVehicleListActivity.class);
//                    startActivity(intent);
                }
                Log.e("response", s.toString());
                Log.e("new_share_id", s.toString().substring(s.indexOf("id") + 3));
            } catch (IOException e) {
                e.printStackTrace();
            }

            postRequest.abort();
            httpClient.getConnectionManager().shutdown();

        });

        Thread.start();
    }

    private void returnFormerSharingDetails(String share_id, @Nullable final formerSharingCallbacks callbacks) {

        String URL = IP_HOST + GET_HISTORY + share_id;

        JsonObjectRequest objectRequest = new JsonObjectRequest(Request.Method.GET, URL, null, response -> {
            Log.e("Response", response.toString());
            JSONObject jsonObject = response;
            JSONArray jsonArray;
            try {

                Share share = new Share();
                share.setCust_id(jsonObject.optString("cust_id"));
                share.setCompany_name(jsonObject.optString("company_name"));
                share.setShare(jsonObject.optString("share_active") == "1" ? true : false);
                share.setRecurring(jsonObject.optString("recurring_flag") == "1" ? true : false);
                share.setRecurring_end_date(new SimpleDateFormat("yyyy-MM-dd").parse(jsonObject.optString("recurring_end_date")));
                share.setDate(new SimpleDateFormat("dd/MM/yyyy").parse(jsonObject.optString("date")));
                share.setStart_time(new SimpleDateFormat("HH:mm:ss").parse(jsonObject.optString("start_time")));
                share.setEnd_time(new SimpleDateFormat("HH:mm:ss").parse(jsonObject.optString("end_time")));

                String recurringDaysStr = jsonObject.optString("recurring_day");
                boolean[] recurringDays = new boolean[] {false, false, false, false, false, false, false};
                for (char c : recurringDaysStr.toCharArray()) recurringDays[c-'0'] = true;
                share.setRecurring_days(recurringDays);

                jsonArray = response.getJSONArray("service_list");
                for (int i = 0; i < jsonArray.length(); i++) {
                    jsonObject = jsonArray.getJSONObject(i);
                    share.getServicesVisibility().put(jsonObject.optInt("service_id"), jsonObject.optInt("is_visible") == 1 ? true : false);
                }
                if (callbacks != null)
                    callbacks.onSuccess(share);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, error -> {

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
                if (callbacks != null)
                    callbacks.onError(message);
            }

        });

        Volley.newRequestQueue(ShareVehicleDetailActivity.this).add(objectRequest);
    }

    public interface formerSharingCallbacks {
        void onSuccess(@NonNull Share share);

        void onError(@NonNull String errorMessage);
    }


    public boolean dispatchTouchEvent(MotionEvent ev) {
        View v = getCurrentFocus();
        if (isShouldHideInput(v, ev)) {
            hideSoftInput(v.getWindowToken());
            if (isShare
                    && companyName != null && companyName.length() > 0
                    && custID != null && custID.length() > 0
                    && date != null
                    && start != null
                    && end != null
                    && (!isRecurring || endDate != null)
            ) {
                saveImageButton.setAlpha(1.0f);
                saveImageButton.setClickable(true);
            } else if (!isShare && !NEW) {
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

    private <T extends View> T $(int id) {
        return (T) findViewById(id);
    }

}
