package com.wisecarCompany.wisecarapp.user;

import com.wisecarCompany.wisecarapp.function.driverLog.CurrDriverLog;
import com.wisecarCompany.wisecarapp.user.vehicle.Vehicle;


public class UserInfo {     //try to delete it totally in the future. Use SharePreference of query DB

    private static Vehicle currVehicle;
    private static Vehicle newVehicle;
    private static CurrDriverLog currLog;

    public static void clear() {
        setNewVehicle(null);
        setCurrVehicle(null);
        setCurrLog(null);
    }

    public static Vehicle getCurrVehicle() {
        return currVehicle;
    }

    public static void setCurrVehicle(Vehicle currVehicle) {
        UserInfo.currVehicle = currVehicle;
    }

    public static Vehicle getNewVehicle() {
        return newVehicle;
    }

    public static void setNewVehicle(Vehicle newVehicle) {
        UserInfo.newVehicle = newVehicle;
    }

    public static CurrDriverLog getCurrLog() {
        return currLog;
    }

    public static void setCurrLog(CurrDriverLog currLog) {
        UserInfo.currLog = currLog;
    }

}
