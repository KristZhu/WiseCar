package com.example.wisecarapp;

import android.graphics.Bitmap;

import java.util.Date;

class RecordLog {

    private Date date;
    private Date startTime;
    private Date endTime;
    private boolean pausing = false;
    private int countPause = 0;
    private int mins = 0;
    private double km = 0;

    private double rate; // For reimbursement. Different for each company.
    private String logJSON; // timestamp and location every 30 or 20 secs.

    private String shareID;
    private String custID;
    private String companyName;
    private Bitmap companyLogo;


    public RecordLog(Date date, Date startTime) {
        this.date = date;
        this.startTime = startTime;
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

    public RecordLog(Date date, Date startTime, Date endTime, boolean pausing, int countPause, int mins, double km, double rate, String shareID, String custID, String companyName, Bitmap companyLogo, String logJSON) {
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.pausing = pausing;
        this.countPause = countPause;
        this.mins = mins;
        this.km = km;
        this.rate = rate;
        this.shareID = shareID;
        this.custID = custID;
        this.companyName = companyName;
        this.companyLogo = companyLogo;
        this.logJSON = logJSON;
    }

    public RecordLog(Date date, Date startTime, Date endTime, int countPause, int mins, double km, double rate, String shareID, String custID, String companyName, Bitmap companyLogo, String logJSON) {
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.countPause = countPause;
        this.mins = mins;
        this.km = km;
        this.rate = rate;
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

    public boolean isPausing() {
        return pausing;
    }

    public void setPausing(boolean pausing) {
        this.pausing = pausing;
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

    public double getKm() {
        return km;
    }

    public void setKm(double km) {
        this.km = km;
    }

    public String getShareID() {
        return shareID;
    }

    public void setShareID(String shareID) {
        this.shareID = shareID;
    }

    public String getCustID() {
        return custID;
    }

    public void setCustID(String custID) {
        this.custID = custID;
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

    public double getRate() {
        return rate;
    }

    public void setRate(double rate) {
        this.rate = rate;
    }

    public String getLogJSON() {
        return logJSON;
    }

    public void setLogJSON(String logJSON) {
        this.logJSON = logJSON;
    }

    @Override
    public String toString() {
        return "RecordLog{" +
                "date=" + date +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", pausing=" + pausing +
                ", countPause=" + countPause +
                ", mins=" + mins +
                ", km=" + km +
                ", shareID='" + shareID + '\'' +
                ", custID='" + custID + '\'' +
                ", companyName='" + companyName + '\'' +
                ", rate='" + rate + '\'' +
                ", logJSON='" + logJSON + '\'' +
                '}';
    }
}
