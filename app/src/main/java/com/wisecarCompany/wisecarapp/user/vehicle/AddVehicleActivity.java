package com.wisecarCompany.wisecarapp.user.vehicle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

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
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
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

import com.wisecarCompany.wisecarapp.R;
import com.wisecarCompany.wisecarapp.function.HttpUtil;
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
import java.util.HashMap;
import java.util.TreeMap;

public class AddVehicleActivity extends AppCompatActivity {

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

        vehicleImageView = $(R.id.vehicleImageView);
        uploadButton = $(R.id.uploadButton);
        rcEditText = $(R.id.rcEditText);
        makeEditText = $(R.id.makeEditText);
        modelEditText = $(R.id.modelEditText);
        descriptionEditText = $(R.id.descriptionEditText);
        servicesCheckBox = new CheckBox[]{
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

        uploadButton.setOnClickListener(v -> {
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
        });

        saveImageButton.setOnClickListener(v -> {
            if (saveImageButton.getAlpha() < 1) return;
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

            if (isServices == null) {
                isServices = new boolean[6];
                for (int i = 0; i < isServices.length; i++) {
                    isServices[i] = servicesCheckBox[i].isChecked();
                }
            }

            boolean tempB = false;
            for (boolean b : isServices) tempB = tempB || b;
            if (!tempB) {
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

                if (!((BitmapDrawable) vehicleImageView.getDrawable()).getBitmap()
                        .sameAs(((BitmapDrawable) ResourcesCompat.getDrawable(getResources(), R.drawable.vehicle0empty_image, null)).getBitmap())) {
                    uploadVehicleInfo();
                } else {
                    Toast.makeText(this, "Please upload your vehicle photo.", Toast.LENGTH_LONG).show();
                }

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
                if (isServices == null) isServices = new boolean[6];
                for (int i = 0; i < isServices.length; i++) {
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

    private void uploadVehicleInfo() {

        for (int i = 0; i < isServices.length; i++) {
            if (isServices[i]) servicesChoice += i + 1;
        }
        Log.d(TAG, "uploadVehicleInfoByHttpClient: servicesChoice: " + servicesChoice);

        Thread thread = new Thread(() -> {

            HashMap<String, String> params = new HashMap<>();
            File file = null;
            String message = null;
            int vehicle_id = 0;

            try {
                params.put("make", make);
                params.put("model", model);
                params.put("registration_no", registration_no);
                params.put("description", description);
                params.put("services", servicesChoice);
                params.put("state", state);
                params.put("year", year);
                params.put("user_id", UserInfo.getUserID());

                if (!((BitmapDrawable) vehicleImageView.getDrawable()).getBitmap()
                        .sameAs(((BitmapDrawable) ResourcesCompat.getDrawable(getResources(), R.drawable.vehicle0empty_image, null)).getBitmap())) {
                    Bitmap toBeUploaded = ((BitmapDrawable) vehicleImageView.getDrawable()).getBitmap();

                    String root = Environment.getExternalStorageDirectory().toString();
                    File myDir = new File(root + "/saved_images");
                    myDir.mkdirs();

                    String fname = "vehicle.png";
                    file = new File(myDir, fname);
                    if (file.exists()) file.delete();
                    file.createNewFile();
                    BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
                    toBeUploaded.compress(Bitmap.CompressFormat.PNG, 100, bos);

                    bos.flush();
                    bos.close();
                }

                String response = HttpUtil.uploadForm(params, "logo", file, "vehicle.png", IP_HOST + ADD_VEHICLE);

                try {
                    JSONObject jsonObject = new JSONObject(response);
                    message = jsonObject.optString("message");
                    vehicle_id = jsonObject.optInt("vehicle_id");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            Log.e("testest", message + "  " + vehicle_id);

            if (message.equals("success")) {
                // Add successfully
                runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(getApplicationContext(), "success", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(AddVehicleActivity.this, VehicleActivity.class));
                    }
                });
                UserInfo.getVehicles().put("a", new Vehicle(registration_no, make, model, year, state, description, vehicleImageBitmap));
            } else {
                runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Add vehicle failed. Please check your registration number.", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        thread.start();
    }

    private <T extends View> T $(int id) {
        return (T) findViewById(id);
    }
}
