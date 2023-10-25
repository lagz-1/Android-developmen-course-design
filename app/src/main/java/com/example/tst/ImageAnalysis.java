package com.example.tst;

import com.example.tst.R;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

public class ImageAnalysis extends AppCompatActivity {

    Button btnReturn;
    ImageView tv2;
    //Bitmap bitmapCompared = null;



    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_analysis);

        tv2 = findViewById(R.id.tv2);

        Intent intent = getIntent();
        if(intent!=null)
        {
            Bitmap bitmapCompared = (Bitmap) intent.getParcelableExtra("image");
            Log.d("MyTag", "成功传输了");
            tv2.setImageBitmap(bitmapCompared);
        }

        btnReturn = findViewById(R.id.btnReturn);
        btnReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //返回上个界面
                finish();
            }
        });





}
}