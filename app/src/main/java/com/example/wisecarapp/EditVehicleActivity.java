package com.example.wisecarapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class EditVehicleActivity extends AppCompatActivity {

    private final static String TAG = "EditVehicle";

    private ImageButton backImageButton;

    private TextView registrationTextView;
    private TextView serviceTextView;

    private LinearLayout servicesLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_vehicle);

        backImageButton = (ImageButton) findViewById(R.id.backImageButton);
        backImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        registrationTextView = (TextView) findViewById(R.id.registrationCheckBox);
        serviceTextView = (TextView) findViewById(R.id.serviceTextView);

        servicesLayout = (LinearLayout) findViewById(R.id.servicesLayout);

        List<Integer> services = new LinkedList<>();
        services.add(1);
        services.add(2);
        services.add(3);
        services.add(6);

        if(services.size()==0) {

        } else if(services.size()%2==0) {
            while (services.size()>0) {
                int left = services.get(0);
                services.remove(0);
                int right = services.get(0);
                services.remove(0);
                LinearLayout servicesLineLayout = new LinearLayout(EditVehicleActivity.this);
                ImageView leftImageView = new ImageView(EditVehicleActivity.this);
                switch (left) {
                    case 0: leftImageView.setImageDrawable(getResources().getDrawable(R.drawable.edit_vehicle0service_button)); break;
                    case 1: leftImageView.setImageDrawable(getResources().getDrawable(R.drawable.edit_vehicle0driver_button)); break;
                    case 2: leftImageView.setImageDrawable(getResources().getDrawable(R.drawable.edit_vehicle0registration_button)); break;
                    case 3: leftImageView.setImageDrawable(getResources().getDrawable(R.drawable.edit_vehicle0parking_button)); break;
                    case 4: leftImageView.setImageDrawable(getResources().getDrawable(R.drawable.edit_vehicle0insurance_button)); break;
                    case 5: leftImageView.setImageDrawable(getResources().getDrawable(R.drawable.edit_vehicle0toll_button)); break;
                    case 6: leftImageView.setImageDrawable(getResources().getDrawable(R.drawable.edit_vehicle0fuel_button)); break;
                }
                LinearLayout.LayoutParams leftParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                leftParams.setMargins(0, 0, 16, 0);
                leftImageView.setLayoutParams(leftParams);
                servicesLineLayout.addView(leftImageView);
                ImageView rightImageView = new ImageView(EditVehicleActivity.this);

                switch (left) {
                    case 0: rightImageView.setImageDrawable(getResources().getDrawable(R.drawable.edit_vehicle0service_button)); break;
                    case 1: rightImageView.setImageDrawable(getResources().getDrawable(R.drawable.edit_vehicle0driver_button)); break;
                    case 2: rightImageView.setImageDrawable(getResources().getDrawable(R.drawable.edit_vehicle0registration_button)); break;
                    case 3: rightImageView.setImageDrawable(getResources().getDrawable(R.drawable.edit_vehicle0parking_button)); break;
                    case 4: rightImageView.setImageDrawable(getResources().getDrawable(R.drawable.edit_vehicle0insurance_button)); break;
                    case 5: rightImageView.setImageDrawable(getResources().getDrawable(R.drawable.edit_vehicle0toll_button)); break;
                    case 6: rightImageView.setImageDrawable(getResources().getDrawable(R.drawable.edit_vehicle0fuel_button)); break;
                }
                LinearLayout.LayoutParams rightParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                rightParams.setMargins(0, 0, 16, 0);
                rightImageView.setLayoutParams(rightParams);
                servicesLineLayout.addView(rightImageView);
                servicesLayout.addView(servicesLineLayout);

            }
        } else {
            while (services.size()>0) {
                int left = services.get(0);
                services.remove(0);
                int right = services.get(0);
                services.remove(0);

            }
            int last = services.get(0);
        }

    }
}
