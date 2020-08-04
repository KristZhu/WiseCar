package com.wisecarCompany.wisecarapp;


import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.wisecarCompany.wisecarapp.element.AppFrontBackListener;
import com.wisecarCompany.wisecarapp.user.profile.LoginActivity;

public class MainActivity extends AppCompatActivity {

    String TAG = "main";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startActivity(new Intent(this, LoginActivity.class));
        //startActivity(new Intent(MainActivity.this, CreateUserActivity.class));


/*  try to push message every day. Not working at all.

        Intent intent = new Intent(this, VehicleActivity.class);
        intent.setAction("VIDEO_TIMER");
        PendingIntent sender = PendingIntent.getBroadcast(this, 0, intent, 0);
        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
        assert am != null;
        am.setRepeating(AlarmManager.RTC, System.currentTimeMillis(), 60*1000, sender);
*/


/*  Float window always show at front of screen even when this app is running at back
    It can be used in the future when driver log is running at back
    Currently when a user is running a log, if he/she turns screen off, or put this app at back, a voice notification loops to ask the user to go back
    Only when the screen is on and the user is using this app at front will gsp run

    put the following in build.gradle:
    implementation 'com.github.yhaolpz:FloatWindow:1.0.9'

        ImageView floatWindowImageView = new ImageView(getApplicationContext());
        floatWindowImageView.setImageResource(R.drawable.blank_white_circle);

        //https://github.com/yhaolpz/FloatWindow
        FloatWindow
                .with(getApplicationContext())
                .setView(floatWindowImageView)
                .setWidth(Screen.width, 0.2f)
                .setHeight(Screen.width, 0.2f)
                .setX(Screen.width, 0.8f)
                .setY(Screen.height, 0.3f)
                .setMoveType(MoveType.slide,0,0)
                .setMoveStyle(500, new BounceInterpolator())
                .setFilter(false, DriverLogActivity.class)  //does not show in this class
                .setViewStateListener(new ViewStateListener() {
                    @Override
                    public void onPositionUpdate(int x, int y) {
                        Log.d(TAG, "onPositionUpdate: x=" + x + " y=" + y);
                    }
                    @Override
                    public void onShow() {
                        Log.d(TAG, "onShow");
                    }
                    @Override
                    public void onHide() {
                        Log.d(TAG, "onHide");
                    }
                    @Override
                    public void onDismiss() {
                        Log.d(TAG, "onDismiss");
                    }
                    @Override
                    public void onMoveAnimStart() {
                        Log.d(TAG, "onMoveAnimStart");
                    }
                    @Override
                    public void onMoveAnimEnd() {
                        Log.d(TAG, "onMoveAnimEnd");
                    }
                    @Override
                    public void onBackToDesktop() {
                        Log.d(TAG, "onBackToDesktop");
                    }
                })
                .setPermissionListener(new PermissionListener() {
                    @Override
                    public void onSuccess() {
                        Log.d(TAG, "onSuccess");
                    }
                    @Override
                    public void onFail() {
                        Log.d(TAG, "onFail");
                    }
                })
                .setDesktopShow(true)
                .build();

        floatWindowImageView.setImageResource(R.drawable.logo_white_bg);
*/

    }

}




