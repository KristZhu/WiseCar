package com.wisecarCompany.wisecarapp.user.introduction;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;

import com.stephentuso.welcome.BasicPage;
import com.stephentuso.welcome.FullscreenParallaxPage;
import com.stephentuso.welcome.ParallaxPage;
import com.stephentuso.welcome.TitlePage;
import com.stephentuso.welcome.WelcomeActivity;
import com.stephentuso.welcome.WelcomeConfiguration;
import com.wisecarCompany.wisecarapp.R;

public class WisecarWelcomeActivity extends WelcomeActivity {

    private SharedPreferences sp;
    private String username;


    @SuppressLint("ResourceType")
    @Override
    protected WelcomeConfiguration configuration() {

        //username = sp.getString("USERNAME", "");
        username = "Krist";

        return new WelcomeConfiguration.Builder(this)
                .defaultBackgroundColor(R.color.darkBG)
                .page(new TitlePage(R.drawable.welcome_1, ""))
                .page(new TitlePage(R.drawable.welcome_2, ""))
                .page(new TitlePage(R.drawable.welcome_3, ""))
                .swipeToDismiss(true)
                .build();
    }
}
