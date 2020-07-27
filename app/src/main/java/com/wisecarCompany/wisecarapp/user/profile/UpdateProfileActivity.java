package com.wisecarCompany.wisecarapp.user.profile;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.text.method.PasswordTransformationMethod;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.wisecarCompany.wisecarapp.MainActivity;
import com.wisecarCompany.wisecarapp.R;
import com.wisecarCompany.wisecarapp.user.UserInfo;
import com.wisecarCompany.wisecarapp.user.vehicle.VehicleActivity;

import java.security.MessageDigest;

public class UpdateProfileActivity extends AppCompatActivity {

    private final static String TAG = "Update Profile";
    private SharedPreferences sp;

    private String fName;
    private String lName;
    private String email;
    private String hashedPassword;

    private ImageButton backImageButton;

    private ImageButton updateImageButton;

    private EditText fNameEditText;
    private EditText lNameEditText;
    private EditText emailEditText;
    private EditText passwordEditText;
    private EditText confirmPasswordEditText;

    private ImageView passImageView;
    private ImageView confirmPassImageView;
    private ImageView confirmNoPassImageView;

    private boolean passwordChanged;


    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_profile);

        sp = this.getSharedPreferences("userInfo", MODE_PRIVATE);

        backImageButton = $(R.id.backImageButton);
        backImageButton.setOnClickListener(v -> startActivity(new Intent(this, VehicleActivity.class)));

        fNameEditText = $(R.id.fNameEditText);
        lNameEditText = $(R.id.lNameEditText);
        emailEditText = $(R.id.emailEditText);
        passwordEditText = $(R.id.passwordEditText);
        confirmPasswordEditText = $(R.id.passwordConfirmEditText);

        passImageView = $(R.id.passImageView);
        confirmPassImageView = $(R.id.confirmPassImageView);
        confirmNoPassImageView = $(R.id.confirmNoPassImageView);

        fNameEditText.setText(UserInfo.getfName()==null ? "" : UserInfo.getfName());
        lNameEditText.setText(UserInfo.getlName()==null ? "" : UserInfo.getlName());
        emailEditText.setText(UserInfo.getUserEmail());

        int passwordLength = sp.getInt("PASSWORD_LENGTH", 10);
        StringBuffer passwordSB = new StringBuffer();
        for(int i=0; i<passwordLength; i++) passwordSB.append("*");
        passwordEditText.setText(passwordSB.toString());    //show a fake password with the same length of the real one
        confirmPasswordEditText.setText(passwordSB.toString());

        passwordChanged = false;
        passwordEditText.setOnTouchListener((v, event) -> {
            passImageView.setVisibility(View.INVISIBLE);
            if(!passwordChanged) {
                passwordChanged = true;
                passwordEditText.setText("");
                confirmPasswordEditText.setText("");
            }
            return false;
        });
        passwordEditText.setOnClickListener(v -> {
            passImageView.setVisibility(View.INVISIBLE);
            if(!passwordChanged) {
                passwordChanged = true;
                passwordEditText.setText("");
                confirmPasswordEditText.setText("");
            }
        });
        passwordEditText.setOnFocusChangeListener((v, hasFocus) -> {
            passImageView.setVisibility(View.INVISIBLE);
            if(!passwordChanged) {
                passwordChanged = true;
                passwordEditText.setText("");
                confirmPasswordEditText.setText("");
            }
        });

        confirmPasswordEditText.setOnTouchListener((v, event) -> {
            confirmPassImageView.setVisibility(View.INVISIBLE);
            confirmNoPassImageView.setVisibility(View.INVISIBLE);
            return false;
        });
        confirmPasswordEditText.setOnClickListener(v -> {
            confirmPassImageView.setVisibility(View.INVISIBLE);
            confirmNoPassImageView.setVisibility(View.INVISIBLE);
        });
        confirmNoPassImageView.setOnFocusChangeListener((v, hasFocus) -> {
            confirmPassImageView.setVisibility(View.INVISIBLE);
            confirmNoPassImageView.setVisibility(View.INVISIBLE);
        });

        updateImageButton = $(R.id.updateImageButton);
        updateImageButton.setOnClickListener(v -> {
            if(passwordChanged) {
                if(passImageView.getVisibility()!=View.VISIBLE || confirmPassImageView.getVisibility()!=View.VISIBLE) return;

                fName = fNameEditText.getText().toString();
                lName = lNameEditText.getText().toString();
                email = emailEditText.getText().toString();
                hashedPassword = sha256(passwordEditText.getText().toString());

                final EditText et = new EditText(this);
                et.setTransformationMethod(PasswordTransformationMethod.getInstance()); //password inputtype
                new AlertDialog.Builder(this)
                        .setTitle("You are changing password. Please input old password to verify. ")
                        .setView(et)
                        .setPositiveButton("Confirm", (dialog, which) -> {
                            hideSoftInput(v.getWindowToken());
                            String input = et.getText().toString();
                            if(sha256(input).equals(sp.getString("HASHED_PASSWORD", ""))) {
                                Toast.makeText(this, "Updating, please wait...", Toast.LENGTH_SHORT).show();
                                SharedPreferences.Editor editor = sp.edit()
                                        .putString("HASHED_PASSWORD", hashedPassword)
                                        .putInt("PASSWORD_LENGTH", passwordEditText.getText().toString().length())
                                        .putBoolean("REMEMBER_PASSWORD", false)
                                        .putBoolean("AUTO_LOGIN", false);
                                editor.commit();
                                update(fName, lName, email, hashedPassword);
                            } else {
                                Toast.makeText(this, "Old password incorrect! ", Toast.LENGTH_SHORT).show();
                            }
                        }).setNegativeButton("Cancel", ((dialog, which) -> hideSoftInput(v.getWindowToken())))
                        .show();
            } else {
                fName = fNameEditText.getText().toString();
                lName = lNameEditText.getText().toString();
                email = emailEditText.getText().toString();
                update(fName, lName, email);
            }
        });

    }

    private void update(String fName, String lName, String email, String hashedPassword) {
        UserInfo.setfName(fName);
        UserInfo.setlName(lName);
        UserInfo.setUserEmail(email);

        //db


        startActivity(new Intent(this, LoginActivity.class));
    }

    private void update(String fName, String lName, String email) {
        UserInfo.setfName(fName);
        UserInfo.setlName(lName);
        UserInfo.setUserEmail(email);

        //db


        startActivity(new Intent(this, VehicleActivity.class));
    }

    public static String sha256(String base) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(base.getBytes("UTF-8"));
            StringBuffer hexString = new StringBuffer();

            for (int i = 0; i < hash.length; i++) {
                String hex = Integer.toHexString(0xff & hash[i]);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private <T extends View> T $(int id) {
        return (T) findViewById(id);
    }

    public boolean dispatchTouchEvent(MotionEvent ev) {
        View v = getCurrentFocus();
        if (isShouldHideInput(v, ev)) {
            hideSoftInput(v.getWindowToken());
            if(passwordChanged) {
                if (passwordEditText.getText().toString().length() > 0) {
                    if (passwordEditText.getText().toString().length() < 8) {
                        Toast.makeText(this, "Password is too short. It should be at least 8 characters. ", Toast.LENGTH_SHORT).show();
                    } else {
                        passImageView.setVisibility(View.VISIBLE);
                    }
                }
                if (confirmPasswordEditText.getText().toString().length() > 0) {
                    if (confirmPasswordEditText.getText().toString().equals(passwordEditText.getText().toString())) {
                        confirmPassImageView.setVisibility(View.VISIBLE);
                        confirmNoPassImageView.setVisibility(View.INVISIBLE);
                    } else {
                        confirmNoPassImageView.setVisibility(View.VISIBLE);
                        confirmPassImageView.setVisibility(View.INVISIBLE);
                    }
                }
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    private boolean isShouldHideInput(View v, MotionEvent event) {
        if (v instanceof EditText) {
            int[] l = {0, 0};
            v.getLocationInWindow(l);
            int left = l[0],
                    top = l[1],
                    bottom = top + v.getHeight(),
                    right = left + v.getWidth();
            return !(event.getX() > left && event.getX() < right
                    && event.getY() > top && event.getY() < bottom);
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
}
