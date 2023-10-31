package com.example.tst;

import static java.lang.Double.max;
import static java.lang.Double.valueOf;

import com.example.tst.R;
import com.squareup.picasso.Picasso;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

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
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.Calendar;
import java.util.Date;



public class ImageAnalysis extends AppCompatActivity implements View.OnClickListener {

    Button btnReturn, btnAnalyse, btnOutPut;
    ImageView tv2;
    File imgForAnalyse = null;

    String str = new String();

    Bitmap bitmapAnalyse;
    String[] comparedPic = new String[]{"white", "pic_20_rank1", "pic_40_rank2", "pic_60_rank3", "pic_80_rank4", "black"};

    // 在类中定义一个成员变量用于记录按钮被调用的次数

    private SharedPreferences sPre,sPreInt;

    private int buttonCallCount;
    private  Uri txtOutPutDir;

    private static final int GET_RECODE_WRITE_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSION_WRITE_EXTERNAL_STORAGE = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };


    int flag = -1;


    int Results_of_analysis = -2;

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

//    // 计算结构相似性指数（SSIM）
//    private static double calculateSSIM(Mat image1, Mat image2) {
//        MatOfFloat ssimMat = new MatOfFloat();
//        Imgproc.matchTemplate(image1, image2, ssimMat, Imgproc.CV_COMP_CORREL);
//        Scalar ssimScalar = Core.mean(ssimMat);
//        return ssimScalar.val[0];
//    }


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
     * 申请写外存权限*/
    public static void verifyWRITE_EXTERNAL_STORAGEPermissions(Activity activity) {
        int permission = ActivityCompat.checkSelfPermission(activity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, PERMISSION_WRITE_EXTERNAL_STORAGE,
                    GET_RECODE_WRITE_EXTERNAL_STORAGE);
        }
    }


    // 创建文件夹
    private void createTXTFolder() {
        String folderPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/txtFolder";

        File folder = new File(folderPath);

        if (!folder.exists()) {
            // 如果文件夹不存在，创建文件夹
            if (folder.mkdirs()) {

                // 文件夹创建成功
            } else {
                // 文件夹创建失败
            }
        }
        Log.i("txtFolder",folder.getAbsolutePath());

    }








    // 在按钮点击事件处理代码中调用下面的方法
    private void saveIntData() {

        verifyWRITE_EXTERNAL_STORAGEPermissions(ImageAnalysis.this);
        createTXTFolder();


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


        txtOutPutDir = Uri.parse("file://" + "/" + Environment.getExternalStorageDirectory().getPath() + "/txtFolder/"+ currentDateString + ".txt");

        Log.e("txtOutPutDir", String.valueOf(txtOutPutDir));


        // 构建完整的文件路径
        File txtFile = null;
        try {
            txtFile = new File(new URI(txtOutPutDir.toString()));
            if (!txtFile.exists()) {
                txtFile.createNewFile();
            }
        } catch (URISyntaxException | IOException e) {
            e.printStackTrace();
        }

        try {
            // 写入 double 数据到文件
//            FileWriter writer = new FileWriter(txtFile);
//            writer.write(String.valueOf(Results_of_analysis)); // 将 double 数据转换为字符串并写入文件
//            writer.close();

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
        setContentView(R.layout.activity_image_analysis);

        tv2 = findViewById(R.id.tv2);


        Intent intent = getIntent();
        if (intent != null) {

            str = intent.getStringExtra("imagePath");
            imgForAnalyse = new File(str);

            if (imgForAnalyse.exists()) {
                // 文件存在
                bitmapAnalyse = BitmapFactory.decodeFile(imgForAnalyse.getAbsolutePath());
                if (bitmapAnalyse != null) {

                    tv2.setImageBitmap(bitmapAnalyse);
                } else {
                    // 解码失败
                    Toast.makeText(this, "文件格式转换失败！", Toast.LENGTH_SHORT).show();
                }
            } else {
                // 文件不存在
                Toast.makeText(this, "文件不存在！", Toast.LENGTH_SHORT).show();
            }
        }

        btnReturn = findViewById(R.id.btnReturn);
        btnReturn.setOnClickListener(this);


        btnAnalyse = findViewById(R.id.btnAnalyse);
        btnAnalyse.setOnClickListener(this);

        btnOutPut = findViewById(R.id.btnOutPut);
        btnOutPut.setVisibility(View.INVISIBLE);
        btnOutPut.setOnClickListener(this);


// 初始化 SharedPreferences
        sPre = getSharedPreferences("Date", Context.MODE_PRIVATE);
        sPreInt = getSharedPreferences("count", Context.MODE_PRIVATE);
        buttonCallCount = sPreInt.getInt("CounterValue",0);
    }


    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.btnReturn:
                finish();
                break;
            case R.id.btnAnalyse:
                Results_of_analysis = Analyse(bitmapAnalyse);
                btnOutPut.setVisibility(View.VISIBLE);
                break;
            case R.id.btnOutPut:
                saveIntData();
                btnOutPut.setVisibility(View.INVISIBLE);
                break;
        }
    }

}