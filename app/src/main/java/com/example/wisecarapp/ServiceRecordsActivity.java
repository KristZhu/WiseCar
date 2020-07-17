package com.example.wisecarapp;

import androidx.appcompat.app.AlertDialog;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

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
import java.util.Locale;

public class ServiceRecordsActivity extends AppCompatActivity {

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

    private EditText dateEditText;
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

    private static final int TAKE_PHOTO = 0;
    private static final int CHOOSE_PHOTO = 1;
    private static final int GROP_PHOTO = 2;
    private static final int MULTI_PERMISSION_CODE = 0;
    private static final int PERMISSION_EXTERNAL_STORAGE_REQUEST_CODE = 1;
    private static final int PERMISSION_CAMERA_REQUEST_CODE = 2;

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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_records);

        servicesOptions = "";
        currentDate = "";

        backImageButton = $(R.id.backImageButton);
        backImageButton.setOnClickListener(v -> startActivity(new Intent(ServiceRecordsActivity.this, EditVehicleActivity.class).putExtra("vehicleID", vehicleID)));

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

        uploadButton.setOnClickListener((v) -> Log.d(TAG, "upload record: "));

        cameraImageButton.setOnClickListener(v -> {
            final String[] ways = new String[]{"Take a photo", "Upload from phone", "Cancel"};
            AlertDialog alertDialog = new AlertDialog.Builder(ServiceRecordsActivity.this)
                    .setTitle("How to upload? ")
                    .setIcon(R.mipmap.ic_launcher)
                    .setItems(ways, (dialogInterface, i) -> {
                        Log.d(TAG, "onClick: " + ways[i]);
                        if (i == 0) {  //take photo
                            int permissionCheckCamera = ContextCompat.checkSelfPermission(ServiceRecordsActivity.this, Manifest.permission.CAMERA);
                            int permissionCheckStorage = ContextCompat.checkSelfPermission(ServiceRecordsActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
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
                                        ServiceRecordsActivity.this,
                                        new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                        MULTI_PERMISSION_CODE
                                );
                            } else if (permissionCheckCamera == PackageManager.PERMISSION_DENIED) {
                                Log.d(TAG, "onClickPermissionRequestCamera: ");
                                ActivityCompat.requestPermissions(
                                        ServiceRecordsActivity.this,
                                        new String[]{Manifest.permission.CAMERA},
                                        PERMISSION_CAMERA_REQUEST_CODE
                                );
                            } else if (permissionCheckStorage == PackageManager.PERMISSION_DENIED) {
                                Log.d(TAG, "onClickPermissionRequestStorage: ");
                                ActivityCompat.requestPermissions(
                                        ServiceRecordsActivity.this,
                                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                        PERMISSION_EXTERNAL_STORAGE_REQUEST_CODE
                                );
                            } else {
                                beforeStartCamera();
                            }
                        } else if (i == 1) {   //upload from phone
                            if (ContextCompat.checkSelfPermission(ServiceRecordsActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                                Log.d(TAG, "onClickPermissionRequestStorage: ");
                                ActivityCompat.requestPermissions(
                                        ServiceRecordsActivity.this,
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
            alertDialog.show();
        });


        dateEditText = $(R.id.dateEditText);
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

        dateEditText.setInputType(InputType.TYPE_NULL);
        dateEditText.setOnClickListener(v -> {
            dateEditText.setText("");
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
                    dateEditText.append(time);
                    Log.d(TAG, "date: " + date);
                }
            }, 0, 0, true).show();
            new DatePickerDialog(ServiceRecordsActivity.this, (view, year, monthOfYear, dayOfMonth) -> {
                date = intToDate(year, monthOfYear, dayOfMonth);
                SimpleDateFormat format1 = new SimpleDateFormat("ddMMM yyyy", Locale.getDefault());
                String str = format1.format(date);
                dateEditText.append(str + ", ");
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
        });
        dateEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                dateEditText.setText("");
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
                        dateEditText.append(time);
                        Log.d(TAG, "date: " + date);
                    }
                }, 0, 0, true).show();
                new DatePickerDialog(ServiceRecordsActivity.this, (view, year, monthOfYear, dayOfMonth) -> {
                    date = intToDate(year, monthOfYear, dayOfMonth);
                    SimpleDateFormat format12 = new SimpleDateFormat("ddMMM yyyy", Locale.getDefault());
                    String str = format12.format(date);
                    dateEditText.append(str + ", ");
                }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        nextDateEditText.setInputType(InputType.TYPE_NULL);
        nextDateEditText.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new DatePickerDialog(ServiceRecordsActivity.this, (view, year, monthOfYear, dayOfMonth) -> {
                nextDate = intToDate(year, monthOfYear, dayOfMonth);
                SimpleDateFormat format13 = new SimpleDateFormat("ddMMM yyyy", Locale.getDefault());
                String str = format13.format(nextDate);
                nextDateEditText.setText(str);
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
        });
        nextDateEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                Calendar c = Calendar.getInstance();
                new DatePickerDialog(ServiceRecordsActivity.this, (view, year, monthOfYear, dayOfMonth) -> {
                    nextDate = intToDate(year, monthOfYear, dayOfMonth);
                    SimpleDateFormat format14 = new SimpleDateFormat("ddMMM yyyy", Locale.getDefault());
                    String str = format14.format(nextDate);
                    nextDateEditText.setText(str);
                }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
            }
        });


        saveImageButton = $(R.id.saveImageButton);
        saveImageButton.setOnClickListener(v -> {
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case TAKE_PHOTO:
                if (resultCode == RESULT_OK) {
                    Intent intent = new Intent("com.android.camera.action.CROP");
                    intent.setDataAndType(qrImageUri, "image/**");
                    intent.putExtra("scale", true);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, qrImageUri);
                    startActivityForResult(intent, GROP_PHOTO);
                }
                break;

            case GROP_PHOTO: //剪裁程序 似乎有bug 不确定是不是由于模拟器！
                if (resultCode == RESULT_OK) {
                    try {
                        Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(qrImageUri));
                        qrImageView.setImageBitmap(bitmap);
                        qrImageBitmap = bitmap;
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
                data.putExtra(MediaStore.EXTRA_OUTPUT, qrImageUri);
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
        qrImageUri = Uri.fromFile(outputImage);
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        intent.putExtra(MediaStore.EXTRA_OUTPUT, qrImageUri);
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
        qrImageUri = Uri.fromFile(outputImage);
        Intent intent = new Intent("android.intent.action.GET_CONTENT");
        intent.setType("image/*");
        intent.putExtra("crop", true);
        intent.putExtra("scale", true);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, qrImageUri);

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
            qrImageView.setImageBitmap(bitmap);
            qrImageBitmap = bitmap;
        } else {
            Toast.makeText(this, "failed to get image", Toast.LENGTH_SHORT).show();
        }
    }

    public boolean dispatchTouchEvent(MotionEvent ev) {
        View v = getCurrentFocus();
        if (isShouldHideInput(v, ev)) {
            hideSoftInput(v.getWindowToken());
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

    private static java.util.Date strToDate(String str) {
        if (str == null || str.length() == 0) return null;
        SimpleDateFormat format = new SimpleDateFormat("ddMMM yyyy", Locale.getDefault());
        java.util.Date date = null;
        try {
            date = format.parse(str);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
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

                    runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(ServiceRecordsActivity.this, "success", Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(ServiceRecordsActivity.this, EditVehicleActivity.class);
                            intent.putExtra("vehicleID", vehicleID);
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
