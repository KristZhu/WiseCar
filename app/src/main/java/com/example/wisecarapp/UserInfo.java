package com.example.wisecarapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

class UserInfo {
    private static String username;
    private static String userEmail;
    private static List<Vehicle> vehicles;


    protected static List<Vehicle> user_Vehicles;
    private String email_address;
    private String user_name;
    private Bitmap ImgBitmap;

    public static String getUsername() {
        return username;
    }

    public static void setUsername(String username) {
        UserInfo.username = username;
    }

    public static String getUserEmail() {
        return userEmail;
    }

    public static void setUserEmail(String userEmail) {
        UserInfo.userEmail = userEmail;
    }

    public static List<Vehicle> getVehicles() {
        return vehicles;
    }

    public static void setVehicles(List<Vehicle> vehicles) {
        UserInfo.vehicles = vehicles;
    }

    public static boolean addVehicle(Vehicle vehicle) {
        return vehicles.add(vehicle);
    }

    /*
    public static boolean deleteVehicle() {

    }

    public Vehicle getVehicle() {

    }

    

     */

}
