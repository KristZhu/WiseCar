package com.wisecarCompany.wisecarapp.function.serviceRecords;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.wisecarCompany.wisecarapp.R;
import com.wisecarCompany.wisecarapp.function.shareVehicle.ShareVehicleListActivity;
import com.wisecarCompany.wisecarapp.user.vehicle.DashboardActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

public class ServiceRecordsDashboardActivity extends AppCompatActivity {

    private final static String TAG = "ServiceRecordsDashboard";

    private ImageButton backImageButton;

    private LinearLayout mainDiv;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @SuppressLint("ResourceType")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_records_dashboard);

        backImageButton = $(R.id.backImageButton);
        backImageButton.setOnClickListener(v -> startActivity(new Intent(ServiceRecordsDashboardActivity.this, DashboardActivity.class)));

        mainDiv = $(R.id.mainDiv);

        //test data:
        Set<ServiceRecord> records = new TreeSet<>();
        records.add(new ServiceRecord("11111111111111", new Date(new Date().getTime() - 24*60*60*1000*30), "1", new Date(new Date().getTime() + 24*60*60*1000*30), 10000, false));
        records.add(new ServiceRecord("222222222222", new Date(new Date().getTime() - 24*60*60*1000*10), "2", new Date(new Date().getTime() + 24*60*60*1000*60), 10000, false));
        records.add(new ServiceRecord("33333333333333", new Date(new Date().getTime() - 24*60*60*1000*20), "3", new Date(new Date().getTime() + 24*60*60*1000*1), 10000, true));
        //test data over

        for(ServiceRecord record: records) {

            ConstraintLayout lineLayout = new ConstraintLayout(this);
            ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.setMargins(0, 16, 0, 16);
            lineLayout.setLayoutParams(params);

            ConstraintSet set = new ConstraintSet();

            ImageView lightImageView = new ImageView(this);
            lightImageView.setId(0);
            if(record.getDate().before(new Date())) lightImageView.setImageDrawable(getResources().getDrawable(R.drawable.dashboard0light_line));
            else lightImageView.setImageDrawable(getResources().getDrawable(R.drawable.dashboard0light_red_line));
            set.connect(lightImageView.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP);
            set.connect(lightImageView.getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM);
            set.connect(lightImageView.getId(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START);
            set.connect(lightImageView.getId(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END);
            set.setDimensionRatio(lightImageView.getId(), "4.5:1");
            set.constrainPercentHeight(lightImageView.getId(), (float)(2.0/3));
            set.setVerticalBias(lightImageView.getId(), 0.0f);
            lightImageView.setScaleType(ImageView.ScaleType.FIT_XY);
            lineLayout.addView(lightImageView);

            ImageView darkImageView = new ImageView(this);
            darkImageView.setId(1);
            darkImageView.setScaleType(ImageView.ScaleType.FIT_XY);
            if(record.getDate().before(new Date())) darkImageView.setImageDrawable(getResources().getDrawable(R.drawable.dashboard0dark_line));
            else darkImageView.setImageDrawable(getResources().getDrawable(R.drawable.dashboard0dark_red_line));
            set.connect(darkImageView.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP);
            set.connect(darkImageView.getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM);
            set.connect(darkImageView.getId(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START);
            set.connect(darkImageView.getId(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END);
            set.setDimensionRatio(darkImageView.getId(), "9:1");
            set.constrainPercentHeight(darkImageView.getId(), (float)(1.0/3));
            set.setVerticalBias(darkImageView.getId(), 1.0f);
            darkImageView.setScaleType(ImageView.ScaleType.FIT_XY);
            lineLayout.addView(darkImageView);

            TextView registrationNoTextView = new TextView(this);
            registrationNoTextView.setId(10);
            set.connect(registrationNoTextView.getId(), ConstraintSet.TOP, lightImageView.getId(), ConstraintSet.TOP);
            set.connect(registrationNoTextView.getId(), ConstraintSet.BOTTOM, lightImageView.getId(), ConstraintSet.BOTTOM);
            set.connect(registrationNoTextView.getId(), ConstraintSet.START, lightImageView.getId(), ConstraintSet.START, 32);
            set.connect(registrationNoTextView.getId(), ConstraintSet.END, lightImageView.getId(), ConstraintSet.END);
            set.constrainPercentHeight(registrationNoTextView.getId(), 0.25f);
            set.setVerticalBias(registrationNoTextView.getId(), 0.0f);
            registrationNoTextView.setAutoSizeTextTypeUniformWithConfiguration(10, 30, 1, TypedValue.COMPLEX_UNIT_SP);
            registrationNoTextView.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
            registrationNoTextView.setTextColor(0xff007ba4);
            registrationNoTextView.setText(record.getRegistrationNo());
            lineLayout.addView(registrationNoTextView);

            TextView dateTextView = new TextView(this);
            dateTextView.setId(11);
            set.connect(dateTextView.getId(), ConstraintSet.TOP, registrationNoTextView.getId(), ConstraintSet.BOTTOM);
            set.connect(dateTextView.getId(), ConstraintSet.BOTTOM, lightImageView.getId(), ConstraintSet.BOTTOM);
            set.connect(dateTextView.getId(), ConstraintSet.START, lightImageView.getId(), ConstraintSet.START, 32);
            set.connect(dateTextView.getId(), ConstraintSet.END, lightImageView.getId(), ConstraintSet.END);
            set.constrainPercentHeight(dateTextView.getId(), 0.2f);
            set.setVerticalBias(dateTextView.getId(), 0.0f);
            dateTextView.setAutoSizeTextTypeUniformWithConfiguration(10, 30, 1, TypedValue.COMPLEX_UNIT_SP);
            dateTextView.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
            dateTextView.setTextColor(0xff000000);
            dateTextView.setText("Date: " + new SimpleDateFormat("ddMMM yyyy", Locale.getDefault()).format(record.getDate()));
            lineLayout.addView(dateTextView);

            TextView refNoTextView = new TextView(this);
            refNoTextView.setId(12);
            set.connect(refNoTextView.getId(), ConstraintSet.TOP, dateTextView.getId(), ConstraintSet.BOTTOM);
            set.connect(refNoTextView.getId(), ConstraintSet.BOTTOM, lightImageView.getId(), ConstraintSet.BOTTOM);
            set.connect(refNoTextView.getId(), ConstraintSet.START, lightImageView.getId(), ConstraintSet.START, 32);
            set.connect(refNoTextView.getId(), ConstraintSet.END, lightImageView.getId(), ConstraintSet.END);
            set.constrainPercentHeight(refNoTextView.getId(), 0.2f);
            set.setVerticalBias(refNoTextView.getId(), 0.0f);
            refNoTextView.setAutoSizeTextTypeUniformWithConfiguration(10, 30, 1, TypedValue.COMPLEX_UNIT_SP);
            refNoTextView.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
            refNoTextView.setTextColor(0xff000000);
            refNoTextView.setText("Ref: " + record.getRefNo());
            lineLayout.addView(refNoTextView);

            TextView nextTextView = new TextView(this);
            nextTextView.setId(13);
            set.connect(nextTextView.getId(), ConstraintSet.TOP, darkImageView.getId(), ConstraintSet.TOP);
            set.connect(nextTextView.getId(), ConstraintSet.BOTTOM, darkImageView.getId(), ConstraintSet.BOTTOM);
            set.connect(nextTextView.getId(), ConstraintSet.START, darkImageView.getId(), ConstraintSet.START, 32);
            set.connect(nextTextView.getId(), ConstraintSet.END, darkImageView.getId(), ConstraintSet.END);
            set.constrainPercentHeight(nextTextView.getId(), 0.2f);
            nextTextView.setAutoSizeTextTypeUniformWithConfiguration(10, 30, 1, TypedValue.COMPLEX_UNIT_SP);
            nextTextView.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
            nextTextView.setTextColor(0xff000000);
            nextTextView.setText("Next Service: " + new SimpleDateFormat("ddMMM yyyy", Locale.getDefault()).format(record.getNextDate()) + " / " + record.getNextDistance() + "km");
            lineLayout.addView(nextTextView);

            ImageView sentImageView = new ImageView(this);
            sentImageView.setId(20);
            set.connect(sentImageView.getId(), ConstraintSet.TOP, registrationNoTextView.getId(), ConstraintSet.TOP);
            set.connect(sentImageView.getId(), ConstraintSet.BOTTOM, refNoTextView.getId(), ConstraintSet.BOTTOM);  //do not know why... if constraint to background, there are bugs
            set.connect(sentImageView.getId(), ConstraintSet.START, lightImageView.getId(), ConstraintSet.START);
            set.connect(sentImageView.getId(), ConstraintSet.END, lightImageView.getId(), ConstraintSet.END, 32);
            set.setDimensionRatio(sentImageView.getId(), "1:1");
            set.constrainPercentWidth(sentImageView.getId(), 0.04f);
            set.setHorizontalBias(sentImageView.getId(), 1.0f);
            sentImageView.setScaleType(ImageView.ScaleType.FIT_XY);
            if(record.isSentBefore()) sentImageView.setImageDrawable(getResources().getDrawable(R.drawable.dashboard0sent));
            else sentImageView.setImageDrawable(getResources().getDrawable(R.drawable.dashboard0unsent));
            lineLayout.addView(sentImageView);

            ImageView sendImageView = new ImageView(this);
            sendImageView.setId(21);
            set.connect(sendImageView.getId(), ConstraintSet.TOP, registrationNoTextView.getId(), ConstraintSet.TOP);
            set.connect(sendImageView.getId(), ConstraintSet.BOTTOM, refNoTextView.getId(), ConstraintSet.BOTTOM);  //do not know y 2...
            set.connect(sendImageView.getId(), ConstraintSet.START, lightImageView.getId(), ConstraintSet.START);
            set.connect(sendImageView.getId(), ConstraintSet.END, sentImageView.getId(), ConstraintSet.START, 32);
            set.setDimensionRatio(sendImageView.getId(), "1:1");
            set.constrainPercentWidth(sendImageView.getId(), 0.1f);
            set.setHorizontalBias(sendImageView.getId(), 1.0f);
            sendImageView.setScaleType(ImageView.ScaleType.FIT_XY);
            sendImageView.setImageDrawable(getResources().getDrawable(R.drawable.dashboard0send));
            sendImageView.setOnClickListener(v -> {
                Log.d(TAG, "send servicerecord registrationNO: " + record.getRegistrationNo());
                startActivity(new Intent(this, ServiceRecordsSendActivity.class).putExtra("record", record));
            });
            lineLayout.addView(sendImageView);

            if(! record.getDate().before(new Date())) {
                ImageView notifyImageView = new ImageView(this);
                notifyImageView.setId(22);
                set.connect(notifyImageView.getId(), ConstraintSet.TOP, darkImageView.getId(), ConstraintSet.TOP);
                set.connect(notifyImageView.getId(), ConstraintSet.BOTTOM, darkImageView.getId(), ConstraintSet.BOTTOM);  //do not know y 2...
                set.connect(notifyImageView.getId(), ConstraintSet.START, darkImageView.getId(), ConstraintSet.START);
                set.connect(notifyImageView.getId(), ConstraintSet.END, darkImageView.getId(), ConstraintSet.END, 16);
                set.setDimensionRatio(notifyImageView.getId(), "1:1");
                set.constrainPercentWidth(notifyImageView.getId(), 0.1f);
                set.setHorizontalBias(notifyImageView.getId(), 1.0f);
                notifyImageView.setScaleType(ImageView.ScaleType.FIT_XY);
                notifyImageView.setImageDrawable(getResources().getDrawable(R.drawable.dashboard0notification));
                lineLayout.addView(notifyImageView);
            }

            set.applyTo(lineLayout);
            mainDiv.addView(lineLayout);


        }

    }

    private <T extends View> T $(int id){
        return (T) findViewById(id);
    }
}
