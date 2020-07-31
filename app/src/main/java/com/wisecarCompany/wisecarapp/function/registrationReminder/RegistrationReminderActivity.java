package com.wisecarCompany.wisecarapp.function.registrationReminder;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.wisecarCompany.wisecarapp.function.HttpUtil;
import com.wisecarCompany.wisecarapp.user.vehicle.ManageVehicleActivity;
import com.wisecarCompany.wisecarapp.R;
import com.wisecarCompany.wisecarapp.user.UserInfo;
import com.wisecarCompany.wisecarapp.user.vehicle.Vehicle;

import net.glxn.qrgen.android.QRCode;
import net.glxn.qrgen.core.image.ImageType;

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

public class RegistrationReminderActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {

    private static final String TAG = "Registration Reminder";

    private Vehicle vehicle;
    private String vehicleID;

    private ImageButton backImageButton;

    private TextView serviceIDTextView;
    private ImageView qrImageView;
    private Bitmap qrImageBitmap;
    private Uri qrImageUri;
    private Button uploadButton;
    private ImageButton cameraImageButton;
    private TextView recordIDTextView;
    private Button resetButton;

    private String servicesOptions;
    private String currentDate;
    private String identifier;
    private String record_id;
    private Bitmap qrCodeBitmap;

    private EditText paymentEditText;
    private EditText dateEditText;
    private EditText expireEditText;
    private EditText expireDateEditText;
    private CheckBox remindCheckBox;

    private String payment;
    private Date date;
    private int durationMonth;
    private Date expireDate;
    private boolean remind;

    private ImageButton saveImageButton;

    private static final int REQUEST_CODE_PERMISSION_CHOOSE_PHOTO = 1;
    private static final int REQUEST_CODE_PERMISSION_TAKE_PHOTO = 2;

    private static final int REQUEST_CODE_CHOOSE_PHOTO = 1;
    private static final int REQUEST_CODE_TAKE_PHOTO = 2;
    private static final int REQUEST_CODE_CROP = 3;

    private BGAPhotoHelper mPhotoHelper;

    private final String IP_HOST = "http://54.206.19.123:3000";
    private final String GET_REGISTRATION_RECORD_IDENTIFIER = "/api/v1/registrationrecords/identifier/";
    private final String scanQRCode = "/api/v1/registrationrecords/upload?identifier=";
    private final String ADD_REGISTRATION_RECORD = "/api/v1/registrationrecords/";
    private final String BLOCKCHAIN_IP = "http://13.236.209.122:3000";
    private final String INVOKE_BLOCKCHAIN = "/api/v1/registrationrecords/blockchaininvoke";

    private SimpleDateFormat displayDateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration_reminder);

        backImageButton = $(R.id.backImageButton);
        backImageButton.setOnClickListener(v -> startActivity(new Intent(RegistrationReminderActivity.this, ManageVehicleActivity.class).putExtra("vehicleID", vehicleID)));

        vehicleID = (String) this.getIntent().getStringExtra("vehicleID");
        Log.d(TAG, "vehicleID: " + vehicleID);
        vehicle = UserInfo.getVehicles().get(vehicleID);
        Log.d(TAG, "vehicle: " + vehicle);

        serviceIDTextView = $(R.id.serviceIDTextView);
        qrImageView = $(R.id.qrImageView);
        uploadButton = $(R.id.uploadButton);
        cameraImageButton = $(R.id.cameraImageButton);
        recordIDTextView = $(R.id.recordIDTextView);
        resetButton = $(R.id.resetButton);

        resetButton.setOnClickListener(v -> qrImageView.setImageBitmap(qrCodeBitmap));

        uploadButton.setOnClickListener(v -> {
            //not implemented
            Log.d(TAG, "upload record: ");
        });

        getRecordIdentifier((returnedIdentifier, returnedRecord_id) -> {

            Log.e("registration identifier", identifier);
            Log.e("registration record_id", record_id);
//                identifier = returnedIdentifier;
//                record_id = returnedRecord_id;

            String idToBeShown = "ID: " + identifier;

            int width = qrImageView.getWidth();
            int height = qrImageView.getHeight();

            serviceIDTextView.setText(idToBeShown);
            recordIDTextView.setText(returnedRecord_id);

            File qrCodeFile = QRCode.from(IP_HOST + scanQRCode + identifier).to(ImageType.PNG).withSize(width, height).file();
            qrCodeBitmap = BitmapFactory.decodeFile(qrCodeFile.getPath());
            qrImageView.setImageBitmap(qrCodeBitmap);
        });

        // The directory for storing photos after taking a photo.
        // Change to the directory where you want to store the photos after you take them.
        // If this parameter is not passed, there is no camera function
        File takePhotoDir = new File(Environment.getExternalStorageDirectory(), "BGAPhotoPickerTakePhoto");
        mPhotoHelper = new BGAPhotoHelper(takePhotoDir);

        cameraImageButton.setOnClickListener(v -> {
            final String[] ways = new String[]{"Take a photo", "Upload from phone"};
            AlertDialog alertDialog = new AlertDialog.Builder(this)
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
            alertDialog.show();
        });

        paymentEditText = $(R.id.paymentEditText);
        dateEditText = $(R.id.dateEditText);
        expireEditText = $(R.id.expireEditText);
        expireDateEditText = $(R.id.expireDateEditText);
        remindCheckBox = $(R.id.remindCheckBox);

        dateEditText.setInputType(InputType.TYPE_NULL);
        dateEditText.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new DatePickerDialog(RegistrationReminderActivity.this, (view, year, monthOfYear, dayOfMonth) -> {
                date = intToDate(year, monthOfYear, dayOfMonth);
                //SimpleDateFormat format13 = new SimpleDateFormat("ddMMM yyyy", Locale.getDefault());
                String str = displayDateFormat.format(date);
                dateEditText.setText(str);
                checkReadyToSave();
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
        });
        dateEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                Calendar c = Calendar.getInstance();
                new DatePickerDialog(RegistrationReminderActivity.this, (view, year, monthOfYear, dayOfMonth) -> {
                    date = intToDate(year, monthOfYear, dayOfMonth);
                    //SimpleDateFormat format14 = new SimpleDateFormat("ddMMM yyyy", Locale.getDefault());
                    String str = displayDateFormat.format(date);
                    dateEditText.setText(str);
                    checkReadyToSave();
                }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        expireEditText.setInputType(InputType.TYPE_NULL);
        expireEditText.setOnClickListener(v -> {
            final String[] types = new String[]{"1 Month", "2 Month", "3 Month", "4 Month", "5 Month", "6 Month",
                    "7 Month", "8 Month", "9 Month", "10 Month", "11 Month", "12 Month"};
            AlertDialog alertDialog = new AlertDialog.Builder(RegistrationReminderActivity.this)
                    //.setTitle("select a cover type")
                    .setIcon(R.mipmap.ic_launcher)
                    .setItems(types, (dialogInterface, i) -> {
                        expireEditText.setText(types[i]);
                        durationMonth = i + 1;
                        checkReadyToSave();
                    })
                    .create();
            alertDialog.show();
        });
        expireEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                final String[] types = new String[]{"1 Month", "2 Month", "3 Month", "4 Month", "5 Month", "6 Month",
                        "7 Month", "8 Month", "9 Month", "10 Month", "11 Month", "12 Month"};
                AlertDialog alertDialog = new AlertDialog.Builder(RegistrationReminderActivity.this)
                        //.setTitle("select a cover type")
                        .setIcon(R.mipmap.ic_launcher)
                        .setItems(types, (dialogInterface, i) -> {
                            expireEditText.setText(types[i]);
                            durationMonth = i + 1;
                            checkReadyToSave();
                        })
                        .create();
                alertDialog.show();
            }
        });

        expireDateEditText.setInputType(InputType.TYPE_NULL);
 /*
        expireDateEditText.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new DatePickerDialog(RegistrationReminderActivity.this, (view, year, monthOfYear, dayOfMonth) -> {
                expireDate = intToDate(year, monthOfYear, dayOfMonth);
                SimpleDateFormat format13 = new SimpleDateFormat("ddMMM yyyy", Locale.getDefault());
                String str = format13.format(expireDate);
                expireDateEditText.setText(str);
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
        });
        expireDateEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                Calendar c = Calendar.getInstance();
                new DatePickerDialog(RegistrationReminderActivity.this, (view, year, monthOfYear, dayOfMonth) -> {
                    expireDate = intToDate(year, monthOfYear, dayOfMonth);
                    SimpleDateFormat format14 = new SimpleDateFormat("ddMMM yyyy", Locale.getDefault());
                    String str = format14.format(expireDate);
                    expireDateEditText.setText(str);
                }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
*/
        saveImageButton = $(R.id.saveImageButton);
        saveImageButton.setOnClickListener(v -> {
            if (saveImageButton.getAlpha() < 1) return;
            Toast.makeText(getApplicationContext(), "Saving, Please Wait...", Toast.LENGTH_LONG).show();

            //Log.d(TAG, "userID" + UserInfo.getUserID());
            //Log.d(TAG, "vehicle" + vehicle);
            Log.d(TAG, "payment: " + payment);
            Log.d(TAG, "date: " + date);
            Log.d(TAG, "durationMonth: " + durationMonth);
            Log.d(TAG, "expireDate: " + expireDate);
            Log.d(TAG, "remind: " + remind);

            if (date.after(new Date()) || expireDate.before(new Date()) || date.after(expireDate)) {
                Toast.makeText(getApplicationContext(), "Please enter correct date", Toast.LENGTH_SHORT).show();
                return;
            }


            //db
            uploadServiceRecord();


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
                BGAImage.display(qrImageView, R.drawable.profile0empty_image, mPhotoHelper.getCropFilePath(), BGABaseAdapterUtil.dp2px(200));
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


    private void checkReadyToSave() {
        //SimpleDateFormat format = new SimpleDateFormat("ddMMM yyyy", Locale.getDefault());
        if (dateEditText.getText().toString().length() > 0 && durationMonth > 0) {
            try {
                date = displayDateFormat.parse(dateEditText.getText().toString());
                Calendar expireCalendar = Calendar.getInstance();
                expireCalendar.setTime(date);
                expireCalendar.add(Calendar.MONTH, durationMonth);
                expireCalendar.add(Calendar.DAY_OF_MONTH, -1);
                expireDate = expireCalendar.getTime();
                expireDateEditText.setText(displayDateFormat.format(expireDate));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        if (paymentEditText.getText().toString().length() > 0
                && dateEditText.getText().toString().length() > 0
                && expireEditText.getText().toString().length() > 0
                && expireDateEditText.getText().toString().length() > 0
        ) {     //allow to click saveImageButton
            try {
                payment = paymentEditText.getText().toString();
                date = displayDateFormat.parse(dateEditText.getText().toString());
                expireDate = displayDateFormat.parse(expireDateEditText.getText().toString());
                remind = remindCheckBox.isChecked();
            } catch (ParseException e) {
                e.printStackTrace();
            }
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
        StringBuffer sb = new StringBuffer();
        if (day < 10) sb.append("0" + day);
        else sb.append(day);
        sb.append("/");
        month++;
        if (month < 10) sb.append("0" + month);
        else sb.append(month);
        sb.append("/" + year);
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

    private void getRecordIdentifier(@Nullable final recordIdentifierCallback callbacks) {

        String URL = IP_HOST + GET_REGISTRATION_RECORD_IDENTIFIER + vehicle.getRegistration_no() + "/" + currentDate;

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

        Volley.newRequestQueue(RegistrationReminderActivity.this).add(objectRequest);
    }

    public interface recordIdentifierCallback {
        void onSuccess(@NonNull String returnedIdentifier, String returnedRecord_id);

//        void onError(@NonNull String errorMessage);
    }

    private void uploadServiceRecord() {

        String isRemind = "";

        if (remind) isRemind += "1";
        else isRemind += "0";

        String finalIsRemind = isRemind;
        Thread thread = new Thread(() -> {

            HashMap<String, String> params = new HashMap<>();//xuzheng
            File file = null;
            String message = null;
            String encrypt_hash = null;
            String s3_temp_path = null;

            DateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

            try {
                params.put("registration_record_identifier", serviceIDTextView.getText().toString().substring(4));
                Log.e("identifier in request", serviceIDTextView.getText().toString().substring(4));

                params.put("vehicle_id", vehicleID);
                params.put("payment_no", payment);
                params.put("registration_date", format.format(date));
                params.put("expires_in", String.valueOf(durationMonth));
                params.put("expiry_date", format.format(expireDate));
                params.put("registration_reminder", finalIsRemind);
                params.put("record_id", recordIDTextView.getText().toString());
                Log.e("recordID in request", recordIDTextView.getText().toString());
/*
                if (!((BitmapDrawable) qrImageView.getDrawable()).getBitmap().sameAs(qrCodeBitmap)) {
                    Bitmap toBeUploaded = ((BitmapDrawable) qrImageView.getDrawable()).getBitmap();

                    String root = Environment.getExternalStorageDirectory().toString();
                    File myDir = new File(root + "/saved_images");
                    myDir.mkdirs();

                    String fname = "registration.png";
                    file = new File(myDir, fname);
                    if (file.exists()) file.delete();
                    file.createNewFile();
                    BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
                    toBeUploaded.compress(Bitmap.CompressFormat.PNG, 100, bos);

                    bos.flush();
                    bos.close();
                }
*/
                file = mPhotoHelper.getCropFilePath()==null? null : new File(mPhotoHelper.getCropFilePath());
                String response = HttpUtil.uploadForm(params, "document", file, "record.png", IP_HOST + ADD_REGISTRATION_RECORD);

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
                    invokeBlockchain(serviceIDTextView.getText().toString().substring(4),
                            payment,
                            String.valueOf(durationMonth),
                            format.format(date),
                            format.format(expireDate),
                            encrypt_hash,
                            s3_temp_path);
                }

                runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(RegistrationReminderActivity.this, "success", Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(RegistrationReminderActivity.this, ManageVehicleActivity.class);
                        intent.putExtra("vehicleID", vehicleID);
                        startActivity(intent);
                    }
                });
            }

        });
        thread.start();
    }

    private void invokeBlockchain(String identifier, String payment_no, String expires_in, String registration_date, String expiry_date, String ecrypt_hash, String registration_file_location) {

        String URL = BLOCKCHAIN_IP + INVOKE_BLOCKCHAIN;

        final JSONObject jsonParam = new JSONObject();
        try {
            jsonParam.put("identifier", identifier);
            jsonParam.put("record_type", "registration");
            jsonParam.put("payment_no", payment_no);
            jsonParam.put("expires_in", expires_in);
            jsonParam.put("registration_date", registration_date);
            jsonParam.put("expiry_date", expiry_date);
            jsonParam.put("ecrypt_hash", ecrypt_hash);
            jsonParam.put("registration_file_location", registration_file_location);

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
        Volley.newRequestQueue(RegistrationReminderActivity.this).add(objectRequest);

    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, ManageVehicleActivity.class).putExtra("vehicleID", vehicleID));
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK) {
            startActivity(new Intent(this, ManageVehicleActivity.class).putExtra("vehicleID", vehicleID));
            return true;    //stop calling super method
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }

}
