package com.wisecarCompany.wisecarapp.user.vehicle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.wisecarCompany.wisecarapp.R;
import com.wisecarCompany.wisecarapp.user.UserInfo;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.TreeMap;

import cn.bingoogolapple.baseadapter.BGABaseAdapterUtil;
import cn.bingoogolapple.photopicker.imageloader.BGAImage;
import cn.bingoogolapple.photopicker.util.BGAPhotoHelper;
import cn.bingoogolapple.photopicker.util.BGAPhotoPickerUtil;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class AddVehicleActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {

    private static final String TAG = "addVehicle";

    private final String IP_HOST = "http://54.206.19.123:3000";
    private final String ADD_VEHICLE = "/api/v1/vehicles/";

    private ImageView vehicleImageView;
    private Uri vehicleImageUri;
    private Bitmap vehicleImageBitmap;
    private Drawable vehicleDrawable;
    private byte[] vehicleImgByte;

    private EditText rcEditText;
    private String registration_no;
    private EditText makeEditText;
    private String make;
    private EditText modelEditText;
    private String model;
    private EditText descriptionEditText;
    private String description;

    private String year = "2020";
    private String state = "1";

    private CheckBox[] servicesCheckBox;
    private boolean[] isServices;
    /*
    private CheckBox serviceCheckBox;
    private boolean services;
    private CheckBox registrationCheckBox;
    private boolean registration;
    private CheckBox driverCheckBox;
    private boolean driver;
    private CheckBox parkingCheckBox;
    private boolean parking;
    private CheckBox insuranceCheckBox;
    private boolean insurance;
    private CheckBox tollCheckBox;
    private boolean toll;
    private CheckBox fuelCheckBox;
    private boolean fuel;
     */
    private String servicesChoice = "";

    private ImageButton backImageButton;
    private Button uploadButton;
    private ImageButton saveImageButton;

    private static final int REQUEST_CODE_PERMISSION_CHOOSE_PHOTO = 1;
    private static final int REQUEST_CODE_PERMISSION_TAKE_PHOTO = 2;

    private static final int REQUEST_CODE_CHOOSE_PHOTO = 1;
    private static final int REQUEST_CODE_TAKE_PHOTO = 2;
    private static final int REQUEST_CODE_CROP = 3;

    private BGAPhotoHelper mPhotoHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_vehicle);

        vehicleImageView = $(R.id.vehicleImageView);
        uploadButton = $(R.id.uploadButton);
        rcEditText = $(R.id.rcEditText);
        makeEditText = $(R.id.makeEditText);
        modelEditText = $(R.id.modelEditText);
        descriptionEditText = $(R.id.descriptionEditText);
        servicesCheckBox = new CheckBox[] {
            $(R.id.serviceCheckBox),
            $(R.id.registrationCheckBox),
            $(R.id.driverCheckBox),
            $(R.id.parkingCheckBox),
            $(R.id.insuranceCheckBox),
            //$(R.id.tollCheckBox),
            $(R.id.fuelCheckBox)
        };
        saveImageButton = $(R.id.saveImageButton);

        backImageButton = $(R.id.backImageButton);
        backImageButton.setOnClickListener(v -> startActivity(new Intent(AddVehicleActivity.this, VehicleActivity.class)));

        // The directory for storing photos after taking a photo.
        // Change to the directory where you want to store the photos after you take them.
        // If this parameter is not passed, there is no camera function
        File takePhotoDir = new File(Environment.getExternalStorageDirectory(), "BGAPhotoPickerTakePhoto");
        mPhotoHelper = new BGAPhotoHelper(takePhotoDir);

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

        saveImageButton.setOnClickListener(v -> {
            if(saveImageButton.getAlpha()<1) return;
            Toast.makeText(getApplicationContext(), "Saving, Please Wait...", Toast.LENGTH_LONG).show();

            vehicleDrawable = vehicleImageView.getDrawable();

            BitmapDrawable bitmapDrawable = (BitmapDrawable) vehicleDrawable;
            vehicleImageBitmap = bitmapDrawable.getBitmap();

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            vehicleImageBitmap.compress(Bitmap.CompressFormat.PNG, 100, bos);
            vehicleImgByte = bos.toByteArray();

            make = makeEditText.getText().toString();
            model = modelEditText.getText().toString();
            registration_no = rcEditText.getText().toString();
            description = descriptionEditText.getText().toString();

            if(isServices == null) {
                isServices = new boolean[6];
                for (int i=0; i<isServices.length; i++){
                    isServices[i] = servicesCheckBox[i].isChecked();
                }
            }

            boolean tempB = false;
            for(boolean b: isServices) tempB = tempB || b;
            if(!tempB) {
                Toast.makeText(AddVehicleActivity.this, "please select at least one service", Toast.LENGTH_SHORT).show();
                return;
            }


            Log.d(TAG, "--------------------Add Vehicle------------------");
            Log.d(TAG, "userID: " + UserInfo.getUserID());
            Log.d(TAG, "rc: " + registration_no);
            Log.d(TAG, "make: " + make);
            Log.d(TAG, "model: " + model);
            Log.d(TAG, "description: " + description);
            Log.d(TAG, "service: " + isServices[0]);
            Log.d(TAG, "registration: " + isServices[1]);
            Log.d(TAG, "driver: " + isServices[2]);
            Log.d(TAG, "parking: " + isServices[3]);
            Log.d(TAG, "insurance: " + isServices[4]);
            //Log.d(TAG, "toll: " + isServices[5]);
            Log.d(TAG, "fuel: " + isServices[5]);

            if (UserInfo.getVehicles() == null)
                UserInfo.setVehicles(new TreeMap<>((o1, o2) -> o2.compareTo(o1)));
            if (UserInfo.getVehicles().containsKey("a")) {   //last new added vehicle has not synchronized. should not happen logically
                Toast.makeText(AddVehicleActivity.this, "failed to add vehicle", Toast.LENGTH_SHORT).show();
            } else {

                // Write database connection here
                uploadVehicleInfoByHttpClient();

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
                BGAImage.display(vehicleImageView, R.drawable.profile0empty_image, mPhotoHelper.getCropFilePath(), BGABaseAdapterUtil.dp2px(200));
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


    public boolean dispatchTouchEvent(MotionEvent ev) {
        View v = getCurrentFocus();
        if (isShouldHideInput(v, ev)) {
            hideSoftInput(v.getWindowToken());
            registration_no = rcEditText.getText().toString();
            make = makeEditText.getText().toString();
            model = modelEditText.getText().toString();
            description = descriptionEditText.getText().toString();
            if (registration_no != null && make != null && model != null && description != null
                    && registration_no.length() > 0 && make.length() > 0 && model.length() > 0 && description.length() > 0
            ) {
                if(isServices == null) isServices = new boolean[6];
                for (int i=0; i<isServices.length; i++){
                    isServices[i] = servicesCheckBox[i].isChecked();
                }
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

    private void uploadVehicleInfoByHttpClient() {

        final String[] vehicle_id = {""};

        for(int i=0; i<isServices.length; i++) {
            if(isServices[i]) servicesChoice += i+1;
        }
        Log.d(TAG, "uploadVehicleInfoByHttpClient: servicesChoice: " + servicesChoice);

        Thread thread = new Thread(() -> {
            HttpClient httpClient = new DefaultHttpClient();
            HttpPost postRequest = new HttpPost(IP_HOST + ADD_VEHICLE);

            MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);


            try {
                reqEntity.addPart("make", new StringBody(make));
                reqEntity.addPart("model", new StringBody(model));
                reqEntity.addPart("registration_no", new StringBody(registration_no));
                reqEntity.addPart("description", new StringBody(description));
                reqEntity.addPart("services", new StringBody(servicesChoice));
                reqEntity.addPart("state", new StringBody(state));
                reqEntity.addPart("year", new StringBody(year));
                reqEntity.addPart("user_id", new StringBody(UserInfo.getUserID()));

                ByteArrayBody vehicleImgBody = new ByteArrayBody(vehicleImgByte, ContentType.IMAGE_PNG, "logo.png");
                reqEntity.addPart("logo", vehicleImgBody);

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
                Log.e("add vehicle response", String.valueOf(response));
                BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
                String sResponse;
                while ((sResponse = reader.readLine()) != null) {
                    s = s.append(sResponse);
                }
                Log.e("response", s.toString());
                if (s.toString().contains("success")) {
                    // Add successfully
                    runOnUiThread(() -> {
                        Toast.makeText(getApplicationContext(), "success", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(AddVehicleActivity.this, VehicleActivity.class));
                    });
                    int position = s.indexOf("vehicle_id");
                    vehicle_id[0] = s.substring(position + 12, s.length() - 1);
                    UserInfo.getVehicles().put("a", new Vehicle(registration_no, make, model, year, state, description, vehicleImageBitmap));
                } else {
                    runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Add vehicle failed. Please check your registration number.", Toast.LENGTH_SHORT).show());
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

            postRequest.abort();
            httpClient.getConnectionManager().shutdown();
        });
        thread.start();
    }

    private <T extends View> T $(int id){
        return (T) findViewById(id);
    }
}
