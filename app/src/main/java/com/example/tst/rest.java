package com.example.tst;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class rest extends AppCompatActivity {

    Button toshow,toMainAcitivity;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rest);

        toshow = findViewById(R.id.toshow);
        toshow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = null;
                intent = new Intent(rest.this, show.class);
                startActivity(intent);
            }
        });

        toMainAcitivity = findViewById(R.id.toMainAcitivity);
        toMainAcitivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = null;
                intent = new Intent(rest.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }
}