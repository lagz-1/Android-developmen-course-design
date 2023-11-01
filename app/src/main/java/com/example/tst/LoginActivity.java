package com.example.tst;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class LoginActivity extends AppCompatActivity {
    //声明控件
    //登陆界面的控件
    EditText user_name;
    EditText user_password;

    Button login,regist,logout;

    //声明数据库
    Mysql mysql;
    SQLiteDatabase db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        //找到当且xml文件内的控件ID
        //数据编辑框的ID
        user_name = this.findViewById(R.id.user_name);
        user_password = this.findViewById(R.id.user_password);
        //按键属性的ID
        login = this.findViewById(R.id.login);
        regist = this.findViewById(R.id.regist);
        logout=this.findViewById(R.id.logout);

        //取出数据库内的数据
        mysql = new Mysql(this,"Userinfo",null,1);
        db = mysql.getReadableDatabase();
        //登录按键按下之后处理的事情
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //需要获取的输入的用户名和密码
                String storage_username = user_name.getText().toString();//用户控件.得到数据.转换为字符串；
                String storage_userpassword = user_password.getText().toString();//用户控件.得到数据.转换为字符串；

                //查询用户名和密码相同的数据
                Cursor cursor = db.query("logins",new String[]{"usname","uspwd"}," usname=? and uspwd=?",
                        new String[]{storage_username,storage_userpassword},null,null,null);
                int flag = cursor.getCount(); //查询出来的记录项的条数，若没有该用户则为0条
                //登录成功后响应的数据
                if (flag!=0){
                    Toast.makeText(getApplicationContext(), "登录成功！", Toast.LENGTH_SHORT).show();//显示登录成功的弹窗，简单写法
                    Intent intent = null;  //这个变量初始申明为空
                    intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                }
                else {
                    //假设正确的账号和密码分别是（VIP账号，没有写入数据库，无需注册账号）
                    if (storage_username.equals("DPT") && storage_userpassword.equals("123456")) {
                        //如果正确
                        Toast.makeText(getApplicationContext(), "超级VIP登录成功！", Toast.LENGTH_SHORT).show();//显示登录成功的弹窗，简单写法
                        Intent intent = null;  //这个变量初始申明为空
                        intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                    }
                    else{
                        Toast.makeText(getApplicationContext(), "用户名输入错误或密码不正确，请重新登录！", Toast.LENGTH_SHORT).show();//获取显示的内容
                    }
                }

            }
        });
        //注册按键按下之后，响应的事件
        regist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //实现界面跳转，从登录界面跳转到注册界面
                Intent intent = null;  //这个变量初始申明为空
                intent = new Intent(LoginActivity.this, RegisterActivity.class);//跳转界面
                startActivity(intent);
            }
        });

        logout.setOnClickListener(new View.OnClickListener() {     //为退出按钮添加监听事件实现退出（用到弹框提示确认退出）
            @Override
            public void onClick(View v) {
                //创建弹框对象,显示在当前页面
                AlertDialog.Builder ab = new AlertDialog.Builder(LoginActivity.this);
                //编辑弹框样式
                ab.setTitle("提示");                   //创建标题
                ab.setIcon(R.mipmap.ic_launcher_round);   //设置图标
                ab.setMessage("您是否确定退出？");         //设置内容
                //设置按钮
                ab.setPositiveButton("取消",null);
                ab.setNeutralButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 实现程序的退出，结束当前
                        LoginActivity.this.finish();
                    }
                });
                //创建弹框
                ab.create();
                //显示弹框
                ab.show();
            }
        });
    }

    //2秒内点击两次返回键退出
    long exittime;    //设定退出时间间隔
    public boolean onKeyDown(int keyCode, KeyEvent event){   //参数：按的键；按键事件
        //判断事件触发
        if (keyCode == KeyEvent.KEYCODE_BACK){
            //判断两次点击间隔时间
            if((System.currentTimeMillis()-exittime)>2000){
                Toast.makeText(LoginActivity.this,
                        "再次返回程序退出！",Toast.LENGTH_SHORT).show();
                exittime = System.currentTimeMillis();    //设置第一次点击时间
            }else{
                finish();
                System.exit(0);
            }
            return true;
        }
        return super.onKeyDown(keyCode,event);
    }
}
