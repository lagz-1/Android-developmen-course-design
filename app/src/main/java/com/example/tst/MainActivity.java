package com.example.tst;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.yalantis.ucrop.UCrop;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.imgproc.Imgproc;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity{

    ImageView tv1;
    Button btn1, btn2,btn3,btnNext;

    Button btnReturn, btnAnalyse, btnOutPut,btnShare;

    Uri image_uri,fileUri;
    Uri resultUri;

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

    private static final int GET_RECODE_MANAGE_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSION_MANAGE_EXTERNAL_STORAGE = {
            Manifest.permission.MANAGE_EXTERNAL_STORAGE
    };

    public static final int TAKE_CAMERA = 101;
    public static final int PICK_PHOTO = 102;
    public static final int CROP_REQUEST_CODE = 103;

    Bitmap bitmapAnalyse;
    String[] comparedPic = new String[]{"white", "pic_20_rank1", "pic_40_rank2", "pic_60_rank3", "pic_80_rank4", "black"};

    private SharedPreferences sPre,sPreInt;


    String photoName;

    int flag = -1;


    int Results_of_analysis = -2;

    private int buttonCallCount;

    File image_file;

    public Bitmap getDrawable(String name) {
        ApplicationInfo appInfo = getApplicationInfo();
        int resID = getResources().getIdentifier(name, "drawable", appInfo.packageName);
        //解析资源文件夹下，id为resID的图片
        return BitmapFactory.decodeResource(getResources(), resID);
    }

    public Mat getDrawableToMat(Bitmap bitmap) {
        Mat matOp = new Mat(bitmap.getHeight(), bitmap.getWidth(), CvType.CV_8U);
        Utils.bitmapToMat(bitmap, matOp);
        return matOp;

    }



    public static double compareHist(Mat src_1, Mat src_2) {//src_1:matOp  src_2:matCompared

//        Imgproc.cvtColor(src_2, src_2,Imgproc.COLOR_BGR2GRAY);//转化为灰度图

//        Imgproc.resize(src_1, src_1, src_2.size());
//        Imgproc.resize(src_2, src_2, src_1.size());

        Mat hvs_1 = new Mat();
        Mat hvs_2 = new Mat();
//        //图片转HSV
        Imgproc.cvtColor(src_1, hvs_1, Imgproc.COLOR_BGR2HSV);
        Imgproc.cvtColor(src_2, hvs_2, Imgproc.COLOR_BGR2HSV);


        List<Mat> channels1 = new ArrayList<>();
        Core.split(hvs_1, channels1); // 将HSV图像拆分为通道
        Mat hist_1 = channels1.get(0); // 提取色调通道

        List<Mat> channels2 = new ArrayList<>();
        Core.split(hvs_1, channels2); // 将HSV图像拆分为通道
        Mat hist_2 = channels2.get(0); // 提取色调通道


//        Mat hist_1 = new Mat();
//        Mat hist_2 = new Mat();

        //直方图计算
        Imgproc.calcHist(Stream.of(src_1).collect(Collectors.toList()), new MatOfInt(0), new Mat(), hist_1, new MatOfInt(255), new MatOfFloat(0, 256));
        Imgproc.calcHist(Stream.of(src_2).collect(Collectors.toList()), new MatOfInt(0), new Mat(), hist_2, new MatOfInt(255), new MatOfFloat(0, 256));

        //图片归一化
        Core.normalize(hist_1, hist_1, 1, hist_1.rows(), Core.NORM_MINMAX, -1, new Mat());
        Core.normalize(hist_2, hist_2, 1, hist_2.rows(), Core.NORM_MINMAX, -1, new Mat());

        //直方图比较
//        double a = Imgproc.compareHist(hist_1,hist_1,Imgproc.CV_COMP_CORREL);
        double b = Imgproc.compareHist(hist_1, hist_2, Imgproc.CV_COMP_CORREL);
//        System.out.println("越接近1越相识度越高");
//        System.out.println("同一张图片\t比较结果(相识度)："+a);
//        System.out.println("不同图片\t比较结果(相识度)："+b);

        return b;

    }


    public int Analyse(Bitmap bitmap) {
        Mat matOp = new Mat(bitmap.getHeight(), bitmap.getWidth(), CvType.CV_8U);
        Utils.bitmapToMat(bitmap, matOp);
//        Imgproc.cvtColor(matOp, matOp, Imgproc.COLOR_BGR2GRAY); // 转换为灰度图

        double maxsimilarity = -2;

        for (int i = 0; i < 6; i++) {
            Mat matCompared = getDrawableToMat(getDrawable(comparedPic[i]));

            double Hist = compareHist(matOp, matCompared);
            Log.e("hhhh", String.valueOf(Hist)+" "+comparedPic[i]);
            if (Hist > maxsimilarity) {
                flag = i;
                maxsimilarity = Hist;
            }
        }
        return flag;
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

    public static void verifyMANAGE_ALL_FILES_ACCESSPermissions(Activity activity) {
        int permission = ActivityCompat.checkSelfPermission(activity,
                Manifest.permission.MANAGE_EXTERNAL_STORAGE);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, PERMISSION_MANAGE_EXTERNAL_STORAGE,
                    GET_RECODE_MANAGE_EXTERNAL_STORAGE);
        }
    }

    private void GoToAnalyse(){

        btn1.setVisibility(View.INVISIBLE);
        btn2.setVisibility(View.INVISIBLE);
        btn3.setVisibility(View.INVISIBLE);
        btnShare.setVisibility(View.INVISIBLE);
        btnNext.setVisibility(View.INVISIBLE);

        btnAnalyse.setVisibility(View.VISIBLE);
        btnReturn.setVisibility(View.VISIBLE);
    }



    private void createTXTFolder(){
        // 获取内存文件夹路径
        String folderPath = Environment.getExternalStorageDirectory() + "/txtFolder";
        File folder = new File(folderPath);
        if (!folder.exists()){
            folder.mkdirs(); // 创建文件夹
        }

    }





    // 在按钮点击事件处理代码中调用下面的方法
    private void saveIntData() {

        verifyWRITE_EXTERNAL_STORAGEPermissions(MainActivity.this);
//        createTXTFolder();

        String folderPath = Environment.getExternalStorageDirectory() + "/txtFolder";
        File folder = new File(folderPath);
        if (!folder.exists()){
            folder.mkdirs(); // 创建文件夹
        }

        // 获取当前日期
        Date currentDate = Calendar.getInstance().getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd", Locale.CHINA);
        String currentDateString = sdf.format(currentDate);
        Log.e("today",currentDateString);


        // 获取存储的日期
        String savedDateString = sPre.getString("LastResetDate", "");


        // 如果当前日期与存储的日期不同，说明已经过了一天，重置按钮被调用的次数
        if (!currentDateString.equals(savedDateString)) {
            buttonCallCount = 0; // 重置按钮被调用的次数
            // 保存当前日期作为上次重置日期
            SharedPreferences.Editor editor = sPre.edit();
            editor.putString("LastResetDate", currentDateString);
            editor.apply();

        }


//        txtOutPutDir = Uri.parse("file://" + "/" + Environment.getExternalStorageDirectory().getPath() + "/txtFolder/"+ currentDateString + ".txt");

        String filePath = Environment.getExternalStorageDirectory() + "/txtFolder/"+currentDateString+".txt";


        Log.e("txtOutPutDir", filePath);


        // 构建完整的文件路径
        File txtFile = null;
        try {
            txtFile = new File(filePath);
//            txtFile = new File(new URI(txtOutPutDir.toString()));
            if (!txtFile.exists()) {
                txtFile.createNewFile();
            }
//        } catch (URISyntaxException | IOException e) {
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(txtFile, true)));
            out.write(String.valueOf(Results_of_analysis)); // 将 int 数据转换为字符串并写入文件
            out.write(System.lineSeparator()); // 添加换行符
            out.close();


            // 增加按钮被调用的次数
            buttonCallCount++;

            SharedPreferences.Editor editorInt = sPreInt.edit();
            editorInt.putInt("CounterValue", buttonCallCount);
            editorInt.apply();

            Log.e("btnInfo",String.valueOf(buttonCallCount));
        } catch (IOException e) {
            e.printStackTrace();
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
        btn3 = findViewById(R.id.btn3);//调用裁剪按钮
        btn3.setVisibility(View.INVISIBLE);

        btnNext = findViewById(R.id.btnNext);
        btnNext.setVisibility(View.INVISIBLE);



        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                verifyCameraPermissions(MainActivity.this);

                photoName = System.currentTimeMillis()+".jpg";
                image_file = new File(getExternalCacheDir(),photoName);//该方法其实并没有在内存中创建文件，所以还要创建文件

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

        btn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                verifyWRITE_EXTERNAL_STORAGEPermissions(MainActivity.this);

                if(image_uri!=null){
                    String filename = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CHINA)
                            .format(new Date()) + ".jpg";

                    UCrop.of(image_uri, fileUri)
                            .start(MainActivity.this);

                    Uri mDestinationUri = Uri.fromFile(new File(getCacheDir(),filename));

                    UCrop uCrop = UCrop.of(image_uri, mDestinationUri);
                    uCrop.start(MainActivity.this);

                }
                else{
                    Toast.makeText(MainActivity.this, "请先选择照片!", Toast.LENGTH_SHORT).show();
                }


            }
        });


        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GoToAnalyse();
            }
        });


        btnReturn = findViewById(R.id.btnReturn);
        btnReturn.setVisibility(View.INVISIBLE);
        btnReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                image_uri = null;

                btnReturn.setVisibility(View.INVISIBLE);
                btnAnalyse.setVisibility(View.INVISIBLE);
                btnOutPut.setVisibility(View.INVISIBLE);

                btn1.setVisibility(View.VISIBLE);
                btn2.setVisibility(View.VISIBLE);
                btnNext.setVisibility(View.INVISIBLE);


                tv1.setImageBitmap(null);

            }
        });


        btnAnalyse = findViewById(R.id.btnAnalyse);
        btnAnalyse.setVisibility(View.INVISIBLE);
        btnAnalyse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Results_of_analysis = Analyse(bitmapAnalyse);
                Log.e("hdjhf", String.valueOf(Results_of_analysis));
                btnOutPut.setVisibility(View.VISIBLE);
            }
        });

        btnOutPut = findViewById(R.id.btnOutPut);
        btnOutPut.setVisibility(View.INVISIBLE);
        btnOutPut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveIntData();
                btnOutPut.setVisibility(View.INVISIBLE);
            }
        });

        btnShare = findViewById(R.id.btnShare);
        btnShare.setVisibility(View.INVISIBLE);
        btnShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String path =Environment.getExternalStorageDirectory()+File.separator;//sd根目录
                 Intent intent = new Intent(Intent.ACTION_SEND);
                 intent.setType("image/*");
                 intent.putExtra(Intent.EXTRA_STREAM, image_uri);
                 startActivity(Intent.createChooser(intent, "Share"));
            }
        });


// 初始化 SharedPreferences
        sPre = getSharedPreferences("Date", Context.MODE_PRIVATE);
        sPreInt = getSharedPreferences("count", Context.MODE_PRIVATE);
        buttonCallCount = sPreInt.getInt("CounterValue", 0);



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
                        bitmapAnalyse = bitmap;
                        tv1.setImageBitmap(bitmap);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    btn3.setVisibility(View.VISIBLE);
                    btnShare.setVisibility(View.VISIBLE);
                    btnNext.setVisibility(View.VISIBLE);
                }
                break;

            case PICK_PHOTO:
                if (resultCode == RESULT_OK) { // 判断手机系统版本号
                    // 从相册返回的数据
                    Log.e(this.getClass().getName(), "Result:" + data.toString());
                    if (data != null) {
                        // 得到图片的全路径
                        image_uri = data.getData();
                        try {
                            bitmapAnalyse = BitmapFactory.decodeStream(getContentResolver().openInputStream(image_uri));
                        } catch (FileNotFoundException e) {
                            throw new RuntimeException(e);
                        }
//                        tv1.setImageURI(image_uri);
                        tv1.setImageBitmap(bitmapAnalyse);
                        Log.e("okiamfine", "Uri:" + String.valueOf(image_uri));
                    }
                    btn3.setVisibility(View.VISIBLE);
                    btnShare.setVisibility(View.VISIBLE);
                    btnNext.setVisibility(View.VISIBLE);
                }
                break;

            case UCrop.REQUEST_CROP:     //调用剪裁后返回
            {
                if (resultCode == RESULT_OK) {
//                    final Uri resultUri = UCrop.getOutput(data);
                    resultUri = UCrop.getOutput(data);
                    if(resultUri==null){Log.e("whoyouare", "whoyouare");}
                        else {Log.e("whoyouare", String.valueOf(resultUri));}

                        Bitmap bitmap;
                    try {
                        bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(resultUri));

                    } catch (FileNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                    tv1.setImageBitmap(bitmap);

                }
            }
                break;

            case UCrop.RESULT_ERROR:
            {
                final Throwable cropError = UCrop.getError(data);
                Log.e("gg","gg了");
            }
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