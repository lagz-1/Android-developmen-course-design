package com.example.tst;

import android.view.View;


// 创建按钮点击事件的回调类
public class Listener implements View.OnClickListener {
    private btnListener callback;

    public Listener(btnListener callback) {
        this.callback = callback;
    }

    @Override
    public void onClick(View v) {
        // 按钮点击时执行回调方法
        callback.onMyButtonClick();
    }
}
