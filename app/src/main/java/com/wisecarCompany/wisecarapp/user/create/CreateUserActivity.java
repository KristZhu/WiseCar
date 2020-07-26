package com.wisecarCompany.wisecarapp.user.create;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
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
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.wisecarCompany.wisecarapp.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.security.MessageDigest;

/**
 * Currently, the photo functions are very very crude!!
 * If user takes a new photo, he/she cannot edit it.
 * If the user upload a photo from local storage, he/she can see different ways to edit it.
 * The ways include correct ones such as "Edit Pic", while unrelated as well such as "Open Wechat" (WTH?)
 * More seriously!! If the user choose "Edit Pic", and finish editing, and save. It will CRASH!!
 * I have no idea neither why unrelated ways will appear, nor why crashes.
 * For now, user can only take or select a photo without editing. The basic behaviours work.
 */


public class CreateUserActivity extends AppCompatActivity{

    private static final String TAG = "createUser";

    private byte[] userImg;
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

    private static final int TAKE_PHOTO = 0;
    private static final int CHOOSE_PHOTO = 1;
    private static final int GROP_PHOTO = 2;
    private static final int MULTI_PERMISSION_CODE = 0;
    private static final int PERMISSION_EXTERNAL_STORAGE_REQUEST_CODE = 1;
    private static final int PERMISSION_CAMERA_REQUEST_CODE = 2;

    private Uri userImgImageUri;
    private Bitmap userImgImageBitmap;
    private Drawable userImgDrawable;

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

        uploadPhotoImageButton.setOnClickListener(v -> {
            final String[] ways = new String[]{"Take a photo", "Upload from phone", "Cancel"};
            AlertDialog alertDialog3 = new AlertDialog.Builder(CreateUserActivity.this)
                    .setTitle("How to upload? ")
                    .setIcon(R.mipmap.ic_launcher)
                    .setItems(ways, (dialogInterface, i) -> {
                        Log.d(TAG, "onClick: " + ways[i]);
                        if (i == 0) {  //take photo
                            int permissionCheckCamera = ContextCompat.checkSelfPermission(CreateUserActivity.this, Manifest.permission.CAMERA);
                            int permissionCheckStorage = ContextCompat.checkSelfPermission(CreateUserActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
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
                                        CreateUserActivity.this,
                                        new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                        MULTI_PERMISSION_CODE
                                );
                            } else if (permissionCheckCamera == PackageManager.PERMISSION_DENIED) {
                                Log.d(TAG, "onClickPermissionRequestCamera: ");
                                ActivityCompat.requestPermissions(
                                        CreateUserActivity.this,
                                        new String[]{Manifest.permission.CAMERA},
                                        PERMISSION_CAMERA_REQUEST_CODE
                                );
                            } else if (permissionCheckStorage == PackageManager.PERMISSION_DENIED) {
                                Log.d(TAG, "onClickPermissionRequestStorage: ");
                                ActivityCompat.requestPermissions(
                                        CreateUserActivity.this,
                                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                        PERMISSION_EXTERNAL_STORAGE_REQUEST_CODE
                                );
                            } else {    //already permitted
                                beforeStartCamera();
                            }
                        } else if (i == 1) {   //upload from phone
                            if (ContextCompat.checkSelfPermission(CreateUserActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                                Log.d(TAG, "onClickPermissionRequestStorage: ");
                                ActivityCompat.requestPermissions(
                                        CreateUserActivity.this,
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

        nextImageButton.setOnClickListener(v -> {

            userImgDrawable = userImgImageView.getDrawable();
            username = usernameEditText.getText().toString();
            userEmail = userEmailEditText.getText().toString();
            password = passwordEditText.getText().toString();

            if (!username.equals("")
                    && !userEmail.equals("")
                    && !password.equals("")
                    && confirmPasswordEditText.getText().toString().equals(password)
                //&& userImgDrawable!=null
            ) {
                boolean isEmail = false;
                try {
                    String check = "^([a-z0-9A-Z]+[-|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";
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

                BitmapDrawable bitmapDrawable = (BitmapDrawable) userImgImageView.getDrawable();
                userImgImageBitmap = bitmapDrawable.getBitmap();
                int width = (int) Math.round(userImgImageBitmap.getWidth() / 1.5);
                int height = (int) Math.round(userImgImageBitmap.getHeight() / 1.5);

                userImgImageBitmap = Bitmap.createScaledBitmap(userImgImageBitmap, width, height, true);

                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                userImgImageBitmap.compress(Bitmap.CompressFormat.PNG, 100, bos);
                userImg = bos.toByteArray();

                username = usernameEditText.getText().toString();
                userEmail = userEmailEditText.getText().toString();
                password = passwordEditText.getText().toString();

                startActivity(new Intent(CreateUserActivity.this, CreateUserActivity2.class)
                        .putExtra("userImg", userImg)
                        .putExtra("username", username)
                        .putExtra("userEmail", userEmail)
//                        .putExtra("password", org.apache.commons.codec.digest.DigestUtils.sha256Hex(password)));
                        .putExtra("password", sha256(password)));
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case TAKE_PHOTO:
                if (resultCode == RESULT_OK) {
                    Intent intent = new Intent("com.android.camera.action.CROP");
                    intent.setDataAndType(userImgImageUri, "image/**");
                    intent.putExtra("scale", true);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, userImgImageUri);
                    startActivityForResult(intent, GROP_PHOTO);
                }
                break;

            case GROP_PHOTO: //剪裁程序 似乎有bug 不确定是不是由于模拟器！
                if (resultCode == RESULT_OK) {
                    try {
                        Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(userImgImageUri));
                        userImgImageView.setImageBitmap(bitmap);
                        userImgImageBitmap = bitmap;
                        //show
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
                break;

            case CHOOSE_PHOTO:
                if (resultCode == RESULT_OK) {
                    if (Build.VERSION.SDK_INT >= 19) handleImageOnKitKat(data);   //4.4 or above
                    else handleImageBeforeKitKat(data);    //below 4.4
                }
                try {
                    data.putExtra("scale", true);
                    data.putExtra(MediaStore.EXTRA_OUTPUT, userImgImageUri);
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
        userImgImageUri = Uri.fromFile(outputImage);
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        intent.putExtra(MediaStore.EXTRA_OUTPUT, userImgImageUri);
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
        userImgImageUri = Uri.fromFile(outputImage);
        Intent intent = new Intent("android.intent.action.GET_CONTENT");
        intent.setType("image/*");
        intent.putExtra("crop", true);
        intent.putExtra("scale", true);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, userImgImageUri);

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
            userImgImageView.setImageBitmap(bitmap);
            userImgImageBitmap = bitmap;
        } else {
            Toast.makeText(this, "failed to get image", Toast.LENGTH_SHORT).show();
        }
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