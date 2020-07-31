package com.wisecarCompany.wisecarapp.user;

import android.graphics.Bitmap;

import com.wisecarCompany.wisecarapp.function.driverLog.CurrDriverLog;
import com.wisecarCompany.wisecarapp.user.vehicle.Vehicle;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class UserInfo {

    private static String userID;
    private static String username;
    private static String fName;
    private static String lName;
    private static String userEmail;
    private static Bitmap userImg;
    private static Map<String, Vehicle> vehicles; //key: ID
    private static CurrDriverLog currLog;
    private static Map<Date, String[]> notices;   //value: {regNo, noticeContent}
    private static Map<Date, String[]> emerNotices;

    public static void clear() {
        setUserID(null);
        setUsername(null);
        setUserEmail(null);
        setUserImg(null);
        setVehicles(null);
        setCurrLog(null);
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

    public static String getfName() {
        return fName;
    }

    public static void setfName(String fName) {
        UserInfo.fName = fName;
    }

    public static String getlName() {
        return lName;
    }

    public static void setlName(String lName) {
        UserInfo.lName = lName;
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

    public static CurrDriverLog getCurrLog() {
        return currLog;
    }

    public static void setCurrLog(CurrDriverLog currLog) {
        UserInfo.currLog = currLog;
    }

    public static Map<Date, String[]> getNotices() {
        return notices;
    }

    public static void setNotices(Map<Date, String[]> notices) {
        UserInfo.notices = notices;
    }

    public static Map<Date, String[]> getEmerNotices() {
        return emerNotices;
    }

    public static void setEmerNotices(Map<Date, String[]> emerNotices) {
        UserInfo.emerNotices = emerNotices;
    }
}
