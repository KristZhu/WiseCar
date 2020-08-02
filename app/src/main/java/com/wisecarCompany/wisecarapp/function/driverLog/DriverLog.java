package com.wisecarCompany.wisecarapp.function.driverLog;

import android.graphics.Bitmap;


import com.wisecarCompany.wisecarapp.user.UserInfo;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DriverLog implements Comparable<DriverLog> {

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

    private String companyName = "";
    private Bitmap companyLogo;

    private String timestamp = "";

    private boolean sentBefore;
    private String id;
    private String registrationNo;

    private String emailAddress;

    public DriverLog(CurrDriverLog currLog) {
        vehicleID = currLog.getVehicleID();
        custID = currLog.getCustID();
        startTime = currLog.getStartTime();
        claimRate = currLog.getClaimRate();
        km = currLog.getKm();
        countPause = currLog.getCountPause();
        mins = (int) currLog.getDuration() / 60;

        DateFormat formatDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String StringJSON = "[";
        for (Date date : currLog.getLocations().keySet()) {
            StringJSON += "{\"" + formatDate.format(date) + "\":\"" + currLog.getLocations().get(date)[0] + "," + currLog.getLocations().get(date)[1] + "\"},";
        }
        StringJSON = StringJSON.substring(0, StringJSON.length() - 1);
        StringJSON += "]";
        setLogJSON(StringJSON);

        setEndTime(new Date());
        setTimestamp(new Date().getTime() + "");
    }


    public DriverLog(String id, String registrationNo, Date startTime, Date endTime, double km, String companyName, boolean sentBefore) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.km = km;
        this.companyName = companyName!=null && companyName.equals("null") ? null : companyName;
        this.sentBefore = sentBefore;
        this.id = id;
        this.registrationNo = registrationNo.replaceAll("\r\n|\r|\n", "");
    }

    public DriverLog(String registrationNo, Date startTime, Date endTime, double km, int mins, String companyName) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.km = km;
        this.mins = mins;
        this.companyName = companyName!=null && companyName.equals("null") ? null : companyName;
        this.registrationNo = registrationNo.replaceAll("\r\n|\r|\n", "");
    }

    public DriverLog(String vehicleID, String custID, Date startTime, double claimRate, String shareID, String companyName, Bitmap companyLogo) {
        this.vehicleID = vehicleID;
        this.custID = custID;
        this.startTime = startTime;
        this.claimRate = claimRate;
        this.shareID = shareID;
        this.companyName = companyName!=null && companyName.equals("null") ? null : companyName;
        this.companyLogo = companyLogo;
    }

    public DriverLog(String vehicleID, String custID, Date startTime, double claimRate, String shareID, String companyName, Bitmap companyLogo, String timestamp) {
        this.vehicleID = vehicleID;
        this.custID = custID;
        this.startTime = startTime;
        this.claimRate = claimRate;
        this.shareID = shareID;
        this.companyName = companyName!=null && companyName.equals("null") ? null : companyName;
        this.companyLogo = companyLogo;
        this.timestamp = timestamp;
    }

    public DriverLog(String vehicleID, Date startTime, Date endTime, int countPause, int mins, double km, double claimRate, String shareID, String custID, String companyName, Bitmap companyLogo, String logJSON, String timestamp) {
        this.vehicleID = vehicleID;
        this.startTime = startTime;
        this.endTime = endTime;
        this.countPause = countPause;
        this.mins = mins;
        this.km = km;
        this.claimRate = claimRate;
        this.shareID = shareID;
        this.custID = custID;
        this.companyName = companyName!=null && companyName.equals("null") ? null : companyName;
        this.companyLogo = companyLogo;
        this.logJSON = logJSON;
        this.timestamp = timestamp;
    }

    public DriverLog(String vehicleID, Date startTime, Date endTime, int countPause, int mins, double km, String logJSON, String timestamp) {
        this.vehicleID = vehicleID;
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
        this.companyName = companyName!=null && companyName.equals("null") ? null : companyName;
    }

    public Bitmap getCompanyLogo() {
        return companyLogo;
    }

    public void setCompanyLogo(Bitmap companyLogo) {
        this.companyLogo = companyLogo;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isSentBefore() {
        return sentBefore;
    }

    public void setSentBefore(boolean sentBefore) {
        this.sentBefore = sentBefore;
    }

    public String getRegistrationNo() {
        return registrationNo.replaceAll("\r\n|\r|\n", "");
    }

    public void setRegistrationNo(String registrationNo) {
        this.registrationNo = registrationNo.replaceAll("\r\n|\r|\n", "");
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    @Override
    public String toString() {
        return "DriverLog{" +
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
                ", sentBefore=" + sentBefore +
                ", id='" + id + '\'' +
                ", registrationNo='" + registrationNo + '\'' +
                ", emailAddress='" + emailAddress + '\'' +
                '}';
    }

    @Override
    public int compareTo(DriverLog o) {
        try {
            if (Long.parseLong(o.getTimestamp()) - Long.parseLong(this.getTimestamp())>0) return 1;
            else if (Long.parseLong(o.getTimestamp()) - Long.parseLong(this.getTimestamp())<0) return -1;
            else return 0;
        } catch (Exception e) {
            return o.getTimestamp().compareTo(this.getTimestamp());
        }
    }
}
