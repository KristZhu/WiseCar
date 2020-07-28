package com.wisecarCompany.wisecarapp.user.profile;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Environment;
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
import com.wisecarCompany.wisecarapp.function.HttpUtil;
import com.wisecarCompany.wisecarapp.user.profile.LoginActivity;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;


public class CreateUserActivity2 extends AppCompatActivity {

    private final static String TAG = "CreateUser2";

   // private byte[] userImg;
    private File userImgFile;
    private String username;
    private String userEmail;
    private String password;
    private String firstName;
    private String lastName;
    private java.util.Date dob;
    private String address1;
    private String address2;
    private String country;
    private String state;
    private String postCode;

    private EditText firstNameEditText;
    private EditText lastNameEditText;
    private EditText dobEditText;
    private EditText address1EditText;
    private EditText address2EditText;
    private EditText countryEditText;
    private EditText stateEditText;
    private EditText postCodeEditText;
    private ImageButton createImageButton;

    private SimpleDateFormat displayDateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());

    private final String IP_HOST = "http://54.206.19.123:3000";
    private final String CREATE_USER = "/api/v1/users/register";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_user2);

        //userImg = (byte[]) this.getIntent().getSerializableExtra("userImg");
        String userImgfilepath = this.getIntent().getStringExtra("userImg");
        //change into byte[]
        userImgFile = new File(userImgfilepath);
        //init array with file length
//        byte[] bytesArray = new byte[(int) file.length()];
//
//        FileInputStream fis = null;
//        try {
//            fis = new FileInputStream(file);
//            fis.read(bytesArray); //read file into bytes[]
//            fis.close();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        userImg = bytesArray;

        username = this.getIntent().getStringExtra("username");
        userEmail = this.getIntent().getStringExtra("userEmail");
        password = this.getIntent().getStringExtra("password");

        firstNameEditText = $(R.id.userFNameEditText);
        lastNameEditText = $(R.id.userLNameEditText);
        dobEditText = $(R.id.dobEditText);
        dobEditText.setInputType(InputType.TYPE_NULL);
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
                //SimpleDateFormat format = new SimpleDateFormat("ddMMM yyyy", Locale.getDefault());
                String str = displayDateFormat.format(dob);
                dobEditText.setText(str);
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
        });
        dobEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                Calendar c = Calendar.getInstance();
                new DatePickerDialog(CreateUserActivity2.this, (view, year, monthOfYear, dayOfMonth) -> {
                    dob = intToDate(year, monthOfYear, dayOfMonth);
                    //SimpleDateFormat format = new SimpleDateFormat("ddMMM yyyy", Locale.getDefault());
                    String str = displayDateFormat.format(dob);
                    dobEditText.setText(str);
                }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();

            }
        });


        createImageButton.setOnClickListener(v -> {
            Toast.makeText(getApplicationContext(), "Creating, Please Wait...", Toast.LENGTH_LONG).show();

            firstName = firstNameEditText.getText().toString();
            lastName = lastNameEditText.getText().toString();
            address1 = address1EditText.getText().toString();
            address2 = address2EditText.getText().toString();
            country = countryEditText.getText().toString();
            state = stateEditText.getText().toString();
            postCode = postCodeEditText.getText().toString();

            Log.d(TAG, "--------------------Create User------------------");
//            Log.d(TAG, "userImg: " + Arrays.toString(userImg));
            Log.d(TAG, "username: " + username);
            Log.d(TAG, "userEmail: " + userEmail);
            Log.d(TAG, "password: " + password);
            Log.d(TAG, "FName: " + firstName);
            Log.d(TAG, "LName: " + lastName);
            Log.d(TAG, "dob: " + dob);
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
        Thread thread = new Thread(() -> {

            HashMap<String, String> params = new HashMap<>();

            String message = null;
            int user_id = 0;

            DateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            try {
                params.put("user_name", username);
                params.put("first_name", firstName);
                params.put("last_name", lastName);
                params.put("date_of_birth", format.format(dob));
                params.put("address_line1", address1);
                params.put("address_line2", address2);
                params.put("postcode", postCode);
                params.put("state", state);
                params.put("country", country);
                params.put("email", userEmail);
                params.put("password", password);

//                Bitmap toBeUploaded = BitmapFactory.decodeByteArray(userImg, 0, userImg.length);
//                String root = Environment.getExternalStorageDirectory().toString();
//                File myDir = new File(root + "/saved_images");
//                myDir.mkdirs();
//
//                String fname = "userImage.png";
//                file = new File(myDir, fname);
//                if (file.exists()) file.delete();
//                file.createNewFile();
//                BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
//                toBeUploaded.compress(Bitmap.CompressFormat.PNG, 100, bos);
//
//                bos.flush();
//                bos.close();

                String response = HttpUtil.uploadForm(params, "logo", userImgFile, "userImage.png", IP_HOST + CREATE_USER);
                Log.e("response", response);
                try {
                    JSONObject jsonObject = new JSONObject(response);

                    message = jsonObject.optString("message");
                    user_id = jsonObject.optInt("user_id");

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            Log.e("testest", message + "  " + user_id);


            if (message != null && message.equals("success")) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(getApplicationContext(), "success", Toast.LENGTH_SHORT).show();
                    }
                });
                Intent intent = new Intent(CreateUserActivity2.this, LoginActivity.class);
                intent.putExtra("user_id", user_id);
                startActivity(intent);
            }
        });
        thread.start();
    }

    private <T extends View> T $(int id) {
        return (T) findViewById(id);
    }

}
