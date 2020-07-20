package com.wisecarCompany.wisecarapp.user.vehicle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.wisecarCompany.wisecarapp.R;
import com.wisecarCompany.wisecarapp.function.fuelReceipt.FuelReceiptActivity;
import com.wisecarCompany.wisecarapp.function.insuranceRecord.InsuranceRecordActivity;
import com.wisecarCompany.wisecarapp.function.parkingReceipt.ParkingReceiptActivity;
import com.wisecarCompany.wisecarapp.function.recordLog.RecordLogActivity;
import com.wisecarCompany.wisecarapp.function.registrationReminder.RegistrationReminderActivity;
import com.wisecarCompany.wisecarapp.function.serviceRecords.ServiceRecordsActivity;
import com.wisecarCompany.wisecarapp.function.serviceRecords.ServiceRecordsDashboardActivity;
import com.wisecarCompany.wisecarapp.user.UserInfo;
import com.wisecarCompany.wisecarapp.user.vehicle.VehicleActivity;

import java.util.ArrayList;
import java.util.List;

public class DashboardActivity extends AppCompatActivity {

    private final String TAG = "dashboard";

    private ImageButton backImageButton;

    private ImageView userImgImageView;
    private TextView usernameTextView;

    private LinearLayout servicesLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        backImageButton = $(R.id.dashboardButton);
        backImageButton.setOnClickListener(v -> startActivity(new Intent(DashboardActivity.this, VehicleActivity.class)));

        userImgImageView = $(R.id.userImgImageView);
        usernameTextView = $(R.id.usernameTextView);

/*
        loadServices(new servicesListCallbacks() {
            @Override
            public void onSuccess(@NonNull List<Integer> serviceList) {
                List<Integer> services = new ArrayList<>(serviceList);
                Log.e("service list", String.valueOf(services));

                servicesLayout = $(R.id.servicesLayout);
                //int column = 3;
                int column = 2;
                for (int i = 0; i < services.size(); i += column) {
                    ConstraintLayout servicesLineLayout = new ConstraintLayout(DashboardActivity.this);
                    ConstraintSet set = new ConstraintSet();
                    ImageView[] imageViews = new ImageView[Math.min(services.size() - i, column)];
                    for (int j = 0; j < imageViews.length; j++) {
                        imageViews[j] = new ImageView(DashboardActivity.this);
                        imageViews[j].setId(j);
                        switch (services.get(i + j)) {
                            case 1:
                                imageViews[j].setImageDrawable(getResources().getDrawable(R.drawable.edit_vehicle0service_button));
                                imageViews[j].setOnClickListener(v -> startServiceRecords());
                                break;
                            case 2:
                                imageViews[j].setImageDrawable(getResources().getDrawable(R.drawable.edit_vehicle0driver_button));
                                imageViews[j].setOnClickListener(v -> startRecordlog());
                                break;
                            case 3:
                                imageViews[j].setImageDrawable(getResources().getDrawable(R.drawable.edit_vehicle0registration_button));
                                imageViews[j].setOnClickListener(v -> startRegistrationReminder());
                                break;
                            case 4:
                                imageViews[j].setImageDrawable(getResources().getDrawable(R.drawable.edit_vehicle0parking_button));
                                imageViews[j].setOnClickListener(v -> startParkingReceipts());
                                break;
                            case 5:
                                imageViews[j].setImageDrawable(getResources().getDrawable(R.drawable.edit_vehicle0insurance_button));
                                imageViews[j].setOnClickListener(v -> startInsuranceRecord());
                                break;
                            //case 6:
                            //imageViews[j].setImageDrawable(getResources().getDrawable(R.drawable.edit_vehicle0toll_button));
                            //break;
                            case 6:
                                imageViews[j].setImageDrawable(getResources().getDrawable(R.drawable.edit_vehicle0fuel_button));
                                imageViews[j].setOnClickListener(v -> startFuelReceipts());
                                break;
                        }
                        set.connect(imageViews[j].getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, 16);
                        set.connect(imageViews[j].getId(), ConstraintSet.LEFT, ConstraintSet.PARENT_ID, ConstraintSet.LEFT);
                        set.connect(imageViews[j].getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, 16);
                        set.connect(imageViews[j].getId(), ConstraintSet.RIGHT, ConstraintSet.PARENT_ID, ConstraintSet.RIGHT);
                        set.constrainPercentWidth(imageViews[j].getId(), 0.45f);
                        set.setDimensionRatio(imageViews[j].getId(), "1:1");
                        set.setHorizontalBias(imageViews[j].getId(), (float) (1.0 * j));
                        servicesLineLayout.addView(imageViews[j]);
                    }
                    set.applyTo(servicesLineLayout);
                    servicesLayout.addView(servicesLineLayout);
                }
            }

            @Override
            public void onError(@NonNull String errorMessage) {
                Log.e("No service", errorMessage);
            }
        });
*/

    }

/*
    private void startServiceRecords() {
        startActivity(new Intent(DashboardActivity.this, ServiceRecordsDashboardActivity.class));
    }

    private void startRecordlog() {
        startActivity(new Intent(DashboardActivity.this, RecordLogDashboardActivity.class));
    }

    private void startRegistrationReminder() {
        startActivity(new Intent(DashboardActivity.this, RegistrationReminderDashboardActivity.class));
    }

    private void startParkingReceipts() {
        startActivity(new Intent(DashboardActivity.this, ParkingReceiptDashboardActivity.class));
    }

    private void startInsuranceRecord() {
        startActivity(new Intent(DashboardActivity.this, InsuranceRecordDashboardActivity.class));
    }

    private void startFuelReceipts() {
        startActivity(new Intent(DashboardActivity.this, FuelReceiptDashboardActivity.class));
    }
*/

    private <T extends View> T $(int id){
        return (T) findViewById(id);
    }
}
