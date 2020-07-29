package com.wisecarCompany.wisecarapp.function.serviceRecords;

import androidx.appcompat.app.AlertDialog;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
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

import java.io.File;
import java.io.IOException;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.wisecarCompany.wisecarapp.user.vehicle.ManageVehicleActivity;
import com.wisecarCompany.wisecarapp.R;
import com.wisecarCompany.wisecarapp.user.UserInfo;
import com.wisecarCompany.wisecarapp.user.vehicle.Vehicle;

import net.glxn.qrgen.android.QRCode;
import net.glxn.qrgen.core.image.ImageType;

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

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import cn.bingoogolapple.baseadapter.BGABaseAdapterUtil;
import cn.bingoogolapple.photopicker.imageloader.BGAImage;
import cn.bingoogolapple.photopicker.util.BGAPhotoHelper;
import cn.bingoogolapple.photopicker.util.BGAPhotoPickerUtil;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class ServiceRecordsActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {

    private static final String TAG = "Service Records";

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

    private EditText dateTimeEditText;
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
    private final String GET_SERVICE_RECORD_IDENTIFIER = "/api/v1/servicerecords/identifier/";
    private final String scanQRCode = "/api/v1/servicerecords/upload?identifier=";
    private final String BLOCKCHAIN_IP = "http://13.236.209.122:3000";
    private final String INVOKE_BLOCKCHAIN = "/api/v1/servicerecords/blockchaininvoke";

    private String servicesOptions;
    private String currentDate;
    private String identifier;
    private String record_id;
    private Bitmap qrCodeBitmap;

    private static final int REQUEST_CODE_PERMISSION_CHOOSE_PHOTO = 1;
    private static final int REQUEST_CODE_PERMISSION_TAKE_PHOTO = 2;

    private static final int REQUEST_CODE_CHOOSE_PHOTO = 1;
    private static final int REQUEST_CODE_TAKE_PHOTO = 2;
    private static final int REQUEST_CODE_CROP = 3;

    private BGAPhotoHelper mPhotoHelper;

    private SimpleDateFormat displayDateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_records);

        servicesOptions = "";
        currentDate = "";

        backImageButton = $(R.id.backImageButton);
        backImageButton.setOnClickListener(v -> startActivity(new Intent(ServiceRecordsActivity.this, ManageVehicleActivity.class).putExtra("vehicleID", vehicleID)));

        SimpleDateFormat format = new SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault());
        currentDate = format.format(Calendar.getInstance().getTime());

        vehicleID = (String) this.getIntent().getStringExtra("vehicleID");
        Log.d(TAG, "vehicleID: " + vehicleID);
        vehicle = UserInfo.getVehicles().get(vehicleID);
        Log.d(TAG, "vehicle: " + vehicle);

        getRecordIdentifier((returnedIdentifier, returnedRecord_id) -> {

            Log.e("SERVICE identifier", identifier);
            Log.e("SERVICE record_id", record_id);
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

        serviceIDTextView = $(R.id.serviceIDTextView);
        qrImageView = $(R.id.qrImageView);
        uploadButton = $(R.id.uploadButton);
        cameraImageButton = $(R.id.cameraImageButton);
        recordIDTextView = $(R.id.recordIDTextView);
        resetButton = $(R.id.resetButton);

        resetButton.setOnClickListener(v -> qrImageView.setImageBitmap(qrCodeBitmap));

        uploadButton.setOnClickListener(v -> {
            Log.d(TAG, "upload record: ");
            //not implemented
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

        dateTimeEditText = $(R.id.dateTimeEditText);
        centreEditText = $(R.id.centreEditText);
        refNoEditText = $(R.id.refNoEditText);
        notesEditText = $(R.id.notesEditText);
        nextDateEditText = $(R.id.nextDateEditText);
        nextDistanceEditText = $(R.id.nextDistanceEditText);
        oilCheckBox = $(R.id.oilCheckBox);
        brakeCheckBox = $(R.id.brakeCheckBox);
        batteryCheckBox = $(R.id.batteryCheckBox);
        coolingCheckBox = $(R.id.coolingCheckBox);
        lightsCheckBox = $(R.id.lightsCheckBox);

        dateTimeEditText.setInputType(InputType.TYPE_NULL);
        dateTimeEditText.setOnClickListener(v -> {
            dateTimeEditText.setText("");
            date = null;
            Calendar c = Calendar.getInstance();
            new TimePickerDialog(ServiceRecordsActivity.this, (view, hour, minute) -> {
                if (date == null)
                    return; //user should see DatePickerDialog first, and set date first. If user click back / cancel and skip date, time set should not be allowed.
                else {
                    StringBuffer time = new StringBuffer();
                    time.append(", ");
                    time.append(hour >= 10 ? hour : "0" + hour);
                    time.append(":");
                    time.append(minute >= 10 ? minute : "0" + minute);
                    time.append("  ");
                    date = new Date(date.getTime() + (hour * 60 + minute) * 60 * 1000);
                    dateTimeEditText.append(time);
                    Log.d(TAG, "date: " + date);
                    checkReadyToSave();
                }
            }, 0, 0, true).show();
            new DatePickerDialog(ServiceRecordsActivity.this, (view, year, monthOfYear, dayOfMonth) -> {
                date = intToDate(year, monthOfYear, dayOfMonth);
                //SimpleDateFormat format1 = new SimpleDateFormat("ddMMM yyyy", Locale.getDefault());
                String str = displayDateFormat.format(date);
                dateTimeEditText.append(str + "  ");
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
        });
        dateTimeEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                dateTimeEditText.setText("");
                Calendar c = Calendar.getInstance();
                new TimePickerDialog(ServiceRecordsActivity.this, (view, hour, minute) -> {
                    if (date == null)
                        return; //user should see DatePickerDialog first, and set date first. If user click back / cancel and skip date, time set should not be allowed.
                    else {
                        StringBuffer time = new StringBuffer();
                        time.append(hour >= 10 ? hour : "0" + hour);
                        time.append(":");
                        time.append(minute >= 10 ? minute : "0" + minute);
                        time.append("  ");
                        date = new Date(date.getTime() + (hour * 60 + minute) * 60 * 1000);
                        dateTimeEditText.append(time);
                        Log.d(TAG, "date: " + date);
                        checkReadyToSave();
                    }
                }, 0, 0, true).show();
                new DatePickerDialog(ServiceRecordsActivity.this, (view, year, monthOfYear, dayOfMonth) -> {
                    date = intToDate(year, monthOfYear, dayOfMonth);
                    //SimpleDateFormat format12 = new SimpleDateFormat("ddMMM yyyy", Locale.getDefault());
                    String str = displayDateFormat.format(date);
                    dateTimeEditText.append(str + "  ");
                }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        nextDateEditText.setInputType(InputType.TYPE_NULL);
        nextDateEditText.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new DatePickerDialog(ServiceRecordsActivity.this, (view, year, monthOfYear, dayOfMonth) -> {
                nextDate = intToDate(year, monthOfYear, dayOfMonth);
                //SimpleDateFormat format13 = new SimpleDateFormat("ddMMM yyyy", Locale.getDefault());
                String str = displayDateFormat.format(nextDate);
                nextDateEditText.setText(str);
                checkReadyToSave();
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
        });
        nextDateEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                Calendar c = Calendar.getInstance();
                new DatePickerDialog(ServiceRecordsActivity.this, (view, year, monthOfYear, dayOfMonth) -> {
                    nextDate = intToDate(year, monthOfYear, dayOfMonth);
                    //SimpleDateFormat format14 = new SimpleDateFormat("ddMMM yyyy", Locale.getDefault());
                    String str = displayDateFormat.format(nextDate);
                    nextDateEditText.setText(str);
                    checkReadyToSave();
                }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
            }
        });


        saveImageButton = $(R.id.saveImageButton);
        saveImageButton.setOnClickListener(v -> {
            if(saveImageButton.getAlpha()<1) return;
            Toast.makeText(getApplicationContext(), "Saving, Please Wait...", Toast.LENGTH_LONG).show();

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
                if (nextDate.after(new Date())) {

                    // Write INSERTIONG here
                    uploadServiceRecord();

                } else {
                    Toast.makeText(getApplicationContext(), "Please enter correct next service date", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), "Please enter correct next service distance", Toast.LENGTH_SHORT).show();
            }

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
        centre = centreEditText.getText().toString();
        refNo = refNoEditText.getText().toString();
        notes = notesEditText.getText().toString();
        nextDistance = nextDistanceEditText.getText().toString();
        if (date != null && centre != null && refNo != null && notes != null && nextDate != null && nextDistance != null
                && centre.length() > 0 && refNo.length() > 0 && notes.length() > 0 && nextDistance.length() > 0
        ) {     //allow to click saveImageButton
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

    private static java.util.Date intToDate(int year, int month, int day) {
        StringBuffer sb = new StringBuffer();
        if (day < 10) sb.append("0" + day);
        else sb.append(day);
        sb.append("/");
        month++;
        if (month < 10) sb.append("0" + month);
        else sb.append(month);
        sb.append("/" + year);
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        java.util.Date date = null;
        try {
            date = format.parse(sb.toString());
        } catch (ParseException e) {
            return null;
        }
        return date;
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

        Thread thread = new Thread(() -> {
            HttpClient httpClient = new DefaultHttpClient();
            HttpPost postRequest = new HttpPost(IP_HOST + ADD_SERVICE_RECORD);

            MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

            DateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

            try {
                reqEntity.addPart("service_record_identifier", new StringBody(serviceIDTextView.getText().toString().substring(4)));
                Log.e("identifier in request", serviceIDTextView.getText().toString().substring(4));

                reqEntity.addPart("vehicle_id", new StringBody(vehicleID));
                reqEntity.addPart("service_date", new StringBody(format.format(date)));
                reqEntity.addPart("service_center", new StringBody(centre));
                reqEntity.addPart("service_ref", new StringBody(refNo));
                reqEntity.addPart("service_option_ids", new StringBody(servicesOptions));
                reqEntity.addPart("service_notes", new StringBody(notes));
                reqEntity.addPart("next_service_date", new StringBody(format.format(nextDate)));
                reqEntity.addPart("next_service_odometer", new StringBody(nextDistance));

                if (qrImageView.getDrawable() != new BitmapDrawable(getResources(), qrCodeBitmap)) {
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    Bitmap toBeUploaded = ((BitmapDrawable) qrImageView.getDrawable()).getBitmap();
                    toBeUploaded.compress(Bitmap.CompressFormat.PNG, 100, stream);
                    byte[] qrbyteArray = stream.toByteArray();
                    ByteArrayBody recordBody = new ByteArrayBody(qrbyteArray, ContentType.IMAGE_PNG, "record.png");
                    reqEntity.addPart("document", recordBody);
                }
                reqEntity.addPart("record_id", new StringBody(recordIDTextView.getText().toString()));
                Log.e("recordID in request", recordIDTextView.getText().toString());


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

                    if (s.toString().indexOf("s3_temp_path") - s.toString().indexOf("encrypt_hash") > 18) {
                        invokeBlockchain(serviceIDTextView.getText().toString().substring(4),
                                format.format(date),
                                centre,
                                vehicle.getRegistration_no(),
                                s.toString().substring(s.toString().indexOf("encrypt_hash") + 15, s.toString().indexOf("s3_temp_path") - 3),
                                s.toString().substring(s.toString().indexOf("s3_temp_path") + 15, s.toString().length() - 2),
                                servicesOptions);
                    }

                    runOnUiThread(() -> {
                        Toast.makeText(ServiceRecordsActivity.this, "success", Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(ServiceRecordsActivity.this, ManageVehicleActivity.class);
                        intent.putExtra("vehicleID", vehicleID);
                        startActivity(intent);
                    });
                }
                Log.e("response", s.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }

            postRequest.abort();
            httpClient.getConnectionManager().shutdown();

        });
        thread.start();
    }

    private void invokeBlockchain(String identifier, String service_date, String service_center, String vehicle_registration, String encrypt_hash, String s3_temp_path, String service_options) {

        String URL = BLOCKCHAIN_IP + INVOKE_BLOCKCHAIN;

        final JSONObject jsonParam = new JSONObject();
        try {
            jsonParam.put("identifier", identifier);
            jsonParam.put("record_type", "service_record");
            jsonParam.put("service_date", service_date);
            jsonParam.put("service_center", service_center);
            jsonParam.put("vehicle_registration", vehicle_registration);
            jsonParam.put("ecrypt_hash", encrypt_hash);
            jsonParam.put("service_file_location", s3_temp_path);
            jsonParam.put("service_options", service_options);

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
        Volley.newRequestQueue(ServiceRecordsActivity.this).add(objectRequest);

    }

    private void getRecordIdentifier(@Nullable final recordIdentifierCallback callbacks) {

        String URL = IP_HOST + GET_SERVICE_RECORD_IDENTIFIER + vehicle.getRegistration_no() + "/" + currentDate;

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

        Volley.newRequestQueue(ServiceRecordsActivity.this).add(objectRequest);
    }

    public interface recordIdentifierCallback {
        void onSuccess(@NonNull String returnedIdentifier, String returnedRecord_id);

//        void onError(@NonNull String errorMessage);
    }

    private <T extends View> T $(int id) {
        return (T) findViewById(id);
    }

}
