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
import org.opencv.core.Core;
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

    public static Bitmap Gray(Bitmap bitmap){//转化bitmap为mat并将mat转化为灰度图像，又转化回去
        Mat img = new Mat(bitmap.getHeight(), bitmap.getWidth(), CvType.CV_8UC3);
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

        //膨胀操作，用来找到背景区域
        Mat background = new Mat();
        Imgproc.dilate(opening, background, kernel, new Point(-1, -1), 2);


        //腐蚀操作，用于找到前景区域
        Mat foreground = new Mat();
        Imgproc.erode(opening, foreground, kernel, new Point(-1, -1), 2);


        /*
        * 距离变换：
        *    dist_transform：使用cv2.distanceTransform计算图像中每个像素到最近背景像素的距离。
        *    通过归一化将其值映射到0到1之间。
        *
        * */
        Mat distTransform = new Mat();
        Imgproc.distanceTransform(opening, distTransform, Imgproc.DIST_L2, 5);
        Core.normalize(distTransform, distTransform, 0, 1.0, Core.NORM_MINMAX);

//二值化
        Mat fgThresholded = new Mat();
        Imgproc.threshold(distTransform, fgThresholded, 0.5 * Core.minMaxLoc(distTransform).maxVal, 255, Imgproc.THRESH_BINARY);
//        Imgproc.threshold(distTransform, foreground, 0.5 * Core.minMaxLoc(distTransform).maxVal, 255, Imgproc.THRESH_BINARY);


// 计算未知区域
        Mat unknown = new Mat();
        Core.subtract(background, fgThresholded, unknown,new Mat(),CvType.CV_32S);
//        unknown.convertTo(unknown,CvType.CV_8UC4);


// 进行连通组件分析
        /*
*        connectedComponents
*public static int connectedComponents(Mat image, Mat labels, int connectivity, int ltype)
*参数一：image，待标记的单通道图像，数据类型必须为CV_8U。
*参数二：labels，标记连通域后的输出图像，与输入图像具有相同的尺寸。
*参数三：connectivity，标记连通域时使用的邻域种类，4表示4-邻域，8表示8-邻域。
*参数四：ltype，输出图像的数据类型，目前支持CV_32S和CV_16U两种数据类型。
*/

        Mat labels = new Mat(bitmap.getHeight(),bitmap.getWidth(),CvType.CV_8U);//省略的参数 ：connectivity = 8，ltype = CV_32S
        foreground.convertTo(foreground,CvType.CV_8UC4);//服了，把fgThresholded换为foregound终于成功了，类目
        Imgproc.connectedComponents(foreground, labels);


// 标记前景，背景和未知区域
//        for (int i = 0; i < labels.rows(); i++) {
//            for (int j = 0; j < labels.cols(); j++) {
//                double[] label = labels.get(i, j);
//                if (label[0] == 1) {
//                    fgThresholded.put(i, j, 0);
//                    background.put(i, j, 1);
//                } else if (label[0] > 1) {
//                    fgThresholded.put(i, j, 1);
//                    background.put(i, j, 0);
//                }
//            }
//        }
//
        for (int i = 0; i < labels.rows(); i++) {
            for (int j = 0; j < labels.cols(); j++) {
                double[] label = labels.get(i, j);
                labels.put(i,j,(int)label[0]+1);
            }
        }


// 修改标签以标记未知区域
        for (int i = 0; i < unknown.rows(); i++) {
            for (int j = 0; j < unknown.cols(); j++) {
                double[] u = unknown.get(i, j);
                if (u[0] == 255) {
                    labels.put(i, j, (int)0);
                }
            }
        }
        unknown.convertTo(unknown,CvType.CV_8UC4);
        labels.convertTo(labels,CvType.CV_8UC4);
//
//        // 应用分水岭算法
//
//        labels.convertTo(labels, CvType.CV_32SC1);
//        Imgproc.watershed(img, labels);
//
//
//        // 创建一个用于掩码的Mat，将抠图区域标记为255，背景标记为0
//        Mat mask = new Mat(img.rows(), img.cols(), CvType.CV_8UC4, new Scalar(0));
//        for (int i = 0; i < labels.rows(); i++) {
//            for (int j = 0; j < labels.cols(); j++) {
//                double[] markerValue = labels.get(i, j);
//                if (markerValue[0] > 1) {
//                    mask.put(i, j, (int)255);
//                }
//            }
//        }
//
//// 使用掩码来提取抠图区域
//        Mat Chosen = new Mat();
//        Core.bitwise_and(img, img, Chosen, mask);
//
//// 在这里，coins包含了抠图后的图像


        Utils.matToBitmap(unknown,bitmap);
//        Utils.matToBitmap(Chosen,bitmap);
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