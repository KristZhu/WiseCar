package com.example.wisecarapp;

import android.graphics.Bitmap;


import java.security.spec.ECField;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

class RecordLog implements Comparable<RecordLog> {

    private String vehicleID;
    private String custID;
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

    private String timestamp = "";

    private boolean pausing = false;

    public RecordLog(String vehicleID, String custID, Date startTime, double claimRate, String shareID, String companyName, Bitmap companyLogo) {
        this.vehicleID = vehicleID;
        this.custID = custID;
        this.startTime = startTime;
        this.claimRate = claimRate;
        this.shareID = shareID;
        this.companyName = companyName;
        this.companyLogo = companyLogo;
    }

    public RecordLog(String vehicleID, String custID, Date startTime, double claimRate, String shareID, String companyName, Bitmap companyLogo, String timestamp) {
        this.vehicleID = vehicleID;
        this.custID = custID;
        this.startTime = startTime;
        this.claimRate = claimRate;
        this.shareID = shareID;
        this.companyName = companyName;
        this.companyLogo = companyLogo;
        this.timestamp = timestamp;
    }

    public RecordLog(Date startTime, Date endTime, boolean pausing, int countPause, int mins, double km, double claimRate, String shareID, String custID, String companyName, Bitmap companyLogo, String logJSON) {
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

    public RecordLog(Date startTime, Date endTime, int countPause, int mins, double km, double claimRate, String shareID, String custID, String companyName, Bitmap companyLogo, String logJSON, String timestamp) {
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
        this.timestamp = timestamp;
    }

    public RecordLog(Date startTime, Date endTime, int countPause, int mins, double km, String logJSON, String timestamp) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.countPause = countPause;
        this.mins = mins;
        this.km = km;
        this.logJSON = logJSON;
        this.timestamp = timestamp;
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
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM", Locale.getDefault());
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            return dateFormat.format(startTime)
                    + "  "
                    + timeFormat.format(startTime)
                    + " to "
                    + timeFormat.format(endTime)
                    + ", "
                    + km
                    + "KM";
        } catch (Exception e) {
            return "RecordLog{" +
                    "vehicleID='" + vehicleID + '\'' +
                    ", custID='" + custID + '\'' +
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
                    ", timestamp='" + timestamp + '\'' +
                    ", pausing=" + pausing +
                    '}';
        }
    }

    @Override
    public int compareTo(RecordLog o) {
        try {
            if (Long.parseLong(o.getTimestamp()) - Long.parseLong(this.getTimestamp())>0) return 1;
            else if (Long.parseLong(o.getTimestamp()) - Long.parseLong(this.getTimestamp())<0) return -1;
            else return 0;
        } catch (Exception e) {
            return o.getTimestamp().compareTo(this.getTimestamp());
        }
    }
}
