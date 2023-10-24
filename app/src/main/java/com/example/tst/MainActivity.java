package com.example.tst;

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
import android.os.Bundle;
import android.util.Log;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    ImageView tv1;
    Button btn1;

    Uri image_uri;

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




    static final int REQUEST_READ_EXTERNAL_STORAGE_CODE = 3;

    public static Bitmap Gray(Bitmap bitmap) {//转化bitmap为mat并将mat转化为灰度图像，又转化回去
        Mat matOp = new Mat(bitmap.getHeight(), bitmap.getWidth(), CvType.CV_8UC1);
        Utils.bitmapToMat(bitmap, matOp);
        Imgproc.cvtColor(matOp, matOp, Imgproc.COLOR_BGR2GRAY); // 转换为灰度图

        //应该用INV，把原来暗的地方变为前景，也就是烟雾变为前景，我们要提取的东西
        Imgproc.threshold(matOp, matOp, 0, 255, Imgproc.THRESH_BINARY_INV | Imgproc.THRESH_OTSU);
//        Imgproc.threshold(matOp, matOp, 127, 255, Imgproc.THRESH_BINARY | Imgproc.THRESH_OTSU);

        // 定义一个3x3的结构元素（kernel）
        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(5, 5));

        // 使用开运算来去除毛边和小噪点
        Mat opening = new Mat();
        Imgproc.morphologyEx(matOp, opening, Imgproc.MORPH_OPEN, kernel, new Point(-1, -1), 2);

        // opening 现在包含了经过开运算处理后的图像

        Utils.matToBitmap(opening,bitmap);
        //Utils.matToBitmap(matOp,bitmap);
        return bitmap;
    }

    public Bitmap calc(Bitmap bitmap){

        Mat matOp = new Mat(bitmap.getHeight(), bitmap.getWidth(), CvType.CV_8UC1);
        Utils.bitmapToMat(bitmap, matOp);

        // 应用大津二值化
        //Mat binaryImage = matOp.clone();
        //Imgproc.threshold(binaryImage, binaryImage, 0, 255, Imgproc.THRESH_BINARY | Imgproc.THRESH_OTSU);
        Imgproc.threshold(matOp, matOp, 0, 255, Imgproc.THRESH_BINARY_INV | Imgproc.THRESH_OTSU);
        //Imgproc.threshold(matOp, matOp, 127, 255, Imgproc.THRESH_BINARY | Imgproc.THRESH_OTSU);

        // 应用高斯模糊以降低噪音
        //Imgproc.GaussianBlur(matOp, matOp, new Size(9, 9), 0);

        // 应用阈值分割以分割烟雾
//        Mat thresholded = new Mat();
//        Imgproc.threshold(matOp, thresholded, 90, 255, Imgproc.THRESH_BINARY);

        // 执行形态学操作（腐蚀和膨胀）
//        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(25, 25));
//        Imgproc.morphologyEx(thresholded, thresholded, Imgproc.MORPH_CLOSE, kernel);
//        Imgproc.erode(thresholded, thresholded, new Mat(), new Point(-1, -1), 4);
//        Imgproc.dilate(thresholded, thresholded, new Mat(), new Point(-1, -1), 4);

        // 查找轮廓
//        List<MatOfPoint> contours = new ArrayList<>();
//        Mat hierarchy = new Mat();
//        Imgproc.findContours(thresholded, contours, hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);

        // 绘制轮廓或提取分割的烟雾区域
//        Mat resultMat = new Mat();
//        Imgproc.drawContours(resultMat, contours, -1, new Scalar(0, 0, 255), 3);

        // 你可以显示结果或保存它
        //Bitmap resultBitmap = Bitmap.createBitmap(resultMat.cols(), resultMat.rows(), Bitmap.Config.ARGB_8888);
        //Utils.matToBitmap(resultMat, resultBitmap);

        Utils.matToBitmap(matOp,bitmap);
        //Utils.matToBitmap(binaryImage,bitmap);
        return bitmap;
    }



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




        public static void PerREAD_EXTERNAL_STORAGE(MainActivity mainActivity)
    {
        //读取相册
        if (ContextCompat.checkSelfPermission(mainActivity, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(mainActivity, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_READ_EXTERNAL_STORAGE_CODE);
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



        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                verifyCameraPermissions(MainActivity.this);
                verifyAudioPermissions(MainActivity.this);

                File image_file = new File(getExternalCacheDir(),"temp.jpg");//该方法其实并没有在内存中创建文件，所以还要创建文件
                if(image_file.exists()){
                    image_file.delete();
                }
                try {
                    image_file.createNewFile();//不加try_cache会报错
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                image_uri = FileProvider.getUriForFile(
                        getApplicationContext(),
                        "com.example.tst.MainActivity.image_Uri",
                        image_file);

                Intent inte = new Intent("android.media.action.IMAGE_CAPTURE");
                inte.putExtra(MediaStore.EXTRA_OUTPUT,image_uri);

                startActivityForResult(inte,1);

            }
        });

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case  1:
                if(resultCode  == RESULT_OK){
                    Bitmap bitmap = null;
                    try{
                        bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(image_uri));
                        bitmap = Gray(bitmap);
                        //bitmap = calc(bitmap);

                        tv1.setImageBitmap(bitmap);//这下展示的就是一个全灰的图了

                        //Intent intentToAnalyse = new Intent(this, ImageAnalysis.class);
                        //intentToAnalyse.putExtra("image", bitmap);
                        //startActivity(intentToAnalyse);
                    }catch(FileNotFoundException e){
                        e.printStackTrace();
                    }

                }
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode){
            case 1:
                break;
            default:break;
        }



    }




}