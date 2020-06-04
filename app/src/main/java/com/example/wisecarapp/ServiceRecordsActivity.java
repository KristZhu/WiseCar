package com.example.wisecarapp;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

public class ServiceRecordsActivity extends AppCompatActivity {

    private static final String TAG = "Service Records";

    private Vehicle vehicle;

    private ImageButton backImageButton;

    private TextView serviceIDTextView;
    private ImageView qrImageView;
    private Bitmap qrImageBitmap;
    private Uri qrImageUri;
    private Button uploadButton;
    private ImageButton cameraImageButton;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_records);

        String vehicleID = (String) this.getIntent().getStringExtra("vehicleID");
        Log.d(TAG, "vehicleID: " + vehicleID);
        vehicle = UserInfo.getVehicles().get(vehicleID);
        Log.d(TAG, "vehicle: " + vehicle);

        backImageButton = (ImageButton) findViewById(R.id.backImageButton);
        backImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ServiceRecordsActivity.this, EditVehicleActivity.class);
                intent.putExtra("vehicleID", vehicleID);
                startActivity(intent);
            }
        });

        serviceIDTextView = (TextView) findViewById(R.id.serviceTextView);
        qrImageView = (ImageView) findViewById(R.id.qrImageView);
        uploadButton = (Button) findViewById(R.id.uploadButton);
        cameraImageButton = (ImageButton) findViewById(R.id.cameraImageButton);
        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "upload record: ");
            }
        });
        cameraImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String[] ways = new String[]{"Take a photo", "Upload from phone", "Cancel"};
                AlertDialog alertDialog = new AlertDialog.Builder(ServiceRecordsActivity.this)
                        .setTitle("How to upload? ")
                        .setIcon(R.mipmap.ic_launcher)
                        .setItems(ways, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
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
                            }
                        }).create();
                alertDialog.show();
            }
        });


        dateEditText = (EditText) findViewById(R.id.dateEditText);
        centreEditText = (EditText) findViewById(R.id.centreEditText);
        refNoEditText = (EditText) findViewById(R.id.refNoEditText);
        notesEditText = (EditText) findViewById(R.id.notesEditText);
        nextDateEditText = (EditText) findViewById(R.id.nextDateEditText);
        nextDistanceEditText = (EditText) findViewById(R.id.nextDistanceEditText);
        oilCheckBox = (CheckBox) findViewById(R.id.oilCheckBox);
        brakeCheckBox = (CheckBox) findViewById(R.id.brakeCheckBox);
        batteryCheckBox = (CheckBox) findViewById(R.id.batteryCheckBox);
        coolingCheckBox = (CheckBox) findViewById(R.id.coolingCheckBox);
        lightsCheckBox = (CheckBox) findViewById(R.id.lightsCheckBox);

        dateEditText.setInputType(InputType.TYPE_NULL);
        dateEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dateEditText.setText("");
                date = null;
                Calendar c = Calendar.getInstance();
                new TimePickerDialog(ServiceRecordsActivity.this,new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hour, int minute) {
                        if(date==null) ; //user should see DatePickerDialog first, and set date first. If user click back / cancel and skip date, time set should not be allowed.
                        else {
                            StringBuffer time = new StringBuffer();
                            time.append(", ");
                            time.append(hour>=10 ? hour : "0"+hour);
                            time.append(":");
                            time.append(minute>=10 ? minute : "0"+minute);
                            time.append("  ");
                            date = new java.util.Date(date.getTime() + (hour*60+minute)*60*1000);
                            dateEditText.append(time);
                        }
                    }
                }, 0, 0, true).show();
                new DatePickerDialog(ServiceRecordsActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        date = intToDate(year, monthOfYear, dayOfMonth);
                        dateEditText.append(dateToStr(date));
                    }
                }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
        dateEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus) {
                    dateEditText.setText("");
                    Calendar c = Calendar.getInstance();
                    new TimePickerDialog(ServiceRecordsActivity.this,new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePicker view, int hour, int minute) {
                            if(date==null) return; //user should see DatePickerDialog first, and set date first. If user click back / cancel and skip date, time set should not be allowed.
                            else {
                                StringBuffer time = new StringBuffer();
                                time.append(hour>=10 ? hour : "0"+hour);
                                time.append(":");
                                time.append(minute>=10 ? minute : "0"+minute);
                                time.append("  ");
                                date = new Date(date.getTime() + (hour*60+minute)*60*1000);
                                dateEditText.append(time);
                                Log.d(TAG, "date: " + date);
                            }
                        }
                    }, 0, 0, true).show();
                    new DatePickerDialog(ServiceRecordsActivity.this, new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                            date = intToDate(year, monthOfYear, dayOfMonth);
                            dateEditText.append(dateToStr(date) + ", ");
                        }
                    }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
                }
            }
        });

        nextDateEditText.setInputType(InputType.TYPE_NULL);
        nextDateEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar c = Calendar.getInstance();
                new DatePickerDialog(ServiceRecordsActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        nextDate = intToDate(year, monthOfYear, dayOfMonth);
                        nextDateEditText.setText(dateToStr(nextDate));
                    }
                }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
        nextDateEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    Calendar c = Calendar.getInstance();
                    new DatePickerDialog(ServiceRecordsActivity.this, new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                            nextDate = intToDate(year, monthOfYear, dayOfMonth);
                            nextDateEditText.setText(dateToStr(nextDate));
                        }
                    }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
                }
            }
        });


        saveImageButton = (ImageButton) findViewById(R.id.saveImageButton);
        saveImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                    if(Double.parseDouble(nextDistance)<=0) throw new Exception();
                    if(nextDate.after(new java.util.Date())) {

                        // Write INSERTIONG here

                    } else {
                        Toast.makeText(getApplicationContext(), "Please enter correct next service date", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), "Please enter correct next service distance", Toast.LENGTH_SHORT).show();
                }

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
            if(date!=null && centre!=null && refNo!=null && notes!=null && nextDate!=null && nextDistance!=null
                && centre.length()>0 && refNo.length()>0 && notes.length()>0 && nextDistance.length()>0
            ) {
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

    private static java.util.Date strToDate(String str) {
        if (str == null || str.length() == 0) return null;
        SimpleDateFormat format = new SimpleDateFormat("ddMMM yyyy");
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
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
        java.util.Date date = null;
        try {
            date = format.parse(sb.toString());
        } catch (ParseException e) {
            return null;
        }
        return date;
    }
    private static String dateToStr(java.util.Date date) {
        SimpleDateFormat format = new SimpleDateFormat("ddMMM yyyy");
        String str = format.format(date);
        return str;
    }
    private static java.sql.Date utilDateToSqlDate(java.util.Date date) {
        return new java.sql.Date(date.getTime());
    }
    private static java.util.Date sqlDateToUtilDate(java.sql.Date date) {
        return new java.util.Date(date.getTime());
    }

}
