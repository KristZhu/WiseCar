package com.wisecarCompany.wisecarapp;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;

import com.wisecarCompany.wisecarapp.function.recordLog.RecordLogActivity;
import com.wisecarCompany.wisecarapp.function.serviceRecords.ServiceRecordsDashboardActivity;
import com.wisecarCompany.wisecarapp.user.login.LoginActivity;

public class MainActivity extends AppCompatActivity {

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //startActivity(new Intent(MainActivity.this, LoginActivity.class));
        startActivity(new Intent(MainActivity.this, ServiceRecordsDashboardActivity.class));
    }
}


