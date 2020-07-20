package com.wisecarCompany.wisecarapp.function.serviceRecords;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.wisecarCompany.wisecarapp.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ServiceRecordsSendActivity extends AppCompatActivity {

    private final static String TAG = "Service Records Send";

    private ServiceRecord record;

    private ImageButton backImageButton;

    private TextView dateTextView;
    private TextView centreTextView;
    private TextView refNoTextView;
    private TextView optionsTextView;
    private TextView notesTextView;
    private TextView nextdateTextView;
    private TextView nextDistanceTextView;
    private TextView documentLinkTextView;

    private EditText emailEditText;
    private Button sendButton;
    private String email;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_record_send);

        record = (ServiceRecord) this.getIntent().getSerializableExtra("record");
        Log.d(TAG, "record: " + record);

        backImageButton = $(R.id.backImageButton);
        backImageButton.setOnClickListener(v -> startActivity(new Intent(this, ServiceRecordsDashboardActivity.class)));

        dateTextView = $(R.id.dateTextView);
        centreTextView = $(R.id.centreTextView);
        refNoTextView = $(R.id.refNoTextView);
        optionsTextView = $(R.id.optionsTextView);
        notesTextView = $(R.id.notesTextView);
        nextdateTextView = $(R.id.nextDateTextView);
        nextDistanceTextView = $(R.id.nextDistanceTextView);
        documentLinkTextView = $(R.id.documentLinkTextView);

        SimpleDateFormat format = new SimpleDateFormat("ddMMM yyyy", Locale.getDefault());
        dateTextView.setText(format.format(record.getDate()));
        centreTextView.setText(record.getCentre());
        refNoTextView.setText(record.getRefNo());
        optionsTextView.setText(record.getOptionsStr());
        notesTextView.setText(record.getNotes());
        nextdateTextView.setText(format.format(record.getNextDate()));
        nextDistanceTextView.setText(""+(int)record.getNextDistance());

        documentLinkTextView.setOnClickListener(v -> {
            Log.d(TAG, "document link onclick");
        });

        emailEditText = $(R.id.emailEditText);
        sendButton = $(R.id.sendButton);
        sendButton.setOnClickListener(v -> {
            email = emailEditText.getText().toString();
            boolean isEmail = false;
            try{
                String check = "^([a-z0-9A-Z]+[-|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";
                Pattern regex = Pattern.compile(check);
                Matcher matcher = regex.matcher(email);
                isEmail = matcher.matches();
            } catch(Exception e ){
                isEmail = false;
            }
            if(isEmail) {



            } else {
                Toast.makeText(getApplicationContext(), "Please enter correct email address", Toast.LENGTH_SHORT).show();
            }
        });

    }

    public boolean dispatchTouchEvent(MotionEvent ev) {
        View v = getCurrentFocus();
        if (isShouldHideInput(v, ev)) {
            assert v != null;
            hideSoftInput(v.getWindowToken());
        }
        return super.dispatchTouchEvent(ev);
    }
    private boolean isShouldHideInput(View v, MotionEvent event) {
        if(v instanceof EditText) {
            int[] l = {0, 0};
            v.getLocationInWindow(l);
            int left = l[0],
                    top = l[1],
                    bottom = top + v.getHeight(),
                    right = left + v.getWidth();
            return !(event.getX()>left && event.getX()<right
                    && event.getY()>top && event.getY()<bottom);
        }
        return false;
    }
    private void hideSoftInput(IBinder token) {
        if (token != null) {
            InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            assert manager != null;
            manager.hideSoftInputFromWindow(token, InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    private <T extends View> T $(int id){
        return (T) findViewById(id);
    }
}
