package com.wisecarCompany.wisecarapp.user.introduction;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import com.wisecarCompany.wisecarapp.R;
import com.wisecarCompany.wisecarapp.user.vehicle.VehicleActivity;

public class AboutActivity extends AppCompatActivity {

    ImageButton backImageButton;
    TextView linkTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        backImageButton = (ImageButton) findViewById(R.id.backImageButton);
        backImageButton.setOnClickListener(v -> startActivity(new Intent(this, VehicleActivity.class)));

        linkTextView = (TextView) findViewById(R.id.linkTextView);
        linkTextView.setOnClickListener(v -> {

        });

    }
}
