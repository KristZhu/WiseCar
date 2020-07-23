package com.wisecarCompany.wisecarapp.function.fuelReceipt;

import java.util.Date;

public class FuelReceipt {

    private String id;
    private String registrationNo;
    private Date date;
    private String ref;
    private String companyName;
    private boolean sentBefore;

    private String type;
    private double fuelAmount;
    private double paidAmount;
    private String url;

    public FuelReceipt(String id, String registrationNo, Date date, String ref, String companyName, boolean sentBefore) {
        this.id = id;
        this.registrationNo = registrationNo;
        this.date = date;
        this.ref = ref;
        this.companyName = companyName;
        this.sentBefore = sentBefore;
    }

    public FuelReceipt(String id, String ref, Date date, String type, double fuelAmount, double paidAmount, String companyName, String url) {
        this.id = id;
        this.date = date;
        this.ref = ref;
        this.companyName = companyName;
        this.type = type;
        this.fuelAmount = fuelAmount;
        this.paidAmount = paidAmount;
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

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getFuelAmount() {
        return fuelAmount;
    }

    public void setFuelAmount(double fuelAmount) {
        this.fuelAmount = fuelAmount;
    }

    public double getPaidAmount() {
        return paidAmount;
    }

    public void setPaidAmount(double paidAmount) {
        this.paidAmount = paidAmount;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
