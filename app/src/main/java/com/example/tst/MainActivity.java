package com.example.tst;
import com.bumptech.glide.Glide;
import com.squareup.picasso.Picasso;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Environment;
import android.os.Bundle;
import android.util.Log;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;


import org.opencv.android.OpenCVLoader;

import java.io.File;
import java.io.IOException;
import java.io.FileOutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;

import pub.devrel.easypermissions.EasyPermissions;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    ImageView tv1;
    Button btn1, btn2,btnNext;

    Uri image_uri;

    private File cameraSavePath;
    private Uri uritempFile;
    private String photoName = System.currentTimeMillis() + ".jpg";

    Bitmap image = null;
    String imagePath;

    //申请拍照权限
    private static final int GET_RECODE_CAMERA = 1;
    private static String[] PERMISSION_CAMERA = {
            Manifest.permission.CAMERA
    };

    private static final int GET_RECODE_WRITE_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSION_WRITE_EXTERNAL_STORAGE = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };


    private static final int GET_RECODE_RECORD_AUDIO = 1;
    private static String[] PERMISSION_RECORD_AUDIO = {
            Manifest.permission.RECORD_AUDIO
    };






    /*
     * 申请拍照权限*/
    public static void verifyCameraPermissions(Activity activity) {
        int permission = ActivityCompat.checkSelfPermission(activity,
                Manifest.permission.CAMERA);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, PERMISSION_CAMERA,
                    GET_RECODE_CAMERA);
        }
    }

    /*
     * 申请录音权限*/
    public static void verifyAudioPermissions(Activity activity) {
        int permission = ActivityCompat.checkSelfPermission(activity,
                Manifest.permission.RECORD_AUDIO);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, PERMISSION_RECORD_AUDIO,
                    GET_RECODE_RECORD_AUDIO);
        }
    }


    //该方法，传入我们拿到的照片的 uri 进行激活 Android 系统的裁剪界面。我是在 onActivityResult 内进行调用该方法。
    private void photoClip(Uri uri) {
        // 调用系统中自带的图片剪裁
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
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

        uritempFile = Uri.parse("file://" + "/" + Environment.getExternalStorageDirectory().getPath() + "/" + System.currentTimeMillis() + ".jpg");
        Log.e("uritempFile", String.valueOf(uritempFile));
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uritempFile);
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        intent.putExtra("noFaceDetection", true);


        startActivityForResult(intent, 3);
    }


    //该方法 传入获取照片的 bitmap 和裁剪之后的照片名称，生成文件的保存路径，将其保存在了本地的根目录。
//    public String saveImage(String name, Bitmap bmp) {
//        File appDir = new File(Environment.getExternalStorageDirectory().getPath());
//        if (!appDir.exists()) {
//            appDir.mkdir();
//        }
//        String fileName = name + ".jpg";
//        File file = new File(appDir, fileName);
//        try {
//            FileOutputStream fos = new FileOutputStream(file);
//            bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
//            fos.flush();
//            fos.close();
//            return file.getAbsolutePath();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return null;
//    }


    /*
     * 申请写外存权限*/
    public static void verifyWRITE_EXTERNAL_STORAGEPermissions(Activity activity) {
        int permission = ActivityCompat.checkSelfPermission(activity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, PERMISSION_WRITE_EXTERNAL_STORAGE,
                    GET_RECODE_WRITE_EXTERNAL_STORAGE);
        }
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
            image_uri = FileProvider.getUriForFile(MainActivity.this, "com.example.tst.MainActivity.image_Uri", cameraSavePath);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } else {
            image_uri = Uri.fromFile(cameraSavePath);
        }
        intent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        MainActivity.this.startActivityForResult(intent, 1);
    }


    private void GoToAnalyse(){
        Intent intentToAnalyse = new Intent(this, ImageAnalysis.class);
        intentToAnalyse.putExtra("imagePath", imagePath);
        startActivity(intentToAnalyse);
    }



    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        OpenCVLoader.initDebug();//忘记初始化了，损失惨重!

        tv1 = findViewById(R.id.tv1);
        btn1 = findViewById(R.id.btn1);//拍照按钮
        btn2 = findViewById(R.id.btn2);//调用相册按钮

        btnNext = findViewById(R.id.btnNext);
            btnNext.setVisibility(View.INVISIBLE);



        //拍照照片路径
        cameraSavePath = new File(Environment.getExternalStorageDirectory().getPath() + "/" + photoName);


        btn1.setOnClickListener(this);
        btn2.setOnClickListener(this);
        btnNext.setOnClickListener(this);


        // 开启一个后台线程来检查bitmap对象



    }


    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.btn1:
                goCamera();
                break;
            case R.id.btn2:
                goPhotoAlbum();
                break;
            case R.id.btnNext:
                GoToAnalyse();
                break;
        }

    }




    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);

        String photoPath;
//        Bitmap image = null;

        if (requestCode == 1 && resultCode == RESULT_OK) {
            // 检查是否是拍照的回调

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                // 如果设备的Android版本高于N（Android 7.0+）

                // 获取拍照后的照片路径
                photoPath = String.valueOf(cameraSavePath);
                // 调用 photoClip 方法裁剪照片
                photoClip(Uri.fromFile(cameraSavePath));
            } else {
                // 如果设备的Android版本低于N

                // 获取拍照后的照片路径
                photoPath = image_uri.getEncodedPath();
                // 调用 photoClip 方法裁剪照片
                photoClip(image_uri);
            }

            Log.d("Photos", photoPath);
            // 使用 Glide 将照片加载到 ImageView 中
            Glide.with(MainActivity.this).load(photoPath).into(tv1);

        } else if (requestCode == 2 && resultCode == RESULT_OK) {
            // 检查是否是从相册选择照片的回调

            // 获取相册选定的照片的路径
            photoPath = getPhotoFromPhotoAlbum.getRealPathFromUri(this, data.getData());
            Log.d("相册返回图片路径:", photoPath);
            // 调用 photoClip 方法裁剪相册照片
            photoClip(data.getData());
            // 使用 Glide 将照片加载到 ImageView 中
            Glide.with(MainActivity.this).load(photoPath).into(tv1);
        } else if (requestCode == 3 && resultCode == RESULT_OK) {
            // 检查是否是裁剪后的回调

            Bundle bundle = data.getExtras();

            if (bundle != null) {
                // 在这里获得了裁剪后的 Bitmap 对象，可以用于上传或其他操作

                image = bundle.getParcelable("data");
//                 也可以执行一些保存、压缩等操作后再上传
//                String path = saveImage("图片", image);
//                Log.e("notmywrong",path);

                // 将 Bitmap 设置到 ImageView 上
                tv1.setImageBitmap(image);

            }

            Picasso.with(MainActivity.this).load(uritempFile).into(tv1);
            File file = null;
            try {
                file = new File(new URI(uritempFile.toString()));
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
            //照片路径
            imagePath = Objects.requireNonNull(file).getPath();
            Log.e("notnot",imagePath);

        }
        super.onActivityResult(requestCode, resultCode, data);


        btnNext.setVisibility(View.VISIBLE);

    }








    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        //框架要求必须这么写
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);



    }




}