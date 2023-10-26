package com.example.tst;

import com.example.tst.R;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.io.File;

public class ImageAnalysis extends AppCompatActivity {

    Button btnReturn,btnAnalyse;
    ImageView tv2;
    File imgForAnalyse = null;

    String str = new String();

    Bitmap bitmapAnalyse;

    public double Analyse(Bitmap bitmap){
        Mat matOp = new Mat(bitmap.getHeight(), bitmap.getWidth(), CvType.CV_8UC4);
        Utils.bitmapToMat(bitmap, matOp);
        Utils.bitmapToMat(bitmap, matOp);
        Imgproc.cvtColor(matOp, matOp, Imgproc.COLOR_BGR2GRAY); // 转换为灰度图	        Imgproc.cvtColor(matOp, matOp, Imgproc.COLOR_BGR2GRAY); // 转换为灰度图

        int resourceIdWHITE = getResources().getIdentifier("white", "drawable", getPackageName());
        if (resourceIdWHITE != 0) {
            // 如果找到资源标识符，则加载图片
            tv2.setImageResource(resourceIdWHITE);
        } else {
            // 如果未找到资源，可以采取适当的处理措施，例如显示默认图片或显示错误消息
        }

        int resourceIdBLACK = getResources().getIdentifier("black", "drawable", getPackageName());
        if (resourceIdBLACK != 0) {
            // 如果找到资源标识符，则加载图片
            tv2.setImageResource(resourceIdBLACK);
        } else {
            // 如果未找到资源，可以采取适当的处理措施，例如显示默认图片或显示错误消息
        }


        return 0.1;
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