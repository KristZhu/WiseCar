package com.wisecarCompany.wisecarapp.user;

import com.wisecarCompany.wisecarapp.function.driverLog.CurrDriverLog;
import com.wisecarCompany.wisecarapp.user.vehicle.Vehicle;

import java.util.Map;

public class UserInfo {     //try to delete it totally in the future. Use SharePreference of query DB

    private static Map<String, Vehicle> vehicles; //key: ID
    private static CurrDriverLog currLog;

    public static void clear() {
        setVehicles(null);
        setCurrLog(null);
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

}
