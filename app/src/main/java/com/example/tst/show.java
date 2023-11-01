package com.example.tst;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;

public class show extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show);

        // 线性图按钮
        findViewById(R.id.lineChartButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(show.this, ZheXian.class);
                startActivity(intent);
            }
        });

        // 柱状图按钮
        findViewById(R.id.analyseButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(show.this, FenXi.class);
                startActivity(intent);
            }
        });
    }
}