package com.example.wisecarapp;

import android.graphics.Bitmap;

import java.util.Date;

class Licence {
    private boolean active;
    private String number;
    private String type;
    private Date startDate;
    private Date expiryDate;
    private boolean remind;
    private Bitmap licenceImg;

    public Licence(boolean active, String number, String type, Date startDate, Date expiryDate, boolean remind) {
        this.active = active;
        this.number = number;
        this.type = type;
        this.startDate = startDate;
        this.expiryDate = expiryDate;
        this.remind = remind;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(Date expiryDate) {
        this.expiryDate = expiryDate;
    }

    public boolean isRemind() {
        return remind;
    }

    public void setRemind(boolean remind) {
        this.remind = remind;
    }

    public Bitmap getLicenceImg() {
        return licenceImg;
    }

    public void setLicenceImg(Bitmap licenceImg) {
        this.licenceImg = licenceImg;
    }
}
