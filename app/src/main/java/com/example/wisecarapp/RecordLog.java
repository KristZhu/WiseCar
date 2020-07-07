package com.example.wisecarapp;

import android.graphics.Bitmap;

import java.util.Date;

class RecordLog {

    private Date date;
    private Date startTime;
    private Date endTime;
    private int recording;  //0:end; 1:recording; 2:pause
    private int countPause;
    private int mins;
    private double km;
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
        this.recording = 0;
        this.countPause = 1;
        this.mins = 10;
        this.km = 2.7;
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

    public int getRecording() {
        return recording;
    }

    public void setRecording(int recording) {
        this.recording = recording;
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

    @Override
    public String toString() {
        return "RecordLog{" +
                "date=" + date +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", recording=" + recording +
                ", countPause=" + countPause +
                ", mins=" + mins +
                ", km=" + km +
                ", custID='" + custID + '\'' +
                ", companyName='" + companyName + '\'' +
                '}';
    }
}
