package com.cmsz.unitepay.webviewdemo;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {
    private EditText et;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        et = (EditText) findViewById(R.id.et);

    }

    public void onClick(View v) {
        Intent intent = new Intent(this, WebViewActivity.class);
        Bundle extras = new Bundle();
        extras.putString("url", et.getText().toString());
        intent.putExtras(extras);
        startActivity(intent);
    }
}
