package com.wisecarCompany.wisecarapp;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;

import com.wisecarCompany.wisecarapp.function.driverLog.DriverLogActivity;
import com.wisecarCompany.wisecarapp.user.UserInfo;

public class WisecarApplication extends Application {

    private final static String TAG = "Application";
    private int mFinalCount;

    private MediaPlayer resumeDriverLogMediaPlayer;


    @Override
    public void onCreate() {
        super.onCreate();
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

            }

            @Override
            public void onActivityStarted(Activity activity) {
                mFinalCount++;
                Log.e("onActivityStarted", mFinalCount +"");
                if (mFinalCount == 1){
                    //return to front from back
                    Log.d(TAG, "front...");
                    if(UserInfo.getCurrLog()!=null && UserInfo.getCurrLog().isPausing() && UserInfo.getCurrLog().isSwitchedBackWhileRunning()){
                        UserInfo.getCurrLog().setSwitchedBackWhileRunning(false);
                        UserInfo.getCurrLog().setPausing(false);
                        //stopResumeDriverLogoice();
                        startActivity(new Intent(getApplicationContext(), DriverLogActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                    }
                }
            }

            @Override
            public void onActivityResumed(Activity activity) {

            }

            @Override
            public void onActivityPaused(Activity activity) {

            }

            @Override
            public void onActivityStopped(Activity activity) {
                mFinalCount--;
                Log.i("onActivityStopped", mFinalCount +"");
                if (mFinalCount == 0){
                    //go to back from front
                    Log.d(TAG, "back...");
                    if(UserInfo.getCurrLog()!=null && !UserInfo.getCurrLog().isPausing()){
                        UserInfo.getCurrLog().setPausing(true);
                        UserInfo.getCurrLog().setSwitchedBackWhileRunning(true);
                        //startResumeDriverLogVoice();
                        resumeDriverLogMediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.resume_driver_log);
                        resumeDriverLogMediaPlayer.start();
                    }
                }
            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

            }

            @Override
            public void onActivityDestroyed(Activity activity) {

            }
        });
    }


/*
    public void startResumeDriverLogVoice() {
        if (resumeDriverLogMediaPlayer != null && resumeDriverLogMediaPlayer.isPlaying()) {return;}
        resumeDriverLogMediaPlayer = MediaPlayer.create(this, R.raw.resume_driver_log);
        resumeDriverLogMediaPlayer.start();
        resumeDriverLogMediaPlayer.setOnCompletionListener(mp -> {
            if (resumeDriverLogMediaPlayer == null) {return;}
            resumeDriverLogMediaPlayer.start();
            resumeDriverLogMediaPlayer.setLooping(true);
        });
    }

    public void stopResumeDriverLogoice() {
        if (resumeDriverLogMediaPlayer != null) {
            resumeDriverLogMediaPlayer.stop();
            resumeDriverLogMediaPlayer.release();
            resumeDriverLogMediaPlayer = null;
        }
    }
*/

}
