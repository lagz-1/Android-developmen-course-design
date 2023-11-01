package com.example.tst;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.TableLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;



public class FenXi extends AppCompatActivity {

    private static final String TAG = FenXi.class.getSimpleName();
    private TableLayout tableLayout;
    private TextView tvOpenCount, tvBlackness, tvDataAnalysis,tvcolor;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fen_xi);

        tableLayout = findViewById(R.id.tableLayout);
        tvOpenCount = findViewById(R.id.tvOpenCount);
        tvBlackness = findViewById(R.id.tvBlackness);
        tvDataAnalysis = findViewById(R.id.tvDataAnalysis);
        tvcolor = findViewById(R.id.tvcolor);

        loadFileData();
    }

    private void loadFileData() {
        String filePath = Environment.getExternalStorageDirectory()+ "/txtfolder/"+getCurrentDate()+".txt";
        // String filePath = Environment.getExternalStorageDirectory()+ "/txtfolder/aaa.txt";
        File file = new File(filePath);
        try {
            FileInputStream fis = new FileInputStream(file);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);
            // InputStream inputStream = getAssets().open("data.txt");
            //  BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            // 读取黑度数值
            String blacknessString = br.readLine();
            float blackness = Float.parseFloat(blacknessString);
            tvBlackness.setText(String.valueOf(blackness));
            tvDataAnalysis.setText(getDataAnalysis(blackness));
            tvcolor.setText(getcolor(blackness));

            // 计算文档打开次数（示例中为黑度数值乘以2）
            int openCount = (int) (blackness * 2);
            tvOpenCount.setText(String.valueOf(openCount));

            br.close();
        } catch (IOException e) {
            Log.e(TAG, "Error reading data from file", e);
        }
    }
    private String getCurrentDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd", Locale.CHINA);
        return dateFormat.format(new Date());
    }
    private String getcolor(float blackness) {
        if (blackness == 0) {
            return "全白";
        } else if (blackness > 0 && blackness <= 1) {
            return "微灰";
        } else if (blackness > 1 && blackness <= 2) {
            return "灰";
        } else if (blackness > 2 && blackness <= 3) {
            return "深灰";
        } else if (blackness > 3 && blackness <= 4) {
            return "灰黑";
        } else if (blackness > 4 && blackness <= 5) {
            return "全黑";
        }

        return "-";
    }

    private String getDataAnalysis(float blackness) {
        if (blackness == 0) {
            return "优。不存在烟气污染，对公众的健康没有任何危害。烟尘量为0克/米。";
        } else if (blackness > 0 && blackness <= 1) {
            return "良。烟气污染被认为是可以接受的，除极少数对某种污染物特别敏感的人以外，对公众健康没有危害。烟尘量为0.25克/米。";
        } else if (blackness > 1 && blackness <= 2) {
            return "轻微污染。对污染物比较敏感的人群，他们的健康状况会受到影响，但对健康人群基本没有影响。烟尘量为0.7克/米。";
        } else if (blackness > 2 && blackness <= 3) {
            return "轻度污染。几乎每个人的健康都会受到影响，对敏感人群的不利影响尤为明显。烟尘量为1.2克/米。";
        } else if (blackness > 3 && blackness <= 4) {
            return "中度重污染。每个人的健康都会受到比较严重的影响。烟尘量为2.3克/米。";
        } else if (blackness > 4 && blackness <= 5) {
            return "重度污染。所有人的健康都会受到严重影响.烟尘量为4～5克/米。";
        }

        return "-";
    }
}
