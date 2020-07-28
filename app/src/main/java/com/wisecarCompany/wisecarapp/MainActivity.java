package com.wisecarCompany.wisecarapp;


import android.app.Activity;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.RemoteViews;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import com.wisecarCompany.wisecarapp.user.profile.LoginActivity;
import com.wisecarCompany.wisecarapp.user.vehicle.VehicleActivity;

public class MainActivity extends AppCompatActivity {

    String TAG = "main";
    final int NOTIFYID = 0x123;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
/*
        Intent intent = new Intent(this, AutoReceiver.class);
        intent.setAction("VIDEO_TIMER");
        PendingIntent sender = PendingIntent.getBroadcast(this, 0, intent, 0);
        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
        am.setRepeating(AlarmManager.RTC, System.currentTimeMillis(), 60000, sender);

*/

        notification();

        startActivity(new Intent(MainActivity.this, LoginActivity.class));
        //startActivity(new Intent(MainActivity.this, DashboardActivity.class));

    }

    private void notification() {

        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        //8.0 and above need channelId
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            String channelId = "default";
            String channelName = "default notice";
            manager.createNotificationChannel(new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH));
        }

        //TaskStackBuilder
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(VehicleActivity.class);
        stackBuilder.addNextIntent(new Intent(this, LoginActivity.class));

        PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notification = new NotificationCompat.Builder(this, "default")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("标题")
                .setContentText("这是内容，点击我可以跳转")
                .setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setContentIntent(pendingIntent)
                .build();

        manager.notify(1, notification);

    }

}




