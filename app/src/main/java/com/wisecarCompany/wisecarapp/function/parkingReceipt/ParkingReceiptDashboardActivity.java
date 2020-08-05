package com.wisecarCompany.wisecarapp.function.parkingReceipt;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.wisecarCompany.wisecarapp.R;
import com.wisecarCompany.wisecarapp.user.vehicle.DashboardActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class ParkingReceiptDashboardActivity extends AppCompatActivity {

    private final static String TAG = "Parking Receipt D";

    private String IP_HOST = "http://7ce7ccc8008dec603016594c02f76d60-1846191374.ap-southeast-2.elb.amazonaws.com";
    private String GET_PARKING_RECEIPTS = "/api/v1/parkingreceipts//getallrecordbyuser";
    private String GET_PARKING_BY_REG_NO = "/api/v1/parkingreceipts/getrecordbyuserregisno";

    private SharedPreferences sp;
    private String userID;

    private ImageButton backImageButton;

    private LinearLayout mainDiv;
    private List<ParkingReceipt> allReceipts;

    private AutoCompleteTextView searchEditText;
    private ImageButton cancelImageButton;

    private SimpleDateFormat displayDateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());


    @SuppressLint("ResourceType")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parking_receipt_dashboard);

        sp = this.getSharedPreferences("userInfo", MODE_PRIVATE);
        userID = sp.getString("USER_ID", "");
        Log.d(TAG, "userID: " + userID);

        backImageButton = $(R.id.backImageButton);
        backImageButton.setOnClickListener(v -> startActivity(new Intent(this, DashboardActivity.class)));

        mainDiv = $(R.id.mainDiv);

        searchEditText = $(R.id.searchEditText);
        cancelImageButton = $(R.id.cancelImageButton);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new ArrayList<>());

        getParkingReceipt(new parkingReceiptCallbacks() {
            @Override
            public void onSuccess(@NonNull List<ParkingReceipt> receipts) {
                Log.e("Receipts: ", String.valueOf(receipts));
                allReceipts = receipts;
                Set<String> regNos = new HashSet<>();
                mainDiv.removeAllViews();
                for(ParkingReceipt receipt: receipts) {
                    regNos.add(receipt.getRegistrationNo());
                    showParkingReceipt(receipt);
                }
                adapter.addAll(regNos);
                searchEditText.setAdapter(adapter);
                searchEditText.setOnItemClickListener((parent, view, position, id) -> {
                    cancelImageButton.setVisibility(View.VISIBLE);
                    String regNo = searchEditText.getText().toString();
                    returnParkingReceiptByRegNo(regNo, new parkingReceiptCallbacks() {
                        @Override
                        public void onSuccess(@NonNull List<ParkingReceipt> receipts) {
                            mainDiv.removeAllViews();
                            for(ParkingReceipt receipt: receipts) showParkingReceipt(receipt);
                        }
                    });
                });
            }
        });

        cancelImageButton.setOnClickListener(v -> {
            searchEditText.setText("");
            cancelImageButton.setVisibility(View.INVISIBLE);
            mainDiv.removeAllViews();
            for(ParkingReceipt receipt: allReceipts) showParkingReceipt(receipt);
        });

    }


    @SuppressLint("ResourceType")
    private void showParkingReceipt(ParkingReceipt receipt) {
        ConstraintLayout lineLayout = new ConstraintLayout(this);
        ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 16, 0, 16);
        lineLayout.setLayoutParams(params);

        ConstraintSet set = new ConstraintSet();

        ImageView lightImageView = new ImageView(this);
        lightImageView.setId(0);
        lightImageView.setImageDrawable(getResources().getDrawable(R.drawable.dashboard0light_line));
        set.connect(lightImageView.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP);
        set.connect(lightImageView.getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM);
        set.connect(lightImageView.getId(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START);
        set.connect(lightImageView.getId(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END);
        set.setDimensionRatio(lightImageView.getId(), "4.5:1");
        set.constrainPercentHeight(lightImageView.getId(), (float)(2.0/3));
        set.setVerticalBias(lightImageView.getId(), 0.0f);
        lightImageView.setScaleType(ImageView.ScaleType.FIT_XY);
        lineLayout.addView(lightImageView);

        ImageView darkImageView = new ImageView(this);
        darkImageView.setId(1);
        darkImageView.setScaleType(ImageView.ScaleType.FIT_XY);
        darkImageView.setImageDrawable(getResources().getDrawable(R.drawable.dashboard0dark_line));
        set.connect(darkImageView.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP);
        set.connect(darkImageView.getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM);
        set.connect(darkImageView.getId(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START);
        set.connect(darkImageView.getId(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END);
        set.setDimensionRatio(darkImageView.getId(), "9:1");
        set.constrainPercentHeight(darkImageView.getId(), (float)(1.0/3));
        set.setVerticalBias(darkImageView.getId(), 1.0f);
        darkImageView.setScaleType(ImageView.ScaleType.FIT_XY);
        lineLayout.addView(darkImageView);

        TextView registrationNoTextView = new TextView(this);
        registrationNoTextView.setId(10);
        set.connect(registrationNoTextView.getId(), ConstraintSet.TOP, lightImageView.getId(), ConstraintSet.TOP);
        set.connect(registrationNoTextView.getId(), ConstraintSet.BOTTOM, lightImageView.getId(), ConstraintSet.BOTTOM);
        set.connect(registrationNoTextView.getId(), ConstraintSet.START, lightImageView.getId(), ConstraintSet.START, 32);
        set.connect(registrationNoTextView.getId(), ConstraintSet.END, lightImageView.getId(), ConstraintSet.END);
        set.constrainPercentHeight(registrationNoTextView.getId(), 0.25f);
        set.setVerticalBias(registrationNoTextView.getId(), 0.0f);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) registrationNoTextView.setAutoSizeTextTypeUniformWithConfiguration(10, 30, 1, TypedValue.COMPLEX_UNIT_SP);
        registrationNoTextView.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        registrationNoTextView.setTextColor(0xff007ba4);
        registrationNoTextView.setText(receipt.getRegistrationNo());
        lineLayout.addView(registrationNoTextView);

        TextView dateTextView = new TextView(this);
        dateTextView.setId(11);
        set.connect(dateTextView.getId(), ConstraintSet.TOP, registrationNoTextView.getId(), ConstraintSet.BOTTOM);
        set.connect(dateTextView.getId(), ConstraintSet.BOTTOM, lightImageView.getId(), ConstraintSet.BOTTOM);
        set.connect(dateTextView.getId(), ConstraintSet.START, lightImageView.getId(), ConstraintSet.START, 32);
        set.connect(dateTextView.getId(), ConstraintSet.END, lightImageView.getId(), ConstraintSet.END);
        set.constrainPercentHeight(dateTextView.getId(), 0.2f);
        set.setVerticalBias(dateTextView.getId(), 0.0f);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) dateTextView.setAutoSizeTextTypeUniformWithConfiguration(10, 30, 1, TypedValue.COMPLEX_UNIT_SP);
        dateTextView.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        dateTextView.setTextColor(0xff000000);
        dateTextView.setText("Date: " + displayDateFormat.format(receipt.getDate()));
        lineLayout.addView(dateTextView);

        TextView ticketRefTextView = new TextView(this);
        ticketRefTextView.setId(12);
        set.connect(ticketRefTextView.getId(), ConstraintSet.TOP, dateTextView.getId(), ConstraintSet.BOTTOM);
        set.connect(ticketRefTextView.getId(), ConstraintSet.BOTTOM, lightImageView.getId(), ConstraintSet.BOTTOM);
        set.connect(ticketRefTextView.getId(), ConstraintSet.START, lightImageView.getId(), ConstraintSet.START, 32);
        set.connect(ticketRefTextView.getId(), ConstraintSet.END, lightImageView.getId(), ConstraintSet.END);
        set.constrainPercentHeight(ticketRefTextView.getId(), 0.2f);
        set.setVerticalBias(ticketRefTextView.getId(), 0.0f);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) ticketRefTextView.setAutoSizeTextTypeUniformWithConfiguration(10, 30, 1, TypedValue.COMPLEX_UNIT_SP);
        ticketRefTextView.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        ticketRefTextView.setTextColor(0xff000000);
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        ticketRefTextView.setText("Ticket Ref: " + receipt.getRefNo());
        lineLayout.addView(ticketRefTextView);

        TextView claimTextView = new TextView(this);
        claimTextView.setId(13);
        set.connect(claimTextView.getId(), ConstraintSet.TOP, darkImageView.getId(), ConstraintSet.TOP);
        set.connect(claimTextView.getId(), ConstraintSet.BOTTOM, darkImageView.getId(), ConstraintSet.BOTTOM);
        set.connect(claimTextView.getId(), ConstraintSet.START, darkImageView.getId(), ConstraintSet.START, 32);
        set.connect(claimTextView.getId(), ConstraintSet.END, darkImageView.getId(), ConstraintSet.END);
        set.constrainPercentHeight(claimTextView.getId(), 0.2f);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) claimTextView.setAutoSizeTextTypeUniformWithConfiguration(10, 30, 1, TypedValue.COMPLEX_UNIT_SP);
        claimTextView.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        claimTextView.setTextColor(0xff000000);
        if(receipt.getCompanyName()==null || receipt.getCompanyName().length()==0) {
            claimTextView.setText("Not shared");
            claimTextView.setAlpha(0.5f);
        } else {
            claimTextView.setText("Claimed to " + receipt.getCompanyName());
        }
        lineLayout.addView(claimTextView);

        ImageView sentImageView = new ImageView(this);
        sentImageView.setId(20);
        set.connect(sentImageView.getId(), ConstraintSet.TOP, registrationNoTextView.getId(), ConstraintSet.TOP);
        set.connect(sentImageView.getId(), ConstraintSet.BOTTOM, ticketRefTextView.getId(), ConstraintSet.BOTTOM);  //do not know why... if constraint to background, there are bugs
        set.connect(sentImageView.getId(), ConstraintSet.START, lightImageView.getId(), ConstraintSet.START);
        set.connect(sentImageView.getId(), ConstraintSet.END, lightImageView.getId(), ConstraintSet.END, 32);
        set.setDimensionRatio(sentImageView.getId(), "1:1");
        set.constrainPercentWidth(sentImageView.getId(), 0.04f);
        set.setHorizontalBias(sentImageView.getId(), 1.0f);
        sentImageView.setScaleType(ImageView.ScaleType.FIT_XY);
        if(receipt.isSentBefore()) sentImageView.setImageDrawable(getResources().getDrawable(R.drawable.dashboard0sent));
        else sentImageView.setImageDrawable(getResources().getDrawable(R.drawable.dashboard0unsent));
        lineLayout.addView(sentImageView);

        ImageView sendImageView = new ImageView(this);
        sendImageView.setId(21);
        set.connect(sendImageView.getId(), ConstraintSet.TOP, registrationNoTextView.getId(), ConstraintSet.TOP);
        set.connect(sendImageView.getId(), ConstraintSet.BOTTOM, ticketRefTextView.getId(), ConstraintSet.BOTTOM);  //do not know y 2...
        set.connect(sendImageView.getId(), ConstraintSet.START, lightImageView.getId(), ConstraintSet.START);
        set.connect(sendImageView.getId(), ConstraintSet.END, sentImageView.getId(), ConstraintSet.START, 32);
        set.setDimensionRatio(sendImageView.getId(), "1:1");
        set.constrainPercentWidth(sendImageView.getId(), 0.1f);
        set.setHorizontalBias(sendImageView.getId(), 1.0f);
        sendImageView.setScaleType(ImageView.ScaleType.FIT_XY);
        sendImageView.setImageDrawable(getResources().getDrawable(R.drawable.dashboard0send));
        sendImageView.setOnClickListener(v -> {
            Log.d(TAG, "send ID: " + receipt.getId());
            startActivity(new Intent(this, ParkingReceiptSendActivity.class).putExtra("receiptID", receipt.getId()));
        });
        lineLayout.addView(sendImageView);

        set.applyTo(lineLayout);
        mainDiv.addView(lineLayout);
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

    private <T extends View> T $(int id){
        return (T) findViewById(id);
    }

    private void getParkingReceipt(@Nullable final parkingReceiptCallbacks callbacks) {

        String URL = IP_HOST + GET_PARKING_RECEIPTS;
        List<ParkingReceipt> receipts = new ArrayList();

        final JSONObject jsonParam = new JSONObject();
        try {
            jsonParam.put("user_id", userID);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest objectRequest = new JsonObjectRequest(Request.Method.POST, URL, jsonParam, response -> {
            Log.e("Logs Response", response.toString());
            JSONObject jsonObject;
            DateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

            try {
                JSONArray jsonArray = response.getJSONArray("record_list");

                for (int i = 0; i < jsonArray.length(); i++) {
                    jsonObject = jsonArray.getJSONObject(i);
                    ParkingReceipt receipt = new ParkingReceipt(
                            jsonObject.optString("id"),
                            jsonObject.optString("registration_no"),
                            format.parse(jsonObject.optString("date")),
                            jsonObject.optString("parking_reference_no"),
                            jsonObject.optString("company_name"),
                            jsonObject.optString("has_sent_before").equals("1")
                    );

                    receipts.add(receipt);
                }
                if (callbacks != null)
                    callbacks.onSuccess(receipts);
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (ParseException e) {
                e.printStackTrace();
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
        Volley.newRequestQueue(this).add(objectRequest);

    }

    public interface parkingReceiptCallbacks {
        void onSuccess(@NonNull List<ParkingReceipt> value);

//        void onError(@NonNull List<ServiceRecord> value);
    }



    private void returnParkingReceiptByRegNo(String regNo, @Nullable final parkingReceiptCallbacks callbacks) {

        String URL = IP_HOST + GET_PARKING_BY_REG_NO;

        List<ParkingReceipt> receipts = new ArrayList();

        final JSONObject jsonParam = new JSONObject();
        try {
            jsonParam.put("user_id", userID);
            jsonParam.put("registration_no", regNo);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest objectRequest = new JsonObjectRequest(Request.Method.POST, URL, jsonParam, response -> {
            Log.e("Records Response", response.toString());
            JSONObject jsonObject;
            DateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

            try {
                JSONArray jsonArray = response.getJSONArray("record_list");

                for (int i = 0; i < jsonArray.length(); i++) {
                    jsonObject = jsonArray.getJSONObject(i);
                    ParkingReceipt receipt;

                    receipt = new ParkingReceipt(
                            jsonObject.optString("id"),
                            jsonObject.optString("registration_no"),
                            format.parse(jsonObject.optString("date")),
                            jsonObject.optString("parking_reference_no"),
                            jsonObject.optString("company_name"),
                            jsonObject.optString("has_sent_before").equals("1")
                    );

                    receipts.add(receipt);
                }
                if (callbacks != null)
                    callbacks.onSuccess(receipts);
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }, error -> {

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
            }
        });

        Volley.newRequestQueue(this).add(objectRequest);
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, DashboardActivity.class));
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK) {
            startActivity(new Intent(this, DashboardActivity.class));
            return true;    //stop calling super method
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }

}
