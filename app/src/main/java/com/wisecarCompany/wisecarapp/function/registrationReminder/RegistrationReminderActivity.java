package com.wisecarCompany.wisecarapp.function.registrationReminder;

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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.wisecarCompany.wisecarapp.user.vehicle.EditVehicleActivity;
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
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class RegistrationReminderActivity extends AppCompatActivity {

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

    private static final int TAKE_PHOTO = 0;
    private static final int CHOOSE_PHOTO = 1;
    private static final int GROP_PHOTO = 2;
    private static final int MULTI_PERMISSION_CODE = 0;
    private static final int PERMISSION_EXTERNAL_STORAGE_REQUEST_CODE = 1;
    private static final int PERMISSION_CAMERA_REQUEST_CODE = 2;

    private final String IP_HOST = "http://54.206.19.123:3000";
    private final String GET_REGISTRATION_RECORD_IDENTIFIER = "/api/v1/registrationrecords/identifier/";
    private final String scanQRCode = "/api/v1/registrationrecords/upload?identifier=";
    private final String ADD_REGISTRATION_RECORD = "/api/v1/registrationrecords/";
    private final String BLOCKCHAIN_IP = "http://13.236.209.122:3000";
    private final String INVOKE_BLOCKCHAIN = "/api/v1/registrationrecords/blockchaininvoke";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration_reminder);

        backImageButton = $(R.id.backImageButton);
        backImageButton.setOnClickListener(v -> startActivity(new Intent(RegistrationReminderActivity.this, EditVehicleActivity.class).putExtra("vehicleID", vehicleID)));

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

        uploadButton.setOnClickListener(v -> Log.d(TAG, "upload record: "));

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

        cameraImageButton.setOnClickListener(v -> {
            final String[] ways = new String[]{"Take a photo", "Upload from phone", "Cancel"};
            AlertDialog alertDialog = new AlertDialog.Builder(RegistrationReminderActivity.this)
                    .setTitle("How to upload? ")
                    .setIcon(R.mipmap.ic_launcher)
                    .setItems(ways, (dialogInterface, i) -> {
                        Log.d(TAG, "onClick: " + ways[i]);
                        if (i == 0) {  //take photo
                            int permissionCheckCamera = ContextCompat.checkSelfPermission(RegistrationReminderActivity.this, Manifest.permission.CAMERA);
                            int permissionCheckStorage = ContextCompat.checkSelfPermission(RegistrationReminderActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
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
                                        RegistrationReminderActivity.this,
                                        new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                        MULTI_PERMISSION_CODE
                                );
                            } else if (permissionCheckCamera == PackageManager.PERMISSION_DENIED) {
                                Log.d(TAG, "onClickPermissionRequestCamera: ");
                                ActivityCompat.requestPermissions(
                                        RegistrationReminderActivity.this,
                                        new String[]{Manifest.permission.CAMERA},
                                        PERMISSION_CAMERA_REQUEST_CODE
                                );
                            } else if (permissionCheckStorage == PackageManager.PERMISSION_DENIED) {
                                Log.d(TAG, "onClickPermissionRequestStorage: ");
                                ActivityCompat.requestPermissions(
                                        RegistrationReminderActivity.this,
                                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                        PERMISSION_EXTERNAL_STORAGE_REQUEST_CODE
                                );
                            } else {
                                beforeStartCamera();
                            }
                        } else if (i == 1) {   //upload from phone
                            if (ContextCompat.checkSelfPermission(RegistrationReminderActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                                Log.d(TAG, "onClickPermissionRequestStorage: ");
                                ActivityCompat.requestPermissions(
                                        RegistrationReminderActivity.this,
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
                SimpleDateFormat format13 = new SimpleDateFormat("ddMMM yyyy", Locale.getDefault());
                String str = format13.format(date);
                dateEditText.setText(str);
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
        });
        dateEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                Calendar c = Calendar.getInstance();
                new DatePickerDialog(RegistrationReminderActivity.this, (view, year, monthOfYear, dayOfMonth) -> {
                    date = intToDate(year, monthOfYear, dayOfMonth);
                    SimpleDateFormat format14 = new SimpleDateFormat("ddMMM yyyy", Locale.getDefault());
                    String str = format14.format(date);
                    dateEditText.setText(str);
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
                        durationMonth = i+1;
                    })
                    .create();
            alertDialog.show();
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
            //Log.d(TAG, "userID" + UserInfo.getUserID());
            //Log.d(TAG, "vehicle" + vehicle);
            Log.d(TAG, "payment: " + payment);
            Log.d(TAG, "date: " + date);
            Log.d(TAG, "durationMonth: " + durationMonth);
            Log.d(TAG, "expireDate: " + expireDate);
            Log.d(TAG, "remind: " + remind);

            if(date.after(new Date()) || expireDate.before(new Date()) || date.after(expireDate)) {
                Toast.makeText(getApplicationContext(), "Please enter correct date", Toast.LENGTH_SHORT).show();
                return;
            }


            //db
            uploadServiceRecord();


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
            SimpleDateFormat format = new SimpleDateFormat("ddMMM yyyy", Locale.getDefault());
            if(dateEditText.getText().toString().length()>0 && durationMonth>0) {
                try {
                    date = format.parse(dateEditText.getText().toString());
                    Calendar expireCalendar = Calendar.getInstance();
                    expireCalendar.setTime(date);
                    expireCalendar.add(Calendar.MONTH, durationMonth);
                    expireCalendar.add(Calendar.DAY_OF_MONTH, -1);
                    expireDate = expireCalendar.getTime();
                    expireDateEditText.setText(format.format(expireDate));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
            if (paymentEditText.getText().toString().length()>0
                    && dateEditText.getText().toString().length()>0
                    && expireEditText.getText().toString().length()>0
                    && expireDateEditText.getText().toString().length()>0
            ) {     //allow to click saveImageButton
                try {
                    payment = paymentEditText.getText().toString();
                    date = format.parse(dateEditText.getText().toString());
                    expireDate = format.parse(expireDateEditText.getText().toString());
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

        if(remind) isRemind += "1";
        else isRemind += "0";

        String finalIsRemind = isRemind;
        Thread thread = new Thread(() -> {
            HttpClient httpClient = new DefaultHttpClient();
            HttpPost postRequest = new HttpPost(IP_HOST + ADD_REGISTRATION_RECORD);

            MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

            DateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

            try {
                reqEntity.addPart("registration_record_identifier", new StringBody(serviceIDTextView.getText().toString().substring(4)));
                Log.e("identifier in request", serviceIDTextView.getText().toString().substring(4));

                reqEntity.addPart("vehicle_id", new StringBody(vehicleID));

                reqEntity.addPart("payment_no", new StringBody(payment));
                reqEntity.addPart("registration_date", new StringBody(format.format(date)));
                reqEntity.addPart("expires_in", new StringBody(String.valueOf(durationMonth)));
                reqEntity.addPart("expiry_date", new StringBody(format.format(expireDate)));
                reqEntity.addPart("registration_reminder", new StringBody(finalIsRemind));
                reqEntity.addPart("record_id", new StringBody(recordIDTextView.getText().toString()));
                Log.e("recordID in request", recordIDTextView.getText().toString());

                if (qrImageView.getDrawable() != new BitmapDrawable(getResources(), qrCodeBitmap)) {
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    Bitmap toBeUploaded = ((BitmapDrawable) qrImageView.getDrawable()).getBitmap();
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
                        invokeBlockchain(serviceIDTextView.getText().toString().substring(4),
                                payment,
                                String.valueOf(durationMonth),
                                format.format(date),
                                format.format(expireDate),
                                s.toString().substring(s.toString().indexOf("encrypt_hash") + 15, s.toString().indexOf("s3_temp_path") - 3),
                                s.toString().substring(s.toString().indexOf("s3_temp_path") + 15, s.toString().length() - 2));
                    }

                    runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(RegistrationReminderActivity.this, "success", Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(RegistrationReminderActivity.this, EditVehicleActivity.class);
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
}
