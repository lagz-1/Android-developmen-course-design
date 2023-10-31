package com.example.tst;
import com.bumptech.glide.Glide;
import com.squareup.picasso.Picasso;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
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
import android.database.Cursor;
import android.provider.DocumentsContract;
import android.content.ContentUris;
import android.widget.Toast;

import org.opencv.android.OpenCVLoader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.FileOutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;

import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity{

    ImageView tv1;
    Button btn1, btn2,btnNext;

    Uri image_uri;


    //申请录音权限
    private static final int GET_RECODE_CAMERA = 1;
    private static String[] PERMISSION_CAMERA = {
            Manifest.permission.CAMERA
    };

    private static final int GET_RECODE_WRITE_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSION_WRITE_EXTERNAL_STORAGE = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };



    private static final int GET_RECODE_READ_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSION_READ_EXTERNAL_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

    public static final int TAKE_CAMERA = 101;
    public static final int PICK_PHOTO = 102;

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
     * 申请写外存权限*/
    public static void verifyWRITE_EXTERNAL_STORAGEPermissions(Activity activity) {
        int permission = ActivityCompat.checkSelfPermission(activity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, PERMISSION_WRITE_EXTERNAL_STORAGE,
                    GET_RECODE_WRITE_EXTERNAL_STORAGE);
        }
    }

    public static void verifyREAD_EXTERNAL_STORAGEPermissions(Activity activity) {
        int permission = ActivityCompat.checkSelfPermission(activity,
                Manifest.permission.READ_EXTERNAL_STORAGE);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, PERMISSION_READ_EXTERNAL_STORAGE,
                    GET_RECODE_READ_EXTERNAL_STORAGE);
        }
    }




    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        OpenCVLoader.initDebug();//忘记初始化了，损失惨重！

        tv1 = findViewById(R.id.tv1);
        btn1 = findViewById(R.id.btn1);//拍照按钮
        btn2 = findViewById(R.id.btn2);//调用相册按钮

        btnNext = findViewById(R.id.btnNext);
        btnNext.setVisibility(View.INVISIBLE);


        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                verifyCameraPermissions(MainActivity.this);

                File image_file = new File(getExternalCacheDir(),System.currentTimeMillis()+".jpg");//该方法其实并没有在内存中创建文件，所以还要创建文件

                try {
                    if(image_file.exists()){
                        image_file.delete();
                    }
                    image_file.createNewFile();//不加try_cache会报错
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }


                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    //大于等于版本24（7.0）的场合
                    image_uri = FileProvider.getUriForFile(
                            getApplicationContext(),
                            "com.example.tst.MainActivity.image_Uri",
                            image_file);
                } else {
                    //小于android 版本7.0（24）的场合
                    image_uri = Uri.fromFile(image_file);
                }



                Intent inte = new Intent("android.media.action.IMAGE_CAPTURE");
                inte.putExtra(MediaStore.EXTRA_OUTPUT,image_uri);

                startActivityForResult(inte,TAKE_CAMERA);
            }
        });

        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                            Manifest.permission.WRITE_EXTERNAL_STORAGE}, 101);
                } else {
                    //打开相册
                    Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
//                    intent.setType("sdcard/*"); //不可选择
                    intent.setType("image/*");
                    startActivityForResult(intent,PICK_PHOTO);
                }

            }
        });
//        btnNext.setOnClickListener(this);

    }




    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case TAKE_CAMERA:
                if (resultCode == RESULT_OK) {
                    Bitmap bitmap;
                    try {
                        bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(image_uri));
                        Log.e("aloha", String.valueOf(image_uri));
                        tv1.setImageBitmap(bitmap);//这下展示的就是一个全灰的图了

                        //Intent intentToAnalyse = new Intent(this, ImageAnalysis.class);
                        //intentToAnalyse.putExtra("image", bitmap);
                        //startActivity(intentToAnalyse);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
                break;

            case PICK_PHOTO:
                if (resultCode == RESULT_OK) { // 判断手机系统版本号
                    // 从相册返回的数据
                    Log.e(this.getClass().getName(), "Result:" + data.toString());
                    if (data != null) {
                        // 得到图片的全路径
                        Uri uri = data.getData();
                        tv1.setImageURI(uri);
                        Log.e(this.getClass().getName(), "Uri:" + String.valueOf(uri));
                    }

                }
                break;

            default:
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        //框架要求必须这么写
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);

    }


}