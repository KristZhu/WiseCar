package com.example.wisecarapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import android.os.Bundle;
import android.util.Log;
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
        services.add(4);
        services.add(5);

        if(services.size()==0) {

        } else {
            while (services.size()>=2) {
                int left = services.get(0);
                services.remove(0);
                int right = services.get(0);
                services.remove(0);
                ConstraintLayout servicesLineLayout = new ConstraintLayout(EditVehicleActivity.this);
                ImageView leftImageView = new ImageView(EditVehicleActivity.this);
                int leftImageViewID = 1;
                leftImageView.setId(leftImageViewID);
                switch (left) {
                    case 1:
                        leftImageView.setImageDrawable(getResources().getDrawable(R.drawable.edit_vehicle0service_button));
                        leftImageView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Log.d(TAG, "onClickService: Start Service Records");
                                //startServiceRecords(vehicleID);
                            }
                        });
                        break;
                    case 2: leftImageView.setImageDrawable(getResources().getDrawable(R.drawable.edit_vehicle0driver_button)); break;
                    case 3: leftImageView.setImageDrawable(getResources().getDrawable(R.drawable.edit_vehicle0registration_button)); break;
                    case 4: leftImageView.setImageDrawable(getResources().getDrawable(R.drawable.edit_vehicle0parking_button)); break;
                    case 5: leftImageView.setImageDrawable(getResources().getDrawable(R.drawable.edit_vehicle0insurance_button)); break;
                    case 6: leftImageView.setImageDrawable(getResources().getDrawable(R.drawable.edit_vehicle0toll_button)); break;
                    case 7: leftImageView.setImageDrawable(getResources().getDrawable(R.drawable.edit_vehicle0fuel_button)); break;
                }
                servicesLineLayout.addView(leftImageView);
                ImageView rightImageView = new ImageView(EditVehicleActivity.this);
                int rightImageViewID = 2;
                rightImageView.setId(rightImageViewID);
                switch (right) {
                    case 1:
                        rightImageView.setImageDrawable(getResources().getDrawable(R.drawable.edit_vehicle0service_button));
                        rightImageView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Log.d(TAG, "onClickService: Start Service Records");
                                //startServiceRecords(vehicleID);
                            }
                        });
                        break;
                    case 2: rightImageView.setImageDrawable(getResources().getDrawable(R.drawable.edit_vehicle0driver_button)); break;
                    case 3: rightImageView.setImageDrawable(getResources().getDrawable(R.drawable.edit_vehicle0registration_button)); break;
                    case 4: rightImageView.setImageDrawable(getResources().getDrawable(R.drawable.edit_vehicle0parking_button)); break;
                    case 5: rightImageView.setImageDrawable(getResources().getDrawable(R.drawable.edit_vehicle0insurance_button)); break;
                    case 6: rightImageView.setImageDrawable(getResources().getDrawable(R.drawable.edit_vehicle0toll_button)); break;
                    case 7: rightImageView.setImageDrawable(getResources().getDrawable(R.drawable.edit_vehicle0fuel_button)); break;
                }
                servicesLineLayout.addView(rightImageView);

                ConstraintSet set = new ConstraintSet();
                set.connect(leftImageView.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, 16);
                set.connect(leftImageView.getId(), ConstraintSet.LEFT, ConstraintSet.PARENT_ID, ConstraintSet.LEFT);
                set.connect(leftImageView.getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, 16);
                set.connect(leftImageView.getId(), ConstraintSet.RIGHT, ConstraintSet.PARENT_ID, ConstraintSet.RIGHT);
                set.constrainPercentWidth(leftImageView.getId(), 0.45f);
                set.setDimensionRatio(leftImageView.getId(), "1:1");
                set.setHorizontalBias(leftImageView.getId(), 0.0f);
                set.connect(rightImageView.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, 8);
                set.connect(rightImageView.getId(), ConstraintSet.LEFT, ConstraintSet.PARENT_ID, ConstraintSet.LEFT);
                set.connect(rightImageView.getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, 8);
                set.connect(rightImageView.getId(), ConstraintSet.RIGHT, ConstraintSet.PARENT_ID, ConstraintSet.RIGHT);
                set.constrainPercentWidth(rightImageView.getId(), 0.45f);
                set.setDimensionRatio(rightImageView.getId(), "1:1");
                set.setHorizontalBias(rightImageView.getId(), 1.0f);
                set.applyTo(servicesLineLayout);
                servicesLayout.addView(servicesLineLayout);

            }
            if(services.size()>=1) {
                int rest = services.get(0);
                services.remove(0);
                ConstraintLayout servicesLineLayout = new ConstraintLayout(EditVehicleActivity.this);
                ImageView imageView = new ImageView(EditVehicleActivity.this);
                int imageViewID = 1;
                imageView.setId(imageViewID);
                switch (rest) {
                    case 1:
                        imageView.setImageDrawable(getResources().getDrawable(R.drawable.edit_vehicle0service_button));
                        imageView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Log.d(TAG, "onClickService: Start Service Records");
                                //startServiceRecords(vehicleID);
                            }
                        });
                        break;
                    case 2: imageView.setImageDrawable(getResources().getDrawable(R.drawable.edit_vehicle0driver_button)); break;
                    case 3: imageView.setImageDrawable(getResources().getDrawable(R.drawable.edit_vehicle0registration_button)); break;
                    case 4: imageView.setImageDrawable(getResources().getDrawable(R.drawable.edit_vehicle0parking_button)); break;
                    case 5: imageView.setImageDrawable(getResources().getDrawable(R.drawable.edit_vehicle0insurance_button)); break;
                    case 6: imageView.setImageDrawable(getResources().getDrawable(R.drawable.edit_vehicle0toll_button)); break;
                    case 7: imageView.setImageDrawable(getResources().getDrawable(R.drawable.edit_vehicle0fuel_button)); break;
                }
                servicesLineLayout.addView(imageView);
                ConstraintSet set = new ConstraintSet();
                set.connect(imageView.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, 8);
                set.connect(imageView.getId(), ConstraintSet.LEFT, ConstraintSet.PARENT_ID, ConstraintSet.LEFT);
                set.connect(imageView.getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, 8);
                set.connect(imageView.getId(), ConstraintSet.RIGHT, ConstraintSet.PARENT_ID, ConstraintSet.RIGHT);
                set.constrainPercentWidth(imageView.getId(), 0.45f);
                set.setDimensionRatio(imageView.getId(), "1:1");
                set.setHorizontalBias(imageView.getId(), 0.0f);
                set.applyTo(servicesLineLayout);
                servicesLayout.addView(servicesLineLayout);
            }
        }

    }
}
