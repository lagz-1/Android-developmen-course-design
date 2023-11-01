package com.example.tst;



        import androidx.appcompat.app.AppCompatActivity;

        import android.content.ContentValues;
        import android.content.Intent;
        import android.content.SharedPreferences;
        import android.database.sqlite.SQLiteDatabase;
        import android.os.Bundle;
        import android.view.View;
        import android.widget.Button;
        import android.widget.EditText;
        import android.widget.Toast;

public class RegisterActivity extends AppCompatActivity {

    //注册界面的控件
    Button zhuce_success;//注册界面的按键
    EditText zhuce_user_name;//注册界面的用户名
    EditText zhuce_user_password; //注册界面的密码
    EditText zhuce_user_password_again; //注册界面的密

    Mysql mysql;
    SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //寻找控件ID
        zhuce_success = this.findViewById(R.id.zhuce_success);
        zhuce_user_name = this.findViewById(R.id.zhuce_user_name);
        zhuce_user_password = this.findViewById(R.id.zhuce_user_password);
        zhuce_user_password_again= this.findViewById(R.id.zhuce_user_password_again);

        mysql = new Mysql(this,"Userinfo",null,1);      //建数据库
        db = mysql.getReadableDatabase();
        zhuce_success.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //保存注册数据的字符串
                String name = zhuce_user_name.getText().toString();          //用户名
                String pwd01 = zhuce_user_password.getText().toString();      //密码
                String pwd02 = zhuce_user_password_again.getText().toString(); //二次输入的密码
                //判断注册内容
                if(name.equals("")||pwd01 .equals("")||pwd02.equals("")){
                    //显示弹窗
                    Toast.makeText(getApplicationContext(),"用户名或密码不能为空！！",Toast.LENGTH_SHORT).show();
                }
                else {
                    //如果注册时第一次输入的密码和第二次输入的密码一致
                    if(pwd01.equals(pwd02)){
                        //ContentValues是一种基本的存储类型
                        ContentValues cv = new ContentValues();
                        //放入数据
                        cv.put("usname",name);
                        cv.put("uspwd",pwd01);
                        db.insert("logins",null,cv);
                        //从当前界面跳转到登录页面
                        Intent intent = new Intent();
                        intent.setClass(RegisterActivity.this,LoginActivity.class);
                        startActivity(intent);
                        //弹窗
                        Toast.makeText(getApplicationContext(),"账号注册成功！！",Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Toast.makeText(getApplicationContext(),"两次输入的密码不一致！！",Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }
}