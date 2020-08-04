package com.wisecarCompany.wisecarapp.user.profile;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.wisecarCompany.wisecarapp.R;

import java.io.File;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.security.MessageDigest;

import cn.bingoogolapple.baseadapter.BGABaseAdapterUtil;
import cn.bingoogolapple.photopicker.imageloader.BGAImage;
import cn.bingoogolapple.photopicker.util.BGAPhotoHelper;
import cn.bingoogolapple.photopicker.util.BGAPhotoPickerUtil;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;


//https://github.com/bingoogolapple/BGAPhotoPicker-Android  This project is used for upload/take photo in every Activity
public class CreateUserActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {

    private static final String TAG = "createUser";

    private static final int REQUEST_CODE_PERMISSION_CHOOSE_PHOTO = 1;
    private static final int REQUEST_CODE_PERMISSION_TAKE_PHOTO = 2;

    private static final int REQUEST_CODE_CHOOSE_PHOTO = 1;
    private static final int REQUEST_CODE_TAKE_PHOTO = 2;
    private static final int REQUEST_CODE_CROP = 3;

    private BGAPhotoHelper mPhotoHelper;

    private String userImgFilePath;
    private String username;
    private String userEmail;
    private String password;

    private java.util.Date dob;
    private String licence;
    private String address1;
    private String address2;
    private String country;
    private String state;
    private String postCode;

    private ImageView userImgImageView;
    private ImageButton uploadPhotoImageButton;
    private EditText usernameEditText;
    private EditText userEmailEditText;
    private EditText passwordEditText;
    private ImageView passImageView;
    private EditText confirmPasswordEditText;
    private ImageView confirmPassImageView;
    private ImageView confirmNoPassImageView;
    private ImageButton nextImageButton;


    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_user);

        userImgImageView = $(R.id.userImgImageView);
        uploadPhotoImageButton = $(R.id.uploadPhotoImageButton);
        usernameEditText = $(R.id.usernameEditText);
        userEmailEditText = $(R.id.userEmailEditText);
        passwordEditText = $(R.id.passwordEditText);
        passImageView = $(R.id.passImageView);
        confirmPasswordEditText = $(R.id.confirmPasswordEditText);
        confirmPassImageView = $(R.id.confirmPassImageView);
        confirmNoPassImageView = $(R.id.confirmNoPassImageView);
        nextImageButton = $(R.id.nextImageButton);


        passwordEditText.setOnTouchListener((v, event) -> {
            passImageView.setVisibility(View.INVISIBLE);
            return false;
        });
        passwordEditText.setOnClickListener(v -> {
            passImageView.setVisibility(View.INVISIBLE);
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

        // The directory for storing photos after taking a photo.
        // Change to the directory where you want to store the photos after you take them.
        // If this parameter is not passed, there is no camera function
        File takePhotoDir = new File(Environment.getExternalStorageDirectory(), "BGAPhotoPickerTakePhoto");
        mPhotoHelper = new BGAPhotoHelper(takePhotoDir);

        uploadPhotoImageButton.setOnClickListener(v -> {
            final String[] ways = new String[]{"Take a photo", "Upload from phone"};
            AlertDialog alertDialog = new AlertDialog.Builder(CreateUserActivity.this)
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

        nextImageButton.setOnClickListener(v -> {
            //if(passImageView.getVisibility()!=View.VISIBLE || confirmPassImageView.getVisibility()!=View.VISIBLE) return;

            userImgFilePath =  mPhotoHelper.getCropFilePath()==null? null : new File(mPhotoHelper.getCropFilePath()).getAbsolutePath();
            username = usernameEditText.getText().toString();
            userEmail = userEmailEditText.getText().toString();
            password = passwordEditText.getText().toString();

            if (!username.equals("")
                    && !userEmail.equals("")
                    && !password.equals("")
                    && confirmPasswordEditText.getText().toString().equals(password)
            ) {
                boolean isEmail = false;
                try {
                    String check = "^[a-zA-Z0-9_.-]+@[a-zA-Z0-9_.-]+(\\.[a-zA-Z0-9_-]+)+$";
                    Pattern regex = Pattern.compile(check);
                    Matcher matcher = regex.matcher(userEmail);
                    isEmail = matcher.matches();
                } catch (Exception e) {
                    isEmail = false;
                }
                if(!isEmail) {
                    Toast.makeText(getApplicationContext(), "Please entry correct email", Toast.LENGTH_SHORT).show();
                    return;
                }

                startActivity(new Intent(CreateUserActivity.this, CreateUserActivity2.class)
                        .putExtra("userImgPath", userImgFilePath)
                        .putExtra("username", username)
                        .putExtra("userEmail", userEmail)
//                        .putExtra("password", org.apache.commons.codec.digest.DigestUtils.sha256Hex(password)));
                        .putExtra("hashedPassword", sha256(password)));
            } else {    //not valid info
                if (username.equals(""))
                    Toast.makeText(getApplicationContext(), "Please entry nick name", Toast.LENGTH_SHORT).show();
                else if (userEmail.equals(""))
                    Toast.makeText(getApplicationContext(), "Please entry email", Toast.LENGTH_SHORT).show();
                else if (password.equals(""))
                    Toast.makeText(getApplicationContext(), "Please entry password", Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(getApplicationContext(), "2 passwords are not the same", Toast.LENGTH_SHORT).show();
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

    public boolean dispatchTouchEvent(MotionEvent ev) {
        View v = getCurrentFocus();
        if (isShouldHideInput(v, ev)) {
            hideSoftInput(v.getWindowToken());
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

    private <T extends View> T $(int id) {
        return (T) findViewById(id);
    }

    public static String sha256(String base) {
        try{
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(base.getBytes("UTF-8"));
            StringBuffer hexString = new StringBuffer();

            for (int i = 0; i < hash.length; i++) {
                String hex = Integer.toHexString(0xff & hash[i]);
                if(hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            return hexString.toString();
        } catch(Exception ex){
            throw new RuntimeException(ex);
        }
    }

}