package com.wisecarCompany.wisecarapp.function.shareVehicle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
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
import com.wisecarCompany.wisecarapp.R;
import com.wisecarCompany.wisecarapp.element.SwitchButton;
import com.wisecarCompany.wisecarapp.user.UserInfo;

//import org.apache.http.HttpResponse;
//import org.apache.http.client.HttpClient;
//import org.apache.http.client.methods.HttpPost;
//import org.apache.http.entity.mime.HttpMultipartMode;
//import org.apache.http.entity.mime.MultipartEntity;
//import org.apache.http.entity.mime.content.StringBody;
//import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

public class ShareVehicleDetailActivity extends AppCompatActivity {

    private static final String TAG = "ShareVehicleDetail";

    private final String IP_HOST = "http://7ce7ccc8008dec603016594c02f76d60-1846191374.ap-southeast-2.elb.amazonaws.com";
    private final String GET_COMPANY_LIST = "/api/v1/customers/customer/list";
    private final String SUBMIT_SHARE_VEHICLE = "/api/v1/sharevehicle/submit";
    private final String SHARE_CHECK = "/api/v1/sharevehicle/check";
    private final String EDIT_SHARE = "/api/v1/sharevehicle/update";
    private final String GET_HISTORY = "/api/v1/sharevehicle/sharedetailapp/";

    private boolean NEW;  //new add or edit

    private String shareID;

    private ImageButton backImageButton;
    private ImageButton saveImageButton;

    private AutoCompleteTextView searchEditText;
    //private ImageButton addImageButton;
    private TextView companyNameTextView;
    private String companyName;
    private TextView companyIDTextView;
    private String custID;
    private ImageButton cancelImageButton;

    private SwitchButton shareSwitchButton;
    private boolean isShare;
    private ConstraintLayout shareDiv;
    private EditText dateEditText;
    private Date date;
    private EditText startTimeEditText;
    private Date startTime;
    private EditText endTimeEditText;
    private Date endTime;
    private SwitchButton recurringSwitchButton;
    private boolean isRecurring = false;

    private ConstraintLayout recurringDiv;
    private EditText endDateEditText;
    private Date endDate;
    private CheckBox[] weekdayCheckBox;
    private boolean[] isWeekday;    //0: Sun, 6: Sat

    private SwitchButton visibilitySwitchButton;
    private boolean isVisibility = false;
    private LinearLayout visibilityDiv;
    private Map<Integer, Boolean> servicesVisibility = new TreeMap<>();   //value: visibility

    private SimpleDateFormat displayDateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
    private SimpleDateFormat displayTimeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_vehicle_detail);

        //vehicleID = (String) this.getIntent().getStringExtra("vehicleID");
        //vehicle = UserInfo.getVehicles().get(vehicleID);
        Log.d(TAG, "currVehicle: " + UserInfo.getCurrVehicle());
        assert UserInfo.getCurrVehicle() != null;

        NEW = (boolean) this.getIntent().getSerializableExtra("NEW");
        Log.d(TAG, "NEW: " + NEW);


        searchEditText = $(R.id.searchEditText);
        //addImageButton = $(R.id.addImageButton);
        companyIDTextView = $(R.id.companyIDTextView);
        companyNameTextView = $(R.id.companyNameTextView);
        cancelImageButton = $(R.id.cancelImageButton);

        shareDiv = $(R.id.shareDiv);
        shareSwitchButton = $(R.id.shareSwitchButton);

        dateEditText = $(R.id.dateEditText);
        dateEditText.setInputType(InputType.TYPE_NULL);
        startTimeEditText = $(R.id.startEditText);
        startTimeEditText.setInputType(InputType.TYPE_NULL);
        endTimeEditText = $(R.id.endEditText);
        endTimeEditText.setInputType(InputType.TYPE_NULL);

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

        saveImageButton = $(R.id.shareImageButton);


        if (NEW) {

            final ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new ArrayList<>());
            returnCompanies(new companiesCallbacks() {
                @SuppressLint("SetTextI18n")
                @Override
                public void onSuccess(@NonNull Map<String, String> companies) {
                    Log.d(TAG, "companies: " + companies);
                    for (String name : companies.keySet()) {
                        adapter.add(name);
                    }
                    searchEditText.setAdapter(adapter);
                    searchEditText.setOnItemClickListener((parent, view, position, id) -> {
                        companyName = searchEditText.getText().toString();
                        companyNameTextView.setText(companyName);
                        custID = companies.get(companyName);
                        cancelImageButton.setVisibility(View.VISIBLE);
                        checkReadyToSave();
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
                checkReadyToSave();
            });

            isShare = false;
            isRecurring = false;
            isVisibility = false;

            Log.d(TAG, "new share services: " + UserInfo.getCurrVehicle().getServices());
            for (int i : UserInfo.getCurrVehicle().getServices()) servicesVisibility.put(i, true);


        } else {    //edit
            shareID = (String) this.getIntent().getStringExtra("shareID");
            returnFormerSharingDetails(new formerSharingCallbacks() {
                @Override
                public void onSuccess(@NonNull Share share) {

                    companyName = share.getCompany_name();
                    custID = share.getCust_id();
                    companyNameTextView.setText(companyName);
                    companyIDTextView.setText(custID);
                    searchEditText.setKeyListener(null);

                    isShare = share.isShare();
                    if (isShare) {
                        shareSwitchButton.setToggleOn(true);
                        shareDiv.setVisibility(View.VISIBLE);

                        date = share.getDate();
                        startTime = share.getStart_time();
                        endTime = share.getEnd_time();
                        //SimpleDateFormat formatDate = new SimpleDateFormat("ddMMM yyyy", Locale.getDefault());
                        //SimpleDateFormat formatTime = new SimpleDateFormat("HH:mm", Locale.getDefault());
                        dateEditText.setText(displayDateFormat.format(date));
                        startTimeEditText.setText(displayTimeFormat.format(startTime));
                        endTimeEditText.setText(displayTimeFormat.format(endTime));

                        isRecurring = share.isRecurring();
                        Log.d(TAG, "isRecurring: " + isRecurring);
                        if (isRecurring) {
                            recurringSwitchButton.setToggleOn(true);
                            recurringDiv.setVisibility(View.VISIBLE);

                            endDate = share.getRecurring_end_date();
                            //SimpleDateFormat format = new SimpleDateFormat("ddMMM yyyy", Locale.getDefault());
                            endDateEditText.setText(displayDateFormat.format(endDate));
                            isWeekday = share.getRecurring_days();
                            for (int i = 0; i < 7; i++) {
                                if (isWeekday[i]) weekdayCheckBox[i].setChecked(true);
                                else weekdayCheckBox[i].setChecked(false);
                            }
                        }

                        servicesVisibility = share.getServicesVisibility();
                        Log.d(TAG, "servicesVisibility: " + servicesVisibility);
                        // this callback does not return immediately, so servicesVisibility at beginning is null
                        // and nothing will be added to visibilityDiv when showVisibilityCheckBoxes is called once this activity is created
                        // so here to call again
                        visibilityDiv.removeAllViews();
                        showVisibilityCheckBoxes();

                        isVisibility = false;
                        for (int i : servicesVisibility.keySet()) {
                            if (servicesVisibility.get(i)) { //has visible
                                isVisibility = true;
                                visibilitySwitchButton.setToggleOn(true);
                                visibilityDiv.setVisibility(View.VISIBLE);
                                break;
                            }
                        }

                    }

                    saveImageButton.setAlpha(1.0f);
                    saveImageButton.setClickable(true);

                }

                @Override
                public void onError(@NonNull String errorMessage) {

                }
            });
        }


        backImageButton = $(R.id.backImageButton);
        backImageButton.setOnClickListener(v -> startActivity(new Intent(ShareVehicleDetailActivity.this, ShareVehicleListActivity.class)));


        shareSwitchButton.setOnToggleChanged(isOn -> {
            Log.d(TAG, "share: " + isOn);
            isShare = isOn;
            if (isOn) shareDiv.setVisibility(View.VISIBLE);
            else shareDiv.setVisibility(View.GONE);
            checkReadyToSave();
        });


        dateEditText.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new DatePickerDialog(ShareVehicleDetailActivity.this, (view, year, monthOfYear, dayOfMonth) -> {
                date = intToDate(year, monthOfYear, dayOfMonth);
                //SimpleDateFormat format = new SimpleDateFormat("ddMMM yyyy", Locale.getDefault());
                String str = displayDateFormat.format(date);
                dateEditText.setText(str);
                checkReadyToSave();
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
        });
        dateEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                Calendar c = Calendar.getInstance();
                new DatePickerDialog(ShareVehicleDetailActivity.this, (view, year, monthOfYear, dayOfMonth) -> {
                    date = intToDate(year, monthOfYear, dayOfMonth);
                    //SimpleDateFormat format = new SimpleDateFormat("ddMMM yyyy", Locale.getDefault());
                    String str = displayDateFormat.format(date);
                    dateEditText.setText(str);
                    checkReadyToSave();
                }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        startTimeEditText.setOnClickListener(v -> {
            new TimePickerDialog(ShareVehicleDetailActivity.this, (view, hour, minute) -> {
                StringBuffer time = new StringBuffer();
                time.append(hour >= 10 ? hour : "0" + hour);
                time.append(":");
                time.append(minute >= 10 ? minute : "0" + minute);
                startTime = new Date(intToDate(1970, 0, 1).getTime() + (hour * 60 + minute) * 60 * 1000); //otherwise there is timezone problem
                startTimeEditText.setText(time);
                checkReadyToSave();
            }, 0, 0, true).show();
        });
        startTimeEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                new TimePickerDialog(ShareVehicleDetailActivity.this, (view, hour, minute) -> {
                    StringBuffer time = new StringBuffer();
                    time.append(hour >= 10 ? hour : "0" + hour);
                    time.append(":");
                    time.append(minute >= 10 ? minute : "0" + minute);
                    startTime = new Date(intToDate(1970, 0, 1).getTime() + (hour * 60 + minute) * 60 * 1000);
                    startTimeEditText.setText(time);
                    checkReadyToSave();
                }, 0, 0, true).show();
            }
        });

        endTimeEditText.setOnClickListener(v -> {
            new TimePickerDialog(ShareVehicleDetailActivity.this, (view, hour, minute) -> {
                StringBuffer time = new StringBuffer();
                time.append(hour >= 10 ? hour : "0" + hour);
                time.append(":");
                time.append(minute >= 10 ? minute : "0" + minute);
                endTime = new Date(intToDate(1970, 0, 1).getTime() + (hour * 60 + minute) * 60 * 1000);
                endTimeEditText.setText(time);
                checkReadyToSave();
            }, 0, 0, true).show();
        });
        endTimeEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                new TimePickerDialog(ShareVehicleDetailActivity.this, (view, hour, minute) -> {
                    StringBuffer time = new StringBuffer();
                    time.append(hour >= 10 ? hour : "0" + hour);
                    time.append(":");
                    time.append(minute >= 10 ? minute : "0" + minute);
                    endTime = new Date(intToDate(1970, 0, 1).getTime() + (hour * 60 + minute) * 60 * 1000);
                    endTimeEditText.setText(time);
                    checkReadyToSave();
                }, 0, 0, true).show();
            }
        });


        recurringSwitchButton.setOnToggleChanged(isOn -> {
            Log.d(TAG, "recurring: " + isOn);
            isRecurring = isOn;
            if (isOn) recurringDiv.setVisibility(View.VISIBLE);
            else recurringDiv.setVisibility(View.GONE);
            checkReadyToSave();
        });


        endDateEditText.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new DatePickerDialog(ShareVehicleDetailActivity.this, (view, year, monthOfYear, dayOfMonth) -> {
                endDate = intToDate(year, monthOfYear, dayOfMonth);
                //SimpleDateFormat format = new SimpleDateFormat("ddMMM yyyy", Locale.getDefault());
                String str = displayDateFormat.format(endDate);
                endDateEditText.setText(str);
                checkReadyToSave();
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
        });
        endDateEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                Calendar c = Calendar.getInstance();
                new DatePickerDialog(ShareVehicleDetailActivity.this, (view, year, monthOfYear, dayOfMonth) -> {
                    endDate = intToDate(year, monthOfYear, dayOfMonth);
                    //SimpleDateFormat format = new SimpleDateFormat("ddMMM yyyy", Locale.getDefault());
                    String str = displayDateFormat.format(endDate);
                    endDateEditText.setText(str);
                    checkReadyToSave();
                }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
            }
        });


        Log.d(TAG, "servicesVisibility: " + servicesVisibility);
        visibilityDiv.removeAllViews();
        showVisibilityCheckBoxes();

        visibilitySwitchButton.setOnToggleChanged(isOn -> {
            Log.d(TAG, "visibility: " + isOn);
            isVisibility = isOn;
            if (isOn) visibilityDiv.setVisibility(View.VISIBLE);
            else visibilityDiv.setVisibility(View.GONE);
            checkReadyToSave();
        });


        saveImageButton.setOnClickListener(v -> {
            if (saveImageButton.getAlpha() < 1) return;

            Log.d(TAG, "companyName: " + companyName);
            Log.d(TAG, "custID: " + custID);

            Log.d(TAG, "isShare: " + isShare);
            if (isShare) {
                Log.d(TAG, "date: " + date);
                Log.d(TAG, "start: " + startTime);
                Log.d(TAG, "end: " + endTime);

                Calendar todayCal = Calendar.getInstance();
                todayCal.set(todayCal.get(Calendar.YEAR), todayCal.get(Calendar.MONTH), todayCal.get(Calendar.DAY_OF_MONTH),0, 0, 0);   //still contains millisec, need /1000*1000 to get only date
                if (date.before(new Date(todayCal.getTime().getTime()/1000*1000))) {   //Start time can be today before real time. In the future, it needs to be after real time. getTime() causes timezone problems
                    Toast.makeText(getApplicationContext(), "Please enter correct date", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (startTime.after(endTime)) {
                    Toast.makeText(getApplicationContext(), "Please enter correct time", Toast.LENGTH_SHORT).show();
                    return;
                }

                Log.d(TAG, "isRecurring: " + isRecurring);
                if (isRecurring) {
                    Log.d(TAG, "endDate: " + endDate);
                    boolean day = false;
                    for (int i = 0; i < 7; i++) {
                        isWeekday[i] = weekdayCheckBox[i].isChecked();
                        day = day || isWeekday[i];
                        Log.d(TAG, "isWeekday" + i + ": " + isWeekday[i]);
                    }
                    if (!day) {
                        Toast.makeText(getApplicationContext(), "Please choose at least one day for recurring, or switch recurring off", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (endDate == null || endDate.before(date)) {
                        Toast.makeText(getApplicationContext(), "Please enter correct end date", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

                Log.d(TAG, "isVisibility: " + isVisibility);
                if (isVisibility) {
                    Log.d(TAG, "visibility: " + servicesVisibility);
                    boolean visible = false;
                    for (int i : servicesVisibility.keySet())
                        visible = visible || servicesVisibility.get(i);
                    if (!visible) {
                        Toast.makeText(getApplicationContext(), "Please choose at least one service visible, or switch service visibility off", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

            } else {    //cancel share
                if(NEW) {   //should not happen logically
                    Toast.makeText(getApplicationContext(), "Please switch share on", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            Toast.makeText(getApplicationContext(), "Saving, Please Wait...", Toast.LENGTH_SHORT).show();
            if (NEW) addShare();
            else editShare();

        });

    }

    @SuppressLint("SetTextI18n")
    private void showVisibilityCheckBoxes() {
        List<Map.Entry<Integer, Boolean>> servicesList = new ArrayList<>(servicesVisibility.entrySet());
        for (int i = 0; i < servicesList.size(); i += 2) {
            ConstraintLayout servicesLineLayout = new ConstraintLayout(ShareVehicleDetailActivity.this);
            ConstraintSet set = new ConstraintSet();
            CheckBox[] checkBoxes = new CheckBox[Math.min(servicesVisibility.size() - i, 2)];
            for (int j = 0; j < checkBoxes.length; j++) {
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
//                    case 6:
//                        checkBoxes[j].setText("Toll");
//                        checkBoxes[j].setChecked(servicesVisibility.get(6));
//                        checkBoxes[j].setOnCheckedChangeListener((buttonView, isChecked) -> servicesVisibility.put(6, isChecked));
//                        break;
                    case 6:
                        checkBoxes[j].setText("Fuel");
                        checkBoxes[j].setChecked(servicesVisibility.get(6));
                        checkBoxes[j].setOnCheckedChangeListener((buttonView, isChecked) -> servicesVisibility.put(6, isChecked));
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
    }

    private void returnCompanies(@Nullable final companiesCallbacks callbacks) {

        String URL = IP_HOST + GET_COMPANY_LIST;

        JsonObjectRequest objectRequest = new JsonObjectRequest(Request.Method.GET, URL, null, response -> {
            Log.e("Response: ", response.toString());
            JSONArray jsonArray;
            JSONObject jsonObject;
            Map<String, String> companies = new HashMap<>(); //<Name, ID> for name is not repeatable, and search company name by user
            try {
                jsonArray = response.getJSONArray("company_list");
                for (int i = 0; i < jsonArray.length(); i++) {
                    jsonObject = jsonArray.getJSONObject(i);
                    companies.put(jsonObject.optString("company_name"), jsonObject.optString("cust_id"));
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

    private void addShare() {

        StringBuilder recurringDays = new StringBuilder();
        for (int i = 0; i < 7; i++) {
            if (isWeekday[i]) recurringDays.append(i);
        }

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        DateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());

        String URL = IP_HOST + SUBMIT_SHARE_VEHICLE;

        final JSONObject jsonParam = new JSONObject();

        try {
            jsonParam.put("cust_id", custID);
            jsonParam.put("vehicle_id", UserInfo.getCurrVehicle().getVehicle_id());
            jsonParam.put("share", isShare ? "1" : "0");
            jsonParam.put("date", dateFormat.format(date));
            jsonParam.put("recurring", isRecurring ? "1" : "0");
            if (isRecurring) {
                jsonParam.put("recurring_end_date", dateFormat.format(endDate));
                jsonParam.put("recurring_days", recurringDays.toString());
            }
            jsonParam.put("service_visibility", isVisibility ? "1" : "0");
            if (isVisibility) {
                StringBuilder servicesSB = new StringBuilder();
                for (int i : servicesVisibility.keySet())
                    if (servicesVisibility.get(i)) servicesSB.append(i);
                jsonParam.put("visible_service_ids", servicesSB.toString());
            }
            jsonParam.put("start_time", timeFormat.format(startTime));
            jsonParam.put("end_time", timeFormat.format(endTime));

        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest objectRequest = new JsonObjectRequest(Request.Method.POST, URL, jsonParam, response -> {
            Log.e("submit Response", response.toString());
            if (response.optString("message").equals("success")) {
                runOnUiThread(() -> Toast.makeText(getApplicationContext(), "success", Toast.LENGTH_SHORT).show());
                startActivity(new Intent(ShareVehicleDetailActivity.this, ShareVehicleListActivity.class));
            }
            Log.e("response", response.toString());
            Log.e("share_id", response.optString("share_id"));
        }, error -> {
            Log.e("ERROR", String.valueOf(error.networkResponse));

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
                Log.e("JSON ERROR MESSAGE", message);
            }
        });

        String checkURL = IP_HOST + SHARE_CHECK;

        JsonObjectRequest checkObjectRequest = new JsonObjectRequest(Request.Method.POST, checkURL, jsonParam, response -> {
            Log.e("check Response", response.toString());
            if (response.optString("message").equals("validated")) {
                Volley.newRequestQueue(ShareVehicleDetailActivity.this).add(objectRequest);
            }
        }, error -> {
            Log.e("ERROR", String.valueOf(error.networkResponse));

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
                Log.e("JSON ERROR MESSAGE", message);
            }

        });
        Volley.newRequestQueue(ShareVehicleDetailActivity.this).add(checkObjectRequest);

    }


    private void editShare() {

        StringBuilder recurringDays = new StringBuilder();
        for (int i = 0; i < 7; i++) {
            if (isWeekday[i]) recurringDays.append(i);
        }

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        DateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());

        String URL = IP_HOST + EDIT_SHARE;

        final JSONObject jsonParam = new JSONObject();

        try {
            jsonParam.put("cust_id", custID);
            jsonParam.put("vehicle_id", UserInfo.getCurrVehicle().getVehicle_id());
            jsonParam.put("share", isShare ? "1" : "0");
            jsonParam.put("date", dateFormat.format(date));
            jsonParam.put("recurring", isRecurring ? "1" : "0");
            if (isRecurring) {
                jsonParam.put("recurring_end_date", dateFormat.format(endDate));
                jsonParam.put("recurring_days", recurringDays.toString());
            }
            jsonParam.put("service_visibility", isVisibility ? "1" : "0");
            if (isVisibility) {
                StringBuilder servicesSB = new StringBuilder();
                for (int i : servicesVisibility.keySet())
                    if (servicesVisibility.get(i)) servicesSB.append(i);
                jsonParam.put("visible_service_ids", servicesSB.toString());
            }
            jsonParam.put("start_time", timeFormat.format(startTime));
            jsonParam.put("end_time", timeFormat.format(endTime));
            if (!isShare) {
                jsonParam.put("mode", "0");
            } else {
                jsonParam.put("mode", "1");
            }
            jsonParam.put("share_id", shareID);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest objectRequest = new JsonObjectRequest(Request.Method.POST, URL, jsonParam, response -> {
            Log.e("Response", response.toString());
            if (response.optString("message").equals("success")) {
                runOnUiThread(() -> Toast.makeText(getApplicationContext(), "success", Toast.LENGTH_SHORT).show());
                Intent intent = new Intent(ShareVehicleDetailActivity.this, ShareVehicleListActivity.class);
                intent.putExtra("vehicleID", UserInfo.getCurrVehicle().getVehicle_id());
                startActivity(intent);
                Log.e("response", response.toString());
                Log.e("new_share_id", response.optString("new_share_id"));
            } else if (response.optString("message").equals("new_share_id")) {
                Log.e("share_id_inactivated", response.optString("share_id_inactivated"));
            }
        }, error -> {
            Log.e("ERROR", String.valueOf(error.networkResponse));

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
                Log.e("JSON ERROR MESSAGE", message);
            }

        });
        Volley.newRequestQueue(ShareVehicleDetailActivity.this).add(objectRequest);
    }

    private void returnFormerSharingDetails(@Nullable final formerSharingCallbacks callbacks) {

        String URL = IP_HOST + GET_HISTORY + shareID;

        @SuppressLint("SimpleDateFormat")
        JsonObjectRequest objectRequest = new JsonObjectRequest(Request.Method.GET, URL, null, response -> {
            Log.e("Response", response.toString());
            Share share = new Share();
            try {
                JSONObject jsonObject = response.getJSONObject("result");

                share.setCust_id(jsonObject.optString("cust_id"));
                share.setCompany_name(jsonObject.optString("company_name"));
                share.setShare(jsonObject.optInt("share_active") == 1);

                if (share.isShare()) {
                    share.setRecurring(jsonObject.optInt("recurring_flag") == 1);
                    if (share.isRecurring()) {
                        share.setRecurring_end_date(new SimpleDateFormat("yyyy-MM-dd").parse(jsonObject.optString("recurring_end_date")));
                        String recurringDaysStr = jsonObject.optString("recurring_days");
                        boolean[] recurringDays = new boolean[]{false, false, false, false, false, false, false};
                        for (char c : recurringDaysStr.toCharArray()) recurringDays[c - '0'] = true;
                        share.setRecurring_days(recurringDays);
                        Log.d(TAG, "recurring days str: " + recurringDaysStr);
                        Log.d(TAG, "recurring days: " + recurringDays[0] + recurringDays[1] + recurringDays[2] + recurringDays[3] + recurringDays[4] + recurringDays[5] + recurringDays[6]);
                    }
                    share.setDate(new SimpleDateFormat("yyyy-MM-dd").parse(jsonObject.optString("date")));
                    share.setStart_time(new SimpleDateFormat("HH:mm:ss").parse(jsonObject.optString("start_time")));
                    share.setEnd_time(new SimpleDateFormat("HH:mm:ss").parse(jsonObject.optString("end_time")));
                    JSONArray jsonArray = jsonObject.getJSONArray("service_list");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject object = jsonArray.getJSONObject(i);
                        share.getServicesVisibility().put(object.optInt("service_id"), object.optInt("is_visible") == 1);
                    }
                } else {

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

    private void checkReadyToSave() {
        if (isShare
                && companyName != null && companyName.length() > 0
                && custID != null && custID.length() > 0
                && date != null
                && startTime != null
                && endTime != null
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

    public boolean dispatchTouchEvent(MotionEvent ev) {
        View v = getCurrentFocus();
        if (isShouldHideInput(v, ev)) {
            hideSoftInput(v.getWindowToken());
            checkReadyToSave();
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

    private static Date intToDate(int year, int month, int day) {
        StringBuilder sb = new StringBuilder();
        if (day < 10) sb.append("0").append(day);
        else sb.append(day);
        sb.append("/");
        month++;
        if (month < 10) sb.append("0").append(month);
        else sb.append(month);
        sb.append("/").append(year);
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        Date date = null;
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

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, ShareVehicleListActivity.class));
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK) {
            startActivity(new Intent(this, ShareVehicleListActivity.class));
            return true;    //stop calling super method
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }

}
