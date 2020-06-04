package com.example.wisecarapp;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcelable;
import android.os.StrictMode;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
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

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

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
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class AddVehicleActivity extends AppCompatActivity {

    private static final String TAG = "addVehicle";

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

    private final String IP_HOST = "http://54.206.19.123:3000";
    private final String ADD_VEHICLE = "/api/v1/vehicles/";

    private String servicesChoice = "";
    List<Integer> servicesList = new ArrayList<>();

    private ImageButton backImageButton;
    private Button uploadButton;
    private ImageButton saveImageButton;

    private static final int TAKE_PHOTO = 0;
    private static final int CHOOSE_PHOTO = 1;
    private static final int GROP_PHOTO = 2;
    private static final int MULTI_PERMISSION_CODE = 0;
    private static final int PERMISSION_EXTERNAL_STORAGE_REQUEST_CODE = 1;
    private static final int PERMISSION_CAMERA_REQUEST_CODE = 2;


    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult: Length: " + grantResults.length);
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 0: //permit both camera and storage
                Log.d(TAG, "onRequestPermissionsResult: MULTI?");
                if (grantResults[0] == 0 && grantResults[1] == 0 && grantResults[2] == 0) {
                    beforeStartCamera();
                } else {
                    Toast.makeText(getApplicationContext(), "You cannot take a photo without authorization", Toast.LENGTH_SHORT).show();
                }
                break;
            case 1: //permit storage
                Log.d(TAG, "onRequestPermissionsResult: STORAGE?");
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    beforeStartStorage();
                } else {
                    Toast.makeText(getApplicationContext(), "You cannot upload the image without authorization", Toast.LENGTH_SHORT).show();
                }
                break;
            case 2: //permit camera
                Log.d(TAG, "onRequestPermissionsResult: CAMERA?");
                if (grantResults[0] == 0) {
                    beforeStartCamera();
                } else {
                    Toast.makeText(getApplicationContext(), "You cannot take a photo without authorization", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_vehicle);

        vehicleImageView = (ImageView) findViewById(R.id.vehicleImageView);
        uploadButton = (Button) findViewById(R.id.uploadButton);
        rcEditText = (EditText) findViewById(R.id.rcEditText);
        makeEditText = (EditText) findViewById(R.id.makeEditText);
        modelEditText = (EditText) findViewById(R.id.modelEditText);
        descriptionEditText = (EditText) findViewById(R.id.descriptionEditText);
        serviceCheckBox = (CheckBox) findViewById(R.id.serviceCheckBox);
        registrationCheckBox = (CheckBox) findViewById(R.id.registrationCheckBox);
        driverCheckBox = (CheckBox) findViewById(R.id.driverCheckBox);
        parkingCheckBox = (CheckBox) findViewById(R.id.parkingCheckBox);
        insuranceCheckBox = (CheckBox) findViewById(R.id.insuranceCheckBox);
        tollCheckBox = (CheckBox) findViewById(R.id.tollCheckBox);
        fuelCheckBox = (CheckBox) findViewById(R.id.fuelCheckBox);
        saveImageButton = (ImageButton) findViewById(R.id.saveImageButton);

        backImageButton = (ImageButton) findViewById(R.id.backImageButton);
        backImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AddVehicleActivity.this, VehicleActivity.class));
            }
        });

        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String[] ways = new String[]{"Take a photo", "Upload from phone", "Cancel"};
                AlertDialog alertDialog = new AlertDialog.Builder(AddVehicleActivity.this)
                        .setTitle("How to upload? ")
                        .setIcon(R.mipmap.ic_launcher)
                        .setItems(ways, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Log.d(TAG, "onClick: " + ways[i]);
                                if (i == 0) {  //take photo
                                    int permissionCheckCamera = ContextCompat.checkSelfPermission(AddVehicleActivity.this, Manifest.permission.CAMERA);
                                    int permissionCheckStorage = ContextCompat.checkSelfPermission(AddVehicleActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
                                    Log.d(TAG, "onClickPermissionCheckCamera: " + permissionCheckCamera);
                                    Log.d(TAG, "onClickPermissionCheckStorage: " + permissionCheckStorage);

                                    // solve android 7.0 problem
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                                        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
                                        StrictMode.setVmPolicy(builder.build());
                                        builder.detectFileUriExposure();
                                    }

                                    if (permissionCheckCamera == PackageManager.PERMISSION_DENIED && permissionCheckStorage == PackageManager.PERMISSION_DENIED) {
                                        Log.d(TAG, "onClickPermissionRequestCamera&Storage: ");
                                        ActivityCompat.requestPermissions(
                                                AddVehicleActivity.this,
                                                new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                                MULTI_PERMISSION_CODE
                                        );
                                    } else if (permissionCheckCamera == PackageManager.PERMISSION_DENIED) {
                                        Log.d(TAG, "onClickPermissionRequestCamera: ");
                                        ActivityCompat.requestPermissions(
                                                AddVehicleActivity.this,
                                                new String[]{Manifest.permission.CAMERA},
                                                PERMISSION_CAMERA_REQUEST_CODE
                                        );
                                    } else if (permissionCheckStorage == PackageManager.PERMISSION_DENIED) {
                                        Log.d(TAG, "onClickPermissionRequestStorage: ");
                                        ActivityCompat.requestPermissions(
                                                AddVehicleActivity.this,
                                                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                                PERMISSION_EXTERNAL_STORAGE_REQUEST_CODE
                                        );
                                    } else {
                                        beforeStartCamera();
                                    }
                                } else if (i == 1) {   //upload from phone
                                    if (ContextCompat.checkSelfPermission(AddVehicleActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                                        Log.d(TAG, "onClickPermissionRequestStorage: ");
                                        ActivityCompat.requestPermissions(
                                                AddVehicleActivity.this,
                                                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                                PERMISSION_EXTERNAL_STORAGE_REQUEST_CODE
                                        );
                                    } else {
                                        beforeStartStorage();
                                    }
                                } else {
                                    //cancel
                                }
                            }
                        }).create();
                alertDialog.show();
            }
        });

        saveImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

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


                Log.d(TAG, "--------------------Add Vehicle------------------");
                Log.d(TAG, "userID: " + UserInfo.getUserID());
                Log.d(TAG, "rc: " + registration_no);
                Log.d(TAG, "make: " + make);
                Log.d(TAG, "model: " + model);
                Log.d(TAG, "description: " + description);
                Log.d(TAG, "service: " + services);
                Log.d(TAG, "registration: " + registration);
                Log.d(TAG, "driver: " + driver);
                Log.d(TAG, "parking: " + parking);
                Log.d(TAG, "insurance: " + insurance);
                Log.d(TAG, "toll: " + toll);
                Log.d(TAG, "fuel: " + fuel);

                // Write database connection here
                uploadVehicleInfoByHttpClient();

                startActivity(new Intent(AddVehicleActivity.this, VehicleActivity.class));
            }
        });

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case TAKE_PHOTO:
                if (resultCode == RESULT_OK) {
                    Intent intent = new Intent("com.android.camera.action.CROP");
                    intent.setDataAndType(vehicleImageUri, "image/**");
                    intent.putExtra("scale", true);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, vehicleImageUri);
                    startActivityForResult(intent, GROP_PHOTO);
                }
                break;

            case GROP_PHOTO: //剪裁程序 似乎有bug 不确定是不是由于模拟器！
                if (resultCode == RESULT_OK) {
                    try {
                        Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(vehicleImageUri));
                        vehicleImageView.setImageBitmap(bitmap);
                        vehicleImageBitmap = bitmap;
                        //show
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
                break;

            case CHOOSE_PHOTO:
                if (resultCode == RESULT_OK) {
                    if (Build.VERSION.SDK_INT >= 19) {  //4.4 or above
                        handleImageOnKitKat(data);
                    } else {    //below 4.4
                        handleImageBeforeKitKat(data);
                    }
                }
                data.putExtra("scale", true);
                data.putExtra(MediaStore.EXTRA_OUTPUT, vehicleImageUri);
                startActivityForResult(data, GROP_PHOTO);
                break;

            default:
                break;
        }

    }

    private void beforeStartCamera() {
        //create a file object to store picture
        File outputImage = new File(getExternalCacheDir(), System.currentTimeMillis() + ".jpg");
        try {
            if (outputImage.exists()) {
                outputImage.delete();
            }
            outputImage.createNewFile();
            Log.d(TAG, "outputImage.createNewFile success ");
        } catch (IOException e) {
            e.printStackTrace();
        }
        vehicleImageUri = Uri.fromFile(outputImage);
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        intent.putExtra(MediaStore.EXTRA_OUTPUT, vehicleImageUri);
        startActivityForResult(intent, TAKE_PHOTO);
        //active camera
    }


    private void beforeStartStorage() {
        File outputImage = new File(getExternalCacheDir(), "output_image.jpg");
        try {
            if (outputImage.exists()) {
                outputImage.delete();
            }
            outputImage.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        vehicleImageUri = Uri.fromFile(outputImage);
        Intent intent = new Intent("android.intent.action.GET_CONTENT");
        intent.setType("image/*");
        intent.putExtra("crop", true);
        intent.putExtra("scale", true);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, vehicleImageUri);

        startActivityForResult(intent, CHOOSE_PHOTO);
    }

    private void handleImageBeforeKitKat(Intent data) {
        Uri uri = data.getData();
        String imagePath = getImagePath(uri, null);
        displayImage(imagePath);
    }

    @TargetApi(19)
    private void handleImageOnKitKat(Intent data) {
        String imagePath = null;
        Uri uri = data.getData();
        if (DocumentsContract.isDocumentUri(this, uri)) {
            //document type Uri
            String docId = DocumentsContract.getDocumentId(uri);
            if ("com.android.providers.media.documents".equals(uri.getAuthority())) {
                String id = docId.split(":")[1];
                String seletion = MediaStore.Images.Media._ID + "=" + id;
                imagePath = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, seletion);
            } else if ("com.android.providers.downloads.documents".equals(uri.getAuthority())) {
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(docId));
                imagePath = getImagePath(contentUri, null);
            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
            //content type Uri
            imagePath = getImagePath(uri, null);
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            //file type Uri
            imagePath = uri.getPath();
        }
        displayImage(imagePath);
    }

    private String getImagePath(Uri uri, String selection) {
        String path = null;
        //get real path
        Cursor cursor = getContentResolver().query(uri, null, selection, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }
        return path;
    }

    private void displayImage(String imagePath) {
        if (imagePath != null) {
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
            vehicleImageView.setImageBitmap(bitmap);
            vehicleImageBitmap = bitmap;
        } else {
            Toast.makeText(this, "failed to get image", Toast.LENGTH_SHORT).show();
        }
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
                services = serviceCheckBox.isChecked();
                registration = registrationCheckBox.isChecked();
                driver = driverCheckBox.isChecked();
                parking = parkingCheckBox.isChecked();
                insurance = insuranceCheckBox.isChecked();
                toll = tollCheckBox.isChecked();
                fuel = fuelCheckBox.isChecked();
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
        if(v != null && (v instanceof EditText)) {
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
            manager.hideSoftInputFromWindow(token, InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    private void uploadVehicleInfoByHttpClient() {

        final String[] vehicle_id = {""};

        if (services) {
            servicesChoice += "1";
            servicesList.add(1);
        }
        if (registration) {
            servicesChoice += "2";
            servicesList.add(2);
        }
        if (driver) {
            servicesChoice += "3";
            servicesList.add(3);
        }
        if (parking) {
            servicesChoice += "4";
            servicesList.add(4);
        }
        if (insurance) {
            servicesChoice += "5";
            servicesList.add(5);
        }
        if (toll) {
            servicesChoice += "6";
            servicesList.add(6);
        }
        if (fuel) {
            servicesChoice += "7";
            servicesList.add(7);
        }

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                HttpClient httpClient = new DefaultHttpClient();
                HttpPost postRequest = new HttpPost(IP_HOST + ADD_VEHICLE);

                MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);


                try {
                    reqEntity.addPart("make", new StringBody(make));
                    reqEntity.addPart("model", new StringBody(model));
                    reqEntity.addPart("registration_no", new StringBody(registration_no));
                    reqEntity.addPart("description", new StringBody(description));
                    reqEntity.addPart("services", new StringBody(servicesChoice));
                    reqEntity.addPart("state", new StringBody("1"));
                    reqEntity.addPart("year", new StringBody("2011"));
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
                        runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(getApplicationContext(), "success", Toast.LENGTH_SHORT).show();
                            }
                        });
                        int position = s.indexOf("vehicle_id");
                        vehicle_id[0] = s.substring(position + 12, s.length() - 1);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }

                postRequest.abort();

            }

        });
        thread.start();
    }
}
