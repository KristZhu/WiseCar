package com.wisecarCompany.wisecarapp.function.insuranceRecord;

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
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
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
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.wisecarCompany.wisecarapp.function.HttpUtil;
import com.wisecarCompany.wisecarapp.user.vehicle.ManageVehicleActivity;
import com.wisecarCompany.wisecarapp.R;
import com.wisecarCompany.wisecarapp.user.UserInfo;
import com.wisecarCompany.wisecarapp.user.vehicle.Vehicle;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import net.glxn.qrgen.android.QRCode;
import net.glxn.qrgen.core.image.ImageType;

public class InsuranceRecordActivity extends AppCompatActivity {

    private static final String TAG = "Insurance Record";

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

    private String currentDate;
    private String identifier;
    private String record_id;
    private Bitmap qrCodeBitmap;

    private EditText numberEditText;
    private EditText insurerEditText;
    private EditText startEditText;
    private EditText endEditText;
    private EditText typeEditText;
    //private Spinner typeSpinner;
    //private AutoCompleteTextView typeEditText;
    //private ConstraintLayout typeSpinnerDiv;
    //private EditText thirdPartyType;
    //private EditText comprehensiveType;

    private String number;
    private String insurer;
    private Date start;
    private Date end;
    private String type;

    private ImageButton saveImageButton;

    private static final int TAKE_PHOTO = 0;
    private static final int CHOOSE_PHOTO = 1;
    private static final int GROP_PHOTO = 2;
    private static final int MULTI_PERMISSION_CODE = 0;
    private static final int PERMISSION_EXTERNAL_STORAGE_REQUEST_CODE = 1;
    private static final int PERMISSION_CAMERA_REQUEST_CODE = 2;


    // TO BE ADJUSTED
    private final String IP_HOST = "http://54.206.19.123:3000";
    private final String GET_INSURANCE_RECORD_IDENTIFIER = "/api/v1/insurancerecords/identifier/";
    private final String scanQRCode = "/api/v1/insurancerecords/upload?identifier=";
    private final String ADD_INSURANCE_RECORD = "/api/v1/insurancerecords/";
    private final String BLOCKCHAIN_IP = "http://13.236.209.122:3000";
    private final String INVOKE_BLOCKCHAIN = "/api/v1/insurancerecords/blockchaininvoke";

    private SimpleDateFormat displayDateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_insurance_record);

        backImageButton = $(R.id.backImageButton);
        backImageButton.setOnClickListener(v -> startActivity(new Intent(InsuranceRecordActivity.this, ManageVehicleActivity.class).putExtra("vehicleID", vehicleID)));

        vehicleID = (String) this.getIntent().getStringExtra("vehicleID");
        Log.d(TAG, "vehicleID: " + vehicleID);
        vehicle = UserInfo.getVehicles().get(vehicleID);
        Log.d(TAG, "vehicle: " + vehicle);

        SimpleDateFormat format = new SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault());
        currentDate = format.format(Calendar.getInstance().getTime());

        serviceIDTextView = $(R.id.serviceIDTextView);
        qrImageView = $(R.id.qrImageView);
        uploadButton = $(R.id.uploadButton);
        cameraImageButton = $(R.id.cameraImageButton);
        recordIDTextView = $(R.id.recordIDTextView);
        resetButton = $(R.id.resetButton);


        // TO BE ADJUSTED

        getRecordIdentifier((returnedIdentifier, returnedRecord_id) -> {

            Log.e("INSURANCE identifier", identifier);
            Log.e("INSURANCE record_id", record_id);
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

        resetButton.setOnClickListener(v -> qrImageView.setImageBitmap(qrCodeBitmap));

        uploadButton.setOnClickListener(v -> Log.d(TAG, "upload record: "));

        cameraImageButton.setOnClickListener(v -> {
            final String[] ways = new String[]{"Take a photo", "Upload from phone", "Cancel"};
            AlertDialog alertDialog = new AlertDialog.Builder(InsuranceRecordActivity.this)
                    .setTitle("How to upload? ")
                    .setIcon(R.mipmap.ic_launcher)
                    .setItems(ways, (dialogInterface, i) -> {
                        Log.d(TAG, "onClick: " + ways[i]);
                        if (i == 0) {  //take photo
                            int permissionCheckCamera = ContextCompat.checkSelfPermission(InsuranceRecordActivity.this, Manifest.permission.CAMERA);
                            int permissionCheckStorage = ContextCompat.checkSelfPermission(InsuranceRecordActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
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
                                        InsuranceRecordActivity.this,
                                        new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                        MULTI_PERMISSION_CODE
                                );
                            } else if (permissionCheckCamera == PackageManager.PERMISSION_DENIED) {
                                Log.d(TAG, "onClickPermissionRequestCamera: ");
                                ActivityCompat.requestPermissions(
                                        InsuranceRecordActivity.this,
                                        new String[]{Manifest.permission.CAMERA},
                                        PERMISSION_CAMERA_REQUEST_CODE
                                );
                            } else if (permissionCheckStorage == PackageManager.PERMISSION_DENIED) {
                                Log.d(TAG, "onClickPermissionRequestStorage: ");
                                ActivityCompat.requestPermissions(
                                        InsuranceRecordActivity.this,
                                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                        PERMISSION_EXTERNAL_STORAGE_REQUEST_CODE
                                );
                            } else {
                                beforeStartCamera();
                            }
                        } else if (i == 1) {   //upload from phone
                            if (ContextCompat.checkSelfPermission(InsuranceRecordActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                                Log.d(TAG, "onClickPermissionRequestStorage: ");
                                ActivityCompat.requestPermissions(
                                        InsuranceRecordActivity.this,
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

        numberEditText = $(R.id.numberEditText);
        insurerEditText = $(R.id.insurerEditText);
        startEditText = $(R.id.startEditText);
        endEditText = $(R.id.endEditText);

        startEditText.setInputType(InputType.TYPE_NULL);
        startEditText.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new DatePickerDialog(InsuranceRecordActivity.this, (view, year, monthOfYear, dayOfMonth) -> {
                start = intToDate(year, monthOfYear, dayOfMonth);
//                SimpleDateFormat format13 = new SimpleDateFormat("ddMMM yyyy", Locale.getDefault());
                String str = displayDateFormat.format(start);
                startEditText.setText(str);
                checkReadyToSave();
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
        });
        startEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                Calendar c = Calendar.getInstance();
                new DatePickerDialog(InsuranceRecordActivity.this, (view, year, monthOfYear, dayOfMonth) -> {
                    start = intToDate(year, monthOfYear, dayOfMonth);
//                    SimpleDateFormat format14 = new SimpleDateFormat("ddMMM yyyy", Locale.getDefault());
                    String str = displayDateFormat.format(start);
                    startEditText.setText(str);
                    checkReadyToSave();
                }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        endEditText.setInputType(InputType.TYPE_NULL);
        endEditText.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new DatePickerDialog(InsuranceRecordActivity.this, (view, year, monthOfYear, dayOfMonth) -> {
                end = intToDate(year, monthOfYear, dayOfMonth);
                //SimpleDateFormat format13 = new SimpleDateFormat("ddMMM yyyy", Locale.getDefault());
                String str = displayDateFormat.format(end);
                endEditText.setText(str);
                checkReadyToSave();
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
        });
        endEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                Calendar c = Calendar.getInstance();
                new DatePickerDialog(InsuranceRecordActivity.this, (view, year, monthOfYear, dayOfMonth) -> {
                    end = intToDate(year, monthOfYear, dayOfMonth);
//                    SimpleDateFormat format14 = new SimpleDateFormat("ddMMM yyyy", Locale.getDefault());
                    String str = displayDateFormat.format(end);
                    endEditText.setText(str);
                    checkReadyToSave();
                }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        typeEditText = $(R.id.typeEditText);
        //typeSpinner = $(R.id.typeSpinner);
        typeEditText.setInputType(InputType.TYPE_NULL);
        typeEditText.setOnClickListener(v -> {
            final String[] types = new String[]{"Third Party", "Comprehensive"};
            AlertDialog alertDialog = new AlertDialog.Builder(InsuranceRecordActivity.this)
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
                final String[] types = new String[]{"Third Party", "Comprehensive"};
                AlertDialog alertDialog = new AlertDialog.Builder(InsuranceRecordActivity.this)
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

/*
        typeSpinnerDiv = $(R.id.typeSpinnerDiv);
        thirdPartyType = $(R.id.thirdPartyType);
        comprehensiveType = $(R.id.comprehensiveType);

        typeEditText.setInputType(InputType.TYPE_NULL);
        typeEditText.setOnClickListener(v -> typeSpinnerDiv.setVisibility(View.VISIBLE));
        thirdPartyType.setInputType(InputType.TYPE_NULL);
        thirdPartyType.setOnClickListener(v -> {
            typeSpinnerDiv.setVisibility(View.GONE);
            typeEditText.setText("Third Party");
        });
        comprehensiveType.setInputType(InputType.TYPE_NULL);
        comprehensiveType.setOnClickListener(v -> {
            typeSpinnerDiv.setVisibility(View.GONE);
            typeEditText.setText("Comprehensive");
        });
*/
/*
        String[] spinnerItems = new String[] {"Third Party", "Comprehensive"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, spinnerItems);
        typeEditText.setAdapter(adapter);
        typeEditText.setOnItemClickListener((parent, view, position, id) -> {
            type = typeEditText.getText().toString();
            typeEditText.setText(type);
        });
        typeEditText.setOnClickListener(v -> typeEditText.setText(""));

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.layout_spinner_item, spinnerItems);
        typeSpinner.setAdapter(adapter);
        typeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                                  @Override
                                                  public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                                                      Log.d(TAG, "onItemSelected: " + spinnerItems[pos]);

                                                  }
                                                  @Override
                                                  public void onNothingSelected(AdapterView<?> parent) {

                                                  }
                                              });
*/


        saveImageButton = $(R.id.saveImageButton);
        saveImageButton.setOnClickListener(v -> {
            if (saveImageButton.getAlpha() < 1) return;
            Toast.makeText(getApplicationContext(), "Saving, Please Wait...", Toast.LENGTH_LONG).show();
            //Log.d(TAG, "userID" + UserInfo.getUserID());
            //Log.d(TAG, "vehicle" + vehicle);
            Log.d(TAG, "number: " + number);
            Log.d(TAG, "insurer: " + insurer);
            Log.d(TAG, "start: " + start);
            Log.d(TAG, "end: " + end);
            Log.d(TAG, "type: " + type);

//            if(start.before(new Date()) || end.after(new Date()) || start.after(end)) {
//                Toast.makeText(getApplicationContext(), "Please enter correct date", Toast.LENGTH_SHORT).show();
//                return;
//            }


            //db
            uploadInsuranceRecord();


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

    private void checkReadyToSave() {
        if (numberEditText.getText().toString().length() > 0
                && insurerEditText.getText().toString().length() > 0
                && startEditText.getText().toString().length() > 0
                && endEditText.getText().toString().length() > 0
                && typeEditText.getText().toString().length() > 0
            //&& typeSpinner.
        ) {     //allow to click saveImageButton
            try {
                //SimpleDateFormat format = new SimpleDateFormat("ddMMM yyyy", Locale.getDefault());
                number = numberEditText.getText().toString();
                insurer = insurerEditText.getText().toString();
                start = displayDateFormat.parse(startEditText.getText().toString());
                end = displayDateFormat.parse(endEditText.getText().toString());
                type = typeEditText.getText().toString();
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
            //typeSpinnerDiv.setVisibility(View.GONE);
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

    private <T extends View> T $(int id) {
        return (T) findViewById(id);
    }


    // TO BE ADJUSTED

    private void getRecordIdentifier(@Nullable final recordIdentifierCallback callbacks) {

        String URL = IP_HOST + GET_INSURANCE_RECORD_IDENTIFIER + vehicle.getRegistration_no() + "/" + currentDate;

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

        Volley.newRequestQueue(InsuranceRecordActivity.this).add(objectRequest);
    }

    public interface recordIdentifierCallback {
        void onSuccess(@NonNull String returnedIdentifier, String returnedRecord_id);

//        void onError(@NonNull String errorMessage);
    }


    private void uploadInsuranceRecord() {

        Thread thread = new Thread(() -> {

            DateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

            HashMap<String, String> params = new HashMap<>();//xuzheng
            File file = null;
            String message = null;
            String encrypt_hash = null;
            String s3_temp_path = null;

            try {
                params.put("insurance_record_identifier", serviceIDTextView.getText().toString().substring(4));
                params.put("vehicle_id", vehicleID);
                params.put("policy_number", number);
                params.put("insurer", insurer);
                params.put("start_of_cover", format.format(start));
                params.put("end_of_cover", format.format(end));
                params.put("cover_type", type);
                params.put("record_id", recordIDTextView.getText().toString());
                if (!((BitmapDrawable) qrImageView.getDrawable()).getBitmap().sameAs(qrCodeBitmap)) {
                    Bitmap toBeUploaded = ((BitmapDrawable) qrImageView.getDrawable()).getBitmap();

                    String root = Environment.getExternalStorageDirectory().toString();
                    File myDir = new File(root + "/saved_images");
                    myDir.mkdirs();

                    String fname = "insurance.png";
                    file = new File(myDir, fname);
                    if (file.exists()) file.delete();
                    file.createNewFile();
                    BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
                    toBeUploaded.compress(Bitmap.CompressFormat.PNG, 100, bos);

                    bos.flush();
                    bos.close();
                }
                String response = HttpUtil.uploadForm(params, "document", file, "record.png", IP_HOST + ADD_INSURANCE_RECORD);

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
                            number,
                            insurer,
                            format.format(start),
                            format.format(end),
                            type,
                            encrypt_hash,
                            s3_temp_path);
                }

                runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(InsuranceRecordActivity.this, "success", Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(InsuranceRecordActivity.this, ManageVehicleActivity.class);
                        intent.putExtra("vehicleID", vehicleID);
                        startActivity(intent);
                    }
                });
            }
        });
        thread.start();
    }

    private void invokeBlockchain(String identifier, String policy_number, String insurer, String start_of_cover, String end_of_cover, String cover_type, String ecrypt_hash, String insurance_file_location) {

        String URL = BLOCKCHAIN_IP + INVOKE_BLOCKCHAIN;

        final JSONObject jsonParam = new JSONObject();
        try {
            jsonParam.put("identifier", identifier);
            jsonParam.put("record_type", "insurance");
            jsonParam.put("policy_number", policy_number);
            jsonParam.put("insurer", insurer);
            jsonParam.put("start_of_cover", start_of_cover);
            jsonParam.put("end_of_cover", end_of_cover);
            jsonParam.put("cover_type", cover_type);
            jsonParam.put("ecrypt_hash", ecrypt_hash);
            jsonParam.put("insurance_file_location", insurance_file_location);

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
        Volley.newRequestQueue(InsuranceRecordActivity.this).add(objectRequest);

    }
}
