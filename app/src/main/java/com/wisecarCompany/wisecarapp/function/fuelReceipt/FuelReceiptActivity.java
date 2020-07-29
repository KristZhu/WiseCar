package com.wisecarCompany.wisecarapp.function.fuelReceipt;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.wisecarCompany.wisecarapp.function.HttpUtil;
import com.wisecarCompany.wisecarapp.user.vehicle.ManageVehicleActivity;
import com.wisecarCompany.wisecarapp.R;
import com.wisecarCompany.wisecarapp.user.vehicle.VehicleActivity;
import com.wisecarCompany.wisecarapp.viewElement.CircleImageView;
import com.wisecarCompany.wisecarapp.user.UserInfo;
import com.wisecarCompany.wisecarapp.user.vehicle.Vehicle;

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

public class FuelReceiptActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {

    private final static String TAG = "Fuel Receipt";

    private Vehicle vehicle;
    private String vehicleID;

    private ImageButton backImageButton;

    private CircleImageView fuelImageView;
    private Uri fuelImageUri;
    private Bitmap fuelImageBitmap;
    private Drawable fuelImageDrawable;

    private TextView idTextView;
    private Button uploadButton;

    private String reference;
    private Date date;
    private String type;
    private double fuelAmount;
    private double paidAmount;
    private boolean claimable;

    private EditText referenceEditText;
    private EditText dateEditText;
    private EditText typeEditText;
    private EditText fuelAmountEditText;
    private EditText paidAmountEditText;
    private CheckBox claimableCheckBox;
    private TextView identifierTextView;
    private TextView sharedTextView;

    private ImageButton saveImageButton;

    private static final int REQUEST_CODE_PERMISSION_CHOOSE_PHOTO = 1;
    private static final int REQUEST_CODE_PERMISSION_TAKE_PHOTO = 2;

    private static final int REQUEST_CODE_CHOOSE_PHOTO = 1;
    private static final int REQUEST_CODE_TAKE_PHOTO = 2;
    private static final int REQUEST_CODE_CROP = 3;

    private BGAPhotoHelper mPhotoHelper;

    private String identifier;
    private String record_id;

    private String shared_company_id = "";

    private final String IP_HOST = "http://54.206.19.123:3000";
    private final String GET_FUEL_IDENTIFIER = "/api/v1/fuelreceipts/identifier/";
    private final String GET_IF_SHARED = "/api/v1/fuelreceipts/checkcurrentshare";
    private final String ADD_FUEL = "/api/v1/fuelreceipts/";
    private final String BLOCKCHAIN_IP = "http://13.236.209.122:3000";
    private final String INVOKE_BLOCKCHAIN = "/api/v1/fuelreceipt/blockchaininvoke";

    private SimpleDateFormat displayDateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fuel_receipt);

        backImageButton = $(R.id.backImageButton);
        backImageButton.setOnClickListener(v -> startActivity(new Intent(FuelReceiptActivity.this, VehicleActivity.class)));

        idTextView = $(R.id.idTextView);
        fuelImageView = $(R.id.fuelImageView);

        identifierTextView = $(R.id.recordIDTextView);
        sharedTextView = $(R.id.sharedTextView);

        vehicleID = (String) this.getIntent().getStringExtra("vehicleID");
        Log.d(TAG, "vehicleID: " + vehicleID);
        vehicle = UserInfo.getVehicles().get(vehicleID);
        Log.d(TAG, "vehicle: " + vehicle);

        getIdentifier((returnedIdentifier, returnedRecord_id) -> {

            Log.e("fuel identifier", identifier);
            Log.e("fuel record_id", record_id);
//                identifier = returnedIdentifier;
//                record_id = returnedRecord_id;

            String idToBeShown = "ID: " + identifier;

            idTextView.setText(idToBeShown);
            identifierTextView.setText(returnedRecord_id);
        });

        checkIfShared((returnedShared) -> {

            Log.e("companyID", shared_company_id);
            sharedTextView.setText(returnedShared);
        });


        // The directory for storing photos after taking a photo.
        // Change to the directory where you want to store the photos after you take them.
        // If this parameter is not passed, there is no camera function
        File takePhotoDir = new File(Environment.getExternalStorageDirectory(), "BGAPhotoPickerTakePhoto");
        mPhotoHelper = new BGAPhotoHelper(takePhotoDir);

        uploadButton = $(R.id.uploadButton);
        uploadButton.setOnClickListener(v -> {
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

        referenceEditText = $(R.id.referenceEditText);
        dateEditText = $(R.id.dateEditText);
        typeEditText = $(R.id.typeEditText);
        fuelAmountEditText = $(R.id.fuelAmountEditText);
        paidAmountEditText = $(R.id.paidAmountEditText);
        claimableCheckBox = $(R.id.claimableCheckBox);

        //SimpleDateFormat format = new SimpleDateFormat("ddMMM yyyy", Locale.getDefault());
        dateEditText.setInputType(InputType.TYPE_NULL);
        dateEditText.setText(displayDateFormat.format(new Date()));
        dateEditText.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new DatePickerDialog(FuelReceiptActivity.this, (view, year, monthOfYear, dayOfMonth) -> {
                date = intToDate(year, monthOfYear, dayOfMonth);
                String str = displayDateFormat.format(date);
                dateEditText.setText(str);
                checkReadyToSave();
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
        });
        dateEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                Calendar c = Calendar.getInstance();
                new DatePickerDialog(FuelReceiptActivity.this, (view, year, monthOfYear, dayOfMonth) -> {
                    date = intToDate(year, monthOfYear, dayOfMonth);
                    String str = displayDateFormat.format(date);
                    dateEditText.setText(str);
                    checkReadyToSave();
                }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        typeEditText.setInputType(InputType.TYPE_NULL);
        typeEditText.setOnClickListener(v -> {
            final String[] types = new String[]{"Unleaded Petrol (ULP)", "Diesel", "Liquefied Petroleum Gas (LPG)", "Electric Vehicle Charge"};
            AlertDialog alertDialog = new AlertDialog.Builder(FuelReceiptActivity.this)
                    //.setTitle("select a cover type")
                    .setIcon(R.mipmap.ic_launcher)
                    .setItems(types, (dialogInterface, i) -> {
                        typeEditText.setText(types[i]);
                        checkReadyToSave();
                    })
                    .create();
            alertDialog.show();
        });
        typeEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                final String[] types = new String[]{"Unleaded Petrol (ULP)", "Diesel", "Liquefied Petroleum Gas (LPG)", "Electric Vehicle Charge"};
                AlertDialog alertDialog = new AlertDialog.Builder(FuelReceiptActivity.this)
                        //.setTitle("select a cover type")
                        .setIcon(R.mipmap.ic_launcher)
                        .setItems(types, (dialogInterface, i) -> {
                            typeEditText.setText(types[i]);
                            checkReadyToSave();
                        })
                        .create();
                alertDialog.show();
            }
        });

        saveImageButton = $(R.id.saveImageButton);
        saveImageButton.setOnClickListener(v -> {
            if (saveImageButton.getAlpha() < 1) return;
            Toast.makeText(getApplicationContext(), "Saving, Please Wait...", Toast.LENGTH_LONG).show();
            //parkingImageDrawable = licenceImageView.getDrawable();
            //...

            Log.d(TAG, "reference: " + reference);
            Log.d(TAG, "date: " + date);
            Log.d(TAG, "type: " + type);
            Log.d(TAG, "fuelAmount: " + fuelAmount);
            Log.d(TAG, "paidAmount: " + paidAmount);
            Log.d(TAG, "claimable: " + claimable);


            //db
            if (claimable && sharedTextView.getText().toString().equals("")) {
                Toast.makeText(getApplicationContext(), "This vehicle is currently not shared with any company, please uncheck Claimable.", Toast.LENGTH_SHORT).show();
            } else {
                uploadFuelReceipt();
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
                BGAImage.display(fuelImageView, R.drawable.profile0empty_image, mPhotoHelper.getCropFilePath(), BGABaseAdapterUtil.dp2px(200));
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

    private void checkReadyToSave() {
        if (referenceEditText.getText().toString().length() > 0
                && dateEditText.getText().toString().length() > 0
                && typeEditText.getText().toString().length() > 0
                && fuelAmountEditText.getText().toString().length() > 0
                && paidAmountEditText.getText().toString().length() > 0) {
            try {
                date = displayDateFormat.parse(dateEditText.getText().toString());
            } catch (ParseException e) {
                e.printStackTrace();
            }
            try {
                fuelAmount = Double.parseDouble(fuelAmountEditText.getText().toString());
            } catch (NumberFormatException e) {
                Toast.makeText(getApplicationContext(), "Please enter correct fuel amount", Toast.LENGTH_SHORT).show();
                saveImageButton.setAlpha(0.5f);
                saveImageButton.setClickable(false);
                return;
            }
            try {
                paidAmount = Double.parseDouble(paidAmountEditText.getText().toString());
            } catch (NumberFormatException e) {
                Toast.makeText(getApplicationContext(), "Please enter correct paid amount", Toast.LENGTH_SHORT).show();
                saveImageButton.setAlpha(0.5f);
                saveImageButton.setClickable(false);
                return;
            }
            reference = referenceEditText.getText().toString();
            type = typeEditText.getText().toString();
            claimable = claimableCheckBox.isChecked();
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

        DateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        String URL = IP_HOST + GET_FUEL_IDENTIFIER + vehicle.getRegistration_no() + "/" + format.format(new Date());

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

        Volley.newRequestQueue(FuelReceiptActivity.this).add(objectRequest);
    }

    public interface recordIdentifierCallback {
        void onSuccess(@NonNull String returnedIdentifier, String returnedRecord_id);

//        void onError(@NonNull String errorMessage);
    }

    private void checkIfShared(@Nullable final sharedCallback callbacks) {

        DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

        String URL = IP_HOST + GET_IF_SHARED;

        final JSONObject jsonParam = new JSONObject();
        try {
            jsonParam.put("vehicle_id", vehicle.getVehicle_id());
            jsonParam.put("current_date_time", format.format(new Date()));

        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest objectRequest = new JsonObjectRequest(Request.Method.POST, URL, jsonParam, response -> {
            Log.e("Response: ", response.toString());
            JSONObject jsonObject = response;

            if (jsonObject.optString("message").equals("success"))
                shared_company_id = jsonObject.optString("cust_id");

            if (callbacks != null)
                callbacks.onSuccess(shared_company_id);

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

        Volley.newRequestQueue(FuelReceiptActivity.this).add(objectRequest);
    }

    public interface sharedCallback {
        void onSuccess(@NonNull String returnedCustID);

//        void onError(@NonNull String errorMessage);
    }

    private void uploadFuelReceipt() {

        String isClaim = "";

        if (claimable) {
            isClaim = "1";
        } else {
            isClaim += "0";
        }

        String finalIsClaim = isClaim;
        Thread thread = new Thread(() -> {

            DateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

            HashMap<String, String> params = new HashMap<>();
            File file = null;
            String message = null;
            String encrypt_hash = null;
            String s3_temp_path = null;

            try {
                params.put("fuel_receipt_identifier", idTextView.getText().toString().substring(4));
                Log.e("identifier in request", idTextView.getText().toString().substring(4));

                params.put("vehicle_id", vehicle.getVehicle_id());
                params.put("invoice_reference", reference);
                params.put("fuel_date", format.format(date));
                params.put("fuel_type", type);
                params.put("fuel_amount", String.valueOf(fuelAmount));
                params.put("paid_amount", String.valueOf(paidAmount));
                params.put("claimable", finalIsClaim);
                params.put("record_id", identifierTextView.getText().toString());
                Log.e("recordID", identifierTextView.getText().toString());
                params.put("shared_company_id", sharedTextView.getText().toString());

                Log.e("ImageView null check",
                        String.valueOf(!((BitmapDrawable) fuelImageView.getDrawable()).getBitmap()
                                .sameAs(((BitmapDrawable) ResourcesCompat.getDrawable(getResources(), R.drawable.licence0camera, null)).getBitmap())));

                if (!((BitmapDrawable) fuelImageView.getDrawable()).getBitmap()
                        .sameAs(((BitmapDrawable) ResourcesCompat.getDrawable(getResources(), R.drawable.licence0camera, null)).getBitmap())) {
                    Bitmap toBeUploaded = ((BitmapDrawable) fuelImageView.getDrawable()).getBitmap();

                    String root = Environment.getExternalStorageDirectory().toString();
                    File myDir = new File(root + "/saved_images");
                    myDir.mkdirs();

                    String fname = "fuel.png";
                    file = new File(myDir, fname);
                    if (file.exists()) file.delete();
                    file.createNewFile();
                    BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
                    toBeUploaded.compress(Bitmap.CompressFormat.PNG, 100, bos);

                    bos.flush();
                    bos.close();
                }

                String response = HttpUtil.uploadForm(params, "document", file, "record.png", IP_HOST + ADD_FUEL);

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
                            reference,
                            type,
                            format.format(date),
                            String.valueOf(fuelAmount),
                            String.valueOf(paidAmount),
                            encrypt_hash,
                            s3_temp_path);
                }

                runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(FuelReceiptActivity.this, "success", Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(FuelReceiptActivity.this, ManageVehicleActivity.class);
                        intent.putExtra("vehicleID", vehicleID);
                        startActivity(intent);
                    }
                });
            }

        });
        thread.start();
    }

    private void invokeBlockchain(String identifier, String invoice_reference, String fuel_type, String fuel_date, String fuel_amount, String paid_amount, String ecrypt_hash, String fuel_file_location) {

        String URL = BLOCKCHAIN_IP + INVOKE_BLOCKCHAIN;

        final JSONObject jsonParam = new JSONObject();
        try {
            jsonParam.put("identifier", identifier);
            jsonParam.put("record_type", "fuel");
            jsonParam.put("invoice_reference", invoice_reference);
            jsonParam.put("fuel_type", fuel_type);
            jsonParam.put("fuel_date", fuel_date);
            jsonParam.put("fuel_amount", fuel_amount);
            jsonParam.put("paid_amount", paid_amount);
            jsonParam.put("ecrypt_hash", ecrypt_hash);
            jsonParam.put("fuel_file_location", fuel_file_location);

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
        Volley.newRequestQueue(FuelReceiptActivity.this).add(objectRequest);

    }
}
