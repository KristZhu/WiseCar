package com.wisecarCompany.wisecarapp.user.licence;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.DatePickerDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.StrictMode;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
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

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.wisecarCompany.wisecarapp.R;
import com.wisecarCompany.wisecarapp.user.vehicle.VehicleActivity;
import com.wisecarCompany.wisecarapp.viewElement.CircleImageView;
import com.wisecarCompany.wisecarapp.viewElement.SwitchButton;
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
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class LicenceActivity extends AppCompatActivity {

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

    private static final int TAKE_PHOTO = 0;
    private static final int CHOOSE_PHOTO = 1;
    private static final int GROP_PHOTO = 2;
    private static final int MULTI_PERMISSION_CODE = 0;
    private static final int PERMISSION_EXTERNAL_STORAGE_REQUEST_CODE = 1;
    private static final int PERMISSION_CAMERA_REQUEST_CODE = 2;

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
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult: Length: " + grantResults.length);
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 0:
                Log.d(TAG, "onRequestPermissionsResult: MULTI?");
                if (grantResults[0] == 0 && grantResults[1] == 0 && grantResults[2] == 0) {
                    beforeStartCamera();
                } else {
                    Toast.makeText(getApplicationContext(), "You cannot take a photo without authorization", Toast.LENGTH_SHORT).show();
                }
                break;
            case 1:
                Log.d(TAG, "onRequestPermissionsResult: STORAGE?");
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    beforeStartStorage();
                } else {
                    Toast.makeText(getApplicationContext(), "You cannot upload the image without authorization", Toast.LENGTH_SHORT).show();
                }
                break;
            case 2:
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case TAKE_PHOTO:
                if (resultCode == RESULT_OK) {
                    Intent intent = new Intent("com.android.camera.action.CROP");
                    intent.setDataAndType(licenceImageUri, "image/**");
                    intent.putExtra("scale", true);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, licenceImageUri);
                    startActivityForResult(intent, GROP_PHOTO);
                }
                break;

            case GROP_PHOTO: //剪裁程序 似乎有bug 不确定是不是由于模拟器！
                if (resultCode == RESULT_OK) {
                    try {
                        Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(licenceImageUri));
                        licenceImageView.setImageBitmap(bitmap);
                        licenceImageBitmap = bitmap;
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
                try {
                    data.putExtra("scale", true);
                    data.putExtra(MediaStore.EXTRA_OUTPUT, licenceImageUri);
                    startActivityForResult(data, GROP_PHOTO);
                } catch (Exception e) {
                    Log.d(TAG, "cancel choosing photo");
                }
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
        licenceImageUri = Uri.fromFile(outputImage);
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        intent.putExtra(MediaStore.EXTRA_OUTPUT, licenceImageUri);
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
        licenceImageUri = Uri.fromFile(outputImage);
        Intent intent = new Intent("android.intent.action.GET_CONTENT");
        intent.setType("image/*");
        intent.putExtra("crop", true);
        intent.putExtra("scale", true);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, licenceImageUri);

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
            licenceImageView.setImageBitmap(bitmap);
            licenceImageBitmap = bitmap;
        } else {
            Toast.makeText(this, "failed to get image", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_licence);

        Log.d(TAG, "username: " + UserInfo.getUsername());
        Log.d(TAG, "userID: " + UserInfo.getUserID());


        backImageButton = $(R.id.backImageButton);
        recordIDTextView = $(R.id.recordIDTextView);
        licenceImageView = $(R.id.licenceImageView);

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


        uploadButton.setOnClickListener(v -> {
            final String[] ways = new String[]{"Take a photo", "Upload from phone", "Cancel"};
            AlertDialog alertDialog3 = new AlertDialog.Builder(LicenceActivity.this)
                    .setTitle("How to upload? ")
                    .setIcon(R.mipmap.ic_launcher)
                    .setItems(ways, (dialogInterface, i) -> {
                        Log.d(TAG, "onClick: " + ways[i]);
                        if (i == 0) {  //take photo
                            int permissionCheckCamera = ContextCompat.checkSelfPermission(LicenceActivity.this, Manifest.permission.CAMERA);
                            int permissionCheckStorage = ContextCompat.checkSelfPermission(LicenceActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
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
                                        LicenceActivity.this,
                                        new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                        MULTI_PERMISSION_CODE
                                );
                            } else if (permissionCheckCamera == PackageManager.PERMISSION_DENIED) {
                                Log.d(TAG, "onClickPermissionRequestCamera: ");
                                ActivityCompat.requestPermissions(
                                        LicenceActivity.this,
                                        new String[]{Manifest.permission.CAMERA},
                                        PERMISSION_CAMERA_REQUEST_CODE
                                );
                            } else if (permissionCheckStorage == PackageManager.PERMISSION_DENIED) {
                                Log.d(TAG, "onClickPermissionRequestStorage: ");
                                ActivityCompat.requestPermissions(
                                        LicenceActivity.this,
                                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                        PERMISSION_EXTERNAL_STORAGE_REQUEST_CODE
                                );
                            } else {    //already permitted
                                beforeStartCamera();
                            }
                        } else if (i == 1) {   //upload from phone
                            if (ContextCompat.checkSelfPermission(LicenceActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                                Log.d(TAG, "onClickPermissionRequestStorage: ");
                                ActivityCompat.requestPermissions(
                                        LicenceActivity.this,
                                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                        PERMISSION_EXTERNAL_STORAGE_REQUEST_CODE
                                );
                            } else {
                                beforeStartStorage();
                            }
                        } else {
                            //cancel
                        }
                    }).create();
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
            if(saveImageButton.getAlpha()<1) return;
            //licenceImageDrawable = licenceImageView.getDrawable();
            //...
            //UserInfo.getLicence().setLicenceImg();

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

            UserInfo.setLicence(new Licence(active, number, type, startDate, expireDate, remind));

            //db
            uploadLicence();


        });

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
            HttpClient httpClient = new DefaultHttpClient();
            HttpPost postRequest = new HttpPost(IP_HOST + ADD_LICENSE);

            MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

            DateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

            try {
                reqEntity.addPart("driver_license_identifier", new StringBody(idTextView.getText().toString().substring(4)));
                Log.e("identifier in request", idTextView.getText().toString().substring(4));

                reqEntity.addPart("user_id", new StringBody(UserInfo.getUserID()));
                reqEntity.addPart("license_no", new StringBody(number));
                reqEntity.addPart("license_type", new StringBody(type));
                reqEntity.addPart("start_date", new StringBody(format.format(startDate)));
                reqEntity.addPart("expires_in", new StringBody(String.valueOf(durationYear)));
                reqEntity.addPart("expiry_date", new StringBody(format.format(expireDate)));
                reqEntity.addPart("remind_me", new StringBody(finalIsRemind));
                reqEntity.addPart("licence_status", new StringBody(finalIsActive));
                reqEntity.addPart("record_id", new StringBody(recordIDTextView.getText().toString()));
                Log.e("recordID", recordIDTextView.getText().toString());

                if (licenceImageView.getDrawable() != null) {
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    Bitmap toBeUploaded = ((BitmapDrawable) licenceImageView.getDrawable()).getBitmap();
                    toBeUploaded.compress(Bitmap.CompressFormat.PNG, 100, stream);
                    byte[] qrbyteArray = stream.toByteArray();
                    ByteArrayBody recordBody = new ByteArrayBody(qrbyteArray, ContentType.IMAGE_PNG, "record.png");
                    reqEntity.addPart("document", recordBody);
                }


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
                        invokeBlockchain(idTextView.getText().toString().substring(4),
                                number,
                                type,
                                format.format(startDate),
                                format.format(expireDate),
                                String.valueOf(durationYear),
                                s.toString().substring(s.toString().indexOf("encrypt_hash") + 15, s.toString().indexOf("s3_temp_path") - 3),
                                s.toString().substring(s.toString().indexOf("s3_temp_path") + 15, s.toString().length() - 2));
                    }

                    runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(LicenceActivity.this, "success", Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(LicenceActivity.this, VehicleActivity.class);
                            startActivity(intent);
                        }
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

}
