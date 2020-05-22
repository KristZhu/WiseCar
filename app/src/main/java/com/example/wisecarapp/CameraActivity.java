package com.example.wisecarapp;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.content.PermissionChecker;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.wisecarapp.CircleImageView;
import com.example.wisecarapp.MainActivity;
import com.example.wisecarapp.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import pub.devrel.easypermissions.EasyPermissions;


public class CameraActivity extends Activity {

    public static final String TAG = "camera";
    public static final int TAKE_PHOTO = 1;
    public static final int GROP_PHOTO = 2;
    public static final int CHOOSE_PHOTO = 3;
    private static final int CAMERA_RESULT_CUT_OVER = 4;
    private Button takePhoto;
    private Button choose;
    private ImageView picture;
    private String imagePath;
    private File imageFile;
    private Uri imageUri;

    private static final int MULTI_PERMISSION_CODE = 0;
    private static final int PERMISSION_EXTERNAL_STORAGE_REQUEST_CODE = 1;
    private static final int PERMISSION_CAMERA_REQUEST_CODE = 2;

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
                    imageUri=Uri.fromFile(outputImage);
                    Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
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
        setContentView(R.layout.activity_camera);

        takePhoto = (Button) findViewById(R.id.take_photo);
        picture = (ImageView) findViewById(R.id.picture);

        takePhoto.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                int permissionCheckCamera = ContextCompat.checkSelfPermission(CameraActivity.this, Manifest.permission.CAMERA);
                int permissionCheckStorage = ContextCompat.checkSelfPermission(CameraActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
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
                            CameraActivity.this,
                            new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            MULTI_PERMISSION_CODE
                    );
                }
                else if(permissionCheckCamera == PackageManager.PERMISSION_DENIED) {
                    Log.d(TAG, "onClickPermissionRequestCamera: ");
                    ActivityCompat.requestPermissions(
                            CameraActivity.this,
                            new String[]{Manifest.permission.CAMERA},
                            PERMISSION_CAMERA_REQUEST_CODE
                    );
                } else if(permissionCheckStorage == PackageManager.PERMISSION_DENIED) {
                    Log.d(TAG, "onClickPermissionRequestStorage: ");
                    ActivityCompat.requestPermissions(
                            CameraActivity.this,
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
                    imageUri=Uri.fromFile(outputImage);
                    Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                    startActivityForResult(intent, TAKE_PHOTO);
                    //启动照相机
                }
            }
        });

        Button chooseFromAlbum = findViewById(R.id.choose_from_album);
        chooseFromAlbum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(ContextCompat.checkSelfPermission(CameraActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(
                            CameraActivity.this,
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
                    imageUri = Uri.fromFile(outputImage);
                    Intent intent=new Intent("android.intent.action.GET_CONTENT");
                    intent.setType("image/*");
                    intent.putExtra("crop",true);
                    intent.putExtra("scale",true);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT,imageUri);
/*
                    if(Build.VERSION.SDK_INT>=19){//4.4及以上系统
                        handleImageOnKitKat(intent);
                    }else{//4.4以下系统
                        handleImageBeforeKitKat(intent);
                    }
                    startActivityForResult(intent,GROP_PHOTO);
*/
                    startActivityForResult(intent,CHOOSE_PHOTO);
                }
            }
        });

    }



    @Override
    public void onActivityResult(int requestCode,int resultCode,Intent data) {
        switch(requestCode){
            case TAKE_PHOTO:
                if(resultCode==RESULT_OK){
                    //Bitmap bitmap = BitmapFactory.decodeFile(imagePath,null);
                    //picture.setImageBitmap(bitmap);

                    Intent intent = new Intent("com.android.camera.action.CROP");
                    intent.setDataAndType(imageUri, "image/**");
                    intent.putExtra("scale", true);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                    //displayImage(imageUri.toString());
                    startActivityForResult(intent, GROP_PHOTO);
                }
                break;

            case GROP_PHOTO: //剪裁程序 似乎有bug 不确定是不是由于模拟器
                if(resultCode == RESULT_OK){
                    /*
                    Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,imageUri);
                    sendBroadcast(intent);
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Uri systemImageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                    ContentResolver contentResolver = getContentResolver();
                    Cursor cursor = contentResolver.query(systemImageUri, null,
                            MediaStore.Images.Media.DISPLAY_NAME + "='"
                                    + imageFile.getName() + "'", null, null);
                    Uri photoUriInMedia = null;
                    if (cursor != null && cursor.getCount() > 0) {
                        cursor.moveToLast();
                        long id = cursor.getLong(0);
                        photoUriInMedia = ContentUris.withAppendedId(systemImageUri, id);
                    }
                    cursor.close();
                    Intent in = new Intent("com.android.camera.action.CROP");
                    //需要裁减的图片格式
                    in.setDataAndType(photoUriInMedia, "image/*");
                    //允许裁减
                    in.putExtra("crop", "true");
                    //剪裁后ImageView显时图片的宽
                    in.putExtra("outputX", 250);
                    //剪裁后ImageView显时图片的高
                    in.putExtra("outputY", 250);
                    //设置剪裁框的宽高比例
                    in.putExtra("aspectX", 1);
                    in.putExtra("aspectY", 1);
                    in.putExtra("return-data", true);
                    startActivityForResult(in, CAMERA_RESULT_CUT_OVER);
                    */

                    try{
                        Bitmap bitmap= BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
                        picture.setImageBitmap(bitmap);
                        //将剪裁后的照片显示出来
                    }catch(FileNotFoundException e){
                        e.printStackTrace();
                    }

                }
                break;

                /*
            case CAMERA_RESULT_CUT_OVER:
                if(data != null){
                    Bitmap bitmap = (Bitmap) data.getExtras().get("data");
                    picture.setImageBitmap(bitmap);
                }

                 */

            case CHOOSE_PHOTO:
                if(resultCode==RESULT_OK){
                    if(Build.VERSION.SDK_INT>=19){//4.4及以上系统
                        handleImageOnKitKat(data);
                    }else{//4.4以下系统
                        handleImageBeforeKitKat(data);
                    }
                }
                data.putExtra("scale", true);
                data.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
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
        String imagePath=null;
        Uri imageUri=data.getData();
        if(DocumentsContract.isDocumentUri(this,imageUri)){
            //document类型的Uri处理方式
            String docId=DocumentsContract.getDocumentId(imageUri);
            if("com.android.providers.media.documents".equals(imageUri.getAuthority())){
                String id=docId.split(":")[1];
                String seletion=MediaStore.Images.Media._ID+"="+id;
                imagePath=getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,seletion);
            }else if("com.android.providers.downloads.documents".equals(imageUri.getAuthority())){
                Uri contentUri= ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"),Long.valueOf(docId));
                imagePath=getImagePath(contentUri,null);
            }
        }else if("content".equalsIgnoreCase(imageUri.getScheme())){
            //content类型的Uri处理方式
            imagePath=getImagePath(imageUri,null);
        }else if("file".equalsIgnoreCase(imageUri.getScheme())){
            //file类型的Uri，直接获取路径即可
            imagePath=imageUri.getPath();
        }
        displayImage(imagePath);
        //startActivityForResult(data, GROP_PHOTO);
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
            Bitmap bitmap=BitmapFactory.decodeFile(imagePath);
            picture.setImageBitmap(bitmap);
        }else{
            Toast.makeText(this,"failed to get image",Toast.LENGTH_SHORT).show();
        }
    }

}




/*
public class CameraActivity extends AppCompatActivity {
    private ImageView mHeader_iv;
    //相册请求码
    private static final int ALBUM_REQUEST_CODE = 1;
    //相机请求码
    private static final int CAMERA_REQUEST_CODE = 2;
    //剪裁请求码
    private static final int CROP_REQUEST_CODE = 3;
    //调用照相机返回图片文件
    private File tempFile;

    private Button mGoCamera_btn;
    private Button mGoAlbm_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        mHeader_iv = (ImageView) findViewById(R.id.mHeader_iv);
        mGoCamera_btn = (Button) findViewById(R.id.mGoCamera_btn);
        mGoAlbm_btn = (Button) findViewById(R.id.mGoAlbm_btn);
        //mGoCamera_btn.setOnClickListener(this);
        //mGoAlbm_btn.setOnClickListener(this);

        mGoCamera_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getPicFromCamera();
            }
        });

        mGoAlbm_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getPicFromAlbm();
            }
        });
    }

    //从相机获取图片
    private void getPicFromCamera() {
        //用于保存调用相机拍照后所生成的文件
        tempFile = new File(Environment.getExternalStorageDirectory().getPath(), System.currentTimeMillis() + ".jpg");
        //跳转到调用系统相机
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        //使用FileProvider获取Uri
        intent.setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        Uri contentUri = FileProvider.getUriForFile(CameraActivity.this, "com.hansion.chosehead", tempFile);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, contentUri);
        Log.e("dasd", contentUri.toString());

        startActivityForResult(intent, CAMERA_REQUEST_CODE);
    }

    //从相册获取图片
    private void getPicFromAlbm() {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, ALBUM_REQUEST_CODE);
    }

    //裁剪图片
    private void cropPhoto(Uri uri) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        intent.setDataAndType(uri, "image/*");
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("outputX", 300);
        intent.putExtra("outputY", 300);
        intent.putExtra("return-data", true);
        startActivityForResult(intent, CROP_REQUEST_CODE);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        switch (requestCode) {
            case CAMERA_REQUEST_CODE:   //调用相机后返回
                if (resultCode == RESULT_OK) {
                    //用相机返回的照片去调用剪裁也需要对Uri进行处理
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        Uri contentUri = FileProvider.getUriForFile(CameraActivity.this, "com.hansion.chosehead", tempFile);
                        cropPhoto(contentUri);
                    } else {
                        cropPhoto(Uri.fromFile(tempFile));
                    }
                }
                break;
            case ALBUM_REQUEST_CODE:    //调用相册后返回
                if (resultCode == RESULT_OK) {
                    Uri uri = intent.getData();
                    cropPhoto(uri);
                }
                break;
            case CROP_REQUEST_CODE:     //调用剪裁后返回
                Bundle bundle = intent.getExtras();
                if (bundle != null) {
                    //在这里获得了剪裁后的Bitmap对象，可以用于上传
                    Bitmap image = bundle.getParcelable("data");
                    //设置到ImageView上
                    mHeader_iv.setImageBitmap(image);
                    //也可以进行一些保存、压缩等操作后上传
//                    String path = saveImage("crop", image);
                }
                break;
        }
    }
    public String saveImage(String name, Bitmap bmp) {
        File appDir = new File(Environment.getExternalStorageDirectory().getPath());
        if (!appDir.exists()) {
            appDir.mkdir();
        }
        String fileName = name + ".jpg";
        File file = new File(appDir, fileName);
        try {
            FileOutputStream fos = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            fos.close();
            return file.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}

*/

/*
public class CameraActivity extends AppCompatActivity implements View.OnClickListener, EasyPermissions.PermissionCallbacks  {

    private CircleImageView ivTest;

    private File cameraSavePath;//拍照照片路径
    private Uri uri;
    private String[] permissions = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};

    private String photoName = System.currentTimeMillis() + ".jpg";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        Button btnGetPicFromCamera = findViewById(R.id.btn_get_pic_from_camera);
        Button btnGetPicFromPhotoAlbum = findViewById(R.id.btn_get_pic_form_photo_album);
        Button btnGetPermission = findViewById(R.id.btn_get_Permission);
        ivTest = findViewById(R.id.iv_test);

        btnGetPicFromCamera.setOnClickListener(this);
        btnGetPicFromPhotoAlbum.setOnClickListener(this);
        btnGetPermission.setOnClickListener(this);

        cameraSavePath = new File(Environment.getExternalStorageDirectory().getPath() + "/" + photoName);



    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.btn_get_pic_from_camera:
                goCamera();
                break;
            case R.id.btn_get_pic_form_photo_album:
                goPhotoAlbum();
                break;
            case R.id.btn_get_Permission:
                getPermission();
                break;
        }
    }

    //获取权限
    private void getPermission() {
        if (EasyPermissions.hasPermissions(this, permissions)) {
            //已经打开权限
            Toast.makeText(this, "已经申请相关权限", Toast.LENGTH_SHORT).show();
        } else {
            //没有打开相关权限、申请权限
            EasyPermissions.requestPermissions(this, "需要获取您的相册、照相使用权限", 1, permissions);
        }

    }


    private void photoClip(Uri uri) {
        // 调用系统中自带的图片剪裁
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        // 下面这个crop=true是设置在开启的Intent中设置显示的VIEW可裁剪
        intent.putExtra("crop", "true");
        // aspectX aspectY 是宽高的比例
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        // outputX outputY 是裁剪图片宽高
        intent.putExtra("outputX", 150);
        intent.putExtra("outputY", 150);
        intent.putExtra("return-data", true);
        startActivityForResult(intent, 3);


    }


    //激活相册操作
    private void goPhotoAlbum() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, 2);
    }

    //激活相机操作
    private void goCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            uri = FileProvider.getUriForFile(CameraActivity.this, "com.example.hxd.pictest.fileprovider", cameraSavePath);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } else {
            uri = Uri.fromFile(cameraSavePath);
        }
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        CameraActivity.this.startActivityForResult(intent, 1);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //框架要求必须这么写
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }


    //成功打开权限
    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {

        Toast.makeText(this, "相关权限获取成功", Toast.LENGTH_SHORT).show();
    }

    //用户未同意权限
    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        Toast.makeText(this, "请同意相关权限，否则功能无法使用", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        String photoPath;
        if (requestCode == 1 && resultCode == RESULT_OK) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                photoPath = String.valueOf(cameraSavePath);
                photoClip(Uri.fromFile(cameraSavePath));
            } else {
                photoPath = uri.getEncodedPath();
                photoClip(uri);
            }
            Log.d("拍照返回图片路径:", photoPath);
            Glide.with(CameraActivity.this).load(photoPath).into(ivTest);
        } else if (requestCode == 2 && resultCode == RESULT_OK) {
            photoPath = getPhotoFromPhotoAlbum.getRealPathFromUri(this, data.getData());
            Log.d("相册返回图片路径:", photoPath);
            photoClip(data.getData());
            Glide.with(CameraActivity.this).load(photoPath).into(ivTest);
        } else if (requestCode == 3 && resultCode == RESULT_OK) {
            Bundle bundle = data.getExtras();

            if (bundle != null) {
                //在这里获得了剪裁后的Bitmap对象，可以用于上传
                Bitmap image = bundle.getParcelable("data");
                //设置到ImageView上
                ivTest.setImageBitmap(image);
                //也可以进行一些保存、压缩等操作后上传
                String path = saveImage("头像", image);
                Log.d("裁剪路径:", path);
            }


        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    public String saveImage(String name, Bitmap bmp) {
        File appDir = new File(Environment.getExternalStorageDirectory().getPath());
        if (!appDir.exists()) {
            appDir.mkdir();
        }
        String fileName = name + ".jpg";
        File file = new File(appDir, fileName);
        try {
            FileOutputStream fos = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            fos.close();
            return file.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}


 */



