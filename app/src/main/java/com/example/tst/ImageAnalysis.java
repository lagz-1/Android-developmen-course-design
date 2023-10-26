package com.example.tst;

import static java.lang.Double.max;

import com.example.tst.R;
import com.squareup.picasso.Picasso;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
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
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Objects;

public class ImageAnalysis extends AppCompatActivity {

    Button btnReturn,btnAnalyse;
    ImageView tv2;
    File imgForAnalyse = null;

    String str = new String();

    Bitmap bitmapAnalyse;
    String[] comparedPic = new String[]{"white","pic_20_rank1","pic_40_rank2","pic_60_rank3","pic_80_rank4","black"};

    int flag = -1;
    public Bitmap getDrawable(String name)
    {
        ApplicationInfo appInfo = getApplicationInfo();
        int resID = getResources().getIdentifier(name, "drawable", appInfo.packageName);
        //解析资源文件夹下，id为resID的图片
        return BitmapFactory.decodeResource(getResources(),  resID);
    }

    public Mat getDrawableToMat(Bitmap bitmap)
    {
        Mat matOp = new Mat(bitmap.getHeight(), bitmap.getWidth(), CvType.CV_8U);
        Utils.bitmapToMat(bitmap,matOp);
        return matOp;

    }

    // 计算结构相似性指数（SSIM）
    private static double calculateSSIM(Mat image1, Mat image2) {
        MatOfFloat ssimMat = new MatOfFloat();
        Imgproc.matchTemplate(image1, image2, ssimMat, Imgproc.CV_COMP_CORREL);
        Scalar ssimScalar = Core.mean(ssimMat);
        return ssimScalar.val[0];
    }







    public double Analyse(Bitmap bitmap){
        Mat matOp = new Mat(bitmap.getHeight(), bitmap.getWidth(), CvType.CV_8U);
        Utils.bitmapToMat(bitmap, matOp);
        Imgproc.cvtColor(matOp, matOp, Imgproc.COLOR_BGR2GRAY); // 转换为灰度图

        double maxsimilarity = 0;


        for(int i = 0;i < 6;i++){
            Mat matCompared = getDrawableToMat(getDrawable(comparedPic[i]));
            Imgproc.cvtColor(matCompared, matCompared, Imgproc.COLOR_BGR2GRAY); // 转换为灰度图


            Imgproc.resize(matOp, matOp, matCompared.size());
            Imgproc.resize(matCompared, matCompared, matOp.size());

            double ssim = calculateSSIM(matOp, matCompared);
            Log.e("(SSIM)", String.valueOf(ssim));

            if(ssim>=maxsimilarity){
                flag = i;
                maxsimilarity = ssim;
            }
        }
        return maxsimilarity*flag;
    }



    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_analysis);

        tv2 = findViewById(R.id.tv2);


        Intent intent = getIntent();
        if(intent!=null)
        {

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
        btnReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //返回上个界面
                finish();
            }
        });


        btnAnalyse = findViewById(R.id.btnAnalyse);
        btnAnalyse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Analyse(bitmapAnalyse);
            }
        });


}
}