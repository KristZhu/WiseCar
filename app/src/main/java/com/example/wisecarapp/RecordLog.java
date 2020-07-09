package com.example.wisecarapp;

import android.graphics.Bitmap;


import java.util.Date;
import java.util.List;

class RecordLog {

    private String vehicleID;
    private String custID;
    private Date date;
    private Date startTime;
    private Date endTime;
    private double claimRate;
    private double km = 0;
    private int countPause = 0;
    private int mins = 0;
    private String logJSON;   //JSONArray
    private String shareID;

    private String companyName;
    private Bitmap companyLogo;

    private String timestamp;

    private boolean pausing = false;

    public RecordLog(String vehicleID, String custID, Date date, Date startTime, double claimRate, String shareID, String companyName, Bitmap companyLogo) {
        this.vehicleID = vehicleID;
        this.custID = custID;
        this.date = date;
        this.startTime = startTime;
        this.claimRate = claimRate;
        this.shareID = shareID;
        this.companyName = companyName;
        this.companyLogo = companyLogo;
    }

    public RecordLog(String vehicleID, String custID, Date date, Date startTime, double claimRate, String shareID, String companyName, Bitmap companyLogo, String timestamp) {
        this.vehicleID = vehicleID;
        this.custID = custID;
        this.date = date;
        this.startTime = startTime;
        this.claimRate = claimRate;
        this.shareID = shareID;
        this.companyName = companyName;
        this.companyLogo = companyLogo;
        this.timestamp = timestamp;
    }

    public RecordLog() {
        this.date = new Date();
        this.startTime = new Date(1000);
        this.endTime = new Date(1000000);
        this.pausing = false;
        this.countPause = 1;
        this.mins = 10;
        this.km = 2.7;
    }

    public RecordLog(Date date, Date startTime, Date endTime, boolean pausing, int countPause, int mins, double km, double claimRate, String shareID, String custID, String companyName, Bitmap companyLogo, String logJSON) {
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.pausing = pausing;
        this.countPause = countPause;
        this.mins = mins;
        this.km = km;
        this.claimRate = claimRate;
        this.shareID = shareID;
        this.custID = custID;
        this.companyName = companyName;
        this.companyLogo = companyLogo;
    }

    public RecordLog(Date date, Date startTime, Date endTime, int countPause, int mins, double km, double claimRate, String shareID, String custID, String companyName, Bitmap companyLogo, String logJSON) {
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.countPause = countPause;
        this.mins = mins;
        this.km = km;
        this.claimRate = claimRate;
        this.shareID = shareID;
        this.custID = custID;
        this.companyName = companyName;
        this.companyLogo = companyLogo;
        this.logJSON = logJSON;
    }

    public RecordLog(Date date, Date startTime, Date endTime, int countPause, int mins, double km, String logJSON) {
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.countPause = countPause;
        this.mins = mins;
        this.km = km;
        this.logJSON = logJSON;
    }

    public String getVehicleID() {
        return vehicleID;
    }

    public void setVehicleID(String vehicleID) {
        this.vehicleID = vehicleID;
    }

    public String getCustID() {
        return custID;
    }

    public void setCustID(String custID) {
        this.custID = custID;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public double getClaimRate() {
        return claimRate;
    }

    public void setClaimRate(double claimRate) {
        this.claimRate = claimRate;
    }

    public double getKm() {
        return km;
    }

    public void setKm(double km) {
        this.km = km;
    }

    public int getCountPause() {
        return countPause;
    }

    public void setCountPause(int countPause) {
        this.countPause = countPause;
    }

    public int getMins() {
        return mins;
    }

    public void setMins(int mins) {
        this.mins = mins;
    }

    public String getLogJSON() {
        return logJSON;
    }

    public void setLogJSON(String logJSON) {
        this.logJSON = logJSON;
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

    public boolean isPausing() {
        return pausing;
    }

    public void setPausing(boolean pausing) {
        this.pausing = pausing;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "RecordLog{" +
                "vehicleID='" + vehicleID + '\'' +
                ", custID='" + custID + '\'' +
                ", date=" + date +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", claimRate=" + claimRate +
                ", km=" + km +
                ", countPause=" + countPause +
                ", mins=" + mins +
                ", logJSON='" + logJSON + '\'' +
                ", shareID='" + shareID + '\'' +
                ", companyName='" + companyName + '\'' +
                ", companyLogo=" + companyLogo +
                ", pausing=" + pausing +
                ", timestamp=" + timestamp +
                '}';
    }
}
