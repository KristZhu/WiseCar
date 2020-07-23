package com.wisecarCompany.wisecarapp.function.parkingReceipt;

import java.util.Date;

public class ParkingReceipt {

    private String id;
    private String registrationNo;
    private Date date;
    private String refNo;
    private String companyName;
    private boolean sentBefore;

    private double hours;
    private double fees;
    private String notes;
    private String url;

    public ParkingReceipt(String id, String registrationNo, Date date, String refNo, String companyName, boolean sentBefore) {
        this.id = id;
        this.registrationNo = registrationNo;
        this.date = date;
        this.refNo = refNo;
        this.companyName = companyName;
        this.sentBefore = sentBefore;
    }

    public ParkingReceipt(String id, String refNo, Date date, double hours, double fees, String notes, String companyName, String url) {
        this.id = id;
        this.date = date;
        this.refNo = refNo;
        this.companyName = companyName;
        this.hours = hours;
        this.fees = fees;
        this.notes = notes;
        this.url = url;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRegistrationNo() {
        return registrationNo;
    }

    public void setRegistrationNo(String registrationNo) {
        this.registrationNo = registrationNo;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getRefNo() {
        return refNo;
    }

    public void setRefNo(String refNo) {
        this.refNo = refNo;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public boolean isSentBefore() {
        return sentBefore;
    }

    public void setSentBefore(boolean sentBefore) {
        this.sentBefore = sentBefore;
    }

    public double getHours() {
        return hours;
    }

    public void setHours(double hours) {
        this.hours = hours;
    }

    public double getFees() {
        return fees;
    }

    public void setFees(double fees) {
        this.fees = fees;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
