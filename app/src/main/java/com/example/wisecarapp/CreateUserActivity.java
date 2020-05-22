package com.example.wisecarapp;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
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
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Blob;

public class CreateUserActivity extends AppCompatActivity {

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
        if(grantResults.length>0) Log.d(TAG, "onRequestPermissionsResult: " + grantResults[0]);
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 0:
                Log.d(TAG, "onRequestPermissionsResult: MULTI?");
                if(grantResults[0] == 0 && grantResults[1] == 0 && grantResults[2] == 0){
                    //创建File对象，用于存储拍照后的照片
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
                    //启动照相机
                } else {
                    //...
                }
                break;
            case 1:
                Log.d(TAG, "onRequestPermissionsResult: STORAGE?");
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //...
                }
                break;
            case 2:
                Log.d(TAG, "onRequestPermissionsResult: CAMERA?");
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //...
                }
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_user);

        userImgImageView = (ImageView) findViewById(R.id.userImgImageView);
        uploadPhotoImageButton = (ImageButton) findViewById(R.id.uploadPhotoImageButton);
        usernameEditText = (EditText) findViewById(R.id.usernameEditText);
        userEmailEditText = (EditText) findViewById(R.id.userEmailEditText);
        passwordEditText = (EditText) findViewById(R.id.passwordEditText);
        passImageView = (ImageView) findViewById(R.id.passImageView);
        confirmPasswordEditText = (EditText) findViewById(R.id.confirmPasswordEditText);
        confirmPassImageView = (ImageView) findViewById(R.id.confirmPassImageView);
        confirmNoPassImageView = (ImageView) findViewById(R.id.confirmNoPassImageView);
        nextImageButton = (ImageButton) findViewById(R.id.nextImageButton);


        passwordEditText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                passImageView.setVisibility(View.INVISIBLE);
                return false;
            }
        });
        passwordEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                passImageView.setVisibility(View.INVISIBLE);
            }
        });

        confirmPasswordEditText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                confirmPassImageView.setVisibility(View.INVISIBLE);
                confirmNoPassImageView.setVisibility(View.INVISIBLE);
                return false;
            }
        });
        confirmPasswordEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmPassImageView.setVisibility(View.INVISIBLE);
                confirmNoPassImageView.setVisibility(View.INVISIBLE);
            }
        });

        uploadPhotoImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String[] ways = new String[]{"Take a photo", "Upload from phone", "Cancel"};
                AlertDialog alertDialog3 = new AlertDialog.Builder(CreateUserActivity.this)
                        .setTitle("How to upload? ")
                        .setIcon(R.mipmap.ic_launcher)
                        .setItems(ways, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Log.d(TAG, "onClick: " + ways[i]);
                                if(i==0) {  //take photo
                                    int permissionCheckCamera = ContextCompat.checkSelfPermission(CreateUserActivity.this, Manifest.permission.CAMERA);
                                    int permissionCheckStorage = ContextCompat.checkSelfPermission(CreateUserActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
                                    Log.d(TAG, "onClickPermissionCheckCamera: " + permissionCheckCamera);
                                    Log.d(TAG, "onClickPermissionCheckStorage: " + permissionCheckStorage);

                                    // android 7.0系统解决拍照的问题
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                                        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
                                        StrictMode.setVmPolicy(builder.build());
                                        builder.detectFileUriExposure();
                                    }

                                    if(permissionCheckCamera == PackageManager.PERMISSION_DENIED && permissionCheckStorage == PackageManager.PERMISSION_DENIED) {
                                        Log.d(TAG, "onClickPermissionRequestCamera&Storage: ");
                                        ActivityCompat.requestPermissions(
                                                CreateUserActivity.this,
                                                new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                                MULTI_PERMISSION_CODE
                                        );
                                    }
                                    else if(permissionCheckCamera == PackageManager.PERMISSION_DENIED) {
                                        Log.d(TAG, "onClickPermissionRequestCamera: ");
                                        ActivityCompat.requestPermissions(
                                                CreateUserActivity.this,
                                                new String[]{Manifest.permission.CAMERA},
                                                PERMISSION_CAMERA_REQUEST_CODE
                                        );
                                    } else if(permissionCheckStorage == PackageManager.PERMISSION_DENIED) {
                                        Log.d(TAG, "onClickPermissionRequestStorage: ");
                                        ActivityCompat.requestPermissions(
                                                CreateUserActivity.this,
                                                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                                PERMISSION_EXTERNAL_STORAGE_REQUEST_CODE
                                        );
                                    } else {
                                        //创建File对象，用于存储拍照后的照片
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
                                        //启动照相机
                                    }
                                } else if(i==1) {   //upload from phone
                                    if(ContextCompat.checkSelfPermission(CreateUserActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
                                        ActivityCompat.requestPermissions(
                                                CreateUserActivity.this,
                                                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                                PERMISSION_EXTERNAL_STORAGE_REQUEST_CODE
                                        );
                                    }else{
                                        File outputImage=new File(getExternalCacheDir(),"output_image.jpg");
                                        try{
                                            if(outputImage.exists()){
                                                outputImage.delete();
                                            }
                                            outputImage.createNewFile();
                                        }catch(IOException e){
                                            e.printStackTrace();
                                        }
                                        userImgImageUri = Uri.fromFile(outputImage);
                                        Intent intent=new Intent("android.intent.action.GET_CONTENT");
                                        intent.setType("image/*");
                                        intent.putExtra("crop",true);
                                        intent.putExtra("scale",true);
                                        intent.putExtra(MediaStore.EXTRA_OUTPUT, userImgImageUri);

                                        startActivityForResult(intent,CHOOSE_PHOTO);
                                    }
                                } else {    //cancel

                                }
                            }
                        }).create();
                alertDialog3.show();
            }
        });

        nextImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userImgDrawable = userImgImageView.getDrawable();
                username = usernameEditText.getText().toString();
                userEmail = userEmailEditText.getText().toString();
                password = passwordEditText.getText().toString();

                if(username!=null
                    && userEmail!=null
                    && passImageView.getVisibility()==View.VISIBLE
                    && confirmPassImageView.getVisibility()==View.VISIBLE
                    && confirmNoPassImageView.getVisibility()==View.INVISIBLE
                    && userImgDrawable!=null
                ) {

                    userImgImageBitmap = Bitmap.createBitmap(
                            userImgDrawable.getIntrinsicWidth(),
                            userImgDrawable.getIntrinsicHeight(),
                            userImgDrawable.getOpacity() != PixelFormat.OPAQUE ?
                                    Bitmap.Config.ARGB_8888: Bitmap.Config.RGB_565);
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    userImgImageBitmap.compress(Bitmap.CompressFormat.PNG, 100, bos);
                    userImg = bos.toByteArray();

                    username = usernameEditText.getText().toString();
                    userEmail = userEmailEditText.getText().toString();
                    password = passwordEditText.getText().toString();
                    Intent intent = new Intent(CreateUserActivity.this, CreateUserActivity2.class);
                    intent.putExtra("userImgImage", userImg);
                    intent.putExtra("username", username);
                    intent.putExtra("userEmail", userEmail);
                    intent.putExtra("password", password);
                    startActivity(intent);
                }
            }
        });

    }

    @Override
    public void onActivityResult(int requestCode,int resultCode,Intent data) {
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

            case GROP_PHOTO: //剪裁程序 似乎有bug 不确定是不是由于模拟器
                if (resultCode == RESULT_OK) {
                    try {
                        Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(userImgImageUri));
                        userImgImageView.setImageBitmap(bitmap);
                        userImgImageBitmap = bitmap;
                        //将剪裁后的照片显示出来
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
                break;

            case CHOOSE_PHOTO:
                if (resultCode == RESULT_OK) {
                    if (Build.VERSION.SDK_INT >= 19) {//4.4及以上系统
                        handleImageOnKitKat(data);
                    } else {//4.4以下系统
                        handleImageBeforeKitKat(data);
                    }
                }
                data.putExtra("scale", true);
                data.putExtra(MediaStore.EXTRA_OUTPUT, userImgImageUri);
                startActivityForResult(data, GROP_PHOTO);
                break;

            default:
                break;
        }

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
            //document类型的Uri处理方式
            String docId=DocumentsContract.getDocumentId(uri);
            if("com.android.providers.media.documents".equals(uri.getAuthority())){
                String id=docId.split(":")[1];
                String seletion=MediaStore.Images.Media._ID+"="+id;
                imagePath=getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,seletion);
            }else if("com.android.providers.downloads.documents".equals(uri.getAuthority())){
                Uri contentUri= ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"),Long.valueOf(docId));
                imagePath=getImagePath(contentUri,null);
            }
        }else if("content".equalsIgnoreCase(uri.getScheme())){
            //content类型的Uri处理方式
            imagePath=getImagePath(uri,null);
        }else if("file".equalsIgnoreCase(uri.getScheme())){
            //file类型的Uri，直接获取路径即可
            imagePath=uri.getPath();
        }
        displayImage(imagePath);
    }

    private String getImagePath(Uri uri,String selection){
        String path=null;
        //获取真实的图片路径
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
            userImgImageView.setImageBitmap(bitmap);
            userImgImageBitmap = bitmap;
        }else{
            Toast.makeText(this,"failed to get image",Toast.LENGTH_SHORT).show();
        }
    }

    public boolean dispatchTouchEvent(MotionEvent ev) {
        View v = getCurrentFocus();
        if(HideKeyBoard.isShouldHideInput(v, ev)) {
            hideSoftInput(v.getWindowToken());
            if(passwordEditText.getText().toString().length()>0) {
                passImageView.setVisibility(View.VISIBLE);
            }
            if(confirmPasswordEditText.getText().toString().length()>0) {
                if(confirmPasswordEditText.getText().toString().equals(passwordEditText.getText().toString())) {
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
    private void hideSoftInput(IBinder token) {
        if (token != null) {
            InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            manager.hideSoftInputFromWindow(token, InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }
}
