package com.wisecarCompany.wisecarapp.user.introduction;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import com.github.appintro.AppIntro;
import com.github.appintro.AppIntroCustomLayoutFragment;
import com.github.appintro.AppIntroFragment;
import com.wisecarCompany.wisecarapp.MainActivity;
import com.wisecarCompany.wisecarapp.R;
import com.wisecarCompany.wisecarapp.user.profile.LoginActivity;

import java.util.HashMap;
import kotlin.Metadata;
import org.jetbrains.annotations.Nullable;


//https://github.com/AppIntro/AppIntro
public class WelcomeActivity extends AppIntro {

    private SharedPreferences sp;


    @SuppressLint({"ResourceType", "SetTextI18n"})
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sp = this.getSharedPreferences("userInfo", MODE_PRIVATE);

        String welcome1Title = "Dear " + sp.getString("USERNAME", "") + ", \n\nWelcome";
        String welcome1Description = "Your WISECAR Account is created and activated. You can start using WISECAR Services now. ";
        addSlide((Fragment)AppIntroFragment.newInstance(
                welcome1Title,
                welcome1Description,
                R.drawable.logo_transparent_bg,
                0xff007ba4
        ));
        //addSlide((Fragment)AppIntroCustomLayoutFragment.Companion.newInstance(R.layout.welcome_1));
        addSlide((Fragment)AppIntroCustomLayoutFragment.Companion.newInstance(R.layout.welcome_2));
        addSlide((Fragment)AppIntroCustomLayoutFragment.Companion.newInstance(R.layout.welcome_3));
        addSlide((Fragment)AppIntroFragment.newInstance(
                "\n\nEnjoy the WISECAR Services.",
                "",
                0,
                0xff007ba4
        ));

        showStatusBar(true);
        setStatusBarColorRes(R.color.darkBG);
        //setNavBarColorRes(R.color.darkBG);
        setProgressIndicator();
    }

    public void onSkipPressed(@Nullable Fragment currentFragment) {
        super.onSkipPressed(currentFragment);
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    public void onDonePressed(@Nullable Fragment currentFragment) {
        super.onDonePressed(currentFragment);
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

}
