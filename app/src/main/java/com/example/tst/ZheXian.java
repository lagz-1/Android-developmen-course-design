package com.example.tst;


import android.Manifest;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.BufferedReader;
import java.io.CharArrayWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import lecho.lib.hellocharts.formatter.SimpleAxisValueFormatter;
import lecho.lib.hellocharts.formatter.SimpleLineChartValueFormatter;
import lecho.lib.hellocharts.gesture.ContainerScrollType;
import lecho.lib.hellocharts.gesture.ZoomType;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.ValueShape;
import lecho.lib.hellocharts.view.LineChartView;


public class ZheXian extends AppCompatActivity {


    private static final int REQUEST_STORAGE_PERMISSION = 1;

    private LineChartView chartView;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zhe_xian);
//        GetPromission();
        checkNeedPermissions();
        chartView = findViewById(R.id.chart);
        if (checkStoragePermission()) {
            try {
                readDataFromFile();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            requestStoragePermission();
        }
    }

    private boolean checkStoragePermission() {
        return ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                REQUEST_STORAGE_PERMISSION);
    }


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_STORAGE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                try {
                    readDataFromFile();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(this, "Storage permission denied.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void readDataFromFile() throws FileNotFoundException {
        // Uri  txtOutPutDir;
        //txtOutPutDir = Uri.parse("file://" + "/" + Environment.getExternalStorageDirectory() + "/txtFolder/"+"aaa.txt");
        String filePath = Environment.getExternalStorageDirectory()+ "/txtfolder/"+getCurrentDate()+".txt";
        Log.e("所以到底是什么",filePath);

        //String Path = Environment.getExternalStorageDirectory() + "/txtfolder";
        //String filePath = Environment.getExternalStorageDirectory()+"/txtFolder/aaa.txt" ;
        // FileReader fileReader=new FileReader(Path+File.separator+"aaa.txt");
        // String filePath = Path + File.separator + "aa2.txt";
        File file = new File(filePath);
        /*try (BufferedReader reader = new BufferedReader(new FileReader(file))) {*/
        try {
            // 创建文件输入流
            FileInputStream fis = new FileInputStream(file);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);
            List<PointValue> values = new ArrayList<>();
            List<AxisValue> axisValues = new ArrayList<>();

            int count = 1;
            String line;
            while ((line = br.readLine()) != null) {
                float value = Float.parseFloat(line);
                values.add(new PointValue(count++, value));
                axisValues.add(new AxisValue(count - 1).setLabel(String.valueOf(count - 1)));
            }
            drawChart(values, axisValues);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to read data from file.", Toast.LENGTH_SHORT).show();
        }
    }

    private void drawChart(List<PointValue> values, List<AxisValue> axisValues) {
        Line line = new Line(values)
                .setColor(ContextCompat.getColor(this, R.color.colorPrimary))
                .setShape(ValueShape.CIRCLE)
                .setCubic(false)
                .setHasPoints(true)
                .setFilled(false)
                .setHasLabels(true)
                .setHasLabelsOnlyForSelected(true)
                .setPointRadius(4);

        List<Line> lines = new ArrayList<>();
        lines.add(line);

        LineChartData data = new LineChartData();
        data.setLines(lines);

        Axis axisX = new Axis(axisValues)
                .setName("打开次数")
                .setTextColor(ContextCompat.getColor(this, R.color.colorPrimaryDark))
                .setMaxLabelChars(8)
                .setHasLines(true)
                .setHasTiltedLabels(false);
        data.setAxisXBottom(axisX);

        Axis axisY = new Axis()
                .setName("黑度")
                .setTextColor(ContextCompat.getColor(this, R.color.colorPrimaryDark))
                .setMaxLabelChars(6)
                .setHasLines(true)
                .setHasTiltedLabels(false);
        data.setAxisYLeft(axisY);

        chartView.setLineChartData(data);
        chartView.setVisibility(View.VISIBLE);
        chartView.setMaxZoom((float) 1.5);
        //chartView.setZoomType(ZoomType.HORIZONTAL_AND_VERTICAL);
        chartView.setZoomType(ZoomType.VERTICAL);
        chartView.setContainerScrollEnabled(true, ContainerScrollType.HORIZONTAL);
    }

    private String getCurrentDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd", Locale.CHINA);
        return dateFormat.format(new Date());
    }
    private void checkNeedPermissions(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            //多个权限一起申请
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
            }, 1);
        }
    }

    private void GetPromission(){
        //判断版本，（当前版本 >= 23） ,即大于6.0
        CharArrayWriter tvlog = null;



        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            List<String> plist = new ArrayList<>();
            plist = GetPromissionList();
            if (!plist.isEmpty()) {
                String[] ps = plist.toArray(new String[plist.size()]);
                ActivityCompat.requestPermissions(ZheXian.this, ps, 1);

            } else {
                //如果权限已经全部申请，也可以分开判断单个权限，
                //获取设备唯一标识符
                String DeviceId = ((TelephonyManager) getSystemService(ZheXian.this.TELEPHONY_SERVICE)).getDeviceId();
                tvlog.append("\r\nDeviceId:" + DeviceId);
            }
        }else{
            //如果设备版本低于6.0，则可自接获取设备唯一标识符
            String DeviceId = ((TelephonyManager) getSystemService(ZheXian.this.TELEPHONY_SERVICE)).getDeviceId();
            tvlog.append("\r\nDeviceId:" + DeviceId);
        }
    }

    //判断需要的权限，将未获取的权限集合成列表返回
    private List<String> GetPromissionList(){
        List<String> plist = new ArrayList<>();
        if(ContextCompat.checkSelfPermission(ZheXian.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            plist.add(Manifest.permission.CAMERA);
        }
        if(ContextCompat.checkSelfPermission(ZheXian.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            plist.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if (ActivityCompat.checkSelfPermission(ZheXian.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            plist.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (ActivityCompat.checkSelfPermission(ZheXian.this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            plist.add(Manifest.permission.READ_PHONE_STATE);
        }
        return plist;
    }
}