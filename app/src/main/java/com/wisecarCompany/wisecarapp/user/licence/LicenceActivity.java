package com.wisecarCompany.wisecarapp.user.licence;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.wisecarCompany.wisecarapp.R;
import com.wisecarCompany.wisecarapp.function.HttpUtil;
import com.wisecarCompany.wisecarapp.user.vehicle.VehicleActivity;
import com.wisecarCompany.wisecarapp.viewElement.CircleImageView;
import com.wisecarCompany.wisecarapp.viewElement.SwitchButton;
import com.wisecarCompany.wisecarapp.user.UserInfo;

//import org.apache.http.HttpResponse;
//import org.apache.http.client.HttpClient;
//import org.apache.http.client.methods.HttpPost;
//import org.apache.http.entity.ContentType;
//import org.apache.http.entity.mime.HttpMultipartMode;
//import org.apache.http.entity.mime.MultipartEntity;
//import org.apache.http.entity.mime.content.ByteArrayBody;
//import org.apache.http.entity.mime.content.StringBody;
//import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.HashMap;
import java.util.Locale;

import cn.bingoogolapple.baseadapter.BGABaseAdapterUtil;
import cn.bingoogolapple.photopicker.imageloader.BGAImage;
import cn.bingoogolapple.photopicker.util.BGAPhotoHelper;
import cn.bingoogolapple.photopicker.util.BGAPhotoPickerUtil;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;


public class LicenceActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {

    private final static String TAG = "LicenceActivity";

    private ImageButton backImageButton;

    private CircleImageView licenceImageView;
    private Uri licenceImageUri;
    private Bitmap licenceImageBitmap;
    private Drawable licenceImageDrawable;

    private TextView idTextView;
    private TextView recordIDTextView;
    private Button uploadButton;

    private boolean active;
    private String number;
    private String type;
    private Date startDate;
    private int durationYear;
    private Date expireDate;
    private boolean remind;

    private SwitchButton activeSwitchButton;
    private EditText numberEditText;
    private EditText typeEditText;
    private EditText startDateEditText;
    private EditText expireEditText;
    private EditText expireDateEditText;
    private CheckBox remindCheckBox;

    private ImageButton saveImageButton;

    private static final int REQUEST_CODE_PERMISSION_CHOOSE_PHOTO = 1;
    private static final int REQUEST_CODE_PERMISSION_TAKE_PHOTO = 2;

    private static final int REQUEST_CODE_CHOOSE_PHOTO = 1;
    private static final int REQUEST_CODE_TAKE_PHOTO = 2;
    private static final int REQUEST_CODE_CROP = 3;

    private BGAPhotoHelper mPhotoHelper;

    private String identifier;
    private String record_id;

    private SimpleDateFormat displayDateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());

    // TO BE ADJUSTED
    private final String IP_HOST = "http://54.206.19.123:3000";
    private final String GET_LICENSE_IDENTIFIER = "/api/v1/driverlicense/identifier/";
    private final String ADD_LICENSE = "/api/v1/driverlicense";
    private final String GET_LICENSE = "/api/v1/driverlicense/getdriverlicense";
    private final String BLOCKCHAIN_IP = "http://13.236.209.122:3000";
    private final String INVOKE_BLOCKCHAIN = "/api/v1/driverlicense/blockchaininvoke";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_licence);

        Log.d(TAG, "username: " + UserInfo.getUsername());
        Log.d(TAG, "userID: " + UserInfo.getUserID());


        backImageButton = $(R.id.backImageButton);
        recordIDTextView = $(R.id.recordIDTextView);

        activeSwitchButton = $(R.id.activeSwitchButton);
        numberEditText = $(R.id.numberEditText);
        typeEditText = $(R.id.typeEditText);
        startDateEditText = $(R.id.startDateEditText);
        expireEditText = $(R.id.expireEditText);
        expireDateEditText = $(R.id.expireDateEditText);
        remindCheckBox = $(R.id.remindCheckBox);

        Log.d(TAG, "active: " + active);
        Log.d(TAG, "number: " + numberEditText.getText().toString());
        Log.d(TAG, "type: " + typeEditText.getText().toString());
        Log.d(TAG, "startDate: " + startDateEditText.getText().toString());
        Log.d(TAG, "durationYear: " + expireEditText.getText().toString());
        Log.d(TAG, "expireDate: " + expireDateEditText.getText().toString());
        Log.d(TAG, "remind: " + remindCheckBox.getText().toString());

        backImageButton.setOnClickListener(v -> startActivity(new Intent(LicenceActivity.this, VehicleActivity.class)));

        idTextView = $(R.id.idTextView);
        licenceImageView = $(R.id.licenceImageView);

        uploadButton = $(R.id.uploadButton);

        active = false;
        activeSwitchButton.setOnToggleChanged(isOn -> active = isOn);

        getLicense((returnedLicense, returnedIdentifier) -> {
            if (returnedLicense != null && returnedIdentifier != null) {
                Log.d(TAG, "getLicense successfully: " + returnedLicense + " " + returnedIdentifier);
                idTextView.setText("ID: " + returnedIdentifier);

                //SimpleDateFormat format = new SimpleDateFormat("ddMMM yyyy", Locale.getDefault());

                numberEditText.setText(returnedLicense.getNumber());
                number = returnedLicense.getNumber();
                disableEditText(numberEditText);

                typeEditText.setText(returnedLicense.getType());
                type = returnedLicense.getType();
                disableEditText(typeEditText);

                startDateEditText.setText(displayDateFormat.format(returnedLicense.getStartDate()));
                startDate = returnedLicense.getStartDate();
                disableEditText(startDateEditText);

                expireEditText.setText(returnedLicense.getExpire() + " years");
                durationYear = Integer.parseInt(returnedLicense.getExpire());
                disableEditText(expireEditText);

                expireDateEditText.setText(displayDateFormat.format(returnedLicense.getExpiryDate()));
                expireDate = returnedLicense.getExpiryDate();
                disableEditText(expireDateEditText);

                remindCheckBox.setChecked(returnedLicense.isRemind());
                remindCheckBox.setFocusable(false);
                remindCheckBox.setEnabled(false);
                remindCheckBox.setKeyListener(null);
                remindCheckBox.setTextColor(Color.GRAY);

                uploadButton.setFocusable(false);
                uploadButton.setEnabled(false);
                uploadButton.setKeyListener(null);
                uploadButton.setTextColor(Color.GRAY);

                if (returnedLicense.isActive()) {
                    activeSwitchButton.setToggleOn();
                    active = true;
                } else {
                    activeSwitchButton.setToggleOff();
                    active = false;
                }

                saveImageButton.setClickable(true);
                saveImageButton.setAlpha(1.0f);
            }
        });


        getIdentifier((returnedIdentifier, returnedRecord_id) -> {

            Log.e("license identifier", identifier);
            Log.e("license record_id", record_id);
//                identifier = returnedIdentifier;
//                record_id = returnedRecord_id;

            String idToBeShown = "ID: " + identifier;

            idTextView.setText(idToBeShown);
            recordIDTextView.setText(returnedRecord_id);
        });

        // The directory for storing photos after taking a photo.
        // Change to the directory where you want to store the photos after you take them.
        // If this parameter is not passed, there is no camera function
        File takePhotoDir = new File(Environment.getExternalStorageDirectory(), "BGAPhotoPickerTakePhoto");
        mPhotoHelper = new BGAPhotoHelper(takePhotoDir);

        uploadButton.setOnClickListener(v -> {
            final String[] ways = new String[]{"Take a photo", "Upload from phone"};
            AlertDialog alertDialog3 = new AlertDialog.Builder(this)
                    .setTitle("How to upload? ")
                    .setIcon(R.mipmap.ic_launcher)
                    .setItems(ways, (dialogInterface, i) -> {
                        Log.d(TAG, "onClick: " + ways[i]);
                        if (i == 0) {  //take photo
                            takePhoto();
                        } else {   //upload from phone
                            choosePhoto();
                        }
                    }).setNegativeButton("cancel", null)
                    .create();
            alertDialog3.show();
        });

        startDateEditText.setInputType(InputType.TYPE_NULL);
        //SimpleDateFormat format = new SimpleDateFormat("ddMMM yyyy", Locale.getDefault());
        startDateEditText.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new DatePickerDialog(LicenceActivity.this, (view, year, monthOfYear, dayOfMonth) -> {
                startDate = intToDate(year, monthOfYear, dayOfMonth);
                String str = displayDateFormat.format(startDate);
                startDateEditText.setText(str);
                checkReadyToSave();
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
        });
        startDateEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                Calendar c = Calendar.getInstance();
                new DatePickerDialog(LicenceActivity.this, (view, year, monthOfYear, dayOfMonth) -> {
                    startDate = intToDate(year, monthOfYear, dayOfMonth);
                    String str = displayDateFormat.format(startDate);
                    startDateEditText.setText(str);
                    checkReadyToSave();
                }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        expireEditText.setInputType(InputType.TYPE_NULL);
        expireEditText.setOnClickListener(v -> {
            final String[] types = new String[]{"1 Year", "2 Years", "3 Years", "4 Years", "5 Years", "6 Years", "7 Years", "8 Years", "9 Years", "10 Years"};
            AlertDialog alertDialog = new AlertDialog.Builder(LicenceActivity.this)
                    //.setTitle("select a cover type")
                    .setIcon(R.mipmap.ic_launcher)
                    .setItems(types, (dialogInterface, i) -> {
                        expireEditText.setText(types[i]);
                        durationYear = i + 1;
                        checkReadyToSave();
                    })
                    .create();
            alertDialog.show();
        });
        expireEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                final String[] types = new String[]{"1 Year", "2 Years", "3 Years", "4 Years", "5 Years", "6 Years", "7 Years", "8 Years", "9 Years", "10 Years"};
                AlertDialog alertDialog = new AlertDialog.Builder(LicenceActivity.this)
                        //.setTitle("select a cover type")
                        .setIcon(R.mipmap.ic_launcher)
                        .setItems(types, (dialogInterface, i) -> {
                            expireEditText.setText(types[i]);
                            durationYear = i + 1;
                            checkReadyToSave();
                        })
                        .create();
                alertDialog.show();
            }
        });

        expireDateEditText.setInputType(InputType.TYPE_NULL);


        saveImageButton = $(R.id.saveImageButton);
        saveImageButton.setOnClickListener(v -> {
            if (saveImageButton.getAlpha() < 1) return;
            //licenceImageDrawable = licenceImageView.getDrawable();
            //...
            //UserInfo.getLicence().setLicenceImg();
            Toast.makeText(getApplicationContext(), "Saving, Please Wait...", Toast.LENGTH_LONG).show();

            Log.d(TAG, "active: " + active);
            Log.d(TAG, "number: " + number);
            Log.d(TAG, "type: " + type);
            Log.d(TAG, "startDate: " + startDate);
            Log.d(TAG, "durationYear: " + durationYear);
            Log.d(TAG, "expireDate: " + expireDate);
            Log.d(TAG, "remind: " + remind);

//            if(startDate.after(new Date()) || expireDate.before(new Date()) || startDate.after(expireDate)) {
//                Toast.makeText(getApplicationContext(), "Please select correct date", Toast.LENGTH_SHORT).show();
//                return;
//            }

            //UserInfo.setLicence(new Licence(active, number, type, startDate, expireDate, remind));

            //db
            uploadLicence();


        });

    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        BGAPhotoHelper.onSaveInstanceState(mPhotoHelper, outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        BGAPhotoHelper.onRestoreInstanceState(mPhotoHelper, savedInstanceState);
    }

    @AfterPermissionGranted(REQUEST_CODE_PERMISSION_CHOOSE_PHOTO)
    public void choosePhoto() {
        String[] perms = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if (EasyPermissions.hasPermissions(this, perms)) {
            startActivityForResult(mPhotoHelper.getChooseSystemGalleryIntent(), REQUEST_CODE_CHOOSE_PHOTO);
        } else {
            EasyPermissions.requestPermissions(this, "Please enable storage permissions to use the App", REQUEST_CODE_PERMISSION_CHOOSE_PHOTO, perms);
        }
    }

    @AfterPermissionGranted(REQUEST_CODE_PERMISSION_TAKE_PHOTO)
    public void takePhoto() {
        String[] perms = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA};
        if (EasyPermissions.hasPermissions(this, perms)) {
            try {
                startActivityForResult(mPhotoHelper.getTakePhotoIntent(), REQUEST_CODE_TAKE_PHOTO);
            } catch (Exception e) {
                BGAPhotoPickerUtil.show("The current device does not support taking photos");
            }
        } else {
            EasyPermissions.requestPermissions(this, "Please enable storage and camera permissions to use the App", REQUEST_CODE_PERMISSION_TAKE_PHOTO, perms);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CODE_CHOOSE_PHOTO) {
                try {
                    startActivityForResult(mPhotoHelper.getCropIntent(mPhotoHelper.getFilePathFromUri(data.getData()), 200, 200), REQUEST_CODE_CROP);
                } catch (Exception e) {
                    mPhotoHelper.deleteCropFile();
                    BGAPhotoPickerUtil.show("The current device does not support cropping photos");
                    e.printStackTrace();
                }
            } else if (requestCode == REQUEST_CODE_TAKE_PHOTO) {
                try {
                    startActivityForResult(mPhotoHelper.getCropIntent(mPhotoHelper.getCameraFilePath(), 200, 200), REQUEST_CODE_CROP);
                } catch (Exception e) {
                    mPhotoHelper.deleteCameraFile();
                    mPhotoHelper.deleteCropFile();
                    BGAPhotoPickerUtil.show("The current device does not support cropping photos");
                    e.printStackTrace();
                }
            } else if (requestCode == REQUEST_CODE_CROP) {
                BGAImage.display(licenceImageView, R.drawable.profile0empty_image, mPhotoHelper.getCropFilePath(), BGABaseAdapterUtil.dp2px(200));
            }
        } else {
            if (requestCode == REQUEST_CODE_CROP) {
                mPhotoHelper.deleteCameraFile();
                mPhotoHelper.deleteCropFile();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
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

    private void checkReadyToSave() {
        //SimpleDateFormat format = new SimpleDateFormat("ddMMM yyyy", Locale.getDefault());
        if (startDateEditText.getText().toString().length() > 0 && durationYear > 0) {  //calculate expire date
            try {
                startDate = displayDateFormat.parse(startDateEditText.getText().toString());
                Calendar expireCalendar = Calendar.getInstance();
                expireCalendar.setTime(startDate);
                expireCalendar.add(Calendar.YEAR, durationYear);
                expireCalendar.add(Calendar.DAY_OF_MONTH, -1);
                expireDate = expireCalendar.getTime();
                expireDateEditText.setText(displayDateFormat.format(expireDate));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        if (numberEditText.getText().toString().length() > 0
                && typeEditText.getText().toString().length() > 0
                && startDateEditText.getText().toString().length() > 0
                && expireEditText.getText().toString().length() > 0
                && expireDateEditText.getText().toString().length() > 0) {
            saveImageButton.setAlpha(1.0f);
            saveImageButton.setClickable(true);
            try {
                number = numberEditText.getText().toString();
                type = typeEditText.getText().toString();
                startDate = displayDateFormat.parse(startDateEditText.getText().toString());
                expireDate = displayDateFormat.parse(expireDateEditText.getText().toString());
                remind = remindCheckBox.isChecked();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } else {
            saveImageButton.setAlpha(0.5f);
            saveImageButton.setClickable(false);
//                Log.d(TAG, "number: " + numberEditText.getText().toString());
//                Log.d(TAG, "type: " + typeEditText.getText().toString());
//                Log.d(TAG, "startDate: " + startDateEditText.getText().toString());
//                Log.d(TAG, "expireDate: " + expireDateEditText.getText().toString());
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

    private <T extends View> T $(int id) {
        return (T) findViewById(id);
    }


    private void getIdentifier(@Nullable final recordIdentifierCallback callbacks) {

        String URL = IP_HOST + GET_LICENSE_IDENTIFIER + UserInfo.getUsername() + "/" + UserInfo.getUserID();

        JsonObjectRequest objectRequest = new JsonObjectRequest(Request.Method.GET, URL, null, response -> {
            Log.e("Response: ", response.toString());
            JSONObject jsonObject = response;

            identifier = jsonObject.optString("identifier");
            record_id = jsonObject.optString("record_id");

            if (callbacks != null)
                callbacks.onSuccess(identifier, record_id);

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
                Log.e("Error", message);
//                    if (callbacks != null)
//                        callbacks.onError(message);
            }

        });

        Volley.newRequestQueue(LicenceActivity.this).add(objectRequest);
    }

    public interface recordIdentifierCallback {
        void onSuccess(@NonNull String returnedIdentifier, String returnedRecord_id);

//        void onError(@NonNull String errorMessage);
    }

    private void uploadLicence() {

        String isActive = "";
        String isRemind = "";

        if (active) {
            isActive = "1";
        } else {
            isActive += "0";
        }

        if (remind) {
            isRemind += "1";
        } else {
            isRemind += "0";
        }

        String finalIsRemind = isRemind;
        String finalIsActive = isActive;

        Thread thread = new Thread(() -> {
            HashMap<String, String> params = new HashMap<>();
            File file = null;
            String message = null;
            String encrypt_hash = null;
            String s3_temp_path = null;

            DateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

            try {
                params.put("driver_license_identifier", idTextView.getText().toString().substring(4));
                Log.e("identifier in request", idTextView.getText().toString().substring(4));

                params.put("user_id", UserInfo.getUserID());
                params.put("license_no", number);
                params.put("license_type", type);
                params.put("start_date", format.format(startDate));
                params.put("expires_in", String.valueOf(durationYear));
                params.put("expiry_date", format.format(expireDate));
                params.put("remind_me", finalIsRemind);
                params.put("licence_status", finalIsActive);
                params.put("record_id", recordIDTextView.getText().toString());
                Log.e("recordID", recordIDTextView.getText().toString());

                /*if (!((BitmapDrawable) licenceImageView.getDrawable()).getBitmap()
                        .sameAs(((BitmapDrawable) ResourcesCompat.getDrawable(getResources(), R.drawable.licence0camera, null)).getBitmap())) {
                    Bitmap toBeUploaded = ((BitmapDrawable) licenceImageView.getDrawable()).getBitmap();

                    String root = Environment.getExternalStorageDirectory().toString();
                    File myDir = new File(root + "/saved_images");
                    myDir.mkdirs();

                    String fname = "license.png";
                    file = new File(myDir, fname);
                    if (file.exists()) file.delete();
                    file.createNewFile();
                    BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
                    toBeUploaded.compress(Bitmap.CompressFormat.PNG, 100, bos);

                    bos.flush();
                    bos.close();
                }*/

                file = mPhotoHelper.getCropFilePath()==null? null : new File(mPhotoHelper.getCropFilePath());

                String response = HttpUtil.uploadForm(params, "document", file, "license.png", IP_HOST + ADD_LICENSE);

                try {
                    JSONObject jsonObject = new JSONObject(response);
                    message = jsonObject.optString("message");
                    encrypt_hash = jsonObject.optString("encrypt_hash");
                    s3_temp_path = jsonObject.optString("s3_temp_path");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            Log.e("testest", message + "  " + encrypt_hash + "  " + s3_temp_path);

            if (message.equals("success")) {
                if (!encrypt_hash.equals("") && !s3_temp_path.equals("")) {
                    invokeBlockchain(idTextView.getText().toString().substring(4),
                            number,
                            type,
                            format.format(startDate),
                            format.format(expireDate),
                            String.valueOf(durationYear),
                            encrypt_hash,
                            s3_temp_path);
                }

                runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(LicenceActivity.this, "success", Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(LicenceActivity.this, VehicleActivity.class);
                        startActivity(intent);
                    }
                });
            }
        });
        thread.start();
    }

    private void invokeBlockchain(String identifier, String license_no, String license_type, String start_date, String expiry_date, String expires_in, String ecrypt_hash, String license_file_location) {

        String URL = BLOCKCHAIN_IP + INVOKE_BLOCKCHAIN;

        final JSONObject jsonParam = new JSONObject();
        try {
            jsonParam.put("identifier", identifier);
            jsonParam.put("record_type", "license");
            jsonParam.put("license_no", license_no);
            jsonParam.put("license_type", license_type);
            jsonParam.put("start_date", start_date);
            jsonParam.put("expiry_date", expiry_date);
            jsonParam.put("expires_in", expires_in);
            jsonParam.put("ecrypt_hash", ecrypt_hash);
            jsonParam.put("license_file_location", license_file_location);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest objectRequest = new JsonObjectRequest(Request.Method.POST, URL, jsonParam, response -> {

            Log.e("Blockchain Response", response.toString());
            Log.e("Blockchain submission", response.optString("message"));

        }, error -> {
            Log.e("Blockchain ERROR", String.valueOf(error.networkResponse));

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
        Volley.newRequestQueue(LicenceActivity.this).add(objectRequest);

    }

    private void getLicense(@Nullable final getLicenseCallback callbacks) {

        Licence license = new Licence();

        DateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        String URL = IP_HOST + GET_LICENSE;

        final JSONObject jsonParam = new JSONObject();
        try {
            jsonParam.put("user_id", UserInfo.getUserID());

        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest objectRequest = new JsonObjectRequest(Request.Method.POST, URL, jsonParam, response -> {
            Log.e("Response: ", response.toString());
            JSONObject jsonObject = response;

            if (jsonObject.optString("message").equals("There is no driver license record for this user"))
                callbacks.onSuccess(null, null);
            else {
                if (!jsonObject.optString("license_no").equals("null")) {
                    license.setNumber(jsonObject.optString("license_no"));
                    license.setType(jsonObject.optString("license_type"));
                    try {
                        license.setStartDate(format.parse(jsonObject.optString("start_date")));
                        license.setExpiryDate(format.parse(jsonObject.optString("expiry_date")));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    license.setExpire(jsonObject.optString("expires_in"));
                    license.setRemind(jsonObject.optString("reminder").equals("1"));
                    license.setActive(jsonObject.optString("license_status").equals("1"));

                    callbacks.onSuccess(license, jsonObject.optString("driver_license_identifier"));
                }
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
                Log.e("Error", message);
//                    if (callbacks != null)
//                        callbacks.onError(message);
            }

        });

        Volley.newRequestQueue(LicenceActivity.this).add(objectRequest);
    }

    public interface getLicenseCallback {
        void onSuccess(@NonNull Licence license, String identifier);

//        void onError(@NonNull String errorMessage);
    }

    private void disableEditText(EditText editText) {
        editText.setFocusable(false);
        editText.setEnabled(false);
        editText.setCursorVisible(false);
        editText.setKeyListener(null);
        editText.setTextColor(Color.GRAY);
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, VehicleActivity.class));
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK) {
            startActivity(new Intent(this, VehicleActivity.class));
            return true;    //stop calling super method
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }

}
