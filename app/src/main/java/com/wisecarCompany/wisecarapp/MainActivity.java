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

import com.wisecarCompany.wisecarapp.user.UserInfo;
import com.wisecarCompany.wisecarapp.user.profile.LoginActivity;
import com.wisecarCompany.wisecarapp.user.vehicle.VehicleActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

public class MainActivity extends AppCompatActivity {

    String TAG = "main";

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


        UserInfo.setEmerNotices(new TreeMap<>((o1, o2) -> { //sort by the time distance from today
            Calendar cal = Calendar.getInstance();
            cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH),0, 0, 0);
            if(Math.abs(cal.getTime().getTime() - o1.getTime()) < Math.abs(cal.getTime().getTime() - o2.getTime())) return -1;
            else if(Math.abs(cal.getTime().getTime() - o1.getTime()) > Math.abs(cal.getTime().getTime() - o2.getTime())) return 1;
            else return 0;
        }));
        UserInfo.setNotices(new TreeMap<>((o1, o2) -> {
            Calendar cal = Calendar.getInstance();
            cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH),0, 0, 0);
            if(Math.abs(cal.getTime().getTime() - o1.getTime()) < Math.abs(cal.getTime().getTime() - o2.getTime())) return -1;
            else if(Math.abs(cal.getTime().getTime() - o1.getTime()) > Math.abs(cal.getTime().getTime() - o2.getTime())) return 1;
            else return 0;
        }));

        //below are test data:
        UserInfo.getNotices().put(intToDate(2020,6,5), new String[]{"Car1","notice1"});
        UserInfo.getNotices().put(intToDate(2020,6,15), new String[]{"Car2","notice2"});
        UserInfo.getEmerNotices().put(intToDate(2020,6,25), new String[]{"Car3","notice3"});
        UserInfo.getEmerNotices().put(intToDate(2020,6,30), new String[]{"Car2","notice4"});
        UserInfo.getNotices().put(intToDate(2020,7,5), new String[]{"Car1","notice5"});
        //test data over

        int seq = 0;
        for(Map.Entry<Date, String[]> entry: UserInfo.getEmerNotices().entrySet()) notification(entry, seq++);

        startActivity(new Intent(MainActivity.this, LoginActivity.class));
        //startActivity(new Intent(MainActivity.this, DashboardActivity.class));

    }

    private void notification(Map.Entry<Date, String[]> notice, int seq) {
        if (notice.getValue().length!=2) Log.e(TAG, "setNotice in status bar: notice value String[] length != 2 ERR0R!!");

        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        assert manager != null;

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
                .setSmallIcon(R.drawable.wisecar_logo)
                .setContentTitle("Due Date: " + new SimpleDateFormat("dd MMM", Locale.getDefault()).format(notice.getKey()))
                .setContentText(notice.getValue()[0] + ", " + notice.getValue()[1])
                .setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setContentIntent(pendingIntent)
                .build();

        manager.notify(seq, notification);

    }

    private static java.util.Date intToDate(int year, int month, int day) {
        StringBuffer sb = new StringBuffer();
        if (day < 10) sb.append("0" + day);
        else sb.append(day);
        sb.append("/");
        month++;
        if (month < 10) sb.append("0" + month);
        else sb.append(month);
        sb.append("/" + year);
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        java.util.Date date = null;
        try {
            date = format.parse(sb.toString());
        } catch (ParseException e) {
            return null;
        }
        return date;
    }

}




