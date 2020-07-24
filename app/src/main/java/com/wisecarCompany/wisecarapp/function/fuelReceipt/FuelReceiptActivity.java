package com.wisecarCompany.wisecarapp.function.fuelReceipt;

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
import com.wisecarCompany.wisecarapp.user.vehicle.VehicleActivity;
import com.wisecarCompany.wisecarapp.viewElement.CircleImageView;
import com.wisecarCompany.wisecarapp.user.UserInfo;
import com.wisecarCompany.wisecarapp.user.vehicle.Vehicle;

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

public class FuelReceiptActivity extends AppCompatActivity {

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

    private static final int TAKE_PHOTO = 0;
    private static final int CHOOSE_PHOTO = 1;
    private static final int GROP_PHOTO = 2;
    private static final int MULTI_PERMISSION_CODE = 0;
    private static final int PERMISSION_EXTERNAL_STORAGE_REQUEST_CODE = 1;
    private static final int PERMISSION_CAMERA_REQUEST_CODE = 2;

    private String identifier;
    private String record_id;

    private String shared_company_id = "";

    private final String IP_HOST = "http://54.206.19.123:3000";
    private final String GET_FUEL_IDENTIFIER = "/api/v1/fuelreceipts/identifier/";
    private final String GET_IF_SHARED = "/api/v1/fuelreceipts/checkcurrentshare";
    private final String ADD_FUEL = "/api/v1/fuelreceipts/";
    private final String BLOCKCHAIN_IP = "http://13.236.209.122:3000";
    private final String INVOKE_BLOCKCHAIN = "/api/v1/fuelreceipt/blockchaininvoke";

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult: Length: " + grantResults.length);
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 0:
                Log.d(TAG, "onRequestPermissionsResult: MULTI?");
                if(grantResults[0] == 0 && grantResults[1] == 0 && grantResults[2] == 0){
                    beforeStartCamera();
                } else {
                    Toast.makeText(getApplicationContext(), "You cannot take a photo without authorization", Toast.LENGTH_SHORT).show();
                }
                break;
            case 1:
                Log.d(TAG, "onRequestPermissionsResult: STORAGE?");
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    beforeStartStorage();
                } else {
                    Toast.makeText(getApplicationContext(), "You cannot upload the image without authorization", Toast.LENGTH_SHORT).show();
                }
                break;
            case 2:
                Log.d(TAG, "onRequestPermissionsResult: CAMERA?");
                if(grantResults[0] == 0) {
                    beforeStartCamera();
                } else {
                    Toast.makeText(getApplicationContext(), "You cannot take a photo without authorization", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode,int resultCode,Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case TAKE_PHOTO:
                if (resultCode == RESULT_OK) {
                    Intent intent = new Intent("com.android.camera.action.CROP");
                    intent.setDataAndType(fuelImageUri, "image/**");
                    intent.putExtra("scale", true);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, fuelImageUri);
                    startActivityForResult(intent, GROP_PHOTO);
                }
                break;

            case GROP_PHOTO: //剪裁程序 似乎有bug 不确定是不是由于模拟器！
                if (resultCode == RESULT_OK) {
                    try {
                        Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(fuelImageUri));
                        fuelImageView.setImageBitmap(bitmap);
                        fuelImageBitmap = bitmap;
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
                    data.putExtra(MediaStore.EXTRA_OUTPUT, fuelImageUri);
                    startActivityForResult(data, GROP_PHOTO);
                } catch (Exception e) {
                    Log.d(TAG, "cancel choosing photo");
                }
                break;

            default:
                break;
        }

    }

    private void beforeStartCamera () {
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
        fuelImageUri = Uri.fromFile(outputImage);
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fuelImageUri);
        startActivityForResult(intent, TAKE_PHOTO);
        //active camera
    }


    private void beforeStartStorage () {
        File outputImage = new File(getExternalCacheDir(),"output_image.jpg");
        try{
            if(outputImage.exists()){
                outputImage.delete();
            }
            outputImage.createNewFile();
        }catch(IOException e){
            e.printStackTrace();
        }
        fuelImageUri = Uri.fromFile(outputImage);
        Intent intent=new Intent("android.intent.action.GET_CONTENT");
        intent.setType("image/*");
        intent.putExtra("crop",true);
        intent.putExtra("scale",true);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fuelImageUri);

        startActivityForResult(intent,CHOOSE_PHOTO);
    }

    private void handleImageBeforeKitKat(Intent data){
        Uri uri=data.getData();
        String imagePath=getImagePath(uri,null);
        displayImage(imagePath);
    }

    @TargetApi(19)
    private void handleImageOnKitKat(Intent data){
        String imagePath = null;
        Uri uri=data.getData();
        if(DocumentsContract.isDocumentUri(this,uri)){
            //document type Uri
            String docId=DocumentsContract.getDocumentId(uri);
            if("com.android.providers.media.documents".equals(uri.getAuthority())){
                String id=docId.split(":")[1];
                String seletion= MediaStore.Images.Media._ID+"="+id;
                imagePath=getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,seletion);
            }else if("com.android.providers.downloads.documents".equals(uri.getAuthority())){
                Uri contentUri= ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"),Long.valueOf(docId));
                imagePath=getImagePath(contentUri,null);
            }
        }else if("content".equalsIgnoreCase(uri.getScheme())){
            //content type Uri
            imagePath=getImagePath(uri,null);
        }else if("file".equalsIgnoreCase(uri.getScheme())){
            //file type Uri
            imagePath=uri.getPath();
        }
        displayImage(imagePath);
    }

    private String getImagePath(Uri uri,String selection){
        String path=null;
        //get real path
        Cursor cursor=getContentResolver().query(uri,null,selection,null,null);
        if(cursor!=null){
            if(cursor.moveToFirst()){
                path=cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }
        return path;
    }

    private void displayImage(String imagePath){
        if(imagePath!=null){
            Bitmap bitmap= BitmapFactory.decodeFile(imagePath);
            fuelImageView.setImageBitmap(bitmap);
            fuelImageBitmap = bitmap;
        }else{
            Toast.makeText(this,"failed to get image",Toast.LENGTH_SHORT).show();
        }
    }

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

        uploadButton = $(R.id.uploadButton);
        uploadButton.setOnClickListener(v -> {
            final String[] ways = new String[]{"Take a photo", "Upload from phone", "Cancel"};
            AlertDialog alertDialog3 = new AlertDialog.Builder(FuelReceiptActivity.this)
                    .setTitle("How to upload? ")
                    .setIcon(R.mipmap.ic_launcher)
                    .setItems(ways, (dialogInterface, i) -> {
                        Log.d(TAG, "onClick: " + ways[i]);
                        if(i==0) {  //take photo
                            int permissionCheckCamera = ContextCompat.checkSelfPermission(FuelReceiptActivity.this, Manifest.permission.CAMERA);
                            int permissionCheckStorage = ContextCompat.checkSelfPermission(FuelReceiptActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
                            Log.d(TAG, "onClickPermissionCheckCamera: " + permissionCheckCamera);
                            Log.d(TAG, "onClickPermissionCheckStorage: " + permissionCheckStorage);

                            // solve android 7.0 problem
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                                StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
                                StrictMode.setVmPolicy(builder.build());
                                builder.detectFileUriExposure();
                            }

                            if(permissionCheckCamera == PackageManager.PERMISSION_DENIED && permissionCheckStorage == PackageManager.PERMISSION_DENIED) {
                                Log.d(TAG, "onClickPermissionRequestCamera&Storage: ");
                                ActivityCompat.requestPermissions(
                                        FuelReceiptActivity.this,
                                        new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                        MULTI_PERMISSION_CODE
                                );
                            }
                            else if(permissionCheckCamera == PackageManager.PERMISSION_DENIED) {
                                Log.d(TAG, "onClickPermissionRequestCamera: ");
                                ActivityCompat.requestPermissions(
                                        FuelReceiptActivity.this,
                                        new String[]{Manifest.permission.CAMERA},
                                        PERMISSION_CAMERA_REQUEST_CODE
                                );
                            } else if(permissionCheckStorage == PackageManager.PERMISSION_DENIED) {
                                Log.d(TAG, "onClickPermissionRequestStorage: ");
                                ActivityCompat.requestPermissions(
                                        FuelReceiptActivity.this,
                                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                        PERMISSION_EXTERNAL_STORAGE_REQUEST_CODE
                                );
                            } else {    //already permitted
                                beforeStartCamera();
                            }
                        } else if(i==1) {   //upload from phone
                            if(ContextCompat.checkSelfPermission(FuelReceiptActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED){
                                Log.d(TAG, "onClickPermissionRequestStorage: ");
                                ActivityCompat.requestPermissions(
                                        FuelReceiptActivity.this,
                                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                        PERMISSION_EXTERNAL_STORAGE_REQUEST_CODE
                                );
                            }else{
                                beforeStartStorage();
                            }
                        } else {
                            //cancel
                        }
                    }).create();
            alertDialog3.show();
        });

        referenceEditText = $(R.id.referenceEditText);
        dateEditText = $(R.id.dateEditText);
        typeEditText = $(R.id.typeEditText);
        fuelAmountEditText = $(R.id.fuelAmountEditText);
        paidAmountEditText = $(R.id.paidAmountEditText);
        claimableCheckBox = $(R.id.claimableCheckBox);

        SimpleDateFormat format = new SimpleDateFormat("ddMMM yyyy", Locale.getDefault());
        dateEditText.setInputType(InputType.TYPE_NULL);
        dateEditText.setText(format.format(new Date()));
        dateEditText.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new DatePickerDialog(FuelReceiptActivity.this, (view, year, monthOfYear, dayOfMonth) -> {
                date = intToDate(year, monthOfYear, dayOfMonth);
                String str = format.format(date);
                dateEditText.setText(str);
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
        });
        dateEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                Calendar c = Calendar.getInstance();
                new DatePickerDialog(FuelReceiptActivity.this, (view, year, monthOfYear, dayOfMonth) -> {
                    date = intToDate(year, monthOfYear, dayOfMonth);
                    String str = format.format(date);
                    dateEditText.setText(str);
                }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        typeEditText.setInputType(InputType.TYPE_NULL);
        typeEditText.setOnClickListener(v -> {
            final String[] types = new String[]{"Unleaded Petrol (ULP)", "Diesel", "Liquefied Petroleum Gas (LPG)", "Electric Vehicle Charge"};
            AlertDialog alertDialog = new AlertDialog.Builder(FuelReceiptActivity.this)
                    //.setTitle("select a cover type")
                    .setIcon(R.mipmap.ic_launcher)
                    .setItems(types, (dialogInterface, i) -> typeEditText.setText(types[i]))
                    .create();
            alertDialog.show();
        });

        saveImageButton = $(R.id.saveImageButton);
        saveImageButton.setOnClickListener(v -> {
            if(saveImageButton.getAlpha()<1) return;
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

    public boolean dispatchTouchEvent(MotionEvent ev) {
        View v = getCurrentFocus();
        if (isShouldHideInput(v, ev)) {
            hideSoftInput(v.getWindowToken());
            if(referenceEditText.getText().toString().length()>0
                && dateEditText.getText().toString().length()>0
                && typeEditText.getText().toString().length()>0
                && fuelAmountEditText.getText().toString().length()>0
                && paidAmountEditText.getText().toString().length()>0)
            {
                try {
                    date = new SimpleDateFormat("ddMMM yyyy", Locale.getDefault()).parse(dateEditText.getText().toString());
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                try {
                    fuelAmount = Double.parseDouble(fuelAmountEditText.getText().toString());
                } catch (NumberFormatException e) {
                    Toast.makeText(getApplicationContext(), "Please enter correct fuel amount", Toast.LENGTH_SHORT).show();
                    saveImageButton.setAlpha(0.5f);
                    saveImageButton.setClickable(false);
                    return super.dispatchTouchEvent(ev);
                }
                try {
                    paidAmount = Double.parseDouble(paidAmountEditText.getText().toString());
                } catch (NumberFormatException e) {
                    Toast.makeText(getApplicationContext(), "Please enter correct paid amount", Toast.LENGTH_SHORT).show();
                    saveImageButton.setAlpha(0.5f);
                    saveImageButton.setClickable(false);
                    return super.dispatchTouchEvent(ev);
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

    private <T extends View> T $(int id){
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
        }else{
            isClaim += "0";
        }

        String finalIsClaim = isClaim;
        Thread thread = new Thread(() -> {
            HttpClient httpClient = new DefaultHttpClient();
            HttpPost postRequest = new HttpPost(IP_HOST + ADD_FUEL);

            MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

            DateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

            try {
                reqEntity.addPart("fuel_receipt_identifier", new StringBody(idTextView.getText().toString().substring(4)));
                Log.e("identifier in request", idTextView.getText().toString().substring(4));

                reqEntity.addPart("vehicle_id", new StringBody(vehicle.getVehicle_id()));
                reqEntity.addPart("invoice_reference", new StringBody(reference));
                reqEntity.addPart("fuel_date", new StringBody(format.format(date)));
                reqEntity.addPart("fuel_type", new StringBody(type));
                reqEntity.addPart("fuel_amount", new StringBody(String.valueOf(fuelAmount)));
                reqEntity.addPart("paid_amount", new StringBody(String.valueOf(paidAmount)));
                reqEntity.addPart("claimable", new StringBody(finalIsClaim));
                reqEntity.addPart("record_id", new StringBody(identifierTextView.getText().toString()));
                Log.e("recordID", identifierTextView.getText().toString());
                reqEntity.addPart("shared_company_id", new StringBody(sharedTextView.getText().toString()));

                if (fuelImageView.getDrawable() != null) {
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    Bitmap toBeUploaded = ((BitmapDrawable) fuelImageView.getDrawable()).getBitmap();
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
                                reference,
                                type,
                                format.format(date),
                                String.valueOf(fuelAmount),
                                String.valueOf(paidAmount),
                                s.toString().substring(s.toString().indexOf("encrypt_hash") + 15, s.toString().indexOf("s3_temp_path") - 3),
                                s.toString().substring(s.toString().indexOf("s3_temp_path") + 15, s.toString().length() - 2));
                    }

                    runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(FuelReceiptActivity.this, "success", Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(FuelReceiptActivity.this, EditVehicleActivity.class);
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