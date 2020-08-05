package com.wisecarCompany.wisecarapp.user.profile;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.text.method.PasswordTransformationMethod;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.wisecarCompany.wisecarapp.R;
import com.wisecarCompany.wisecarapp.function.HttpUtil;
import com.wisecarCompany.wisecarapp.user.UserInfo;
import com.wisecarCompany.wisecarapp.user.licence.Licence;
import com.wisecarCompany.wisecarapp.user.vehicle.AddVehicleActivity;
import com.wisecarCompany.wisecarapp.user.vehicle.DashboardActivity;
import com.wisecarCompany.wisecarapp.user.vehicle.Vehicle;
import com.wisecarCompany.wisecarapp.user.vehicle.VehicleActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.security.MessageDigest;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import cn.bingoogolapple.baseadapter.BGABaseAdapterUtil;
import cn.bingoogolapple.photopicker.imageloader.BGAImage;
import cn.bingoogolapple.photopicker.util.BGAPhotoHelper;
import cn.bingoogolapple.photopicker.util.BGAPhotoPickerUtil;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class UpdateProfileActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {

    private final static String TAG = "Update Profile";
    private final String GET_IMG_EMAIL = "/api/v1/users/";
    private String GET_FNAME_LNAME = "/api/v1/users/getprofile";
    private SharedPreferences sp;

    private String userID;
    private String fName;
    private String lName;
    private String email;
    private String hashedPassword;
    //private Bitmap userImgBitmap;
    private File userImgFile;
    private boolean passwordChanged;

    private ImageButton backImageButton;

    private ImageView userImgImageView;
    private EditText fNameEditText;
    private EditText lNameEditText;
    private EditText emailEditText;
    private EditText passwordEditText;
    private EditText confirmPasswordEditText;

    private ImageView passImageView;
    private ImageView confirmPassImageView;
    private ImageView confirmNoPassImageView;

    private ImageButton uploadPhotoImageButton;

    private ImageButton updateImageButton;

    private static final int REQUEST_CODE_PERMISSION_CHOOSE_PHOTO = 1;
    private static final int REQUEST_CODE_PERMISSION_TAKE_PHOTO = 2;

    private static final int REQUEST_CODE_CHOOSE_PHOTO = 1;
    private static final int REQUEST_CODE_TAKE_PHOTO = 2;
    private static final int REQUEST_CODE_CROP = 3;

    private BGAPhotoHelper mPhotoHelper;

    private String IP_HOST = "http://7ce7ccc8008dec603016594c02f76d60-1846191374.ap-southeast-2.elb.amazonaws.com";
    private String UPDATE_PROFILE = "/api/v1/users/updateprofile";


    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_profile);

        sp = this.getSharedPreferences("userInfo", MODE_PRIVATE);
        userID = sp.getString("USER_ID", "");
        Log.d(TAG, "userID: " + userID);

        backImageButton = $(R.id.backImageButton);
        backImageButton.setOnClickListener(v -> startActivity(new Intent(this, VehicleActivity.class)));

        userImgImageView = $(R.id.userImgImageView);
        fNameEditText = $(R.id.fNameEditText);
        lNameEditText = $(R.id.lNameEditText);
        emailEditText = $(R.id.emailEditText);

        loadUserEmailImg(new userImageCallback() {
            @Override
            public void onSuccess(@NonNull Bitmap value) {
                userImgImageView.setImageBitmap(value);
            }
        }, new userEmailCallback() {
            @Override
            public void onSuccess(@NonNull String value) {
                emailEditText.setText(value);
            }
        });

        loadFNameLName(new getFLNameCallback(){
            @Override
            public void onSuccess(@NonNull String fName, String lName) {
                fNameEditText.setText(fName);
                lNameEditText.setText(lName);
            }
        });

        passwordEditText = $(R.id.passwordEditText);
        confirmPasswordEditText = $(R.id.passwordConfirmEditText);
        passImageView = $(R.id.passImageView);
        confirmPassImageView = $(R.id.confirmPassImageView);
        confirmNoPassImageView = $(R.id.confirmNoPassImageView);

        int passwordLength = sp.getInt("PASSWORD_LENGTH", 10);
        StringBuilder passwordSB = new StringBuilder();
        for (int i = 0; i < passwordLength; i++) passwordSB.append("*");
        passwordEditText.setText(passwordSB.toString());    //show a fake password with the same length of the real one
        confirmPasswordEditText.setText(passwordSB.toString());

        passwordChanged = false;
        passwordEditText.setOnTouchListener((v, event) -> {
            passImageView.setVisibility(View.INVISIBLE);
            if (!passwordChanged) {
                passwordChanged = true;
                passwordEditText.setText("");
                confirmPasswordEditText.setText("");
            }
            return false;
        });
        passwordEditText.setOnClickListener(v -> {
            passImageView.setVisibility(View.INVISIBLE);
            if (!passwordChanged) {
                passwordChanged = true;
                passwordEditText.setText("");
                confirmPasswordEditText.setText("");
            }
        });
        passwordEditText.setOnFocusChangeListener((v, hasFocus) -> {
            passImageView.setVisibility(View.INVISIBLE);
            if (!passwordChanged) {
                passwordChanged = true;
                passwordEditText.setText("");
                confirmPasswordEditText.setText("");
            }
        });

        confirmPasswordEditText.setOnTouchListener((v, event) -> {
            confirmPassImageView.setVisibility(View.INVISIBLE);
            confirmNoPassImageView.setVisibility(View.INVISIBLE);
            return false;
        });
        confirmPasswordEditText.setOnClickListener(v -> {
            confirmPassImageView.setVisibility(View.INVISIBLE);
            confirmNoPassImageView.setVisibility(View.INVISIBLE);
        });
        confirmNoPassImageView.setOnFocusChangeListener((v, hasFocus) -> {
            confirmPassImageView.setVisibility(View.INVISIBLE);
            confirmNoPassImageView.setVisibility(View.INVISIBLE);
        });


        uploadPhotoImageButton = $(R.id.uploadPhotoImageButton);

        // The directory for storing photos after taking a photo.
        // Change to the directory where you want to store the photos after you take them.
        // If this parameter is not passed, there is no camera function
        File takePhotoDir = new File(Environment.getExternalStorageDirectory(), "BGAPhotoPickerTakePhoto");
        mPhotoHelper = new BGAPhotoHelper(takePhotoDir);

        uploadPhotoImageButton.setOnClickListener(v -> {
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

        updateImageButton = $(R.id.updateImageButton);
        updateImageButton.setOnClickListener(v -> {
            //BitmapDrawable bitmapDrawable = (BitmapDrawable) userImgImageView.getDrawable();
            //userImgBitmap = bitmapDrawable.getBitmap();
            userImgFile = mPhotoHelper.getCropFilePath()==null? null : new File(mPhotoHelper.getCropFilePath());
            fName = fNameEditText.getText().toString();
            lName = lNameEditText.getText().toString();
            email = emailEditText.getText().toString();

            if (passwordChanged) {
                if (passImageView.getVisibility() != View.VISIBLE || confirmPassImageView.getVisibility() != View.VISIBLE)
                    return;
                hashedPassword = sha256(passwordEditText.getText().toString());

                final EditText et = new EditText(this);
                et.setTransformationMethod(PasswordTransformationMethod.getInstance()); //password inputtype
                et.setHint("Old password");
                new AlertDialog.Builder(this)
                        .setTitle("You are changing password. Please input old password to verify. ")
                        .setView(et)
                        .setPositiveButton("Confirm", (dialog, which) -> {
                            hideSoftInput(v.getWindowToken());
                            String input = et.getText().toString();
                            if (sha256(input).equals(sp.getString("HASHED_PASSWORD", ""))) {
                                Toast.makeText(this, "Updating, please wait...", Toast.LENGTH_SHORT).show();
                                SharedPreferences.Editor editor = sp.edit()
                                        .putString("HASHED_PASSWORD", hashedPassword)
                                        .putInt("PASSWORD_LENGTH", passwordEditText.getText().toString().length())
                                        .putBoolean("REMEMBER_PASSWORD", false)
                                        .putBoolean("AUTO_LOGIN", false);
                                editor.commit();
                                update(userImgFile, fName, lName, email, hashedPassword);
                            } else {
                                Toast.makeText(this, "Old password incorrect! ", Toast.LENGTH_SHORT).show();
                            }
                        }).setNegativeButton("Cancel", ((dialog, which) -> hideSoftInput(v.getWindowToken())))
                        .show();
            } else {
                update(userImgFile, fName, lName, email);
            }
        });

    }

    private void update(File userImgFile, String fName, String lName, String email, String hashedPassword) {

        updateProfile(fName, lName, email, hashedPassword, userImgFile, true);

    }

    private void update(File userImgFile, String fName, String lName, String email) {

        String unchangedHashedPassword = sp.getString("HASHED_PASSWORD", "");
        updateProfile(fName, lName, email, unchangedHashedPassword, userImgFile, false);

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
                BGAImage.display(userImgImageView, R.drawable.profile0empty_image, mPhotoHelper.getCropFilePath(), BGABaseAdapterUtil.dp2px(200));
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


    public static String sha256(String base) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(base.getBytes("UTF-8"));
            StringBuilder hexString = new StringBuilder();

            for (int i = 0; i < hash.length; i++) {
                String hex = Integer.toHexString(0xff & hash[i]);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private <T extends View> T $(int id) {
        return (T) findViewById(id);
    }

    public boolean dispatchTouchEvent(MotionEvent ev) {
        View v = getCurrentFocus();
        if (isShouldHideInput(v, ev)) {
            hideSoftInput(v.getWindowToken());
            if (passwordChanged) {
                if (passwordEditText.getText().toString().length() > 0) {
                    if (passwordEditText.getText().toString().length() < 8) {
                        Toast.makeText(this, "Password is too short. It should be at least 8 characters. ", Toast.LENGTH_SHORT).show();
                    } else {
                        passImageView.setVisibility(View.VISIBLE);
                    }
                }
                if (confirmPasswordEditText.getText().toString().length() > 0) {
                    if (confirmPasswordEditText.getText().toString().equals(passwordEditText.getText().toString())) {
                        confirmPassImageView.setVisibility(View.VISIBLE);
                        confirmNoPassImageView.setVisibility(View.INVISIBLE);
                    } else {
                        confirmNoPassImageView.setVisibility(View.VISIBLE);
                        confirmPassImageView.setVisibility(View.INVISIBLE);
                    }
                }
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


    private void loadUserEmailImg(@Nullable final userImageCallback imageCallback, @Nullable final userEmailCallback emailCallback) {
        String URL = IP_HOST + GET_IMG_EMAIL + userID;

        JsonObjectRequest objectRequest = new JsonObjectRequest(Request.Method.GET, URL, null, response -> {
            Log.e("Response", response.toString());
            byte[] logoBase64 = Base64.decode(response.optString("logo"), Base64.DEFAULT);
            Bitmap imgBitmap = BitmapFactory.decodeByteArray(logoBase64, 0, logoBase64.length);
            Log.e("image bitmap method: ", imgBitmap == null ? "null img" : imgBitmap.toString());
            String email_address = response.optString("email_address");
            if (imgBitmap == null) {
                Log.e("No image: ", "this user has no image");
            }
            if (imgBitmap != null)
                imageCallback.onSuccess(imgBitmap);
            if (emailCallback != null)
                emailCallback.onSuccess(email_address);
        }, error -> {
            Log.e("ERROR!!!", error.toString());
            Log.e("ERROR!!!", String.valueOf(error.networkResponse));

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
                Log.e("JSON ERROR MESSAGE!!!", message);
            }
        });

        Volley.newRequestQueue(this).add(objectRequest);
    }

    public interface userImageCallback {
        void onSuccess(@NonNull Bitmap value);

//        void onError(@NonNull String error);
    }

    public interface userEmailCallback {
        void onSuccess(@NonNull String value);

//        void onError(@NonNull Throwable throwable);
    }


    private void loadFNameLName(@Nullable final getFLNameCallback callbacks) {
        String URL = IP_HOST + GET_FNAME_LNAME;

        final JSONObject jsonParam = new JSONObject();
        try {
            jsonParam.put("user_id", userID);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest objectRequest = new JsonObjectRequest(Request.Method.POST, URL, jsonParam, response -> {
            Log.e("Response: ", response.toString());
            if (response.optString("message").equals("success"))
                callbacks.onSuccess(response.optString("first_name"), response.optString("last_name"));
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

        Volley.newRequestQueue(this).add(objectRequest);
    }

    public interface getFLNameCallback {
        void onSuccess(@NonNull String fName, String lName);
//        void onError(@NonNull String errorMessage);
    }


    private void updateProfile(String fName, String lName, String email, String hashedPassword, File file, boolean passwordChanged) {

        Thread thread = new Thread(() -> {

            HashMap<String, String> params = new HashMap<>();
            String message = null;

            try {
                params.put("user_id", userID);
                params.put("first_name", fName);
                params.put("last_name", lName);
                params.put("email_address", email);
                params.put("password", hashedPassword);

                String response = HttpUtil.uploadForm(params, "logo", file, "userImage.png", IP_HOST + UPDATE_PROFILE);
                if (response == null) {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(getApplicationContext(), "Failed", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        message = jsonObject.optString("message");
                        Log.e("testest", message);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    if (message.equals("success")) {
                        runOnUiThread(() -> {
                            Toast.makeText(getApplicationContext(), "success", Toast.LENGTH_SHORT).show();
                            if(passwordChanged) startActivity(new Intent(UpdateProfileActivity.this, LoginActivity.class));
                            else startActivity(new Intent(UpdateProfileActivity.this, VehicleActivity.class));
                        });
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        });
        thread.start();
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, VehicleActivity.class));
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK) {
            startActivity(new Intent(this, VehicleActivity.class));
            return true;    //stop calling super method
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }


}
