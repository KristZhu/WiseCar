package com.wisecarCompany.wisecarapp.function.driverLog;

import android.graphics.Bitmap;

import com.wisecarCompany.wisecarapp.user.vehicle.Vehicle;

import java.util.Date;
import java.util.Map;
import java.util.Timer;
import java.util.TreeMap;

public class CurrDriverLog {

    private boolean pausing = false;
    private double latitude = 0.0;
    private double longitude = 0.0;
    private long duration = 0;  //total recording time (not counting time) in second
    private Map<Date, double[]> locations = new TreeMap<>();  //location every 30s, time:[lat,lng]

    private Vehicle vehicle;

    private String custID;
    private Date startTime;
    private double claimRate;
    private String shareID;
    private String companyName = "";
    private Bitmap companyLogo;

    private int countPause = 0;
    private double km = 0;
    private boolean timerRunning = false;
    private Timer timer;


    private static CurrDriverLog currLog;

    public static void clearInstance() {
        currLog = null;
    }

    private CurrDriverLog(Vehicle vehicle, String custID, Date startTime, double claimRate, String shareID, String companyName, Bitmap companyLogo) {
        this.vehicle = vehicle;
        this.custID = custID;
        this.startTime = startTime;
        this.claimRate = claimRate;
        this.shareID = shareID;
        this.companyName = companyName;
        this.companyLogo = companyLogo;
    }

    public static CurrDriverLog getInstance(Vehicle vehicle, String custID, Date startTime, double claimRate, String shareID, String companyName, Bitmap companyLogo) {
        currLog = new CurrDriverLog(vehicle, custID, startTime, claimRate, shareID, companyName, companyLogo);
        return currLog;
    }

    public static CurrDriverLog getInstance() {
        return currLog;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public void setVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
    }

    public boolean isPausing() {
        return pausing;
    }

    public void setPausing(boolean pausing) {
        this.pausing = pausing;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public Map<Date, double[]> getLocations() {
        return locations;
    }

    public void setLocations(Map<Date, double[]> locations) {
        this.locations = locations;
    }

    public String getCustID() {
        return custID;
    }

    public void setCustID(String custID) {
        this.custID = custID;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public double getClaimRate() {
        return claimRate;
    }

    public void setClaimRate(double claimRate) {
        this.claimRate = claimRate;
    }

    public String getShareID() {
        return shareID;
    }

    public void setShareID(String shareID) {
        this.shareID = shareID;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public Bitmap getCompanyLogo() {
        return companyLogo;
    }

    public void setCompanyLogo(Bitmap companyLogo) {
        this.companyLogo = companyLogo;
    }

    public int getCountPause() {
        return countPause;
    }

    public void setCountPause(int countPause) {
        this.countPause = countPause;
    }

    public double getKm() {
        return km;
    }

    public void setKm(double km) {
        this.km = km;
    }

    public boolean isTimerRunning() {
        return timerRunning;
    }

    public void setTimerRunning(boolean timerRunning) {
        this.timerRunning = timerRunning;
    }

    public Timer getTimer() {
        return timer;
    }

    public void setTimer(Timer timer) {
        this.timer = timer;
    }

    @Override
    public String toString() {
        return "CurrDriverLog{" +
                "pausing=" + pausing +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", duration=" + duration +
                ", locations=" + locations +
                ", vehicleID='" + vehicle.getVehicle_id() + '\'' +
                ", regNo='" + vehicle.getRegistration_no() + '\'' +
                ", custID='" + custID + '\'' +
                ", startTime=" + startTime +
                ", claimRate=" + claimRate +
                ", shareID='" + shareID + '\'' +
                ", companyName='" + companyName + '\'' +
                ", companyLogo=" + companyLogo +
                ", countPause=" + countPause +
                ", km=" + km +
                ", timerRunning=" + timerRunning +
                '}';
    }
}
