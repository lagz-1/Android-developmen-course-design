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
import android.icu.lang.UCharacter;
import android.os.Build;
import android.os.Environment;
import android.os.Bundle;
import android.util.Log;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import androidx.fragment.app.Fragment;
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
import java.io.FileOutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;

import com.example.tst.getPhotoFromPhotoAlbum;

import pub.devrel.easypermissions.EasyPermissions;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    ImageView tv1;
    Button btn1, btn2;

    Uri image_uri;

    private File cameraSavePath;
    private Uri uritempFile;
    private String photoName = System.currentTimeMillis() + ".jpg";

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
        Mat img = new Mat(bitmap.getHeight(), bitmap.getWidth(), CvType.CV_8UC3);
        Mat matOp = new Mat(bitmap.getHeight(), bitmap.getWidth(), CvType.CV_8UC1);
        Utils.bitmapToMat(bitmap, matOp);
        Imgproc.cvtColor(matOp, matOp, Imgproc.COLOR_BGR2GRAY); // 转换为灰度图

        //应该用INV，把原来暗的地方变为前景，也就是烟雾变为前景，我们要提取的东西
        Imgproc.threshold(matOp, matOp, 0, 255, Imgproc.THRESH_BINARY_INV | Imgproc.THRESH_OTSU);
        //        Imgproc.threshold(matOp, matOp, 127, 255, Imgproc.THRESH_BINARY | Imgproc.THRESH_OTSU);

        // 定义一个3x3的结构元素（kernel）
        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3, 3));

        // 使用开运算来去除毛边和小噪点
        Mat opening = new Mat();
        Imgproc.morphologyEx(matOp, opening, Imgproc.MORPH_OPEN, kernel, new Point(-1, -1), 2);

        // opening 现在包含了经过开运算处理后的图像

        //膨胀操作，用来找到背景区域
        Mat background = new Mat();
        Imgproc.dilate(opening, background, kernel, new Point(-1, -1), 1);


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
        Core.subtract(background, fgThresholded, unknown, new Mat(), CvType.CV_32S);
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

        Mat labels = new Mat(bitmap.getHeight(), bitmap.getWidth(), CvType.CV_8U);//省略的参数 ：connectivity = 8，ltype = CV_32S
        foreground.convertTo(foreground, CvType.CV_8UC4);//服了，把fgThresholded换为foregound终于成功了，类目
        Imgproc.connectedComponents(foreground, labels);


        Core.add(labels, new Scalar(1), labels);


// 修改标签以标记未知区域
//        for (int i = 0; i < unknown.rows(); i++) {
//            for (int j = 0; j < unknown.cols(); j++) {
//                double[] u = unknown.get(i, j);
//                if (u[0] == 255) {
//                    labels.put(i, j, (int)0);
//                }
//            }
//        }
        unknown.convertTo(unknown, CvType.CV_8UC4);
        labels.convertTo(labels, CvType.CV_8UC4);
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


        Utils.matToBitmap(unknown, bitmap);
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
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uritempFile);
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        intent.putExtra("noFaceDetection", true);


        startActivityForResult(intent, 3);
    }


    //该方法 传入获取照片的 bitmap 和裁剪之后的照片名称，生成文件的保存路径，将其保存在了本地的根目录。
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


    public static void PerREAD_EXTERNAL_STORAGE(MainActivity mainActivity) {
        //读取相册
        if (ContextCompat.checkSelfPermission(mainActivity, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(mainActivity, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_READ_EXTERNAL_STORAGE_CODE);
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






    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        OpenCVLoader.initDebug();//忘记初始化了，损失惨重！

        tv1 = findViewById(R.id.tv1);
        btn1 = findViewById(R.id.btn1);//拍照按钮
        btn2 = findViewById(R.id.btn2);//调用相册按钮


        //拍照照片路径
        cameraSavePath = new File(Environment.getExternalStorageDirectory().getPath() + "/" + photoName);


        btn1.setOnClickListener(this);
        btn2.setOnClickListener(this);

//        btn1.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//                verifyCameraPermissions(MainActivity.this);
//                verifyAudioPermissions(MainActivity.this);
//
//                File image_file = new File(getExternalCacheDir(),"temp.jpg");//该方法其实并没有在内存中创建文件，所以还要创建文件
//                if(image_file.exists()){
//                    image_file.delete();
//                }
//                try {
//                    image_file.createNewFile();//不加try_cache会报错
//                } catch (IOException e) {
//                    throw new RuntimeException(e);
//                }
//
//                image_uri = FileProvider.getUriForFile(
//                        getApplicationContext(),
//                        "com.example.tst.MainActivity.image_Uri",
//                        image_file);
//
//                Intent inte = new Intent("android.media.action.IMAGE_CAPTURE");
//                inte.putExtra(MediaStore.EXTRA_OUTPUT,image_uri);
//
//                startActivityForResult(inte,1);
//
//            }
//        });


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
        }
    }


//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        switch (requestCode) {
//            case 1:
//                if (resultCode == RESULT_OK) {
//                    Bitmap bitmap = null;
//                    try {
//                        bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(image_uri));
//                        bitmap = Gray(bitmap);
//                        //bitmap = calc(bitmap);
//
//                        tv1.setImageBitmap(bitmap);//这下展示的就是一个全灰的图了
//
//                        //Intent intentToAnalyse = new Intent(this, ImageAnalysis.class);
//                        //intentToAnalyse.putExtra("image", bitmap);
//                        //startActivity(intentToAnalyse);
//                    } catch (FileNotFoundException e) {
//                        e.printStackTrace();
//                    }
//
//                }
//        }
//    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        String photoPath;

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

            Log.d("拍照返回图片路径:", photoPath);
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
                Bitmap image = bundle.getParcelable("data");
                // 将 Bitmap 设置到 ImageView 上
                tv1.setImageBitmap(image);
                // 也可以执行一些保存、压缩等操作后再上传
                String path = saveImage("头像", image);
                Log.d("裁剪路径:", path);
            }

            Picasso.with(MainActivity.this).load(uritempFile).into(tv1);
            File file = null;
            try {
                file = new File(new URI(uritempFile.toString()));
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
            //照片路径
            String path = Objects.requireNonNull(file).getPath();

        }
        super.onActivityResult(requestCode, resultCode, data);
    }








    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        //框架要求必须这么写
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);



    }




}