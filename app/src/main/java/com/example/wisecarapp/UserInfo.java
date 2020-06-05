package com.example.wisecarapp;

import android.graphics.Bitmap;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

class UserInfo {

    private static String userID;
    private static String username;
    private static String userEmail;
    private static Bitmap userImg;
    private static Map<String, Vehicle> vehicles = new TreeMap<>((o1, o2) -> o2.compareTo(o1));   //key: ID

    public static void clear() {
        setUserID(null);
        setUsername(null);
        setUserEmail(null);
        setUserImg(null);
        setVehicles(null);
    }

    public static String getUserID() {
        return userID;
    }

    public static void setUserID(String userID) {
        UserInfo.userID = userID;
    }

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

    public static Bitmap getUserImg() {
        return userImg;
    }

    public static void setUserImg(Bitmap userImg) {
        UserInfo.userImg = userImg;
    }

    public static Map<String, Vehicle> getVehicles() {
        return vehicles;
    }

    public static void setVehicles(Map<String, Vehicle> vehicles) {
        UserInfo.vehicles = vehicles;
    }

}
